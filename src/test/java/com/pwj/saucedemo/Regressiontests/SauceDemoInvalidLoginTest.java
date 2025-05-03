package com.pwj.saucedemo.Regressiontests;

import com.pwj.saucedemo.base.BaseTest;
import com.pwj.saucedemo.pages.SauceDemoHomePage;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

/**
 * Project Title: SauceDemo Playwright Automation
 * File Name: SauceDemoInvalidLoginTest.java
 * Author: Siddhartha Upadhyay
 * GitHub: https://github.com/Sidpng
 * Creation Date: 29-April-2025
 * Description: This test class attempts to login with invalid credentials and verifies the error message.
 */

public class SauceDemoInvalidLoginTest extends BaseTest {

    @Test
    public void testInvalidLoginShowsErrorMessage() {
    	SauceDemoHomePage landingPage = new SauceDemoHomePage(page);
        landingPage.login("invalid_user", "wrong_password");

        String actualError = landingPage.getErrorMessage();
        String expectedError = "Epic sadface: Username and password do not match any user in this service";

        assertEquals(actualError, expectedError, "Error message mismatch on invalid login.");
    }
}
