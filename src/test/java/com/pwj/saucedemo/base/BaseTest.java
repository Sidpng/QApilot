/**
 * Project Title: SauceDemo Playwright Automation
 * File Name: BaseTest.java
 * Author: Siddhartha Upadhyay
 * GitHub: https://github.com/Sidpng
 * Creation Date: 12-Aug-2025
 * Description: Thread-safe Playwright bootstrap with cross-browser support,
 *              headless toggle, trace/video capture, and base URL navigation.
 */

package com.pwj.saucedemo.base;

import com.microsoft.playwright.*;
import com.pwj.saucedemo.utilities.Config;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class BaseTest {

    // Keep these for backward compatibility with existing tests & listener
    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    public Page page;

    // ThreadLocals for safe parallel execution
    private static final ThreadLocal<Playwright> TL_PLAYWRIGHT = new ThreadLocal<>();
    private static final ThreadLocal<Browser> TL_BROWSER = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> TL_CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Page> TL_PAGE = new ThreadLocal<>();

    // ===== Accessors (usable from anywhere, including listeners) =====
    public static Playwright getPlaywright() { return TL_PLAYWRIGHT.get(); }
    public static Browser getBrowser() { return TL_BROWSER.get(); }
    public static BrowserContext getContext() { return TL_CONTEXT.get(); }
    public static Page getPage() { return TL_PAGE.get(); }

    @BeforeMethod(alwaysRun = true)
    public void setUp(Object[] data) {
        // Read config (overridable via -Dkey=value)
        final String browserName = Config.get("browser", "chromium").toLowerCase();
        final boolean headless = Config.getBoolean("headless", true);
        final int slowMo = Config.getInt("slowMo", 0);
        final int viewportWidth = Config.getInt("viewportWidth", 1280);
        final int viewportHeight = Config.getInt("viewportHeight", 800);
        final String baseUrl = Config.get("baseUrl", "https://www.saucedemo.com");
        final String video = Config.get("video", "off").toLowerCase(); // off | on
        final String trace = Config.get("trace", "retain-on-failure").toLowerCase(); // off | on | retain-on-failure

        // Ensure artifact folders exist
        Path videoDir = Paths.get("logs", "videos");
        Path traceDir = Paths.get("logs", "traces");
        new File(videoDir.toString()).mkdirs();
        new File(traceDir.toString()).mkdirs();

        // Create Playwright + Browser
        Playwright pw = Playwright.create();

        BrowserType.LaunchOptions launch = new BrowserType.LaunchOptions().setHeadless(headless);
        if (slowMo > 0) {
            try {
                launch.setSlowMo((double) slowMo);
            } catch (Throwable ignored) { /* older versions may not support slowMo */ }
        }

        Browser br;
        switch (browserName) {
            case "firefox":
                br = pw.firefox().launch(launch);
                break;
            case "webkit":
                br = pw.webkit().launch(launch);
                break;
            case "chromium":
            default:
                br = pw.chromium().launch(launch);
        }

        // BrowserContext with viewport & optional video
        Browser.NewContextOptions ctxOpts = new Browser.NewContextOptions()
                .setViewportSize(viewportWidth, viewportHeight);

        if ("on".equals(video)) {
            ctxOpts.setRecordVideoDir(videoDir);
            // Optional: fix video resolution; supported in Playwright Java API
            try { ctxOpts.setRecordVideoSize(viewportWidth, viewportHeight); } catch (Throwable ignored) {}
        }

        BrowserContext ctx = br.newContext(ctxOpts);

        // Tracing control
        if ("on".equals(trace) || "retain-on-failure".equals(trace)) {
            try {
                ctx.tracing().start(new Tracing.StartOptions()
                        .setScreenshots(true)
                        .setSnapshots(true)
                        .setSources(true));
            } catch (Throwable ignored) {}
        }

        Page pg = ctx.newPage();
        pg.navigate(baseUrl);

        // Bind to fields for old code
        this.playwright = pw;
        this.browser = br;
        this.context = ctx;
        this.page = pg;

        // Bind to ThreadLocal for parallel safety
        TL_PLAYWRIGHT.set(pw);
        TL_BROWSER.set(br);
        TL_CONTEXT.set(ctx);
        TL_PAGE.set(pg);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        String traceMode = Config.get("trace", "retain-on-failure").toLowerCase();

        // Save trace if applicable
        if (context != null) {
            try {
                if ("on".equals(traceMode) || ("retain-on-failure".equals(traceMode) && result.getStatus() == ITestResult.FAILURE)) {
                    String name = safeName(result);
                    Path out = Paths.get("logs", "traces", name + ".zip");
                    context.tracing().stop(new Tracing.StopOptions().setPath(out));
                } else {
                    // stop without saving
                    context.tracing().stop();
                }
            } catch (Throwable ignored) {}
        }

        // Clean up (close in the right order)
        try { if (page != null) page.close(); } catch (Throwable ignored) {}
        try { if (context != null) context.close(); } catch (Throwable ignored) {}
        try { if (browser != null) browser.close(); } catch (Throwable ignored) {}
        try { if (playwright != null) playwright.close(); } catch (Throwable ignored) {}

        // Clear ThreadLocals
        TL_PAGE.remove();
        TL_CONTEXT.remove();
        TL_BROWSER.remove();
        TL_PLAYWRIGHT.remove();
    }

    private String safeName(ITestResult result) {
        String cls = result.getTestClass() != null ? result.getTestClass().getName() : "UnknownClass";
        String m = result.getMethod() != null ? result.getMethod().getMethodName() : "UnknownMethod";
        return (cls + "_" + m + "_" + System.currentTimeMillis())
                .replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
