package com.example.DI;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class to scan a package and find classes with a specific annotation.
 */
public class PackageScanner {
    /**
     * Scans the specified package for classes annotated with the given annotation.
     *
     * @param packageName the name of the package to scan
     * @param annotation  the annotation to look for
     * @return a set of classes annotated with the specified annotation
     * @throws Exception if scanning fails
     */
    public static Set<Class<?>> getClassesWithAnnotation(String packageName, Class<?> annotation) throws Exception {
        Set<Class<?>> classes = new HashSet<>();
        // Convert package name to file path
        String path = packageName.replace('.', '/');
        // Locate the directory for the package
        File directory = new File(Thread.currentThread().getContextClassLoader().getResource(path).toURI());

        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".class")) {
                        // Construct class name from file name
                        String className = packageName + '.' + file.getName().replace(".class", "");
                        Class<?> cls = Class.forName(className);
                        // Add class to set if it has the specified annotation
                        if (cls.isAnnotationPresent((Class<? extends Annotation>) annotation)) {
                            classes.add(cls);
                        }
                    }
                }
            }
        }

        return classes;
    }
}
