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
 * Page Object Model for Order Section in Cashier page
 *
 * This class encapsulates all interactions with the order section,
 * including order items, bill summary, payment methods, and checkout.
 */
public class OrderSection {

    private WebDriver driver;
    private WebDriverWait wait;

    // Section header
    @FindBy(id = "selectedTableInfo")
    private WebElement selectedTableInfo;

    // Order items
    @FindBy(id = "orderItems")
    private WebElement orderItemsContainer;

    @FindBy(css = ".order-item")
    private List<WebElement> orderItems;

    @FindBy(css = ".empty-order")
    private WebElement emptyOrderMessage;

    // Bill summary elements
    @FindBy(id = "subtotal")
    private WebElement subtotalElement;

    @FindBy(id = "discount")
    private WebElement discountElement;

    @FindBy(id = "discountRow")
    private WebElement discountRow;

    @FindBy(id = "vatRate")
    private WebElement vatRateInput;

    @FindBy(id = "vat")
    private WebElement vatElement;

    @FindBy(id = "total")
    private WebElement totalElement;

    // Payment method buttons
    @FindBy(css = ".payment-btn[data-method='cash']")
    private WebElement cashPaymentButton;

    @FindBy(css = ".payment-btn[data-method='card']")
    private WebElement cardPaymentButton;

    @FindBy(css = ".payment-btn[data-method='transfer']")
    private WebElement transferPaymentButton;

    // Action buttons
    @FindBy(id = "clearOrder")
    private WebElement clearOrderButton;

    @FindBy(id = "orderNoteBtn")
    private WebElement orderNoteButton;

    @FindBy(id = "discountBtn")
    private WebElement discountButton;

    @FindBy(id = "notifyKitchenBtn")
    private WebElement notifyKitchenButton;

    @FindBy(id = "checkoutBtn")
    private WebElement checkoutButton;

    // Discount modal elements
    @FindBy(id = "discountModal")
    private WebElement discountModal;

    @FindBy(id = "discountInput")
    private WebElement discountInput;

    @FindBy(css = ".discount-tabs .tab[data-type='percent']")
    private WebElement percentDiscountTab;

    @FindBy(css = ".discount-tabs .tab[data-type='amount']")
    private WebElement amountDiscountTab;

    @FindBy(id = "removeDiscountBtn")
    private WebElement removeDiscountButton;

    /**
     * Constructor
     *
     * @param driver WebDriver instance
     */
    public OrderSection(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    /**
     * Get selected table info text
     *
     * @return selected table info text
     */
    public String getSelectedTableInfo() {
        return selectedTableInfo.getText();
    }

    /**
     * Check if order is empty
     *
     * @return true if order is empty
     */
    public boolean isOrderEmpty() {
        try {
            return emptyOrderMessage.isDisplayed();
        } catch (Exception e) {
            return orderItems.isEmpty();
        }
    }

    /**
     * Get number of order items
     *
     * @return number of order items
     */
    public int getOrderItemsCount() {
        return orderItems.size();
    }

    /**
     * Get order item names
     *
     * @return list of order item names
     */
    public List<String> getOrderItemNames() {
        return orderItems.stream()
            .map(item -> {
                try {
                    return item.findElement(By.cssSelector(".item-name, .order-item-name")).getText();
                } catch (Exception e) {
                    return "";
                }
            })
            .filter(name -> !name.isEmpty())
            .toList();
    }

    /**
     * Remove order item by index
     *
     * @param index index of the item to remove (0-based)
     * @return true if item was removed successfully
     */
    public boolean removeOrderItem(int index) {
        try {
            if (index >= 0 && index < orderItems.size()) {
                WebElement item = orderItems.get(index);
                WebElement removeButton = item.findElement(By.cssSelector(".remove-item, .btn-remove, .delete-btn"));
                wait.until(ExpectedConditions.elementToBeClickable(removeButton)).click();
                Thread.sleep(300);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Failed to remove order item: " + e.getMessage());
            return false;
        }
    }

    /**
     * Remove order item by name
     *
     * @param itemName name of the item to remove
     * @return true if item was removed successfully
     */
    public boolean removeOrderItemByName(String itemName) {
        try {
            for (WebElement item : orderItems) {
                String name = item.findElement(By.cssSelector(".item-name, .order-item-name")).getText();
                if (name.equalsIgnoreCase(itemName)) {
                    WebElement removeButton = item.findElement(By.cssSelector(".remove-item, .btn-remove, .delete-btn"));
                    wait.until(ExpectedConditions.elementToBeClickable(removeButton)).click();
                    Thread.sleep(300);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Failed to remove order item by name: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update order item quantity
     *
     * @param index index of the item (0-based)
     * @param quantity new quantity
     * @return true if quantity was updated successfully
     */
    public boolean updateOrderItemQuantity(int index, int quantity) {
        try {
            if (index >= 0 && index < orderItems.size()) {
                WebElement item = orderItems.get(index);
                WebElement quantityInput = item.findElement(By.cssSelector(".quantity-input, input[type='number']"));

                quantityInput.clear();
                quantityInput.sendKeys(String.valueOf(quantity));

                // Trigger change event
                quantityInput.sendKeys(org.openqa.selenium.Keys.TAB);
                Thread.sleep(300);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Failed to update order item quantity: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get subtotal amount
     *
     * @return subtotal as string (e.g., "100,000đ")
     */
    public String getSubtotal() {
        return subtotalElement.getText();
    }

    /**
     * Get discount amount
     *
     * @return discount as string
     */
    public String getDiscount() {
        return discountElement.getText();
    }

    /**
     * Check if discount is applied
     *
     * @return true if discount row is visible
     */
    public boolean isDiscountApplied() {
        return discountRow.isDisplayed();
    }

    /**
     * Get VAT rate
     *
     * @return VAT rate as integer
     */
    public int getVATRate() {
        return Integer.parseInt(vatRateInput.getAttribute("value"));
    }

    /**
     * Set VAT rate
     *
     * @param rate VAT rate percentage
     */
    public void setVATRate(int rate) {
        wait.until(ExpectedConditions.elementToBeClickable(vatRateInput));
        vatRateInput.clear();
        vatRateInput.sendKeys(String.valueOf(rate));
        vatRateInput.sendKeys(org.openqa.selenium.Keys.TAB);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Get VAT amount
     *
     * @return VAT amount as string
     */
    public String getVAT() {
        return vatElement.getText();
    }

    /**
     * Get total amount
     *
     * @return total as string
     */
    public String getTotal() {
        return totalElement.getText();
    }

    /**
     * Select payment method
     *
     * @param method payment method ("cash", "card", "transfer")
     */
    public void selectPaymentMethod(String method) {
        WebElement paymentButton;
        switch (method.toLowerCase()) {
            case "cash":
                paymentButton = cashPaymentButton;
                break;
            case "card":
                paymentButton = cardPaymentButton;
                break;
            case "transfer":
                paymentButton = transferPaymentButton;
                break;
            default:
                throw new IllegalArgumentException("Invalid payment method: " + method);
        }

        wait.until(ExpectedConditions.elementToBeClickable(paymentButton)).click();
    }

    /**
     * Get selected payment method
     *
     * @return selected payment method, or null if none selected
     */
    public String getSelectedPaymentMethod() {
        try {
            List<WebElement> paymentButtons = driver.findElements(By.cssSelector(".payment-btn"));
            for (WebElement btn : paymentButtons) {
                if (btn.getAttribute("class").contains("active") ||
                    btn.getAttribute("class").contains("selected")) {
                    return btn.getAttribute("data-method");
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Clear order
     */
    public void clearOrder() {
        wait.until(ExpectedConditions.elementToBeClickable(clearOrderButton)).click();

        // Handle confirmation dialog if present
        try {
            Thread.sleep(300);
            driver.switchTo().alert().accept();
        } catch (Exception ignored) {
            // No alert present
        }
    }

    /**
     * Click order note button
     */
    public void clickOrderNote() {
        wait.until(ExpectedConditions.elementToBeClickable(orderNoteButton)).click();
    }

    /**
     * Open discount modal
     */
    public void openDiscountModal() {
        wait.until(ExpectedConditions.elementToBeClickable(discountButton)).click();
        wait.until(ExpectedConditions.visibilityOf(discountModal));
    }

    /**
     * Apply percentage discount
     *
     * @param percentage discount percentage
     */
    public void applyPercentageDiscount(double percentage) {
        openDiscountModal();

        // Select percentage tab
        wait.until(ExpectedConditions.elementToBeClickable(percentDiscountTab)).click();

        // Enter discount value
        wait.until(ExpectedConditions.elementToBeClickable(discountInput));
        discountInput.clear();
        discountInput.sendKeys(String.valueOf(percentage));

        // Click apply button
        WebElement applyButton = driver.findElement(By.xpath("//button[contains(text(), 'Áp dụng')]"));
        wait.until(ExpectedConditions.elementToBeClickable(applyButton)).click();

        // Wait for modal to close
        wait.until(ExpectedConditions.invisibilityOf(discountModal));
    }

    /**
     * Apply amount discount
     *
     * @param amount discount amount
     */
    public void applyAmountDiscount(double amount) {
        openDiscountModal();

        // Select amount tab
        wait.until(ExpectedConditions.elementToBeClickable(amountDiscountTab)).click();

        // Enter discount value
        wait.until(ExpectedConditions.elementToBeClickable(discountInput));
        discountInput.clear();
        discountInput.sendKeys(String.valueOf(amount));

        // Click apply button
        WebElement applyButton = driver.findElement(By.xpath("//button[contains(text(), 'Áp dụng')]"));
        wait.until(ExpectedConditions.elementToBeClickable(applyButton)).click();

        // Wait for modal to close
        wait.until(ExpectedConditions.invisibilityOf(discountModal));
    }

    /**
     * Remove discount
     */
    public void removeDiscount() {
        if (isDiscountApplied()) {
            openDiscountModal();
            wait.until(ExpectedConditions.elementToBeClickable(removeDiscountButton)).click();
            wait.until(ExpectedConditions.invisibilityOf(discountModal));
        }
    }

    /**
     * Notify kitchen
     */
    public void notifyKitchen() {
        wait.until(ExpectedConditions.elementToBeClickable(notifyKitchenButton)).click();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Check if notify kitchen button is enabled
     *
     * @return true if button is enabled
     */
    public boolean isNotifyKitchenEnabled() {
        return notifyKitchenButton.isEnabled() &&
               !notifyKitchenButton.getAttribute("class").contains("disabled");
    }

    /**
     * Click checkout button
     */
    public void clickCheckout() {
        wait.until(ExpectedConditions.elementToBeClickable(checkoutButton)).click();
    }

    /**
     * Check if checkout button is enabled
     *
     * @return true if button is enabled
     */
    public boolean isCheckoutEnabled() {
        return checkoutButton.isEnabled() &&
               !checkoutButton.getAttribute("class").contains("disabled");
    }

    /**
     * Perform complete checkout with payment method
     *
     * @param paymentMethod payment method ("cash", "card", "transfer")
     */
    public void checkout(String paymentMethod) {
        selectPaymentMethod(paymentMethod);
        clickCheckout();

        // Wait for checkout to complete
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Verify order total matches expected value
     *
     * @param expectedTotal expected total amount (as formatted string)
     * @return true if totals match
     */
    public boolean verifyOrderTotal(String expectedTotal) {
        String actualTotal = getTotal();
        return actualTotal.equals(expectedTotal);
    }
}
