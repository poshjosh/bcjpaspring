package com.bc.jpa.spring.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.metamodel.Attribute;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author hp
 */
@NoRepositoryBean
public interface Dao<E, ID extends Serializable> extends JpaRepository<E, ID> {
 
    Class<E> getEntityType();
    
    default boolean hasRecords() {
        return this.findAll(PageRequest.of(0, 1)).getSize() != 0;
        
    }

    Optional<ID> getIdOptional(E entity);
        
    void create(E entity);
    
    boolean exists(Object id);
    
    boolean existsBy(String name, Object value);
        
    default List<E> findAllBy(Attribute key, Object value) {
        return findAllBy(key.getName(), value);
    }
    
    List<E> findAllBy(String key, Object value);
    
    List<E> findAllBy(String key, Object value, int offset, int limit);

    default E findSingleBy(Attribute key, Object value, E outputIfNone) 
            throws NonUniqueResultException{
        return findSingleBy(key.getName(), value, outputIfNone);
    }
    default E findSingleBy(String key, Object value, E outputIfNone) 
            throws NonUniqueResultException{
        E found = null;
        try{
            found = findSingleBy(key, value);
        }catch(NoResultException ignored) { }
        return found == null ? outputIfNone : found;
    }

    default E findSingleBy(Attribute key, Object value) 
            throws NoResultException, NonUniqueResultException{
        return findSingleBy(key.getName(), value);
    }
    
    E findSingleBy(String key, Object value) 
            throws NoResultException, NonUniqueResultException;
            
    List<E> findAll(int offset, int limit);
    
    E find(Object id) throws EntityNotFoundException;

    E findOrDefault(Object id, E resultIfNone);
    
    void deleteManagedEntity(E entity);

    void update(E entity);

    List<E> search(String query);
}