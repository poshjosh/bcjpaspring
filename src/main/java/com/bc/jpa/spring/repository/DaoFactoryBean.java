package com.bc.jpa.spring.repository;

import com.bc.jpa.dao.JpaObjectFactory;
import com.bc.jpa.spring.util.JpaUtil;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

/**
 * @author hp
 */
public class DaoFactoryBean<R extends JpaRepository<E, ID>, E,
        ID extends Serializable> extends JpaRepositoryFactoryBean<R, E, ID> {
 
    private final JpaObjectFactory jpaObjectFactory;

    public DaoFactoryBean(
            JpaObjectFactory jpaObjectFactory, Class<? extends R> repositoryInterface) {
        super(repositoryInterface);
        this.jpaObjectFactory = jpaObjectFactory;
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory(EntityManager em) {
        return new DaoFactory(jpaObjectFactory, em);
    }
 
    private static class DaoFactory<E, ID extends Serializable>
            extends JpaRepositoryFactory {
 
        private final JpaObjectFactory jpaObjectFactory;

        
        public DaoFactory(JpaObjectFactory jpa, EntityManager em) {
            super(em);
            this.jpaObjectFactory = Objects.requireNonNull(jpa);
        }
 
        @Override
        protected JpaRepositoryImplementation<?, ?> getTargetRepository(
                RepositoryInformation metadata, EntityManager entityManager) {
            Class<E> domainType = (Class<E>) metadata.getDomainType();
            domainType = JpaUtil.deduceActualDomainType(domainType);            
            return new DaoImpl<E, ID>(jpaObjectFactory, entityManager, domainType);
        }
 
        @Override
        protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
            return DaoImpl.class;
        }
    }
}