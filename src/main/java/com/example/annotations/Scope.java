package com.example.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to specify the scope of a component (e.g., singleton, prototype).
 */
@Retention(RetentionPolicy.RUNTIME)
//@Target(ElementType.TYPE)
public @interface Scope {
    String value();
//    ScopeType value() default ScopeType.SINGLETON;
}
