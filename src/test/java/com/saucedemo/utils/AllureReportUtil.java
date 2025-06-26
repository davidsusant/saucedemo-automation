package com.saucedemo.utils;

import io.qameta.allure.Allure;

public class AllureReportUtil {
    public static void addEnvironmentInfo() {
        Allure.parameter("Browser", System.getProperty("browser", "chrome"));
        Allure.parameter("Environment", System.getProperty("env", "test"));
        Allure.parameter("OS", System.getProperty("os.name"));
        Allure.parameter("Java Version", System.getProperty("java.version"));
    }
}
