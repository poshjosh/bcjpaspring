package com.bc.jpa.spring.repository;

import java.util.Objects;
import java.util.function.Predicate;
import javax.persistence.EntityManagerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

/**
 * @author hp
 */
public class JpaRepositoryFactoryImpl implements JpaRepositoryFactory{

    private final EntityManagerFactory entityManagerFactory;
    private final Predicate<Class> domainTypeTest;

    public JpaRepositoryFactoryImpl(
            EntityManagerFactory entityManagerFactory, 
            Predicate<Class> domainTypeTest) {
        this.entityManagerFactory = Objects.requireNonNull(entityManagerFactory);
        this.domainTypeTest = Objects.requireNonNull(domainTypeTest);
    }
    
    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return this.entityManagerFactory;
    }

    @Override
    public boolean isSupported(Class domainClass) {
        return this.domainTypeTest.test(domainClass);
    }
    
    @Override
    public <E, ID> JpaRepository<E, ID> forEntity(
            Class<E> domainClass, Class<ID> idClass) {
        return new SimpleJpaRepository(domainClass, 
                this.getEntityManagerFactory().createEntityManager());
    }
}
