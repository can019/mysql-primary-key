package com.github.can019.performance.entity.v1;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class UUIDv4WithCreatedTimeWithCreatedAt implements PrimaryKeyPerformanceTestEntityWithCreatedAt {
    @Id
    @Column(name="ID",columnDefinition = "BINARY(16)")
    @GeneratedValue(generator = "uuidV4")
    @GenericGenerator(
            name="uuidV4",
            strategy = "com.github.can019.performance.entity.strategy.UUIDv4"
    )
    public UUID id;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Override
    public LocalDateTime getLocalDateTime(){
        return this.createdAt;
    }
}
