package com.example;

import com.example.annotations.Component;
import com.example.annotations.Inject;

/**
 * A service class that depends on ServiceA.
 * ServiceA is injected into this class.
 */
@Component
public class ServiceB {
    @Inject
    private ServiceA serviceA;

    public void doSomething() {
        serviceA.serve();
        System.out.println("ServiceB is doing something...");
    }
}
