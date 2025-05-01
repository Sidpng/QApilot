package com.pwj.saucedemo.tests;

import com.microsoft.playwright.*;
import com.pwj.saucedemo.base.BaseTest;
import com.pwj.saucedemo.pages.InventoryPage;
import com.pwj.saucedemo.pages.SauceDemoHomePage;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

/**
 * Project Title: SauceDemo Playwright Automation
 * File Name: AddToCartTest.java
 * Author: Siddhartha Upadhyay
 * GitHub: https://github.com/Sidpng
 * Creation Date: 01-May-2025
 * Description: This test logs in with a standard user and verifies that a product can be added to the cart.
 */


public class AddToCartTest extends BaseTest {

    @Test
    public void testAddFirstProductToCart() {
        // Step 1: Login
    	SauceDemoHomePage landingPage = new SauceDemoHomePage(page);
        landingPage.login("standard_user", "secret_sauce");

        // Step 2: Interact with Inventory Page
        InventoryPage inventoryPage = new InventoryPage(page);
        inventoryPage.clickAddToCart("Sauce Labs Backpack");

        // Step 3: Click on cart icon to go to cart
        inventoryPage.goToCart();

        // Step 4: Assert that cart badge shows 1 item added
        Locator cartBadge = page.locator(".shopping_cart_badge");
        assertEquals(cartBadge.textContent().trim(), "1", "Cart count did not reflect added item.");
    }
}

