package com.example.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LogUtils {

    // Prevent instantiation
    private LogUtils() {
    }

    // Static logger instance
    private static final Logger logger = LoggerFactory.getLogger("ApplicationLogger");

    // Static methods for logging
    public static void info(String message) {
        logger.info(message);
    }

    public static void debug(String message) {
        logger.debug(message);
    }

    public static void error(String message) {
        logger.error(message);
    }

    public static void warn(String message) {
        logger.warn(message);
    }

    public static void info(String message, Object... args) {
        logger.info(message, args);
    }

    public static void debug(String message, Object... args) {
        logger.debug(message, args);
    }

    public static void error(String message, Object... args) {
        logger.error(message, args);
    }

    public static void warn(String message, Object... args) {
        logger.warn(message, args);
    }

    public static void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }
}

