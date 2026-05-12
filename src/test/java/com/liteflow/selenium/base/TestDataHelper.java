package com.liteflow.selenium.base;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Test Data Helper for Selenium tests
 *
 * This class provides utility methods for generating test data
 * used in Selenium system tests.
 */
public class TestDataHelper {

    private static final Random random = new Random();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Generate random room name
     *
     * @return random room name
     */
    public static String generateRandomRoomName() {
        String[] roomTypes = {"Main Hall", "VIP Room", "Private Room", "Garden Room", "Rooftop Room"};
        int number = random.nextInt(1000);
        return roomTypes[random.nextInt(roomTypes.length)] + " " + number;
    }

    /**
     * Generate random table number
     *
     * @return random table number
     */
    public static String generateRandomTableNumber() {
        return "T" + String.format("%03d", random.nextInt(1000));
    }

    /**
     * Generate random table name
     *
     * @param tableNumber table number
     * @return table name based on number
     */
    public static String generateTableName(String tableNumber) {
        return "Table " + tableNumber.substring(1);
    }

    /**
     * Generate random capacity between min and max
     *
     * @param min minimum capacity
     * @param max maximum capacity
     * @return random capacity
     */
    public static int generateRandomCapacity(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    /**
     * Generate random customer name
     *
     * @return random customer name
     */
    public static String generateRandomCustomerName() {
        String[] firstNames = {"John", "Jane", "Michael", "Emily", "David", "Sarah", "Robert", "Lisa"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis"};
        return firstNames[random.nextInt(firstNames.length)] + " " + lastNames[random.nextInt(lastNames.length)];
    }

    /**
     * Generate random phone number
     *
     * @return random phone number in format 0XXXXXXXXX
     */
    public static String generateRandomPhoneNumber() {
        return "0" + String.format("%09d", random.nextInt(1000000000));
    }

    /**
     * Generate random email
     *
     * @return random email address
     */
    public static String generateRandomEmail() {
        String[] domains = {"gmail.com", "yahoo.com", "outlook.com", "example.com"};
        return "test" + random.nextInt(10000) + "@" + domains[random.nextInt(domains.length)];
    }

    /**
     * Generate reservation code
     *
     * @return unique reservation code
     */
    public static String generateReservationCode() {
        return "RES" + System.currentTimeMillis() + random.nextInt(1000);
    }

    /**
     * Generate order number
     *
     * @return unique order number
     */
    public static String generateOrderNumber() {
        return "ORD" + System.currentTimeMillis() + random.nextInt(1000);
    }

    /**
     * Generate invoice number
     *
     * @return unique invoice number
     */
    public static String generateInvoiceNumber() {
        return "INV" + System.currentTimeMillis();
    }

    /**
     * Get current date string
     *
     * @return current date in yyyy-MM-dd format
     */
    public static String getCurrentDate() {
        return LocalDateTime.now().format(DATE_FORMATTER);
    }

    /**
     * Get current time string
     *
     * @return current time in HH:mm format
     */
    public static String getCurrentTime() {
        return LocalDateTime.now().format(TIME_FORMATTER);
    }

    /**
     * Get future date time string
     *
     * @param hoursFromNow hours from now
     * @return future date time in yyyy-MM-dd HH:mm format
     */
    public static String getFutureDateTime(int hoursFromNow) {
        return LocalDateTime.now().plusHours(hoursFromNow).format(DATETIME_FORMATTER);
    }

    /**
     * Get future date string
     *
     * @param daysFromNow days from now
     * @return future date in yyyy-MM-dd format
     */
    public static String getFutureDate(int daysFromNow) {
        return LocalDateTime.now().plusDays(daysFromNow).format(DATE_FORMATTER);
    }

    /**
     * Get future time string
     *
     * @param hoursFromNow hours from now
     * @return future time in HH:mm format
     */
    public static String getFutureTime(int hoursFromNow) {
        return LocalDateTime.now().plusHours(hoursFromNow).format(TIME_FORMATTER);
    }

    /**
     * Generate random number of guests
     *
     * @return random number between 1 and 10
     */
    public static int generateRandomGuestCount() {
        return 1 + random.nextInt(10);
    }

    /**
     * Generate random discount percentage
     *
     * @return random discount between 0 and 50
     */
    public static int generateRandomDiscount() {
        return random.nextInt(51);
    }

    /**
     * Generate random price
     *
     * @param min minimum price
     * @param max maximum price
     * @return random price
     */
    public static double generateRandomPrice(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    /**
     * Generate random quantity
     *
     * @param min minimum quantity
     * @param max maximum quantity
     * @return random quantity
     */
    public static int generateRandomQuantity(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    /**
     * Generate random notes/description
     *
     * @return random notes
     */
    public static String generateRandomNotes() {
        String[] notes = {
                "Please prepare the table near the window",
                "Birthday celebration",
                "Business meeting",
                "Anniversary dinner",
                "Family gathering",
                "Quiet corner preferred",
                "Need high chair for baby",
                "Vegetarian menu required"
        };
        return notes[random.nextInt(notes.length)];
    }

    /**
     * Wait for a specified duration (wrapper for Thread.sleep)
     *
     * @param milliseconds duration in milliseconds
     */
    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Sleep interrupted: " + e.getMessage());
        }
    }

    /**
     * Generate unique string with timestamp
     *
     * @param prefix prefix for the string
     * @return unique string
     */
    public static String generateUniqueString(String prefix) {
        return prefix + "_" + System.currentTimeMillis();
    }
}
