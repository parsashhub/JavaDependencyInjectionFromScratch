package com.example.enums;

public enum Scope {
    SINGLETON,  // Only one instance of the bean exists in the container
    PROTOTYPE   // A new instance of the bean is created each time it is requested
}