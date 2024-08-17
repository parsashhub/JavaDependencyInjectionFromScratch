package com.example;

import com.example.annotations.Component;
import com.example.enums.Scope;

@Component(scope = Scope.SINGLETON)
public class MyService {
    public void run() {
        System.out.println("MyService is running...");
    }
}
