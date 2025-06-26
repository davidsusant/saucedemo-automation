package com.saucedemo.utils;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;

public class ScreenshotUtil {
    @Attachment(value = "Screenshot", type = "image/png")
    public static byte[] captureScreenshotAsBytes(WebDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

    public static void attachScreenshotToAllure(WebDriver driver, String name) {
        byte[] screenshot = captureScreenshotAsBytes(driver);
        Allure.addAttachment(name, new ByteArrayInputStream(screenshot));
    }
}
