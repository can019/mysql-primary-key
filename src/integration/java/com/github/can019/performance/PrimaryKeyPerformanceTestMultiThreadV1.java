package com.github.can019.performance;

import com.github.can019.performance.entity.*;
import com.github.can019.performance.identifier.IdentifierStrategy;
import com.github.can019.performance.test.util.listener.execution.time.ParallelTestTimeExecutionExportListener;
import com.github.can019.performance.test.util.stopwatch.StopWatchUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.util.StopWatch;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@ActiveProfiles("silence")
@Testcontainers
@TestExecutionListeners(value = {ParallelTestTimeExecutionExportListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PrimaryKeyPerformanceTestMultiThreadInternal.class)
public class PrimaryKeyPerformanceTestMultiThreadV1 {

    @Autowired
    PrimaryKeyPerformanceTestMultiThreadInternal internal;

    private final static int repeatTestTime = 10;

    @DynamicPropertySource
    static void hikariPool(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.hikari.minimum-idle", ()-> 11);
        registry.add("spring.datasource.hikari.maximum-pool-size", ()-> 11);
    }

    @Test
    @DisplayName("JpaAutoIncrement")
    @Execution(ExecutionMode.CONCURRENT)
    public void jpaAutoIncrement() throws Exception {
        insertTest(IdentifierStrategy.JPA_AUTO_INCREMENT, JpaAutoIncrement.class);
    }

    @Test
    @DisplayName("JpaSequence")
    @Execution(ExecutionMode.CONCURRENT)
    public void jpaSequence() throws Exception {
        insertTest(IdentifierStrategy.JPA_SEQUENCE, JpaSequence.class);
    }

    @Test
    @DisplayName("UUIDv4")
    @Execution(ExecutionMode.CONCURRENT)
    public void uuidV4() throws Exception {
        insertTest(IdentifierStrategy.UUID_V4, UUIDv4.class);
    }

    @Test
    @DisplayName("UUIDv1")
    @Execution(ExecutionMode.CONCURRENT)
    public void uuidV1() throws Exception {
        insertTest(IdentifierStrategy.UUID_V1, UUIDv1.class);
    }

    @Test
    @DisplayName("UUIDv1 Base Sequential")
    @Execution(ExecutionMode.CONCURRENT)
    public void uuidV1BaseSequentialNoHyphen() throws Exception {
        insertTest(IdentifierStrategy.UUID_V1_SEQUENTIAL, UUIDv1Sequential.class);
    }

    private <T extends PrimaryKeyPerformanceTestEntity> void insertTest(IdentifierStrategy identifierStrategy, Class<T> entityClass) throws Exception {
        StopWatch stopWatch = ParallelTestTimeExecutionExportListener.threadLocalStopWatch.get();
        for (int i = 0; i < repeatTestTime; i++) {
            stopWatch.start(identifierStrategy.getSimpleName() + StopWatchUtil.Helper.TASK_NUM_DELIMINATOR.getValue() + i);
            internal.persistEntity(entityClass);
            stopWatch.stop();
        }
    }
}
