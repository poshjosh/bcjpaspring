package com.bc.jpa.spring.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class JpaUtil {

    private static final Logger LOG = LoggerFactory.getLogger(JpaUtil.class);
    
    private JpaUtil() { }
    
    public static Object convertToType(Object candidate, Class targetType) {
        final Object result;
        if(targetType.equals(Short.class)) {
            if(candidate instanceof Short) {
                result = (Short)candidate;
            }else{
                result = Short.parseShort(candidate.toString());
            }
        }else if(targetType.equals(Integer.class)) {
            if(candidate instanceof Integer) {
                result = (Integer)candidate;
            }else{
                result = Integer.parseInt(candidate.toString());
            }
        }else if(targetType.equals(Long.class)) {
            if(candidate instanceof Long) {
                result = (Long)candidate;
            }else{
                result = Long.parseLong(candidate.toString());
            }
        }else if(targetType.equals(String.class)) {
            result = candidate.toString();
        }else{
            result = candidate;
        }
        return result;
    }
    
    /**
     * This method formats delegates of classes passed around by some 
     * Persistence APIs to the actual class representing the domain type. 
     * 
     * We could use {@link org.hibernate.Hibernate#unproxy(java.lang.Object)}.
     * However we are working with a class, as opposed to an instance.
     * 
     * <p>
     * For example given domain type <code>com.domain.Person</code>
     * some persistence APIs were observed to pass around types of format
     * <code>com.domain.Person$HibernateProxy$uvcIsv</code>. 
     * </p>
     * Given argument of <code>com.domain.Person$HibernateProxy$uvcIsv</code>,
     * this method return's <code>com.domain.Person</code>
     * @param type
     * @return 
     */
    public static Class deduceActualDomainType(Class type) {
        
        Class output = getTopmostEnclosingClass(type);
        
        final String className = output.getName();
        
        final int end = className.indexOf('$');
        
        if(end > 0) {
            final String newClassName = className.substring(0, end);
            LOG.trace("Will attempt to use: {}", newClassName);
            try{
                output = Class.forName(newClassName);
                LOG.debug("Formatted {} to {}", output, newClassName);
            }catch(ClassNotFoundException e) {
                LOG.warn(e.toString());
            }
        }
        return output;
    } 
    
    private static Class getTopmostEnclosingClass(Class type) {
        Class output = type;
        while(output.isAnonymousClass()) {
            final Class next = output.getEnclosingClass();
            if(next == null || output.equals(next)) {
                break;
            }
            output = next;
        }
        if(output != type) {
            LOG.debug("Formatted {} to {}", type, output);
        }
        return output;
    }
}
