package com.bc.jpa.spring.repository;

import javax.persistence.EntityManagerFactory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 6, 2019 2:04:27 PM
 */
public interface JpaRepositoryFactory {

    EntityManagerFactory getEntityManagerFactory();
    
    boolean isSupported(Class domainClass);
    
    default <E> JpaRepository<E, Object> forEntity(Class<E> domainClass) {
        return this.forEntity(domainClass, Object.class);
    }
    
    <E, ID> JpaRepository<E, ID> forEntity(Class<E> domainClass, Class<ID> idClass);
}
