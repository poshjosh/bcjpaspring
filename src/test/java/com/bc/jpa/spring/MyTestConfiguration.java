package com.bc.jpa.spring;

import com.bc.jpa.spring.repository.DaoImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author hp
 */
@TestConfiguration
@EnableJpaRepositories(
        basePackages = "com.bc.jpa.spring", 
        repositoryBaseClass = DaoImpl.class)    
public class MyTestConfiguration {
    
}
