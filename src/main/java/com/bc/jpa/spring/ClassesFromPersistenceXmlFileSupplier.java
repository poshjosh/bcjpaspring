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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 6, 2019 3:55:57 PM
 */
public interface ClassesFromPersistenceXmlFileSupplier 
        extends Supplier<List<Class>>, Function<String, List<Class>>{

    @Override
    default List<Class> get() {
        
        return this.apply("META-INF/persistence.xml");
    }
    
    @Override
    default List<Class> apply(String resourcePath) {
    
        List<Class> output;
        try {
            
            output = get(resourcePath);
            
        }catch(IOException e) {
            
            e.printStackTrace();
            
            output = Collections.EMPTY_LIST;
        }
        
        return output;
    }
    
    List<Class> get(String resourcePath) throws IOException;
}
