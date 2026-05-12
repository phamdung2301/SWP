package com.liteflow.selenium.pages.roomtable;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Page Object Model for Room Section in RoomTable page
 *
 * This class encapsulates all interactions with the rooms section,
 * including CRUD operations for rooms.
 */
public class RoomSection {

    private WebDriver driver;
    private WebDriverWait wait;

    // Room table elements
    @FindBy(css = ".room-table-container:first-of-type .table")
    private WebElement roomTable;

    @FindBy(css = ".room-table-container:first-of-type .table tbody tr")
    private List<WebElement> roomRows;

    @FindBy(css = ".room-table-container:first-of-type .empty-state")
    private WebElement emptyState;

    // Add Room Modal elements
    @FindBy(id = "addRoomModal")
    private WebElement addRoomModal;

    @FindBy(id = "roomName")
    private WebElement roomNameInput;

    @FindBy(id = "roomDescription")
    private WebElement roomDescriptionInput;

    @FindBy(id = "roomTableCount")
    private WebElement roomTableCountInput;

    @FindBy(id = "roomTotalCapacity")
    private WebElement roomTotalCapacityInput;

    @FindBy(css = "#addRoomModal .close")
    private WebElement closeAddRoomModalButton;

    // Submit button will be found dynamically in addRoom method

    @FindBy(css = "#addRoomModal .btn-warning[onclick*='closeAddRoomModal']")
    private WebElement cancelAddRoomButton;

    // Edit Room Modal elements (if exists)
    @FindBy(id = "editRoomModal")
    private WebElement editRoomModal;

    // Delete Confirmation Modal
    @FindBy(id = "deleteConfirmModal")
    private WebElement deleteConfirmModal;

    @FindBy(id = "confirmDeleteBtn")
    private WebElement confirmDeleteButton;

    @FindBy(css = "#deleteConfirmModal .close")
    private WebElement closeDeleteModalButton;

    /**
     * Constructor
     *
     * @param driver WebDriver instance
     */
    public RoomSection(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    // ==================== CREATE Operations ====================

    /**
     * Add a new room
     *
     * @param name room name
     * @param description room description (can be null)
     * @param tableCount maximum table count
     * @param totalCapacity total capacity
     * @return true if room was added successfully
     */
    public boolean addRoom(String name, String description, int tableCount, int totalCapacity) {
        try {
            // Click Add Room button - try multiple selectors
            WebElement addRoomBtn = null;
            try {
                addRoomBtn = driver.findElement(By.cssSelector(".toolbar .btn[onclick*='addRoom']"));
            } catch (Exception e) {
                // Try alternative selector
                addRoomBtn = driver.findElement(By.xpath("//button[contains(@onclick, 'addRoom')] | //a[contains(@onclick, 'addRoom')]"));
            }
            
            wait.until(ExpectedConditions.elementToBeClickable(addRoomBtn)).click();
            
            // Wait for modal to be visible and displayed
            wait.until(ExpectedConditions.visibilityOf(addRoomModal));
            
            // Wait for modal to be fully displayed (check if it's actually visible)
            int attempts = 0;
            while (attempts < 10) {
                try {
                    String displayStyle = addRoomModal.getCssValue("display");
                    if (!"none".equals(displayStyle) && addRoomModal.isDisplayed()) {
                        break;
                    }
                } catch (Exception e) {
                    // Continue waiting
                }
                Thread.sleep(100);
                attempts++;
            }
            
            Thread.sleep(500); // Wait for modal to fully render

            // Fill in form - wait for inputs to be ready
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("roomName")));
            wait.until(ExpectedConditions.elementToBeClickable(By.id("roomName")));
            
            WebElement nameInput = driver.findElement(By.id("roomName"));
            nameInput.clear();
            nameInput.sendKeys(name);

            if (description != null && !description.isEmpty()) {
                WebElement descInput = driver.findElement(By.id("roomDescription"));
                descInput.clear();
                descInput.sendKeys(description);
            }

            WebElement tableCountInput = driver.findElement(By.id("roomTableCount"));
            tableCountInput.clear();
            tableCountInput.sendKeys(String.valueOf(tableCount));

            WebElement capacityInput = driver.findElement(By.id("roomTotalCapacity"));
            capacityInput.clear();
            capacityInput.sendKeys(String.valueOf(totalCapacity));

            // Wait a bit for form to be ready and JavaScript to create buttons
            Thread.sleep(500);

            // Wait for submit button to be present - buttons might be created dynamically
            WebElement submitBtn = null;
            int buttonWaitAttempts = 0;
            while (buttonWaitAttempts < 30) {
                try {
                    // Try by text content first (most reliable)
                    submitBtn = driver.findElement(By.xpath("//div[@id='addRoomModal']//button[contains(@class, 'btn-success') and (contains(text(), 'Thêm phòng') or contains(text(), 'Thêm'))]"));
                    if (submitBtn != null && submitBtn.isDisplayed()) {
                        break;
                    }
                } catch (Exception e1) {
                    // Continue trying
                }
                
                try {
                    // Try by onclick attribute
                    submitBtn = driver.findElement(By.cssSelector("#addRoomModal button.btn-success[onclick*='submitAddRoom']"));
                    if (submitBtn != null && submitBtn.isDisplayed()) {
                        break;
                    }
                } catch (Exception e2) {
                    // Continue trying
                }
                
                try {
                    // Try any button in modal footer
                    submitBtn = driver.findElement(By.cssSelector("#addRoomModal .modal-footer .btn-success"));
                    if (submitBtn != null && submitBtn.isDisplayed()) {
                        break;
                    }
                } catch (Exception e3) {
                    // Continue trying
                }
                
                Thread.sleep(100);
                buttonWaitAttempts++;
            }

            if (submitBtn == null) {
                throw new Exception("Could not find submit button after waiting");
            }

            // Wait for button to be clickable
            wait.until(ExpectedConditions.elementToBeClickable(submitBtn));
            Thread.sleep(300);
            
            // Try clicking with JavaScript if normal click doesn't work
            try {
                submitBtn.click();
            } catch (Exception clickEx) {
                // Fallback to JavaScript click
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", submitBtn);
            }
            
            // Wait a moment for the click to register
            Thread.sleep(500);

            // Wait for modal to close - check for invisibility
            try {
                wait.until(ExpectedConditions.invisibilityOf(addRoomModal));
            } catch (Exception e) {
                // Modal might not close immediately, check display style
                try {
                    int closeAttempts = 0;
                    while (closeAttempts < 20) {
                        String displayStyle = addRoomModal.getCssValue("display");
                        if ("none".equals(displayStyle) || !addRoomModal.isDisplayed()) {
                            break;
                        }
                        Thread.sleep(100);
                        closeAttempts++;
                    }
                } catch (Exception styleEx) {
                    // Wait a bit more as fallback
                    Thread.sleep(1000);
                }
            }

            // Wait for page to update
            Thread.sleep(1500);

            return true;
        } catch (Exception e) {
            System.err.println("Failed to add room: " + e.getMessage());
            e.printStackTrace();
            
            // Try to close modal if it's still open
            try {
                if (addRoomModal.isDisplayed()) {
                    WebElement closeBtn = driver.findElement(By.cssSelector("#addRoomModal .close, #addRoomModal .btn-warning"));
                    closeBtn.click();
                    Thread.sleep(500);
                }
            } catch (Exception closeEx) {
                // Ignore close errors
            }
            
            return false;
        }
    }

    // ==================== READ Operations ====================

    /**
     * Get all room names from the table
     *
     * @return list of room names
     */
    public List<String> getAllRoomNames() {
        List<String> roomNames = new ArrayList<>();
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".room-table-container:first-of-type .table tbody tr")));
            
            List<WebElement> rows = driver.findElements(
                By.cssSelector(".room-table-container:first-of-type .table tbody tr"));
            
            for (WebElement row : rows) {
                WebElement nameCell = row.findElement(By.cssSelector("td:first-child .room-name"));
                roomNames.add(nameCell.getText().trim());
            }
        } catch (Exception e) {
            System.err.println("Failed to get room names: " + e.getMessage());
        }
        return roomNames;
    }

    /**
     * Get room details by name
     *
     * @param roomName room name
     * @return map containing room details, or null if not found
     */
    public Map<String, String> getRoomDetails(String roomName) {
        try {
            List<WebElement> rows = driver.findElements(
                By.cssSelector(".room-table-container:first-of-type .table tbody tr"));
            
            for (WebElement row : rows) {
                WebElement nameCell = row.findElement(By.cssSelector("td:first-child .room-name"));
                if (nameCell.getText().trim().equals(roomName)) {
                    Map<String, String> details = new HashMap<>();
                    details.put("name", nameCell.getText().trim());
                    
                    // Get description
                    WebElement descCell = row.findElement(By.cssSelector("td:nth-child(2)"));
                    details.put("description", descCell.getText().trim());
                    
                    // Get table count
                    WebElement tableCountCell = row.findElement(By.cssSelector("td:nth-child(4)"));
                    details.put("tableCount", tableCountCell.getText().trim());
                    
                    // Get total capacity
                    WebElement capacityCell = row.findElement(By.cssSelector("td:nth-child(5)"));
                    details.put("totalCapacity", capacityCell.getText().trim());
                    
                    // Get room ID from data attribute
                    String roomId = row.getAttribute("data-room-id");
                    details.put("roomId", roomId);
                    
                    return details;
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Failed to get room details: " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if room exists
     *
     * @param roomName room name
     * @return true if room exists
     */
    public boolean roomExists(String roomName) {
        return getAllRoomNames().contains(roomName);
    }

    /**
     * Get number of rooms displayed
     *
     * @return number of rooms
     */
    public int getRoomCount() {
        try {
            if (isEmptyStateDisplayed()) {
                return 0;
            }
            List<WebElement> rows = driver.findElements(
                By.cssSelector(".room-table-container:first-of-type .table tbody tr"));
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
                By.cssSelector(".room-table-container:first-of-type .empty-state"));
            return emptyState.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== UPDATE Operations ====================

    /**
     * Edit a room
     *
     * @param roomName current room name
     * @param updates map containing fields to update (name, description, tableCount, totalCapacity)
     * @return true if room was updated successfully
     */
    public boolean editRoom(String roomName, Map<String, Object> updates) {
        try {
            // Find and click Edit button for the room
            List<WebElement> rows = driver.findElements(
                By.cssSelector(".room-table-container:first-of-type .table tbody tr"));
            
            for (WebElement row : rows) {
                WebElement nameCell = row.findElement(By.cssSelector("td:first-child .room-name"));
                if (nameCell.getText().trim().equals(roomName)) {
                    // Click Edit button
                    WebElement editButton = row.findElement(
                        By.cssSelector("button[onclick*='editRoom']"));
                    wait.until(ExpectedConditions.elementToBeClickable(editButton)).click();
                    
                    // Wait for edit modal (might be same as add modal or different)
                    Thread.sleep(500);
                    
                    // Update fields if provided
                    if (updates.containsKey("name")) {
                        WebElement nameInput = driver.findElement(By.id("editRoomName"));
                        if (nameInput != null) {
                            nameInput.clear();
                            nameInput.sendKeys(updates.get("name").toString());
                        }
                    }
                    
                    if (updates.containsKey("description")) {
                        WebElement descInput = driver.findElement(By.id("editRoomDescription"));
                        if (descInput != null) {
                            descInput.clear();
                            descInput.sendKeys(updates.get("description").toString());
                        }
                    }
                    
                    if (updates.containsKey("tableCount")) {
                        WebElement tableCountInput = driver.findElement(By.id("editRoomTableCount"));
                        if (tableCountInput != null) {
                            tableCountInput.clear();
                            tableCountInput.sendKeys(updates.get("tableCount").toString());
                        }
                    }
                    
                    if (updates.containsKey("totalCapacity")) {
                        WebElement capacityInput = driver.findElement(By.id("editRoomTotalCapacity"));
                        if (capacityInput != null) {
                            capacityInput.clear();
                            capacityInput.sendKeys(updates.get("totalCapacity").toString());
                        }
                    }
                    
                    // Submit changes
                    WebElement submitButton = driver.findElement(
                        By.cssSelector("#editRoomModal .btn-success[onclick*='submitEditRoom']"));
                    if (submitButton != null) {
                        wait.until(ExpectedConditions.elementToBeClickable(submitButton)).click();
                        Thread.sleep(1000);
                        return true;
                    }
                    
                    // If no edit modal, try using JavaScript
                    String roomId = row.getAttribute("data-room-id");
                    if (roomId != null) {
                        // Use JavaScript to call editRoom function
                        ((org.openqa.selenium.JavascriptExecutor) driver)
                            .executeScript("editRoom('" + roomId + "');");
                        Thread.sleep(500);
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Failed to edit room: " + e.getMessage());
            return false;
        }
    }

    // ==================== DELETE Operations ====================

    /**
     * Delete a room
     *
     * @param roomName room name to delete
     * @return true if room was deleted successfully
     */
    public boolean deleteRoom(String roomName) {
        try {
            // Find the room row
            List<WebElement> rows = driver.findElements(
                By.cssSelector(".room-table-container:first-of-type .table tbody tr"));
            
            for (WebElement row : rows) {
                WebElement nameCell = row.findElement(By.cssSelector("td:first-child .room-name"));
                if (nameCell.getText().trim().equals(roomName)) {
                    // Click Delete button
                    WebElement deleteButton = row.findElement(
                        By.cssSelector("button[onclick*='deleteRoom']"));
                    wait.until(ExpectedConditions.elementToBeClickable(deleteButton)).click();
                    
                    // Wait for confirmation modal
                    wait.until(ExpectedConditions.visibilityOf(deleteConfirmModal));
                    
                    // Confirm deletion
                    wait.until(ExpectedConditions.elementToBeClickable(confirmDeleteButton)).click();
                    
                    // Wait for modal to close and page to update
                    wait.until(ExpectedConditions.invisibilityOf(deleteConfirmModal));
                    Thread.sleep(1000);
                    
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Failed to delete room: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cancel room deletion
     *
     * @param roomName room name
     * @return true if deletion was cancelled
     */
    public boolean cancelDeleteRoom(String roomName) {
        try {
            // Find and click Delete button
            List<WebElement> rows = driver.findElements(
                By.cssSelector(".room-table-container:first-of-type .table tbody tr"));
            
            for (WebElement row : rows) {
                WebElement nameCell = row.findElement(By.cssSelector("td:first-child .room-name"));
                if (nameCell.getText().trim().equals(roomName)) {
                    WebElement deleteButton = row.findElement(
                        By.cssSelector("button[onclick*='deleteRoom']"));
                    wait.until(ExpectedConditions.elementToBeClickable(deleteButton)).click();
                    
                    // Wait for confirmation modal
                    wait.until(ExpectedConditions.visibilityOf(deleteConfirmModal));
                    
                    // Cancel deletion
                    wait.until(ExpectedConditions.elementToBeClickable(closeDeleteModalButton)).click();
                    
                    // Wait for modal to close
                    wait.until(ExpectedConditions.invisibilityOf(deleteConfirmModal));
                    
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Failed to cancel delete room: " + e.getMessage());
            return false;
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Close Add Room modal
     */
    public void closeAddRoomModal() {
        try {
            if (addRoomModal.isDisplayed()) {
                wait.until(ExpectedConditions.elementToBeClickable(closeAddRoomModalButton)).click();
                wait.until(ExpectedConditions.invisibilityOf(addRoomModal));
            }
        } catch (Exception e) {
            // Modal might not be open
        }
    }

    /**
     * Wait for room table to update
     */
    public void waitForTableUpdate() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

