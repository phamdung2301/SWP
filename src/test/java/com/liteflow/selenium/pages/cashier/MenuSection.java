package com.liteflow.selenium.pages.cashier;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object Model for Menu Section in Cashier page
 *
 * This class encapsulates all interactions with the menu section,
 * including category filters, menu items, and search functionality.
 */
public class MenuSection {

    private WebDriver driver;
    private WebDriverWait wait;

    // Search box
    @FindBy(id = "headerSearch")
    private WebElement searchInput;

    // Category filters
    @FindBy(css = ".category-filters")
    private WebElement categoryFiltersContainer;

    @FindBy(css = ".category-btn")
    private List<WebElement> categoryButtons;

    // Menu grid
    @FindBy(id = "menuGrid")
    private WebElement menuGrid;

    @FindBy(css = ".menu-grid .menu-item")
    private List<WebElement> menuItems;

    // Guide button
    @FindBy(css = ".guide-btn")
    private WebElement guideButton;

    /**
     * Constructor
     *
     * @param driver WebDriver instance
     */
    public MenuSection(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    /**
     * Search for menu items
     *
     * @param keyword search keyword
     */
    public void searchMenu(String keyword) {
        wait.until(ExpectedConditions.elementToBeClickable(searchInput));
        searchInput.clear();
        searchInput.sendKeys(keyword);

        // Wait for search to apply
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
        wait.until(ExpectedConditions.elementToBeClickable(searchInput));
        searchInput.clear();
        searchInput.sendKeys(Keys.ESCAPE);

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Filter menu by category
     *
     * @param categoryName category name to filter by (e.g., "Món chính", "Đồ uống")
     * @return true if category was found and selected
     */
    public boolean filterByCategory(String categoryName) {
        try {
            for (WebElement categoryBtn : categoryButtons) {
                if (categoryBtn.getText().contains(categoryName)) {
                    wait.until(ExpectedConditions.elementToBeClickable(categoryBtn)).click();
                    Thread.sleep(300);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Failed to filter by category: " + e.getMessage());
            return false;
        }
    }

    /**
     * Select "All" category
     */
    public void selectAllCategories() {
        try {
            WebElement allCategoryBtn = driver.findElement(By.cssSelector(".category-btn[data-category='all']"));
            wait.until(ExpectedConditions.elementToBeClickable(allCategoryBtn)).click();
            Thread.sleep(300);
        } catch (Exception e) {
            System.err.println("Failed to select all categories: " + e.getMessage());
        }
    }

    /**
     * Add menu item to order by item name
     *
     * @param itemName name of the menu item
     * @param quantity quantity to add (number of clicks)
     * @return true if item was added successfully
     */
    public boolean addMenuItem(String itemName, int quantity) {
        try {
            WebElement menuItem = findMenuItemByName(itemName);
            if (menuItem != null) {
                scrollToElement(menuItem);

                // Click the menu item multiple times for quantity
                for (int i = 0; i < quantity; i++) {
                    wait.until(ExpectedConditions.elementToBeClickable(menuItem)).click();
                    Thread.sleep(200); // Small delay between clicks
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Failed to add menu item: " + e.getMessage());
            return false;
        }
    }

    /**
     * Add menu item to order (single click)
     *
     * @param itemName name of the menu item
     * @return true if item was added successfully
     */
    public boolean addMenuItem(String itemName) {
        return addMenuItem(itemName, 1);
    }

    /**
     * Check if menu item is available (in stock)
     *
     * @param itemName name of the menu item
     * @return true if item is available
     */
    public boolean isMenuItemAvailable(String itemName) {
        WebElement menuItem = findMenuItemByName(itemName);
        if (menuItem != null) {
            // Check if item has "out-of-stock" class or disabled attribute
            String classes = menuItem.getAttribute("class");
            return !classes.contains("out-of-stock") &&
                   !classes.contains("disabled") &&
                   menuItem.isEnabled();
        }
        return false;
    }

    /**
     * Get menu item price
     *
     * @param itemName name of the menu item
     * @return price as string, or null if not found
     */
    public String getMenuItemPrice(String itemName) {
        WebElement menuItem = findMenuItemByName(itemName);
        if (menuItem != null) {
            try {
                WebElement priceElement = menuItem.findElement(By.cssSelector(".menu-item-price, .item-price, .price"));
                return priceElement.getText();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Get menu item stock quantity
     *
     * @param itemName name of the menu item
     * @return stock quantity as string, or null if not found
     */
    public String getMenuItemStock(String itemName) {
        WebElement menuItem = findMenuItemByName(itemName);
        if (menuItem != null) {
            try {
                WebElement stockElement = menuItem.findElement(By.cssSelector(".stock-info, .item-stock"));
                return stockElement.getText();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Get total number of visible menu items
     *
     * @return number of visible menu items
     */
    public int getVisibleMenuItemsCount() {
        return menuItems.size();
    }

    /**
     * Get all visible menu item names
     *
     * @return list of menu item names
     */
    public List<String> getVisibleMenuItemNames() {
        return menuItems.stream()
            .map(item -> {
                try {
                    return item.findElement(By.cssSelector(".menu-item-name, .item-name")).getText();
                } catch (Exception e) {
                    return "";
                }
            })
            .filter(name -> !name.isEmpty())
            .toList();
    }

    /**
     * Check if a specific menu item is visible
     *
     * @param itemName name of the menu item
     * @return true if item is visible
     */
    public boolean isMenuItemVisible(String itemName) {
        return findMenuItemByName(itemName) != null;
    }

    /**
     * Get active category name
     *
     * @return name of the active category
     */
    public String getActiveCategory() {
        try {
            WebElement activeBtn = driver.findElement(By.cssSelector(".category-btn.active"));
            return activeBtn.getText();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get all available categories
     *
     * @return list of category names
     */
    public List<String> getAllCategories() {
        return categoryButtons.stream()
            .map(WebElement::getText)
            .toList();
    }

    /**
     * Click guide button
     */
    public void clickGuide() {
        wait.until(ExpectedConditions.elementToBeClickable(guideButton)).click();
    }

    /**
     * Find menu item element by name
     *
     * @param itemName name of the menu item
     * @return WebElement of the menu item, or null if not found
     */
    private WebElement findMenuItemByName(String itemName) {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("menuGrid")));

            // Try to find by data attribute first
            List<WebElement> items = driver.findElements(By.cssSelector(".menu-item"));
            for (WebElement item : items) {
                String name = item.getAttribute("data-item-name");
                if (name != null && name.equalsIgnoreCase(itemName)) {
                    return item;
                }

                // Alternative: check text content
                try {
                    String itemText = item.findElement(By.cssSelector(".menu-item-name, .item-name")).getText();
                    if (itemText.equalsIgnoreCase(itemName)) {
                        return item;
                    }
                } catch (Exception ignored) {
                    // Continue to next item
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error finding menu item: " + e.getMessage());
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
