package com.saucedemo.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ProductsPage extends BasePage{

    @FindBy(className = "title")
    private WebElement pageTitle;

    public ProductsPage(WebDriver driver) {
        super(driver);
    }

    @Step("Get page title")
    public String getPageTitleText() {
        return getText(pageTitle);
    }

    @Step("Check if products page is displayed")
    public boolean isProductsPageDisplayed() {
        return isElementDisplayed(pageTitle) && getPageTitleText().equals("Products");
    }
}
