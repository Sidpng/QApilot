/**
 * Project Title: SauceDemo Playwright Automation 
 * File Name: BaseTest.java
 * Author: Siddhartha Upadhyay 
 * GitHub: https://github.com/Sidpng 
 * Creation Date: 29-April-2025 
 * Description: This abstract class provides Playwright browser setup and teardown and should be extended by all test classes.
 */

package com.pwj.saucedemo.base;

import com.microsoft.playwright.*;
import org.testng.ITestResult;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.logging.log4j.Logger;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.pwj.saucedemo.utilities.ExtentManager;
import com.pwj.saucedemo.utilities.LoggerUtil;

import java.lang.reflect.Method;

public abstract class BaseTest {

	protected Playwright playwright;
	protected Browser browser;
	protected BrowserContext context;
	public Page page;
	protected ExtentReports extent;
	protected ExtentTest test;
	protected Logger logger;

	@BeforeMethod
	public void setUp(Method method) {
		
		extent = ExtentManager.getInstance();
		// Initialize test node with method name
		test = extent.createTest(method.getName());
		logger = LoggerUtil.getLogger(this.getClass());
		logger.info("=== Starting Test: " + this.getClass().getSimpleName() + " ===");
		playwright = Playwright.create();
		browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
		context = browser.newContext();
		page = context.newPage();
		page.navigate("https://www.saucedemo.com/");
		test.log(Status.INFO, "Navigated to SauceDemo site");
		logger.info("Navigated to SauceDemo homepage.");
		
	}

	/**
	 * Takes a screenshot and saves it to test-output/screenshots directory.
	 * 
	 * @param testName The name of the test method for file naming.
	 */
	
	public String takeScreenshot(String name) {
		
		try {	
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
			String folderPath = "test-output/screenshots";
			String filePath = folderPath + "/" + name + "_" + timestamp + ".png";
			Files.createDirectories(Paths.get(folderPath));
			page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(filePath)));
			System.out.println("Screenshot saved: " + filePath);
			return filePath;

		} catch (Exception e) {
			System.err.println("Screenshot failed: " + e.getMessage());
			return null;
		}
	}

	@AfterMethod
	public void tearDown(ITestResult result) {
		try {
			String testName = result.getMethod().getMethodName();

			if (!result.isSuccess()) {
				String path = takeScreenshot(testName + "_FAILED");
				test.fail(result.getThrowable());
				test.addScreenCaptureFromPath(path);
			} else {
				String path = takeScreenshot(testName + "_PASSED");
				test.pass("Test passed");
				test.addScreenCaptureFromPath(path);
			}
		} catch (Exception e) {
			System.err.println("Screenshot or Extent logging failed: " + e.getMessage());
		} finally {
			logger.info("Closing browser and Playwright context...");
			if (page != null)
				page.close();
			if (context != null)
				context.close();
			if (browser != null)
				browser.close();
			if (playwright != null)
				playwright.close();
			if (extent != null)
				extent.flush();
			logger.info("=== Finished Test: " + this.getClass().getSimpleName() + " ===\n");
		}
	}

}
