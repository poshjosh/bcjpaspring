package com.bc.jpa.spring;

import com.bc.xml.DomReaderImpl;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author hp
 */
public class ClassesFromPersistenceXmlFileSupplierImpl 
        implements ClassesFromPersistenceXmlFileSupplier{
    
    private static final Logger LOG = LoggerFactory
            .getLogger(ClassesFromPersistenceXmlFileSupplierImpl.class);

    public ClassesFromPersistenceXmlFileSupplierImpl() { }
    
    @Override
    public List<Class> get(String resourcePath) throws IOException {
        
        try(final InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resourcePath)) {

            final Document doc = new DomReaderImpl().read(in);

            final List<Class> output;
            
            if(doc == null) {
                
                LOG.debug("Persistence file not found: {}", resourcePath);
                
                output = Collections.EMPTY_LIST;
                
            }else{
            
                final NodeList nodeList = doc.getElementsByTagName("class");

                final List<Class> temp = new ArrayList<>(nodeList.getLength());

                for(int i = 0; i< nodeList.getLength(); i++) {

                    final Node clzNode = nodeList.item(i);

                    final NodeList children = clzNode.getChildNodes();

                    if(children.getLength() > 0) {

                        final String className = children.item(0).getTextContent();

                        try{
                            final Class clz = Class.forName(className);
                            temp.add(clz);
                        }catch(Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                
                output = Collections.unmodifiableList(temp);
            }
            
            LOG.trace("Persistence classes: {}", output.stream()
                    .map((cls) -> cls.getName()).collect(Collectors.joining("\n", "\n", "")));

            return output;
        }
    }
}

