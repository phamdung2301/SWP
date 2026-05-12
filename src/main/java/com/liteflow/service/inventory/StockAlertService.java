package com.liteflow.service.inventory;

import com.liteflow.dao.alert.UserAlertPreferenceDAO;
import com.liteflow.dao.inventory.ProductStockDAO;
import com.liteflow.dao.inventory.StockAlertNotificationDAO;
import com.liteflow.model.alert.UserAlertPreference;
import com.liteflow.model.inventory.ProductVariant;
import com.liteflow.model.inventory.StockAlertNotification;
import com.liteflow.service.alert.NotificationService;
import com.liteflow.service.ai.AIAgentConfigService;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service for checking stock levels after payment and sending Telegram notifications
 */
public class StockAlertService {
    
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("LiteFlowPU");
    
    // Default thresholds (fallback if config not found)
    private static final int DEFAULT_WARNING_THRESHOLD = 20;
    private static final int DEFAULT_CRITICAL_THRESHOLD = 10;
    
    private final StockAlertNotificationDAO notificationDAO;
    private final ProductStockDAO productStockDAO;
    private final UserAlertPreferenceDAO userAlertPreferenceDAO;
    private final NotificationService notificationService;
    private final AIAgentConfigService configService;
    
    public StockAlertService() {
        this.notificationDAO = new StockAlertNotificationDAO();
        this.productStockDAO = new ProductStockDAO();
        this.userAlertPreferenceDAO = new UserAlertPreferenceDAO();
        this.notificationService = new NotificationService();
        this.configService = new AIAgentConfigService();
    }
    
    /**
     * Get warning threshold from config or default
     */
    private int getWarningThreshold() {
        return configService.getIntConfig("stock.warning_threshold", DEFAULT_WARNING_THRESHOLD);
    }
    
    /**
     * Get critical threshold from config or default
     */
    private int getCriticalThreshold() {
        return configService.getIntConfig("stock.critical_threshold", DEFAULT_CRITICAL_THRESHOLD);
    }
    
    /**
     * Check if stock alerts are enabled
     */
    private boolean isStockAlertsEnabled() {
        return configService.getBooleanConfig("stock.enable_alerts", true);
    }
    
    /**
     * Check if Telegram notifications are enabled globally
     */
    private boolean isTelegramNotificationsEnabled() {
        return configService.getBooleanConfig("notification.enable_telegram", true);
    }
    
    /**
     * Get Telegram Bot Token from .env file or system environment
     * Priority: 1. .env file at project root, 2. System environment variable
     * @return Telegram bot token, or null if not found
     */
    private String getTelegramBotToken() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🔍 [StockAlert] getTelegramBotToken() called");
        
        // Priority 1: Load from .env file (try multiple locations)
        try {
            System.out.println("🔍 [StockAlert] Attempting to load token from .env file...");
            
            // Build list of possible paths to check
            java.util.List<String> pathList = new java.util.ArrayList<>();
            
            // Strategy 1: Project root (where pom.xml is)
            String projectRoot = findProjectRoot();
            if (projectRoot != null) {
                pathList.add(projectRoot);
                System.out.println("  ✅ Added project root to search paths: " + projectRoot);
            } else {
                System.out.println("  ⚠️ Project root not found");
            }
            
            // Strategy 2: Tomcat webapps path
            String catalinaBase = System.getProperty("catalina.base");
            if (catalinaBase != null) {
                String webappsPath = catalinaBase + "/webapps/LiteFlow";
                pathList.add(webappsPath);
                System.out.println("  ✅ Added Tomcat webapps path: " + webappsPath);
                
                // Also try WEB-INF path
                String webInfPath = catalinaBase + "/webapps/LiteFlow/WEB-INF";
                pathList.add(webInfPath);
                System.out.println("  ✅ Added WEB-INF path: " + webInfPath);
            } else {
                System.out.println("  ⚠️ catalina.base not set");
            }
            
            // Strategy 3: Current working directory
            String userDir = System.getProperty("user.dir");
            if (userDir != null && !pathList.contains(userDir)) {
                pathList.add(userDir);
                System.out.println("  ✅ Added user.dir: " + userDir);
            }
            
            // Strategy 4: Try absolute path from class location
            try {
                String classPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
                if (classPath != null) {
                    // Decode URL encoding
                    classPath = java.net.URLDecoder.decode(classPath, "UTF-8");
                    java.io.File classFile = new java.io.File(classPath);
                    if (classFile.isFile()) {
                        // It's a JAR file, get parent directory
                        classPath = classFile.getParent();
                    }
                    if (classPath != null && !pathList.contains(classPath)) {
                        pathList.add(classPath);
                        System.out.println("  ✅ Added class location path: " + classPath);
                    }
                }
            } catch (Exception e) {
                System.out.println("  ⚠️ Could not get class location: " + e.getMessage());
            }
            
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("🔍 [StockAlert] Total paths to check: " + pathList.size());
            
            // Try each path
            int pathIndex = 0;
            for (String path : pathList) {
                pathIndex++;
                if (path == null) {
                    System.out.println("  [" + pathIndex + "/" + pathList.size() + "] ⚠️ Path is null, skipping");
                    continue;
                }
                
                try {
                    System.out.println("  [" + pathIndex + "/" + pathList.size() + "] 🔍 Trying .env at: " + path);
                    
                    // Check if .env file exists
                    java.io.File envFile = new java.io.File(path, ".env");
                    if (envFile.exists() && envFile.isFile()) {
                        System.out.println("    ✅ .env file exists at this path");
                    } else {
                        System.out.println("    ⚠️ .env file NOT found at this path");
                    }
                    
                    Dotenv dotenv = Dotenv.configure()
                        .directory(path)
                        .ignoreIfMissing()
                        .load();
                    
                    String token = dotenv.get("TELEGRAM_BOT_TOKEN");
                    if (token != null && !token.isEmpty()) {
                        // Clean and validate token
                        token = token.trim();
                        System.out.println("    ✅ Token found in .env file!");
                        System.out.println("    📏 Token length: " + token.length() + " characters");
                        System.out.println("    📝 Token preview: " + (token.length() > 20 ? token.substring(0, 20) + "..." : token));
                        
                        // Validate token format (should contain ':')
                        if (token.contains(":")) {
                            System.out.println("    ✅ Token format valid (contains ':')");
                            String[] parts = token.split(":", 2);
                            System.out.println("    📋 Bot ID: " + parts[0]);
                            System.out.println("    📋 Token part length: " + (parts.length > 1 ? parts[1].length() : 0));
                        } else {
                            System.out.println("    ⚠️ Token format warning: No ':' found (may be invalid)");
                        }
                        
                        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                        return token;
                    } else {
                        System.out.println("    ⚠️ TELEGRAM_BOT_TOKEN not found in .env at this path");
                        System.out.println("    🔍 Checking if .env file has any content...");
                        try {
                            java.io.File envFile2 = new java.io.File(path, ".env");
                            if (envFile2.exists()) {
                                java.util.Scanner scanner = new java.util.Scanner(envFile2);
                                boolean hasContent = false;
                                while (scanner.hasNextLine()) {
                                    String line = scanner.nextLine();
                                    if (line.trim().startsWith("TELEGRAM_BOT_TOKEN")) {
                                        System.out.println("    📝 Found TELEGRAM_BOT_TOKEN line: " + line.substring(0, Math.min(50, line.length())) + "...");
                                        hasContent = true;
                                        break;
                                    }
                                }
                                scanner.close();
                                if (!hasContent) {
                                    System.out.println("    ⚠️ .env file exists but no TELEGRAM_BOT_TOKEN found");
                                }
                            }
                        } catch (Exception e2) {
                            System.out.println("    ⚠️ Could not read .env file: " + e2.getMessage());
                        }
                    }
                } catch (Exception e) {
                    System.out.println("    ❌ Error loading .env from " + path + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("⚠️ [StockAlert] Could not load token from .env file in any of the " + pathList.size() + " checked paths");
            System.out.println("📋 Paths checked:");
            for (int i = 0; i < pathList.size(); i++) {
                System.out.println("   " + (i+1) + ". " + pathList.get(i));
            }
        } catch (Exception e) {
            System.err.println("❌ [StockAlert] Exception while trying to load .env: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Priority 2: System environment variable
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🔍 [StockAlert] Attempting to load token from system environment variable...");
        String token = System.getenv("TELEGRAM_BOT_TOKEN");
        if (token != null && !token.isEmpty()) {
            token = token.trim();
            System.out.println("✅ [StockAlert] Token loaded from system environment variable");
            System.out.println("📏 Token length: " + token.length() + " characters");
            System.out.println("📝 Token preview: " + (token.length() > 20 ? token.substring(0, 20) + "..." : token));
            
            // Validate token format
            if (token.contains(":")) {
                System.out.println("✅ Token format valid (contains ':')");
            } else {
                System.out.println("⚠️ Token format warning: No ':' found");
            }
            
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            return token;
        } else {
            System.out.println("⚠️ [StockAlert] TELEGRAM_BOT_TOKEN not found in system environment or is empty");
        }
        
        System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.err.println("❌ [StockAlert] Telegram bot token not found in .env file or system environment");
        System.err.println("💡 Troubleshooting tips:");
        System.err.println("   1. Check if .env file exists in project root (same level as pom.xml)");
        System.err.println("   2. Verify .env file format: TELEGRAM_BOT_TOKEN=your_token_here");
        System.err.println("   3. Check file permissions");
        System.err.println("   4. Try setting system environment variable: TELEGRAM_BOT_TOKEN");
        System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        return null;
    }
    
    /**
     * Find project root by looking for pom.xml using multiple strategies
     * Similar to ChatBotServlet.findProjectRoot() but adapted for non-servlet context
     */
    private String findProjectRoot() {
        System.out.println("🔍 [StockAlert] findProjectRoot() - Starting search...");
        
        // Strategy 1: Try class location (most reliable for IDE deployments)
        try {
            String classPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            if (classPath != null) {
                // Decode URL encoding
                if (classPath.startsWith("file:")) {
                    classPath = classPath.substring(5);
                }
                // Handle Windows paths
                if (classPath.startsWith("/") && System.getProperty("os.name").toLowerCase().contains("win")) {
                    classPath = classPath.substring(1);
                }
                
                File classFile = new File(java.net.URLDecoder.decode(classPath, "UTF-8"));
                System.out.println("🔍 [StockAlert] Strategy 1 - Class location: " + classFile.getAbsolutePath());
                
                // If it's a JAR file, get the parent directory
                if (classFile.getName().endsWith(".jar") || classFile.getName().endsWith(".war")) {
                    classFile = classFile.getParentFile();
                }
                
                // If it's WEB-INF/classes, go up to webapp root, then to project root
                if (classFile.getName().equals("classes") && classFile.getParentFile() != null && 
                    classFile.getParentFile().getName().equals("WEB-INF")) {
                    classFile = classFile.getParentFile().getParentFile();
                }
                
                // Walk up from class location to find pom.xml
                File dir = classFile;
                for (int i = 0; i < 10 && dir != null; i++) {
                    File pomFile = new File(dir, "pom.xml");
                    if (pomFile.exists() && pomFile.isFile()) {
                        System.out.println("✅ [StockAlert] Found project root via class location: " + dir.getAbsolutePath());
                        return dir.getAbsolutePath();
                    }
                    dir = dir.getParentFile();
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ [StockAlert] Strategy 1 (class location) failed: " + e.getMessage());
        }
        
        // Strategy 2: Try Tomcat webapps path
        try {
            String catalinaBase = System.getProperty("catalina.base");
            if (catalinaBase != null) {
                File webappsDir = new File(catalinaBase, "webapps/LiteFlow");
                System.out.println("🔍 [StockAlert] Strategy 2 - Checking Tomcat webapps: " + webappsDir.getAbsolutePath());
                
                if (webappsDir.exists() && webappsDir.isDirectory()) {
                    // Walk up from webapps to find project root
                    File dir = webappsDir;
                    for (int i = 0; i < 5 && dir != null; i++) {
                        File pomFile = new File(dir, "pom.xml");
                        if (pomFile.exists() && pomFile.isFile()) {
                            System.out.println("✅ [StockAlert] Found project root via Tomcat path: " + dir.getAbsolutePath());
                            return dir.getAbsolutePath();
                        }
                        dir = dir.getParentFile();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ [StockAlert] Strategy 2 (Tomcat path) failed: " + e.getMessage());
        }
        
        // Strategy 3: Try current working directory and walk up
        try {
            String userDir = System.getProperty("user.dir");
            if (userDir != null) {
                File currentDir = new File(userDir);
                System.out.println("🔍 [StockAlert] Strategy 3 - Checking user.dir: " + currentDir.getAbsolutePath());
                
                File pomFile = new File(currentDir, "pom.xml");
                if (pomFile.exists() && pomFile.isFile()) {
                    System.out.println("✅ [StockAlert] Found project root via user.dir: " + currentDir.getAbsolutePath());
                    return currentDir.getAbsolutePath();
                }
                
                // Walk up directories (max 10 levels)
                File dir = currentDir;
                for (int i = 0; i < 10 && dir != null; i++) {
                    dir = dir.getParentFile();
                    if (dir == null) break;
                    
                    pomFile = new File(dir, "pom.xml");
                    if (pomFile.exists() && pomFile.isFile()) {
                        System.out.println("✅ [StockAlert] Found project root via user.dir walk-up: " + dir.getAbsolutePath());
                        return dir.getAbsolutePath();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ [StockAlert] Strategy 3 (user.dir) failed: " + e.getMessage());
        }
        
        System.err.println("❌ [StockAlert] Could not find project root using any strategy");
        return null;
    }
    
    /**
     * Check stock levels for products after payment and send alerts if needed
     * This method should be called asynchronously after payment completion
     * @param orderItems List of items from payment (each item contains variantId and quantity)
     * @param userId User ID to send notification to (null = send to all users with Telegram enabled)
     */
    public void checkAndSendAlertsAfterPayment(List<Map<String, Object>> orderItems, UUID userId) {
        if (orderItems == null || orderItems.isEmpty()) {
            System.out.println("⚠️ No order items to check stock alerts");
            return;
        }
        
        // Check if stock alerts are enabled
        if (!isStockAlertsEnabled()) {
            System.out.println("ℹ️ Stock alerts are disabled in configuration");
            return;
        }
        
        // Run asynchronously to not block payment response
        CompletableFuture.runAsync(() -> {
            EntityManager em = null;
            try {
                em = emf.createEntityManager();
                
                System.out.println("🔍 Checking stock levels for " + orderItems.size() + " items after payment...");
                
                for (Map<String, Object> item : orderItems) {
                    try {
                        String variantIdStr = (String) item.get("variantId");
                        if (variantIdStr == null || variantIdStr.isEmpty()) {
                            continue;
                        }
                        
                        UUID productVariantId = UUID.fromString(variantIdStr);
                        
                        // Get current stock level
                        int currentStock = productStockDAO.getStockLevel(productVariantId);
                        
                        System.out.println("📦 ProductVariant: " + productVariantId + " | Current Stock: " + currentStock);
                        
                        // Check if notification should be sent for this variant
                        checkAndSendAlertForVariant(em, productVariantId, currentStock, userId);
                        
                    } catch (Exception e) {
                        System.err.println("❌ Error checking stock alert for item: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                System.out.println("✅ Stock alert check completed");
                
            } catch (Exception e) {
                System.err.println("❌ Error in stock alert check: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (em != null && em.isOpen()) {
                    em.close();
                }
            }
        });
    }
    
    /**
     * Check and send alert for a specific product variant
     */
    private void checkAndSendAlertForVariant(EntityManager em, UUID variantId, int currentStock, UUID targetUserId) {
        try {
            // Get ProductVariant details
            ProductVariant variant = em.find(ProductVariant.class, variantId);
            if (variant == null) {
                System.err.println("⚠️ ProductVariant not found: " + variantId);
                return;
            }
            
            String productName = variant.getProduct().getName();
            String size = variant.getSize();
            
            // Get thresholds from config
            int criticalThreshold = getCriticalThreshold();
            int warningThreshold = getWarningThreshold();
            
            // Determine which thresholds need alerts
            if (currentStock <= criticalThreshold && currentStock > 0) {
                // Critical alert
                sendAlertIfNeeded(em, variantId, productName, size, currentStock, criticalThreshold, targetUserId);
            }
            
            if (currentStock <= warningThreshold && currentStock > criticalThreshold) {
                // Warning alert
                sendAlertIfNeeded(em, variantId, productName, size, currentStock, warningThreshold, targetUserId);
            }
            
            // Reset notification state if stock increased above threshold
            if (currentStock > warningThreshold) {
                notificationDAO.resetNotificationState(variantId, warningThreshold);
            }
            if (currentStock > criticalThreshold) {
                notificationDAO.resetNotificationState(variantId, criticalThreshold);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error checking alert for variant " + variantId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send alert if notification hasn't been sent for this threshold yet
     */
    private void sendAlertIfNeeded(EntityManager em, UUID variantId, String productName, String size, 
                                   int currentStock, int threshold, UUID targetUserId) {
        
        // Check if Telegram notifications are enabled globally
        if (!isTelegramNotificationsEnabled()) {
            System.out.println("ℹ️ Telegram notifications are disabled globally (notification.enable_telegram = false). Skipping Telegram notification.");
            return;
        }
        
        // Get list of users to notify
        List<UUID> userIdsToNotify = getUsersToNotify(targetUserId);
        
        if (userIdsToNotify.isEmpty()) {
            System.out.println("⚠️ No users configured for Telegram notifications");
            return;
        }
        
        for (UUID userId : userIdsToNotify) {
            EntityManager notificationEm = null;
            try {
                // Check if notification has already been sent for this user + variant + threshold
                if (notificationDAO.hasNotificationBeenSent(userId, variantId, threshold)) {
                    System.out.println("⏭️ Notification already sent for User " + userId + 
                                     " | Variant " + variantId + " | Threshold " + threshold);
                    continue;
                }
                
                // Get user's Telegram Chat ID
                UserAlertPreference preference = userAlertPreferenceDAO.getByUserId(userId);
                if (preference == null || preference.getTelegramUserID() == null || 
                    preference.getTelegramUserID().isEmpty()) {
                    System.out.println("⚠️ User " + userId + " does not have Telegram Chat ID configured");
                    continue;
                }
                
                if (preference.getEnableTelegram() == null || !preference.getEnableTelegram()) {
                    System.out.println("⚠️ User " + userId + " has Telegram notifications disabled");
                    continue;
                }
                
                String chatId = preference.getTelegramUserID();
                
                // Build message (không thêm emoji vào title vì sendTelegramToUser đã thêm rồi)
                String title;
                String message;
                String priority;
                
                int criticalThreshold = getCriticalThreshold();
                if (threshold == criticalThreshold) {
                    title = "NGUY HIỂM TỒN KHO";
                    message = String.format(
                        "Sản phẩm <b>%s</b> (Size: <b>%s</b>) chỉ còn <b>%d</b> đơn vị trong kho.\n\n" +
                        "Cần nhập hàng ngay để tránh thiếu hụt!",
                        escapeHtmlForTelegram(productName), escapeHtmlForTelegram(size), currentStock
                    );
                    priority = "CRITICAL";
                } else {
                    title = "CẢNH BÁO TỒN KHO";
                    message = String.format(
                        "Sản phẩm <b>%s</b> (Size: <b>%s</b>) còn <b>%d</b> đơn vị trong kho.\n\n" +
                        "Nên nhập hàng sớm để đảm bảo cung ứng.",
                        escapeHtmlForTelegram(productName), escapeHtmlForTelegram(size), currentStock
                    );
                    priority = "HIGH";
                }
                
                // Get Telegram bot token
                System.out.println("🔍 [StockAlert] Loading Telegram bot token...");
                String telegramToken = getTelegramBotToken();
                if (telegramToken == null) {
                    System.err.println("❌ [StockAlert] Telegram bot token not configured. Cannot send notification.");
                    System.err.println("❌ [StockAlert] Checked .env file and system environment variable TELEGRAM_BOT_TOKEN");
                    continue;
                }
                System.out.println("✅ [StockAlert] Telegram bot token loaded (length: " + telegramToken.length() + " characters)");
                System.out.println("🔍 [StockAlert] Token format check: " + (telegramToken.contains(":") ? "Valid format (contains ':')" : "Warning: May be invalid (no ':')"));
                
                // Send Telegram message
                System.out.println("🔍 [StockAlert] Attempting to send Telegram notification to Chat ID: " + chatId);
                System.out.println("🔍 [StockAlert] Title: " + title);
                System.out.println("🔍 [StockAlert] Message length: " + (message != null ? message.length() : 0) + " characters");
                System.out.println("🔍 [StockAlert] Priority: " + priority);
                System.out.println("🔍 [StockAlert] Calling sendTelegramToUser with:");
                System.out.println("  - Chat ID: " + chatId);
                System.out.println("  - Title: " + title);
                System.out.println("  - Message length: " + (message != null ? message.length() : 0));
                System.out.println("  - Priority: " + priority);
                System.out.println("  - Token length: " + (telegramToken != null ? telegramToken.length() : 0));
                
                boolean sent = false;
                try {
                    sent = notificationService.sendTelegramToUser(chatId, title, message, priority, telegramToken);
                    System.out.println("🔍 [StockAlert] Send result: " + (sent ? "✅ SUCCESS" : "❌ FAILED"));
                } catch (Exception e) {
                    System.err.println("❌ [StockAlert] Exception while sending Telegram: " + e.getMessage());
                    e.printStackTrace();
                    sent = false;
                }
                
                if (sent) {
                    // Mark notification as sent (use separate EntityManager for transaction)
                    notificationEm = emf.createEntityManager();
                    notificationEm.getTransaction().begin();
                    
                    StockAlertNotification notification = new StockAlertNotification();
                    notification.setProductVariant(notificationEm.getReference(ProductVariant.class, variantId));
                    notification.setUserId(userId);
                    notification.setAlertThreshold(threshold);
                    notification.setStockLevel(currentStock);
                    notification.setMessageSent(message);
                    
                    notificationEm.persist(notification);
                    notificationEm.getTransaction().commit();
                    
                    System.out.println("✅ Stock alert sent to User " + userId + 
                                     " | Product: " + productName + " | Stock: " + currentStock + 
                                     " | Threshold: " + threshold);
                } else {
                    System.err.println("❌ Failed to send stock alert to User " + userId);
                }
                
            } catch (Exception e) {
                if (notificationEm != null && notificationEm.getTransaction().isActive()) {
                    notificationEm.getTransaction().rollback();
                }
                System.err.println("❌ Error sending alert to user " + userId + ": " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (notificationEm != null && notificationEm.isOpen()) {
                    notificationEm.close();
                }
            }
        }
    }
    
    /**
     * Get list of user IDs to notify
     * If targetUserId is provided, only notify that user
     * Otherwise, notify all users with Telegram enabled
     */
    private List<UUID> getUsersToNotify(UUID targetUserId) {
        if (targetUserId != null) {
            return List.of(targetUserId);
        }
        
        // Get all users with Telegram enabled
        List<UserAlertPreference> preferences = userAlertPreferenceDAO.getUsersWithTelegramEnabled();
            return preferences.stream()
            .filter(p -> p.getTelegramUserID() != null && !p.getTelegramUserID().isEmpty())
            .map(UserAlertPreference::getUserID)
            .toList();
    }
    
    /**
     * Escape HTML cho Telegram (chỉ escape các ký tự đặc biệt, giữ lại tags HTML hợp lệ)
     */
    private String escapeHtmlForTelegram(String text) {
        if (text == null) return "";
        // Telegram hỗ trợ HTML nhưng cần escape các ký tự đặc biệt
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }
}

