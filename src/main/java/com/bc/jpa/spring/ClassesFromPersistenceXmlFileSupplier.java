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

import com.bc.xml.DomReaderImpl;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 6, 2019 3:55:57 PM
 */
public class ClassesFromPersistenceXmlFileSupplier implements Supplier<List<Class>>{

    private static final Logger LOG = LoggerFactory.getLogger(ClassesFromPersistenceXmlFileSupplier.class);

    public ClassesFromPersistenceXmlFileSupplier() { }
    
    @Override
    public List<Class> get() {
        List<Class> output;
        try {
            
            output = getClassesFromPersistenceFile("META-INF/persistence.xml");
            
        }catch(IOException e) {
            
            output = Collections.EMPTY_LIST;
        }
        
        LOG.debug("Persistence classes: {}", output.stream()
                .map((cls) -> cls.getName()).collect(Collectors.joining("\n")));

        return output;
    }

    public List<Class> getClassesFromPersistenceFile(String resourcePath) throws IOException {
        
        try(final InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resourcePath)) {
        
            final Document doc = new DomReaderImpl().read(in);

            final NodeList nodeList = doc.getElementsByTagName("class");
            
            final List<Class> output = new ArrayList<>(nodeList.getLength());
            
            for(int i = 0; i< nodeList.getLength(); i++) {
                
                final Node clzNode = nodeList.item(i);
                
                final NodeList children = clzNode.getChildNodes();
                
                if(children.getLength() > 0) {
                    
                    final String className = children.item(0).getTextContent();
                    
                    try{
                        final Class clz = Class.forName(className);
                        output.add(clz);
                    }catch(Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            
            LOG.debug("Persistence classes: {}", output.stream().map((cls) -> cls.getName()).collect(Collectors.joining("\n")));

            return output;
        }
    }
}
