package com.example.DI;

import com.example.annotations.*;
import com.example.enums.Scope;
import com.example.logger.LogUtils;
import exceptions.CircularDependencyException;
import org.reflections.Reflections;

import java.util.*;


/**
 * ApplicationContext is the core class responsible for managing the lifecycle of beans.
 * It handles scanning for components, dependency injection, and lifecycle management (like @PostConstruct).
 */
public class ApplicationContext {
    // BeanFactory for managing bean creation and retrieval
    private final BeanFactory beanFactory = new BeanFactory();
    private final String basePackage;

    // Constructor that takes a base package to scan for components
    public ApplicationContext(String basePackage) throws Exception {
        this.basePackage = basePackage;
        loadProperties();                               // Load properties from application.properties
        scanComponents(basePackage);                    // Scan for components in the provided package
        beanFactory.injectDependencies();               // Inject dependencies into the components
        beanFactory.initializePostConstructMethods();   // Invoke @PostConstruct methods after dependencies are injected
        detectCircularDependencies();                   // Check for circular dependencies among components
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

            beanFactory.registerBeanDefinition(className, new BeanDefinition(componentClass, scope, qualifier));
            beanFactory.createBean(componentClass);
        }
    }

    // Method to detect circular dependencies in the entire dependency graph
    private void detectCircularDependencies() {
        Set<Class<?>> visited = new HashSet<>();
        Set<Class<?>> inStack = new HashSet<>();

        // Iterate over all classes in the classDependencies map
        for (Class<?> clazz : beanFactory.getClassDependencies().keySet()) {
            // If the class hasn't been visited yet, check for cycles
            if (!visited.contains(clazz)) {
                detectCycle(clazz, visited, inStack, new Stack<>());
            }
        }
    }

    // Recursive method to detect cycles in the dependency graph
    private void detectCycle(Class<?> current, Set<Class<?>> visited, Set<Class<?>> inStack, Stack<Class<?>> path) {
        // If the current class is already in the stack, a cycle is detected
        if (inStack.contains(current)) throw new CircularDependencyException("Circular dependency detected: " + path);

        // If the class has already been processed, return
        if (visited.contains(current)) return;

        visited.add(current); // Mark the class as visited
        inStack.add(current); // Add the class to the stack
        path.push(current);   // Add the class to the current path

        // Get the dependencies of the current class and recurse into them
        Set<Class<?>> dependencies = beanFactory.getClassDependencies().getOrDefault(current, Collections.emptySet());
        for (Class<?> dependency : dependencies)
            detectCycle(dependency, visited, inStack, path);

        // After recursion, remove the class from the current path and the stack
        path.pop();
        inStack.remove(current);
    }

}




