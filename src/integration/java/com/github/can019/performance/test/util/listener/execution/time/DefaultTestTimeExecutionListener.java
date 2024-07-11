package com.github.can019.performance.test.util.listener.execution.time;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.util.StopWatch;

/**
 * 총 수행시간을 측정하는 listener
 *
 * @author jys01012@gmail.com
 * @version 1.0
 */
public class DefaultTestTimeExecutionListener extends AbstractTestExecutionListener {
    private StopWatch totalTaskStopWatch;

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        super.beforeTestClass(testContext);
        System.out.println("Running test '" + testContext.getTestClass().getSimpleName() + "'...");
        totalTaskStopWatch = new StopWatch(testContext.getTestClass().getSimpleName()+ " Total");
        totalTaskStopWatch.start("Total");
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        super.afterTestClass(testContext);
        totalTaskStopWatch.stop();

        System.out.println("The test in '" + testContext.getTestClass().getSimpleName()+"' has been completed");
        System.out.println(totalTaskStopWatch.prettyPrint());
    }
}
