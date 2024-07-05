package com.github.can019.performance;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PrimaryKeyPerformanceTestMultiThreadInternal {


    @PersistenceContext
    EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Commit
    public <T> void persistEntity(Class<T> entityClass) throws Exception {
        T entity = entityClass.getDeclaredConstructor().newInstance();
        em.persist(entity);
        em.flush();
    }
}
