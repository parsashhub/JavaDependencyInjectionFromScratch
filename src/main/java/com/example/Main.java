package com.example;

import com.example.DI.ApplicationContext;
import com.example.interfaces.IGreetingService;

public class Main {
    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ApplicationContext("com.example");

        GreetingClient client = context.getBean(GreetingClient.class);
        client.greet("parsa");

        IGreetingService spanishService = context.getBean(SpanishGreetingService.class);
        spanishService.greet("Juan");

        AppConfig appConfig = context.getBean(AppConfig.class);
        System.out.println(appConfig.getAppName());
    }
}