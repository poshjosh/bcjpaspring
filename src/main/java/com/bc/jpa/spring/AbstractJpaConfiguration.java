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

package com.bc.jpa.spring;

import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import com.bc.jpa.spring.repository.EntityRepositoryFactoryImpl;
import com.bc.db.meta.access.MetaDataAccess;
import com.bc.db.meta.access.MetaDataAccessImpl;
import com.bc.jpa.dao.JpaObjectFactory;
import com.bc.jpa.dao.JpaObjectFactoryImpl;
import com.bc.jpa.dao.functions.EntityManagerFactoryCreator;
import com.bc.jpa.dao.functions.EntityManagerFactoryCreatorImpl;
import com.bc.jpa.dao.sql.MySQLDateTimePatterns;
import com.bc.jpa.dao.sql.SQLDateTimePatterns;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 9, 2019 11:31:20 AM
 */
public abstract class AbstractJpaConfiguration {
    
    protected AbstractJpaConfiguration() { }

    @Bean @Scope("prototype") public JdbcPropertiesProvider jdbcPropertiesProvider(
            @Autowired ApplicationContext spring) {
        return new JdbcPropertiesProviderFromSpringProperties(spring.getEnvironment());
    }
    
    public abstract String getPersistenceUnitName();
    
    public String [] getEntityPackageNames() {
        return new String[0];
    }

    @Bean @Scope("prototype") public TypeFromNameResolver entityTypeResolver() {
        return new TypeFromNameResolverComposite(
                new TypeFromNameResolverUsingPersistenceXmlFile(),
                new TypeFromNameResolverUsingPackageNames(getEntityPackageNames())
        );
    }
    
    @Bean public EntityRepositoryFactory entityRepositoryFactory(JpaObjectFactory jpa) {
        return new EntityRepositoryFactoryImpl(jpa);
    }
    
    @Bean @Scope("prototype") public MetaDataAccess metaDataAccess(JpaObjectFactory jpa) {
        return new MetaDataAccessImpl(jpa.getEntityManagerFactory());
    }
    
    @Bean public JpaObjectFactory jpaObjectFactory(
            EntityManagerFactoryCreator emfCreator,
            SQLDateTimePatterns sqlDateTimePatterns) {
        return new JpaObjectFactoryImpl(getPersistenceUnitName(), emfCreator, sqlDateTimePatterns);
    }
    
    @Bean @Scope("prototype") public SQLDateTimePatterns sqlDateTimePatterns(
            JdbcPropertiesProvider jdbcPropertiesProvider) {
        // @todo parse properties and determine if driver is: myql, postgresql etc
        // and then return the corresponding datetimepatterns object
        return new MySQLDateTimePatterns();
    }
    
    @Bean @Scope("prototype") public EntityManagerFactoryCreator entityManagerFactoryCreator(
            JdbcPropertiesProvider jdbcPropertiesProvider) {
        return new EntityManagerFactoryCreatorImpl(){
            @Override
            protected Properties getProperties(String persistenceUnit) {
                return jdbcPropertiesProvider.apply(persistenceUnit); 
            }
        };
    }
}
