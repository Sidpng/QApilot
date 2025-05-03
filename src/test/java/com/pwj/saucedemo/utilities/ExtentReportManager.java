// Updated ExtentReportManager.java with styled configuration
package com.pwj.saucedemo.utilities;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

public class ExtentReportManager {
    private static ExtentReports extent;

    public static ExtentReports getReporter() {
        if (extent == null) {
            ExtentSparkReporter spark = new ExtentSparkReporter("test-output/ExtentReport.html");
            spark.config().setTheme(Theme.DARK);
            spark.config().setReportName("SauceDemo Playwright Automation Report");
            spark.config().setDocumentTitle("Execution Report");
            spark.config().setEncoding("utf-8");
            spark.config().setTimeStampFormat("MMM dd, yyyy HH:mm:ss");
            spark.config().setCss(".badge-primary { background-color: #1f6feb; }");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Framework", "Playwright with Java");
            extent.setSystemInfo("Tester", "Siddhartha Upadhyay");
        }
        return extent;
    }
}