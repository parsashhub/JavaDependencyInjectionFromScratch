package com.example;

import com.example.annotations.Component;
import com.example.annotations.Value;

@Component
public class AppConfig {

    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    @Value("${app.debug}")
    private boolean debugMode;

    @Value("${app.maxConnections}")
    private int maxConnections;

    public String getAppName() {
        return appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public int getMaxConnections() {
        return maxConnections;
    }
}
