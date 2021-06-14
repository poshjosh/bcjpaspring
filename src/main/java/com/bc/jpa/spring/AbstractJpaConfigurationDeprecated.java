package com.bc.jpa.spring;

import com.bc.db.meta.access.MetaDataAccess;
import com.bc.db.meta.access.MetaDataAccessImpl;
import com.bc.db.meta.access.functions.GetConnectionFromEntityManager;
import com.bc.jpa.dao.JpaObjectFactory;
import com.bc.jpa.dao.JpaObjectFactoryImpl;
import com.bc.jpa.dao.functions.EntityManagerFactoryCreator;
import com.bc.jpa.dao.functions.EntityManagerFactoryCreatorImpl;
import com.bc.jpa.dao.sql.MySQLDateTimePatterns;
import com.bc.jpa.dao.sql.SQLDateTimePatterns;
import com.bc.jpa.spring.repository.EntityRepositoryFactory;
import com.bc.jpa.spring.repository.EntityRepositoryFactoryImpl;
import java.util.Properties;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

/**
 * @author hp
 */
public abstract class AbstractJpaConfigurationDeprecated {

    protected AbstractJpaConfigurationDeprecated() { }
    
    @Bean @Scope("prototype") public JdbcPropertiesProvider jdbcPropertiesProvider(
            @Autowired ApplicationContext spring) {
        return new JdbcPropertiesProviderFromSpringProperties(spring.getEnvironment());
    }
    
    public abstract String getPersistenceUnitName();
    
    public String [] getEntityPackageNames() {
        return new String[0];
    }

    @Bean @Scope("prototype") public TypeFromNameResolver typeFromNameResolver(
            JpaObjectFactory jpa) {
        final Set<Class> classes = this.domainClasses(jpa).get();
        return new TypeFromNameResolverUsingClassNames(classes);
    }
    
    @Bean @Scope("prototype") public DomainClasses domainClasses(JpaObjectFactory jpa) {
        return this.domainClassesBuilder()
                .reset()
                .addFrom(jpa.getEntityManagerFactory())
                .addFromPersistenceXmlFile()
                .addFromPackages(this.getEntityPackageNames())
                .build();
    }

    @Bean @Scope("prototype") public DomainClassesBuilder domainClassesBuilder() {
        return new DomainClassesBuilder();
    }
    
    @Bean @Scope("singleton") public EntityRepositoryFactory 
        entityRepositoryFactory(JpaObjectFactory jpa) {
        return new EntityRepositoryFactoryImpl(
                jpa, this.metaDataAccess(jpa), this.domainClasses(jpa));
    }
    
    @Bean @Scope("prototype") public MetaDataAccess metaDataAccess(JpaObjectFactory jpa) {
        return new MetaDataAccessImpl(
                jpa.getEntityManagerFactory(), this.getConnectionFromEntityManager());
    }

    @Bean @Scope("prototype") public GetConnectionFromEntityManager getConnectionFromEntityManager() {
        return new GetConnectionFromEntityManager();
    }
    
    @Bean @Scope("singleton") public JpaObjectFactory jpaObjectFactory(
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
