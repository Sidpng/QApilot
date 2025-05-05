// Project Title     : SauceDemo Automation Framework using Playwright with Java
// File Name         : CheckoutFlowTest.java
// Author Name       : Siddhartha Upadhyay
// GitHub Link       : https://github.com/Sidpng/com.pwj.saucedemo.git
// Creation Date     : 01-May-2025
// Description       : Consolidated TestNG class to verify the entire checkout process and all cancel points.

package com.pwj.saucedemo.Regressiontests;

import com.pwj.saucedemo.base.BaseTest;
import com.pwj.saucedemo.pages.*;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CheckoutFlowTest extends BaseTest {

    @Test(description = "Complete checkout from cart to order confirmation")
    public void testFullCheckoutFlow() {
        SauceDemoHomePage homePage = new SauceDemoHomePage(page);
        homePage.login("standard_user", "secret_sauce");

        InventoryPage inventoryPage = new InventoryPage(page);
        inventoryPage.clickAddToCart("Sauce Labs Backpack");
        inventoryPage.goToCart();

        CartPage cartPage = new CartPage(page);
        cartPage.clickCheckout();

        CheckoutFlowPage checkout = new CheckoutFlowPage(page);
        checkout.enterUserInfo("John", "Doe", "500001");
        checkout.continueToOverview();

        Assert.assertTrue(checkout.isOverviewSummaryVisible(), "Summary section should be visible.");
        Assert.assertTrue(checkout.isProductDisplayed(), "Product item should be listed.");

        checkout.finishCheckout();
        Assert.assertTrue(checkout.isConfirmationDisplayed(), "Confirmation message should be displayed.");

        checkout.clickBackHome();
        Assert.assertTrue(page.url().contains("inventory"), "User should be returned to inventory page.");
    }

    @Test(description = "Cancel from Step One should return to cart page")
    public void testCancelFromStepOne() {
        SauceDemoHomePage homePage = new SauceDemoHomePage(page);
        homePage.login("standard_user", "secret_sauce");

        InventoryPage inventoryPage = new InventoryPage(page);
        inventoryPage.clickAddToCart("Sauce Labs Bike Light");
        inventoryPage.goToCart();

        CartPage cartPage = new CartPage(page);
        cartPage.clickCheckout();

        CheckoutFlowPage checkout = new CheckoutFlowPage(page);
        checkout.cancelFromUserInfoPage();

        Assert.assertTrue(page.url().contains("cart"), "Cancel from Step One should go back to cart.");
    }

    @Test(description = "Cancel from overview page should return to inventory")
    public void testCancelFromOverviewStep() {
        SauceDemoHomePage homePage = new SauceDemoHomePage(page);
        homePage.login("standard_user", "secret_sauce");

        InventoryPage inventoryPage = new InventoryPage(page);
        inventoryPage.clickAddToCart("Test.allTheThings() T-Shirt (Red)");
        inventoryPage.goToCart();

        CartPage cartPage = new CartPage(page);
        cartPage.clickCheckout();

        CheckoutFlowPage checkout = new CheckoutFlowPage(page);
        checkout.enterUserInfo("Jane", "Smith", "600002");
        checkout.continueToOverview();

        checkout.cancelFromOverviewPage();
        Assert.assertTrue(page.url().contains("inventory"), "Cancel from Step Two should go back to inventory.");
    }
}
