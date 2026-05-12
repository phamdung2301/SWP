package com.liteflow.controller.api;

import com.liteflow.service.ai.GPTService;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

/**
 * ChatBot API Servlet
 * Handles chat messages and returns GPT responses
 * Endpoint: /api/chatbot
 */
@WebServlet("/api/chatbot")
public class ChatBotServlet extends HttpServlet {
    
    private GPTService gptService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        
        System.out.println("🚀 ChatBotServlet initialization started...");
        
        // Load API key from environment or config
        String apiKey = getOpenAIApiKey();
        
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("❌ CRITICAL: OPENAI_API_KEY not configured. ChatBot will not work.");
            System.err.println("   Solutions:");
            System.err.println("   1. Set System Environment Variable: OPENAI_API_KEY");
            System.err.println("   2. Or uncomment hardcode line in ChatBotServlet.init() for testing");
            gptService = null;  // Explicitly set to null
        } else {
            System.out.println("✅ OpenAI API Key loaded successfully");
            System.out.println("   Key length: " + apiKey.length() + " characters");
            System.out.println("   Key preview: " + apiKey.substring(0, Math.min(20, apiKey.length())) + "..." + apiKey.substring(Math.max(0, apiKey.length() - 4)));
            System.out.println("   Key format valid: " + apiKey.startsWith("sk-"));
            
            try {
                gptService = new GPTService(apiKey);
                System.out.println("✅ GPTService initialized successfully");
            } catch (Exception e) {
                System.err.println("❌ Failed to initialize GPTService: " + e.getMessage());
                e.printStackTrace();
                gptService = null;
            }
        }
        
        System.out.println("🏁 ChatBotServlet initialization completed. GPTService: " + (gptService != null ? "READY" : "NOT READY"));
    }
    
    /**
     * GET: Check chatbot status
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        JSONObject status = new JSONObject();
        status.put("status", "active");
        status.put("configured", gptService != null && gptService.isConfigured());
        status.put("model", "gpt-3.5-turbo");
        status.put("message", "LiteFlow ChatBot API is ready");
        
        response.getWriter().write(status.toString());
    }
    
    /**
     * POST: Send message and get response
     * Request JSON: { "message": "user message", "systemPrompt": "optional" }
     * Response JSON: { "success": true, "response": "GPT response", "error": null }
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        JSONObject jsonResponse = new JSONObject();
        
        try {
            // Check if GPT service is configured
            if (gptService == null || !gptService.isConfigured()) {
                jsonResponse.put("success", false);
                jsonResponse.put("error", "ChatBot is not configured. Please contact administrator.");
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.getWriter().write(jsonResponse.toString());
                return;
            }
            
            // Read request body
            StringBuilder requestBody = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }
            
            JSONObject requestJson = new JSONObject(requestBody.toString());
            String userMessage = requestJson.optString("message", "");
            String systemPrompt = requestJson.optString("systemPrompt", null);
            
            // Validate message
            if (userMessage.isEmpty()) {
                jsonResponse.put("success", false);
                jsonResponse.put("error", "Message is required");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(jsonResponse.toString());
                return;
            }
            
            // Get user ID from session for PO creation
            java.util.UUID userId = null;
            try {
                jakarta.servlet.http.HttpSession session = request.getSession(false);
                if (session != null) {
                    String userLogin = (String) session.getAttribute("UserLogin");
                    if (userLogin != null && !userLogin.isEmpty()) {
                        userId = java.util.UUID.fromString(userLogin);
                        System.out.println("👤 User ID from session: " + userId);
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠️ Could not get user ID from session: " + e.getMessage());
            }
            
            System.out.println("💬 ChatBot request from: " + request.getRemoteAddr());
            System.out.println("   Message: " + userMessage);
            
            // Get GPT response with intelligent features (demand forecasting, stock alerts, etc.)
            String gptResponse;
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                // Custom system prompt provided
                gptResponse = gptService.chat(userMessage, systemPrompt);
            } else {
                // Use intelligent chat with demand forecasting and PO creation support
                gptResponse = gptService.chatWithIntelligence(userMessage, userId);
            }
            
            // Build success response
            jsonResponse.put("success", true);
            jsonResponse.put("response", gptResponse);
            jsonResponse.put("timestamp", System.currentTimeMillis());
            
            System.out.println("✅ ChatBot response sent successfully");
            
        } catch (Exception e) {
            System.err.println("❌ ChatBot error: " + e.getMessage());
            e.printStackTrace();
            
            jsonResponse.put("success", false);
            jsonResponse.put("error", "Đã xảy ra lỗi khi xử lý tin nhắn. Vui lòng thử lại.");
            jsonResponse.put("details", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        response.getWriter().write(jsonResponse.toString());
    }
    
    /**
     * Find project root by locating pom.xml file
     * @return Path to project root, or user.dir if not found
     */
    private String findProjectRoot() {
        String userDir = System.getProperty("user.dir");
        if (userDir == null) {
            return null;
        }
        
        // Check current directory first
        File currentDir = new File(userDir);
        File pomFile = new File(currentDir, "pom.xml");
        if (pomFile.exists() && pomFile.isFile()) {
            return currentDir.getAbsolutePath();
        }
        
        // Walk up directories to find pom.xml (max 5 levels up)
        File dir = currentDir;
        int maxLevels = 5;
        int level = 0;
        while (dir != null && dir.getParentFile() != null && level < maxLevels) {
            dir = dir.getParentFile();
            pomFile = new File(dir, "pom.xml");
            if (pomFile.exists() && pomFile.isFile()) {
                return dir.getAbsolutePath();
            }
            level++;
        }
        
        // Fallback to user.dir if pom.xml not found
        return userDir;
    }
    
    /**
     * Get OpenAI API Key from environment
     * Priority: 1. .env file (development), 2. System environment variable (production)
     * 
     * SECURITY: No hardcoded API keys allowed
     */
    private String getOpenAIApiKey() {
        String apiKey = null;
        
        // Priority 1: Load from .env file (recommended for development)
        try {
            // Find project root dynamically
            String projectRoot = findProjectRoot();
            
            // Try multiple locations for .env file
            java.util.List<String> pathList = new java.util.ArrayList<>();
            
            // Add servlet context paths (for deployed apps)
            String servletRoot = getServletContext().getRealPath("/");
            if (servletRoot != null) {
                pathList.add(servletRoot);
                String webInfPath = getServletContext().getRealPath("/WEB-INF/");
                if (webInfPath != null) {
                    pathList.add(webInfPath);
                }
            }
            
            // Add Tomcat webapps path
            String catalinaBase = System.getProperty("catalina.base");
            if (catalinaBase != null) {
                pathList.add(catalinaBase + "/webapps/LiteFlow/");
            }
            
            // Add project root (where pom.xml is located)
            if (projectRoot != null) {
                pathList.add(projectRoot);
            }
            
            // Add current working directory as fallback
            String userDir = System.getProperty("user.dir");
            if (userDir != null && !pathList.contains(userDir)) {
                pathList.add(userDir);
            }
            
            String[] possiblePaths = pathList.toArray(new String[0]);
            
            for (String path : possiblePaths) {
                if (path == null) continue;
                
                try {
                    System.out.println("🔍 Trying .env at: " + path);
                    Dotenv dotenv = Dotenv.configure()
                        .directory(path)
                        .ignoreIfMissing()
                        .load();
                    
                    apiKey = dotenv.get("OPENAI_API_KEY");
                    if (apiKey != null && !apiKey.isEmpty()) {
                        System.out.println("✅ Loaded OPENAI_API_KEY from .env file at: " + path);
                        return apiKey;
                    }
                } catch (Exception e) {
                    // Try next path
                }
            }
            System.out.println("⚠️ .env file not found in any location");
        } catch (Exception e) {
            System.out.println("ℹ️ .env loading failed, checking system environment...");
        }
        
        // Priority 2: System environment variable (recommended for production)
        apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey != null && !apiKey.isEmpty()) {
            System.out.println("✅ Loaded OPENAI_API_KEY from system environment");
            return apiKey;
        }
        
        // No API key found - return null
        System.err.println("❌ OPENAI_API_KEY not found!");
        System.err.println("   Please set it in one of these locations:");
        System.err.println("   1. .env file in project root (same level as pom.xml)");
        System.err.println("   2. System environment variable (for production)");
        System.err.println("   ");
        System.err.println("   Format in .env file: OPENAI_API_KEY=sk-your-key-here");
        
        return null;
    }
}

