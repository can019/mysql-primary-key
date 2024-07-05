package com.github.can019.performance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
public class UUIDv4 implements PrimaryKeyPerformanceTestEntity{
    @Id
    @Column(name="ID",columnDefinition = "BINARY(16)")
    @GeneratedValue(generator = "uuidV4")
    @GenericGenerator(
            name="uuidV4",
            strategy = "com.github.can019.performance.entity.strategy.UUIDv4"
    )
    public UUID id;
}
