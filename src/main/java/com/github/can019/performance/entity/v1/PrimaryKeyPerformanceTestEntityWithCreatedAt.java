package com.github.can019.performance.entity.v1;

import com.github.can019.performance.entity.PrimaryKeyPerformanceTestEntity;

import java.time.LocalDateTime;

public interface PrimaryKeyPerformanceTestEntityWithCreatedAt<T> extends PrimaryKeyPerformanceTestEntity<T> {
    public LocalDateTime getLocalDateTime();
}
