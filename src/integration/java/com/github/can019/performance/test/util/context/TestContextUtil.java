package com.github.can019.performance.test.util.context;

import com.github.can019.performance.PrimaryKeyPerformanceTestMultiThreadV2;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TestContextUtil {
    public static int getTotalRepeatedTestMethodCount(){
        return (int) Arrays.stream(PrimaryKeyPerformanceTestMultiThreadV2.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(RepeatedTest.class))
                .count();
    }

    public static int getTotalTestMethodCount(){
        return (int) Arrays.stream(PrimaryKeyPerformanceTestMultiThreadV2.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Test.class))
                .count();
    }
}
