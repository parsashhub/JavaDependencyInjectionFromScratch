package com.example;

import com.example.annotations.Autowired;
import com.example.annotations.Component;
import com.example.annotations.PostConstruct;
import com.example.annotations.Qualifier;
import com.example.interfaces.IGreetingService;

@Component
public class GreetingClient {

    @Autowired // Can also use @Inject
    @Qualifier("englishGreeting")
    private IGreetingService greetingService;

    public void greet(String name) {
        greetingService.greet(name);
    }

    @PostConstruct
    public void init() {
        System.out.println("GreetingClient has been initialized.");
    }
}
