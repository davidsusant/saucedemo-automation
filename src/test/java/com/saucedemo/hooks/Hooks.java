package com.saucedemo.hooks;

import com.saucedemo.utils.AllureReportUtil;
import com.saucedemo.utils.ScreenshotUtil;
import com.saucedemo.utils.SlackNotifier;
import com.saucedemo.utils.WebDriverManager;
import io.cucumber.java.*;
import io.qameta.allure.Allure;
import org.openqa.selenium.WebDriver;

public class Hooks {
    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static int skippedTests = 0;
    private static long suitesStartTime;

    @BeforeAll
    public static void beforeAll() {
        suitesStartTime = System.currentTimeMillis();
        System.out.println("Test Suite Execution started");
    }

    @Before
    public void setUp(Scenario scenario) {
        totalTests++;
        AllureReportUtil.addEnvironmentInfo();
        System.out.println("Starting scenario: " + scenario.getName());
    }

    @After
    public void tearDown(Scenario scenario) {
        WebDriver driver = WebDriverManager.getDriver();

        if (scenario.isFailed()) {
            failedTests++;
            if (driver != null) {
                ScreenshotUtil.attachScreenshotToAllure(driver, "Failed_Screenshot");
                Allure.addAttachment("URL", driver.getCurrentUrl());
                Allure.addAttachment("Page Source", "text/html", driver.getPageSource(), ".html");
            }
        } else if (scenario.getStatus().toString().equals("SKIPPED")) {
            skippedTests++;
        } else {
            passedTests++;
        }

        if (driver != null) {
            WebDriverManager.quitDriver();
        }
    }

    @AfterAll
    public static void afterAll() {
        long duration = System.currentTimeMillis() - suitesStartTime;
        String durationStr = String.format("%d min, %d sec",
                (duration / 1000) / 60, (duration / 1000) % 60);

        System.out.println("\n=== Test Execution Summary ===");
        System.out.println("Total Tests: " + totalTests);
        System.out.println("Passed: " + passedTests);
        System.out.println("Failed: " + failedTests);
        System.out.println("Skipped: " + skippedTests);
        System.out.println("Duration: " + durationStr);

        // Send Slack notification
        SlackNotifier.sendTestResults(
                totalTests, passedTests, failedTests, skippedTests, durationStr, "http://localhost:8080/allure-report"
        );
    }
}
