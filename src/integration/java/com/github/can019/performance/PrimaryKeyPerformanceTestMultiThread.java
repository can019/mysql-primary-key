package com.github.can019.performance;

import com.github.can019.performance.entity.*;
import com.github.can019.performance.test.util.ParallelTestTimeExecutionListener;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest(properties = {"spring.profiles.active=test",
        "logging.level.org.springframework=ERROR",
        "logging.level.com.example.base=ERROR",
        "spring.main.banner-mode=off",
        "logging.level.root=ERROR",
        "spring.jpa.properties.hibernate.show_sql=false",
        "spring.jpa.properties.hibernate.use_sql_comments=false",
        "spring.jpa.properties.hibernate.highlight_sql=false",
        "logging.level.org.hibernate.SQL=OFF",
        "logging.level.org.hibernate.orm.jdbc.bind=OFF",
})
@ActiveProfiles("test")
@Testcontainers
@TestExecutionListeners(value = {ParallelTestTimeExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PrimaryKeyPerformanceTestMultiThreadInternal.class)
public class PrimaryKeyPerformanceTestMultiThread {

    @PersistenceContext
    EntityManager em;

    @Autowired
    PrimaryKeyPerformanceTestMultiThreadInternal internal;

    private final static int repeatTestTime = 1000;

    @DynamicPropertySource
    static void hikariPool(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.hikari.minimum-idle", ()-> 11);
        registry.add("spring.datasource.hikari.maximum-pool-size", ()-> 11);
    }

    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0.32")
            .withDatabaseName("test");

    @Test
    @DisplayName("JpaAutoIncrement")
    @Execution(ExecutionMode.CONCURRENT)
    public void jpaAutoIncrement() throws Exception {
        insertTest("JpaAutoIncrement", JpaAutoIncrement.class);
    }

    @Test
    @DisplayName("JpaSequence")
    @Execution(ExecutionMode.CONCURRENT)
    public void jpaSequence() throws Exception {
        insertTest("JpaSequence", JpaSequence.class);
    }

    @Test
    @DisplayName("UUIDv4")
    @Execution(ExecutionMode.CONCURRENT)
    public void uuidV4() throws Exception {
        insertTest("UUIDv4", UUIDv4.class);
    }

    @Test
    @DisplayName("UUIDv1")
    @Execution(ExecutionMode.CONCURRENT)
    public void uuidV1() throws Exception {
        insertTest("UUIDv1", UUIDv1.class);
    }

    @Test
    @DisplayName("UUIDv1 Base Sequential")
    @Execution(ExecutionMode.CONCURRENT)
    public void uuidV1BaseSequentialNoHyphen() throws Exception {
        insertTest("UUIDv1Sequential", UUIDv1Sequential.class);
    }

    private <T> void insertTest(String testName, Class<T> entityClass) throws Exception {
        StopWatch stopWatch = ParallelTestTimeExecutionListener.threadLocalStopWatch.get();
        for (int i = 0; i < repeatTestTime; i++) {
            stopWatch.start(testName + " # " + i);
            internal.persistEntity(entityClass);
            stopWatch.stop();
        }
    }
}
