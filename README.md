
---

# Custom Dependency Injection Framework in Java

This repository contains a custom implementation of a Dependency Injection (DI) framework in Java, inspired by the
concepts used in popular frameworks like Spring. The project demonstrates how DI works and includes examples of
both `singleton` and `prototype` scopes.

## Overview

This project implements a custom Dependency Injection (DI) framework in Java, similar to Spring. It provides annotations
for managing dependencies and allows components to be injected into other components automatically.

## Table of Contents

- [Introduction](#Introduction)
- [Prerequisites](#Prerequisites)
- [Installation](#installation)
- [Project Structure](#project-structure)
- [Annotations](#annotations)
    - [@Component](#component)
    - [@Inject](#inject)
    - [@Autowired](#autowired)
    - [@Qualifier](#qualifier)
    - [@PostConstruct](#postConstruct)
    - [@Value](#value)
- [Usage](#usage)
    - [Component Registration](#component-registration)
    - [Dependency Injection](#dependency-injection)
    - [Qualifier Usage](#qualifier-usage)
- [Example](#example)
- [License](#license)

## Introduction

Dependency Injection is a design pattern used to implement IoC (Inversion of Control), allowing the creation of
dependent objects outside of a class and providing those objects to the class. This project is a simplified version of a
DI container that can scan packages, resolve dependencies, and manage the lifecycle of components.

### Dependency Injection (DI)

DI is a design pattern that allows an object to receive its dependencies from an external source rather than creating
them itself. This makes code more modular, testable, and maintainable.

### Inversion of Control (IoC)

In short, "Don't call us, we'll call you."

- Inversion of Control (IoC) is a design principle. It is used to invert different kinds of controls (ie. object
  creation or dependent object creation and binding ) in object-oriented design to achieve loose coupling.
- Dependency injection one of the approach to implement the IoC.
- IoC helps to decouple the execution of a task from implementation.
- IoC helps it focus a module on the task it is designed for.
- IoC prevents side effects when replacing a module.

### Annotations

Annotations in Java provide metadata for the code and can be used to mark classes, fields, methods, etc., for special
processing. In our DI framework, annotations like @Component, @Inject, @Scope, and @PostConstruct help manage the
components and their dependencies.

### Scopes

Scopes define the lifecycle of an object. The two common scopes are:

- Singleton: A single instance is created and shared throughout the application.
- Prototype: A new instance is created every time it's requested.

### Features

- **Custom DI Container**: Manages the creation and injection of dependencies.
- **Singleton Scope**: Components are created once and shared across the application.
- **Prototype Scope**: A new instance of a component is created every time it is requested.
- **Annotation-Based Configuration**: Components are identified and managed using annotations like `@Component`
  and `@Scope`.
- **Package Scanning**: Automatically detects and registers components from a specified package.


## Prerequisites

- Java Development Kit (JDK) 8 or higher
- An IDE or text editor to view and edit the code

## Installation

1. **Clone the repository**:

   ```bash
   git clone https://github.com/parsashhub/JavaDependencyInjectionFromScratch.git
   ```

2. **Navigate to the project directory**:

   ```bash
   cd JavaDependencyInjectionFromScratch
   ```

3. **Compile the project**:

   Use a Java IDE (e.g., IntelliJ IDEA, Eclipse) or the command line:

   ```bash
   javac -d bin src/com/example/*.java
   ```

4. **Run the project**:

   ```bash
   java -cp bin com.example.Main
   ```

## Project Structure

```
/src
│
├── /com/example
│   ├── /DI 
│   │    ├── ApplicationContext.java           // Main dependency injection context
│   │    ├── BeanDefinition.java             
│   │    └── BeanFactory.java            
│   ├── /Enums               // Enum for bean scopes (SINGLETON, PROTOTYPE)
│   │    └── Scope.java
│   ├── /annotations
│   │    ├── Component.java            
│   │    ├── Autowired.java            
│   │    ├── Inject.java               
│   │    ├── PostConstruct.java               
│   │    ├── Value.java               
│   │    └── Qualifier.java            
│   ├── /interfaces
│   │    └── IGreetingService.java            
│   ├── A.java        
│   ├── B.java        
│   ├── C.java                      // A, B, C classes create a circular dependency exception
│   ├── AppConfig.java              // example of @Value usage
│   ├── EnglishGreetingService.java // Singleton component with qualifier
│   ├── SpanishGreetingService.java // Prototype component with qualifier
│   ├── GreetingClient.java         // Client class using injected services
│   ├── MyClient.java         
│   ├── MyService.java         
│   └── Main.java                   // Main class to run the example
```

## Annotations

### `@Component`

Marks a class as a component, making it eligible for DI. Components can have different scopes.

- **Usage**: Place this annotation on classes you want to manage as beans.
- **Scope Options**:
    - `Scope.SINGLETON` (default): A single instance is shared.
    - `Scope.PROTOTYPE`: A new instance is created each time it is injected.

### `@Inject`

Indicates that a dependency should be injected. Works on fields, constructors, or methods.

- **Usage**: Place this annotation on fields or constructors to indicate where dependencies should be injected.

### `@Autowired`

Similar to `@Inject`, it marks a dependency for injection. Commonly used in Spring-based projects.

- **Usage**: Can be used interchangeably with `@Inject` on fields, constructors, or methods.

### `@Qualifier`

Used to resolve ambiguity when multiple implementations of an interface are available.

- **Usage**: Place this annotation on a field, constructor, or method along with `@Inject` or `@Autowired` to specify
  the desired implementation.

### `@PostConstruct`
Used to invoke a method after the construction is done.

- **Usage**: Place this annotation on a method along with `@PostConstruct` to specify
  the desired implementation.

### `@Value`
Used to inject a field from application.properties file.

- **Usage**: Place this annotation on a filed along with `@Value("${app.name}")` to specify
  the desired implementation.

## Usage

### Component Registration

To register a class as a component:

```java

@Component(scope = Scope.SINGLETON)
public class MyService {
    // Class content
}
```

### Dependency Injection

Inject dependencies into another class:

```java

@Component
public class MyClient {

    @Inject // or @Autowired
    private MyService myService;

    public void performAction() {
        myService.doSomething();
    }
}
```

### Qualifier Usage

When multiple implementations of an interface exist:

```java

@Component(scope = Scope.SINGLETON)
@Qualifier("englishGreeting")
public class EnglishGreetingService implements GreetingService {
    // Implementation
}

@Component(scope = Scope.PROTOTYPE)
@Qualifier("spanishGreeting")
public class SpanishGreetingService implements GreetingService {
    // Implementation
}

@Component
public class GreetingClient {

    @Autowired
    @Qualifier("englishGreeting")
    private GreetingService greetingService;

    public void greet(String name) {
        greetingService.greet(name);
    }
}
```

## Example

### Main Class

The `Main.java` file initializes the DI container and retrieves the components:

```java
public class Main {
    public static void main(String[] args) {
        DIContainer container = new DIContainer("com.example");

        GreetingClient client = container.getComponent(GreetingClient.class);
        client.greet("John");

        GreetingService spanishService = container.getComponent(SpanishGreetingService.class);
        spanishService.greet("Juan");
    }
}
```

### Expected Output

When you run the `Main` class, you should see:

```
Hello, John!
¡Hola, Juan!
```

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---
