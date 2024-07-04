package com.github.can019.performance.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class JpaSequence implements PrimaryKeyPerformanceTestEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public Long id;
}
