/**
 * Project Title: SauceDemo Playwright Automation
 * File Name: BaseTest.java
 * Author: Siddhartha Upadhyay
 * GitHub: https://github.com/Sidpng
 * Creation Date: 12-Aug-2025
 * Description: Thread-safe Playwright bootstrap with cross-browser support,
 *              headless toggle, trace/video capture, base URL navigation,
 *              and Log4j2 logging via LoggerUtil.
 */

package com.pwj.saucedemo.base;

import com.microsoft.playwright.*;
import com.pwj.saucedemo.utilities.Config;
import com.pwj.saucedemo.utilities.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class BaseTest {

    // Logger restored
    protected final Logger logger = LoggerUtil.getLogger(getClass());

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
        final String browserName = Config.get("browser", "chromium").toLowerCase();
        final boolean headless = Config.getBoolean("headless", true);
        final int slowMo = Config.getInt("slowMo", 0);
        final int viewportWidth = Config.getInt("viewportWidth", 1280);
        final int viewportHeight = Config.getInt("viewportHeight", 800);
        final String baseUrl = Config.get("baseUrl", "https://www.saucedemo.com");
        final String video = Config.get("video", "off").toLowerCase();               // off | on
        final String trace = Config.get("trace", "retain-on-failure").toLowerCase(); // off | on | retain-on-failure

        logger.info("=== Starting Test: {} ===", this.getClass().getSimpleName());
        logger.info("Config => browser={}, headless={}, slowMo={}, viewport={}x{}, video={}, trace={}",
                browserName, headless, slowMo, viewportWidth, viewportHeight, video, trace);

        Path videoDir = Paths.get("logs", "videos");
        Path traceDir = Paths.get("logs", "traces");
        new File(videoDir.toString()).mkdirs();
        new File(traceDir.toString()).mkdirs();

        Playwright pw = Playwright.create();
        BrowserType.LaunchOptions launch = new BrowserType.LaunchOptions().setHeadless(headless);
        if (slowMo > 0) {
            try { launch.setSlowMo((double) slowMo); } catch (Throwable t) { logger.warn("slowMo not supported: {}", t.toString()); }
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
        logger.info("Launched browser: {}", browserName);

        Browser.NewContextOptions ctxOpts = new Browser.NewContextOptions()
                .setViewportSize(viewportWidth, viewportHeight);

        if ("on".equals(video)) {
            try {
                ctxOpts.setRecordVideoDir(videoDir);
                ctxOpts.setRecordVideoSize(viewportWidth, viewportHeight);
                logger.info("Video recording enabled -> {}", videoDir.toAbsolutePath());
            } catch (Throwable t) {
                logger.warn("Video recording not supported: {}", t.toString());
            }
        }

        BrowserContext ctx = br.newContext(ctxOpts);

        if ("on".equals(trace) || "retain-on-failure".equals(trace)) {
            try {
                ctx.tracing().start(new Tracing.StartOptions()
                        .setScreenshots(true)
                        .setSnapshots(true)
                        .setSources(true));
                logger.info("Tracing started");
            } catch (Throwable t) {
                logger.warn("Tracing not supported: {}", t.toString());
            }
        }

        Page pg = ctx.newPage();
        pg.navigate(baseUrl);
        logger.info("Navigated to baseUrl: {}", baseUrl);

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
        String outcome = result.getStatus() == ITestResult.SUCCESS ? "PASS"
                : result.getStatus() == ITestResult.SKIP ? "SKIP" : "FAIL";
        logger.info("TearDown for {}.{} -> {}",
                result.getTestClass() != null ? result.getTestClass().getName() : "UnknownClass",
                result.getMethod() != null ? result.getMethod().getMethodName() : "UnknownMethod",
                outcome);

        if (context != null) {
            try {
                if ("on".equals(traceMode) || ("retain-on-failure".equals(traceMode) && result.getStatus() == ITestResult.FAILURE)) {
                    String name = safeName(result);
                    Path out = Paths.get("logs", "traces", name + ".zip");
                    context.tracing().stop(new Tracing.StopOptions().setPath(out));
                    logger.info("Trace saved: {}", out.toAbsolutePath());
                } else {
                    context.tracing().stop();
                    logger.info("Trace stopped (not saved)");
                }
            } catch (Throwable t) {
                logger.warn("Trace stop error: {}", t.toString());
            }
        }

        // Close in order; log any issues but keep going
        try { if (page != null) page.close(); } catch (Throwable t) { logger.warn("page.close(): {}", t.toString()); }
        try { if (context != null) context.close(); } catch (Throwable t) { logger.warn("context.close(): {}", t.toString()); }
        try { if (browser != null) browser.close(); } catch (Throwable t) { logger.warn("browser.close(): {}", t.toString()); }
        try { if (playwright != null) playwright.close(); } catch (Throwable t) { logger.warn("playwright.close(): {}", t.toString()); }

        // Clear ThreadLocals
        TL_PAGE.remove();
        TL_CONTEXT.remove();
        TL_BROWSER.remove();
        TL_PLAYWRIGHT.remove();

        logger.info("=== Finished Test: {} ===\n", this.getClass().getSimpleName());
    }

    private String safeName(ITestResult result) {
        String cls = result.getTestClass() != null ? result.getTestClass().getName() : "UnknownClass";
        String m = result.getMethod() != null ? result.getMethod().getMethodName() : "UnknownMethod";
        return (cls + "_" + m + "_" + System.currentTimeMillis())
                .replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
