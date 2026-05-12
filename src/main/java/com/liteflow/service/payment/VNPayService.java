package com.liteflow.service.payment;

import com.liteflow.dao.BaseDAO;
import com.liteflow.model.inventory.*;
import com.liteflow.model.auth.User;
import com.liteflow.util.VNPayUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service class for VNPay payment processing.
 * Handles payment request creation, callback processing, and status updates.
 */
public class VNPayService {
    
    private static final String PROPERTIES_FILE = "vnpay.properties";
    private String tmnCode;
    private String hashSecret;
    private String vnpayUrl;
    private String returnUrl;
    private String ipnUrl;
    
    public VNPayService() {
        loadProperties();
    }
    
    /**
     * Load VNPay configuration from properties file.
     */
    private void loadProperties() {
        try {
            Properties props = new Properties();
            InputStream is = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE);
            if (is == null) {
                throw new RuntimeException("VNPay properties file not found: " + PROPERTIES_FILE);
            }
            props.load(is);
            is.close();
            
            tmnCode = props.getProperty("vnpay.tmncode");
            hashSecret = props.getProperty("vnpay.hashsecret");
            vnpayUrl = props.getProperty("vnpay.url");
            returnUrl = props.getProperty("vnpay.returnurl");
            ipnUrl = props.getProperty("vnpay.ipnurl");
            
            // Trim whitespace from configuration values
            if (tmnCode != null) {
                tmnCode = tmnCode.trim();
            }
            if (hashSecret != null) {
                hashSecret = hashSecret.trim();
            }
            if (vnpayUrl != null) {
                vnpayUrl = vnpayUrl.trim();
            }
            if (returnUrl != null) {
                returnUrl = returnUrl.trim();
                // Remove trailing slash if exists
                if (returnUrl.endsWith("/")) {
                    returnUrl = returnUrl.substring(0, returnUrl.length() - 1);
                }
            }
            if (ipnUrl != null) {
                ipnUrl = ipnUrl.trim();
                // Remove trailing slash if exists
                if (ipnUrl.endsWith("/")) {
                    ipnUrl = ipnUrl.substring(0, ipnUrl.length() - 1);
                }
            }
            
            if (tmnCode == null || tmnCode.isEmpty() || 
                hashSecret == null || hashSecret.isEmpty() || 
                vnpayUrl == null || vnpayUrl.isEmpty()) {
                throw new RuntimeException("VNPay configuration is incomplete in properties file");
            }
            
            System.out.println("✅ VNPay configuration loaded:");
            System.out.println("   TMN Code: " + tmnCode);
            System.out.println("   Hash Secret: " + (hashSecret.length() > 0 ? hashSecret.substring(0, Math.min(10, hashSecret.length())) + "..." : "EMPTY"));
            System.out.println("   VNPay URL: " + vnpayUrl);
            System.out.println("   Return URL: " + returnUrl);
            System.out.println("   IPN URL: " + ipnUrl);
            
        } catch (Exception e) {
            System.err.println("Error loading VNPay properties: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to load VNPay configuration", e);
        }
    }
    
    /**
     * Create VNPay payment request.
     * Creates a PaymentTransaction with "Pending" status and returns payment URL.
     * 
     * @param sessionId Table session ID (optional, will find/create session if null and tableId is provided)
     * @param tableId Table ID (optional, used if sessionId is null to find/create session)
     * @param orderId Order ID (optional, can be null for full session payment)
     * @param amount Payment amount
     * @param ipAddress Client IP address
     * @param userId User ID who initiated the payment
     * @param orderInfo Order information description
     * @return Map containing paymentUrl and transactionId
     */
    public Map<String, Object> createPaymentRequest(
            UUID sessionId,
            UUID tableId,
            UUID orderId,
            BigDecimal amount,
            String ipAddress,
            UUID userId,
            String orderInfo) {
        
        EntityManager em = BaseDAO.emf.createEntityManager();
        
        try {
            em.getTransaction().begin();
            
            // 1. Find or create session
            TableSession session = null;
            
            if (sessionId != null) {
                // Use provided sessionId
                session = em.find(TableSession.class, sessionId);
                if (session == null) {
                    throw new RuntimeException("Session not found: " + sessionId);
                }
            } else if (tableId != null) {
                // Find or create session for table
                session = findOrCreateActiveSession(em, tableId, userId, orderInfo);
            } else {
                throw new RuntimeException("Either sessionId or tableId must be provided");
            }
            
            // 2. Validate order exists (if provided)
            Order order = null;
            if (orderId != null) {
                order = em.find(Order.class, orderId);
                if (order == null) {
                    throw new RuntimeException("Order not found: " + orderId);
                }
            }
            
            // 3. Create PaymentTransaction with "Pending" status
            PaymentTransaction transaction = new PaymentTransaction();
            UUID transactionId = UUID.randomUUID();
            transaction.setTransactionId(transactionId);
            transaction.setSession(session);
            transaction.setOrder(order);
            transaction.setAmount(amount);
            transaction.setPaymentMethod("VNPay");
            transaction.setPaymentStatus("Pending");
            transaction.setProcessedAt(LocalDateTime.now());
            
            if (userId != null) {
                User user = em.find(User.class, userId);
                if (user != null) {
                    transaction.setProcessedBy(user);
                }
            }
            
            // Set order info as notes
            if (orderInfo != null && !orderInfo.isEmpty()) {
                transaction.setNotes(orderInfo);
            }
            
            em.persist(transaction);
            em.flush();
            
            // 4. Create payment URL
            // Build return URL dynamically if needed (for production, use configured URL)
            String finalReturnUrl = returnUrl;
            // Note: In production, returnUrl should be a full URL like https://yourdomain.com/LiteFlow/api/payment/vnpay/return
            
            String paymentUrl = VNPayUtil.createPaymentUrl(
                    amount,
                    orderInfo != null ? orderInfo : "Thanh toan don hang",
                    ipAddress,
                    finalReturnUrl,
                    transactionId,
                    tmnCode,
                    hashSecret,
                    vnpayUrl
            );
            
            em.getTransaction().commit();
            
            // 5. Return result
            Map<String, Object> result = new HashMap<>();
            result.put("paymentUrl", paymentUrl);
            result.put("transactionId", transactionId.toString());
            result.put("success", true);
            result.put("sessionId", session.getSessionId().toString());
            result.put("amount", amount.toString());
            
            System.out.println("✅ Created VNPay payment request: " + transactionId);
            System.out.println("✅ Payment URL length: " + paymentUrl.length());
            System.out.println("✅ Session ID: " + session.getSessionId());
            return result;
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Error creating VNPay payment request: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create VNPay payment request: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Process Return URL callback from VNPay.
     * This is called when user is redirected back from VNPay.
     * 
     * @param params All parameters from VNPay callback (decoded by servlet container)
     * @param rawQueryString Raw query string from request (before servlet container decoding)
     * @return Map containing transaction status and information
     */
    public Map<String, Object> processReturnCallback(Map<String, String> params, String rawQueryString) {
        // Check if params is empty or null
        if (params == null || params.isEmpty()) {
            System.err.println("❌ No parameters received in return callback");
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("status", "Failed");
            result.put("message", "Không nhận được thông tin từ hệ thống thanh toán. Vui lòng kiểm tra lại hoặc liên hệ hỗ trợ.");
            return result;
        }
        
        // Validate secure hash using raw query string to preserve encoding
        // VNPay calculates hash from URL-encoded values in query string
        boolean hashValid = false;
        if (rawQueryString != null && !rawQueryString.isEmpty()) {
            // Use raw query string for hash validation (preserves + signs, etc.)
            hashValid = VNPayUtil.validateSecureHashFromRawQuery(rawQueryString, hashSecret);
        } else {
            // Fallback to params-based validation (may not work correctly for special characters)
            hashValid = VNPayUtil.validateSecureHash(params, hashSecret);
        }
        
        if (!hashValid) {
            System.err.println("❌ Invalid secure hash in return callback");
            System.err.println("❌ Received params: " + params);
            System.err.println("❌ Raw query string: " + rawQueryString);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("status", "Failed");
            result.put("message", "Không thể xác thực giao dịch. Mã bảo mật không hợp lệ. Vui lòng thử lại hoặc liên hệ hỗ trợ.");
            return result;
        }
        
        // Parse transaction ID
        String vnpTxnRef = params.get("vnp_TxnRef");
        if (vnpTxnRef == null || vnpTxnRef.isEmpty()) {
            System.err.println("❌ Missing transaction reference in callback");
            System.err.println("❌ Received params: " + params);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("status", "Failed");
            result.put("message", "Thiếu thông tin giao dịch. Vui lòng thử lại.");
            return result;
        }
        
        UUID transactionId = VNPayUtil.parseTransactionId(vnpTxnRef);
        if (transactionId == null) {
            System.err.println("❌ Invalid transaction reference: " + vnpTxnRef);
            System.err.println("❌ Received params: " + params);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("status", "Failed");
            result.put("message", "Mã giao dịch không hợp lệ: " + vnpTxnRef);
            return result;
        }
        
        // Get response code and transaction number
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");
        String amount = params.get("vnp_Amount");
        
        // Update payment status
        return updatePaymentStatus(transactionId, responseCode, transactionNo, amount, false);
    }
    
    /**
     * Process IPN (Instant Payment Notification) callback from VNPay.
     * This is called server-to-server by VNPay.
     * 
     * @param params All parameters from VNPay IPN
     * @return "OK" if successful, error message otherwise
     */
    public String processIPNCallback(Map<String, String> params) {
        // Validate secure hash
        if (!VNPayUtil.validateSecureHash(params, hashSecret)) {
            System.err.println("❌ Invalid secure hash in IPN callback");
            return "INVALID_HASH";
        }
        
        // Parse transaction ID
        String vnpTxnRef = params.get("vnp_TxnRef");
        UUID transactionId = VNPayUtil.parseTransactionId(vnpTxnRef);
        if (transactionId == null) {
            System.err.println("❌ Invalid transaction reference in IPN: " + vnpTxnRef);
            return "INVALID_TRANSACTION_REF";
        }
        
        // Get response code and transaction number
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");
        String amount = params.get("vnp_Amount");
        
        // Update payment status (IPN is authoritative)
        Map<String, Object> result = updatePaymentStatus(transactionId, responseCode, transactionNo, amount, true);
        
        if (result.get("success").equals(true)) {
            return "OK";
        } else {
            return "FAILED";
        }
    }
    
    /**
     * Update payment transaction status.
     * 
     * @param transactionId Transaction ID
     * @param responseCode VNPay response code ("00" = success)
     * @param transactionNo VNPay transaction number
     * @param amountStr Amount in cents (from VNPay)
     * @param isIPN Whether this is from IPN (more authoritative)
     * @return Map containing update result
     */
    private Map<String, Object> updatePaymentStatus(
            UUID transactionId,
            String responseCode,
            String transactionNo,
            String amountStr,
            boolean isIPN) {
        
        EntityManager em = BaseDAO.emf.createEntityManager();
        
        try {
            em.getTransaction().begin();
            
            // Find transaction
            PaymentTransaction transaction = em.find(PaymentTransaction.class, transactionId);
            if (transaction == null) {
                System.err.println("❌ Transaction not found: " + transactionId);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("status", "Failed");
                result.put("transactionId", transactionId.toString());
                result.put("message", "Không tìm thấy giao dịch với mã: " + transactionId);
                return result;
            }
            
            // Validate amount (only if IPN)
            if (isIPN && amountStr != null) {
                BigDecimal expectedAmount = transaction.getAmount();
                BigDecimal receivedAmount = VNPayUtil.parseAmount(amountStr);
                
                // Allow small difference due to rounding
                BigDecimal difference = expectedAmount.subtract(receivedAmount).abs();
                if (difference.compareTo(new BigDecimal("0.01")) > 0) {
                    System.err.println("❌ Amount mismatch. Expected: " + expectedAmount + ", Received: " + receivedAmount);
                    transaction.setPaymentStatus("Failed");
                    transaction.setNotes("Amount mismatch");
                    em.merge(transaction);
                    em.getTransaction().commit();
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", false);
                    result.put("status", "Failed");
                    result.put("transactionId", transactionId.toString());
                    result.put("responseCode", responseCode);
                    result.put("message", "Số tiền thanh toán không khớp. Vui lòng liên hệ hỗ trợ.");
                    return result;
                }
            }
            
            // Update transaction
            transaction.setVnpayResponseCode(responseCode);
            if (transactionNo != null && !transactionNo.isEmpty()) {
                transaction.setVnpayTransactionNo(transactionNo);
            }
            
            // Determine payment status
            if (VNPayUtil.isSuccessResponse(responseCode)) {
                transaction.setPaymentStatus("Completed");
                transaction.setProcessedAt(LocalDateTime.now());
                
                // Update session and orders if payment is successful
                updateSessionAndOrders(em, transaction);
                
            } else if ("07".equals(responseCode)) {
                // Transaction suspected of fraud
                transaction.setPaymentStatus("Failed");
                transaction.setNotes("Transaction suspected of fraud");
            } else if ("09".equals(responseCode)) {
                // Card/Account is not registered for Internet Banking
                transaction.setPaymentStatus("Failed");
                transaction.setNotes("Card/Account not registered for Internet Banking");
            } else if ("10".equals(responseCode)) {
                // Amount verification failed
                transaction.setPaymentStatus("Failed");
                transaction.setNotes("Amount verification failed");
            } else if ("11".equals(responseCode)) {
                // Transaction failed
                transaction.setPaymentStatus("Failed");
                transaction.setNotes("Transaction failed");
            } else if ("12".equals(responseCode)) {
                // Transaction timeout
                transaction.setPaymentStatus("Failed");
                transaction.setNotes("Transaction timeout");
            } else if ("51".equals(responseCode)) {
                // Insufficient balance
                transaction.setPaymentStatus("Failed");
                transaction.setNotes("Insufficient balance");
            } else if ("65".equals(responseCode)) {
                // Exceed daily transaction limit
                transaction.setPaymentStatus("Failed");
                transaction.setNotes("Exceed daily transaction limit");
            } else if ("75".equals(responseCode)) {
                // Bank maintenance
                transaction.setPaymentStatus("Failed");
                transaction.setNotes("Bank maintenance");
            } else {
                // Other errors
                transaction.setPaymentStatus("Failed");
                transaction.setNotes("Payment failed: " + responseCode);
            }
            
            em.merge(transaction);
            em.getTransaction().commit();
            
            System.out.println("✅ Updated payment transaction status: " + transactionId + ", Status: " + transaction.getPaymentStatus());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("transactionId", transactionId.toString());
            result.put("status", transaction.getPaymentStatus());
            result.put("responseCode", responseCode);
            return result;
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Error updating payment status: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("status", "Failed");
            result.put("transactionId", transactionId != null ? transactionId.toString() : null);
            result.put("responseCode", responseCode);
            result.put("message", "Lỗi khi cập nhật trạng thái thanh toán. Vui lòng liên hệ hỗ trợ.");
            return result;
        } finally {
            em.close();
        }
    }
    
    /**
     * Update session and orders after successful payment.
     * 
     * @param em EntityManager
     * @param transaction PaymentTransaction
     */
    private void updateSessionAndOrders(EntityManager em, PaymentTransaction transaction) {
        try {
            TableSession session = transaction.getSession();
            if (session != null) {
                // Update session payment status
                session.setPaymentStatus("Paid");
                session.setPaymentMethod("VNPay");
                session.setCheckOutTime(LocalDateTime.now());
                session.setStatus("Completed");
                
                // Update total amount if not set
                if (session.getTotalAmount() == null || session.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
                    session.setTotalAmount(transaction.getAmount());
                }
                
                em.merge(session);
                
                // Update all orders in session
                String updateOrdersQuery = "UPDATE Order o SET o.status = 'Served', o.paymentStatus = 'Paid', o.paymentMethod = 'VNPay' WHERE o.session.sessionId = :sessionId";
                Query updateQuery = em.createQuery(updateOrdersQuery);
                updateQuery.setParameter("sessionId", session.getSessionId());
                int updatedOrders = updateQuery.executeUpdate();
                System.out.println("✅ Updated " + updatedOrders + " orders to Paid status");
                
                // Update table status if session has a table
                if (session.getTable() != null) {
                    Table table = session.getTable();
                    table.setStatus("Available");
                    em.merge(table);
                    System.out.println("✅ Updated table status to Available");
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Warning: Error updating session and orders: " + e.getMessage());
            e.printStackTrace();
            // Don't fail the transaction if session update fails
        }
    }
    
    /**
     * Find or create active session for table.
     * Similar to OrderDAO.findOrCreateActiveSession.
     */
    private TableSession findOrCreateActiveSession(EntityManager em, UUID tableId, UUID userId, String invoiceName) {
        // Handle special tables (tableId can be null for takeaway/delivery)
        if (tableId == null) {
            TableSession session = new TableSession();
            session.setSessionId(UUID.randomUUID());
            session.setTable(null);
            session.setCheckInTime(LocalDateTime.now());
            session.setStatus("Active");
            session.setPaymentStatus("Unpaid");
            
            if (invoiceName != null && !invoiceName.isEmpty()) {
                session.setInvoiceName(invoiceName);
            }
            
            if (userId != null) {
                User user = em.find(User.class, userId);
                if (user != null) {
                    session.setCreatedBy(user);
                }
            }
            
            em.persist(session);
            return session;
        }
        
        // Find active session for regular table
        Query query = em.createQuery(
            "SELECT s FROM TableSession s WHERE s.table.tableId = :tableId AND s.status = 'Active'"
        );
        query.setParameter("tableId", tableId);
        
        @SuppressWarnings("unchecked")
        List<TableSession> sessions = query.getResultList();
        
        if (!sessions.isEmpty()) {
            TableSession existingSession = sessions.get(0);
            if (invoiceName != null && !invoiceName.isEmpty()) {
                existingSession.setInvoiceName(invoiceName);
                em.merge(existingSession);
            }
            
            // Ensure table status is "Occupied"
            Table table = existingSession.getTable();
            if (table != null && !"Occupied".equals(table.getStatus())) {
                table.setStatus("Occupied");
                em.merge(table);
            }
            
            return existingSession;
        }
        
        // Create new session for regular table
        Table table = em.find(Table.class, tableId);
        if (table == null) {
            throw new RuntimeException("Table not found: " + tableId);
        }
        
        TableSession session = new TableSession();
        session.setSessionId(UUID.randomUUID());
        session.setTable(table);
        session.setCheckInTime(LocalDateTime.now());
        session.setStatus("Active");
        session.setPaymentStatus("Unpaid");
        
        if (invoiceName != null && !invoiceName.isEmpty()) {
            session.setInvoiceName(invoiceName);
        }
        
        if (userId != null) {
            User user = em.find(User.class, userId);
            if (user != null) {
                session.setCreatedBy(user);
            }
        }
        
        em.persist(session);
        
        // Update table status to Occupied
        table.setStatus("Occupied");
        em.merge(table);
        
        return session;
    }
    
    /**
     * Get VNPay configuration (for testing/debugging).
     */
    public Map<String, String> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("tmnCode", tmnCode);
        config.put("vnpayUrl", vnpayUrl);
        config.put("returnUrl", returnUrl);
        config.put("ipnUrl", ipnUrl);
        return config;
    }
}

