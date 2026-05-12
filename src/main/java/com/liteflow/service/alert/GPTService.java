package com.liteflow.service.alert;

import com.liteflow.dao.alert.GPTInteractionDAO;
import com.liteflow.model.alert.GPTInteraction;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Service for GPT API integration
 * Uses gpt-4o-mini for cost-effective AI summaries
 */
public class GPTService {
    
    private static final String GPT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String GPT_MODEL = "gpt-4o-mini";
    private static final int MAX_TOKENS = 500;
    private static final double TEMPERATURE = 0.7;
    
    private final GPTInteractionDAO gptDAO;
    private String apiKey;
    
    public GPTService() {
        this.gptDAO = new GPTInteractionDAO();
      
        this.apiKey = System.getenv("OPENAI_API_KEY");
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            System.err.println("⚠️ OPENAI_API_KEY not set. GPT features will be disabled.");
        }
    }
    
    public GPTService(String apiKey) {
        this.gptDAO = new GPTInteractionDAO();
        this.apiKey = apiKey;
    }
    
    /**
     * Check if GPT is available
     */
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isEmpty();
    }
    
    /**
     * Generate daily summary from revenue data
     */
    public String generateDailySummary(JSONObject revenueData) {
        if (!isAvailable()) {
            return "GPT API not configured";
        }
        
        String systemPrompt = "Bạn là trợ lý AI của LiteFlow - hệ thống quản lý quán cafe. " +
                            "Nhiệm vụ của bạn là tóm tắt dữ liệu doanh thu hàng ngày một cách ngắn gọn, " +
                            "dễ hiểu và chuyên nghiệp. Trả lời bằng tiếng Việt.";
        
        String userPrompt = "Hãy tóm tắt doanh thu hôm nay:\n" + revenueData.toString(2);
        
        return callGPT("DAILY_SUMMARY", systemPrompt, userPrompt, null);
    }
    
    /**
     * Generate alert summary
     */
    public String generateAlertSummary(String alertType, JSONObject contextData) {
        if (!isAvailable()) {
            return "GPT API not configured";
        }
        
        String systemPrompt = "Bạn là trợ lý AI của LiteFlow. Nhiệm vụ của bạn là phân tích " +
                            "và đưa ra cảnh báo ngắn gọn, rõ ràng về tình huống. " +
                            "Đề xuất hành động cụ thể nếu cần. Trả lời bằng tiếng Việt, tối đa 3-4 câu.";
        
        String userPrompt = buildAlertPrompt(alertType, contextData);
        
        return callGPT("ALERT_SUMMARY", systemPrompt, userPrompt, null);
    }
    
    /**
     * Generate inventory recommendation
     */
    public String generateInventoryRecommendation(JSONObject inventoryData) {
        if (!isAvailable()) {
            return "GPT API not configured";
        }
        
        String systemPrompt = "Bạn là chuyên gia quản lý kho hàng. Phân tích dữ liệu tồn kho " +
                            "và đưa ra khuyến nghị về việc đặt hàng. Trả lời bằng tiếng Việt, " +
                            "ngắn gọn và có số liệu cụ thể.";
        
        String userPrompt = "Phân tích tình trạng tồn kho:\n" + inventoryData.toString(2);
        
        return callGPT("INVENTORY_RECOMMENDATION", systemPrompt, userPrompt, null);
    }
    
    /**
     * Generate procurement alert
     */
    public String generateProcurementAlert(String poId, double amount, int daysOverdue) {
        if (!isAvailable()) {
            return "GPT API not configured";
        }
        
        String systemPrompt = "Bạn là trợ lý quản lý mua hàng. Đưa ra cảnh báo và đề xuất " +
                            "hành động cho đơn đặt hàng. Trả lời bằng tiếng Việt, ngắn gọn.";
        
        String userPrompt = String.format(
            "Đơn đặt hàng %s với giá trị %,.0f VND đã quá hạn %d ngày. " +
            "Hãy đưa ra cảnh báo và đề xuất hành động.",
            poId, amount, daysOverdue
        );
        
        return callGPT("PROCUREMENT_ALERT", systemPrompt, userPrompt, null);
    }
    
    /**
     * Answer user question (chatbot)
     */
    public String answerQuestion(String question, JSONObject context, UUID userId) {
        if (!isAvailable()) {
            return "GPT API không khả dụng. Vui lòng liên hệ quản trị viên.";
        }
        
        String systemPrompt = "Bạn là LiteFlow Assistant - trợ lý AI chuyên nghiệp của hệ thống " +
                            "quản lý quán cafe LiteFlow. Bạn có thể trả lời câu hỏi về doanh thu, " +
                            "tồn kho, nhân viên, và các chức năng của hệ thống. " +
                            "Trả lời bằng tiếng Việt, lịch sự và chuyên nghiệp. " +
                            "Nếu không chắc chắn, hãy thành thật nói không biết.";
        
        String userPrompt = "Câu hỏi: " + question;
        if (context != null && context.length() > 0) {
            userPrompt += "\n\nDữ liệu tham khảo:\n" + context.toString(2);
        }
        
        return callGPT("CHATBOT", systemPrompt, userPrompt, userId);
    }
    
    /**
     * Core GPT API call
     */
    private String callGPT(String purpose, String systemPrompt, String userPrompt, UUID userId) {
        GPTInteraction interaction = new GPTInteraction(GPT_MODEL, purpose);
        interaction.setSystemPrompt(systemPrompt);
        interaction.setUserPrompt(userPrompt);
        if (userId != null) {
            interaction.setCreatedBy(userId);
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Build request
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", GPT_MODEL);
            requestBody.put("max_tokens", MAX_TOKENS);
            requestBody.put("temperature", TEMPERATURE);
            
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject()
                .put("role", "system")
                .put("content", systemPrompt));
            messages.put(new JSONObject()
                .put("role", "user")
                .put("content", userPrompt));
            requestBody.put("messages", messages);
            
            // Make HTTP request
            URL url = URI.create(GPT_API_URL).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);
            
            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Read response
            int responseCode = conn.getResponseCode();
            StringBuilder response = new StringBuilder();
            
            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }
                
                // Parse response
                JSONObject jsonResponse = new JSONObject(response.toString());
                String assistantMessage = jsonResponse
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
                
                // Extract token usage
                JSONObject usage = jsonResponse.getJSONObject("usage");
                int promptTokens = usage.getInt("prompt_tokens");
                int completionTokens = usage.getInt("completion_tokens");
                
                // Record success
                int responseTime = (int) (System.currentTimeMillis() - startTime);
                interaction.recordSuccess(assistantMessage, promptTokens, completionTokens, responseTime);
                gptDAO.insert(interaction);
                
                System.out.println("✅ GPT call successful - Tokens: " + (promptTokens + completionTokens) + 
                                 ", Cost: " + interaction.getFormattedCostVND());
                
                return assistantMessage;
                
            } else {
                // Read error response
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }
                
                String errorMsg = "GPT API error: " + responseCode + " - " + response.toString();
                interaction.recordFailure(errorMsg);
                gptDAO.insert(interaction);
                
                System.err.println("❌ " + errorMsg);
                
                if (responseCode == 429) {
                    return "Rate limit exceeded. Vui lòng thử lại sau.";
                } else if (responseCode == 401) {
                    return "API key không hợp lệ.";
                } else {
                    return "Lỗi khi gọi GPT API.";
                }
            }
            
        } catch (Exception e) {
            interaction.recordFailure(e.getMessage());
            gptDAO.insert(interaction);
            
            System.err.println("❌ GPT call failed: " + e.getMessage());
            e.printStackTrace();
            
            return "Lỗi khi xử lý yêu cầu: " + e.getMessage();
        }
    }
    
    /**
     * Build alert-specific prompt
     */
    private String buildAlertPrompt(String alertType, JSONObject contextData) {
        switch (alertType) {
            case "PO_PENDING":
                return "Có đơn đặt hàng chờ duyệt:\n" + contextData.toString(2) + 
                       "\nHãy tóm tắt và đề xuất hành động.";
            
            case "LOW_INVENTORY":
                return "Sản phẩm sắp hết hàng:\n" + contextData.toString(2) + 
                       "\nHãy đưa ra cảnh báo và khuyến nghị.";
            
            case "OUT_OF_STOCK":
                return "Sản phẩm đã hết hàng:\n" + contextData.toString(2) + 
                       "\nHãy đưa ra cảnh báo khẩn cấp.";
            
            case "REVENUE_ANOMALY":
                return "Phát hiện bất thường về doanh thu:\n" + contextData.toString(2) + 
                       "\nHãy phân tích và đưa ra nhận xét.";
            
            case "PO_OVERDUE":
                return "Đơn đặt hàng quá hạn giao:\n" + contextData.toString(2) + 
                       "\nHãy đưa ra cảnh báo và đề xuất.";
            
            case "PO_HIGH_VALUE":
                return "Đơn đặt hàng giá trị cao cần phê duyệt:\n" + contextData.toString(2) + 
                       "\nHãy tóm tắt và lưu ý.";
            
            default:
                return "Cảnh báo hệ thống:\n" + contextData.toString(2);
        }
    }
    
    /**
     * Get GPT statistics
     */
    public JSONObject getStatistics() {
        JSONObject stats = new JSONObject();
        
        try {
            stats.put("totalCostVND", gptDAO.getTotalCostVND());
            stats.put("totalCostUSD", gptDAO.getTotalCostUSD());
            stats.put("totalTokens", gptDAO.getTotalTokens());
            stats.put("averageResponseTime", gptDAO.getAverageResponseTime());
            stats.put("successRate", gptDAO.getSuccessRate());
            stats.put("model", GPT_MODEL);
        } catch (Exception e) {
            System.err.println("❌ Failed to get GPT statistics: " + e.getMessage());
        }
        
        return stats;
    }
}


