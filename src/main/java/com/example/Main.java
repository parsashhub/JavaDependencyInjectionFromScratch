package com.example;

import com.example.DI.DIContainer;
import com.example.interfaces.IGreetingService;

public class Main {
    public static void main(String[] args) throws Exception {
        DIContainer container = new DIContainer("com.example");
        // Get GreetingClient from the container and use it
        GreetingClient client = container.getComponent(GreetingClient.class);
        client.greet("John");

        // Directly access the SpanishGreetingService and use it
        IGreetingService spanishService = container.getComponent(SpanishGreetingService.class);
        spanishService.greet("Juan");
    }
}