package com.example;

import com.example.annotations.Component;


@Component
public class ServiceA {
    public void serve() {
        System.out.println("ServiceA is serving...");
    }
}
