/*
 * Copyright 2019 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bc.jpa.spring.repository;

import com.bc.db.meta.access.MetaDataAccess;
import com.bc.jpa.dao.Dao;
import com.bc.jpa.dao.Delete;
import com.bc.jpa.dao.JpaObjectFactory;
import com.bc.jpa.dao.Select;
import com.bc.jpa.dao.Update;
import com.bc.jpa.dao.functions.GetColumnNames;
import com.bc.jpa.dao.functions.GetTableName;
import com.bc.jpa.dao.sql.SQLUtils;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NonUniqueResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 6, 2019 1:54:23 PM
 */
public class EntityRepositoryImpl<E> implements EntityRepository<E> {

    private static final Logger LOG = LoggerFactory.getLogger(EntityRepositoryImpl.class);

    private final JpaObjectFactory jpaObjectFactory;
    
    private final MetaDataAccess metaDataAccess;
    
    private final Class<E> entityType;

    public EntityRepositoryImpl(
            JpaObjectFactory jpa, MetaDataAccess mda, Class<E> entityType) {
        this.jpaObjectFactory = Objects.requireNonNull(jpa);
        this.entityType = Objects.requireNonNull(entityType);
        this.metaDataAccess = Objects.requireNonNull(mda);
        LOG.debug("Entity type: {}", entityType);
    }

    protected void preCreate(E entity){ }
    
    protected void preUpdate(E entity){ }

    @Override
    public String getTableName() {
        return this.getMetaData().getTableName();
    }
    
    @Override
    public long count() {
        final String primaryColumnName = this.getMetaData().getPrimaryColumnName();
        final Long count = jpaObjectFactory.getDaoForSelect(Long.class)
                .from(entityType)
                .count(primaryColumnName)
                .getSingleResultAndClose();
        return Objects.requireNonNull(count);
    }
    
    @Override
    public boolean hasRecords() {
        final String primaryColumnName = this.getMetaData().getPrimaryColumnName();
        final List results = jpaObjectFactory.getDaoForSelect(Object.class)
                .from(entityType)
                .select(primaryColumnName)
                .getResultsAndClose(0, 1);
        return results != null && ! results.isEmpty();
    }
    
    @Override
    public List<E> search(String query) {
        return jpaObjectFactory.getTextSearch().search(entityType, query);
    }
    
    @Override
    public Optional getIdOptional(Object entity) {

        Object id = getBeanIdValue(entity);
        
        LOG.debug("Id: {}, from entity: {}", id, entity);
        
        if(id == null) {
            
            id = jpaObjectFactory.getEntityManagerFactory()
                    .getPersistenceUnitUtil().getIdentifier(entity);
        }
        
// Too costly and not necessary given PersistenceUnitUtil.getIdentifier is now used
//
//        if(id == null) {
        
//            final Object fromDb = jpaObjectFactory.getDao().merge(entity);
            
//            LOG.debug("Merged entity: {}", fromDb);
            
//            id = getBeanIdValue(fromDb);

//            LOG.debug("Id: {}, from merged entity: {}", id, fromDb);
//        }
        
//        if(id == null) {
            
//            final Collection found = jpaObjectFactory.getTextSearch().searchEntityRecords(
//                    entity, Number.class, CharSequence.class, Date.class);
            
//            if(found.size() == 1) {
                
//                final Object obj = found.iterator().next();
                
//                id = getBeanIdValue(obj);

//                LOG.debug("Id: {}, from searched entity: {}", id, obj);
//            }
//        }
        
        return Optional.ofNullable(id);
    }
    
    public Object getBeanIdValue(Object entity) {
        
        final String primaryColumnName = this.getMetaData().getPrimaryColumnName();

        final BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(entity);

        final Object id = bean.isReadableProperty(primaryColumnName)
                ? bean.getPropertyValue(primaryColumnName) : null;
        
        return id;
    }

    @Override
    public boolean exists(Object id) {
        try{
            final Object found = getDao().find(entityType, id);
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
    public void create(E entity) {
        this.preCreate(entity);
        try(final Dao dao = getDao()) {
            dao.begin().persist(entity).commit();
        }
    }
    
    @Override
    public void deleteManagedEntity(E entity) {
        this.getDao().removeAndClose(entity);
    }

    @Override
    public void deleteById(Object id) {
// This will fail with message: 
// java.lang.IllegalArgumentException: Entity must be managed to call remove: 
// try merging the detached and try the remove again.        
//
// So we use the same Dao to find and then delete the entity
//        final Object found = this.find(id);
//        this.getDao().removeAndClose(found);
        try(final Dao dao = this.getDao()) {
            dao.begin();
            final Object found = dao.find(entityType, toPrimaryColumnType(id));
            this.requireNotNull(found, id);
            dao.remove(found);
            dao.commit();
        }
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
        final Dao dao = this.getDao();
        final Object found = dao.begin().findAndClose(entityType, toPrimaryColumnType(id));
        this.requireNotNull(found, id);;
        return (E)found;
    }

    @Override
    public void update(E entity) {
        this.preUpdate(entity);
        this.getDao().mergeAndClose(entity);
    }
    
    private Object toPrimaryColumnType(Object id) {
        final Object result;
        final Class primaryColumnType = this.getMetaData().getPrimaryColumnType();
        if(primaryColumnType.equals(Short.class)) {
            if(id instanceof Short) {
                result = (Short)id;
            }else{
                result = Short.parseShort(id.toString());
            }
        }else if(primaryColumnType.equals(Integer.class)) {
            if(id instanceof Integer) {
                result = (Integer)id;
            }else{
                result = Integer.parseInt(id.toString());
            }
        }else if(primaryColumnType.equals(Long.class)) {
            if(id instanceof Long) {
                result = (Long)id;
            }else{
                result = Long.parseLong(id.toString());
            }
        }else if(primaryColumnType.equals(String.class)) {
            result = id.toString();
        }else{
            result = id;
        }
        return result;
    }
    
    public Dao getDao() {
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

    private MetaData<E> _meta;
    private MetaData<E> getMetaData() {
        if(_meta == null) {
            _meta = new MetaData(this.metaDataAccess, this.entityType);
        }
        return _meta;
    }

    private static class MetaData<E> implements Serializable{
        
        private final String tableName;
        private final List<String> columnNames;
        private final String primaryColumnName;
        private final Class primaryColumnType;
        
        private MetaData(MetaDataAccess mda, Class<E> entityType) {
            LOG.trace("Entity type: {}", entityType);

            this.tableName = new GetTableName(mda).apply(entityType);
            LOG.trace("Table name: {}", this.tableName);

            //@todo primary column may not be the first column
            final int primaryColumnIndex = 0;
            this.columnNames = new GetColumnNames(mda).apply(this.tableName);
            LOG.debug("Entity: {}, table: {}, columns: {}", 
                    entityType.getName(), this.tableName, this.columnNames);

            this.primaryColumnName = columnNames.get(primaryColumnIndex);
            LOG.trace("Primary column name: {}", this.primaryColumnName);

            final int idType = mda.fetchColumnDataTypes(tableName)[primaryColumnIndex];
            this.primaryColumnType = SQLUtils.getClass(idType, Object.class);
            LOG.trace("Primary column type: {}", this.primaryColumnType);
        }

        public String getTableName() {
            return tableName;
        }

        public List<String> getColumnNames() {
            return columnNames;
        }

        public String getPrimaryColumnName() {
            return primaryColumnName;
        }

        public Class getPrimaryColumnType() {
            return primaryColumnType;
        }
    }
}
