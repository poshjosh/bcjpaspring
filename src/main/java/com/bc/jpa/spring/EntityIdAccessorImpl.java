package com.bc.jpa.spring;

import com.bc.jpa.spring.util.JpaUtil;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.metamodel.Metamodel;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

/**
 * @author hp
 * @param <E> The type of the entity/domain object
 * @param <ID> The type of the id of the entity/domain object
 */
public class EntityIdAccessorImpl<E, ID> implements EntityIdAccessor<E, ID>{
    
    private final Metamodel metamodel;
    private final PersistenceUnitUtil persistenceUnitUtil;
    
    public EntityIdAccessorImpl(EntityManagerFactory entityManagerFactory) {
        this.metamodel = entityManagerFactory.getMetamodel();
        this.persistenceUnitUtil = entityManagerFactory.getPersistenceUnitUtil();
    }
    
    @Override
    public Class<ID> getType(Class<E> entityType) {
        entityType = JpaUtil.deduceActualDomainType(entityType);
        return (Class<ID>)metamodel.entity(entityType).getIdType().getJavaType();
    }
    
    
    @Override
    public String getName(Class<E> entityType) {
        entityType = JpaUtil.deduceActualDomainType(entityType);
        final String idName = metamodel
                .entity(entityType)
                .getId(Object.class).getName();
        return idName;
    }

    @Override
    public ID getValueOrDefault(E entity, ID resultIfNone) {
        
        final Class entityType = JpaUtil.deduceActualDomainType(entity.getClass());

        final String name = this.getName((Class<E>)entityType);
        
        ID value = this.getBeanIdValue(entity, name);
        
        if(value == null && this.persistenceUnitUtil.isLoaded(entity, name)) {
            
            value = (ID)this.persistenceUnitUtil.getIdentifier(entity);
        }
        
        return value;
    }

    public ID getBeanIdValue(Object entity, String primaryColumnName) {

        final BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(entity);

        final ID id = bean.isReadableProperty(primaryColumnName)
                ? (ID)bean.getPropertyValue(primaryColumnName) : null;
        
        return id;
    }
}
