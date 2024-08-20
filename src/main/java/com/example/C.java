package com.example;

import com.example.annotations.*;

@Component
public class C {

    @Autowired
    private A a;

    public C() {
        System.out.println("C created");
    }
}
