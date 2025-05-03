// TestListener.java
package com.pwj.saucedemo.utilities;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.microsoft.playwright.Page;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import com.pwj.saucedemo.base.BaseTest;

import java.nio.file.Paths;

public class TestListener implements ITestListener {

    private static ExtentReports extent = ExtentReportManager.getReporter();
    private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = extent.createTest(result.getMethod().getMethodName());
        for (String group : result.getMethod().getGroups()) {
            test.assignCategory(group);
        }
        extentTest.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        extentTest.get().log(Status.PASS, "Test passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = extentTest.get();
        test.log(Status.FAIL, "Test failed: " + result.getThrowable());

        Object testClass = result.getInstance();
        Page page = ((BaseTest) testClass).page;

        try {
            String testName = result.getMethod().getMethodName();
            String timestamp = String.valueOf(System.currentTimeMillis());
            String screenshotPath = "test-output/screenshots/" + testName + "_" + timestamp + ".png";

            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(screenshotPath)));
            test.addScreenCaptureFromPath(screenshotPath);
        } catch (Exception e) {
            test.log(Status.WARNING, "Could not attach screenshot: " + e.getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        extentTest.get().log(Status.SKIP, "Test skipped: " + result.getThrowable());
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }
}
