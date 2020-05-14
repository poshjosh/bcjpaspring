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
import com.bc.db.meta.access.functions.GetConnectionFromEntityManager;
import com.bc.jpa.dao.JpaObjectFactory;
import com.bc.jpa.dao.JpaObjectFactoryBase;
import com.bc.jpa.dao.sql.MySQLDateTimePatterns;
import com.bc.jpa.dao.sql.SQLDateTimePatterns;
import java.util.Set;
import javax.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 9, 2019 11:31:20 AM
 */
public abstract class AbstractJpaConfiguration {
    
    protected AbstractJpaConfiguration() { }

    public abstract EntityManagerFactory entityManagerFactory();
    
    /**
     * Use this to add additional packages to search for entity classes.
     * That is, in addition to those specified in the
     * {@link javax.persistence.EntityManagerFactory EntityManagerFactory} i.e
     * the <tt>META-INF/persistence.xml</tt> file.
     * @return 
     */
    public String [] getAdditionalEntityPackageNames() {
        return new String[0];
    }

    @Bean @Scope("prototype") public TypeFromNameResolver typeFromNameResolver() {
        final Set<Class> classes = this.domainClasses().get();
        return new TypeFromNameResolverUsingClassNames(classes);
    }
    
    @Bean @Scope("prototype") public DomainClasses domainClasses() {
        return this.domainClassesBuilder()
                .reset()
                .addFrom(this.entityManagerFactory())
                .addFromPersistenceXmlFile()
                .addFromPackages(this.getAdditionalEntityPackageNames())
                .build();
    }

    @Bean @Scope("prototype") public DomainClassesBuilder domainClassesBuilder() {
        return new DomainClasses.Builder();
    }
    
    @Bean @Scope("singleton") public EntityRepositoryFactory entityRepositoryFactory() {
        return new EntityRepositoryFactoryImpl(
                this.jpaObjectFactory(), this.metaDataAccess(), this.domainClasses());
    }
    
    @Bean @Scope("prototype") public MetaDataAccess metaDataAccess() {
        return new MetaDataAccessImpl(
                this.entityManagerFactory(), this.getConnectionFromEntityManager());
    }

    @Bean @Scope("prototype") public GetConnectionFromEntityManager 
        getConnectionFromEntityManager() {
        return new GetConnectionFromEntityManager();
    }
    
    @Bean @Scope("singleton") public JpaObjectFactory jpaObjectFactory() {
        return new JpaObjectFactoryBase(
                this.entityManagerFactory(), this.sqlDateTimePatterns());
    }
    
    @Bean @Scope("prototype") public SQLDateTimePatterns sqlDateTimePatterns() {
        
        // @todo parse properties and determine if driver is: myql, postgresql etc
        // and then return the corresponding datetimepatterns object
//        final Map<String, Object> properties = this.entityManagerFactory().getProperties();
        return new MySQLDateTimePatterns();
    }
}
