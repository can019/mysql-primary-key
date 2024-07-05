package com.github.can019.performance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.context.annotation.Profile;

import java.util.UUID;

@Entity
public class UUIDv1 implements PrimaryKeyPerformanceTestEntity {
    @Id
    @Column(name="ID",columnDefinition = "BINARY(16)")
    @GeneratedValue(generator = "uuidV1")
    @GenericGenerator(
            name="uuidV1",
            strategy = "com.github.can019.performance.entity.strategy.UUIDv1"
    )
    public UUID id;
}
