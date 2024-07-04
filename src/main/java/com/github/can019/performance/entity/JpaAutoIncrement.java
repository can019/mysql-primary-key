package com.github.can019.performance.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class JpaAutoIncrement implements PrimaryKeyPerformanceTestEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
}
