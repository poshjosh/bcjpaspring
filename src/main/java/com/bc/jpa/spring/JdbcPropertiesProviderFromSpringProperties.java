package com.bc.jpa.spring;

import java.util.Objects;
import java.util.Properties;
import org.springframework.core.env.Environment;

/**
 * @author hp
 */
public class JdbcPropertiesProviderFromSpringProperties implements JdbcPropertiesProvider{

    private final Environment env;

    public JdbcPropertiesProviderFromSpringProperties(Environment env) {
        this.env = Objects.requireNonNull(env);
    }
    
    @Override
    public Properties apply(String persistenceUnitName) {
// MySQL 5        com.mysql.jdbc.Driver
// MySQL 8+       com.mysql.cj.jdbc.Driver  
        final Properties props = new Properties();
        setProperty(props, "javax.persistence.jdbc.driver", "spring.datasource.driver-class-name");
        setProperty(props, "javax.persistence.jdbc.url", "spring.datasource.url");
        setProperty(props, "javax.persistence.jdbc.user", "spring.datasource.username");
        setProperty(props, "javax.persistence.jdbc.password", "spring.datasource.password");
        return props;
    }
    
    private void setProperty(Properties props, String name, String envKey) {
        final String envVal = env.getProperty(envKey);
        if(envVal != null && !envVal.isEmpty()) {
            props.setProperty(name, envVal);
        }
    }
}
