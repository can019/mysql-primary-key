package com.github.can019.performance.entity.v1;

import com.github.can019.performance.entity.PrimaryKeyPerformanceTestEntity;
import com.github.can019.performance.identifier.IdentifierStrategy;

import java.time.LocalDateTime;

public interface PrimaryKeyPerformanceTestEntityCreatedAt<T> extends PrimaryKeyPerformanceTestEntity {
    public String getId();
}
