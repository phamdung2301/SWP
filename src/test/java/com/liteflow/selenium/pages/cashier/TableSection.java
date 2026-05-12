package com.liteflow.selenium.pages.cashier;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object Model for Table Section in Cashier page
 *
 * This class encapsulates all interactions with the tables section,
 * including filters and table selection.
 */
public class TableSection {

    private WebDriver driver;
    private WebDriverWait wait;

    // Filter elements
    @FindBy(id = "statusFilter")
    private WebElement statusFilter;

    @FindBy(id = "roomFilter")
    private WebElement roomFilter;

    @FindBy(id = "capacityFilter")
    private WebElement capacityFilter;

    @FindBy(css = ".btn-view-reservations")
    private WebElement viewReservationsButton;

    @FindBy(id = "autoSwitchMenuBtn")
    private WebElement autoSwitchMenuButton;

    // Tables grid
    @FindBy(id = "tablesGrid")
    private WebElement tablesGrid;

    @FindBy(css = ".tables-grid .table-card")
    private List<WebElement> tableCards;

    // Guide button
    @FindBy(css = ".guide-btn")
    private WebElement guideButton;

    /**
     * Constructor
     *
     * @param driver WebDriver instance
     */
    public TableSection(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    /**
     * Filter tables by status
     *
     * @param status status to filter by ("all", "available", "occupied")
     */
    public void filterByStatus(String status) {
        Select select = new Select(statusFilter);
        select.selectByValue(status);
        waitForTablesGridUpdate();
    }

    /**
     * Filter tables by room
     *
     * @param roomName room name to filter by
     */
    public void filterByRoom(String roomName) {
        Select select = new Select(roomFilter);
        select.selectByVisibleText(roomName);
        waitForTablesGridUpdate();
    }

    /**
     * Filter tables by capacity
     *
     * @param capacity capacity range to filter by ("all", "2-4", "5-6", "7+")
     */
    public void filterByCapacity(String capacity) {
        Select select = new Select(capacityFilter);
        select.selectByValue(capacity);
        waitForTablesGridUpdate();
    }

    /**
     * Select a table by table number
     *
     * @param tableNumber table number to select (e.g., "T001")
     * @return true if table was selected successfully
     */
    public boolean selectTable(String tableNumber) {
        try {
            // Find table card by table number
            WebElement tableCard = findTableCardByNumber(tableNumber);
            if (tableCard != null) {
                // Scroll to table card if needed
                scrollToElement(tableCard);

                // Click the table card
                wait.until(ExpectedConditions.elementToBeClickable(tableCard)).click();

                // Wait for selection to complete
                Thread.sleep(500);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Failed to select table: " + e.getMessage());
            return false;
        }
    }

    /**
     * Select a table by table name (visual display name)
     *
     * @param tableName table name to select (e.g., "Bàn 1")
     * @return true if table was selected successfully
     */
    public boolean selectTableByName(String tableName) {
        try {
            WebElement tableCard = findTableCardByName(tableName);
            if (tableCard != null) {
                scrollToElement(tableCard);
                wait.until(ExpectedConditions.elementToBeClickable(tableCard)).click();
                Thread.sleep(500);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Failed to select table by name: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a table is available
     *
     * @param tableNumber table number to check
     * @return true if table is available
     */
    public boolean isTableAvailable(String tableNumber) {
        WebElement tableCard = findTableCardByNumber(tableNumber);
        if (tableCard != null) {
            return tableCard.getAttribute("class").contains("available") ||
                   !tableCard.getAttribute("class").contains("occupied");
        }
        return false;
    }

    /**
     * Check if a table is occupied
     *
     * @param tableNumber table number to check
     * @return true if table is occupied
     */
    public boolean isTableOccupied(String tableNumber) {
        WebElement tableCard = findTableCardByNumber(tableNumber);
        if (tableCard != null) {
            return tableCard.getAttribute("class").contains("occupied");
        }
        return false;
    }

    /**
     * Get total number of visible tables
     *
     * @return number of visible tables
     */
    public int getVisibleTablesCount() {
        return tableCards.size();
    }

    /**
     * Get table capacity
     *
     * @param tableNumber table number
     * @return capacity of the table, or -1 if not found
     */
    public int getTableCapacity(String tableNumber) {
        WebElement tableCard = findTableCardByNumber(tableNumber);
        if (tableCard != null) {
            try {
                String capacityText = tableCard.findElement(By.cssSelector(".table-capacity")).getText();
                // Extract number from text like "4 chỗ"
                return Integer.parseInt(capacityText.replaceAll("[^0-9]", ""));
            } catch (Exception e) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * Click view reservations button
     */
    public void clickViewReservations() {
        wait.until(ExpectedConditions.elementToBeClickable(viewReservationsButton)).click();
    }

    /**
     * Toggle auto switch to menu
     */
    public void toggleAutoSwitchMenu() {
        wait.until(ExpectedConditions.elementToBeClickable(autoSwitchMenuButton)).click();
    }

    /**
     * Check if auto switch menu is enabled
     *
     * @return true if auto switch is enabled
     */
    public boolean isAutoSwitchMenuEnabled() {
        String btnText = autoSwitchMenuButton.findElement(By.cssSelector(".btn-text")).getText();
        return btnText.contains("Bật");
    }

    /**
     * Click guide button
     */
    public void clickGuide() {
        wait.until(ExpectedConditions.elementToBeClickable(guideButton)).click();
    }

    /**
     * Wait for tables grid to update after filter change
     */
    private void waitForTablesGridUpdate() {
        try {
            Thread.sleep(300); // Wait for filter to apply
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Find table card element by table number
     *
     * @param tableNumber table number (e.g., "T001")
     * @return WebElement of the table card, or null if not found
     */
    private WebElement findTableCardByNumber(String tableNumber) {
        try {
            // Wait for tables grid to load
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("tablesGrid")));

            // Find table by data attribute or text content
            List<WebElement> tables = driver.findElements(By.cssSelector(".table-card"));
            for (WebElement table : tables) {
                String tableNum = table.getAttribute("data-table-number");
                if (tableNum != null && tableNum.equals(tableNumber)) {
                    return table;
                }
                // Alternative: check if table text contains the number
                if (table.getText().contains(tableNumber)) {
                    return table;
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error finding table by number: " + e.getMessage());
            return null;
        }
    }

    /**
     * Find table card element by table name
     *
     * @param tableName table name (e.g., "Bàn 1")
     * @return WebElement of the table card, or null if not found
     */
    private WebElement findTableCardByName(String tableName) {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("tablesGrid")));

            List<WebElement> tables = driver.findElements(By.cssSelector(".table-card"));
            for (WebElement table : tables) {
                if (table.getText().contains(tableName)) {
                    return table;
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error finding table by name: " + e.getMessage());
            return null;
        }
    }

    /**
     * Scroll to element
     *
     * @param element element to scroll to
     */
    private void scrollToElement(WebElement element) {
        ((org.openqa.selenium.JavascriptExecutor) driver)
            .executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
