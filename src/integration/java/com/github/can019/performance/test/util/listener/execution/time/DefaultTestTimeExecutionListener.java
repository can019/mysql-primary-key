package com.github.can019.performance.test.util.listener.execution.time;

import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.util.StopWatch;

/**
 * 총 수행시간을 측정하는 listener
 *
 * @author jys01012@gmail.com
 * @version 1.0
 */
@Slf4j
public class DefaultTestTimeExecutionListener extends AbstractTestExecutionListener {
    private StopWatch totalTaskStopWatch;

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        log.info("Running test '{}'...",testContext.getTestClass().getSimpleName());
        totalTaskStopWatch = new StopWatch(testContext.getTestClass().getSimpleName()+ " Total");
        totalTaskStopWatch.start("Total");
        super.beforeTestClass(testContext);
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        super.afterTestClass(testContext);
        totalTaskStopWatch.stop();

        log.info("The test in '{}' has been completed",testContext.getTestClass().getSimpleName());
        log.info(totalTaskStopWatch.prettyPrint());
    }
}
