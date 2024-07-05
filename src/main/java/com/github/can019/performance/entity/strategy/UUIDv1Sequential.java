package com.github.can019.performance.entity.strategy;

import com.github.can019.performance.identifier.TimeBasedSequenceIdentifier;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;

public class UUIDv1Sequential implements IdentifierGenerator, Configurable {
    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) {

        return TimeBasedSequenceIdentifier.generate();
    }
}