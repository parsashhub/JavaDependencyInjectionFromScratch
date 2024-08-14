package com.example;

import com.example.annotations.Component;
import com.example.annotations.Inject;
import com.example.annotations.PostConstruct;
import com.example.annotations.Scope;
import com.example.enums.ScopeType;
import com.example.interfaces.IServiceB;

/**
 * A client class that depends on ServiceB.
 * ServiceB is injected into this class through the constructor.
 * This class is a singleton and will be initialized once.
 */
@Component
@Scope("prototype")
//@Scope(ScopeType.PROTOTYPE)
public class Client {
    @Inject
    private IServiceB serviceB;

    @PostConstruct
    public void init() {
        System.out.println("Client initialized");
    }

    public void process() {
        serviceB.doSomething();
    }

}
