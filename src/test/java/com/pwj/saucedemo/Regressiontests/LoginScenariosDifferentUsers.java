// Project Title     : SauceDemo Automation Framework using Playwright with Java
// File Name         : LoginScenariosDifferentUsers.java
// Author Name       : Siddhartha Upadhyay
// GitHub Link       : https://github.com/YourUsername/saucedemo-playwright-java
// Creation Date     : 01-May-2025
// Description       : TestNG test class to validate login behavior for different types of users on SauceDemo.

package com.pwj.saucedemo.Regressiontests;

import com.microsoft.playwright.Locator;
import com.pwj.saucedemo.base.BaseTest;
import com.pwj.saucedemo.pages.SauceDemoHomePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginScenariosDifferentUsers extends BaseTest {

	@Test(description = "Valid login with standard_user")
	public void testStandardUserLogin() {
		SauceDemoHomePage homePage = new SauceDemoHomePage(page);
		homePage.login("standard_user", "secret_sauce");
		Assert.assertTrue(page.url().contains("inventory"), "Standard user should land on inventory page.");
	}

	@Test(description = "Locked out user should see error")
	public void testLockedOutUserLogin() {
		SauceDemoHomePage homePage = new SauceDemoHomePage(page);
		homePage.login("locked_out_user", "secret_sauce");
		Locator error = homePage.getErrorLocator();
		Assert.assertTrue(error.isVisible(), "Locked out user should trigger error message.");
		Assert.assertTrue(error.textContent().contains("locked out"), "Error should mention 'locked out'.");
	}

	@Test(description = "Problem user login — expected to succeed but may show broken UI later")
	public void testProblemUserLogin() {
		SauceDemoHomePage homePage = new SauceDemoHomePage(page);
		homePage.login("problem_user", "secret_sauce");
		Assert.assertTrue(page.url().contains("inventory"), "Problem user should land on inventory page.");
	}

	@Test(description = "Performance glitch user may require second click to login")
	public void testPerformanceGlitchUserLogin() {
		SauceDemoHomePage homePage = new SauceDemoHomePage(page);

		homePage.enterUsername("performance_glitch_user");
		homePage.enterPassword("secret_sauce");
		homePage.clickLogin();

		page.waitForTimeout(1500); // brief pause

		if (!page.url().contains("inventory")) {
			homePage.clickLogin(); // Retry
		}

		Assert.assertTrue(page.url().contains("inventory"),
				"Performance glitch user should eventually land on inventory.");
	}

	@Test(description = "Invalid user login should display error")
	public void testInvalidUserLogin() {
		SauceDemoHomePage homePage = new SauceDemoHomePage(page);
		homePage.login("fake_user", "wrong_pass");
		Locator error = homePage.getErrorLocator();
		Assert.assertTrue(error.isVisible(), "Invalid user should see error.");
	}
}
