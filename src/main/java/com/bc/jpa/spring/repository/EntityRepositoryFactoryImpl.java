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
import com.bc.jpa.spring.util.JpaUtil;
import java.util.Objects;
import java.util.function.Predicate;
import javax.persistence.EntityManagerFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 6, 2019 2:05:27 PM
 */
public class EntityRepositoryFactoryImpl implements EntityRepositoryFactory{

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
        entityType = JpaUtil.deduceActualDomainType(entityType);
        return classTest.test(entityType);
    }
    
    @Override
    public EntityRepository forEntity(Class entityType) {
        entityType = JpaUtil.deduceActualDomainType(entityType);
        return new EntityRepositoryImpl(this.jpaObjectFactory, this.metaDataAccess, entityType);
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
