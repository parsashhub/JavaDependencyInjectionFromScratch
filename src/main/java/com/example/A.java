package com.example;

import com.example.annotations.*;

@Component
public class A {

    @Autowired
    private B b;

    public A() {
        System.out.println("A created");
    }
}
