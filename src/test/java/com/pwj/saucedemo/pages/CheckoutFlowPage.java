// Project Title     : SauceDemo Automation Framework using Playwright with Java
// File Name         : CheckoutFlowPage.java
// Author Name       : Siddhartha Upadhyay
// GitHub Link       : https://github.com/YourUsername/saucedemo-playwright-java
// Creation Date     : 01-May-2025
// Description       : Consolidated Page Object Model for the entire checkout process (Step One → Confirmation)

package com.pwj.saucedemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class CheckoutFlowPage {

    // Step One – User Info
    private Locator firstNameInput;
    private Locator lastNameInput;
    private Locator postalCodeInput;
    private Locator continueButton;
    private Locator cancelButtonStepOne;

    // Step Two – Overview
    private Locator summaryInfo;
    private Locator productItem;
    private Locator finishButton;
    private Locator cancelButtonStepTwo;

    // Confirmation Page
    private Locator confirmationHeader;
    private Locator backHomeButton;

    public CheckoutFlowPage(Page page) {
        // Step One
        this.firstNameInput = page.locator("#first-name");
        this.lastNameInput = page.locator("#last-name");
        this.postalCodeInput = page.locator("#postal-code");
        this.continueButton = page.locator("#continue");
        this.cancelButtonStepOne = page.locator("#cancel");

        // Step Two
        this.summaryInfo = page.locator(".summary_info");
        this.productItem = page.locator(".cart_item");
        this.finishButton = page.locator("#finish");
        this.cancelButtonStepTwo = page.locator("#cancel");

        // Complete
        this.confirmationHeader = page.locator(".complete-header");
        this.backHomeButton = page.locator("#back-to-products");
    }

    /** STEP ONE **/

    public void enterUserInfo(String first, String last, String zip) {
        firstNameInput.fill(first);
        lastNameInput.fill(last);
        postalCodeInput.fill(zip);
    }

    public void continueToOverview() {
        continueButton.click();
    }

    public void cancelFromUserInfoPage() {
        cancelButtonStepOne.click();
    }

    /** STEP TWO **/

    public boolean isOverviewSummaryVisible() {
        return summaryInfo.isVisible();
    }

    public boolean isProductDisplayed() {
        return productItem.isVisible();
    }

    public void finishCheckout() {
        finishButton.click();
    }

    public void cancelFromOverviewPage() {
        cancelButtonStepTwo.click();
    }

    /** STEP THREE – Confirmation **/

    public boolean isConfirmationDisplayed() {
        return confirmationHeader.isVisible();
    }

    public void clickBackHome() {
        backHomeButton.click();
    }
}
