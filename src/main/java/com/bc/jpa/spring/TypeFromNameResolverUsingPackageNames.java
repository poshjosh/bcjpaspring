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

import com.bc.reflection.function.FindClassesInPackage;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javax.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 6, 2019 3:02:44 PM
 */
public class TypeFromNameResolverUsingPackageNames extends AbstractEntityTypeResolver {

    private static final Logger LOG = LoggerFactory.getLogger(TypeFromNameResolverUsingPackageNames.class);
    
    public static class ClassNotFoundForNameException extends RuntimeException{
        public ClassNotFoundForNameException() { }
        public ClassNotFoundForNameException(String string) {
            super(string);
        }
    }

    private final List<String> packageNames;
    
    private final Function<String, List<Class>> findClassesInPackage;

    public TypeFromNameResolverUsingPackageNames(String... packageNames) {
        this(Arrays.asList(packageNames));
    }
    
    public TypeFromNameResolverUsingPackageNames(List<String> packageNames) {
        this(packageNames, new FindClassesInPackage());
    }
    
    public TypeFromNameResolverUsingPackageNames(List<String> packageNames, 
            Function<String, List<Class>> findClassesInPackage) {
        this.packageNames = Objects.requireNonNull(packageNames);
        this.findClassesInPackage = Objects.requireNonNull(findClassesInPackage);
    }

    @Override
    public Class getType(String entityName) {

        final Class type = getType(entityName, (Class)null);
        
        if(type == null) {
            throw new ClassNotFoundForNameException(
                    "Failed to find class for name: " + entityName + 
                    ", in packages: " + packageNames);
        }
        
        return type;
    }

    @Override
    public Class getType(String entityName, Class resultIfNone) {
        
        for(String packageName : packageNames) {

            final Class type = this.getType(entityName, packageName, null);
            
            if(type != null) {
                
                return type;
            }
        }
        
        return resultIfNone;
    }
    
    public Class getType(String tableName, String packageName, Class resultIfNone) {
        final List<Class> list = this.findClassesInPackage.apply(packageName);
        for(Class cls : list) {
            if(cls.getName().equalsIgnoreCase(tableName)) {
                return cls;
            }
            final Table en = (Table)cls.getAnnotation(Table.class);
            if(Objects.equals(en == null ? null : en.name(), tableName)) {
                return cls;
            }
        }
        return resultIfNone;
    }
}
