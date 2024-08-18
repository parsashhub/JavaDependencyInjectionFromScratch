package com.example.DI;

import com.example.annotations.*;
import com.example.enums.Scope;
import org.reflections.Reflections;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DIContainer {
    // Single map to store both singleton instances and prototype classes
    private Map<String, Object> components = new HashMap<>();
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
            String className = componentClass.getName();
            System.out.println(componentClass + "\tscope " + scope);

            if (scope == Scope.SINGLETON) {
                try {
                    Object instance = createInstance(componentClass);
                    components.put(className, instance);
                    System.out.println(componentClass.getName() + "created successfully and the dependency has been injected.\n");

                    // Handle components with qualifiers
                    Qualifier qualifier = componentClass.getAnnotation(Qualifier.class);
                    if (qualifier != null)
                        qualifiedComponents.put(qualifier.value(), instance);

                } catch (Exception e) {
                    throw new RuntimeException("Failed to create component: " + componentClass.getName(), e);
                }
            } else if (scope == Scope.PROTOTYPE) {
                // Handle PROTOTYPE scoped components
                components.put(className, componentClass);

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

        for (var constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                // If the constructor is annotated with @Inject, inject its parameters
                Class<?>[] paramTypes = constructor.getParameterTypes();
                Object[] params = new Object[paramTypes.length];

                for (int i = 0; i < paramTypes.length; i++) {
                    // Inject dependencies into constructor parameters
                    params[i] = getComponent(paramTypes[i].getName());
                }

                return componentClass.cast(constructor.newInstance(params));
            }
        }

        // If no @Inject constructor is found, use the default constructor
        return componentClass.getDeclaredConstructor().newInstance();
    }

    // Method to get a component by its class type
    public <T> T getComponent(String className) {
        Object component = components.get(className);
        // Prototype case
        if (component instanceof Class) {

            try {
                Class<?> componentClass = (Class<?>) component;
                T instance = (T) createInstance(componentClass);
                injectComponentDependencies(instance);
                invokePostConstructMethods(instance);  // Invoke @PostConstruct methods for prototype components
                return (T) componentClass.cast(instance);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create prototype component: " + className, e);
            }
        } else {
            return (T) component;
        }
    }

    // Method to inject dependencies into a specific component
    private void injectComponentDependencies(Object component) {
        Field[] fields = component.getClass().getDeclaredFields();

        for (var field : fields) {
            // Check if the field is annotated with @Autowired or @Inject
            if (field.isAnnotationPresent(Autowired.class) || field.isAnnotationPresent(Inject.class)) {
                try {
                    field.setAccessible(true);  // Allow access to private fields

                    // Check if the field has a @Qualifier annotation
                    Qualifier qualifier = field.getAnnotation(Qualifier.class);
                    Object dependency;

                    // Get the specific qualified component
                    if (qualifier != null) {
                        dependency = qualifiedComponents.get(qualifier.value());
                    }
                    // Get the component by its type
                    else {
                        dependency = getComponent(field.getType().getName());
                    }
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
        for (Object component : components.values()) {
            // Skip prototype classes
            if (!(component instanceof Class))
                injectComponentDependencies(component);

        }
    }

    private void initializePostConstructMethods() {
        for (Object component : components.values()) {
            if (!(component instanceof Class)) {  // Skip prototype classes
                invokePostConstructMethods(component);
            }
        }
    }

    // Method to invoke @PostConstruct methods on a specific component
    private void invokePostConstructMethods(Object component) {
        Method[] methods = component.getClass().getDeclaredMethods();

        for (var method : methods) {
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

    public Map<String, Object> getComponents() {
        return components;
    }

    public Map<String, Object> getQualifiedComponents() {
        return qualifiedComponents;
    }
}
