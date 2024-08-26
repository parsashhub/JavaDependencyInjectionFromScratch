package com.example.DI;

import com.example.annotations.*;
import com.example.enums.Scope;
import com.example.logger.LogUtils;
import exceptions.CircularDependencyException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BeanFactory {
    private final Map<Class<?>, Set<Class<?>>> classDependencies = new ConcurrentHashMap<>();
    private final Map<String, BeanDefinition> beanDefinitions = new ConcurrentHashMap<>();
    private final Map<String, Object> qualifiedBeans = new ConcurrentHashMap<>();
    private final Map<String, Object> beans = new ConcurrentHashMap<>();
    private Properties properties = new Properties();

    public void createBean(String beanName, BeanDefinition beanDefinition, Class<?> componentClass) {
        // register bean
        beanDefinitions.put(beanName, beanDefinition);
        // create bean
        var classDef = beanDefinitions.get(componentClass.getName());
        var className = classDef.getBeanClass().getName();
        var qualifier = componentClass.getAnnotation(Qualifier.class);

        if (classDef.getScope() == Scope.SINGLETON) {
            try {
                // Create and store the singleton instance if it's not already present
                beans.putIfAbsent(className, createInstance(componentClass));
                Object instance = beans.get(className);
                LogUtils.info(componentClass.getName() + "created successfully and the dependency has been injected.\n");

                if (qualifier != null) qualifiedBeans.putIfAbsent(qualifier.value(), instance);

            } catch (Exception e) {
                throw new RuntimeException("Failed to create component: " + componentClass.getName(), e);
            }
        } else if (classDef.getScope() == Scope.PROTOTYPE) {
            // Store the class itself instead of an instance
            beans.putIfAbsent(className, componentClass);
            if (qualifier != null) qualifiedBeans.putIfAbsent(qualifier.value(), componentClass);
        }
    }

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

    public <T> T getComponent(String className) {
        Object component = beans.get(className);

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

    private void injectComponentDependencies(Object component) {
        Class<?> componentClass = component.getClass();
        // Set to track dependencies of the component
        Set<Class<?>> dependencies = new LinkedHashSet<>();

        // Iterate over the fields to check for @Autowired, @Inject, and @Value annotations
        for (var field : componentClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class) || field.isAnnotationPresent(Inject.class)) {
                try {
                    field.setAccessible(true);  // Allow access to private fields
                    Qualifier qualifier = field.getAnnotation(Qualifier.class);
                    Object dependency;

                    // If the field has a qualifier, get the dependency by the qualifier value
                    // Otherwise, get the component by its class name
                    if (qualifier != null) dependency = qualifiedBeans.get(qualifier.value());
                    else dependency = getComponent(field.getType().getName());

                    if (dependency != null) dependencies.add(field.getType());

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
        detectCircularDependencies();
    }

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

    public void injectDependencies() {
        // iterate through all beans and inject their dependencies method and skip prototype classes
        for (Object component : beans.values())
            if (!(component instanceof Class)) injectComponentDependencies(component);
    }

    public void initializePostConstructMethods() {
        // iterate through all beans and call their PostConstruct method and skip prototype classes
        for (Object component : beans.values())
            if (!(component instanceof Class)) invokePostConstructMethods(component);
    }

    // Method to detect circular dependencies in the entire dependency graph
    private void detectCircularDependencies() {
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

    public Map<Class<?>, Set<Class<?>>> getClassDependencies() {
        return classDependencies;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(InputStream input) throws IOException {
        properties.load(input);
    }
}
