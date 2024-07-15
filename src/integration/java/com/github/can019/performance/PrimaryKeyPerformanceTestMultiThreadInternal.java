package com.github.can019.performance;

import com.github.can019.performance.entity.PrimaryKeyPerformanceTestEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Repository
@Slf4j
public class PrimaryKeyPerformanceTestMultiThreadInternal {

    @PersistenceContext
    EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Commit
    public <T extends PrimaryKeyPerformanceTestEntity> void persistEntity(Class<T> entityClass) throws Exception {
        T entity = entityClass.getDeclaredConstructor().newInstance();
        em.persist(entity);
        em.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Commit
    public <T extends PrimaryKeyPerformanceTestEntity> T persistAndGetEntity(Class<T> entityClass) throws Exception {
        T entity = entityClass.getDeclaredConstructor().newInstance();
        em.persist(entity);
        em.flush();
        return entity;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Commit
    public void setMySQLAutoTimeStamp() {
            em.createNativeQuery("ALTER TABLE jpa_auto_increment_with_created_at MODIFY created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6);").executeUpdate();
            em.createNativeQuery("ALTER TABLE jpa_sequence_with_created_at MODIFY COLUMN created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6);").executeUpdate();
            em.createNativeQuery("ALTER TABLE uuidv1_with_created_at MODIFY COLUMN created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6);").executeUpdate();
            em.createNativeQuery("ALTER TABLE uuidv4_with_created_at MODIFY COLUMN created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6);").executeUpdate();
            em.createNativeQuery("ALTER TABLE uuidv1_sequential_with_created_at MODIFY COLUMN created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6);").executeUpdate();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Commit
    public <T extends PrimaryKeyPerformanceTestEntity> List<T> getEntityListOrderByCreatedAt(Class<T> entityClass) throws Exception {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq =  cb.createQuery(entityClass);

        Root<T> root = cq.from(entityClass);
        cq.orderBy(cb.asc(root.get("createdAt")));

        List<T> list = em.createQuery(cq).getResultList();
        return list;
    }
}
