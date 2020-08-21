package com.bc.jpa.spring.util;

import java.util.List;
import java.util.function.Function;

/**
 * @author hp
 */
public class FindClassesInPackage implements Function<String, List<Class<?>>>{
    @Override
    public List<Class<?>> apply(String packageName) {
        List<Class<?>> found = new FindClassesInPackageUsingSpringframework().apply(packageName);
        if(found.isEmpty()) {
            found = new com.bc.reflection.function.FindClassesInPackage().apply(packageName);
        }
        return found;
    }
}
