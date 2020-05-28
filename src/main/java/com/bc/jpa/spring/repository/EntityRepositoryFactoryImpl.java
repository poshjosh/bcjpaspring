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
import com.bc.jpa.dao.JpaObjectFactory;
import java.util.Objects;
import java.util.function.Predicate;
import javax.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 6, 2019 2:05:27 PM
 */
public class EntityRepositoryFactoryImpl implements EntityRepositoryFactory{

    private static final Logger LOG = LoggerFactory.getLogger(EntityRepositoryFactoryImpl.class);
    
    private final JpaObjectFactory jpaObjectFactory;
    
    private final MetaDataAccess metaDataAccess;
    
    private final Predicate<Class> classTest;
    
    public EntityRepositoryFactoryImpl(
            JpaObjectFactory jpa, MetaDataAccess mda, Predicate<Class> classTest) {
        this.jpaObjectFactory = Objects.requireNonNull(jpa);
        this.metaDataAccess = Objects.requireNonNull(mda);
        this.classTest = Objects.requireNonNull(classTest);
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return jpaObjectFactory.getEntityManagerFactory();
    }
    
    @Override
    public boolean isSupported(Class entityType) {
        entityType = this.deduceActualDomainType(entityType);
        return classTest.test(entityType);
    }
    
    @Override
    public EntityRepository forEntity(Class entityType) {
        entityType = this.deduceActualDomainType(entityType);
        return new EntityRepositoryImpl(this.jpaObjectFactory, this.metaDataAccess, entityType);
    }
    
    /**
     * This method formats delegates of classes passed around by some 
     * Persistence APIs to the actual class representing the domain type. 
     * 
     * <p>
     * For example given domain type <code>com.domain.Person</code>
     * some persistence APIs were observed to pass around types of format
     * <code>com.domain.Person$HibernateProxy$uvcIsv</code>. 
     * </p>
     * Given argument of <code>com.domain.Person$HibernateProxy$uvcIsv</code>,
     * this method return's <code>com.domain.Person</code>
     * @param type
     * @return 
     */
    private Class deduceActualDomainType(Class type) {
        Class output = type;
        while(output.isAnonymousClass()) {
            final Class next = output.getEnclosingClass();
            if(next == null || output.equals(next)) {
                break;
            }
            output = next;
        }
        if(output != type) {
            LOG.debug("Formatted {} to {}", type, output);
        }
        final String className = output.getName();
        final int end = className.indexOf('$');
        if(end > 0) {
            final String newClassName = className.substring(0, end);
            LOG.trace("Will attempt to use: {}", newClassName);
            try{
                output = Class.forName(newClassName);
                LOG.debug("Formatted {} to {}", output, newClassName);
            }catch(ClassNotFoundException e) {
                LOG.warn(e.toString());
            }
        }
        return output;
    } 

    public JpaObjectFactory getJpaObjectFactory() {
        return jpaObjectFactory;
    }

    public MetaDataAccess getMetaDataAccess() {
        return metaDataAccess;
    }

    public Predicate<Class> getClassTest() {
        return classTest;
    }
}
