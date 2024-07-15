# Summary
JPA를 이용한 MySQL PK 성능 테스트 및 테스트 수행 속도 개선

## MySQL PK 성능 테스트
### Core concept
#### Insertion test
```mermaid
sequenceDiagram
    participant InsertTest
    participant StopWatch
    participant EntityManager
    participant Database

    loop "For loop or repeated test..."
        InsertTest->>InsertTest: create entity instance
        InsertTest->>StopWatch: start
        activate StopWatch
        InsertTest->>EntityManager: persist(entity)
        activate EntityManager
        EntityManager->>EntityManager: persist entity
        EntityManager->>Database: commit
        deactivate EntityManager
        Database-->>EntityManager: committed
        EntityManager-->>InsertTest: committed
        InsertTest->>StopWatch: stop
        deactivate StopWatch
    end
```
- 쓰기 지연이 아닌 즉시 commit이 일어나야 함
- 선언적 transactional 적용 시 주의
### Reports
#### 1000 times (Single thread)

![](./docs/resources/1000_summary_full.png)
![](./docs/resources/1000_summary_1.png)

## 테스트 수행 속도 개선
1. N threads per method + for loop (n=3, 15 thread)
    - n = ForkJoinPool thread 개수 / Test method 개수
    - test 환경 n = 15 / 5 = 3
2. Thread per method + for loop (5 thread)
3. Single thread (1 thread)

### 총 수행 시간 측정 방법
`TestExecutionListeners`에 `DefaultTestTimeExecutionListener`를 추가
#### DefaultTestTimeExecutionListener
```mermaid
sequenceDiagram
    actor Junit
    participant DefaultTestTimeExecutionListener
    participant StopWatch
    participant Test Class
    
    Junit ->>+ DefaultTestTimeExecutionListener: beforeTest
        DefaultTestTimeExecutionListener ->>+ StopWatch: start
            StopWatch -->>+ StopWatch: : start
    DefaultTestTimeExecutionListener -->>- Junit: notify

    Junit ->>+ Test Class: execute beforeAll
        Test Class ->> Test Class: Run beforeAll
    Test Class -->>- Junit: notify
    
    Junit ->>+ Test Class: execute test method
        Test Class ->> Test Class: Run test method
    Test Class -->>- Junit: notify

    Junit -->>+ Test Class: execute afterAll
        Test Class -->> Test Class:  Run afterAll
    Test Class -->>- Junit: notify

    Junit ->>+ DefaultTestTimeExecutionListener: afterTest
         DefaultTestTimeExecutionListener ->> StopWatch: stop
         StopWatch -->>- StopWatch: : stop
    DefaultTestTimeExecutionListener ->> DefaultTestTimeExecutionListener: pretty print StopWatch
    DefaultTestTimeExecutionListener -->>- Junit: notify
```

### N threads per method + for loop
- Concurrency로 설정 후 RepeatedTest시 repeatedTime 마다 서로 다른 thread에서 수행되는 것을 이용
  - `@ReapeatedTest(value)`에 할당하고자 하는 thread 개수 기입
  - 원하는 insertion 개수 / `@ReapeatedTest(value)` 만큼 for-loop의 iteration 기입
- 각 thread에서 호출되는 insertionTest끼리 순서를 알 수 없음
  - 어떤 entity가 먼저 삽입될지 알 수 없음
  - persist 시간 역시 측정 대상이므로 StopWatch의 task name으로 pk를 지정 불가
  - PK와 TaskInfo를 key - value로 map에 넣음
  - entity에는 created at column 추가, 해당 정보는 database에서 insert 시점에 작성하도록 함
- Thread local map 에 넣은 후 iteration이 끝나면 ConcurrentMap에 putAll
  - ConcurrentMap에 각 iteration마다 Pk-task mapping 정보 삽입 시 lock이 걸려 성능 저하가 발생할 것이라고 예상
  - for-loop 안에서 thread local map에 넣어두었다가 loop가 끝나면 ConcurrentMap에 putAll
- OrderBy createdAt으로 읽어 온 후 해당 list를 순회하며 ConcurrentMap에서 해당 pk에 대한 TaskInfo를 가져옴
  - Taskinfo에서 nanoSecond 정보를 가져와 csv 작성
#### Rough concept
@RepeatedTest(value = N = 2)
- 원하는 insert time이 100, n=2인 경우 loop iteration = 100 / 2 = 50
```mermaid
sequenceDiagram
    actor Junit
    participant PrimaryKeyPerformanceTestMultiThreadV2
    participant  PrimaryKeyPerformanceTestInternal
    par
        par repeated test for method 1 
            Junit ->>+ PrimaryKeyPerformanceTestMultiThreadV2: Execute test method 1
                PrimaryKeyPerformanceTestMultiThreadV2 ->> PrimaryKeyPerformanceTestMultiThreadV2: insertTest
                    loop: Measure time and save mapping info 
                        PrimaryKeyPerformanceTestMultiThreadV2 ->>+ PrimaryKeyPerformanceTestInternal: persisAndGetEntity
                        PrimaryKeyPerformanceTestInternal -->>- PrimaryKeyPerformanceTestMultiThreadV2: 
                    end
            PrimaryKeyPerformanceTestMultiThreadV2-->>- Junit: notify
        and
            Junit ->>+ PrimaryKeyPerformanceTestMultiThreadV2: Execute test method 1
                PrimaryKeyPerformanceTestMultiThreadV2 ->> PrimaryKeyPerformanceTestMultiThreadV2: insertTest
                    loop: Measure time and save mapping info
PrimaryKeyPerformanceTestMultiThreadV2 ->>+ PrimaryKeyPerformanceTestInternal: persisAndGetEntity
                        PrimaryKeyPerformanceTestInternal -->>- PrimaryKeyPerformanceTestMultiThreadV2: 
                    end
            PrimaryKeyPerformanceTestMultiThreadV2-->>- Junit: notify
        end
        
        par repeated test for method 2
            Junit ->>+ PrimaryKeyPerformanceTestMultiThreadV2: Execute test method 2
                PrimaryKeyPerformanceTestMultiThreadV2 ->> PrimaryKeyPerformanceTestMultiThreadV2: insertTest
                loop: Measure time and save mapping info
PrimaryKeyPerformanceTestMultiThreadV2 ->>+ PrimaryKeyPerformanceTestInternal: persisAndGetEntity
                    PrimaryKeyPerformanceTestInternal -->>- PrimaryKeyPerformanceTestMultiThreadV2: 
                end                                                     
            PrimaryKeyPerformanceTestMultiThreadV2-->>- Junit: notify
        and
            Junit ->>+ PrimaryKeyPerformanceTestMultiThreadV2: Execute test method 1
                PrimaryKeyPerformanceTestMultiThreadV2 ->> PrimaryKeyPerformanceTestMultiThreadV2: insertTest
                loop: Measure time and save mapping info
PrimaryKeyPerformanceTestMultiThreadV2 ->>+ PrimaryKeyPerformanceTestInternal: persisAndGetEntity
                    PrimaryKeyPerformanceTestInternal -->>- PrimaryKeyPerformanceTestMultiThreadV2: 
                end
            PrimaryKeyPerformanceTestMultiThreadV2-->>- Junit: notify
        end
    end
```

#### Measure time and save mapping info

```mermaid
sequenceDiagram
    participant InsertTest
    participant Thread local StopWatch
    participant PrimaryKeyTestMultiThreadInternal
    participant Database
    participant Root ConcurrentMap
    participant ConcurrentMap

    InsertTest->> Root ConcurrentMap: get ConcurrentMap("entity class type")
    Root ConcurrentMap->> InsertTest: return ConcurrentMap("entity class type")
    InsertTest->> Thread local StopWatch: get StopWatch
    Thread local StopWatch->> InsertTest: return StopWatch
loop "Measure time and save mapping info"
        InsertTest->>InsertTest: create entity instance
        InsertTest->>Thread local StopWatch: start
        activate Thread local StopWatch
        InsertTest->>PrimaryKeyTestMultiThreadInternal: persist(entity)
        activate PrimaryKeyTestMultiThreadInternal
        PrimaryKeyTestMultiThreadInternal->>PrimaryKeyTestMultiThreadInternal: persist entity
        PrimaryKeyTestMultiThreadInternal->>Database: commit
        deactivate PrimaryKeyTestMultiThreadInternal
        Database-->>PrimaryKeyTestMultiThreadInternal: committed
        PrimaryKeyTestMultiThreadInternal-->>InsertTest: committed
        InsertTest->>Thread local StopWatch: stop
        deactivate Thread local StopWatch
        
        activate InsertTest
        InsertTest->> InsertTest: ThreadLocal map.put(entity.getId(), stopWatch.getLastTaskInfo());
        deactivate InsertTest
    end
    InsertTest->>+ ConcurrentMap: ConcurrentMap("entity class type").putAll(map)
    ConcurrentMap ->> ConcurrentMap: putAll
    ConcurrentMap ->>- InsertTest: 
```

[//]: # (### Thread per method + for loop)

[//]: # (sequenceDiagram)

[//]: # (    actor Junit)

[//]: # (    participant )

[//]: # (    actor Junit)

[//]: # (    participant PrimaryKeyPerformanceTestMultiThreadV1)

[//]: # (    participant ParallelTestTimeExecutionExportListener)

[//]: # (    participant DefaultTestTimeExecutionListener)

[//]: # (    participant PrimaryKeyPerformanceTestMultiThreadInternal)

[//]: # (    participant StopWatch)

[//]: # (    participant ThreadLocal StopWatch)

[//]: # ()
[//]: # ()
[//]: # (    par Jpa auto increment)

[//]: # (        Junit ->>+ ParallelTestTimeExecutionExportListener : create PrimaryKeyPerformanceTestMultiThreadV1)

[//]: # (        ParallelTestTimeExecutionExportListener ->>+ ParallelTestTimeExecutionExportListener: super.beforeTestClass)

[//]: # (        ParallelTestTimeExecutionExportListener ->>+ DefaultTestTimeExecutionListener: beforeTestClass)

[//]: # (        DefaultTestTimeExecutionListener ->>+ StopWatch: start )

[//]: # (        StopWatch -->> DefaultTestTimeExecutionListener: "")

[//]: # (        DefaultTestTimeExecutionListener ->>- ParallelTestTimeExecutionExportListener: "")

[//]: # (        ParallelTestTimeExecutionExportListener ->>- Junit: "")

[//]: # ()
[//]: # (        Junit ->>+ PrimaryKeyPerformanceTestMultiThreadV1 : jpaAutoIncrementWithCreatedTime)

[//]: # (        PrimaryKeyPerformanceTestMultiThreadV1 ->>+ PrimaryKeyPerformanceTestMultiThreadV1: insertTest)

[//]: # (        loop: iteration = insert time)

[//]: # (            PrimaryKeyPerformanceTestMultiThreadV1 ->>+ PrimaryKeyPerformanceTestMultiThreadInternal: persistEntiy)

[//]: # (            PrimaryKeyPerformanceTestMultiThreadInternal -->>- PrimaryKeyPerformanceTestMultiThreadV1: "")

[//]: # (        end)

[//]: # (        PrimaryKeyPerformanceTestMultiThreadV1 -->>- Junit: notify)

[//]: # (        Junit ->>+ ParallelTestTimeExecutionExportListener: afterTestClass)

[//]: # (        ParallelTestTimeExecutionExportListener ->>+ ParallelTestTimeExecutionExportListener: super.afterTestClass)

[//]: # (        ParallelTestTimeExecutionExportListener ->>+ DefaultTestTimeExecutionListener: afterTestClass)

[//]: # (        DefaultTestTimeExecutionListener ->>- StopWatch: stop )

[//]: # (       )
[//]: # (        )
[//]: # (     and Jpa sequence)

[//]: # (     Junit ->> PrimaryKeyPerformanceTestMultiThreadV1 : jpaSequenceWithCreatedTime)

[//]: # (     and UUIDV1)

[//]: # (     Junit ->> PrimaryKeyPerformanceTestMultiThreadV1 : uuidV1WithCreatedTime)

[//]: # (     and UUIDV4)

[//]: # (     Junit ->> PrimaryKeyPerformanceTestMultiThreadV1 : uuidV4WithCreatedTime)

[//]: # (     and UUIDV1 base sequential)

[//]: # (     Junit ->> PrimaryKeyPerformanceTestMultiThreadV1 : uuidV1BaseSequentialNoHyphenWithCreatedTime)

[//]: # (    end)

[//]: # (### Single thread)


#### Line graph
![](docs/resources/test_performance/line_graph_with_prediction.png)

#### Efficiency ratio(time)
![](docs/resources/test_performance/efficiency_bar_graph.png)

#### Thread efficiency
![](docs/resources/test_performance/thread_efficiency.png)

# Detail
## ERD
### v0 (Single thread, Thread per method + for loop)
```mermaid
erDiagram
jpa_auto_increment {
bigint id PK
}
jpa_sequence {
bigint id PK
}
uuidv1_with_created_at {
binary(16) id PK
}
uuidv4_with_created_at {
binary(16) id PK
}
uuidv1_sequential_with_created_at {
binary(16) id PK
}
```
### v1 (N threads per method + for loop)
```mermaid
erDiagram
jpa_auto_increment_with_created_at {
  bigint id PK 
  timestamp(6) created_at
}
jpa_sequence_with_created_at {
  bigint id PK
  timestamp(6) created_at
}
uuidv1_with_created_at {
  binary(16) id PK
  timestamp(6) created_at
}
uuidv4_with_created_at {
  binary(16) id PK
  timestamp(6) created_at
}
uuidv1_sequential_with_created_at {
  binary(16) id PK
  timestamp(6) created_at
}

```
### `Common`

**Description**
- `created_at`:
  > Default: CURRENT_TIMESTAMP(6)
  >
  > Extra: DEFAULT_GENERATED
  >
  > Null: YES
  
