package com.example;

import com.example.DI.DIContainer;
import com.example.interfaces.IGreetingService;

public class Main {
    public static void main(String[] args) throws Exception {
        DIContainer container = new DIContainer("com.example");
        System.out.println("\n*************************");
        System.out.println("components: " + container.getComponents());
        System.out.println("qualified components: " + container.getQualifiedComponents());
        System.out.println("*************************\n");

        // Get GreetingClient from the container and use it
        GreetingClient client = container.getComponent(GreetingClient.class.getName());
        client.greet("John");

        // Directly access the SpanishGreetingService and use it
        IGreetingService spanishService = container.getComponent(SpanishGreetingService.class.getName());
        spanishService.greet("Juan");

        AppConfig appConfig = container.getComponent(AppConfig.class.getName());
        System.out.println(appConfig.getAppName());
    }
}