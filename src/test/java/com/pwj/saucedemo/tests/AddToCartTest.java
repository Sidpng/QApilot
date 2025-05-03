
/**
 * Project Title: SauceDemo Playwright Automation
 * File Name: AddToCartTest.java
 * Author: Siddhartha Upadhyay
 * GitHub: https://github.com/Sidpng
 * Creation Date: 01-May-2025
 * Description: This test logs in with a standard user and verifies that a product can be added to the cart.
 */


package com.pwj.saucedemo.tests;

import com.microsoft.playwright.*;
import com.pwj.saucedemo.base.BaseTest;
import com.pwj.saucedemo.pages.InventoryPage;
import com.pwj.saucedemo.pages.SauceDemoHomePage;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

public class AddToCartTest extends BaseTest {

    @Test
    public void testAddFirstProductToCart() {
        // Step 1: Login
    	SauceDemoHomePage landingPage = new SauceDemoHomePage(page);
        landingPage.login("standard_user", "secret_sauce");
        logger.info("Logging into SauceDemo website with standard user credentials");

        // Step 2: Interact with Inventory Page
        InventoryPage inventoryPage = new InventoryPage(page);
        inventoryPage.clickAddToCart("Sauce Labs Backpack");

        logger.info("Added product to cart");

        // Step 3: Click on cart icon to go to cart
        logger.info("Navigating to cart");
        inventoryPage.goToCart();

        // Step 4: Assert that cart badge shows 1 item added
        Locator cartBadge = page.locator(".shopping_cart_badge");
        logger.info("Checking if one item has been added to the cart.");
        assertEquals(cartBadge.textContent().trim(), "1", "Cart count did not reflect added item.");
    }
}

