// Project Title: SauceDemo Playwright Automation
// File Name: TestListener.java
// Author: Siddhartha Upadhyay
// GitHub: https://github.com/Sidpng
// Creation Date: 12-Aug-2025
// Description: TestNG listener for ExtentReports integration, with
//              thread-safe ExtentTest and Playwright screenshot capture.

package com.pwj.saucedemo.utilities;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.microsoft.playwright.Page;
import com.pwj.saucedemo.base.BaseTest;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestListener implements ITestListener {

    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    private static ExtentReports extent;

    private static final Path SCREENSHOT_DIR = Paths.get("logs", "screenshots");

    private static String safeName(String in) {
        return in.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static String testDisplayName(ITestResult result) {
        String cls = result.getTestClass() != null ? result.getTestClass().getName() : "UnknownClass";
        String m = result.getMethod() != null ? result.getMethod().getMethodName() : "UnknownMethod";
        return cls + "." + m;
    }

    @Override
    public void onStart(ITestContext context) {
        new File(SCREENSHOT_DIR.toString()).mkdirs();
        extent = ExtentManager.getInstance();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String name = testDisplayName(result);
        ExtentTest test = extent.createTest(name);
        extentTest.set(test);
        test.log(Status.INFO, "Starting: " + name);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        extentTest.get().log(Status.PASS, "Passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = extentTest.get();
        test.log(Status.FAIL, result.getThrowable() == null ? "Failed" : result.getThrowable().toString());

        // Try to get the Page: prefer thread-local, fallback to instance field
        Page page = BaseTest.getPage();
        if (page == null && result.getInstance() instanceof BaseTest) {
            page = ((BaseTest) result.getInstance()).page;
        }

        if (page != null) {
            try {
                String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
                String base = safeName(testDisplayName(result)) + "_" + stamp + ".png";
                Path out = SCREENSHOT_DIR.resolve(base);
                page.screenshot(new Page.ScreenshotOptions().setPath(out).setFullPage(true));
                test.addScreenCaptureFromPath(out.toString());
                test.log(Status.INFO, "Screenshot saved: " + out);
            } catch (Exception e) {
                test.log(Status.WARNING, "Could not capture screenshot: " + e.getMessage());
            }
        } else {
            test.log(Status.WARNING, "Playwright Page unavailable; cannot capture screenshot.");
        }

        // Note: Playwright trace is saved in BaseTest @AfterMethod (if enabled)
        test.log(Status.INFO, "If tracing is enabled, trace will be saved under logs/traces/");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        extentTest.get().log(Status.SKIP, "Skipped: " + (result.getThrowable() == null ? "" : result.getThrowable()));
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }
}
