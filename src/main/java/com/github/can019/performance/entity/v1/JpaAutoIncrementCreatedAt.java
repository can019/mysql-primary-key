package com.github.can019.performance.entity.v1;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="jpa_auto_increment_with_created_at",
        indexes = {@Index(name = "idx_created_at", columnList = "created_at")})
public class JpaAutoIncrementCreatedAt implements PrimaryKeyPerformanceTestEntityCreatedAt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;


    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Override
    public String getId() {
        return this.id.toString();
    }
}
