package com.liteflow.controller.cashier;

import com.liteflow.service.inventory.OrderService;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet("/api/order/table/*")
public class GetSessionOrdersServlet extends HttpServlet {
    
    private OrderService orderService;
    
    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        
        try {
            // Lấy tableId từ URL path: /api/order/table/{tableId}
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.length() <= 1) {
                sendErrorResponse(response, out, gson, 400, "Table ID không được rỗng");
                return;
            }
            
            String tableIdStr = pathInfo.substring(1); // Bỏ dấu / đầu tiên
            
            // Convert to UUID
            UUID tableId;
            try {
                tableId = UUID.fromString(tableIdStr);
            } catch (IllegalArgumentException e) {
                sendErrorResponse(response, out, gson, 400, "Table ID không hợp lệ: " + tableIdStr);
                return;
            }
            
            // Lấy orders của bàn
            List<Map<String, Object>> orders = orderService.getOrdersByTable(tableId);
            
            // Trả về response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("tableId", tableIdStr);
            responseData.put("orders", orders);
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(responseData));
            
            System.out.println("✅ Đã lấy " + orders.size() + " items cho bàn " + tableId);
            
        } catch (IllegalArgumentException e) {
            System.err.println("⚠️ Lỗi validation: " + e.getMessage());
            sendErrorResponse(response, out, gson, 400, e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy orders: " + e.getMessage());
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

