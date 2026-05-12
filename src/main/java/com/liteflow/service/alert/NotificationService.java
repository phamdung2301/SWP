package com.liteflow.service.alert;

import com.liteflow.dao.alert.NotificationChannelDAO;
import com.liteflow.model.alert.NotificationChannel;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Service for sending notifications to multiple channels
 * Supports: Slack, Telegram, Email, In-App
 */
public class NotificationService {
    
    private final NotificationChannelDAO channelDAO;
    
    public NotificationService() {
        this.channelDAO = new NotificationChannelDAO();
    }
    
    /**
     * Send notification to all configured channels
     */
    public boolean sendToAllChannels(String title, String message, String priority) {
        List<NotificationChannel> channels = channelDAO.getAllActive();
        boolean anySuccess = false;
        
        for (NotificationChannel channel : channels) {
            boolean sent = sendToChannel(channel, title, message, priority);
            if (sent) {
                anySuccess = true;
            }
        }
        
        return anySuccess;
    }
    
    /**
     * Send notification to specific channel
     */
    public boolean sendToChannel(NotificationChannel channel, String title, String message, String priority) {
        if (!channel.isConfigured()) {
            System.err.println("❌ Channel not configured: " + channel.getName());
            return false;
        }
        
        if (channel.isRateLimitExceeded()) {
            System.err.println("⚠️ Rate limit exceeded for channel: " + channel.getName());
            channelDAO.recordUsage(channel.getChannelID(), false, "Rate limit exceeded");
            return false;
        }
        
        boolean success = false;
        String errorMessage = null;
        
        try {
            switch (channel.getChannelType().toUpperCase()) {
                case "SLACK":
                    success = sendToSlack(channel, title, message, priority);
                    break;
                case "TELEGRAM":
                    success = sendToTelegram(channel, title, message, priority);
                    break;
                case "EMAIL":
                    success = sendEmail(channel, title, message, priority);
                    break;
                default:
                    errorMessage = "Unsupported channel type: " + channel.getChannelType();
                    System.err.println("❌ " + errorMessage);
            }
        } catch (Exception e) {
            errorMessage = e.getMessage();
            System.err.println("❌ Failed to send to " + channel.getName() + ": " + errorMessage);
            e.printStackTrace();
        }
        
        // Record usage
        channelDAO.recordUsage(channel.getChannelID(), success, errorMessage);
        
        return success;
    }
    
    /**
     * Send to Slack webhook
     */
    private boolean sendToSlack(NotificationChannel channel, String title, String message, String priority) {
        try {
            String webhookUrl = channel.getSlackWebhookURL();
            if (webhookUrl == null || webhookUrl.isEmpty()) {
                System.err.println("❌ Slack webhook URL not configured");
                return false;
            }
            
            // Build Slack message
            JSONObject slackMessage = new JSONObject();
            
            // Add emoji based on priority
            String emoji = getPriorityEmoji(priority);
            slackMessage.put("text", emoji + " *" + title + "*");
            
            // Add blocks for better formatting
            org.json.JSONArray blocks = new org.json.JSONArray();
            
            // Header block
            JSONObject headerBlock = new JSONObject();
            headerBlock.put("type", "header");
            JSONObject headerText = new JSONObject();
            headerText.put("type", "plain_text");
            headerText.put("text", emoji + " " + title);
            headerBlock.put("text", headerText);
            blocks.put(headerBlock);
            
            // Message block
            JSONObject messageBlock = new JSONObject();
            messageBlock.put("type", "section");
            JSONObject messageText = new JSONObject();
            messageText.put("type", "mrkdwn");
            messageText.put("text", message);
            messageBlock.put("text", messageText);
            blocks.put(messageBlock);
            
            // Context block (priority + timestamp)
            JSONObject contextBlock = new JSONObject();
            contextBlock.put("type", "context");
            org.json.JSONArray contextElements = new org.json.JSONArray();
            JSONObject contextText = new JSONObject();
            contextText.put("type", "mrkdwn");
            contextText.put("text", "*Priority:* " + priority + " | " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            contextElements.put(contextText);
            contextBlock.put("elements", contextElements);
            blocks.put(contextBlock);
            
            slackMessage.put("blocks", blocks);
            
            // Send HTTP POST
            URL url = URI.create(webhookUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = slackMessage.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                System.out.println("✅ Slack notification sent: " + title);
                return true;
            } else {
                System.err.println("❌ Slack returned: " + responseCode);
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Slack send failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Send to Telegram bot
     */
    private boolean sendToTelegram(NotificationChannel channel, String title, String message, String priority) {
        try {
            String botToken = channel.getTelegramBotToken();
            String chatId = channel.getTelegramChatID();
            
            if (botToken == null || botToken.isEmpty() || chatId == null || chatId.isEmpty()) {
                System.err.println("❌ Telegram bot token or chat ID not configured");
                return false;
            }
            
            // Build Telegram message (HTML format)
            String emoji = getPriorityEmoji(priority);
            StringBuilder telegramMessage = new StringBuilder();
            telegramMessage.append(emoji).append(" <b>").append(escapeHtml(title)).append("</b>\n\n");
            telegramMessage.append(escapeHtml(message)).append("\n\n");
            telegramMessage.append("<i>Priority: ").append(priority).append(" | ");
            telegramMessage.append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            telegramMessage.append("</i>");
            
            // Build request
            JSONObject requestBody = new JSONObject();
            requestBody.put("chat_id", chatId);
            requestBody.put("text", telegramMessage.toString());
            requestBody.put("parse_mode", "HTML");
            
            // Send HTTP POST
            String apiUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            URL url = URI.create(apiUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Read response
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                System.out.println("✅ Telegram notification sent: " + title);
                return true;
            } else {
                // Read error
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    System.err.println("❌ Telegram error: " + response.toString());
                }
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Telegram send failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Send email (simplified - would use JavaMail in production)
     */
    private boolean sendEmail(NotificationChannel channel, String title, String message, String priority) {
       
        // For now, just log
        System.out.println("📧 Email notification (not implemented yet): " + title);
        System.out.println("   To: " + channel.getEmailRecipients());
        System.out.println("   Message: " + message);
        
        // Return true for demo purposes
        return true;
    }
    
    /**
     * Get emoji based on priority
     */
    private String getPriorityEmoji(String priority) {
        if (priority == null) return "ℹ️";
        
        switch (priority.toUpperCase()) {
            case "CRITICAL":
                return "🚨";
            case "HIGH":
                return "⚠️";
            case "MEDIUM":
                return "ℹ️";
            case "LOW":
                return "📝";
            default:
                return "ℹ️";
        }
    }
    
    /**
     * Escape HTML for Telegram
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }
    
    /**
     * Send notification to specific channel by ID
     */
    public boolean sendToChannelById(UUID channelID, String title, String message, String priority) {
        NotificationChannel channel = channelDAO.getById(channelID);
        if (channel == null) {
            System.err.println("❌ Channel not found: " + channelID);
            return false;
        }
        
        return sendToChannel(channel, title, message, priority);
    }
    
    /**
     * Send to default Slack channel
     */
    public boolean sendToDefaultSlack(String title, String message, String priority) {
        NotificationChannel channel = channelDAO.getDefaultSlackChannel();
        if (channel == null) {
            System.err.println("❌ No default Slack channel configured");
            return false;
        }
        
        return sendToChannel(channel, title, message, priority);
    }
    
    /**
     * Send to default Telegram channel
     */
    public boolean sendToDefaultTelegram(String title, String message, String priority) {
        NotificationChannel channel = channelDAO.getDefaultTelegramChannel();
        if (channel == null) {
            System.err.println("❌ No default Telegram channel configured");
            return false;
        }
        
        return sendToChannel(channel, title, message, priority);
    }
    
    /**
     * Test notification (for setup verification)
     */
    public boolean sendTestNotification(UUID channelID) {
        String title = "🧪 Test Notification";
        String message = "This is a test notification from LiteFlow Alert System.\n" +
                       "If you receive this, the channel is configured correctly! ✅";
        String priority = "LOW";
        
        return sendToChannelById(channelID, title, message, priority);
    }
    
    /**
     * Get all active channels
     */
    public List<NotificationChannel> getAllActiveChannels() {
        return channelDAO.getAllActive();
    }
    
    /**
     * Get channel by ID
     */
    public NotificationChannel getChannelById(UUID channelID) {
        return channelDAO.getById(channelID);
    }
    
    /**
     * Send Telegram message directly to a user's Chat ID
     * This method is used for direct user notifications (not via NotificationChannel)
     * @param chatId Telegram Chat ID
     * @param title Message title
     * @param message Message content
     * @param priority Priority level (CRITICAL, HIGH, MEDIUM, LOW)
     * @param botToken Telegram Bot Token (if null, will try to get from default channel)
     * @return true if sent successfully
     */
    public boolean sendTelegramToUser(String chatId, String title, String message, String priority, String botToken) {
        System.out.println("🔍 [Telegram] Starting sendTelegramToUser()");
        System.out.println("🔍 [Telegram] Chat ID: " + (chatId != null ? chatId : "NULL"));
        System.out.println("🔍 [Telegram] Title: " + (title != null ? title : "NULL"));
        System.out.println("🔍 [Telegram] Priority: " + (priority != null ? priority : "NULL"));
        System.out.println("🔍 [Telegram] Bot Token provided: " + (botToken != null && !botToken.isEmpty() ? "YES (length: " + botToken.length() + ")" : "NO"));
        
        try {
            // Use provided bot token or get from default Telegram channel
            String token = botToken;
            if (token == null || token.isEmpty()) {
                System.out.println("🔍 [Telegram] Bot token not provided, trying to get from default channel...");
                NotificationChannel defaultTelegram = channelDAO.getDefaultTelegramChannel();
                if (defaultTelegram != null && defaultTelegram.getTelegramBotToken() != null) {
                    token = defaultTelegram.getTelegramBotToken();
                    System.out.println("✅ [Telegram] Got token from default channel (length: " + token.length() + ")");
                } else {
                    System.err.println("❌ [Telegram] Telegram bot token not provided and no default Telegram channel configured");
                    if (defaultTelegram == null) {
                        System.err.println("❌ [Telegram] Default Telegram channel is NULL");
                    } else {
                        System.err.println("❌ [Telegram] Default Telegram channel exists but token is NULL");
                    }
                    return false;
                }
            } else {
                System.out.println("✅ [Telegram] Using provided bot token");
            }
            
            // Validate token format (should be like "123456789:ABCdefGHIjklMNOpqrsTUVwxyz")
            if (token != null && !token.contains(":")) {
                System.err.println("⚠️ [Telegram] Token format may be invalid (should contain ':')");
            }
            
            if (chatId == null || chatId.isEmpty()) {
                System.err.println("❌ [Telegram] Telegram chat ID is required");
                return false;
            }
            
            System.out.println("✅ [Telegram] Chat ID validated: " + chatId);
            
            // Build Telegram message (HTML format)
            System.out.println("🔍 [Telegram] Building message...");
            String emoji = getPriorityEmoji(priority);
            StringBuilder telegramMessage = new StringBuilder();
            telegramMessage.append(emoji).append(" <b>").append(escapeHtml(title)).append("</b>\n\n");
            // Message đã được format sẵn với HTML tags, chỉ cần escape các ký tự đặc biệt nhưng giữ lại tags
            // Không escape HTML tags trong message vì chúng đã được format đúng
            telegramMessage.append(message).append("\n\n");
            telegramMessage.append("<i>Priority: ").append(escapeHtml(priority != null ? priority : "MEDIUM")).append(" | ");
            telegramMessage.append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            telegramMessage.append("</i>");
            
            String finalMessage = telegramMessage.toString();
            System.out.println("🔍 [Telegram] Message length: " + finalMessage.length() + " characters");
            
            // Build request
            System.out.println("🔍 [Telegram] Building request body...");
            JSONObject requestBody = new JSONObject();
            requestBody.put("chat_id", chatId);
            requestBody.put("text", finalMessage);
            requestBody.put("parse_mode", "HTML");
            
            String requestBodyStr = requestBody.toString();
            System.out.println("🔍 [Telegram] Request body: " + requestBodyStr.substring(0, Math.min(200, requestBodyStr.length())) + "...");
            
            // Send HTTP POST
            String apiUrl = "https://api.telegram.org/bot" + token + "/sendMessage";
            System.out.println("🔍 [Telegram] API URL: https://api.telegram.org/bot" + (token != null ? token.substring(0, Math.min(10, token.length())) + "..." : "NULL") + "/sendMessage");
            
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000); // 10 seconds
            conn.setReadTimeout(10000); // 10 seconds
            
            System.out.println("🔍 [Telegram] Sending HTTP POST request...");
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBodyStr.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                System.out.println("🔍 [Telegram] Request body sent (" + input.length + " bytes)");
            }
            
            // Read response
            System.out.println("🔍 [Telegram] Reading response...");
            int responseCode = conn.getResponseCode();
            System.out.println("🔍 [Telegram] Response code: " + responseCode);
            
            if (responseCode == 200) {
                // Read success response
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    System.out.println("✅ [Telegram] Success response: " + response.toString());
                }
                System.out.println("✅ [Telegram] Telegram message sent to user: " + chatId);
                return true;
            } else {
                // Read error
                System.err.println("❌ [Telegram] API returned error code: " + responseCode);
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    System.err.println("❌ [Telegram] Error response body: " + response.toString());
                } catch (Exception e) {
                    System.err.println("❌ [Telegram] Could not read error stream: " + e.getMessage());
                }
                return false;
            }
            
        } catch (java.net.SocketTimeoutException e) {
            System.err.println("❌ [Telegram] Connection timeout: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (java.net.UnknownHostException e) {
            System.err.println("❌ [Telegram] Unknown host (network issue): " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (java.io.IOException e) {
            System.err.println("❌ [Telegram] IO error: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ [Telegram] Unexpected error: " + e.getMessage());
            System.err.println("❌ [Telegram] Error class: " + e.getClass().getName());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Overloaded method with default priority
     */
    public boolean sendTelegramToUser(String chatId, String title, String message, String botToken) {
        return sendTelegramToUser(chatId, title, message, "MEDIUM", botToken);
    }
}


