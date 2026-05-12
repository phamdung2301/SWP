package com.liteflow.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class để test và đối chiếu cách mã hóa hash cho VNPay
 */
public class VNPayHash {
    
    // Test HMAC SHA512
    public static void testHMACSHA512() {
        System.out.println("=======================================================");
        System.out.println("TEST HMAC SHA512 Hash Calculation");
        System.out.println("=======================================================");
        
        // Test case 1
        String hashSecret = "PHM5SXSA6J9U9ZCMAJPITW1Q9SW745OM";
        String hashInput = "vnp_Amount=11000000&vnp_Command=pay&vnp_CreateDate=20251111223017&vnp_CurrCode=VND&vnp_IpAddr=127.0.0.1&vnp_Locale=vn&vnp_OrderInfo=Thanh toan don hang - LT-06 - HD 2 - Ban LT-06&vnp_OrderType=other&vnp_ReturnUrl=http://localhost:8080/LiteFlow/api/payment/vnpay/return&vnp_TmnCode=KGAZB8XO&vnp_TxnRef=13bf7a78f35a46fe8650e30682537025&vnp_Version=2.1.0";
        String expectedHash = "ec9c883dfa02b5b9274b0d914435ccfac5c6e48efc4d9a6a22c49ae7f5dda576c2bcaabdfd0f64d0bebbbe67d01570974fc9f690337b28d0ac4b71c96d12763e";
        
        String calculatedHash = calculateHMACSHA512(hashSecret, hashInput);
        
        System.out.println("Test Case 1: Real transaction data");
        System.out.println("Expected Hash: " + expectedHash);
        System.out.println("Calculated Hash: " + calculatedHash);
        System.out.println("Match: " + (calculatedHash.equals(expectedHash) ? "YES" : "NO"));
        System.out.println("-------------------------------------------------------");
        
        // Test case 2
        String hashSecret2 = "SECRETKEY";
        String hashInput2 = "vnp_Amount=1000000&vnp_Command=pay&vnp_TmnCode=TEST123";
        String calculatedHash2 = calculateHMACSHA512(hashSecret2, hashInput2);
        
        System.out.println("Test Case 2: Simple test");
        System.out.println("Calculated Hash: " + calculatedHash2);
        System.out.println("Hash Length: " + calculatedHash2.length());
        System.out.println("-------------------------------------------------------");
        
        // Test case 3
        String hashSecret3 = "SECRETKEY";
        String hashInput3 = "vnp_Amount=1000000&vnp_Command=pay&vnp_OrderInfo=Thanh toan don hang&vnp_TmnCode=TEST123";
        String calculatedHash3 = calculateHMACSHA512(hashSecret3, hashInput3);
        
        System.out.println("Test Case 3: With spaces in OrderInfo");
        System.out.println("Calculated Hash: " + calculatedHash3);
        System.out.println("=======================================================");
    }
    
    // Hàm tính HMAC SHA512
    public static String calculateHMACSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            
            byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error calculating HMAC SHA512: " + e.getMessage(), e);
        }
    }
    
    // Hàm SHA256
    public static String calculateSHA256(String hashSecret, String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = hashSecret + data;
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error calculating SHA256: " + e.getMessage(), e);
        }
    }
    
    // Main
    public static void main(String[] args) {
        System.out.println("Starting VNPay Hash Tests...");
        testHMACSHA512();
        System.out.println("Tests completed!");
    }
}
