package com.bc.jpa.spring;

import java.util.Objects;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Attribute;
import org.hibernate.criterion.Example;
import org.hibernate.type.Type;

/**
 * @author hp
 */
public class IdSelector implements Example.PropertySelector{
    
    private final EntityManagerFactory entityManagerFactory;
    
    private final Class entityType;
    
    public IdSelector(EntityManagerFactory entityManagerFactory, Class entityType) {
        this.entityManagerFactory = Objects.requireNonNull(entityManagerFactory);
        this.entityType = Objects.requireNonNull(entityType);
    }
    
    @Override
    public boolean include(Object propertyValue, String propertyName, Type type) {
        final Attribute idAttribute = entityManagerFactory
                .getMetamodel().entity(entityType).getId(Object.class);
        return propertyName.equalsIgnoreCase(idAttribute.getName());
    }
}
