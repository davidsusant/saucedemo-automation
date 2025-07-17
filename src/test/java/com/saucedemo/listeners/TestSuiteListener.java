package com.saucedemo.listeners;

import com.saucedemo.utils.ConfigReader;
import com.saucedemo.utils.SlackNotifier;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;

public class TestSuiteListener implements ISuiteListener, ITestListener {
    private static final ConfigReader configReader = new ConfigReader();

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static int skippedTests = 0;
    private static long suiteStartTime;

    private static final String ALLURE_REPORT_PORT = configReader.getProperty("allure.report.port");

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

        // Generate static Allure report first
        generateAllureReport();

        // Generate Allure Report and send Slack notification
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

    private static void generateAllureReport() {
        try {
            System.out.println("Generating Allure report...");
            // Generate static Allure report from results
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "allure", "generate", "build/allure-results", "--clean", "-o", "build/allure-report"
            );
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Allure report generated successfully at: build/allure-report");
            } else {
                System.err.println("Failed to generate Allure report (exit code: " + exitCode + ")");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to generate Allure report: " + e.getMessage());
        }
    }

    private static String generateAllureReportUrl() {
        String ciEnvironment = detectCiEnvironment();

        switch (ciEnvironment) {
            case "GITHUB_ACTIONS":
                return getGitHubPagesUrl();
            case "DOCKER":
                return getDockerReportUrl();
            case "LOCAL":
            default:
                return getLocalReportUrl();
        }
    }

    private static String detectCiEnvironment() {
        if (System.getenv("GITHUB_ACTIONS") != null) {
            return "GITHUB_ACTIONS";
        } else if (System.getenv("SELENIUM_GRID_URL") != null) {
            return "DOCKER";
        }
        return "LOCAL";
    }

    private static String getGitHubPagesUrl() {
        String repoOwner = System.getenv("GITHUB_REPOSITORY_OWNER");
        String repoName = System.getenv("GITHUB_REPOSITORY");
        String runId = System.getenv("GITHUB_RUN_ID");
        String runNumber = System.getenv("GITHUB_RUN_NUMBER");

        if (repoName != null) {
            // Extract just the repo name without owner
            String repo = repoOwner.contains("/") ? repoName.split("/")[1] : repoName;

            // Option 1: Static GitHub Pages URL
            if (repoOwner != null) {
                return String.format("https://%s.github.io/%s/allure-report", repoOwner, repo);
            }

            // Option 2: With run number for unique URLs
            if (runNumber != null) {
                return String.format("https://%s.github.io/%s/runs/%s", repoOwner, repo, runNumber);
            }
        }

        // Fallback to GitHub Actions run URL
        if (repoName != null && runId != null) {
            return "https://github.com/" + repoName + "/actions/runs/" + runId;
        }

        return "GitHub Actions - Report URL not configured";
    }

    private static String getDockerReportUrl() {
        // For Docker environment - can be configured via environment variable
        String reportUrl = System.getenv("ALLURE_REPORT_URL");
        if (reportUrl != null && !reportUrl.isEmpty()) {
            return reportUrl;
        }

        // Default Docker setup - use different port than Selenium Grid
        return "http://localhost:" + ALLURE_REPORT_PORT;
    }

    private static String getLocalReportUrl() {
        // For local development, provide options
        String reportPath = System.getProperty("user.dir") + "/build/allure-report/index.html";

        System.out.println("\n=== Allure Report Options ===");
        System.out.println("1. Static Report: file://" + reportPath);
        System.out.println("2. Live Server: http://localhost:" + ALLURE_REPORT_PORT);
        System.out.println("3. Manual command: allure serve build/allure-results");
        System.out.println("4. Manual open: allure open build/allure-report --port " + ALLURE_REPORT_PORT);

        // Try to start local server in background (optional)
        boolean startServer = Boolean.parseBoolean(System.getProperty("allure.start.server", "false"));
        if (startServer) {
            startLocalAllureServer();
            return "http://localhost:" + ALLURE_REPORT_PORT;
        }

        // Return static file path by default
        return "file://" + reportPath;
    }

    private static void startLocalAllureServer() {
        try {
            System.out.println("Starting Allure server on port " + ALLURE_REPORT_PORT + "...");

            // Start Allure serve in background thread for local development
            Thread serverThread = new Thread(() -> {
                try {
                    ProcessBuilder pb = new ProcessBuilder(
                            "allure", "open", "build/allure-report", "--port", String.valueOf(ALLURE_REPORT_PORT)
                    );
                    pb.inheritIO();
                    Process process = pb.start();

                    // Add shutdown hook to stop the server
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        process.destroyForcibly();
                    }));

                    process.waitFor();
                } catch (IOException | InterruptedException e) {
                    System.err.println("Failed to start Allure server: " + e.getMessage());
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();

            // Give server time to start
            Thread.sleep(3000);
            System.out.println("Allure server should be available at: http://localhost:" + ALLURE_REPORT_PORT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while starting Allure server");
        }
    }
}
