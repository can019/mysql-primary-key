package com.github.can019.performance.entity.v1;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
public class UUIDv1SequentialWithCreatedTimeWithCreatedAt implements PrimaryKeyPerformanceTestEntityWithCreatedAt {
    @Id
    @Column(name="ID",columnDefinition = "BINARY(16)")
    @GeneratedValue(generator = "uuidV1")
    @GenericGenerator(
            name="uuidV1",
            strategy = "com.github.can019.performance.entity.strategy.UUIDv1Sequential"
    )
    public byte[] id;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Override
    public LocalDateTime getLocalDateTime(){
        return this.createdAt;
    }
}
