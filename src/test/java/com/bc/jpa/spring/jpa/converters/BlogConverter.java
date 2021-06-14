package com.bc.jpa.spring.jpa.converters;

import com.bc.jpa.spring.domain.Blog;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
@Converter(autoApply = true)
public class BlogConverter implements AttributeConverter<Blog, String> {
  
    private static final Logger LOG = LoggerFactory.getLogger(BlogConverter.class);
    
    @Override
    public String convertToDatabaseColumn(Blog entityAttribute) {
        final String databaseValue = entityAttribute == null ? null : 
                String.valueOf(entityAttribute.getId());
        LOG.trace("Converted: {} to: {}", entityAttribute, databaseValue);
        return databaseValue;
    }
 
    @Override
    public Blog convertToEntityAttribute(String databaseValue) {
        final Integer id;
        if (databaseValue == null) {
            id = null;
        }else{
            try{
                id = Integer.valueOf(databaseValue);
            }catch(NumberFormatException e) {
                throw new RuntimeException(e);
            }
        }
        final Blog entityAttribute = new Blog(id);
        LOG.trace("Converted: {} to: {}", databaseValue, entityAttribute);
        return entityAttribute;
    }
}
