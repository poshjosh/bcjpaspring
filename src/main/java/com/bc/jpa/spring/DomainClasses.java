package com.bc.jpa.spring;

import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author hp
 */
public interface DomainClasses extends Supplier<Set<Class>>, Predicate<Class>{ 

    class Builder extends DomainClassesBuilder{ }
}
