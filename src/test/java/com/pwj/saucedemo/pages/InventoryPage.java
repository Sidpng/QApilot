// Project Title     : SauceDemo Automation Framework using Playwright with Java
// File Name         : InventoryPage.java
// Author Name       : Siddhartha Upadhyay
// GitHub Link       : https://github.com/YourUsername/saucedemo-playwright-java
// Creation Date     : 01-May-2025
// Description       : Page Object Model class for Inventory Page – used for adding products to cart and accessing cart.

package com.pwj.saucedemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class InventoryPage {

    private Page page;
    private Locator cartIcon;

    // Constructor to initialize page and cart icon locator
    public InventoryPage(Page page) {
        this.page = page;
        this.cartIcon = page.locator(".shopping_cart_link");
    }

    /**
     * Clicks the "Add to cart" button for the specified product
     * @param productName Exact name of the product as displayed (e.g., "Sauce Labs Backpack")
     */
    public void clickAddToCart(String productName) {
        Locator addToCartButton = page.locator(
            "xpath=//div[text()='" + productName + "']/ancestor::div[@class='inventory_item']//button"
        );
        addToCartButton.click();
    }

    /**
     * Clicks the cart icon directly from the header
     */
    public void clickCartIcon() {
        cartIcon.click();
    }

    /**
     * Semantic wrapper to navigate to cart page from inventory
     * Improves test readability by abstracting cart icon click
     */
    public void goToCart() {
        clickCartIcon();
    }
}
