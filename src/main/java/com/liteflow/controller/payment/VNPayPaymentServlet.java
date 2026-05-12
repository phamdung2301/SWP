package com.liteflow.controller.payment;

import com.liteflow.service.payment.VNPayService;
import com.liteflow.util.VNPayUtil;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;
import java.util.UUID;

/**
 * Servlet for handling VNPay payment integration.
 * 
 * Endpoints:
 * - POST /api/payment/vnpay/create - Create payment URL
 * - GET  /api/payment/vnpay/return - Handle return URL callback
 * - POST /api/payment/vnpay/ipn    - Handle IPN callback
 */
@WebServlet("/api/payment/vnpay/*")
public class VNPayPaymentServlet extends HttpServlet {
    
    private VNPayService vnpayService;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        vnpayService = new VNPayService();
        gson = new Gson();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null) {
            sendErrorResponse(response, 400, "Invalid path");
            return;
        }
        
        if (pathInfo.equals("/create")) {
            handleCreatePayment(request, response);
        } else if (pathInfo.equals("/ipn")) {
            handleIPN(request, response);
        } else {
            sendErrorResponse(response, 404, "Endpoint not found: " + pathInfo);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null) {
            sendErrorResponse(response, 400, "Invalid path");
            return;
        }
        
        if (pathInfo.equals("/return")) {
            handleReturn(request, response);
        } else {
            sendErrorResponse(response, 404, "Endpoint not found: " + pathInfo);
        }
    }
    
    /**
     * Handle payment creation request.
     * POST /api/payment/vnpay/create
     * 
     * Request body:
     * {
     *   "sessionId": "uuid",
     *   "orderId": "uuid" (optional),
     *   "amount": 100000.00,
     *   "orderInfo": "Thanh toan don hang"
     * }
     */
    private void handleCreatePayment(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        PrintWriter out = response.getWriter();
        
        try {
            // Read request body
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
            String requestBody = sb.toString();
            System.out.println("📥 VNPay create payment request: " + requestBody);
            
            // Parse JSON
            Map<String, Object> requestData = gson.fromJson(requestBody, Map.class);
            if (requestData == null) {
                sendErrorResponse(response, 400, "Invalid request body");
                return;
            }
            
            // Get parameters
            String sessionIdStr = (String) requestData.get("sessionId");
            String tableIdStr = (String) requestData.get("tableId");
            String orderIdStr = (String) requestData.get("orderId");
            Object amountObj = requestData.get("amount");
            String orderInfo = (String) requestData.get("orderInfo");
            
            // Validate: either sessionId or tableId must be provided
            if ((sessionIdStr == null || sessionIdStr.isEmpty()) && 
                (tableIdStr == null || tableIdStr.isEmpty())) {
                sendErrorResponse(response, 400, "Either sessionId or tableId is required");
                return;
            }
            
            UUID sessionId = null;
            if (sessionIdStr != null && !sessionIdStr.isEmpty()) {
                try {
                    sessionId = UUID.fromString(sessionIdStr);
                } catch (IllegalArgumentException e) {
                    sendErrorResponse(response, 400, "Invalid sessionId format");
                    return;
                }
            }
            
            UUID tableId = null;
            if (tableIdStr != null && !tableIdStr.isEmpty()) {
                try {
                    tableId = UUID.fromString(tableIdStr);
                } catch (IllegalArgumentException e) {
                    sendErrorResponse(response, 400, "Invalid tableId format");
                    return;
                }
            }
            
            // Validate amount
            BigDecimal amount;
            if (amountObj == null) {
                sendErrorResponse(response, 400, "amount is required");
                return;
            }
            
            if (amountObj instanceof Number) {
                amount = BigDecimal.valueOf(((Number) amountObj).doubleValue());
            } else if (amountObj instanceof String) {
                amount = new BigDecimal((String) amountObj);
            } else {
                sendErrorResponse(response, 400, "Invalid amount format");
                return;
            }
            
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                sendErrorResponse(response, 400, "Amount must be greater than 0");
                return;
            }
            
            // Parse orderId (optional)
            UUID orderId = null;
            if (orderIdStr != null && !orderIdStr.isEmpty()) {
                try {
                    orderId = UUID.fromString(orderIdStr);
                } catch (IllegalArgumentException e) {
                    sendErrorResponse(response, 400, "Invalid orderId format");
                    return;
                }
            }
            
            // Get user ID from session
            UUID userId = getUserIdFromSession(request);
            
            // Get IP address (will convert IPv6 to IPv4 if needed)
            String ipAddress = VNPayUtil.getIpAddress(request);
            System.out.println("🌐 Client IP address: " + ipAddress);
            
            // Default order info
            if (orderInfo == null || orderInfo.isEmpty()) {
                if (tableId != null) {
                    orderInfo = "Thanh toan don hang - Table: " + tableId.toString();
                } else if (sessionId != null) {
                    orderInfo = "Thanh toan don hang - Session: " + sessionId.toString();
                } else {
                    orderInfo = "Thanh toan don hang";
                }
            }
            
            // Create payment request
            Map<String, Object> result = vnpayService.createPaymentRequest(
                    sessionId,
                    tableId,
                    orderId,
                    amount,
                    ipAddress,
                    userId,
                    orderInfo
            );
            
            // Send response
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(result));
            out.flush();
            
            System.out.println("✅ Created VNPay payment URL: " + result.get("transactionId"));
            System.out.println("✅ Payment URL: " + result.get("paymentUrl"));
            
        } catch (RuntimeException e) {
            System.err.println("❌ RuntimeException creating VNPay payment: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, 400, "Error creating payment: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Exception creating VNPay payment: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, 500, "Error creating payment: " + e.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
    
    /**
     * Handle return URL callback from VNPay.
     * GET /api/payment/vnpay/return
     * 
     * VNPay will redirect user to this URL with query parameters.
     * CRITICAL: Must collect ALL parameters from VNPay for hash validation.
     */
    private void handleReturn(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        try {
            // Get all parameters from request (both query string and form data)
            // VNPay sends parameters via GET query string
            Map<String, String> params = getAllParameters(request);
            
            // Get raw query string BEFORE servlet container decoding
            // This is critical for hash validation - VNPay calculates hash from URL-encoded values
            String rawQueryString = request.getQueryString();
            
            System.out.println("═══════════════════════════════════════════════════════");
            System.out.println("📥 VNPay Return Callback");
            System.out.println("═══════════════════════════════════════════════════════");
            System.out.println("📋 Total params received: " + params.size());
            System.out.println("📋 Params: " + params);
            System.out.println("📋 Raw query string: " + (rawQueryString != null ? rawQueryString.substring(0, Math.min(500, rawQueryString.length())) : "null"));
            System.out.println("═══════════════════════════════════════════════════════");
            
            // Process return callback (will validate hash internally using raw query string)
            Map<String, Object> result = vnpayService.processReturnCallback(params, rawQueryString);
            
            // Redirect to payment result page
            String transactionId = (String) result.get("transactionId");
            String status = (String) result.get("status");
            String responseCode = (String) result.get("responseCode");
            String message = (String) result.get("message");
            Boolean success = (Boolean) result.get("success");
            
            // Build redirect URL
            String redirectUrl = request.getContextPath() + "/payment/payment-result.jsp";
            
            // Only add transactionId if it exists
            if (transactionId != null && !transactionId.isEmpty()) {
                redirectUrl += "?transactionId=" + java.net.URLEncoder.encode(transactionId, "UTF-8");
            } else {
                redirectUrl += "?transactionId=";
            }
            
            // Add status
            if (status != null && !status.isEmpty()) {
                redirectUrl += "&status=" + java.net.URLEncoder.encode(status, "UTF-8");
            } else {
                redirectUrl += "&status=" + (success != null && success ? "Completed" : "Failed");
            }
            
            // Add responseCode if exists
            if (responseCode != null && !responseCode.isEmpty()) {
                redirectUrl += "&responseCode=" + java.net.URLEncoder.encode(responseCode, "UTF-8");
            } else {
                redirectUrl += "&responseCode=";
            }
            
            // Add success flag
            redirectUrl += "&success=" + (success != null ? success : false);
            
            // Add message if exists (important for error cases)
            if (message != null && !message.isEmpty()) {
                redirectUrl += "&message=" + java.net.URLEncoder.encode(message, "UTF-8");
            }
            
            response.sendRedirect(redirectUrl);
            
        } catch (Exception e) {
            System.err.println("❌ Error processing VNPay return callback: " + e.getMessage());
            e.printStackTrace();
            
            // Redirect to error page with detailed error message
            String redirectUrl = request.getContextPath() + "/payment/payment-result.jsp";
            try {
                String errorMessage = "Lỗi xử lý thanh toán: " + e.getMessage();
                // Limit error message length to avoid URL too long
                if (errorMessage.length() > 200) {
                    errorMessage = errorMessage.substring(0, 200) + "...";
                }
                redirectUrl += "?success=false&status=Failed&message=" + 
                    java.net.URLEncoder.encode(errorMessage, "UTF-8");
            } catch (Exception ex) {
                // Fallback if encoding fails
                redirectUrl += "?success=false&status=Failed&message=" + 
                    java.net.URLEncoder.encode("Lỗi xử lý thanh toán. Vui lòng thử lại hoặc liên hệ hỗ trợ.", "UTF-8");
            }
            response.sendRedirect(redirectUrl);
        }
    }
    
    /**
     * Handle IPN (Instant Payment Notification) callback from VNPay.
     * POST /api/payment/vnpay/ipn
     * 
     * VNPay will send server-to-server notification to this URL.
     * CRITICAL: Must collect ALL parameters from VNPay for hash validation.
     */
    private void handleIPN(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        PrintWriter out = response.getWriter();
        
        try {
            // Get all parameters from request (both query string and form data)
            // VNPay sends parameters via POST form data or query string
            Map<String, String> params = getAllParameters(request);
            
            System.out.println("═══════════════════════════════════════════════════════");
            System.out.println("📥 VNPay IPN Callback");
            System.out.println("═══════════════════════════════════════════════════════");
            System.out.println("📋 Total params received: " + params.size());
            System.out.println("📋 Params: " + params);
            System.out.println("═══════════════════════════════════════════════════════");
            
            // Process IPN callback (will validate hash internally)
            String result = vnpayService.processIPNCallback(params);
            
            // VNPay expects "OK" response for successful IPN processing
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            out.print(result);
            out.flush();
            
            System.out.println("✅ Processed VNPay IPN: " + result);
            
        } catch (Exception e) {
            System.err.println("❌ Error processing VNPay IPN: " + e.getMessage());
            e.printStackTrace();
            
            // Return error to VNPay
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            out.print("ERROR");
            out.flush();
        } finally {
            out.close();
        }
    }
    
    /**
     * Get all parameters from request.
     * Handles both GET (query string) and POST (form data) requests.
     * CRITICAL: Must collect ALL parameters for VNPay hash validation.
     * 
     * @param request HTTP request
     * @return Map of all parameters
     */
    private Map<String, String> getAllParameters(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("📥 Collecting Parameters from Request");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("📋 Request Method: " + request.getMethod());
        System.out.println("📋 Query String: " + (request.getQueryString() != null ? request.getQueryString() : "null"));
        System.out.println("📋 Content Type: " + request.getContentType());
        
        // Get parameters from request (works for both GET and POST)
        // Servlet container automatically decodes URL-encoded parameters
        Enumeration<String> paramNames = request.getParameterNames();
        int paramCount = 0;
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            
            // If multiple values, take the first one (VNPay typically sends single values)
            String paramValue = "";
            if (paramValues != null && paramValues.length > 0) {
                paramValue = paramValues[0];
                params.put(paramName, paramValue);
            } else {
                params.put(paramName, "");
            }
            
            paramCount++;
            // Log important VNPay parameters
            if (paramName.startsWith("vnp_")) {
                System.out.println("   ✅ VNPay param: " + paramName + " = " + 
                    (paramValue.length() > 100 ? paramValue.substring(0, 100) + "..." : paramValue));
            }
        }
        
        System.out.println("📋 Total params from request.getParameterNames(): " + paramCount);
        
        // Also check query string directly (in case parameters are in URL but not parsed)
        // This is a fallback mechanism
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            System.out.println("📋 Processing query string: " + queryString.substring(0, Math.min(500, queryString.length())));
            // Parse query string manually to ensure no parameters are missed
            String[] pairs = queryString.split("&");
            int queryParamCount = 0;
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > 0) {
                    String key = pair.substring(0, idx);
                    String value = idx < pair.length() - 1 ? pair.substring(idx + 1) : "";
                    try {
                        // URL decode the value (servlet container should do this, but do it again to be safe)
                        value = java.net.URLDecoder.decode(value, "UTF-8");
                        // Only add if not already in params (request.getParameter takes precedence)
                        if (!params.containsKey(key)) {
                            params.put(key, value);
                            queryParamCount++;
                            System.out.println("   ⚠️ Added param from query string: " + key + " = " + 
                                (value.length() > 100 ? value.substring(0, 100) + "..." : value));
                        }
                    } catch (Exception e) {
                        // If decode fails, use raw value
                        if (!params.containsKey(key)) {
                            params.put(key, value);
                            queryParamCount++;
                            System.err.println("   ⚠️ Warning: Could not decode param " + key + ", using raw value");
                        }
                    }
                }
            }
            System.out.println("📋 Additional params from query string: " + queryParamCount);
        }
        
        System.out.println("📋 Total params collected: " + params.size());
        System.out.println("═══════════════════════════════════════════════════════");
        
        return params;
    }
    
    /**
     * Get user ID from session.
     */
    private UUID getUserIdFromSession(HttpServletRequest request) {
        try {
            Object userIdObj = request.getSession().getAttribute("UserID");
            if (userIdObj instanceof UUID) {
                return (UUID) userIdObj;
            } else if (userIdObj instanceof String) {
                return UUID.fromString((String) userIdObj);
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not get user ID from session: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Send error response.
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(error));
        out.flush();
    }
}

