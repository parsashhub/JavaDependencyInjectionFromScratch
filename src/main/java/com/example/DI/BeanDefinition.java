package com.example.DI;


import com.example.annotations.Qualifier;
import com.example.enums.Scope;

// BeanDefinition class stores metadata about a bean, including its class type and scope.
public class BeanDefinition {
    private final Class<?> beanClass; // the class type of the bean
    private final Scope scope; // the scope of the bean (SINGLETON or PROTOTYPE)
    private final Qualifier qualifier;

    public BeanDefinition(Class<?> beanClass, Scope scope, Qualifier qualifier) {
        this.beanClass = beanClass;
        this.scope = scope;
        this.qualifier = qualifier;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public Scope getScope() {
        return scope;
    }

    public Qualifier getQualifier() {
        return qualifier;
    }
}
