package com.saucedemo.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage extends BasePage {

    @FindBy(className = "login_logo")
    private WebElement loginLogo;

    @FindBy(id = "user-name")
    private WebElement usernameInput;

    @FindBy(id = "password")
    private WebElement passwordInput;

    @FindBy(id = "login-button")
    private WebElement loginButton;

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    @Step("Check if login page is displayed")
    public boolean isLoginPageDisplayed() {
        return isElementDisplayed(loginLogo);
    }

    @Step("Enter username: {username}")
    public LoginPage enterUsername(String username) {
        sendKeys(usernameInput, username);
        return this;
    }

    @Step("Enter password")
    public LoginPage enterPassword(String password) {
        sendKeys(passwordInput, password);
        return this;
    }

    @Step("Click login button")
    public void clickLogin() {
        click(loginButton);
    }
}
