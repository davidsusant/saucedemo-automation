package com.saucedemo.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.ConnectException;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class WebDriverManager {
    private static final ConfigReader configReader = new ConfigReader();
    private static WebDriver driver;
    private static boolean isInitialized = false;


    public static WebDriver getDriver() {
        if (driver == null) {
            initializeDriver();
        }
        return driver;
    }

    /**
     * Get driver and navigate to the application URL
     * This is the main method to use when starting tests
     */
    public static WebDriver getDriverAndNavigate() {
        if (driver == null) {
            initializeDriver();
            navigateToApplication();
        } else if (!isInitialized) {
            navigateToApplication();
        }
        return driver;
    }

    private static void initializeDriver() {
        String browser = configReader.getProperty("browser");

        // Try to determine environment and create appropriate driver
        if (isRunningInDocker() || isRunningInCI()) {
            driver = createRemoteDriver(getGridUrlForBrowser(browser), browser);
        } else {
            // Local environment - try local grid first, then fallback to local driver
            driver = createDriverWithFallback(browser);
        }

        // Set implicit wait
        String implicitWait = configReader.getProperty("implicit.wait");
        if (implicitWait != null && !implicitWait.isEmpty()) {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(Long.parseLong(implicitWait)));
        } else {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10)); // Default 10 seconds
        }

        // Set page load timeout
        String pageLoadTimeout = configReader.getProperty("page.load.timeout");
        if (pageLoadTimeout != null && !pageLoadTimeout.isEmpty()) {
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(Long.parseLong(pageLoadTimeout)));
        } else {
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30)); // Default 30 seconds
        }

        // Maximize window (for local environment only)
        if (!isRunningInDocker() && !isRunningInCI()) {
            driver.manage().window().maximize();
        }

        System.out.println("WebDriver initialized successfully with browser: " + browser);
    }

    // Navigate to the main application URL from config
    public static void navigateToApplication() {
        String appUrl = configReader.getProperty("app.url");
        if (appUrl == null || appUrl.isEmpty()) {
            appUrl = "https://www.saucedemo.com/"; // Default fallback
            System.out.println("Warning: app.url not found in config, using default: " + appUrl);
        }

        navigateToUrl(appUrl);
        isInitialized = true;
    }

    // Navigate to a specific URL
    public static void navigateToUrl(String url) {
        if (driver == null) {
            initializeDriver();
        }

        System.out.println("Navigating to " + url);
        driver.get(url);

        // Add implicit wait after navigation
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        System.out.println("Current URL after navigation: " + driver.getCurrentUrl());
        System.out.println("Page title: " + driver.getTitle());
    }

    private static boolean isRunningInDocker() {
        // Check if running inside Docker container
        return System.getenv("DOCKER_CONTAINER") != null ||
                System.getProperty("java.awt.headless") != null ||
                new java.io.File("/.dockerenv").exists();
    }

    private static boolean isRunningInCI() {
        // Check if running in CI environment (GitHub Actions)
        return System.getenv("GITHUB_ACTIONS") != null;
    }

    private static WebDriver createRemoteDriver(String gridUrl, String browser) {
        try {
            System.out.println("Using Selenium Grid at: " + gridUrl + " with browser: " + browser);
            URL url = new URL(gridUrl);

            switch (browser) {
                case "chrome":
                    return new RemoteWebDriver(url, createChromeOptions());
                case "firefox":
                    return new RemoteWebDriver(url, createFirefoxOptions());
                case "edge":
                    return new RemoteWebDriver(url, createEdgeOptions());
                default:
                    throw new IllegalArgumentException("Unsupported browser: " + browser);
            }
        } catch (Exception e) {
            if (e.getCause() instanceof ConnectException ||
                e.getCause() instanceof UnknownHostException) {
                throw new RuntimeException("Cannot connect to Selenium Grid at: " + gridUrl, e);
            }
            throw new RuntimeException("Failed to create remote driver for browser: " + browser, e);
        }
    }

    private static WebDriver createDriverWithFallback(String browser) {
        // First, try local Selenium Grid (if user has one running)
        try {
            String localGridUrl = getLocalGridUrlForBrowser(browser);
            System.out.println("Attempting to connect to local Selenium Grid at: " + localGridUrl);
            return createRemoteDriver(localGridUrl, browser);
        } catch (Exception e) {
            System.out.println("Local Selenium Grid not available, using local " + browser + " driver");
            return createLocalDriver(browser);
        }
    }

    private static WebDriver createLocalDriver(String browser) {
        System.out.println("Using local " + browser + " driver");

        switch (browser) {
            case "chrome":
                io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
                return new ChromeDriver(createChromeOptions());
            case "firefox":
                io.github.bonigarcia.wdm.WebDriverManager.firefoxdriver().setup();
                return new FirefoxDriver(createFirefoxOptions());
            case "edge":
                io.github.bonigarcia.wdm.WebDriverManager.edgedriver().setup();
                return new EdgeDriver(createEdgeOptions());
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
    }

    private static String getGridUrlForBrowser(String browser) {
        // Map browser to appropriate Selenium Grid service
        switch (browser) {
            case "firefox":
                return configReader.getProperty("firefox.grid.url");
            case "edge":
                return configReader.getProperty("edge.grid.url");
            case "chrome":
            default:
                return configReader.getProperty("chrome.grid.url");
        }
    }

    private static String getLocalGridUrlForBrowser(String browser) {
        // For local grid, typically all browsers use same hub
        return configReader.getProperty("local.grid.url");
    }

    private static ChromeOptions createChromeOptions() {
        ChromeOptions options = new ChromeOptions();

        // Common Chrome options
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-password-manager");
        options.addArguments("--disable-save-password-bubble");
        options.addArguments("--disable-password-generation");
        options.addArguments("--disable-features=VizDisplayCompositor");

        // Additional option for remote/Docker environment
        if (isRunningInDocker() || isRunningInCI()) {
            options.addArguments("--remote-debugging-port=9222");
            options.addArguments("--headless");
        }

        // Disable automation indicators
        options.setExperimentalOption("useAutomationExtension", false);
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

        // Configure preferences
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("autofill.credit_card_enabled", false);
        prefs.put("autofill.profile_enabled", false);
        prefs.put("credentials_enable_service", false);
        prefs.put("password_manager_enabled", false);
        prefs.put("profile.default_content_setting_values.notifications", 2);
        prefs.put("profile.default_content_settings.popups", 0);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);

        return options;
    }

    private static FirefoxOptions createFirefoxOptions() {
        FirefoxOptions options = new FirefoxOptions();

        // Common Firefox options
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        // Additional options for remote/Docker environment
        if (isRunningInDocker() || isRunningInCI()) {
            options.addArguments("--headless");
        }

        // Firefox preferences
        options.addPreference("dom.webnotifications.enabled", false);
        options.addPreference("media.volume_scale", "0.0");

        return options;
    }

    private static EdgeOptions createEdgeOptions() {
        EdgeOptions options = new EdgeOptions();

        // Common Edge options
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        // Additional options for remote/Docker environment
        if (isRunningInDocker() || isRunningInCI()) {
            options.addArguments("--headless");
        }

        // Disable automation indicators
        options.setExperimentalOption("useAutomationExtension", false);
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

        return options;
    }

    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    // Utility methods for navigation
    public static String getCurrentUrl() {
        return driver != null ? driver.getCurrentUrl() : null;
    }

    public static String getPageTitle() {
        return driver != null ? driver.getTitle() : null;
    }

    public static void refreshPage() {
        if (driver != null) {
            driver.navigate().refresh();
        }
    }
}
