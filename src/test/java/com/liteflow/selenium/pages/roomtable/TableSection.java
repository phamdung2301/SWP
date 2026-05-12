package com.liteflow.selenium.pages.roomtable;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Page Object Model for Table Section in RoomTable page
 *
 * This class encapsulates all interactions with the tables section,
 * including CRUD operations for tables and status updates.
 */
public class TableSection {

    private WebDriver driver;
    private WebDriverWait wait;

    // Table table elements
    @FindBy(css = ".room-table-container:last-of-type .table")
    private WebElement tableTable;

    @FindBy(css = ".room-table-container:last-of-type .table tbody tr")
    private List<WebElement> tableRows;

    @FindBy(css = ".room-table-container:last-of-type .empty-state")
    private WebElement emptyState;

    // Add Table Modal elements
    @FindBy(id = "addTableModal")
    private WebElement addTableModal;

    @FindBy(id = "tableNumber")
    private WebElement tableNumberInput;

    @FindBy(id = "tableName")
    private WebElement tableNameInput;

    @FindBy(id = "roomId")
    private WebElement roomIdSelect;

    @FindBy(id = "capacity")
    private WebElement capacityInput;

    @FindBy(css = "#addTableModal .close")
    private WebElement closeAddTableModalButton;

    @FindBy(css = "#addTableModal .btn-success[onclick*='submitAddTable']")
    private WebElement submitAddTableButton;

    @FindBy(css = "#addTableModal .btn-warning[onclick*='closeAddTableModal']")
    private WebElement cancelAddTableButton;

    // Edit Table Modal elements
    @FindBy(id = "editTableModal")
    private WebElement editTableModal;

    @FindBy(id = "editTableNumber")
    private WebElement editTableNumberInput;

    @FindBy(id = "editTableName")
    private WebElement editTableNameInput;

    @FindBy(id = "editRoomId")
    private WebElement editRoomIdSelect;

    @FindBy(id = "editCapacity")
    private WebElement editCapacityInput;

    @FindBy(id = "editStatus")
    private WebElement editStatusSelect;

    @FindBy(css = "#editTableModal .btn-success[onclick*='submitEditTable']")
    private WebElement submitEditTableButton;

    // Delete Table Confirmation Modal
    @FindBy(id = "deleteTableConfirmModal")
    private WebElement deleteTableConfirmModal;

    @FindBy(id = "confirmDeleteTableBtn")
    private WebElement confirmDeleteTableButton;

    @FindBy(css = "#deleteTableConfirmModal .close")
    private WebElement closeDeleteTableModalButton;

    /**
     * Constructor
     *
     * @param driver WebDriver instance
     */
    public TableSection(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    // ==================== CREATE Operations ====================

    /**
     * Add a new table
     *
     * @param tableNumber table number
     * @param tableName table name
     * @param roomName room name (can be null)
     * @param capacity table capacity
     * @return true if table was added successfully
     */
    public boolean addTable(String tableNumber, String tableName, String roomName, int capacity) {
        try {
            // Click Add Table button
            WebElement addTableBtn = driver.findElement(By.cssSelector(".toolbar .btn[onclick*='addTable']"));
            wait.until(ExpectedConditions.elementToBeClickable(addTableBtn)).click();
            wait.until(ExpectedConditions.visibilityOf(addTableModal));

            // Fill in form
            wait.until(ExpectedConditions.elementToBeClickable(tableNumberInput)).clear();
            tableNumberInput.sendKeys(tableNumber);

            tableNameInput.clear();
            tableNameInput.sendKeys(tableName);

            if (roomName != null && !roomName.isEmpty()) {
                Select roomSelect = new Select(roomIdSelect);
                try {
                    roomSelect.selectByVisibleText(roomName);
                } catch (Exception e) {
                    // Try selecting by partial text
                    List<WebElement> options = roomSelect.getOptions();
                    for (WebElement option : options) {
                        if (option.getText().contains(roomName)) {
                            option.click();
                            break;
                        }
                    }
                }
            }

            capacityInput.clear();
            capacityInput.sendKeys(String.valueOf(capacity));

            // Submit form
            wait.until(ExpectedConditions.elementToBeClickable(submitAddTableButton)).click();

            // Wait for modal to close and page to update
            wait.until(ExpectedConditions.invisibilityOf(addTableModal));
            Thread.sleep(1000);

            return true;
        } catch (Exception e) {
            System.err.println("Failed to add table: " + e.getMessage());
            return false;
        }
    }

    // ==================== READ Operations ====================

    /**
     * Get all table numbers from the table
     *
     * @return list of table numbers
     */
    public List<String> getAllTableNumbers() {
        List<String> tableNumbers = new ArrayList<>();
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".room-table-container:last-of-type .table tbody tr")));
            
            List<WebElement> rows = driver.findElements(
                By.cssSelector(".room-table-container:last-of-type .table tbody tr"));
            
            for (WebElement row : rows) {
                WebElement numberCell = row.findElement(By.cssSelector("td:first-child .table-number"));
                tableNumbers.add(numberCell.getText().trim());
            }
        } catch (Exception e) {
            System.err.println("Failed to get table numbers: " + e.getMessage());
        }
        return tableNumbers;
    }

    /**
     * Get table details by table number
     *
     * @param tableNumber table number
     * @return map containing table details, or null if not found
     */
    public Map<String, String> getTableDetails(String tableNumber) {
        try {
            List<WebElement> rows = driver.findElements(
                By.cssSelector(".room-table-container:last-of-type .table tbody tr"));
            
            for (WebElement row : rows) {
                WebElement numberCell = row.findElement(By.cssSelector("td:first-child .table-number"));
                if (numberCell.getText().trim().equals(tableNumber)) {
                    Map<String, String> details = new HashMap<>();
                    details.put("tableNumber", numberCell.getText().trim());
                    
                    // Get table name
                    WebElement nameCell = row.findElement(By.cssSelector("td:nth-child(2) .table-name"));
                    details.put("tableName", nameCell.getText().trim());
                    
                    // Get room name
                    WebElement roomCell = row.findElement(By.cssSelector("td:nth-child(3)"));
                    details.put("roomName", roomCell.getText().trim());
                    
                    // Get capacity
                    WebElement capacityCell = row.findElement(By.cssSelector("td:nth-child(4)"));
                    details.put("capacity", capacityCell.getText().trim());
                    
                    // Get status
                    WebElement statusCell = row.findElement(By.cssSelector("td:nth-child(5) .status"));
                    details.put("status", statusCell.getText().trim());
                    
                    // Get table ID from data attribute
                    String tableId = row.getAttribute("data-table-id");
                    details.put("tableId", tableId);
                    
                    return details;
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Failed to get table details: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get table status by table number
     *
     * @param tableNumber table number
     * @return table status (Available, Occupied, Reserved, Maintenance), or null if not found
     */
    public String getTableStatus(String tableNumber) {
        Map<String, String> details = getTableDetails(tableNumber);
        if (details != null) {
            return details.get("status");
        }
        return null;
    }

    /**
     * Check if table exists
     *
     * @param tableNumber table number
     * @return true if table exists
     */
    public boolean tableExists(String tableNumber) {
        return getAllTableNumbers().contains(tableNumber);
    }

    /**
     * Get number of tables displayed
     *
     * @return number of tables
     */
    public int getTableCount() {
        try {
            if (isEmptyStateDisplayed()) {
                return 0;
            }
            List<WebElement> rows = driver.findElements(
                By.cssSelector(".room-table-container:last-of-type .table tbody tr"));
            return rows.size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Check if empty state is displayed
     *
     * @return true if empty state is visible
     */
    public boolean isEmptyStateDisplayed() {
        try {
            WebElement emptyState = driver.findElement(
                By.cssSelector(".room-table-container:last-of-type .empty-state"));
            return emptyState.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== UPDATE Operations ====================

    /**
     * Edit a table
     *
     * @param tableNumber current table number
     * @param updates map containing fields to update (tableNumber, tableName, roomName, capacity, status)
     * @return true if table was updated successfully
     */
    public boolean editTable(String tableNumber, Map<String, Object> updates) {
        try {
            // Find and click Edit button for the table
            List<WebElement> rows = driver.findElements(
                By.cssSelector(".room-table-container:last-of-type .table tbody tr"));
            
            for (WebElement row : rows) {
                WebElement numberCell = row.findElement(By.cssSelector("td:first-child .table-number"));
                if (numberCell.getText().trim().equals(tableNumber)) {
                    // Click Edit button
                    WebElement editButton = row.findElement(
                        By.cssSelector("button[onclick*='editTable']"));
                    wait.until(ExpectedConditions.elementToBeClickable(editButton)).click();
                    
                    // Wait for edit modal
                    wait.until(ExpectedConditions.visibilityOf(editTableModal));
                    Thread.sleep(500);
                    
                    // Update fields if provided
                    if (updates.containsKey("tableNumber")) {
                        editTableNumberInput.clear();
                        editTableNumberInput.sendKeys(updates.get("tableNumber").toString());
                    }
                    
                    if (updates.containsKey("tableName")) {
                        editTableNameInput.clear();
                        editTableNameInput.sendKeys(updates.get("tableName").toString());
                    }
                    
                    if (updates.containsKey("roomName")) {
                        Select roomSelect = new Select(editRoomIdSelect);
                        try {
                            roomSelect.selectByVisibleText(updates.get("roomName").toString());
                        } catch (Exception e) {
                            // Try selecting by partial text
                            List<WebElement> options = roomSelect.getOptions();
                            for (WebElement option : options) {
                                if (option.getText().contains(updates.get("roomName").toString())) {
                                    option.click();
                                    break;
                                }
                            }
                        }
                    }
                    
                    if (updates.containsKey("capacity")) {
                        editCapacityInput.clear();
                        editCapacityInput.sendKeys(updates.get("capacity").toString());
                    }
                    
                    if (updates.containsKey("status")) {
                        Select statusSelect = new Select(editStatusSelect);
                        String status = updates.get("status").toString();
                        // Map Vietnamese status to English
                        if (status.contains("Trống") || status.equals("Available")) {
                            statusSelect.selectByValue("Available");
                        } else if (status.contains("Đang sử dụng") || status.equals("Occupied")) {
                            statusSelect.selectByValue("Occupied");
                        } else if (status.contains("Đã đặt") || status.equals("Reserved")) {
                            statusSelect.selectByValue("Reserved");
                        } else if (status.contains("Bảo trì") || status.equals("Maintenance")) {
                            statusSelect.selectByValue("Maintenance");
                        } else {
                            statusSelect.selectByValue(status);
                        }
                    }
                    
                    // Submit changes
                    wait.until(ExpectedConditions.elementToBeClickable(submitEditTableButton)).click();
                    
                    // Wait for modal to close and page to update
                    wait.until(ExpectedConditions.invisibilityOf(editTableModal));
                    Thread.sleep(1000);
                    
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Failed to edit table: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update table status
     *
     * @param tableNumber table number
     * @param status new status (Available, Occupied, Reserved, Maintenance)
     * @return true if status was updated successfully
     */
    public boolean updateTableStatus(String tableNumber, String status) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        return editTable(tableNumber, updates);
    }

    // ==================== DELETE Operations ====================

    /**
     * Delete a table
     *
     * @param tableNumber table number to delete
     * @return true if table was deleted successfully
     */
    public boolean deleteTable(String tableNumber) {
        try {
            // Find the table row
            List<WebElement> rows = driver.findElements(
                By.cssSelector(".room-table-container:last-of-type .table tbody tr"));
            
            for (WebElement row : rows) {
                WebElement numberCell = row.findElement(By.cssSelector("td:first-child .table-number"));
                if (numberCell.getText().trim().equals(tableNumber)) {
                    // Click Delete button
                    WebElement deleteButton = row.findElement(
                        By.cssSelector("button[onclick*='deleteTable']"));
                    wait.until(ExpectedConditions.elementToBeClickable(deleteButton)).click();
                    
                    // Wait for confirmation modal
                    wait.until(ExpectedConditions.visibilityOf(deleteTableConfirmModal));
                    
                    // Confirm deletion
                    wait.until(ExpectedConditions.elementToBeClickable(confirmDeleteTableButton)).click();
                    
                    // Wait for modal to close and page to update
                    wait.until(ExpectedConditions.invisibilityOf(deleteTableConfirmModal));
                    Thread.sleep(1000);
                    
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Failed to delete table: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cancel table deletion
     *
     * @param tableNumber table number
     * @return true if deletion was cancelled
     */
    public boolean cancelDeleteTable(String tableNumber) {
        try {
            // Find and click Delete button
            List<WebElement> rows = driver.findElements(
                By.cssSelector(".room-table-container:last-of-type .table tbody tr"));
            
            for (WebElement row : rows) {
                WebElement numberCell = row.findElement(By.cssSelector("td:first-child .table-number"));
                if (numberCell.getText().trim().equals(tableNumber)) {
                    WebElement deleteButton = row.findElement(
                        By.cssSelector("button[onclick*='deleteTable']"));
                    wait.until(ExpectedConditions.elementToBeClickable(deleteButton)).click();
                    
                    // Wait for confirmation modal
                    wait.until(ExpectedConditions.visibilityOf(deleteTableConfirmModal));
                    
                    // Cancel deletion
                    wait.until(ExpectedConditions.elementToBeClickable(closeDeleteTableModalButton)).click();
                    
                    // Wait for modal to close
                    wait.until(ExpectedConditions.invisibilityOf(deleteTableConfirmModal));
                    
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Failed to cancel delete table: " + e.getMessage());
            return false;
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Close Add Table modal
     */
    public void closeAddTableModal() {
        try {
            if (addTableModal.isDisplayed()) {
                wait.until(ExpectedConditions.elementToBeClickable(closeAddTableModalButton)).click();
                wait.until(ExpectedConditions.invisibilityOf(addTableModal));
            }
        } catch (Exception e) {
            // Modal might not be open
        }
    }

    /**
     * Wait for table table to update
     */
    public void waitForTableUpdate() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Check if table has specific status
     *
     * @param tableNumber table number
     * @param expectedStatus expected status
     * @return true if table has the expected status
     */
    public boolean hasStatus(String tableNumber, String expectedStatus) {
        String currentStatus = getTableStatus(tableNumber);
        if (currentStatus == null) {
            return false;
        }
        
        // Normalize status for comparison
        String normalizedCurrent = normalizeStatus(currentStatus);
        String normalizedExpected = normalizeStatus(expectedStatus);
        
        return normalizedCurrent.equals(normalizedExpected);
    }

    /**
     * Normalize status string for comparison
     *
     * @param status status string
     * @return normalized status
     */
    private String normalizeStatus(String status) {
        if (status == null) return "";
        status = status.trim();
        
        if (status.contains("Trống") || status.equalsIgnoreCase("Available")) {
            return "Available";
        } else if (status.contains("Đang sử dụng") || status.equalsIgnoreCase("Occupied")) {
            return "Occupied";
        } else if (status.contains("Đã đặt") || status.equalsIgnoreCase("Reserved")) {
            return "Reserved";
        } else if (status.contains("Bảo trì") || status.equalsIgnoreCase("Maintenance")) {
            return "Maintenance";
        }
        return status;
    }
}

