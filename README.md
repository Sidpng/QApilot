
# SauceDemo Automation Framework – Playwright with Java

This is a Playwright-based automation framework written in Java for end-to-end testing of the [SauceDemo](https://www.saucedemo.com/) web application. The framework follows the Page Object Model (POM) design pattern and is structured for scalability, readability, and maintainability.

---

##  Features

- Automated login testing for multiple user types
- Add-to-cart and cart validation
- Full checkout flow (Step 1 → Step 2 → Confirmation) with one unified page object
- Reusable POM structure
- TestNG for test management and grouping
- Easy integration into CI/CD pipelines

---

##  Technology Stack

- **Language:** Java
- **Test Framework:** TestNG
- **Automation Engine:** Playwright for Java
- **Build Tool:** Maven
- **Design Pattern:** Page Object Model (POM)

---

##  Project Structure

```
src
└── test
    └── java
        ├── base
        │   └── BaseTest.java
        ├── pages
        │   ├── SauceDemoHomePage.java
        │   ├── InventoryPage.java
        │   ├── CartPage.java
        │   └── CheckoutFlowPage.java         #  Unified checkout handler
        └── tests
            ├── SauceDemoStandardUserLogin.java
            ├── SauceDemoInvalidLoginTest.java
            ├── LoginScenariosDifferentUsers.java
            ├── AddToCartTest.java
            └── CheckoutFlowTest.java         # Consolidated checkout test class
```

---

##  Test Execution

To run all tests:

```bash
mvn test -DsuiteXmlFile=testng.xml
```

TestNG suite includes:
- All login test scenarios
- Cart validation
- Optimized full checkout flow in a single test class

---

##  Notes

- `CheckoutFlowPage.java` replaces previous individual page files for:
  - `CheckoutPage.java`
  - `CheckoutOverviewPage.java`
  - `CheckoutCompletePage.java`
- `CheckoutFlowTest.java` replaces:
  - `CheckoutTest.java`
  - `CheckoutOverviewTest.java`
  - `CheckoutCompleteTest.java`

---

##  Author

**Siddhartha Upadhyay**  
GitHub: [@Sidpng](https://github.com/Sidpng)
