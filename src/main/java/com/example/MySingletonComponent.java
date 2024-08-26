package com.example;

import com.example.annotations.Component;
import com.example.enums.Scope;

@Component(scope = Scope.SINGLETON)
public class MySingletonComponent {
    private int counter = 0;

    // Method to increment the counter
    public void incrementCounter() {
        counter++;
    }

    public int getCounter() {
        return counter;
    }
}
