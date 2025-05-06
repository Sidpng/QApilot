package com.pwj.saucedemo.tests.Orderplaced_tests;

import org.testng.annotations.Test;
import org.testng.AssertJUnit;
import com.microsoft.playwright.Page;

//Project Title     : SauceDemo Automation Framework using Playwright with Java
//File Name         : StandardUserOrderTest.java
//Author Name       : Siddhartha Upadhyay
//GitHub Link       : https://github.com/YourUsername/saucedemo-playwright-java
//Creation Date     : 01-May-2025
//Description       : Full regression test for standard_user from login to successful order confirmation with screenshot.

import com.pwj.saucedemo.base.BaseTest;
import com.pwj.saucedemo.pages.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Paths;

public class StandardUserOrderTest extends BaseTest {

 @Test(description = "Regression test for standard_user to place a full order successfully")
 public void testStandardUserOrderFlow() {
     logger.info("Starting regression test for standard_user");

     // Step 1: Login
     SauceDemoHomePage homePage = new SauceDemoHomePage(page);
     homePage.login("standard_user", "secret_sauce");
     AssertJUnit.assertTrue("User should land on inventory page", page.url().contains("inventory"));

     // Step 2: Add to Cart
     InventoryPage inventoryPage = new InventoryPage(page);
     inventoryPage.clickAddToCart("Sauce Labs Backpack");
     inventoryPage.goToCart();

     CartPage cartPage = new CartPage(page);
     AssertJUnit.assertEquals(cartPage.getProductNameInCart(), "Sauce Labs Backpack", "Correct product added");
     cartPage.clickCheckout();

     // Step 3: Checkout Flow
     CheckoutFlowPage checkout = new CheckoutFlowPage(page);
     checkout.enterUserInfo("Siddhartha", "Upadhyay", "560001");
     checkout.continueToOverview();

     AssertJUnit.assertTrue("Overview should display summary", checkout.isOverviewSummaryVisible());
     AssertJUnit.assertTrue("Product should appear in overview", checkout.isProductDisplayed());

     checkout.finishCheckout();
     AssertJUnit.assertTrue("Order confirmation should be visible", checkout.isConfirmationDisplayed());

     // Step 4: Screenshot
     String screenshotPath = "test-output/screenshots/standard_user_order_confirmation.png";
     page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(screenshotPath)));
     logger.info("Screenshot saved at: " + screenshotPath);
     
     checkout.clickBackHome();
     AssertJUnit.assertTrue("Back Home should return to inventory", page.url().contains("inventory"));
 }
}
