
# SauceDemo Playwright Automation Framework

A scalable and hybrid automation testing framework using **Playwright with Java**, built for the [SauceDemo](https://www.saucedemo.com/) application.

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
- **CI/CD-ready**: Jenkins pipeline with schedule triggers
- **Screenshots on failure**, saved and attached to reports
- **Well-organized packages**: base, pages, tests, utilities

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

##  Jenkins CI/CD

- Runs every **Wednesday at 4 PM IST**
- Pulls latest code, runs Maven suite, generates report
- Optional: archive screenshots and test-output in build

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
