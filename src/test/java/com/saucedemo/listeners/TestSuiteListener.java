package com.saucedemo.listeners;

import com.saucedemo.utils.SlackNotifier;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;

public class TestSuiteListener implements ISuiteListener, ITestListener {
    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static int skippedTests = 0;
    private static long suiteStartTime;

    @Override
    public void onStart(ISuite suite) {
        suiteStartTime = System.currentTimeMillis();
        System.out.println("Test Suite Execution started");
    }

    @Override
    public void onFinish(ISuite suite) {
        long duration = System.currentTimeMillis() - suiteStartTime;
        String durationStr = String.format("%d min, %d sec",
                (duration / 1000) / 60, (duration / 1000) % 60);

        System.out.println("\n=== Test Execution Summary ===");
        System.out.println("Total Tests: " + totalTests);
        System.out.println("Passed: " + passedTests);
        System.out.println("Failed: " + failedTests);
        System.out.println("Skipped: " + skippedTests);
        System.out.println("Duration: " + durationStr);

        // Generate Allure Report and Slack notification
        String allureReportUrl = generateAllureReportUrl();
        SlackNotifier.sendTestResults(
                totalTests, passedTests, failedTests, skippedTests, durationStr, allureReportUrl
        );
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        totalTests++;
        passedTests++;
    }

    @Override
    public void onTestFailure(ITestResult result) {
        totalTests++;
        failedTests++;
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        totalTests++;
        skippedTests++;
    }

    private static String generateAllureReportUrl() {
        // Generate static report first
        generateStaticAllureReport();

        // Check environment
        String ciEnvironment = detectCiEnvironment();

        switch (ciEnvironment) {
            case "GITHUB_ACTIONS":
                return getGitHubActionUrl();
            case "LOCAL":
            default:
                // For local development, return file URL or localhost if serving
                return "http://localhost:8088";
        }
    }

    private static void generateStaticAllureReport() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "allure", "generate", "build/allure-results", "--clean", "-o", "build/allure-report"
            );
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Allure report generated successfully");
                // Start server for local viewing
                startAllureServer();
            } else {
                System.err.println("Failed to generate Allure report");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to generate Allure report: " + e.getMessage());
        }
    }

    private static void startAllureServer() {
        try {
            // Start allure serve in background thread
            Thread serverThread = new Thread(() -> {
                try {
                    ProcessBuilder pb = new ProcessBuilder(
                            "allure", "serve", "build/allure-results", "--host", "localhost", "--port", "8088"
                    );
                    pb.start();
                } catch (IOException e) {
                    System.err.println("Failed to start Allure server: " + e.getMessage());
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();

            // Give server time to start
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String detectCiEnvironment() {
        if (System.getenv("GITHUB_ACTIONS") != null) {
            return "GITHUB_ACTIONS";
        }
        return "LOCAL";
    }

    private static String getGitHubActionUrl() {
        String repoName = System.getenv("GITHUB_REPOSITORY");
        String runId = System.getenv("GITHUB_RUN_ID");

        if (repoName != null && runId != null) {
            return "https://github.com/" + repoName + "/actions/runs/" + runId;
        }
        return "http://localhost:8088";
    }
}
