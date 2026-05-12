package com.liteflow.service.ai;

import com.liteflow.service.analytics.DemandForecastService;
import com.liteflow.service.report.RevenueReportService;
import com.liteflow.service.inventory.ProductInventoryService;
import com.liteflow.service.procurement.POAutoCreationService;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * GPT Service - Integration with OpenAI GPT API
 * Handles chat completions using GPT-3.5-turbo or GPT-4
 */
public class GPTService {
    
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    // Default values (fallback if config not found)
    private static final String DEFAULT_MODEL = "gpt-3.5-turbo";
    private static final int DEFAULT_MAX_TOKENS = 1000;
    private static final double DEFAULT_TEMPERATURE = 0.7;
    private static final int DEFAULT_CONNECT_TIMEOUT = 30;
    private static final int DEFAULT_WRITE_TIMEOUT = 30;
    private static final int DEFAULT_READ_TIMEOUT = 60;
    
    private final OkHttpClient client;
    private final String apiKey;
    private DemandForecastService demandService;
    private RevenueReportService revenueService;
    private ProductInventoryService productInventoryService;
    private final AIAgentConfigService configService;
    
    public GPTService(String apiKey) {
        this.apiKey = apiKey;
        this.configService = new AIAgentConfigService();
        
        // Build client with configurable timeouts
        int connectTimeout = configService.getIntConfig("gpt.connect_timeout", DEFAULT_CONNECT_TIMEOUT);
        int writeTimeout = configService.getIntConfig("gpt.write_timeout", DEFAULT_WRITE_TIMEOUT);
        int readTimeout = configService.getIntConfig("gpt.read_timeout", DEFAULT_READ_TIMEOUT);
        
        this.client = new OkHttpClient.Builder()
            .connectTimeout(connectTimeout, TimeUnit.SECONDS)
            .writeTimeout(writeTimeout, TimeUnit.SECONDS)
            .readTimeout(readTimeout, TimeUnit.SECONDS)
            .build();
        try {
            this.demandService = new DemandForecastService();
            this.revenueService = new RevenueReportService();
            this.productInventoryService = new ProductInventoryService();
            System.out.println("✅ GPTService: All dependency services initialized successfully");
        } catch (Throwable e) {
            System.err.println("❌ GPTService: Failed to initialize dependency services: " + e.getMessage());
            e.printStackTrace();
            // Set to null to prevent NullPointerException later
            this.demandService = null;
            this.revenueService = null;
            this.productInventoryService = null;
        }
    }
    
    /**
     * Get GPT model from config or default
     */
    private String getModel() {
        return configService.getStringConfig("gpt.model", DEFAULT_MODEL);
    }
    
    /**
     * Get max tokens from config or default
     */
    private int getMaxTokens() {
        return configService.getIntConfig("gpt.max_tokens", DEFAULT_MAX_TOKENS);
    }
    
    /**
     * Get temperature from config or default
     */
    private double getTemperature() {
        return configService.getDecimalConfig("gpt.temperature", DEFAULT_TEMPERATURE);
    }
    
    /**
     * Check if GPT features are enabled
     */
    public boolean isGPTFeaturesEnabled() {
        return configService.getBooleanConfig("gpt.enable_features", true);
    }
    
    /**
     * Send a message to GPT and get response
     * @param userMessage User's message
     * @param systemPrompt Optional system prompt (null for default)
     * @return GPT's response text
     */
    public String chat(String userMessage, String systemPrompt) throws IOException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }
        
        System.out.println("🤖 GPT Request: " + userMessage);
        
        // Check if GPT features are enabled
        if (!isGPTFeaturesEnabled()) {
            throw new IllegalStateException("GPT features are disabled in configuration");
        }
        
        // Build request JSON
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", getModel());
        requestBody.put("max_tokens", getMaxTokens());
        requestBody.put("temperature", getTemperature());
        
        // Build messages array
        JSONArray messages = new JSONArray();
        
        // Add system message (if provided)
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.put(systemMessage);
        } else {
            // Default system prompt for LiteFlow assistant
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", 
                "Bạn là trợ lý AI thông minh của LiteFlow - hệ thống quản lý nhà hàng. " +
                "Hãy trả lời bằng tiếng Việt, ngắn gọn, hữu ích và thân thiện. " +
                "Giúp người dùng về các vấn đề liên quan đến quản lý nhà hàng, đơn hàng, báo cáo, và tính năng hệ thống.");
            messages.put(systemMessage);
        }
        
        // Add user message
        JSONObject userMessageObj = new JSONObject();
        userMessageObj.put("role", "user");
        userMessageObj.put("content", userMessage);
        messages.put(userMessageObj);
        
        requestBody.put("messages", messages);
        
        System.out.println("📤 Sending request to OpenAI...");
        
        // Create HTTP request
        RequestBody body = RequestBody.create(requestBody.toString(), JSON);
        Request request = new Request.Builder()
            .url(OPENAI_API_URL)
            .addHeader("Authorization", "Bearer " + apiKey)
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build();
        
        // Execute request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                System.err.println("❌ OpenAI API Error (" + response.code() + "): " + errorBody);
                throw new IOException("OpenAI API error: " + response.code() + " - " + errorBody);
            }
            
            String responseBody = response.body().string();
            System.out.println("📥 Received response from OpenAI");
            
            // Parse response
            JSONObject jsonResponse = new JSONObject(responseBody);
            
            // Extract message content
            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject message = firstChoice.getJSONObject("message");
                String content = message.getString("content");
                
                System.out.println("✅ GPT Response: " + content.substring(0, Math.min(100, content.length())) + "...");
                
                return content.trim();
            } else {
                throw new IOException("No response from GPT");
            }
        }
    }
    
    /**
     * Intelligent chat with demand forecasting capabilities
     * Detects keywords and provides data-driven responses
     * @param userMessage User's message
     * @param userId Optional user ID for PO creation (can be null)
     */
    public String chatWithIntelligence(String userMessage, UUID userId) throws IOException {
        System.out.println("🧠 Intelligent Chat: Analyzing message...");
        
        // If intelligent services are not available, fallback to basic chat
        if (demandService == null || revenueService == null || productInventoryService == null) {
            System.out.println("⚠️ Intelligent services not available, falling back to basic chat");
            return chat(userMessage, null);
        }
        
        // Detect if user is asking about stock/inventory/demand forecasting
        String lowerMessage = userMessage.toLowerCase();
        
        // Detect confirmation messages (có, đồng ý, tạo đơn, etc) - cần check riêng để xử lý trong context stock
        // Trim và remove punctuation để match linh hoạt hơn
        String trimmedMessage = lowerMessage.trim().replaceAll("[.,!?;:]", "").trim();
        boolean isConfirmationOnly = (trimmedMessage.equals("có") || 
                                     trimmedMessage.equals("đồng ý") || 
                                     trimmedMessage.equals("ok") ||
                                     trimmedMessage.equals("yes") ||
                                     trimmedMessage.equals("tạo đơn") ||
                                     trimmedMessage.equals("đặt hàng") ||
                                     trimmedMessage.equals("tạo po")) &&
                                     !lowerMessage.contains("tồn kho") &&
                                     !lowerMessage.contains("doanh thu") &&
                                     !lowerMessage.contains("báo cáo");
        
        // Detect stock queries first - "tồn kho" queries should go to stock handler
        boolean askingAboutStock = lowerMessage.contains("tồn kho") || 
                                   lowerMessage.contains("số lượng còn lại") ||
                                   lowerMessage.contains("còn bao nhiêu") ||
                                   (lowerMessage.contains("số lượng") && !lowerMessage.contains("doanh")) ||
                                   isConfirmationOnly; // Nếu chỉ là confirmation, route vào stock handler để check context
        
        // Demand forecasting queries - loại bỏ "tồn kho" để tránh conflict
        boolean askingAboutDemand = lowerMessage.contains("nhập hàng") || 
                                    lowerMessage.contains("dự đoán") ||
                                    lowerMessage.contains("forecast") ||
                                    lowerMessage.contains("gợi ý nhập") ||
                                    lowerMessage.contains("cần mua") ||
                                    (lowerMessage.contains("hết hàng") && !askingAboutStock) ||
                                    (lowerMessage.contains("out of stock") && !askingAboutStock);
        
        boolean askingAboutAlerts = lowerMessage.contains("cảnh báo") ||
                                   lowerMessage.contains("alert") ||
                                   lowerMessage.contains("nguy hiểm") ||
                                   lowerMessage.contains("sắp hết");
        
        // Detect revenue/analytics queries
        boolean askingAboutRevenue = lowerMessage.contains("doanh thu") ||
                                    lowerMessage.contains("doanh số") ||
                                    lowerMessage.contains("bán chạy") ||
                                    lowerMessage.contains("top sản phẩm") ||
                                    lowerMessage.contains("sản phẩm bán chạy") ||
                                    lowerMessage.contains("doanh thu theo") ||
                                    lowerMessage.contains("danh mục") ||
                                    lowerMessage.contains("category") ||
                                    lowerMessage.contains("thống kê") ||
                                    lowerMessage.contains("báo cáo") ||
                                    lowerMessage.contains("phân tích") ||
                                    lowerMessage.contains("xu hướng") ||
                                    lowerMessage.contains("revenue") ||
                                    lowerMessage.contains("sales");
        
        // Detect category-specific queries
        boolean askingAboutCategory = lowerMessage.contains("doanh thu theo danh mục") ||
                                     lowerMessage.contains("danh mục nào bán chạy") ||
                                     lowerMessage.contains("danh mục bán chạy") ||
                                     lowerMessage.contains("category revenue") ||
                                     lowerMessage.contains("doanh thu danh mục") ||
                                     lowerMessage.contains("top danh mục");
        
        if (askingAboutCategory) {
            return handleCategoryRevenueQuery(userMessage);
        } else if (askingAboutRevenue) {
            return handleRevenueQuery(userMessage);
        } else if (askingAboutStock) {
            // Route stock queries to demand forecast handler but with direct stock query
            return handleStockQuery(userMessage, userId);
        } else if (askingAboutDemand) {
            return handleDemandForecastQuery(userMessage);
        } else if (askingAboutAlerts) {
            return handleStockAlertQuery(userMessage);
        } else {
            // Normal chat - check if it's a confirmation for PO creation
            // If not asking about stock but user confirmed PO, check if we can create PO
            boolean userConfirmedPO = lowerMessage.contains("có") || 
                                     lowerMessage.contains("đồng ý") || 
                                     lowerMessage.contains("tạo đơn") ||
                                     lowerMessage.contains("tạo po") ||
                                     lowerMessage.contains("đặt hàng") ||
                                     lowerMessage.contains("ok") ||
                                     lowerMessage.contains("yes");
            
            if (userConfirmedPO && userId != null && !askingAboutStock) {
                // User might be confirming PO creation from previous stock query
                // Try to get low stock items and create PO
                try {
                    if (productInventoryService == null) {
                        return "Xin lỗi, dịch vụ kiểm tra tồn kho hiện không khả dụng. Vui lòng thử lại sau.";
                    }
                    final int CRITICAL_THRESHOLD = 10;
                    final int WARNING_THRESHOLD = 20;
                    JSONObject lowStockResult = productInventoryService.getAllLowStockProducts(CRITICAL_THRESHOLD, WARNING_THRESHOLD);
                    
                    if (lowStockResult.getBoolean("success")) {
                        JSONArray criticalItems = lowStockResult.getJSONArray("criticalItems");
                        JSONArray warningItems = lowStockResult.getJSONArray("warningItems");
                        
                        JSONArray allLowStockItems = new JSONArray();
                        if (criticalItems.length() > 0) {
                            for (int i = 0; i < criticalItems.length(); i++) {
                                allLowStockItems.put(criticalItems.getJSONObject(i));
                            }
                        }
                        if (warningItems.length() > 0) {
                            for (int i = 0; i < warningItems.length(); i++) {
                                allLowStockItems.put(warningItems.getJSONObject(i));
                            }
                        }
                        
                        if (allLowStockItems.length() > 0) {
                            System.out.println("🚀 User confirmed PO creation in normal chat. Creating PO...");
                            POAutoCreationService poAutoService = new POAutoCreationService();
                            POAutoCreationService.POCreationResult result = poAutoService.createPOsFromLowStockItems(allLowStockItems, userId);
                            
                            if (!result.getCreatedPOs().isEmpty()) {
                                StringBuilder poMessage = new StringBuilder();
                                poMessage.append("✅ Đã tạo ").append(result.getCreatedPOs().size()).append(" đơn đặt hàng tự động:\n\n");
                                for (Map.Entry<UUID, UUID> entry : result.getCreatedPOs().entrySet()) {
                                    poMessage.append("- Đơn hàng ID: ").append(entry.getValue()).append("\n");
                                }
                                poMessage.append("\nVui lòng kiểm tra trong module 'Mua sắm' -> 'Đơn đặt hàng' để duyệt đơn hàng.");
                                
                                // Thêm note về các items đã skip
                                if (!result.getSkippedItems().isEmpty()) {
                                    poMessage.append("\n\n📋 Lưu ý: Các sản phẩm sau đã được đặt hàng trong vòng 1 ngày qua (đã đặt thêm hàng):\n");
                                    for (String itemName : result.getSkippedItems()) {
                                        poMessage.append("- ").append(itemName).append("\n");
                                    }
                                }
                                
                                return poMessage.toString();
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Error checking for PO creation in normal chat: " + e.getMessage());
                    // Continue to normal chat
                }
            }
            
            // Normal chat
            return chat(userMessage, null);
        }
    }
    
    /**
     * Handle stock queries - Query trực tiếp từ ProductStock table
     * @param userMessage User's message
     * @param userId Optional user ID for PO creation (can be null)
     */
    private String handleStockQuery(String userMessage, UUID userId) throws IOException {
        System.out.println("📦 Handling Stock Query - Query from ProductStock table...");
        
        try {
            if (productInventoryService == null) {
                return "Xin lỗi, dịch vụ kiểm tra tồn kho hiện không khả dụng. Vui lòng thử lại sau.";
            }
            // Thresholds
            final int CRITICAL_THRESHOLD = 10;
            final int WARNING_THRESHOLD = 20;
            
            // Query trực tiếp từ ProductStock table để lấy amount chính xác
            JSONObject lowStockResult = productInventoryService.getAllLowStockProducts(CRITICAL_THRESHOLD, WARNING_THRESHOLD);
            
            if (!lowStockResult.getBoolean("success")) {
                String error = lowStockResult.optString("error", "Lỗi khi truy vấn tồn kho");
                return error;
            }
            
            // Get replenishment suggestions from DemandForecastService
            JSONObject forecastResult = new JSONObject();
            try {
                if (demandService != null) {
                    forecastResult = demandService.generateReplenishmentSuggestions();
                } else {
                    forecastResult = new JSONObject().put("success", false).put("error", "Dịch vụ dự báo nhu cầu không khả dụng");
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error getting forecast suggestions: " + e.getMessage());
                forecastResult.put("success", false);
                forecastResult.put("error", e.getMessage());
            }
            
            StringBuilder context = new StringBuilder();
            context.append("**TỔNG QUAN TỒN KHO TỪ DATABASE (ProductStock table):**\n\n");
            context.append("📊 **Thống kê:**\n");
            context.append("- Tổng số sản phẩm cần nhập: ").append(lowStockResult.optInt("count", 0)).append("\n");
            context.append("- Sản phẩm NGUY HIỂM (≤").append(CRITICAL_THRESHOLD).append("): ").append(lowStockResult.optInt("criticalCount", 0)).append("\n");
            context.append("- Sản phẩm CẢNH BÁO (≤").append(WARNING_THRESHOLD).append("): ").append(lowStockResult.optInt("warningCount", 0)).append("\n\n");
            
            // Critical items (URGENT)
            JSONArray criticalItems = lowStockResult.optJSONArray("criticalItems");
            if (criticalItems != null && criticalItems.length() > 0) {
                context.append("🔴 **SẢN PHẨM NGUY HIỂM - CẦN NHẬP NGAY:**\n");
                try {
                    for (int i = 0; i < Math.min(10, criticalItems.length()); i++) {
                        JSONObject item = criticalItems.getJSONObject(i);
                        context.append(String.format("%d. %s (Size: %s) - Tồn kho: %d - Giá: %s\n",
                            i + 1,
                            item.getString("productName"),
                            item.getString("size"),
                            item.getInt("stockAmount"),
                            formatCurrency(item.getDouble("price"))
                        ));
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Error processing critical items: " + e.getMessage());
                }
                if (criticalItems.length() > 10) {
                    context.append(String.format("... và %d sản phẩm khác\n", criticalItems.length() - 10));
                }
                context.append("\n");
            }
            
            // Warning items (HIGH)
            JSONArray warningItems = lowStockResult.optJSONArray("warningItems");
            if (warningItems != null && warningItems.length() > 0) {
                context.append("🟡 **SẢN PHẨM CẢNH BÁO - Nên nhập sớm:**\n");
                try {
                    for (int i = 0; i < Math.min(10, warningItems.length()); i++) {
                        JSONObject item = warningItems.getJSONObject(i);
                        context.append(String.format("%d. %s (Size: %s) - Tồn kho: %d - Giá: %s\n",
                            i + 1,
                            item.getString("productName"),
                            item.getString("size"),
                            item.getInt("stockAmount"),
                            formatCurrency(item.getDouble("price"))
                        ));
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Error processing warning items: " + e.getMessage());
                }
                if (warningItems.length() > 10) {
                    context.append(String.format("... và %d sản phẩm khác\n", warningItems.length() - 10));
                }
                context.append("\n");
            }
            
            // Get replenishment suggestions
            JSONArray urgentSuggestions = new JSONArray();
            JSONArray highPrioritySuggestions = new JSONArray();
            
            if (forecastResult.getBoolean("success")) {
                JSONArray allSuggestions = forecastResult.optJSONArray("allSuggestions");
                if (allSuggestions != null) {
                    for (int i = 0; i < allSuggestions.length(); i++) {
                        JSONObject suggestion = allSuggestions.getJSONObject(i);
                        String urgency = suggestion.getString("urgency");
                        if ("URGENT".equals(urgency)) {
                            urgentSuggestions.put(suggestion);
                        } else if ("HIGH".equals(urgency)) {
                            highPrioritySuggestions.put(suggestion);
                        }
                    }
                }
            }
            
            // Detect user confirmation for PO creation
            String lowerMessage = userMessage.toLowerCase();
            boolean userConfirmedPO = lowerMessage.contains("có") || 
                                     lowerMessage.contains("đồng ý") || 
                                     lowerMessage.contains("tạo đơn") ||
                                     lowerMessage.contains("tạo po") ||
                                     lowerMessage.contains("đặt hàng") ||
                                     lowerMessage.contains("ok") ||
                                     lowerMessage.contains("yes");
            
            // Add replenishment recommendations
            boolean needsReplenishment = (criticalItems != null && criticalItems.length() > 0) || 
                                         (warningItems != null && warningItems.length() > 0);
            
            // Combine all low stock items for PO creation
            JSONArray allLowStockItems = new JSONArray();
            if (criticalItems != null && criticalItems.length() > 0) {
                for (int i = 0; i < criticalItems.length(); i++) {
                    allLowStockItems.put(criticalItems.getJSONObject(i));
                }
            }
            if (warningItems != null && warningItems.length() > 0) {
                for (int i = 0; i < warningItems.length(); i++) {
                    allLowStockItems.put(warningItems.getJSONObject(i));
                }
            }
            
            // If user confirmed and has low stock items, create PO
            if (needsReplenishment && userConfirmedPO && userId != null && allLowStockItems.length() > 0) {
                try {
                    System.out.println("🚀 User confirmed PO creation. Creating PO for " + allLowStockItems.length() + " items...");
                    POAutoCreationService poAutoService = new POAutoCreationService();
                    POAutoCreationService.POCreationResult result = poAutoService.createPOsFromLowStockItems(allLowStockItems, userId);
                    
                    if (!result.getCreatedPOs().isEmpty()) {
                        StringBuilder poMessage = new StringBuilder();
                        poMessage.append("✅ Đã tạo ").append(result.getCreatedPOs().size()).append(" đơn đặt hàng tự động:\n\n");
                        for (Map.Entry<UUID, UUID> entry : result.getCreatedPOs().entrySet()) {
                            poMessage.append("- Đơn hàng ID: ").append(entry.getValue()).append("\n");
                        }
                        poMessage.append("\nVui lòng kiểm tra trong module 'Mua sắm' -> 'Đơn đặt hàng' để duyệt đơn hàng.\n\n");
                        
                        // Thêm note về các items đã skip
                        if (!result.getSkippedItems().isEmpty()) {
                            poMessage.append("📋 Lưu ý: Các sản phẩm sau đã được đặt hàng trong vòng 1 ngày qua (đã đặt thêm hàng):\n");
                            for (String itemName : result.getSkippedItems()) {
                                poMessage.append("- ").append(itemName).append("\n");
                            }
                            poMessage.append("\n");
                        }
                        
                        return poMessage.toString();
                    } else {
                        // Check if all items were skipped
                        if (!result.getSkippedItems().isEmpty()) {
                            StringBuilder poMessage = new StringBuilder();
                            poMessage.append("ℹ️ Tất cả các sản phẩm đã được đặt hàng trong vòng 1 ngày qua (đã đặt thêm hàng):\n\n");
                            for (String itemName : result.getSkippedItems()) {
                                poMessage.append("- ").append(itemName).append("\n");
                            }
                            return poMessage.toString();
                        }
                        return "Xin lỗi, không thể tạo đơn đặt hàng. Vui lòng kiểm tra lại thông tin nhà cung cấp hoặc liên hệ quản trị viên.";
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error creating PO: " + e.getMessage());
                    e.printStackTrace();
                    return "Xin lỗi, đã xảy ra lỗi khi tạo đơn đặt hàng: " + e.getMessage();
                }
            }
            
            if (needsReplenishment) {
                context.append("**⚠️ KHUYẾN NGHỊ NHẬP HÀNG:**\n\n");
                
                // Calculate total suggested quantity
                int totalSuggestedQty = 0;
                double totalEstimatedValue = 0.0;
                
                // Match suggestions with low stock items
                if (criticalItems != null) {
                    for (int i = 0; i < criticalItems.length(); i++) {
                        JSONObject item = criticalItems.getJSONObject(i);
                        String productName = item.getString("productName");
                        String size = item.getString("size");
                        int stock = item.getInt("stockAmount");
                        
                        // Find matching suggestion
                        JSONObject matchingSuggestion = null;
                        for (int j = 0; j < urgentSuggestions.length(); j++) {
                            JSONObject suggestion = urgentSuggestions.getJSONObject(j);
                            if (suggestion.getString("productName").equalsIgnoreCase(productName) &&
                                suggestion.getString("size").equalsIgnoreCase(size)) {
                                matchingSuggestion = suggestion;
                                break;
                            }
                        }
                        
                        int suggestedQty;
                        if (matchingSuggestion != null) {
                            suggestedQty = matchingSuggestion.getInt("suggestedOrderQty");
                        } else {
                            suggestedQty = Math.max(20 - stock, 15); // Ít nhất đưa về 20, tối thiểu nhập 15
                        }
                        
                        totalSuggestedQty += suggestedQty;
                        totalEstimatedValue += suggestedQty * item.getDouble("price");
                    }
                }
                
                if (warningItems != null) {
                    for (int i = 0; i < warningItems.length(); i++) {
                        JSONObject item = warningItems.getJSONObject(i);
                        String productName = item.getString("productName");
                        String size = item.getString("size");
                        int stock = item.getInt("stockAmount");
                        
                        // Find matching suggestion
                        JSONObject matchingSuggestion = null;
                        for (int j = 0; j < highPrioritySuggestions.length(); j++) {
                            JSONObject suggestion = highPrioritySuggestions.getJSONObject(j);
                            if (suggestion.getString("productName").equalsIgnoreCase(productName) &&
                                suggestion.getString("size").equalsIgnoreCase(size)) {
                                matchingSuggestion = suggestion;
                                break;
                            }
                        }
                        
                        int suggestedQty;
                        if (matchingSuggestion != null) {
                            suggestedQty = matchingSuggestion.getInt("suggestedOrderQty");
                        } else {
                            suggestedQty = Math.max(20 - stock, 15);
                        }
                        
                        totalSuggestedQty += suggestedQty;
                        totalEstimatedValue += suggestedQty * item.getDouble("price");
                    }
                }
                
                context.append("**📋 TÓM TẮT:**\n");
                context.append(String.format("- 🔴 %d sản phẩm NGUY HIỂM (≤%d) - CẦN NHẬP NGAY\n", 
                    criticalItems != null ? criticalItems.length() : 0, CRITICAL_THRESHOLD));
                context.append(String.format("- 🟡 %d sản phẩm CẢNH BÁO (≤%d) - Nên nhập sớm\n", 
                    warningItems != null ? warningItems.length() : 0, WARNING_THRESHOLD));
                if (totalSuggestedQty > 0) {
                    context.append(String.format("- 💰 Tổng số lượng ước tính nên nhập: %d đơn vị\n", totalSuggestedQty));
                    context.append(String.format("- 💵 Giá trị đơn hàng ước tính: %s\n", formatCurrency(totalEstimatedValue)));
                }
                context.append("- ⚠️ LƯU Ý: Bất kỳ sản phẩm nào có tồn kho ≤").append(WARNING_THRESHOLD).append(" đều được khuyến nghị nhập hàng\n");
                context.append("- 📋 Hành động: Tạo đơn đặt hàng (PO) trong module Procurement\n");
                context.append("\n**🤖 AI CÓ THỂ TỰ ĐỘNG TẠO ĐƠN ĐẶT HÀNG:**\n");
                context.append("Bạn có muốn tôi tự động tạo đơn đặt hàng (PO) cho các sản phẩm trên không?\n");
                context.append("Trả lời: 'có', 'đồng ý', 'tạo đơn', hoặc 'đặt hàng' để tôi tạo đơn hàng tự động.\n");
            } else {
                context.append("\n✅ **TÌNH TRẠNG TỒN KHO:**\n");
                context.append("Tất cả sản phẩm đều đủ hàng (>").append(WARNING_THRESHOLD).append(" đơn vị). Không cần nhập hàng ngay lúc này.\n");
            }
            
            String enhancedPrompt = String.format(
                "Bạn là AI Inventory Analyst chuyên nghiệp của LiteFlow. " +
                "Dựa vào dữ liệu tồn kho TRỰC TIẾP TỪ DATABASE (ProductStock table) bên dưới, hãy:\n\n" +
                "1. Tóm tắt tình trạng tồn kho hiện tại một cách chi tiết\n" +
                "2. Phân tích các rủi ro:\n" +
                "   - HẾT HÀNG (stock = 0): Cần nhập NGAY LẬP TỨC\n" +
                "   - NGUY HIỂM (stock ≤ %d): Cần nhập trong 1-2 ngày\n" +
                "   - CẢNH BÁO (stock ≤ %d): Nên nhập trong tuần này\n" +
                "3. Liệt kê các sản phẩm cần nhập hàng với số liệu cụ thể\n" +
                "4. QUAN TRỌNG: Nếu có bất kỳ sản phẩm nào có tồn kho ≤ %d, BẮT BUỘC phải khuyến nghị nhập hàng\n" +
                "5. Gợi ý hành động cụ thể (tạo PO, liên hệ nhà cung cấp)\n" +
                "6. QUAN TRỌNG: Nếu có sản phẩm tồn kho thấp, cuối câu trả lời HÃY HỎI user: 'Bạn có muốn tôi tự động tạo đơn đặt hàng (PO) không? Trả lời: có/đồng ý/tạo đơn để tôi tạo đơn hàng tự động.'\n\n" +
                "LƯU Ý: Không bao giờ nói 'không cần nhập hàng' nếu có sản phẩm có tồn kho ≤ %d!\n\n" +
                "Dữ liệu từ database:\n\n%s\n\n" +
                "Hãy trả lời một cách rõ ràng, có số liệu cụ thể và khuyến nghị thực thi được. " +
                "Nếu có sản phẩm tồn kho thấp (≤%d), phải nhấn mạnh cần nhập hàng và HỎI user có muốn tạo PO tự động không.",
                CRITICAL_THRESHOLD, WARNING_THRESHOLD, WARNING_THRESHOLD, WARNING_THRESHOLD,
                context.toString(), WARNING_THRESHOLD
            );
            
            return chat(userMessage, enhancedPrompt);
            
        } catch (Exception e) {
            System.err.println("❌ Error in stock query: " + e.getMessage());
            e.printStackTrace();
            return "Xin lỗi, tôi gặp lỗi khi kiểm tra tồn kho. Vui lòng thử lại sau.";
        }
    }
    
    /**
     * Handle demand forecasting queries with real data
     */
    private String handleDemandForecastQuery(String userMessage) throws IOException {
        System.out.println("📊 Handling Demand Forecast Query...");
        
        try {
            if (demandService == null) {
                return "Xin lỗi, dịch vụ dự báo nhu cầu hiện không khả dụng. Vui lòng thử lại sau.";
            }
            // Get real demand forecast data
            JSONObject forecast = demandService.generateReplenishmentSuggestions();
            
            if (!forecast.getBoolean("success")) {
                return "Xin lỗi, tôi gặp lỗi khi phân tích dữ liệu tồn kho. Vui lòng thử lại sau.";
            }
            
            // Build context from real data
            StringBuilder context = new StringBuilder();
            context.append("Dựa trên phân tích dữ liệu thực tế của hệ thống:\n\n");
            
            JSONObject summary = forecast.getJSONObject("summary");
            context.append("**Tổng quan:**\n");
            context.append("- Tổng số sản phẩm cần nhập: ").append(summary.getInt("totalSuggestions")).append("\n");
            context.append("- Sản phẩm URGENT: ").append(summary.getInt("urgentItems")).append("\n");
            context.append("- Sản phẩm ưu tiên cao: ").append(summary.getInt("highPriorityItems")).append("\n");
            context.append("- Giá trị đơn hàng ước tính: ").append(String.format("%,d", summary.getLong("estimatedOrderValue"))).append(" VNĐ\n\n");
            
            // Add urgent items
            JSONArray urgentItems = forecast.getJSONArray("urgentItems");
            if (urgentItems.length() > 0) {
                context.append("**Top sản phẩm cần nhập NGAY:**\n");
                for (int i = 0; i < Math.min(5, urgentItems.length()); i++) {
                    JSONObject item = urgentItems.getJSONObject(i);
                    context.append(String.format("%d. %s (%s) - Tồn kho: %d - Gợi ý nhập: %d - Nguy cơ: %s\n",
                        i + 1,
                        item.getString("productName"),
                        item.getString("size"),
                        item.getInt("currentStock"),
                        item.getInt("suggestedOrderQty"),
                        item.getString("stockoutRisk")
                    ));
                }
            }
            
            // Add insights
            JSONObject insights = forecast.getJSONObject("insights");
            if (insights.has("recommendations")) {
                JSONArray recommendations = insights.getJSONArray("recommendations");
                if (recommendations.length() > 0) {
                    context.append("\n**Khuyến nghị:**\n");
                    for (int i = 0; i < recommendations.length(); i++) {
                        context.append("- ").append(recommendations.getString(i)).append("\n");
                    }
                }
            }
            
            // Create enhanced system prompt with real data
            String enhancedPrompt = String.format(
                "Bạn là AI Analyst thông minh của LiteFlow. " +
                "Dựa vào dữ liệu phân tích thực tế bên dưới, hãy trả lời câu hỏi của người dùng một cách chi tiết, chuyên nghiệp và hữu ích.\n\n" +
                "%s\n\n" +
                "Hãy diễn giải dữ liệu trên một cách dễ hiểu, đưa ra insight và khuyến nghị cụ thể.",
                context.toString()
            );
            
            // Call GPT with enhanced context
            return chat(userMessage, enhancedPrompt);
            
        } catch (Exception e) {
            System.err.println("❌ Error in demand forecast query: " + e.getMessage());
            e.printStackTrace();
            return "Xin lỗi, tôi gặp lỗi khi phân tích dữ liệu. Vui lòng thử lại sau.";
        }
    }
    
    /**
     * Handle revenue queries - Category revenue and top products
     */
    private String handleRevenueQuery(String userMessage) throws IOException {
        System.out.println("📊 Handling Revenue Query...");
        
        try {
            // Get date range (default: last 30 days)
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);
            
            // Try to extract date range from message
            String lowerMessage = userMessage.toLowerCase();
            if (lowerMessage.contains("hôm nay") || lowerMessage.contains("today")) {
                startDate = endDate;
            } else if (lowerMessage.contains("tuần này") || lowerMessage.contains("this week")) {
                startDate = endDate.minusDays(7);
            } else if (lowerMessage.contains("tháng này") || lowerMessage.contains("this month")) {
                startDate = endDate.withDayOfMonth(1);
            } else if (lowerMessage.contains("tháng trước") || lowerMessage.contains("last month")) {
                LocalDate firstDayOfCurrentMonth = endDate.withDayOfMonth(1);
                endDate = firstDayOfCurrentMonth.minusDays(1);
                startDate = endDate.withDayOfMonth(1);
            }
            
            System.out.println("📅 Revenue query date range: " + startDate + " to " + endDate);
            
            if (revenueService == null) {
                return "Xin lỗi, dịch vụ báo cáo doanh thu hiện không khả dụng. Vui lòng thử lại sau.";
            }
            // Get revenue report data
            JSONObject report = revenueService.generateReport(startDate, endDate);
            
            // Build comprehensive context
            StringBuilder context = new StringBuilder();
            context.append("**DỮ LIỆU DOANH THU THỰC TẾ** (từ ").append(startDate).append(" đến ").append(endDate).append(")\n\n");
            
            // Overall statistics
            context.append("**📊 TỔNG QUAN:**\n");
            context.append("- Tổng doanh thu: ").append(formatCurrency(report.optDouble("totalRevenue", 0))).append("\n");
            context.append("- Tổng số đơn: ").append(report.optLong("totalOrders", 0)).append(" đơn\n");
            context.append("- Giá trị đơn trung bình: ").append(formatCurrency(report.optDouble("avgOrderValue", 0))).append("\n");
            if (report.has("growth")) {
                double growth = report.optDouble("growth", 0);
                context.append("- Tăng trưởng: ").append(String.format("%+.1f%%", growth)).append("\n");
            }
            context.append("\n");
            
            // Category revenue data
            if (report.has("productData")) {
                JSONObject categoryData = report.getJSONObject("productData");
                JSONArray categories = categoryData.optJSONArray("categories");
                JSONArray revenues = categoryData.optJSONArray("revenues");
                
                if (categories != null && revenues != null && categories.length() > 0) {
                    context.append("**🏷️ DOANH THU THEO DANH MỤC:**\n");
                    double totalCategoryRevenue = 0;
                    for (int i = 0; i < categories.length(); i++) {
                        String category = categories.getString(i);
                        double revenue = revenues.optDouble(i, 0);
                        totalCategoryRevenue += revenue;
                        if (revenue > 0) {
                            context.append(String.format("- %s: %s\n", category, formatCurrency(revenue)));
                        }
                    }
                    context.append(String.format("Tổng: %s\n\n", formatCurrency(totalCategoryRevenue)));
                }
            }
            
            // Top products
            if (report.has("topProducts")) {
                JSONArray topProducts = report.getJSONArray("topProducts");
                if (topProducts.length() > 0) {
                    context.append("**🏆 TOP 10 SẢN PHẨM BÁN CHẠY:**\n");
                    for (int i = 0; i < Math.min(10, topProducts.length()); i++) {
                        JSONObject product = topProducts.getJSONObject(i);
                        String name = product.optString("name", "N/A");
                        long quantity = product.optLong("quantity", 0);
                        double revenue = product.optDouble("revenue", 0);
                        String share = product.optString("share", "0%");
                        
                        context.append(String.format("%d. %s - SL: %d - Doanh thu: %s (%s)\n",
                            i + 1, name, quantity, formatCurrency(revenue), share));
                    }
                    context.append("\n");
                }
            }
            
            // Hourly trend (if available)
            if (report.has("hourlyData")) {
                JSONObject hourlyData = report.getJSONObject("hourlyData");
                JSONArray hours = hourlyData.optJSONArray("hours");
                JSONArray hourRevenues = hourlyData.optJSONArray("revenues");
                
                if (hours != null && hourRevenues != null && hours.length() > 0) {
                    // Find peak hour
                    double maxRevenue = 0;
                    String peakHour = "";
                    for (int i = 0; i < hours.length(); i++) {
                        double revenue = hourRevenues.optDouble(i, 0);
                        if (revenue > maxRevenue) {
                            maxRevenue = revenue;
                            peakHour = hours.getString(i);
                        }
                    }
                    if (!peakHour.isEmpty()) {
                        context.append("**⏰ GIỜ CAO ĐIỂM:** ").append(peakHour).append(" (Doanh thu: ").append(formatCurrency(maxRevenue)).append(")\n\n");
                    }
                }
            }
            
            // Create enhanced system prompt with insights
            String enhancedPrompt = String.format(
                "Bạn là AI Business Analyst chuyên nghiệp của LiteFlow - hệ thống quản lý nhà hàng. " +
                "Bạn có khả năng phân tích dữ liệu doanh thu, đưa ra insights sâu sắc và khuyến nghị chiến lược.\n\n" +
                "Dữ liệu doanh thu thực tế của hệ thống:\n\n%s\n\n" +
                "Hãy phân tích dữ liệu trên và:\n" +
                "1. Tóm tắt xu hướng và điểm nổi bật\n" +
                "2. So sánh hiệu suất giữa các danh mục sản phẩm\n" +
                "3. Phân tích top sản phẩm bán chạy và lý do\n" +
                "4. Đưa ra 3-5 khuyến nghị cụ thể để cải thiện doanh thu\n" +
                "5. Dự đoán xu hướng ngắn hạn nếu có thể\n\n" +
                "Trả lời bằng tiếng Việt, rõ ràng, chuyên nghiệp và có tính hành động cao.",
                context.toString()
            );
            
            return chat(userMessage, enhancedPrompt);
            
        } catch (Exception e) {
            System.err.println("❌ Error in revenue query: " + e.getMessage());
            e.printStackTrace();
            return "Xin lỗi, tôi gặp lỗi khi phân tích dữ liệu doanh thu. Vui lòng thử lại sau.";
        }
    }
    
    /**
     * Handle category revenue queries - Focused on category performance
     */
    private String handleCategoryRevenueQuery(String userMessage) throws IOException {
        System.out.println("🏷️ Handling Category Revenue Query...");
        
        try {
            // Get date range (default: last 30 days)
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);
            
            // Try to extract date range from message
            String lowerMessage = userMessage.toLowerCase();
            if (lowerMessage.contains("hôm nay") || lowerMessage.contains("today")) {
                startDate = endDate;
            } else if (lowerMessage.contains("tuần này") || lowerMessage.contains("this week")) {
                startDate = endDate.minusDays(7);
            } else if (lowerMessage.contains("tháng này") || lowerMessage.contains("this month")) {
                startDate = endDate.withDayOfMonth(1);
            } else if (lowerMessage.contains("tháng trước") || lowerMessage.contains("last month")) {
                LocalDate firstDayOfCurrentMonth = endDate.withDayOfMonth(1);
                endDate = firstDayOfCurrentMonth.minusDays(1);
                startDate = endDate.withDayOfMonth(1);
            }
            
            System.out.println("📅 Category revenue query date range: " + startDate + " to " + endDate);
            
            if (revenueService == null) {
                return "Xin lỗi, dịch vụ báo cáo doanh thu hiện không khả dụng. Vui lòng thử lại sau.";
            }
            // Get revenue report data
            JSONObject report = revenueService.generateReport(startDate, endDate);
            
            // Build focused category analysis context
            StringBuilder context = new StringBuilder();
            context.append("**PHÂN TÍCH DOANH THU THEO DANH MỤC SẢN PHẨM**\n");
            context.append("Kỳ phân tích: ").append(startDate).append(" đến ").append(endDate).append("\n\n");
            
            // Overall summary
            double totalRevenue = report.optDouble("totalRevenue", 0);
            long totalOrders = report.optLong("totalOrders", 0);
            
            context.append("**📊 TỔNG QUAN:**\n");
            context.append("- Tổng doanh thu: ").append(formatCurrency(totalRevenue)).append("\n");
            context.append("- Tổng số đơn: ").append(totalOrders).append(" đơn\n\n");
            
            // Category revenue analysis
            if (report.has("productData")) {
                JSONObject categoryData = report.getJSONObject("productData");
                JSONArray categories = categoryData.optJSONArray("categories");
                JSONArray revenues = categoryData.optJSONArray("revenues");
                
                if (categories != null && revenues != null && categories.length() > 0) {
                    // Build category list with ranking
                    java.util.List<CategoryInfo> categoryList = new java.util.ArrayList<>();
                    double totalCategoryRevenue = 0;
                    
                    for (int i = 0; i < categories.length(); i++) {
                        String category = categories.getString(i);
                        double revenue = revenues.optDouble(i, 0);
                        if (revenue > 0 && !category.equals("Chưa có dữ liệu")) {
                            categoryList.add(new CategoryInfo(category, revenue));
                            totalCategoryRevenue += revenue;
                        }
                    }
                    
                    // Sort by revenue (descending)
                    categoryList.sort((a, b) -> Double.compare(b.revenue, a.revenue));
                    
                    if (!categoryList.isEmpty()) {
                        context.append("**🏆 BẢNG XẾP HẠNG DANH MỤC THEO DOANH THU:**\n\n");
                        
                        int rank = 1;
                        for (CategoryInfo cat : categoryList) {
                            double percentage = totalCategoryRevenue > 0 ? 
                                (cat.revenue / totalCategoryRevenue * 100) : 0;
                            
                            String emoji = "";
                            if (rank == 1) emoji = "🥇";
                            else if (rank == 2) emoji = "🥈";
                            else if (rank == 3) emoji = "🥉";
                            else emoji = rank + ".";
                            
                            context.append(String.format("%s %s\n", emoji, cat.name));
                            context.append(String.format("   💰 Doanh thu: %s\n", formatCurrency(cat.revenue)));
                            context.append(String.format("   📊 Tỷ trọng: %.1f%%\n\n", percentage));
                            
                            rank++;
                        }
                        
                        // Add top 3 highlights
                        context.append("**⭐ ĐIỂM NỔI BẬT:**\n");
                        if (categoryList.size() >= 1) {
                            CategoryInfo top1 = categoryList.get(0);
                            double top1Percent = totalCategoryRevenue > 0 ? 
                                (top1.revenue / totalCategoryRevenue * 100) : 0;
                            context.append(String.format("- Danh mục số 1: %s (%.1f%% tổng doanh thu)\n", 
                                top1.name, top1Percent));
                        }
                        if (categoryList.size() >= 2) {
                            CategoryInfo top2 = categoryList.get(1);
                            double top2Percent = totalCategoryRevenue > 0 ? 
                                (top2.revenue / totalCategoryRevenue * 100) : 0;
                            context.append(String.format("- Danh mục số 2: %s (%.1f%% tổng doanh thu)\n", 
                                top2.name, top2Percent));
                        }
                        
                        // Calculate distribution insights
                        if (categoryList.size() > 1) {
                            CategoryInfo top1 = categoryList.get(0);
                            double top1Percent = totalCategoryRevenue > 0 ? 
                                (top1.revenue / totalCategoryRevenue * 100) : 0;
                            
                            if (top1Percent > 50) {
                                context.append("\n⚠️ **Lưu ý:** Danh mục hàng đầu chiếm hơn 50% doanh thu. ");
                                context.append("Nên đa dạng hóa để giảm rủi ro phụ thuộc.\n");
                            } else if (top1Percent > 30) {
                                context.append("\n✅ **Tốt:** Doanh thu được phân bổ khá đồng đều giữa các danh mục.\n");
                            }
                        }
                    } else {
                        context.append("⚠️ Chưa có dữ liệu doanh thu theo danh mục trong kỳ này.\n");
                    }
                }
            }
            
            // Cross-reference with top products if available
            if (report.has("topProducts")) {
                JSONArray topProducts = report.getJSONArray("topProducts");
                if (topProducts.length() > 0) {
                    context.append("\n**🔗 LIÊN KẾT VỚI TOP SẢN PHẨM:**\n");
                    context.append("Top 3 sản phẩm bán chạy nhất:\n");
                    for (int i = 0; i < Math.min(3, topProducts.length()); i++) {
                        JSONObject product = topProducts.getJSONObject(i);
                        String name = product.optString("name", "N/A");
                        double revenue = product.optDouble("revenue", 0);
                        context.append(String.format("%d. %s - %s\n", 
                            i + 1, name, formatCurrency(revenue)));
                    }
                }
            }
            
            // Create enhanced system prompt for category analysis
            String enhancedPrompt = String.format(
                "Bạn là AI Category Analyst chuyên nghiệp của LiteFlow. " +
                "Bạn chuyên phân tích hiệu suất danh mục sản phẩm và đưa ra chiến lược tối ưu.\n\n" +
                "Dữ liệu doanh thu theo danh mục:\n\n%s\n\n" +
                "Hãy phân tích CHUYÊN SÂU về danh mục và:\n" +
                "1. **Phân tích cấu trúc doanh thu:** Xác định danh mục nào đóng góp nhiều nhất và tại sao\n" +
                "2. **So sánh hiệu suất:** So sánh tỷ trọng và tác động của từng danh mục\n" +
                "3. **Đánh giá rủi ro:** Phân tích sự phụ thuộc vào danh mục hàng đầu\n" +
                "4. **Khuyến nghị chiến lược:**\n" +
                "   - Danh mục nào nên đầu tư thêm (marketing, inventory)\n" +
                "   - Danh mục nào có tiềm năng tăng trưởng\n" +
                "   - Cách cân bằng portfolio danh mục\n" +
                "5. **Insights hành động:** Đưa ra 3-5 hành động cụ thể để tối ưu doanh thu theo danh mục\n\n" +
                "Trả lời bằng tiếng Việt, rõ ràng, có số liệu cụ thể và khuyến nghị thực thi được.",
                context.toString()
            );
            
            return chat(userMessage, enhancedPrompt);
            
        } catch (Exception e) {
            System.err.println("❌ Error in category revenue query: " + e.getMessage());
            e.printStackTrace();
            return "Xin lỗi, tôi gặp lỗi khi phân tích doanh thu theo danh mục. Vui lòng thử lại sau.";
        }
    }
    
    /**
     * Helper class for category ranking
     */
    private static class CategoryInfo {
        String name;
        double revenue;
        
        CategoryInfo(String name, double revenue) {
            this.name = name;
            this.revenue = revenue;
        }
    }
    
    /**
     * Format currency helper
     */
    private String formatCurrency(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount < 0) {
            return "0 VNĐ";
        }
        return String.format("%,.0f VNĐ", amount);
    }
    
    /**
     * Handle stock alert queries
     */
    private String handleStockAlertQuery(String userMessage) throws IOException {
        System.out.println("⚠️ Handling Stock Alert Query...");
        
        try {
            if (demandService == null) {
                return "Xin lỗi, dịch vụ cảnh báo tồn kho hiện không khả dụng. Vui lòng thử lại sau.";
            }
            JSONObject alerts = demandService.getStockAlerts();
            
            if (!alerts.getBoolean("success")) {
                return "Xin lỗi, tôi gặp lỗi khi kiểm tra cảnh báo tồn kho.";
            }
            
            StringBuilder context = new StringBuilder();
            context.append("**Cảnh báo tồn kho hiện tại:**\n\n");
            
            JSONArray critical = alerts.getJSONArray("criticalStock");
            JSONArray warning = alerts.getJSONArray("lowStock");
            
            if (critical.length() > 0) {
                context.append("🔴 **NGUY HIỂM (≤5 sản phẩm):**\n");
                for (int i = 0; i < Math.min(5, critical.length()); i++) {
                    JSONObject item = critical.getJSONObject(i);
                    context.append(String.format("- %s (%s): %d sản phẩm\n",
                        item.getString("productName"),
                        item.getString("size"),
                        item.getInt("currentStock")
                    ));
                }
                context.append("\n");
            }
            
            if (warning.length() > 0) {
                context.append("🟡 **CẢNH BÁO (≤20 sản phẩm):**\n");
                for (int i = 0; i < Math.min(5, warning.length()); i++) {
                    JSONObject item = warning.getJSONObject(i);
                    context.append(String.format("- %s (%s): %d sản phẩm\n",
                        item.getString("productName"),
                        item.getString("size"),
                        item.getInt("currentStock")
                    ));
                }
            }
            
            if (critical.length() == 0 && warning.length() == 0) {
                return "✅ Tốt! Hiện tại không có cảnh báo tồn kho nào. Tất cả sản phẩm đều đủ số lượng.";
            }
            
            String enhancedPrompt = String.format(
                "Bạn là AI Analyst của LiteFlow. Dựa vào dữ liệu cảnh báo tồn kho bên dưới, hãy phân tích và đưa ra khuyến nghị:\n\n%s\n\n" +
                "Hãy ưu tiên giải quyết các sản phẩm NGUY HIỂM trước, sau đó đến CẢNH BÁO.",
                context.toString()
            );
            
            return chat(userMessage, enhancedPrompt);
            
        } catch (Exception e) {
            System.err.println("❌ Error in stock alert query: " + e.getMessage());
            e.printStackTrace();
            return "Xin lỗi, tôi gặp lỗi khi kiểm tra cảnh báo. Vui lòng thử lại sau.";
        }
    }
    
    /**
     * Simple chat without custom system prompt
     */
    public String chat(String userMessage) throws IOException {
        return chat(userMessage, null);
    }
    
    /**
     * Check if API key is configured
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }
}

