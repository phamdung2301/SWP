package com.liteflow.selenium.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Base class for Selenium system tests
 *
 * This class provides:
 * - WebDriver initialization and cleanup
 * - Screenshot capture on test failure
 * - Implicit and explicit wait configuration
 * - Base URL configuration
 * - Common helper methods
 */
public abstract class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected static final String BASE_URL = "http://localhost:8080/LiteFlow";
    protected static final int IMPLICIT_WAIT_SECONDS = 10;
    protected static final int EXPLICIT_WAIT_SECONDS = 15;

    /**
     * Setup WebDriver manager before all tests
     */
    @BeforeAll
    public static void setUpClass() {
        // Setup ChromeDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();
    }

    /**
     * Initialize WebDriver before each test
     */
    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();

        // Recommended options for testing
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-infobars");

        // Uncomment for headless testing
        // options.addArguments("--headless");
        // options.addArguments("--no-sandbox");
        // options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(IMPLICIT_WAIT_SECONDS));
        wait = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT_SECONDS));
    }

    /**
     * Cleanup WebDriver after each test
     */
    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Navigate to a specific page relative to BASE_URL
     *
     * @param path the path to navigate to (e.g., "/cashier")
     */
    protected void navigateTo(String path) {
        driver.get(BASE_URL + path);
    }

    /**
     * Navigate to the base URL
     */
    protected void navigateToHome() {
        driver.get(BASE_URL);
    }

    /**
     * Take screenshot on test failure
     *
     * @param testName the name of the test
     * @return the path to the screenshot file
     */
    protected String takeScreenshot(String testName) {
        try {
            TakesScreenshot screenshot = (TakesScreenshot) driver;
            File sourceFile = screenshot.getScreenshotAs(OutputType.FILE);

            // Create screenshots directory if it doesn't exist
            Path screenshotsDir = Paths.get("target/screenshots");
            if (!Files.exists(screenshotsDir)) {
                Files.createDirectories(screenshotsDir);
            }

            // Generate filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("%s_%s.png", testName, timestamp);
            Path destinationPath = screenshotsDir.resolve(fileName);

            // Copy screenshot to destination
            Files.copy(sourceFile.toPath(), destinationPath);

            return destinationPath.toString();
        } catch (IOException e) {
            System.err.println("Failed to take screenshot: " + e.getMessage());
            return null;
        }
    }

    /**
     * Wait for page to load completely
     */
    protected void waitForPageLoad() {
        wait.until(driver ->
            ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return document.readyState")
                .equals("complete")
        );
    }

    /**
     * Execute JavaScript
     *
     * @param script the JavaScript to execute
     * @param args arguments to pass to the script
     * @return the result of the script execution
     */
    protected Object executeScript(String script, Object... args) {
        return ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(script, args);
    }

    /**
     * Scroll to element
     *
     * @param element the element to scroll to
     */
    protected void scrollToElement(org.openqa.selenium.WebElement element) {
        executeScript("arguments[0].scrollIntoView(true);", element);
    }

    /**
     * Wait for a specific duration (use sparingly)
     *
     * @param milliseconds the duration to wait in milliseconds
     */
    protected void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Sleep interrupted: " + e.getMessage());
        }
    }

    /**
     * Get current URL
     *
     * @return the current URL
     */
    protected String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /**
     * Get page title
     *
     * @return the page title
     */
    protected String getPageTitle() {
        return driver.getTitle();
    }

    /**
     * Refresh the current page
     */
    protected void refreshPage() {
        driver.navigate().refresh();
    }

    /**
     * Navigate back
     */
    protected void navigateBack() {
        driver.navigate().back();
    }

    /**
     * Navigate forward
     */
    protected void navigateForward() {
        driver.navigate().forward();
    }
}
