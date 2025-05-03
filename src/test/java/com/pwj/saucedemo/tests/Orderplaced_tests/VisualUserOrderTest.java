// Project Title     : SauceDemo Automation Framework using Playwright with Java
// File Name         : VisualUserOrderTest.java
// Author Name       : Siddhartha Upadhyay
// GitHub Link       : https://github.com/YourUsername/saucedemo-playwright-java
// Creation Date     : 01-May-2025
// Description       : Full regression test for visual_user including order placement and screenshot capture.

package com.pwj.saucedemo.tests.Orderplaced_tests;

import com.microsoft.playwright.Page;
import com.pwj.saucedemo.base.BaseTest;
import com.pwj.saucedemo.pages.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Paths;

public class VisualUserOrderTest extends BaseTest {

    @Test(description = "Regression test for visual_user to complete an order successfully")
    public void testVisualUserOrderFlow() {
        logger.info("Starting regression test for visual_user");

        // Step 1: Login
        SauceDemoHomePage homePage = new SauceDemoHomePage(page);
        homePage.login("visual_user", "secret_sauce");
        Assert.assertTrue(page.url().contains("inventory"), "User should land on inventory page");

        // Step 2: Add to Cart
        InventoryPage inventoryPage = new InventoryPage(page);
        inventoryPage.clickAddToCart("Test.allTheThings() T-Shirt (Red)");
        inventoryPage.goToCart();

        CartPage cartPage = new CartPage(page);
        Assert.assertTrue(cartPage.isProductInCart(), "Product should be visible in cart");
        Assert.assertEquals(cartPage.getProductNameInCart(), "Test.allTheThings() T-Shirt (Red)", "Correct product added");
        cartPage.clickCheckout();

        // Step 3: Checkout Flow
        CheckoutFlowPage checkout = new CheckoutFlowPage(page);
        checkout.enterUserInfo("Visual", "User", "800001");
        checkout.continueToOverview();

        Assert.assertTrue(checkout.isOverviewSummaryVisible(), "Overview should display summary");
        Assert.assertTrue(checkout.isProductDisplayed(), "Product should appear in overview");

        checkout.finishCheckout();
        Assert.assertTrue(checkout.isConfirmationDisplayed(), "Order confirmation should be visible");

        // Step 4: Screenshot
        String screenshotPath = "test-output/screenshots/visual_user_order_confirmation.png";
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(screenshotPath)));
        logger.info("Screenshot saved at: " + screenshotPath);

        checkout.clickBackHome();
        Assert.assertTrue(page.url().contains("inventory"), "Back Home should return to inventory");
    }
}
