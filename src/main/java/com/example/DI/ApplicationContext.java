package com.example.DI;

import com.example.annotations.*;
import com.example.enums.Scope;
import com.example.logger.LogUtils;
import org.reflections.Reflections;

import java.util.*;


/**
 * ApplicationContext is the core class responsible for managing the lifecycle of beans.
 * It handles scanning for components, dependency injection, and lifecycle management (like @PostConstruct).
 */
public class ApplicationContext {
    // BeanFactory for managing bean creation and retrieval
    private final BeanFactory beanFactory = new BeanFactory();

    // Constructor that takes a base package to scan for components
    public ApplicationContext(String basePackage) throws Exception {
        loadProperties();                               // Load properties from application.properties
        scanComponents(basePackage);                    // Scan for components in the provided package
        beanFactory.injectDependencies();               // Inject dependencies into the components
        beanFactory.initializePostConstructMethods();   // Invoke @PostConstruct methods after dependencies are injected
    }

    public <T> T getBean(Class<T> clazz) {
        return beanFactory.getComponent(clazz.getName());
    }

    // Method to load properties from application.properties file
    private void loadProperties() throws Exception {
        try (var input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                LogUtils.warn("Sorry, unable to find application.properties");
                return;
            }
            beanFactory.setProperties(input);
        }
    }

    // Method to scan components annotated with @Component within a given package
    private void scanComponents(String basePackage) throws Exception {
        LogUtils.info("start scanning " + basePackage + " package...");
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> componentClasses = reflections.getTypesAnnotatedWith(Component.class);

        // Iterate over all detected component classes
        for (Class<?> componentClass : componentClasses) {
            Component componentAnnotation = componentClass.getAnnotation(Component.class);
            Scope scope = componentAnnotation.scope();

            String className = componentClass.getName();
            Qualifier qualifier = componentClass.getAnnotation(Qualifier.class);
            LogUtils.info(componentClass + "\tscope " + scope);

            beanFactory.createBean(className, new BeanDefinition(componentClass, scope, qualifier), componentClass);
        }
    }
}




