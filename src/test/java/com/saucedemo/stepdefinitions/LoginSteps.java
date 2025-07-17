package com.saucedemo.stepdefinitions;

import com.saucedemo.pages.LoginPage;
import com.saucedemo.pages.ProductsPage;
import com.saucedemo.utils.WebDriverManager;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Step;
import org.testng.Assert;

public class LoginSteps {
    private LoginPage loginPage;
    private ProductsPage productsPage;

    @Given("I am on the login page")
    @Step("Navigate to login page")
    public void i_am_on_the_login_page() {
        // WebDriver will handle navigation to the application URL
        loginPage = new LoginPage(WebDriverManager.getDriverAndNavigate());

        // Ad debug information
        System.out.println("Current URL: " + WebDriverManager.getCurrentUrl());
        System.out.println("Page Title: " + WebDriverManager.getPageTitle());

        // Verify login page is displayed
        Assert.assertTrue(loginPage.isLoginPageDisplayed(), "Login page is not displayed. Current URL: " + WebDriverManager.getCurrentUrl());
    }

    @When("I enter username {string} and password {string}")
    @Step("Enter credentials - Username: {0}, Password: {1}")
    public void i_enter_username_and_password(String username, String password) {
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
    }

    @When("I click on login button")
    @Step("Click login button")
    public void i_click_on_login_button() {
        loginPage.clickLogin();
    }

    @Then("I should be redirected to products page")
    @Step("Verify redirection to products page")
    public void i_should_be_redirected_to_products_page() {
        productsPage = new ProductsPage(WebDriverManager.getDriver());
        Assert.assertTrue(productsPage.isProductsPageDisplayed(), "Products page is not displayed");
    }

    @Then("I should see {string} as page title")
    @Step("Verify page title: {0}")
    public void i_should_see_as_page_title(String expectedTitle) {
        String actualTitle = productsPage.getPageTitleText();
        Assert.assertEquals(actualTitle, expectedTitle, "Page title mismatch");
    }
}
