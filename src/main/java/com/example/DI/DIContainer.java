package com.example.DI;

import com.example.annotations.*;
import com.example.enums.Scope;
import exceptions.CircularDependencyException;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DIContainer {
    // Single map to store both singleton instances and prototype classes
    private Map<String, Object> components = new ConcurrentHashMap<>();
    private Map<String, Object> qualifiedComponents = new ConcurrentHashMap<>();
    // Properties to hold key-value pairs
    private Properties properties = new Properties();
    private Map<Class<?>, Set<Class<?>>> classDependencies = new ConcurrentHashMap<>();

    // Constructor that takes a base package to scan for components
    public DIContainer(String basePackage) throws Exception {
        loadProperties();                   // Load properties from application.properties
        scanComponents(basePackage);        // Scan for components in the provided package
        injectDependencies();               // Inject dependencies into the components
        initializePostConstructMethods();   // Invoke @PostConstruct methods after dependencies are injected
        detectCircularDependencies();       // Check for circular dependencies among components
    }

    // Method to load properties from application.properties file
    private void loadProperties() throws Exception {
        try (var input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find application.properties");
                return;
            }
            properties.load(input);
        }
    }

    // Method to scan components annotated with @Component within a given package
    private void scanComponents(String basePackage) throws Exception {
        System.out.println("start scanning " + basePackage + " package...");
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> componentClasses = reflections.getTypesAnnotatedWith(Component.class);

        // Iterate over all detected component classes
        for (Class<?> componentClass : componentClasses) {
            Component componentAnnotation = componentClass.getAnnotation(Component.class);
            Scope scope = componentAnnotation.scope(); // Get the scope (SINGLETON or PROTOTYPE) of the component
            String className = componentClass.getName();
            System.out.println(componentClass + "\tscope " + scope);

            if (scope == Scope.SINGLETON) {
                try {
                    // Create and store the singleton instance if it's not already present
                    components.putIfAbsent(className, createInstance(componentClass));
                    Object instance = components.get(className);
                    System.out.println(componentClass.getName() + "created successfully and the dependency has been injected.\n");

                    // Handle components with qualifiers
                    Qualifier qualifier = componentClass.getAnnotation(Qualifier.class);
                    if (qualifier != null) qualifiedComponents.putIfAbsent(qualifier.value(), instance);

                } catch (Exception e) {
                    throw new RuntimeException("Failed to create component: " + componentClass.getName(), e);
                }
            } else if (scope == Scope.PROTOTYPE) {
                // Store the class itself instead of an instance
                components.putIfAbsent(className, componentClass);

                // Handle components with qualifiers
                Qualifier qualifier = componentClass.getAnnotation(Qualifier.class);
                if (qualifier != null) qualifiedComponents.putIfAbsent(qualifier.value(), componentClass);
            }
        }
    }

    // Method to create a new instance of a component class
    private <T> T createInstance(Class<T> componentClass) throws Exception {
        // Get all constructors of the class
        Constructor<?>[] constructors = componentClass.getConstructors();

        // Iterate over constructors and look for @Inject annotations
        for (var constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                // If @Inject annotation is found, get the parameter types
                Class<?>[] paramTypes = constructor.getParameterTypes();
                Object[] params = new Object[paramTypes.length];

                // Inject dependencies into the constructor parameters
                for (int i = 0; i < paramTypes.length; i++) {
                    // Inject dependencies into constructor parameters
                    params[i] = getComponent(paramTypes[i].getName());
                }
                // Create a new instance with the injected dependencies
                return componentClass.cast(constructor.newInstance(params));
            }
        }

        // If no @Inject constructor is found, use the default constructor
        return componentClass.getDeclaredConstructor().newInstance();
    }

    // Method to get a component by its class type
    public <T> T getComponent(String className) {
        Object component = components.get(className);

        // If the component is of type Class, it means it's a PROTOTYPE component
        if (component instanceof Class) {
            try {
                // Create a new instance of the prototype component
                Class<?> componentClass = (Class<?>) component;
                T instance = (T) createInstance(componentClass);

                // Inject dependencies and invoke @PostConstruct methods
                injectComponentDependencies(instance);
                invokePostConstructMethods(instance);
                return (T) componentClass.cast(instance);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create prototype component: " + className, e);
            }
        } else {
            // If it's not a Class, return the singleton instance
            return (T) component;
        }
    }

    // Method to inject dependencies into a specific component
    private void injectComponentDependencies(Object component) {
        Class<?> componentClass = component.getClass();
        // Set to track dependencies of the component
        Set<Class<?>> dependencies = new LinkedHashSet<>();
        // Stack to track the current path of dependency injection (for circular dependency detection)
        Stack<Class<?>> currentPath = new Stack<>();

        Field[] fields = componentClass.getDeclaredFields();

        // Iterate over the fields to check for @Autowired, @Inject, and @Value annotations
        for (var field : fields) {
            if (field.isAnnotationPresent(Autowired.class) || field.isAnnotationPresent(Inject.class)) {
                try {
                    field.setAccessible(true);  // Allow access to private fields

                    Qualifier qualifier = field.getAnnotation(Qualifier.class);
                    Object dependency;

                    // If the field has a qualifier, get the dependency by the qualifier value
                    if (qualifier != null) {
                        dependency = qualifiedComponents.get(qualifier.value());
                    } else {
                        // Otherwise, get the component by its class name
                        dependency = getComponent(field.getType().getName());
                    }

                    if (dependency != null) {
                        dependencies.add(field.getType());
                        // Check for circular dependencies
                        currentPath.push(componentClass);
                        currentPath.pop();
                    }

                    field.set(component, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject dependencies into: " + componentClass.getName(), e);
                }
            } else if (field.isAnnotationPresent(Value.class)) {
                Value valueAnnotation = field.getAnnotation(Value.class);
                String key = valueAnnotation.value().replace("${", "").replace("}", "");
                String value = properties.getProperty(key);

                try {
                    field.setAccessible(true);
                    Object convertedValue = convertValue(field.getType(), value);
                    field.set(component, convertedValue);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject value into: " + componentClass.getName(), e);
                }
            }
        }
        // Record the dependencies of the component class
        classDependencies.put(componentClass, dependencies);
    }

    // Method to convert the string value to the appropriate field type
    private Object convertValue(Class<?> type, String value) {
        if (type.equals(String.class)) {
            return value;
        } else if (type.equals(int.class) || type.equals(Integer.class)) {
            return Integer.parseInt(value);
        } else if (type.equals(double.class) || type.equals(Double.class)) {
            return Double.parseDouble(value);
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return Boolean.parseBoolean(value);
        }
        throw new RuntimeException("Unsupported type for @Value annotation: " + type);
    }

    // Method to inject dependencies into singleton components
    private void injectDependencies() {
        for (Object component : components.values()) {
            // Skip prototype classes
            if (!(component instanceof Class)) injectComponentDependencies(component);

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
            // If the method is annotated with @PostConstruct, invoke it
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

    // Method to detect circular dependencies in the entire dependency graph
    public void detectCircularDependencies() {
        Set<Class<?>> visited = new HashSet<>();
        Set<Class<?>> inStack = new HashSet<>();

        // Iterate over all classes in the classDependencies map
        for (Class<?> clazz : classDependencies.keySet()) {
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
        Set<Class<?>> dependencies = classDependencies.getOrDefault(current, Collections.emptySet());
        for (Class<?> dependency : dependencies)
            detectCycle(dependency, visited, inStack, path);

        // After recursion, remove the class from the current path and the stack
        path.pop();
        inStack.remove(current);
    }

    public Map<String, Object> getComponents() {
        return components;
    }

    public Map<String, Object> getQualifiedComponents() {
        return qualifiedComponents;
    }

    public Map<Class<?>, Set<Class<?>>> getClassDependencies() {
        return classDependencies;
    }
}
