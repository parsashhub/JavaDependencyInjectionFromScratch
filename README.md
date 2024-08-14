# Custom Dependency Injection Framework in Java

This repository contains a custom implementation of a Dependency Injection (DI) framework in Java, inspired by the
concepts used in popular frameworks like Spring. The project demonstrates how DI works and includes examples of
both `singleton` and `prototype` scopes.

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Setup](#setup)
- [Usage](#usage)
    - [Singleton Scope](#singleton-scope)
    - [Prototype Scope](#prototype-scope)
- [How It Works](#how-it-works)
- [Components](#components)
- [License](#license)

## Introduction

Dependency Injection is a design pattern used to implement IoC (Inversion of Control), allowing the creation of
dependent objects outside of a class and providing those objects to the class. This project is a simplified version of a
DI container that can scan packages, resolve dependencies, and manage the lifecycle of components.

## Dependency Injection (DI)

DI is a design pattern that allows an object to receive its dependencies from an external source rather than creating
them itself. This makes code more modular, testable, and maintainable.

## Inversion of Control (IoC)

In short, "Don't call us, we'll call you."

- Inversion of Control (IoC) is a design principle. It is used to invert different kinds of controls (ie. object
  creation or dependent object creation and binding ) in object-oriented design to achieve loose coupling.
- Dependency injection one of the approach to implement the IoC.
- IoC helps to decouple the execution of a task from implementation.
- IoC helps it focus a module on the task it is designed for.
- IoC prevents side effects when replacing a module.

## Annotations

Annotations in Java provide metadata for the code and can be used to mark classes, fields, methods, etc., for special
processing. In our DI framework, annotations like @Component, @Inject, @Scope, and @PostConstruct help manage the
components and their dependencies.

## Scopes

Scopes define the lifecycle of an object. The two common scopes are:

- Singleton: A single instance is created and shared throughout the application.
- Prototype: A new instance is created every time it's requested.

## Features

- **Custom DI Container**: Manages the creation and injection of dependencies.
- **Singleton Scope**: Components are created once and shared across the application.
- **Prototype Scope**: A new instance of a component is created every time it is requested.
- **Annotation-Based Configuration**: Components are identified and managed using annotations like `@Component`
  and `@Scope`.
- **Package Scanning**: Automatically detects and registers components from a specified package.

## Setup

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- An IDE or text editor to view and edit the code

### Clone the Repository

```bash
git clone https://github.com/yourusername/custom-di-framework.git
cd custom-di-framework
```

### Compile and Run

You can compile and run the project using your IDE or from the command line:

```bash
javac -d out/ src/com/example/*.java
java -cp out/ com.example.Main
```

## Usage

### Singleton Scope

In the singleton scope, only one instance of a component is created and shared across the entire application. Here’s how
you can see it in action:

```java

@Component
@Scope("singleton")
class Client {
    @Inject
    private ServiceB serviceB;

    @Inject
    private PrototypeService prototypeService;

    public void process() {
        serviceB.doSomething();
        prototypeService.serve();
    }
}
```

- The `Client` class is marked as a singleton, and the DI container will ensure that only one instance is created and
  shared.

### Prototype Scope

In the prototype scope, a new instance of the component is created every time it is requested:

```java

@Component
@Scope("prototype")
class PrototypeService {
    private final int instanceId = (int) (Math.random() * 1000);

    public void serve() {
        System.out.println("PrototypeService instance ID: " + instanceId + " is serving...");
    }
}
```

- The `PrototypeService` class will have a different instance every time it is injected or resolved.

### Running the Application

You can see the DI in action by running the `Main` class:

```java
public class Main {
    public static void main(String[] args) throws Exception {
        // Create the DI container
        DIContainer container = new DIContainer();
        // Scan the package for components and register them
        container.scanPackage("com.example");

        // Resolve the Client component
        Client client = container.resolve(Client.class);

        // Call the process method to demonstrate dependency injection
        client.process();
    }
}
```

This will output logs demonstrating the lifecycle of the singleton and prototype components.

## How It Works

### Key Components

1. **DIContainer**: The core of the DI framework, responsible for scanning packages, registering components, and
   resolving dependencies.
2. **@Component**: Annotation used to mark classes as components to be managed by the DI container.
3. **@Scope**: Annotation to specify the scope (`singleton` or `prototype`) of a component.
4. **@Inject**: Annotation used to indicate where dependencies should be injected.

### Example Components

- **Client**: A singleton component that depends on `ServiceB` and `PrototypeService`.
- **PrototypeService**: A prototype component that creates a new instance every time it is requested.

## Components

- `DIContainer.java`: The DI container that handles the creation and injection of components.
- `Component.java`: Annotation to mark a class as a component.
- `Scope.java`: Annotation to define the scope of a component.
- `Inject.java`: Annotation to mark dependencies that need to be injected.
- `Main.java`: Entry point of the application, demonstrating the DI framework in action.
- `Client.java`, `ServiceA.java`, `ServiceB.java`: Example classes showing how to use the DI framework.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
