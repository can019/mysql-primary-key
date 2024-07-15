package com.github.can019.performance;

import com.github.can019.performance.entity.v1.*;
import com.github.can019.performance.identifier.IdentifierStrategy;
import com.github.can019.performance.test.util.listener.execution.time.DefaultTestTimeExecutionListener;
import com.github.can019.performance.test.util.stopwatch.StopWatchUtil;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

import static com.github.can019.performance.test.util.context.TestContextUtil.getTotalRepeatedTestMethodCount;
import static com.github.can019.performance.test.util.io.FileUtil.createDirectory;
import static com.github.can019.performance.test.util.report.ReportPath.getTestReportDirectoryPath;

@DataJpaTest
@ActiveProfiles("silence")
@Testcontainers
@TestExecutionListeners(value = {DefaultTestTimeExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PrimaryKeyPerformanceTestMultiThreadInternal.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Commit
@Slf4j
public class PrimaryKeyPerformanceTestMultiThreadV2 {
    @Autowired
    PrimaryKeyPerformanceTestMultiThreadInternal internal;

    private final static int repeatTestTime = 3; // ForkJoinPool thread 개수 / Test method 개수
    private final int totalInsertTime = 10;
    private final int iteration = totalInsertTime / repeatTestTime;
    private static final int testCount = getTotalRepeatedTestMethodCount();
    private final int actualInsertTime = iteration * repeatTestTime;
    private final int forkJonPoolThreads = ForkJoinPool.getCommonPoolParallelism();

    private static final ThreadLocal<StopWatch> threadLocalStopWatch =
            ThreadLocal.withInitial(StopWatch::new);

    private final static ThreadLocal<Map<String, StopWatch.TaskInfo>> threadLocalPkTaskMap
            = ThreadLocal.withInitial(HashMap::new);

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, StopWatch.TaskInfo>> rootConcurrentHashMap
            = new ConcurrentHashMap<>();


    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private final Date startedTime = new Date();

    @DynamicPropertySource
    static void hikariPool(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.hikari.minimum-idle", ()-> 31);
        registry.add("spring.datasource.hikari.maximum-pool-size", ()-> 31);
    }

    @BeforeAll
    @Transactional
    void beforeAll() {
        printOptimalParameter();
        internal.setMySQLAutoTimeStamp();
        initializeConcurrentMap();
    }

    @RepeatedTest(value =  repeatTestTime, name = RepeatedTest.LONG_DISPLAY_NAME)
    @DisplayName("JpaAutoIncrement WithCreatedTime")
    @Execution(ExecutionMode.CONCURRENT)
    public void jpaAutoIncrementWithCreatedTime() throws Exception {
        insertTest(IdentifierStrategy.JPA_AUTO_INCREMENT_CREATED_AT, JpaAutoIncrementCreatedAt.class);
    }

    @RepeatedTest(value = repeatTestTime, name = RepeatedTest.LONG_DISPLAY_NAME)
    @DisplayName("JpaSequence WithCreatedTime")
    @Execution(ExecutionMode.CONCURRENT)
    public void jpaSequenceWithCreatedTime() throws Exception {
        insertTest(IdentifierStrategy.JPA_SEQUENCE_CREATED_AT, JpaSequenceCreatedAt.class);
    }

    @RepeatedTest(value = repeatTestTime, name = RepeatedTest.LONG_DISPLAY_NAME)
    @DisplayName("UUIDv4 WithCreatedTime")
    @Execution(ExecutionMode.CONCURRENT)
    public void uuidV4WithCreatedTime() throws Exception {
        insertTest(IdentifierStrategy.UUID_V4_CREATED_AT, UUIDv4CreatedAt.class);
    }

    @RepeatedTest(value = repeatTestTime, name = RepeatedTest.LONG_DISPLAY_NAME)
    @DisplayName("UUIDv1 WithCreatedTime")
    @Execution(ExecutionMode.CONCURRENT)
    public void uuidV1WithCreatedTime() throws Exception {
        insertTest(IdentifierStrategy.UUID_V1_CREATED_AT, UUIDv1CreatedAt.class);
    }

    @RepeatedTest(value = repeatTestTime, name = RepeatedTest.LONG_DISPLAY_NAME)
    @DisplayName("UUIDv1 Base Sequential WithCreatedTime")
    @Execution(ExecutionMode.CONCURRENT)
    public void uuidV1BaseSequentialNoHyphenWithCreatedTime() throws Exception {
        insertTest(IdentifierStrategy.UUID_V1_SEQUENTIAL_CREATED_AT, UUIDv1SequentialCreatedAt.class);
    }

    @AfterAll
    @Transactional
    void afterAll() throws Exception {
        CompletableFuture<Void> exportCsv1 = exportCsvConcurrently(IdentifierStrategy.JPA_AUTO_INCREMENT_CREATED_AT, JpaAutoIncrementCreatedAt.class);
        CompletableFuture<Void> exportCsv2 = exportCsvConcurrently(IdentifierStrategy.JPA_SEQUENCE_CREATED_AT, JpaSequenceCreatedAt.class);
        CompletableFuture<Void> exportCsv3 = exportCsvConcurrently(IdentifierStrategy.UUID_V1_CREATED_AT, UUIDv1CreatedAt.class);
        CompletableFuture<Void> exportCsv4 = exportCsvConcurrently(IdentifierStrategy.UUID_V4_CREATED_AT, UUIDv4CreatedAt.class);
        CompletableFuture<Void> exportCsv5 = exportCsvConcurrently(IdentifierStrategy.UUID_V1_SEQUENTIAL_CREATED_AT, UUIDv1SequentialCreatedAt.class);

        CompletableFuture<Void> exportCsvConcurrently = CompletableFuture.allOf(
                exportCsv1,
                exportCsv2,
                exportCsv3,
                exportCsv4,
                exportCsv5);

        exportCsvConcurrently.get();
    }

    private void initializeConcurrentMap() {
        rootConcurrentHashMap.put(IdentifierStrategy.JPA_AUTO_INCREMENT_CREATED_AT.getSimpleName(), new ConcurrentHashMap<>());
        rootConcurrentHashMap.put(IdentifierStrategy.JPA_SEQUENCE_CREATED_AT.getSimpleName(), new ConcurrentHashMap<>());
        rootConcurrentHashMap.put(IdentifierStrategy.UUID_V1_CREATED_AT.getSimpleName(), new ConcurrentHashMap<>());
        rootConcurrentHashMap.put(IdentifierStrategy.UUID_V4_CREATED_AT.getSimpleName(), new ConcurrentHashMap<>());
        rootConcurrentHashMap.put(IdentifierStrategy.UUID_V1_SEQUENTIAL_CREATED_AT.getSimpleName(), new ConcurrentHashMap<>());

        if(rootConcurrentHashMap.size() != testCount){
            log.warn("Test count is {} but concurrent hash map size after initialized is {}.",testCount, rootConcurrentHashMap.size());
        }
    }

    public <T extends PrimaryKeyPerformanceTestEntityCreatedAt> void insertTest(IdentifierStrategy identifierStrategy, Class<T> entityClass) throws Exception {
        StopWatch stopWatch = threadLocalStopWatch.get();
        Map<String, StopWatch.TaskInfo> map = threadLocalPkTaskMap.get();
        String rootConcurrentHashMapKey = identifierStrategy.getSimpleName();

        for(int i = 0; i< iteration; i++){
            stopWatch.start(rootConcurrentHashMapKey+ StopWatchUtil.Helper.TASK_NUM_DELIMINATOR.getValue()+i);
            T entity = internal.persistAndGetEntity(entityClass);
            stopWatch.stop();
            map.put(entity.getId(),stopWatch.getLastTaskInfo());
        }

        rootConcurrentHashMap.get(rootConcurrentHashMapKey).putAll(map);
        threadLocalStopWatch.remove();
        threadLocalPkTaskMap.remove();
    }


    private <T extends PrimaryKeyPerformanceTestEntityCreatedAt> CompletableFuture<Void> exportCsvConcurrently(IdentifierStrategy strategy, Class<T> clazz) {
        return CompletableFuture.runAsync(() -> {
            try {
                exportCsv(strategy, clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String getCsvFilePath(String fileName){
        return String.join("/",
                getTestReportDirectoryPath(this.getClass()),
                dateFormat.format(startedTime),
                fileName+".csv");
    }


    private <T extends PrimaryKeyPerformanceTestEntityCreatedAt> void exportCsv(IdentifierStrategy identifierStrategy
            , Class<T> entityClass) throws Exception {
        String rootConcurrentHashMapKey = identifierStrategy.getSimpleName();

        ConcurrentHashMap<String, StopWatch.TaskInfo> primaryKeyTaskInfoMap
                = rootConcurrentHashMap.get(rootConcurrentHashMapKey);

        if(primaryKeyTaskInfoMap.size() != actualInsertTime){
            String exceptionMessage = new StringBuilder()
                    .append("Map size: ")
                    .append(primaryKeyTaskInfoMap.size())
                    .append(", actual insert time: ")
                    .append(actualInsertTime)
                    .toString();
            throw new IllegalStateException(exceptionMessage);
        }

        List<T> entityListOrderByCreatedAt = internal.getEntityListOrderByCreatedAt(entityClass);

        String csvFilePath = getCsvFilePath(rootConcurrentHashMapKey);
        createDirectory(Paths.get(csvFilePath).getParent());

        int i = 1;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath))) {
            writer.write("Task Name,Total Time (nano second)");
            writer.newLine();

            for (PrimaryKeyPerformanceTestEntityCreatedAt entity : entityListOrderByCreatedAt) {
                if( primaryKeyTaskInfoMap.get(entity.getId())==null){
                    throw new NullPointerException("Entity ID not found in primaryKeyTaskInfoMap: " + entity.getId());
                }

                String x = new StringBuilder()
                        .append(rootConcurrentHashMapKey)
                        .append(StopWatchUtil.Helper.TASK_NUM_DELIMINATOR.getValue())
                        .append(i++)
                        .toString();

                long nanos = primaryKeyTaskInfoMap.get(entity.getId()).getTimeNanos();

                writer.write(x+","+nanos);
                writer.newLine();
            }
            log.info("Successfully export csv: {}",csvFilePath);
        } catch (Exception e) {
            throw e;
        }
    }

    private void printOptimalParameter() {
        log.info("!! Optimal parameter > Max available threads (Actual CPU threads): {}", Runtime.getRuntime().availableProcessors());
        log.info("!! Optimal parameter > Available threads for parallel execution (excluding current thread): {}", forkJonPoolThreads);

        log.info("!! Optimal parameter > Execution test count: {}", testCount);
        log.info("!! Optimal parameter > Optimal expected repeated time: {}", forkJonPoolThreads / testCount);

        log.info("!! Optimal parameter > TotalInsertTime: {}", totalInsertTime);
        log.info("!! Optimal parameter > ActualTotalInsertTime: {}", actualInsertTime);

        if(actualInsertTime != totalInsertTime){
            log.warn("!! ActualTotalInsertTime is [{}] TotalInsertTime", actualInsertTime);
        }
    }
}
