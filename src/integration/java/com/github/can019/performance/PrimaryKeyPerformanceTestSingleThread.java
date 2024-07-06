package com.github.can019.performance;


import com.github.can019.performance.entity.*;
import com.github.can019.performance.test.util.TestTimeExecutionListener;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestExecutionListeners;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ActiveProfiles("silence")
@Testcontainers
@TestExecutionListeners(value = {TestTimeExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Commit
public class PrimaryKeyPerformanceTestSingleThread {

    @PersistenceContext
    private EntityManager em;

    private final static int repeatTestTime = 10;

    @Autowired
    private DataSource dataSource;

    @DynamicPropertySource
    static void logger(DynamicPropertyRegistry registry) {
        registry.add("logging.level.root", ()-> "ERROR");
    }

    @Test
    @Disabled
    void checkUsingMysqlContainer() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            assertThat(databaseProductName).isEqualTo("MySQL");
        }
    }

    @RepeatedTest(value = repeatTestTime, name = RepeatedTest.LONG_DISPLAY_NAME)
    @DisplayName("JpaAutoIncrement")
    void jpaAutoIncrement() {
        JpaAutoIncrement jpaAutoIncrement = new JpaAutoIncrement();
        em.persist(jpaAutoIncrement);
    }

    @RepeatedTest(value = repeatTestTime, name = RepeatedTest.LONG_DISPLAY_NAME)
    @DisplayName("JpaSequence")
    void jpaSequence() {
        JpaSequence jpaSequence = new JpaSequence();
        em.persist(jpaSequence);
    }

    @RepeatedTest(value = repeatTestTime, name = RepeatedTest.LONG_DISPLAY_NAME)
    @DisplayName("UUIDv4")
    void uuidV4() {
        UUIDv4 uuiDv4 = new UUIDv4();
        em.persist(uuiDv4);
    }

    @RepeatedTest(value = repeatTestTime, name = RepeatedTest.LONG_DISPLAY_NAME)
    @DisplayName("UUIDv1")
    @Execution(value = ExecutionMode.SAME_THREAD)
    void uuidV1() {
        UUIDv1 uuiDv1 = new UUIDv1();
        em.persist(uuiDv1);
    }

    @RepeatedTest(value = repeatTestTime, name = RepeatedTest.LONG_DISPLAY_NAME)
    @DisplayName("UUIDv1 Base Sequential")
    @Execution(value = ExecutionMode.SAME_THREAD)
    void uuiDv1Sequential() {
        UUIDv1Sequential uuiDv1Sequential = new UUIDv1Sequential();
        em.persist(uuiDv1Sequential);
    }
}
