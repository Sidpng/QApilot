
# Java Concepts Utilized in SauceDemo Playwright Java Automation Framework

This document lists all the core Java, OOP, and testing-related concepts implemented so far in the project.

---

## Core Java Concepts

| Concept | Where It Was Used |
|--------|-------------------|
| **Classes and Objects** | Every page object (`LandingPage`, `InventoryPage`), test class |
| **Constructors** | Used in `LandingPage(Page page)` and `InventoryPage(Page page)` to initialize locators |
| **Methods** | All page and test actions like `login()`, `addToCart()`, etc. |
| **Method Overloading** | Not yet used, but possible for `takeScreenshot()` variants |
| **Access Modifiers** | `private`, `public`, `protected` — used for class members |
| **Encapsulation** | Private locators + public methods in POM classes |
| **Inheritance** | `AddToCartTest`, `InvalidLoginTest` extending `BaseTest.java` |
| **Abstraction** | `BaseTest` provides common setup/teardown logic, hides Playwright internals |
| **Polymorphism (compile-time)** | Slightly with `ITestResult`, `Method` as arguments in overridden methods |
| **Static Keyword** | Used in `ExtentManager` to create a singleton `ExtentReports` instance |

---

## OOP Design Principles / Architecture

| Principle | Where It Was Applied |
|----------|----------------------|
| **Single Responsibility Principle** | Separate Page classes, test classes, `ExtentManager` |
| **Don't Repeat Yourself (DRY)** | Centralized `BaseTest` for shared logic |
| **Open/Closed Principle** | You can extend `BaseTest` without modifying it |
| **Page Object Model (POM)** | `LandingPage`, `InventoryPage` encapsulate UI interactions |
| **Separation of Concerns** | Pages handle elements, Base handles setup, Tests handle logic |

---

## Testing-Specific Concepts

| Concept | Where It Was Used |
|--------|--------------------|
| **TestNG Annotations** | `@Test`, `@BeforeMethod`, `@AfterMethod` |
| **TestNG Assertions** | `assertTrue()`, `assertEquals()` for validation |
| **ITestResult Interface** | To detect test status in `@AfterMethod` for screenshot capture |
| **Method Reflection** | `Method method` in `@BeforeMethod` to fetch test name dynamically |
| **Suite Management** | `testng.xml` used to run multiple test classes |
| **Maven Surefire Plugin** | To run tests via `mvn clean test` with XML suite |

---

## Java NIO & File Handling

| Concept | Where It Was Used |
|--------|-------------------|
| `Paths.get()` | Constructing screenshot paths |
| `Files.createDirectories()` | Automatically creating screenshot folder |
| `LocalDateTime`, `DateTimeFormatter` | For timestamping screenshot filenames |

---

## Third-Party Library Integration

| Library | Java Concepts Utilized |
|--------|------------------------|
| **Playwright Java** | Locators, fluent API chaining, page management |
| **ExtentReports** | Singleton (`ExtentManager`), logging results with `Status` enum |
| **Maven** | Dependency management (`pom.xml`), project build lifecycle |

---

## Additional Concepts You’ll Touch If We Continue

| Concept | Expected When |
|--------|----------------|
| **Collections (List, Map)** | When capturing all product names or validating cart items |
| **Exception Handling (try/catch)** | Already used in `tearDown()`, deeper use coming for recovery strategies |
| **Lambdas/Streams** | Filtering locators, verifying multiple conditions |
| **Interfaces & Abstract Classes** | Can be applied for test utilities, base page structure |
| **Enums** | To model sort types like `PRICE_LOW_TO_HIGH`, `NAME_A_TO_Z` etc.
