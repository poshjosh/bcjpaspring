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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 6, 2019 4:56:21 PM
 */
public class TypeFromNameResolverUsingClassNames extends AbstractTypeFromNameResolver {

    private static final Logger LOG = LoggerFactory.getLogger(TypeFromNameResolverUsingClassNames.class);

    private final Collection<Class> classes;

    public TypeFromNameResolverUsingClassNames(Collection<String> classNames) {
        this.classes = Collections.unmodifiableList(getUniqueClasses(classNames));
    }
    
    public TypeFromNameResolverUsingClassNames(Set<Class> classes) {
        this.classes = Collections.unmodifiableList(getUniqueClasses(classes));
    }

    @Override
    public Class getType(String entityName) {

        final Class type = getType(entityName, (Class)null);
        
        if(type == null) {
            throw new RuntimeException("Failed to find class named: " + 
                    entityName + ", in: " + this.classes);
        }
        
        return type;
    }
    
    @Override
    public Class getType(String entityName, Class resultIfNone) {
        
        final Predicate<Class> matchingClassName = (cls) ->
                cls.getSimpleName().equalsIgnoreCase(entityName);
        
        final Predicate<Class> matchingTableName = (cls) -> {
            final Table en = (Table)cls.getAnnotation(Table.class);
            if(en == null) {
                return false;
            }else{
                return en.name().equalsIgnoreCase(entityName);
            }
        };    
        
        final Predicate<Class> filter = matchingClassName.or(matchingTableName);
        
        final Set<Class> found = this.classes.stream()
                .filter(filter)
                .collect(Collectors.toSet());
        
        LOG.trace("For name: {}, found matching classs: {}", entityName, found);
        
        final Class type;
        if(found.isEmpty()) {
            type = null;
        }else if(found.size() > 1) {
            LOG.warn("For: {}, multiple class names found: {}", entityName, found);
            type = null;
        }else{
            type = found.iterator().next();
        }
        
        return type == null ? resultIfNone : type;
    }

    /**
     * @param classes
     * @return A set of unique classes, with distinction based on class name
     * @see #getUniqueClasses(java.util.Set) 
     */
    private List<Class> getUniqueClasses(Collection<String> classNames) {
        classNames = classNames instanceof Set ? (Set<String>)classNames : new HashSet(classNames);
        final List<Class> list = new ArrayList(classNames.size());
        for(String className : classNames) {
            try{
                final Class cls = Class.forName(className);
                if( ! list.contains(cls)) {
                    list.add(cls);
                }
            }catch(ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        LOG.trace(" Input: {}\nOutput: {}", classNames, list);
        return list;
    }
    
    /**
     * Create and populate a unique set of Classes based on class names.
     * 
     * Two classes may have the same class name but not be equal. This is 
     * because, the <code>Class</code> class override's neither the equals or 
     * hashcode methods of the <code>Object</code> class.
     * 
     * This method considers 2 classes equal if their class names are equal
     * 
     * @param classes
     * @return A set of unique classes, with distinction based on class name
     */
    private List<Class> getUniqueClasses(Set<Class> classes) {
        Set<String> added = new HashSet<>();
        List<Class> unique = new ArrayList<>(classes.size());
        for(Class cls : classes) {
            String className = cls.getName();
            if(added.add(className)) {
                unique.add(cls);
            }
        }
        LOG.trace(" Input: {}\nOutput: {}", classes, unique);
        return unique;
    }
}
