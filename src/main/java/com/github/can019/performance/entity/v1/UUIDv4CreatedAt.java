package com.github.can019.performance.entity.v1;

import com.github.can019.performance.identifier.IdentifierStrategy;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name=" uuidv4_with_created_at",
        indexes = {@Index(name = "idx_created_at", columnList = "created_at")})
public class UUIDv4CreatedAt implements PrimaryKeyPerformanceTestEntityCreatedAt{
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
    public String getId() {
        return this.id.toString();
    }
}
