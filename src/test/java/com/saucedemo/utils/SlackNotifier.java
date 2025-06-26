package com.saucedemo.utils;

import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import com.slack.api.webhook.WebhookResponse;

import java.io.IOException;
import java.util.Properties;

public class SlackNotifier {
    private static final Slack slack = Slack.getInstance();
    private static String webhookUrl;
    private static boolean notificationsEnabled;

    static {
        loadSlackConfig();
    }

    private static void loadSlackConfig() {
        try {
            Properties props = new Properties();
            props.load(SlackNotifier.class.getClassLoader()
                    .getResourceAsStream("config/slack.properties"));
            webhookUrl = props.getProperty("slack.webhook.url");
            notificationsEnabled = Boolean.parseBoolean(
                    props.getProperty("slack.notifications.enabled", "false"));
        } catch (IOException e) {
            System.err.println("Failed to load Slack configuration: " + e.getMessage());
            notificationsEnabled = false;
        }
    }

    public static void sendTestResults(int totalTests, int passed, int failed, int skipped, String duration, String reportUrl) {
        if (!notificationsEnabled || webhookUrl == null || webhookUrl.isEmpty()) {
            return;
        }

        String status = (failed == 0) ? "✅ PASSED" : "❌ FAILED";
        String color = (failed == 0) ? "#36a64f" : "#ff0000";

        String message = String.format(
                "*Test Execution Summary*\n" +
                "Status: %s\n" +
                "Total Tests: %d\n" +
                "Passed: %d ✅\n" +
                "Failed: %d ❌\n" +
                "Skipped: %d ⏭\n" +
                "Duration: %s\n" +
                "Report: <%s|View Allure Report>",
                status, totalTests, passed, failed, skipped, duration, reportUrl
        );

        Payload payload = Payload.builder()
                .text(message)
                .build();

        try {
            WebhookResponse response = slack.send(webhookUrl, payload);
            if (response.getCode() != 200) {
                System.err.println("Failed to send Slack notification: " + response.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Error sending Slack notification:: " + e.getMessage());
        }
    }
}
