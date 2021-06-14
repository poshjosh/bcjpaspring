package com.bc.jpa.spring;

import java.util.Optional;

/**
 * @author hp
 * @param <E> The type of the entity/domain object
 * @param <ID> The type of the id of the entity/domain object
 */
public interface EntityIdAccessor<E, ID> {
    
    Class<ID> getType(Class<E> entityType);

    String getName(Class<E> entityType);

    default Optional<ID> getValueOptional(E entity) {
        return Optional.ofNullable(this.getValueOrDefault(entity, null));
    }
    
    ID getValueOrDefault(E entity, ID resultIfNone);
}
