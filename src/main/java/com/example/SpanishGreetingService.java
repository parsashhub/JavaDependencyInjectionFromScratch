package com.example;

import com.example.annotations.Component;
import com.example.annotations.Qualifier;
import com.example.enums.Scope;
import com.example.interfaces.IGreetingService;

@Component(scope = Scope.PROTOTYPE)
public class SpanishGreetingService implements IGreetingService {
    @Override
    public void greet(String name) {
        System.out.println("Hola, " + name + "!");
    }
}
