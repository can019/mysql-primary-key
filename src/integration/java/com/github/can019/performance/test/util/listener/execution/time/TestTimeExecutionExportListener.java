package com.github.can019.performance.test.util.listener.execution.time;

import org.springframework.test.context.TestContext;
import org.springframework.util.StopWatch;

import java.util.UUID;

/**
 * Non thread safe
 * `@Execution(value = ExecutionMode.SAME_THREAD)`에서만 사용 가능
 */
public class TestTimeExecutionExportListener extends DefaultTestTimeExecutionListener {

    private StopWatch stopWatch;

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        super.beforeTestClass(testContext);
        stopWatch = new StopWatch(testContext.getTestClass().getSimpleName());
    }

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        super.beforeTestMethod(testContext);
        stopWatch.start(testContext.getTestMethod().getName());
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        if (stopWatch.isRunning()) {
            stopWatch.stop();
        }
        super.afterTestMethod(testContext);
    }

    private String exportCsvPathResolver(TestContext testContext) {
        String packageName = testContext.getTestClass().getPackageName();
        String className = testContext.getTestClass().getSimpleName();
        String csvFilePath = "./test/reports/" + packageName
                + "/" + className + "/"+ UUID.randomUUID()+".csv";

        return csvFilePath;
    }
}