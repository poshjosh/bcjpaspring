package com.bc.jpa.spring;

import com.bc.jpa.dao.functions.GetEntityClasses;
import com.bc.jpa.spring.util.FindClassesInPackage;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.EntityManagerFactory;

/**
 * @author hp
 */
public class DomainClassesBuilder{
    
//    private static final Logger LOG = LoggerFactory.getLogger(DomainClassesBuilder.class);
 
    private boolean buildAttempted;
    
    private Set<Class> classes;
    
    private final Function<EntityManagerFactory, Set<Class>> getEntityClasses;
    private final ClassesFromPersistenceXmlFileSupplier getClassesFromPersistenceXmlFile;
    private final Function<String, List<Class<?>>> findClassesInPackage;

    public DomainClassesBuilder() {
        this(
                new GetEntityClasses(),
                new ClassesFromPersistenceXmlFileSupplierImpl(),
                new FindClassesInPackage());
    }

    public DomainClassesBuilder(
            Function<EntityManagerFactory, Set<Class>> getEntityClasses, 
            ClassesFromPersistenceXmlFileSupplier getClassesFromPersistenceXmlFile, 
            Function<String, List<Class<?>>> findClassesInPackage) {
        this.getEntityClasses = Objects.requireNonNull(getEntityClasses);
        this.getClassesFromPersistenceXmlFile = 
                Objects.requireNonNull(getClassesFromPersistenceXmlFile);
        this.findClassesInPackage = Objects.requireNonNull(findClassesInPackage);
    }
    
    public DomainClasses build() {
        if(this.buildAttempted) {
            throw new IllegalStateException(
                    "You have already called method build(), but it may only be called once. Call reset() before each subsequent call to build()");
        }
        this.buildAttempted = true;
        Objects.requireNonNull(classes, 
                "You have not added any classes. Please do via any of the add methos");
        return new DomainClassesImpl(this.classes);
    }

    public DomainClassesBuilder reset() {
        this.buildAttempted = false;
        this.classes = null;
        return this;
    }

    public DomainClassesBuilder classes(Set<Class> classes) {
        this.classes = Collections.unmodifiableSet(classes);
        return this;
    }
    
    public DomainClassesBuilder addFrom(EntityManagerFactory emf) {
        this.add(this.getEntityClasses.apply(emf));
        return this;
    }

    public DomainClassesBuilder addFromPersistenceXmlFile() {
        this.add(this.getClassesFromPersistenceXmlFile.get());
        return this;
    }
    
    public DomainClassesBuilder addFrom(String persistenceXmlFile) {
        this.add(this.getClassesFromPersistenceXmlFile.apply(persistenceXmlFile));
        return this;
    }

    public DomainClassesBuilder addFromPackages(String... packageNames) {
        if(packageNames != null) {
            for(String packageName : packageNames) {
                List<Class<?>> found = this.findClassesInPackage.apply(packageName);
                this.add(Collections.unmodifiableList(found));
            }
        }
        return this;
    }
    
    public DomainClassesBuilder add(Collection<Class> classesToAdd) {
        if(this.classes == null) {
            this.classes = new LinkedHashSet<>();
        }
        this.classes.addAll(classesToAdd);
        return this;
    }
    
    private static class DomainClassesImpl implements DomainClasses{

        private final Set<Class> classes;
        private final Set<String> classNames;

        public DomainClassesImpl(Set<Class> classes) {
            this.classes = Objects.requireNonNull(classes);
            this.classNames = classes.stream().map(Class::getName).collect(Collectors.toSet());
        }

        @Override
        public boolean test(Class type) {
            return classNames.contains(type.getName());
        }

        @Override
        public Set<Class> get() {
            return this.classes;
        }
    }
}
