package com.example.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to mark fields or methods for dependency injection.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
}
