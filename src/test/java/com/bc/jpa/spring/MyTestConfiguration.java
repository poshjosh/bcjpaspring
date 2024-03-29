package com.bc.jpa.spring;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author hp
 */
//@EnableJpaRepositories(
//        basePackages = "com.bc.jpa.spring", 
//        repositoryBaseClass = DaoImpl.class)    
@TestConfiguration
@EnableJpaRepositories(basePackages = "com.bc.jpa.spring.repository")
@PropertySource("persistence.properties")
@EnableTransactionManagement
public class MyTestConfiguration {
    
    @Autowired
    private Environment env;
     
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("jdbc.driverClassName"));
        dataSource.setUrl(env.getProperty("jdbc.url"));
        dataSource.setUsername(env.getProperty("jdbc.user"));
        dataSource.setPassword(env.getProperty("jdbc.pass"));
 
        return dataSource;
    }
}
