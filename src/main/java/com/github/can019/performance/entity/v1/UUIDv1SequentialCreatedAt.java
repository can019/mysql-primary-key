package com.github.can019.performance.entity.v1;

import com.github.can019.performance.util.TypeConvertor;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name="uuidv1_sequential_with_created_at",
        indexes = {@Index(name = "idx_created_at", columnList = "created_at")})
public class UUIDv1SequentialWithCreatedTimeWithCreatedAt implements PrimaryKeyPerformanceTestEntityWithCreatedAt<byte[]> {
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

    @Override
    public String getId() {
        return TypeConvertor.byteArrayToHexString(this.id);
    }
}
