package com.bc.jpa.spring;

import com.bc.db.meta.access.MetaDataAccess;
import com.bc.db.meta.access.MetaDataAccessImpl;
import com.bc.jpa.dao.JpaObjectFactory;
import com.bc.jpa.dao.JpaObjectFactoryBase;
import com.bc.jpa.dao.sql.MySQLDateTimePatterns;
import com.bc.jpa.spring.repository.EntityRepository;
import com.bc.jpa.spring.repository.EntityRepositoryImpl;
import com.bc.jpa.spring.repository.JpaRepositoryFactory;
import com.bc.jpa.spring.repository.JpaRepositoryFactoryImpl;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author hp
 */
public class TestConfig {
    
    public static final boolean DEBUG = false;

    private static final EntityManagerFactory emf =
        Persistence.createEntityManagerFactory("bcjpaspring_persistence_unit");
    
    private static final JpaRepositoryFactory repoFactory =
            new JpaRepositoryFactoryImpl(emf, (cls) -> true);
    
    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }
    
    public JpaRepositoryFactory getJpaRepositoryFactory() {
        return repoFactory;
    }

    public <E> JpaRepository<E, Object> getJpaRepo(Class<E> entityType){
        return repoFactory.forEntity(entityType);
    }
    
    public <E> EntityRepository<E> getEntityRepo(Class<E> entityType){
        return this.getEntityRepo(emf, entityType);
    }
    
    public <E> EntityRepository<E> getEntityRepo(
            EntityManagerFactory emf, Class<E> entityType){
        return new EntityRepositoryImpl(
                this.getJpaObjectFactory(emf),
                this.getMetaDataAccess(emf),
                entityType
        );
    }
    
    public MetaDataAccess getMetaDataAccess() {
        return this.getMetaDataAccess(emf);
    }
    
    public MetaDataAccess getMetaDataAccess(@Autowired EntityManagerFactory emf) {
        return new MetaDataAccessImpl(emf);
    }
    
    public JpaObjectFactory getJpaObjectFactory() {
        return this.getJpaObjectFactory(emf);
    }
    
    public JpaObjectFactory getJpaObjectFactory(@Autowired EntityManagerFactory emf) {
        return new JpaObjectFactoryBase(emf, new MySQLDateTimePatterns());
    }
}
