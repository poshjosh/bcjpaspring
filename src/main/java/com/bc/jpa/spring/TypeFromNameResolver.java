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

import java.util.Optional;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 6, 2019 3:03:11 PM
 */
public interface TypeFromNameResolver {
    
    default Optional<Object> newInstanceOptional(String name) {
        final Object instance = this.newInstance(name, null);
        return Optional.ofNullable(instance);
    }

    default Object newInstance(String name) {
        final Object instance = this.newInstance(name, null);
        if(instance == null) {
            throw new RuntimeException("Failed to resolve name: " + 
                    name + ", to a Class");
        }
        return instance;
    }
    
    default Object newInstance(String name, Object resultIfNone) {
        final Class type = getType(name, null);
        return type == null ? resultIfNone : newInstance(type);
    }

    default Object newInstance(Class type) {
        try{
            return type.newInstance();
        }catch(IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param name whose declared type is to be returned
     * @return the deduced type or throw {@link java.lang.NullPointerException}
     */
    default Class getType(String name) {

        final Class type = getType(name, (Class)null);
        
        if(type == null) {
            throw new RuntimeException("Failed to resolve name: " + 
                    name + ", to a Class");
        }
        
        return type;
    }
    
    default Optional<Class> getTypeOptional(String name) {

        final Class type = getType(name, (Class)null);
        
        return Optional.ofNullable(type);
    }

    Class getType(String name, Class resultIfNone);
    
    default String getName(Class type) {
        return type.getSimpleName();
    }
}
