package com.example;


import com.example.annotations.*;

@Component
public class B {

    @Autowired
    private C c;

    public B() {
        System.out.println("B created");
    }
}
