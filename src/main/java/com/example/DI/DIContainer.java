package com.example.DI;

import com.example.annotations.Component;
import com.example.annotations.Inject;
import com.example.annotations.PostConstruct;
import com.example.annotations.Scope;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DIContainer {
    // Stores singleton instances of components
    private Map<Class<?>, Object> singletonObjects = new HashMap<>();
    // Maps component classes to their implementations
    private Map<Class<?>, Class<?>> componentRegistry = new HashMap<>();
    // Keeps track of classes currently being created to detect circular dependencies
    private Set<Class<?>> currentlyInCreation = new HashSet<>();

    /**
     * Scans the specified package for classes annotated with @Component and registers them.
     *
     * @param packageName the name of the package to scan
     * @throws Exception if scanning or registration fails
     */
    public void scanPackage(String packageName) throws Exception {
        // Get all classes annotated with @Component from the package
        Set<Class<?>> components = PackageScanner.getClassesWithAnnotation(packageName, Component.class);

        for (Class<?> component : components) {
            register(component);
        }
    }

    /**
     * Registers a component class in the container.
     * If the component is a singleton, creates and stores its instance.
     *
     * @param componentClass the class of the component to register
     * @throws Exception if instance creation or registration fails
     */
    public void register(Class<?> componentClass) throws Exception {
        // Avoid re-registering the same component
        if (componentRegistry.containsKey(componentClass)) {
            return;
        }
        // Check the component's scope (singleton or prototype)
        Scope scope = componentClass.getAnnotation(Scope.class);
        if (scope == null || scope.value() == "singleton") {
            // Create a singleton instance and store it
            Object singletonInstance = createInstance(componentClass);
            singletonObjects.put(componentClass, singletonInstance);

        } else {
            // Register the component class for prototype instantiation
            componentRegistry.put(componentClass, componentClass);
        }
    }

    /**
     * Creates an instance of the specified component class.
     * Injects dependencies and calls the @PostConstruct method if present.
     *
     * @param componentClass the class of the component to create
     * @return a new instance of the component
     * @throws Exception if instance creation or injection fails
     */
    private Object createInstance(Class<?> componentClass) throws Exception {
        // Create a new instance using the default constructor
        Constructor<?> constructor = componentClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object instance = constructor.newInstance();
        // Inject dependencies into the instance
        injectDependencies(instance);
        // Call the @PostConstruct method if present
        invokePostConstruct(instance);

        return instance;
    }

    /**
     * Resolves a component and its dependencies.
     * Throws an exception if a circular dependency is detected.
     *
     * @param componentClass the class of the component to resolve
     * @param <T>            the type of the component
     * @return an instance of the resolved component
     * @throws Exception if component resolution fails
     */
    public <T> T resolve(Class<T> componentClass) throws Exception {
        // Check for circular dependencies
        if (currentlyInCreation.contains(componentClass)) {
            throw new RuntimeException("Circular dependency detected for " + componentClass.getName());
        }

        // Return the singleton instance if it exists
        if (singletonObjects.containsKey(componentClass)) {
            return componentClass.cast(singletonObjects.get(componentClass));
        }

        if (componentRegistry.containsKey(componentClass)) {
            currentlyInCreation.add(componentClass);
            Object instance = createInstance(componentClass);
            currentlyInCreation.remove(componentClass);
            return componentClass.cast(instance);
        }

        throw new RuntimeException("No component registered for " + componentClass.getName());
    }

    /**
     * Injects dependencies into fields and setter methods annotated with @Inject.
     *
     * @param instance the instance in which to inject dependencies
     * @throws Exception if dependency injection fails
     */
    private void injectDependencies(Object instance) throws Exception {
        // Inject dependencies into fields
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                var dependency = resolve(field.getType());
                field.setAccessible(true);
                field.set(instance, dependency);
            }
        }

        // Inject dependencies via setter methods
        Method[] methods = instance.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Inject.class) && method.getParameterCount() == 1) {
                Object dependency = resolve(method.getParameterTypes()[0]);
                method.setAccessible(true);
                method.invoke(instance, dependency);
            }
        }
    }

    /**
     * Calls methods annotated with @PostConstruct after dependency injection.
     *
     * @param instance the instance in which to invoke the @PostConstruct method
     * @throws Exception if method invocation fails
     */
    private void invokePostConstruct(Object instance) throws Exception {
        Method[] methods = instance.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                method.setAccessible(true);
                method.invoke(instance);
            }
        }
    }

    public Map<Class<?>, Object> getSingletonObjects() {
        return singletonObjects;
    }

    public Map<Class<?>, Class<?>> getComponentRegistry() {
        return componentRegistry;
    }
}
