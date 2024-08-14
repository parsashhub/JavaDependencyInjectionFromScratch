package com.example;

import com.example.annotations.Component;
import com.example.annotations.Scope;
import com.example.enums.ScopeType;


@Component
public class ServiceA {
    public void serve() {
        System.out.println("ServiceA is serving...");
    }
}
