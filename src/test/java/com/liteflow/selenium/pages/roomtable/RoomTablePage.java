package com.liteflow.selenium.pages.roomtable;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Main Page Object Model for RoomTable page
 *
 * This class is the main entry point for interacting with the RoomTable page.
 * It coordinates the two main sections: RoomSection and TableSection.
 */
public class RoomTablePage {

    private WebDriver driver;
    private WebDriverWait wait;

    // Section objects
    private RoomSection roomSection;
    private TableSection tableSection;

    // Statistics cards
    @FindBy(css = ".stats .stat-card:nth-child(1) .stat-number")
    private WebElement totalRoomsStat;

    @FindBy(css = ".stats .stat-card:nth-child(2) .stat-number")
    private WebElement totalTablesStat;

    @FindBy(css = ".stats .stat-card:nth-child(3) .stat-number")
    private WebElement availableTablesStat;

    @FindBy(css = ".stats .stat-card:nth-child(4) .stat-number")
    private WebElement occupiedTablesStat;

    // Toolbar elements
    @FindBy(id = "searchInput")
    private WebElement searchInput;

    @FindBy(css = ".toolbar .btn[onclick*='searchItems']")
    private WebElement searchButton;

    @FindBy(css = ".toolbar .btn[onclick*='addRoom']")
    private WebElement addRoomButton;

    @FindBy(css = ".toolbar .btn[onclick*='addTable']")
    private WebElement addTableButton;

    @FindBy(css = ".toolbar .btn[onclick*='showImportModal']")
    private WebElement importExcelButton;

    @FindBy(css = ".toolbar .btn[onclick*='exportToExcel']")
    private WebElement exportExcelButton;

    // Success/Error messages
    @FindBy(css = "div[style*='background: #d4edda']")
    private WebElement successMessage;

    @FindBy(css = "div[style*='background: #f8d7da']")
    private WebElement errorMessage;

    /**
     * Constructor
     *
     * @param driver WebDriver instance
     */
    public RoomTablePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);

        // Initialize section objects
        this.roomSection = new RoomSection(driver);
        this.tableSection = new TableSection(driver);
    }

    /**
     * Get RoomSection object
     *
     * @return RoomSection instance
     */
    public RoomSection getRoomSection() {
        return roomSection;
    }

    /**
     * Get TableSection object
     *
     * @return TableSection instance
     */
    public TableSection getTableSection() {
        return tableSection;
    }

    /**
     * Navigate to RoomTable page
     */
    public void navigateToRoomTable() {
        driver.get(driver.getCurrentUrl().split("/cart")[0] + "/inventory/roomtable");
        waitForPageToLoad();
    }

    /**
     * Wait for page to load completely
     */
    public void waitForPageToLoad() {
        // Wait for main elements to be present
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".content")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".stats")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".room-table-container")));

        // Wait for JavaScript to finish loading data
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Get total rooms count from statistics
     *
     * @return total rooms count
     */
    public int getTotalRoomsCount() {
        try {
            if (totalRoomsStat.isDisplayed()) {
                return Integer.parseInt(totalRoomsStat.getText().trim());
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Get total tables count from statistics
     *
     * @return total tables count
     */
    public int getTotalTablesCount() {
        try {
            if (totalTablesStat.isDisplayed()) {
                return Integer.parseInt(totalTablesStat.getText().trim());
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Get available tables count from statistics
     *
     * @return available tables count
     */
    public int getAvailableTablesCount() {
        try {
            if (availableTablesStat.isDisplayed()) {
                return Integer.parseInt(availableTablesStat.getText().trim());
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Get occupied tables count from statistics
     *
     * @return occupied tables count
     */
    public int getOccupiedTablesCount() {
        try {
            if (occupiedTablesStat.isDisplayed()) {
                return Integer.parseInt(occupiedTablesStat.getText().trim());
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Search for rooms or tables
     *
     * @param keyword search keyword
     */
    public void search(String keyword) {
        wait.until(ExpectedConditions.elementToBeClickable(searchInput)).clear();
        searchInput.sendKeys(keyword);
        wait.until(ExpectedConditions.elementToBeClickable(searchButton)).click();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Clear search
     */
    public void clearSearch() {
        wait.until(ExpectedConditions.elementToBeClickable(searchInput)).clear();
        wait.until(ExpectedConditions.elementToBeClickable(searchButton)).click();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Click Add Room button
     */
    public void clickAddRoom() {
        wait.until(ExpectedConditions.elementToBeClickable(addRoomButton)).click();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Click Add Table button
     */
    public void clickAddTable() {
        wait.until(ExpectedConditions.elementToBeClickable(addTableButton)).click();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Click Import Excel button
     */
    public void clickImportExcel() {
        wait.until(ExpectedConditions.elementToBeClickable(importExcelButton)).click();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Click Export Excel button
     */
    public void clickExportExcel() {
        wait.until(ExpectedConditions.elementToBeClickable(exportExcelButton)).click();
        try {
            Thread.sleep(2000); // Wait for file download
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Check if success message is displayed
     *
     * @return true if success message is visible
     */
    public boolean isSuccessMessageDisplayed() {
        try {
            return successMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get success message text
     *
     * @return success message text
     */
    public String getSuccessMessage() {
        try {
            if (successMessage.isDisplayed()) {
                return successMessage.getText();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check if error message is displayed
     *
     * @return true if error message is visible
     */
    public boolean isErrorMessageDisplayed() {
        try {
            return errorMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get error message text
     *
     * @return error message text
     */
    public String getErrorMessage() {
        try {
            if (errorMessage.isDisplayed()) {
                return errorMessage.getText();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get page title
     *
     * @return page title
     */
    public String getPageTitle() {
        return driver.getTitle();
    }

    /**
     * Check if page is loaded successfully
     *
     * @return true if main elements are present
     */
    public boolean isPageLoaded() {
        try {
            return driver.findElement(By.cssSelector(".content")).isDisplayed() &&
                   driver.findElement(By.cssSelector(".stats")).isDisplayed() &&
                   driver.findElement(By.cssSelector(".room-table-container")).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}

