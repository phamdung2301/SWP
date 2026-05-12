package com.liteflow.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.Base64;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class Utils {

    // Properties handling
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = Utils.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("⚠ config.properties not found, using default values");
                setDefaultProperties();
            } else {
                properties.load(input);
                System.out.println("✅ Properties loaded successfully");
            }
        } catch (IOException e) {
            System.err.println("❌ Error loading config.properties: " + e.getMessage());
            setDefaultProperties();
        }
    }

    private static void setDefaultProperties() {
        properties.setProperty("google.clientId", "demo-client-id");
        properties.setProperty("google.clientSecret", "demo-client-secret");
        properties.setProperty("google.redirectUri", "http://localhost:8080/callback");
        properties.setProperty("scope", "email profile");
        properties.setProperty("db.url", "jdbc:sqlserver://localhost:1433");
        properties.setProperty("db.username", "sa");
        properties.setProperty("db.password", "123");
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    // String helpers
    public static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static String normalizeName(String name) {
        if (name == null || name.isEmpty()) return "";
        name = name.replaceAll("[^a-zA-Z ]", "");
        StringBuilder sb = new StringBuilder();
        for (String part : name.trim().split("\\s+")) {
            if (!part.isEmpty()) {
                sb.append(part.substring(0, 1).toUpperCase())
                  .append(part.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }

    // Validation helpers
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^(\\d{9}|\\d{10})$");
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;
        return password.matches(".*[A-Z].*") && password.matches(".*[0-9].*");
    }

    // Time helpers
    public static String formatTimestamp(Instant instant) {
        if (instant == null) return "N/A";
        LocalDateTime dt = LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Ho_Chi_Minh"));
        return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    // Security helpers
    public static String generateOTP(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) otp.append(random.nextInt(10));
        return otp.toString();
    }

    public static String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest.digest(token.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }

    // Privacy helpers
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "Invalid Email";
        String[] parts = email.split("@");
        String prefix = parts[0].length() > 2 ? parts[0].substring(0, 2) + "****" : parts[0].charAt(0) + "****";
        return prefix + "@" + parts[1];
    }

    // URL encode
    public static String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Failed to URL encode: " + value, e);
        }
    }
}
