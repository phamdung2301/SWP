package com.liteflow.selenium.pages.cashier;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Main Page Object Model for Cashier page
 *
 * This class is the main entry point for interacting with the Cashier page.
 * It coordinates the three main sections: TableSection, MenuSection, and OrderSection.
 */
public class CashierPage {

    private WebDriver driver;
    private WebDriverWait wait;

    // Section objects
    private TableSection tableSection;
    private MenuSection menuSection;
    private OrderSection orderSection;

    // Main tab buttons
    @FindBy(css = ".main-tab-btn[data-tab='tables']")
    private WebElement tablesTabButton;

    @FindBy(css = ".main-tab-btn[data-tab='menu']")
    private WebElement menuTabButton;

    // Tab panels
    @FindBy(id = "tables-tab")
    private WebElement tablesTabPanel;

    @FindBy(id = "menu-tab")
    private WebElement menuTabPanel;

    // Invoice management
    @FindBy(css = ".invoice-tabs")
    private WebElement invoiceTabsContainer;

    @FindBy(css = ".invoice-tab")
    private List<WebElement> invoiceTabs;

    @FindBy(css = ".add-invoice-btn")
    private WebElement addInvoiceButton;

    @FindBy(id = "currentInvoiceName")
    private WebElement currentInvoiceName;

    // Notification panel
    @FindBy(css = ".notification-btn")
    private WebElement notificationButton;

    @FindBy(id = "notificationPanel")
    private WebElement notificationPanel;

    @FindBy(id = "notificationCount")
    private WebElement notificationCount;

    // User menu
    @FindBy(css = ".user-menu-btn")
    private WebElement userMenuButton;

    @FindBy(id = "userDropdown")
    private WebElement userDropdown;

    // Sound toggle
    @FindBy(id = "soundToggle")
    private WebElement soundToggleButton;

    /**
     * Constructor
     *
     * @param driver WebDriver instance
     */
    public CashierPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);

        // Initialize section objects
        this.tableSection = new TableSection(driver);
        this.menuSection = new MenuSection(driver);
        this.orderSection = new OrderSection(driver);
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
     * Get MenuSection object
     *
     * @return MenuSection instance
     */
    public MenuSection getMenuSection() {
        return menuSection;
    }

    /**
     * Get OrderSection object
     *
     * @return OrderSection instance
     */
    public OrderSection getOrderSection() {
        return orderSection;
    }

    /**
     * Navigate to cashier page
     */
    public void navigateToCashier() {
        driver.get(driver.getCurrentUrl().split("/cart")[0] + "/cart/cashier");
        waitForPageToLoad();
    }

    /**
     * Wait for page to load completely
     */
    public void waitForPageToLoad() {
        // Wait for main elements to be present
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".cashier-container")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("tablesGrid")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("menuGrid")));

        // Wait for JavaScript to finish loading data
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Switch to Tables tab
     */
    public void switchToTablesTab() {
        wait.until(ExpectedConditions.elementToBeClickable(tablesTabButton)).click();
        wait.until(ExpectedConditions.visibilityOf(tablesTabPanel));
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Switch to Menu tab
     */
    public void switchToMenuTab() {
        wait.until(ExpectedConditions.elementToBeClickable(menuTabButton)).click();
        wait.until(ExpectedConditions.visibilityOf(menuTabPanel));
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Check if Tables tab is active
     *
     * @return true if Tables tab is active
     */
    public boolean isTablesTabActive() {
        return tablesTabButton.getAttribute("class").contains("active");
    }

    /**
     * Check if Menu tab is active
     *
     * @return true if Menu tab is active
     */
    public boolean isMenuTabActive() {
        return menuTabButton.getAttribute("class").contains("active");
    }

    /**
     * Add a new invoice
     */
    public void addNewInvoice() {
        wait.until(ExpectedConditions.elementToBeClickable(addInvoiceButton)).click();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Switch to invoice by index
     *
     * @param index invoice index (1-based)
     */
    public void switchToInvoice(int index) {
        if (index > 0 && index <= invoiceTabs.size()) {
            WebElement invoiceTab = invoiceTabs.get(index - 1);
            wait.until(ExpectedConditions.elementToBeClickable(invoiceTab)).click();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Get current invoice name
     *
     * @return current invoice name
     */
    public String getCurrentInvoiceName() {
        return currentInvoiceName.getText();
    }

    /**
     * Get number of open invoices
     *
     * @return number of invoice tabs
     */
    public int getInvoiceCount() {
        return invoiceTabs.size();
    }

    /**
     * Close invoice by index
     *
     * @param index invoice index (1-based)
     */
    public void closeInvoice(int index) {
        if (index > 0 && index <= invoiceTabs.size()) {
            WebElement invoiceTab = invoiceTabs.get(index - 1);
            WebElement closeButton = invoiceTab.findElement(By.cssSelector(".bx-x"));
            wait.until(ExpectedConditions.elementToBeClickable(closeButton)).click();

            // Handle confirmation if present
            try {
                Thread.sleep(300);
                driver.switchTo().alert().accept();
            } catch (Exception ignored) {
                // No alert present
            }
        }
    }

    /**
     * Toggle notifications panel
     */
    public void toggleNotifications() {
        wait.until(ExpectedConditions.elementToBeClickable(notificationButton)).click();
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Get notification count
     *
     * @return notification count as integer, or 0 if not visible
     */
    public int getNotificationCount() {
        try {
            if (notificationCount.isDisplayed()) {
                return Integer.parseInt(notificationCount.getText());
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Check if notification panel is open
     *
     * @return true if notification panel is visible
     */
    public boolean isNotificationPanelOpen() {
        return notificationPanel.isDisplayed();
    }

    /**
     * Toggle user menu
     */
    public void toggleUserMenu() {
        wait.until(ExpectedConditions.elementToBeClickable(userMenuButton)).click();
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Navigate to a different page from user menu
     *
     * @param pageName page name ("management", "kitchen", "reception", etc.)
     */
    public void navigateFromUserMenu(String pageName) {
        toggleUserMenu();
        wait.until(ExpectedConditions.visibilityOf(userDropdown));

        // Find and click the menu item
        String xpath = String.format("//button[@class='user-dropdown-item']//span[contains(text(), '%s')]",
                                    getPageDisplayName(pageName));
        WebElement menuItem = driver.findElement(By.xpath(xpath));
        wait.until(ExpectedConditions.elementToBeClickable(menuItem)).click();
    }

    /**
     * Logout from user menu
     */
    public void logout() {
        toggleUserMenu();
        wait.until(ExpectedConditions.visibilityOf(userDropdown));

        WebElement logoutButton = driver.findElement(
            By.xpath("//button[@class='user-dropdown-item danger']//span[contains(text(), 'Đăng xuất')]")
        );
        wait.until(ExpectedConditions.elementToBeClickable(logoutButton)).click();
    }

    /**
     * Toggle sound
     */
    public void toggleSound() {
        wait.until(ExpectedConditions.elementToBeClickable(soundToggleButton)).click();
    }

    /**
     * Check if sound is enabled
     *
     * @return true if sound is enabled
     */
    public boolean isSoundEnabled() {
        String iconClass = soundToggleButton.findElement(By.tagName("i")).getAttribute("class");
        return iconClass.contains("bx-volume-full");
    }

    /**
     * Complete order workflow: select table, add items, and checkout
     *
     * @param tableNumber table number to select
     * @param items list of menu items to add (item name)
     * @param paymentMethod payment method ("cash", "card", "transfer")
     */
    public void completeOrderWorkflow(String tableNumber, List<String> items, String paymentMethod) {
        // Switch to tables tab and select table
        switchToTablesTab();
        tableSection.selectTable(tableNumber);

        // Switch to menu tab and add items
        switchToMenuTab();
        for (String item : items) {
            menuSection.addMenuItem(item);
        }

        // Checkout with selected payment method
        orderSection.checkout(paymentMethod);
    }

    /**
     * Get page display name for navigation
     *
     * @param pageName internal page name
     * @return display name in Vietnamese
     */
    private String getPageDisplayName(String pageName) {
        return switch (pageName.toLowerCase()) {
            case "management" -> "Quản lý";
            case "kitchen" -> "Nhà bếp";
            case "reception" -> "Lễ tân";
            case "end-of-day-report" -> "Báo cáo cuối ngày";
            default -> pageName;
        };
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
            return driver.findElement(By.cssSelector(".cashier-container")).isDisplayed() &&
                   driver.findElement(By.id("tablesGrid")).isDisplayed() &&
                   driver.findElement(By.id("menuGrid")).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
