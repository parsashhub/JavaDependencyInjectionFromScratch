package com.example;

import com.example.annotations.*;

@Component
public class C {
// if you want to throw error for circular dependency uncomment the next lines.
//    @Autowired
//    private A a;

    public C() {
        System.out.println("C created");
    }
}
