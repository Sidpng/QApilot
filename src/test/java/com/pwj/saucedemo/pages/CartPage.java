// Project Title     : SauceDemo Automation Framework using Playwright with Java
// File Name         : CartPage.java
// Author Name       : Siddhartha Upadhyay
// GitHub Link       : https://github.com/YourUsername/saucedemo-playwright-java
// Creation Date     : 01-May-2025
// Description       : Page Object Model class for Cart Page – used to validate cart contents and proceed to checkout.

package com.pwj.saucedemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class CartPage {

    private Locator cartItemLabel;
    private Locator checkoutButton;
    private Locator continueShoppingButton;

    public CartPage(Page page) {
        this.cartItemLabel = page.locator(".inventory_item_name");         // Product name in cart
        this.checkoutButton = page.locator("#checkout");                  // Checkout button
        this.continueShoppingButton = page.locator("#continue-shopping"); // Continue shopping button
    }

    /**
     * Verifies if any product is present in the cart
     * @return true if at least one product is found
     */
    public boolean isProductInCart() {
        return cartItemLabel.isVisible();
    }

    /**
     * Returns the name of the product present in the cart
     * @return Product name as string
     */
    public String getProductNameInCart() {
        return cartItemLabel.textContent().trim();
    }

    /**
     * Clicks the Checkout button to proceed to the next checkout step
     */
    public void clickCheckout() {
        checkoutButton.click();
    }

    /**
     * Clicks the Continue Shopping button to return to the inventory page
     */
    public void clickContinueShopping() {
        continueShoppingButton.click();
    }
}
