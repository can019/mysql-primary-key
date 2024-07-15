package com.github.can019.performance.test.util.listener.execution.time;

import com.github.can019.performance.test.util.stopwatch.StopWatchUtil;
import org.springframework.test.context.TestContext;
import org.springframework.util.StopWatch;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.github.can019.performance.test.util.report.ReportPath.getTestReportDirectoryPath;

/**
 * Thread safe
 *  Test method 실행 전 thread safe StopWatch의 이름을 현재 teste method의 이름으로 설정.
 *  Test method 실행 후 thread safe StopWatch의 결과를 csv로 export해줌.
 * `@Execution(value = ExecutionMode.CONCURRENT)`에서 사용 가능.
 * `@RepeatedTest`과 `@Execution(value = ExecutionMode.CONCURRENT)`를 동시 사용 테스트에서 사용 불가.
 *
 * @author jys01012@gmail.com
 * @version 1.0
 *
 * @see com.github.can019.performance.test.util.listener.execution.time.DefaultTestTimeExecutionListener
 * @see com.github.can019.performance.test.util.listener.execution.time.TestTimeExecutionExportListener
 *
 */
public class ParallelTestTimeExecutionExportListener extends DefaultTestTimeExecutionListener {

    public static final ThreadLocal<StopWatch> threadLocalStopWatch =
            ThreadLocal.withInitial(StopWatch::new);

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private final Date startedTime = new Date();

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        super.beforeTestMethod(testContext);
        Thread.currentThread().setName(testContext.getTestMethod().getName());
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        super.afterTestMethod(testContext);
        StopWatch stopWatch = ParallelTestTimeExecutionExportListener.threadLocalStopWatch.get();
        StopWatchUtil.exportCsv(stopWatch, exportCsvPathResolver(testContext));
        threadLocalStopWatch.remove();
    }

    private String exportCsvPathResolver(TestContext testContext){
        String csvFilePath = String.join("/",
                getTestReportDirectoryPath(testContext.getTestClass()),
                dateFormat.format(startedTime),
                testContext.getTestMethod().getName()) + ".csv";

        return csvFilePath;
    }
}