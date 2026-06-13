
# QApilot — AI-Paired Playwright Automation Framework

[![playwright-ci](https://github.com/Sidpng/QApilot/actions/workflows/ci.yml/badge.svg)](https://github.com/Sidpng/QApilot/actions/workflows/ci.yml)
[![nightly-regression](https://github.com/Sidpng/QApilot/actions/workflows/nightly.yml/badge.svg)](https://github.com/Sidpng/QApilot/actions/workflows/nightly.yml)

A scalable, hybrid automation testing framework using **Playwright with Java**, built for the [SauceDemo](https://www.saucedemo.com/) application — and developed in active pair-collaboration with AI (Claude / GitHub Copilot) for test design, refactoring, and CI automation.

> **QApilot** = QA on autopilot: a maintained Playwright framework with CI on every push and a nightly regression run that records its own test-health history.

---

## Features

- **Playwright with Java** integration
- **Page Object Model (POM)** for modular, maintainable code
- **TestNG** for structured test execution and grouping
- **Extent Reports** with rich UI, screenshots, and logs
- **Log4j2** for granular logging across test levels
- **RetryAnalyzer** to auto-retry flaky tests (configurable)
- **TestNG groups** and XML filters (e.g., regression, sanity, order)
- **Regression flow by user type** (e.g., `standard_user`, `visual_user`)
- **CI/CD-ready** Jenkins pipeline with schedule triggers
- **Screenshots on failure** saved and attached to reports
- **Well-organized packages** base, pages, tests, utilities

---

## Project Structure

```
src
└── test
    └── java
        ├── base                     # BaseTest with Playwright setup
        ├── pages                    # POM classes: LoginPage, CartPage, etc.
        ├── tests                    # Feature-specific test classes
        ├── tests.Orderplaced_tests # Regression: full order flow per user
        └── utilities                # Logger, ExtentManager, RetryAnalyzer
```

---

##  Test Execution

###  Run all tests:
```bash
mvn clean test -DsuiteXmlFile=testng.xml
```

###  Run regression tests only:
```bash
mvn clean test -DsuiteXmlFile=testng-groups.xml
```

---

##  Reports

- HTML Report: `test-output/ExtentReport.html`
- Screenshots: `test-output/screenshots/`
- Logs via Log4j2 console/file output

---

##  CI/CD

**GitHub Actions** (`.github/workflows/`):
- `playwright-ci` — on every push / PR: compiles, installs Chromium, runs the TestNG suite headless, uploads Extent reports + surefire output as artifacts.
- `nightly-regression` — daily at 01:30 UTC (~07:00 IST): runs the full suite and commits a test-health row to `runs/history.md`.

An **Azure Pipelines** definition (`azure-pipelines.yml`) is also included for reference.

> To let the nightly auto-commit count toward the contribution graph, add a fine-grained PAT (Contents: Read/Write on this repo) as a repo secret named `PUSH_TOKEN`.

---

##  Author

**Siddhartha Upadhyay**  
GitHub: [Sidpng](https://github.com/Sidpng)  
Framework version: May 2025

---

## Notes

- Designed for interview-ready demonstration
- Focused on modularity, debugging, CI/CD readiness
- Expandable for API, BDD, or cross-browser testing
