package com.saucedemo.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private Properties properties;
    private static final String CONFIG_PATH = "src/test/resources/config/config.properties";

    public ConfigReader() {
        properties = new Properties();
        try {
            FileInputStream fis = new FileInputStream(CONFIG_PATH);
            properties.load(fis);
            fis.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration file: " + CONFIG_PATH, e);
        }
    }

    public String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Property '" + key + "' not found in configuration file");
        }
        return value.trim();
    }

    public String getProperty(String key, String defaultValue) {
        String value = properties.getProperty(key);
        return (value != null) ? value.trim() : defaultValue;
    }
}
