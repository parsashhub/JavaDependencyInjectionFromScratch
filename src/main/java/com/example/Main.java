package com.example;

import com.example.DI.DIContainer;

public class Main {
    public static void main(String[] args) throws Exception {
        DIContainer container = new DIContainer();
        // Scan the package for components and register them
        container.scanPackage("com.example");

        System.out.println("component registery:\n" + container.getComponentRegistry() + "\n");
        System.out.println("SingletonObjects:\n" + container.getSingletonObjects() + "\n");

//        ServiceB serviceB = container.resolve(ServiceB.class);
//        serviceB.doSomething();
//        // Same instance of ServiceB will be used
//        ServiceB serviceB2 = container.resolve(ServiceB.class);
//        serviceB2.doSomething();
//
//        Client client = container.resolve(Client.class);
//        client.process();
    }
}