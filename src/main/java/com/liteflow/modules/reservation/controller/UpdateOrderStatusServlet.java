package com.liteflow.modules.reservation.controller;

import com.liteflow.modules.inventory.service.OrderService;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet("/api/order/status")
public class UpdateOrderStatusServlet extends HttpServlet {
    
    private OrderService orderService;
    
    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        
        try {
            // Đọc JSON từ request body
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
            String requestBody = sb.toString();
            System.out.println("📥 Nhận request cập nhật trạng thái: " + requestBody);
            
            // Parse JSON
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = gson.fromJson(requestBody, Map.class);
            
            if (requestData == null) {
                sendErrorResponse(response, out, gson, 400, "Request body không hợp lệ");
                return;
            }
            
            String orderIdStr = (String) requestData.get("orderId");
            String status = (String) requestData.get("status");
            
            if (orderIdStr == null || orderIdStr.isEmpty()) {
                sendErrorResponse(response, out, gson, 400, "Order ID không được rỗng");
                return;
            }
            
            if (status == null || status.isEmpty()) {
                sendErrorResponse(response, out, gson, 400, "Status không được rỗng");
                return;
            }
            
            // Convert orderId to UUID
            UUID orderId;
            try {
                orderId = UUID.fromString(orderIdStr);
            } catch (IllegalArgumentException e) {
                sendErrorResponse(response, out, gson, 400, "Order ID không hợp lệ: " + orderIdStr);
                return;
            }
            
            // Cập nhật trạng thái
            boolean success = orderService.updateOrderStatus(orderId, status);
            
            if (!success) {
                sendErrorResponse(response, out, gson, 404, "Không tìm thấy order");
                return;
            }
            
            // Trả về response thành công
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "Đã cập nhật trạng thái thành công!");
            responseData.put("orderId", orderIdStr);
            responseData.put("newStatus", status);
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(responseData));
            
            System.out.println("✅ Đã cập nhật trạng thái order " + orderId + " thành " + status);
            
        } catch (IllegalArgumentException e) {
            System.err.println("⚠️ Lỗi validation: " + e.getMessage());
            sendErrorResponse(response, out, gson, 400, e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi cập nhật trạng thái: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, out, gson, 500, "Lỗi server: " + e.getMessage());
        } finally {
            out.flush();
        }
    }
    
    /**
     * Gửi error response
     */
    private void sendErrorResponse(HttpServletResponse response, PrintWriter out, 
                                   Gson gson, int statusCode, String message) {
        response.setStatus(statusCode);
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        out.print(gson.toJson(errorResponse));
    }
}

