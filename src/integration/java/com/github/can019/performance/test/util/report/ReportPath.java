package com.github.can019.performance.test.util.report;

public class ReportPath {
    private final static String ROOT_PATH = String.join("/",".","test","reports");

    /**
     *
     * @param clazz
     * @return "./test/reports/package name of class/class name"
     */
    public final static String getTestReportDirectoryPath(Class<?> clazz) {
        String packageName = clazz.getPackageName();
        String className = clazz.getSimpleName();
        return String.join("/", ROOT_PATH, packageName, className);
    }
}
