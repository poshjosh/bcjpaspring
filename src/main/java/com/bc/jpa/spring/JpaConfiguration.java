package com.bc.jpa.spring;

import java.util.Objects;
import javax.persistence.EntityManagerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * @author chinomso ikwuagwu
 */
public class JpaConfiguration extends AbstractJpaConfiguration{
    
    @FunctionalInterface
    public static interface AdditionalEntityPackageNamesSupplier{
        String [] get();
    }
   
    private final ApplicationContext applicationContext;

    public JpaConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = Objects.requireNonNull(applicationContext);
    }

    @Override
    protected EntityManagerFactory entityManagerFactory() {
        return applicationContext.getBean(EntityManagerFactory.class);
    }

    @Override
    protected String[] getAdditionalEntityPackageNames() {
        try{
            return applicationContext
                    .getBean(AdditionalEntityPackageNamesSupplier.class)
                    .get();
        }catch(NoSuchBeanDefinitionException ignored) {
            return super.getAdditionalEntityPackageNames();
        }
    }
}
