/**
 * Project Title: SauceDemo Playwright Automation
 * File Name: SauceDemoHomePage.java
 * Author: Siddhartha Upadhyay
 * GitHub: https://github.com/Sidpng
 * Creation Date: 29-April-2025
 * Description: This class represents the home page of the SauceDemo website.
 */

package com.pwj.saucedemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class SauceDemoHomePage {

	private Page page;

	// Locators
	private Locator usernameInput;
	private Locator passwordInput;
	private Locator loginButton;
	private Locator errorMessage;

	public SauceDemoHomePage(Page page) {
		this.page = page;
		this.usernameInput = page.locator("#user-name");
		this.passwordInput = page.locator("#password");
		this.loginButton = page.locator("#login-button");
		this.errorMessage = page.locator("[data-test='error']");
	}

	/**
	 * Enters username in the username input box.
	 * 
	 * @param username The username to be entered.
	 */
	public void enterUsername(String username) {
		usernameInput.fill(username);
	}

	/**
	 * Enters password in the password input box.
	 * 
	 * @param password The password to be entered.
	 */
	public void enterPassword(String password) {
		passwordInput.fill(password);
	}

	/**
	 * Clicks on the login button to submit login form.
	 */
	public void clickLogin() {
		loginButton.click();
	}

	/**
	 * Performs a full login with given username and password.
	 * 
	 * @param username User's username.
	 * @param password User's password.
	 */
	public void login(String username, String password) {
		enterUsername(username);
		enterPassword(password);
		clickLogin();
	}

	public String getCurrentUrl() {
		return page.url();
	}

	/**
	 * Gets the error message displayed when login fails.
	 * 
	 * @return The error message text.
	 */
	public String getErrorMessage() {
		if (errorMessage.isVisible()) {
			return errorMessage.textContent();
		}
		return null;
	}

	/**
	 * Returns the error message locator (used after failed login attempts)
	 * 
	 * @return Locator object for the error message element
	 */
	public Locator getErrorLocator() {
		return page.locator("[data-test='error']");
	}

}
