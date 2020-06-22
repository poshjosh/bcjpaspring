package com.bc.jpa.spring;

import java.util.Objects;
import org.springframework.core.convert.converter.Converter;

/**
 * @author hp
 */
public class ConvertToType<T> implements Converter<Object, Object>{

    private final Class<T> targetType;

    public ConvertToType(Class<T> targetType) {
        this.targetType = Objects.requireNonNull(targetType);
    }
    
    public boolean isSupportedTargetType(Class type) {
        return Short.class.equals(type) ||
                Integer.class.equals(type) ||
                Long.class.equals(type) ||
                String.class.equals(type);
    }
    
    @Override
    public Object convert(Object id) {
        final Object result;
        if(targetType.equals(Short.class)) {
            if(id instanceof Short) {
                result = (Short)id;
            }else{
                result = Short.parseShort(id.toString());
            }
        }else if(targetType.equals(Integer.class)) {
            if(id instanceof Integer) {
                result = (Integer)id;
            }else{
                result = Integer.parseInt(id.toString());
            }
        }else if(targetType.equals(Long.class)) {
            if(id instanceof Long) {
                result = (Long)id;
            }else{
                result = Long.parseLong(id.toString());
            }
        }else if(targetType.equals(String.class)) {
            result = id.toString();
        }else{
            result = id;
        }
        return result;
    }
}
