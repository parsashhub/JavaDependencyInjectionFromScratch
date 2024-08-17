package com.example;

import com.example.annotations.Component;
import com.example.annotations.Qualifier;
import com.example.enums.Scope;
import com.example.interfaces.IGreetingService;

@Component(scope = Scope.SINGLETON)
@Qualifier("englishGreeting")
public class EnglishGreetingService implements IGreetingService {

    public void greet(String name) {
        System.out.println("Hello, " + name + "!");
    }
}
