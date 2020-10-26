package com.bc.jpa.spring.repository;

import com.bc.jpa.dao.Delete;
import com.bc.jpa.dao.JpaObjectFactory;
import com.bc.jpa.dao.Select;
import com.bc.jpa.dao.Update;
import com.bc.jpa.spring.EntityIdAccessor;
import com.bc.jpa.spring.EntityIdAccessorImpl;
import com.bc.jpa.spring.util.JpaUtil;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NonUniqueResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hp
 */
@Repository
@Transactional(readOnly = true)
public class DaoImpl <E, ID extends Serializable>
        extends SimpleJpaRepository<E, ID>  implements Dao<E, ID> {
         
    private static final Logger LOG = LoggerFactory.getLogger(DaoImpl.class);

    private final JpaObjectFactory jpaObjectFactory;
    
    private final Class<E> entityType;
    
    private final EntityManager entityManager;
    
    public DaoImpl(
            JpaObjectFactory jpa, EntityManager entityManager, Class<E> entityType) {
        super(entityType, entityManager);
        this.jpaObjectFactory = Objects.requireNonNull(jpa);
        this.entityManager = Objects.requireNonNull(entityManager);
        this.entityType = Objects.requireNonNull(entityType);
        LOG.debug("Entity type: {}", entityType);
    }
    
    @Override
    public List<E> search(String query) {
        return jpaObjectFactory.getTextSearch().search(entityType, query);
    }
    
    @Override
    public Optional<ID> getIdOptional(E entity) {
        return this.getIdAccessor().getValueOptional(entity);
    }

    @Override
    public boolean exists(Object id) {
        try{
            final Object found = this.find(id);
            return found != null;
        }catch(NonUniqueResultException ignored) {
            return true;
        }catch(EntityNotFoundException ignored) {
            return false;
        }
    }

    @Override
    public boolean existsBy(String name, Object value) {
        final List found = jpaObjectFactory.getDaoForSelect(value.getClass())
                .where(entityType, name, value)
                .select(name).getResultsAndClose(0, 1);
        return found != null  && ! found.isEmpty();
    }

    @Override
    @Transactional
    public void create(E entity) {
        try(final com.bc.jpa.dao.Dao dao = getDao()) {
            dao.begin().persist(entity).commit();
        }
    }
    
    @Override
    @Transactional
    public void deleteManagedEntity(E entity) {
        this.getDao().removeAndClose(entity);
    }

    @Override
    public List<E> findAllBy(String key, Object value) {
        return jpaObjectFactory.getDaoForSelect(entityType)
                .where(key, value).distinct(true).getResultsAndClose();
    }
    
    @Override
    public List<E> findAllBy(String key, Object value, int offset, int limit) {
        return jpaObjectFactory.getDaoForSelect(entityType)
                .where(key, value).distinct(true)
                .getResultsAndClose(offset, limit);
    }

    @Override
    public E findSingleBy(String key, Object value) {
        return jpaObjectFactory.getDaoForSelect(entityType)
                .where(key, value).distinct(true).getSingleResultAndClose();
    }

    @Override
    public List<E> findAll() {
        return jpaObjectFactory.getDaoForSelect(entityType)
                .distinct(true).findAllAndClose(entityType);
    }

    @Override
    public List<E> findAll(int offset, int limit) {
        return jpaObjectFactory.getDaoForSelect(entityType)
                .distinct(true).findAllAndClose(entityType, offset, limit);
    }

    @Override
    public E findOrDefault(Object id, E resultIfNone) {
        E found;
        try{
            found = find(id);
        }catch(EntityNotFoundException e) {
            found = null;
        }
        return found == null ? resultIfNone : found;
    }

    @Override
    public E find(Object id) throws EntityNotFoundException {
        final com.bc.jpa.dao.Dao dao = this.getDao();
        id = this.convertToIdTypeIfNeed(id);
        final Object found = dao.begin().findAndClose(entityType, id);
        this.requireNotNull(found, id);
        return (E)found;
    }

    @Override
    @Transactional
    public void update(E entity) {
        this.getDao().mergeAndClose(entity);
    }

    public Object convertToIdTypeIfNeed(Object id) {
        if( ! this.getPrimaryColumnType().isAssignableFrom(id.getClass())) {
            id = JpaUtil.convertToType(id, this.getPrimaryColumnType());
        }
        return id;
    }
    
    public String getPrimaryColumnName() {
        return this.getIdAccessor().getName(entityType);
    }
    
    public Class<ID> getPrimaryColumnType() {
        return this.getIdAccessor().getType(entityType);
    }
    
    private EntityIdAccessor _eia;
    private EntityIdAccessor getIdAccessor() {
        if(_eia == null) {
            _eia = new EntityIdAccessorImpl(this.getEntityManagerFactory());
        }
        return _eia;
    }
    
    public EntityManagerFactory getEntityManagerFactory() {
        return this.entityManager.getEntityManagerFactory();
    }
    
    public com.bc.jpa.dao.Dao getDao() {
        return jpaObjectFactory.getDao();
    }

    public Select<E> getDaoForSelect() {
        return jpaObjectFactory.getDaoForSelect(entityType).from(entityType);
    }
    
    public <T> Select<T> getDaoForSelect(Class<T> resultType) {
        return jpaObjectFactory.getDaoForSelect(resultType).from(entityType);
    }

    public Update<E> getDaoForUpdate() {
        return jpaObjectFactory.getDaoForUpdate(entityType);
    }

    public Delete<E> getDaoForDelete() {
        return jpaObjectFactory.getDaoForDelete(entityType);
    }

    public JpaObjectFactory getJpaObjectFactory() {
        return jpaObjectFactory;
    }

    @Override
    public Class getEntityType() {
        return entityType;
    }
    
    public void requireNotNull(Object entity, Object id) 
            throws EntityNotFoundException{
        if(entity == null) {
            throw this.getNotFoundException(id);
        }
    }
    
    public EntityNotFoundException getNotFoundException(Object id) {
         return new EntityNotFoundException("Not found. " + 
                 entityType.getName() + " with id: " + id);
    }
}
