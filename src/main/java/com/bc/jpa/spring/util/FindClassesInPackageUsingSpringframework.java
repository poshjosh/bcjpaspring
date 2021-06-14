package com.bc.jpa.spring.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

/**
 * @author hp
 * @see https://stackoverflow.com/questions/520328/can-you-find-all-classes-in-a-package-using-reflection
 */
public class FindClassesInPackageUsingSpringframework implements Function<String, List<Class<?>>>{
    
    private static final Logger LOG = LoggerFactory.getLogger(FindClassesInPackageUsingSpringframework.class);

    @Override
    public List<Class<?>> apply(String packageName) {
        try{
            return find(packageName);
        }catch(ClassNotFoundException e) {
            LOG.warn("Failed to find classes in package: " + packageName, e);
            return Collections.EMPTY_LIST;
        }
    }

    public List<Class<?>> find(String packageName) throws ClassNotFoundException{
        
        // create scanner and disable default filters (that is the 'false' argument)
        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        
        // add include filters which matches all the classes (or use your own)
        provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));

        // get matching classes defined in the package
        final Set<BeanDefinition> classes = provider.findCandidateComponents(packageName);
        
        final List<Class<?>> output = new ArrayList<>(classes.size());

        // this is how you can load the class type from BeanDefinition instance
        for (BeanDefinition bean: classes) {
            String className = bean.getBeanClassName();
            Class<?> clazz = Class.forName(className);
            output.add(clazz);
        }        
        
        List<Class<?>> result = output.isEmpty() ? Collections.EMPTY_LIST : Collections.unmodifiableList(output);
        
        LOG.info("In package: {}, found classes: {}", packageName, result);
        
        return result;
    }
}
