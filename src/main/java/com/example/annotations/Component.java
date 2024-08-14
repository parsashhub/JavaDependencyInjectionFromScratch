package com.example.annotations;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to mark a class as DI component.
 * The DI container will manage instances of this class.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
}
