package com.example.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to mark a method to be called after dependency injection is done.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PostConstruct {
}
