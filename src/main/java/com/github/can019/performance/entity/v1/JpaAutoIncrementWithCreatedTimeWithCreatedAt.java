package com.github.can019.performance.entity.v1;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class JpaAutoIncrementWithCreatedTimeWithCreatedAt implements PrimaryKeyPerformanceTestEntityWithCreatedAt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;


    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Override
    public LocalDateTime getLocalDateTime(){
        return this.createdAt;
    }
}
