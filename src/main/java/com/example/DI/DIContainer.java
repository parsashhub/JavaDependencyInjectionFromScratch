package com.example.DI;

import com.example.annotations.*;
import com.example.enums.Scope;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DIContainer {
    private static final Logger log = LoggerFactory.getLogger(DIContainer.class);
    // Map to store singleton components
    private Map<Class<?>, Object> singletonComponents = new HashMap<>();
    // Map to store prototype component classes
    private Map<Class<?>, Class<?>> prototypeComponents = new HashMap<>();
    // Map to store components with qualifiers
    private Map<String, Object> qualifiedComponents = new HashMap<>();

    // Constructor that takes a base package to scan for components
    public DIContainer(String basePackage) throws Exception {
        scanComponents(basePackage);  // Scan for components in the provided package
        injectDependencies();         // Inject dependencies into the components
        initializePostConstructMethods(); // Invoke @PostConstruct methods after dependencies are injected

    }

    // Method to scan components annotated with @Component within a given package
    private void scanComponents(String basePackage) throws Exception {
        System.out.println("start scanning " + basePackage + " package...");
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> componentClasses = reflections.getTypesAnnotatedWith(Component.class);
        for (Class<?> componentClass : componentClasses) {
            Component componentAnnotation = componentClass.getAnnotation(Component.class);
            Scope scope = componentAnnotation.scope(); // Get the scope (SINGLETON or PROTOTYPE) of the component
            System.out.println(componentClass + "\tscope " + scope);

            if (scope == Scope.SINGLETON) {
                try {
                    Object instance = createInstance(componentClass);
                    System.out.println(componentClass.getName() + "created successfully and the dependency has been injected.\n");
                    singletonComponents.put(componentClass, instance);

                    // Handle components with qualifiers
                    Qualifier qualifier = componentClass.getAnnotation(Qualifier.class);
                    if (qualifier != null)
                        qualifiedComponents.put(qualifier.value(), instance);

                } catch (Exception e) {
                    throw new RuntimeException("Failed to create component: " + componentClass.getName(), e);
                }
            } else if (scope == Scope.PROTOTYPE) {
                // Handle PROTOTYPE scoped components
                prototypeComponents.put(componentClass, componentClass);

                // Handle components with qualifiers
                Qualifier qualifier = componentClass.getAnnotation(Qualifier.class);
                if (qualifier != null)
                    qualifiedComponents.put(qualifier.value(), componentClass);

            }
        }
    }

    // Method to create a new instance of a component class
    private <T> T createInstance(Class<T> componentClass) throws Exception {
        Constructor<?>[] constructors = componentClass.getConstructors();

        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                // If the constructor is annotated with @Inject, inject its parameters
                Class<?>[] paramTypes = constructor.getParameterTypes();
                Object[] params = new Object[paramTypes.length];

                for (int i = 0; i < paramTypes.length; i++) {
                    params[i] = getComponent(paramTypes[i]);  // Inject dependencies into constructor parameters
                }

                return componentClass.cast(constructor.newInstance(params));
            }
        }

        // If no @Inject constructor is found, use the default constructor
        return componentClass.getDeclaredConstructor().newInstance();
    }

    // Method to get a component by its class type
    public <T> T getComponent(Class<T> componentClass) {
        if (singletonComponents.containsKey(componentClass)) {
            // Return the singleton instance if available
            return componentClass.cast(singletonComponents.get(componentClass));
        } else if (prototypeComponents.containsKey(componentClass)) {
            // Create a new prototype instance if it's a prototype-scoped component
            try {
                T instance = createInstance(componentClass);
                injectComponentDependencies(instance);
                invokePostConstructMethods(instance);
                return instance;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create prototype component: " + componentClass.getName(), e);
            }
        }
        throw new RuntimeException("Component not found: " + componentClass.getName());
    }

    // Method to inject dependencies into a specific component
    private void injectComponentDependencies(Object component) {
        Field[] fields = component.getClass().getDeclaredFields();

        for (Field field : fields) {
            // Check if the field is annotated with @Autowired or @Inject
            if (field.isAnnotationPresent(Autowired.class) || field.isAnnotationPresent(Inject.class)) {
                try {
                    field.setAccessible(true);  // Allow access to private fields

                    // Check if the field has a @Qualifier annotation
                    Qualifier qualifier = field.getAnnotation(Qualifier.class);
                    Object dependency;

                    if (qualifier != null)
                        // Get the specific qualified component
                        dependency = qualifiedComponents.get(qualifier.value());
                    else
                        // Get the component by its type
                        dependency = getComponent(field.getType());


                    // Inject the dependency into the field
                    field.set(component, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject dependencies into: " + component.getClass().getName(), e);
                }
            }
        }
    }

    // Method to inject dependencies into singleton components
    private void injectDependencies() {
        for (Object component : singletonComponents.values()) {
            injectComponentDependencies(component);
        }
    }

    private void initializePostConstructMethods() {
        for (Object component : singletonComponents.values()) {
            invokePostConstructMethods(component);
        }
    }

    // Method to invoke @PostConstruct methods on a specific component
    private void invokePostConstructMethods(Object component) {
        Method[] methods = component.getClass().getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                try {
                    method.setAccessible(true);
                    method.invoke(component); // Invoke the method annotated with @PostConstruct
                } catch (Exception e) {
                    throw new RuntimeException("Failed to invoke @PostConstruct method on: " + component.getClass().getName(), e);
                }
            }
        }
    }
}
