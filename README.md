# SauceDemo Web Automation Framework

A comprehensive web automation testing framework for https://www.saucedemo.com/ built with Selenium WebDriver, Cucumber BDD, TestNG, and Allure reporting.

## 🚀 Features

- **BDD with Cucumber**: Write tests in Gherkin syntax
- **Page Object Model**: Clean separation of page elements and actions
- **Parallel Execution**: Run tests in parallel using TestNG
- **Cross-browser Testing**: Support Chrome, Firefox, and Edge
- **Beautiful Reports**: Allure reports with screenshots on failure
- **Slack Notification**: Get test results in your Slack channel
- **Docker Support**: Run tests in containerized environment
- **CI/CD Integration**: GitHub Actions workflow for automated testing
- **Automatic Driver Management**: WebDriverManager handles driver binaries

## 📋 Pre-requisites

- Java 21 or higher
- Gradle 8 or higher
- Docker (for containerized execution)
- Git

## 🛠️ Setup

1. Clone the repository:
```bash
git clone https://github.com/davidsusant/saucedemo-automation.git
cd saucedemo-automation
```

2. Configure properties:
- Update `src/test/resources/config/config.properties` with your settings
- Update `src/test/resources/config/slack.properties` with you settings, especially Slack Webhook URL and Slack Channel Name

3. Install dependencies:
```bash
./gradlew clean build
```

## 🏃 Running Tests

### Local Execution

Run all tests:
```bash
./gradlew clean test
```

Run specific tags:
```bash
./gradlew clean test -Dcucumber.filter.tags="@smoke"
```

Run with specific browser:
```bash
./gradlew clean test -Dbrowser=firefox
```

Run in headless mode:
```bash
./gradlew clean test -Dheadless=true
```

### Docker Execution

Using Docker Compose:
```bash
docker-compose -f docker/docker-compose.yml up
```

Using Dockerfile:
```bash
# Clean up failed container
docker rm -f selenium-chrome 2>/dev/null || true

# Re-build docker image
docker build -f docker/Dockerfile -t saucedemo-tests .

# Start Selenium Grid
docker run -d --name selenium-chrome -p 4444:4444 -p 7900:7900 --shm-size=2gb seleniarm/standalone-chromium:latest
  
# Create a temporary container, run tests, copy report, then remove
docker run --link selenium-chrome -e SELENIUM_GRID_URL=http://selenium-chrome:4444 -v $(pwd)/allure-results:/app/allure-results --name temp-saucedemo-test saucedemo-tests

# Run tests with GitHub Pages URL
docker run --rm --link selenium-chrome -e SELENIUM_GRID_URL=http://selenium-chrome:4444 -e ALLURE_REPORT_URL=https://davidsusant.github.io/saucedemo-automation -v $(pwd)/allure-results:/app/allure-results saucedemo-tests

# Copy the report immediately after
docker cp temp-saucedemo-test:/app/build/allure-report ./

# Clean up the container
docker rm temp-saucedemo-test

# Open the report locally with Python web server
cd allure-report && python3 -m http.server 8088

# Or if you have Allure CLI installed
allure open ./allure-report

# Clean up when done
docker stop selenium-chrome && docker rm selenium-chrome
```