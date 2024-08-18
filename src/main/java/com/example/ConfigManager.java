package com.example;

// Sample class with singleton  design pattern
public class ConfigManager {
    private static ConfigManager instance = new ConfigManager();

    // Private constructor to prevent instantiation
    private ConfigManager() {
    }

    // Public method to provide access to the single instance
    public static ConfigManager getInstance() {
        return instance;
    }

    public void loadConfigurations() {
        System.out.println("Configurations loaded.");
    }
}