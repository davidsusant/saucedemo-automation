FROM eclipse-temurin:21-jdk

# Install system dependencies
RUN apt-get update && apt-get install -y \
    chromium-browser \
    chromium-chromedriver \
    curl \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Install Allure CI
RUN wget -O allure.tgz https://github.com/allure-framework/allure2/releases/download/2.34.0/allure-2.34.0.tgz \
    && tar -xzf allure.tgz \
    && mv allure-2.34.0 /opt/allure \
    && ln -s /opt/allure/bin/allure /usr/local/bin/allure \
    && rm allure.tgz

# Copy project files
COPY . /app
WORKDIR /app

# Make gradlew executable
RUN chmod +x ./gradlew

# Download dependencies and compile
RUN ./gradlew build -x test

# Default command to run tests
CMD ["./gradlew", "clean", "test"]