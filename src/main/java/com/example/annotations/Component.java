package com.example.annotations;


import com.example.enums.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Annotation to mark a class as a component to be managed by the DI container
@Retention(RetentionPolicy.RUNTIME)  // Makes this annotation available at runtime
@Target(ElementType.TYPE)            // This annotation can only be applied to classes
public @interface Component {
    Scope scope() default Scope.SINGLETON;
}
