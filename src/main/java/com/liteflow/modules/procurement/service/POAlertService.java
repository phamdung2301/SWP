package com.liteflow.modules.procurement.service;

import com.liteflow.modules.core.dao.UserAlertPreferenceDAO;
import com.liteflow.modules.procurement.dao.POAlertNotificationDAO;
import com.liteflow.modules.procurement.dao.PurchaseOrderDAO;
import com.liteflow.modules.procurement.dao.SupplierDAO;
import com.liteflow.modules.core.model.UserAlertPreference;
import com.liteflow.modules.procurement.model.POAlertNotification;
import com.liteflow.modules.procurement.model.PurchaseOrder;
import com.liteflow.modules.core.service.NotificationService;
import com.liteflow.modules.analytics.service.AIAgentConfigService;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Service for sending Telegram notifications when new Purchase Orders are created
 */
public class POAlertService {
    
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("LiteFlowPU");
    
    private final POAlertNotificationDAO notificationDAO;
    private final PurchaseOrderDAO poDAO;
    private final SupplierDAO supplierDAO;
    private final UserAlertPreferenceDAO userAlertPreferenceDAO;
    private final NotificationService notificationService;
    private final AIAgentConfigService configService;
    
    public POAlertService() {
        this.notificationDAO = new POAlertNotificationDAO();
        this.poDAO = new PurchaseOrderDAO();
        this.supplierDAO = new SupplierDAO();
        this.userAlertPreferenceDAO = new UserAlertPreferenceDAO();
        this.notificationService = new NotificationService();
        this.configService = new AIAgentConfigService();
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
        System.out.println("🔍 [POAlert] getTelegramBotToken() called");
        
        // Priority 1: Load from .env file (try multiple locations)
        try {
            System.out.println("🔍 [POAlert] Attempting to load token from .env file...");
            
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
            System.out.println("🔍 [POAlert] Total paths to check: " + pathList.size());
            
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
            System.out.println("⚠️ [POAlert] Could not load token from .env file in any of the " + pathList.size() + " checked paths");
            System.out.println("📋 Paths checked:");
            for (int i = 0; i < pathList.size(); i++) {
                System.out.println("   " + (i+1) + ". " + pathList.get(i));
            }
        } catch (Exception e) {
            System.err.println("❌ [POAlert] Exception while trying to load .env: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Priority 2: System environment variable
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🔍 [POAlert] Attempting to load token from system environment variable...");
        String token = System.getenv("TELEGRAM_BOT_TOKEN");
        if (token != null && !token.isEmpty()) {
            token = token.trim();
            System.out.println("✅ [POAlert] Token loaded from system environment variable");
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
            System.out.println("⚠️ [POAlert] TELEGRAM_BOT_TOKEN not found in system environment or is empty");
        }
        
        System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.err.println("❌ [POAlert] Telegram bot token not found in .env file or system environment");
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
        System.out.println("🔍 [POAlert] findProjectRoot() - Starting search...");
        
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
                System.out.println("🔍 [POAlert] Strategy 1 - Class location: " + classFile.getAbsolutePath());
                
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
                        System.out.println("✅ [POAlert] Found project root via class location: " + dir.getAbsolutePath());
                        return dir.getAbsolutePath();
                    }
                    dir = dir.getParentFile();
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ [POAlert] Strategy 1 (class location) failed: " + e.getMessage());
        }
        
        // Strategy 2: Try Tomcat webapps path
        try {
            String catalinaBase = System.getProperty("catalina.base");
            if (catalinaBase != null) {
                File webappsDir = new File(catalinaBase, "webapps/LiteFlow");
                System.out.println("🔍 [POAlert] Strategy 2 - Checking Tomcat webapps: " + webappsDir.getAbsolutePath());
                
                if (webappsDir.exists() && webappsDir.isDirectory()) {
                    // Walk up from webapps to find project root
                    File dir = webappsDir;
                    for (int i = 0; i < 5 && dir != null; i++) {
                        File pomFile = new File(dir, "pom.xml");
                        if (pomFile.exists() && pomFile.isFile()) {
                            System.out.println("✅ [POAlert] Found project root via Tomcat path: " + dir.getAbsolutePath());
                            return dir.getAbsolutePath();
                        }
                        dir = dir.getParentFile();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ [POAlert] Strategy 2 (Tomcat path) failed: " + e.getMessage());
        }
        
        // Strategy 3: Try current working directory and walk up
        try {
            String userDir = System.getProperty("user.dir");
            if (userDir != null) {
                File currentDir = new File(userDir);
                System.out.println("🔍 [POAlert] Strategy 3 - Checking user.dir: " + currentDir.getAbsolutePath());
                
                File pomFile = new File(currentDir, "pom.xml");
                if (pomFile.exists() && pomFile.isFile()) {
                    System.out.println("✅ [POAlert] Found project root via user.dir: " + currentDir.getAbsolutePath());
                    return currentDir.getAbsolutePath();
                }
                
                // Walk up directories (max 10 levels)
                File dir = currentDir;
                for (int i = 0; i < 10 && dir != null; i++) {
                    dir = dir.getParentFile();
                    if (dir == null) break;
                    
                    pomFile = new File(dir, "pom.xml");
                    if (pomFile.exists() && pomFile.isFile()) {
                        System.out.println("✅ [POAlert] Found project root via user.dir walk-up: " + dir.getAbsolutePath());
                        return dir.getAbsolutePath();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ [POAlert] Strategy 3 (user.dir) failed: " + e.getMessage());
        }
        
        System.err.println("❌ [POAlert] Could not find project root using any strategy");
        return null;
    }
    
    /**
     * Send Telegram notification for new PO creation
     * This method should be called asynchronously after PO creation
     * @param poid PO ID of the newly created purchase order
     * @param targetUserId User ID to send notification to (null = send to all users with Telegram enabled)
     */
    public void sendPOCreationNotification(UUID poid, UUID targetUserId) {
        // Check if Telegram notifications are enabled globally
        if (!isTelegramNotificationsEnabled()) {
            System.out.println("ℹ️ Telegram notifications are disabled globally (notification.enable_telegram = false). Skipping PO creation notification for PO: " + poid);
            return;
        }
        
        System.out.println("🔔 Initiating PO notification for POID: " + poid);
        System.out.println("🔔 Target user ID: " + (targetUserId != null ? targetUserId : "null (all users)"));
        
        // Run asynchronously to not block PO creation response
        CompletableFuture.runAsync(() -> {
            EntityManager em = null;
            try {
                em = emf.createEntityManager();
                
                System.out.println("🔔 [Async] Checking PO notification for POID: " + poid);
                
                // Get PO details
                PurchaseOrder po = poDAO.findById(poid);
                if (po == null) {
                    System.err.println("⚠️ PO not found: " + poid);
                    return;
                }
                
                // Get supplier name
                String supplierName = "Nhà cung cấp";
                if (po.getSupplierID() != null) {
                    try {
                        com.liteflow.modules.procurement.model.Supplier supplier = supplierDAO.findById(po.getSupplierID());
                        if (supplier != null && supplier.getName() != null) {
                            supplierName = supplier.getName();
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ Error getting supplier: " + e.getMessage());
                    }
                }
                
                // Get list of users to notify
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println("🔍 Getting users to notify...");
                System.out.println("  Target user ID: " + (targetUserId != null ? targetUserId : "null (all users)"));
                List<UUID> userIdsToNotify = getUsersToNotify(targetUserId);
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println("🔍 Found " + userIdsToNotify.size() + " users to notify");
                
                if (userIdsToNotify.isEmpty()) {
                    System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    System.err.println("⚠️ No users configured for Telegram notifications - Check UserAlertPreferences table");
                    System.err.println("💡 Make sure:");
                    System.err.println("   1. UserAlertPreferences table has records");
                    System.err.println("   2. EnableTelegram = 1");
                    System.err.println("   3. EnableNotifications = 1");
                    System.err.println("   4. TelegramUserID is set (not null/empty)");
                    System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    return;
                }
                
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println("📋 Processing " + userIdsToNotify.size() + " users...");
                
                int successCount = 0;
                int failCount = 0;
                int skipCount = 0;
                
                for (UUID userId : userIdsToNotify) {
                    EntityManager notificationEm = null;
                    try {
                        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                        System.out.println("👤 Processing User: " + userId);
                        
                        // Check if notification has already been sent for this user + PO
                        if (notificationDAO.hasNotificationBeenSent(userId, poid)) {
                            System.out.println("⏭️ Notification already sent for this user + PO, skipping");
                            skipCount++;
                            continue;
                        }
                        
                        // Get user's Telegram Chat ID
                        System.out.println("🔍 Checking User Telegram settings...");
                        UserAlertPreference preference = userAlertPreferenceDAO.getByUserId(userId);
                        if (preference == null) {
                            System.err.println("❌ User does not have UserAlertPreference configured");
                            failCount++;
                            continue;
                        }
                        
                        System.out.println("  📋 UserAlertPreference found:");
                        System.out.println("    - EnableNotifications: " + preference.getEnableNotifications());
                        System.out.println("    - EnableTelegram: " + preference.getEnableTelegram());
                        System.out.println("    - TelegramUserID: " + (preference.getTelegramUserID() != null ? preference.getTelegramUserID() : "null"));
                        
                        if (preference.getTelegramUserID() == null || preference.getTelegramUserID().isEmpty()) {
                            System.err.println("❌ User does not have Telegram Chat ID configured");
                            failCount++;
                            continue;
                        }
                        
                        if (preference.getEnableTelegram() == null || !preference.getEnableTelegram()) {
                            System.err.println("❌ User has Telegram notifications disabled");
                            failCount++;
                            continue;
                        }
                        
                        String chatId = preference.getTelegramUserID();
                        System.out.println("✅ User configuration valid:");
                        System.out.println("    - Telegram enabled: Yes");
                        System.out.println("    - Chat ID: " + chatId);
                        
                        // Build message
                        String title = "📋 ĐƠN ĐẶT HÀNG MỚI";
                        
                        // Format amount
                        DecimalFormat df = new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(Locale.US));
                        String formattedAmount = df.format(po.getTotalAmount() != null ? po.getTotalAmount() : 0);
                        
                        String message = String.format(
                            "Đơn đặt hàng mới đã được tạo:\n\n" +
                            "<b>Mã đơn:</b> %s\n" +
                            "<b>Nhà cung cấp:</b> %s\n" +
                            "<b>Tổng tiền:</b> %s VNĐ\n" +
                            "<b>Ngày giao dự kiến:</b> %s\n" +
                            "<b>Trạng thái:</b> %s",
                            poid.toString().substring(0, 8).toUpperCase(),
                            escapeHtmlForTelegram(supplierName),
                            formattedAmount,
                            po.getExpectedDelivery() != null 
                                ? po.getExpectedDelivery().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                                : "Chưa xác định",
                            po.getStatus() != null ? po.getStatus() : "PENDING"
                        );
                        
                        if (po.getNotes() != null && !po.getNotes().trim().isEmpty()) {
                            message += "\n<b>Ghi chú:</b> " + escapeHtmlForTelegram(po.getNotes());
                        }
                        
                        String priority = "HIGH";
                        
                        // Send Telegram message
                        System.out.println("🔍 [POAlert] Attempting to send Telegram notification to Chat ID: " + chatId + " for PO: " + poid);
                        System.out.println("🔍 [POAlert] Title: " + title);
                        System.out.println("🔍 [POAlert] Message length: " + (message != null ? message.length() : 0) + " characters");
                        System.out.println("🔍 [POAlert] Priority: " + priority);
                        
                        System.out.println("🔍 [POAlert] Loading Telegram bot token...");
                        String telegramToken = getTelegramBotToken();
                        if (telegramToken == null) {
                            System.err.println("❌ [POAlert] Telegram bot token not configured. Cannot send notification.");
                            System.err.println("❌ [POAlert] Checked .env file and system environment variable TELEGRAM_BOT_TOKEN");
                            continue;
                        }
                        System.out.println("✅ [POAlert] Telegram bot token loaded (length: " + telegramToken.length() + " characters)");
                        System.out.println("🔍 [POAlert] Token format check: " + (telegramToken.contains(":") ? "Valid format (contains ':')" : "Warning: May be invalid (no ':')"));
                        
                        System.out.println("🔍 [POAlert] Calling sendTelegramToUser with:");
                        System.out.println("  - Chat ID: " + chatId);
                        System.out.println("  - Title: " + title);
                        System.out.println("  - Message length: " + (message != null ? message.length() : 0));
                        System.out.println("  - Priority: " + priority);
                        System.out.println("  - Token length: " + (telegramToken != null ? telegramToken.length() : 0));
                        
                        boolean sent = false;
                        try {
                            sent = notificationService.sendTelegramToUser(chatId, title, message, priority, telegramToken);
                            System.out.println("🔍 [POAlert] Send result: " + (sent ? "✅ SUCCESS" : "❌ FAILED"));
                        } catch (Exception e) {
                            System.err.println("❌ [POAlert] Exception while sending Telegram: " + e.getMessage());
                            e.printStackTrace();
                            sent = false;
                        }
                        
                        if (sent) {
                            // Mark notification as sent (use separate EntityManager for transaction)
                            notificationEm = emf.createEntityManager();
                            notificationEm.getTransaction().begin();
                            
                            POAlertNotification notification = new POAlertNotification();
                            notification.setPoid(poid);
                            notification.setUserId(userId);
                            notification.setMessageSent(message);
                            
                            notificationEm.persist(notification);
                            notificationEm.getTransaction().commit();
                            
                            System.out.println("✅ PO notification sent successfully to User " + userId + " | PO: " + poid);
                            successCount++;
                        } else {
                            System.err.println("❌ Failed to send PO notification to User " + userId);
                            failCount++;
                        }
                        
                    } catch (Exception e) {
                        if (notificationEm != null && notificationEm.getTransaction().isActive()) {
                            notificationEm.getTransaction().rollback();
                        }
                        System.err.println("❌ Error sending PO notification to user " + userId + ": " + e.getMessage());
                        System.err.println("   Exception type: " + e.getClass().getName());
                        e.printStackTrace();
                        failCount++;
                    } finally {
                        if (notificationEm != null && notificationEm.isOpen()) {
                            notificationEm.close();
                        }
                    }
                }
                
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println("📊 PO notification summary for POID: " + poid);
                System.out.println("  ✅ Success: " + successCount);
                System.out.println("  ❌ Failed: " + failCount);
                System.out.println("  ⏭️ Skipped: " + skipCount);
                System.out.println("  📋 Total processed: " + userIdsToNotify.size());
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                
            } catch (Exception e) {
                System.err.println("❌ [Async] Error in PO notification check: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (em != null && em.isOpen()) {
                    em.close();
                }
            }
        }).exceptionally(ex -> {
            // Handle any exceptions that weren't caught in the async task
            System.err.println("❌ [CompletableFuture] Unhandled exception in PO notification: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        });
        
        System.out.println("🔔 PO notification task submitted for POID: " + poid);
    }
    
    /**
     * Send Telegram notification for PO status update (approve/reject)
     * @param poid PO ID
     * @param newStatus New status (APPROVED, REJECTED)
     * @param approverId User ID who approved/rejected
     */
    public void sendPOStatusUpdateNotification(UUID poid, String newStatus, UUID approverId) {
        // Check if Telegram notifications are enabled globally
        if (!isTelegramNotificationsEnabled()) {
            System.out.println("ℹ️ Telegram notifications are disabled globally (notification.enable_telegram = false). Skipping PO status update notification for PO: " + poid);
            return;
        }
        
        // Run asynchronously to not block approval/rejection response
        CompletableFuture.runAsync(() -> {
            EntityManager em = null;
            try {
                em = emf.createEntityManager();
                
                System.out.println("🔔 Sending PO status update notification for POID: " + poid + " | Status: " + newStatus);
                
                // Get PO details
                PurchaseOrder po = poDAO.findById(poid);
                if (po == null) {
                    System.err.println("⚠️ PO not found: " + poid);
                    return;
                }
                
                // Get supplier name
                String supplierName = "Nhà cung cấp";
                if (po.getSupplierID() != null) {
                    try {
                        com.liteflow.modules.procurement.model.Supplier supplier = supplierDAO.findById(po.getSupplierID());
                        if (supplier != null && supplier.getName() != null) {
                            supplierName = supplier.getName();
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ Error getting supplier: " + e.getMessage());
                    }
                }
                
                // Get list of users to notify (same users who received creation notification)
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println("🔍 Getting users to notify for status update...");
                List<UUID> userIdsToNotify = getUsersToNotify(null); // Notify all users with Telegram enabled
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println("🔍 Found " + userIdsToNotify.size() + " users to notify");
                
                if (userIdsToNotify.isEmpty()) {
                    System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    System.err.println("⚠️ No users configured for Telegram notifications");
                    System.err.println("💡 Check UserAlertPreferences table configuration");
                    System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    return;
                }
                
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println("📋 Processing " + userIdsToNotify.size() + " users for status update...");
                
                int successCount = 0;
                int failCount = 0;
                
                for (UUID userId : userIdsToNotify) {
                    try {
                        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                        System.out.println("👤 Processing User: " + userId);
                        
                        // Get user's Telegram Chat ID
                        System.out.println("🔍 Checking User Telegram settings...");
                        UserAlertPreference preference = userAlertPreferenceDAO.getByUserId(userId);
                        if (preference == null) {
                            System.err.println("❌ User does not have UserAlertPreference configured");
                            failCount++;
                            continue;
                        }
                        
                        System.out.println("  📋 UserAlertPreference found:");
                        System.out.println("    - EnableNotifications: " + preference.getEnableNotifications());
                        System.out.println("    - EnableTelegram: " + preference.getEnableTelegram());
                        System.out.println("    - TelegramUserID: " + (preference.getTelegramUserID() != null ? preference.getTelegramUserID() : "null"));
                        
                        if (preference.getTelegramUserID() == null || preference.getTelegramUserID().isEmpty()) {
                            System.err.println("❌ User does not have Telegram Chat ID configured");
                            failCount++;
                            continue;
                        }
                        
                        if (preference.getEnableTelegram() == null || !preference.getEnableTelegram()) {
                            System.err.println("❌ User has Telegram notifications disabled");
                            failCount++;
                            continue;
                        }
                        
                        String chatId = preference.getTelegramUserID();
                        System.out.println("✅ User configuration valid:");
                        System.out.println("    - Telegram enabled: Yes");
                        System.out.println("    - Chat ID: " + chatId);
                        
                        // Build update message
                        String title;
                        String message;
                        String priority;
                        
                        if ("APPROVED".equals(newStatus)) {
                            title = "✅ ĐƠN ĐẶT HÀNG ĐÃ ĐƯỢC DUYỆT";
                            priority = "MEDIUM";
                        } else if ("REJECTED".equals(newStatus)) {
                            title = "❌ ĐƠN ĐẶT HÀNG ĐÃ BỊ TỪ CHỐI";
                            priority = "HIGH";
                        } else {
                            title = "📋 CẬP NHẬT ĐƠN ĐẶT HÀNG";
                            priority = "MEDIUM";
                        }
                        
                        // Format amount
                        DecimalFormat df = new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(Locale.US));
                        String formattedAmount = df.format(po.getTotalAmount() != null ? po.getTotalAmount() : 0);
                        
                        // Build message
                        message = String.format(
                            "Đơn đặt hàng đã được cập nhật trạng thái:\n\n" +
                            "<b>Mã đơn:</b> %s\n" +
                            "<b>Nhà cung cấp:</b> %s\n" +
                            "<b>Tổng tiền:</b> %s VNĐ\n" +
                            "<b>Trạng thái cũ:</b> %s\n" +
                            "<b>Trạng thái mới:</b> <b>%s</b>\n",
                            poid.toString().substring(0, 8).toUpperCase(),
                            escapeHtmlForTelegram(supplierName),
                            formattedAmount,
                            "PENDING", // Trạng thái cũ
                            newStatus
                        );
                        
                        if (po.getApprovedAt() != null) {
                            message += "<b>Thời gian:</b> " + 
                                po.getApprovedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n";
                        }
                        
                        if (po.getNotes() != null && !po.getNotes().trim().isEmpty()) {
                            message += "<b>Ghi chú:</b> " + escapeHtmlForTelegram(po.getNotes());
                        }
                        
                        // Send Telegram message (new message, not edit)
                        System.out.println("🔍 [POAlert] Attempting to send PO status update notification to Chat ID: " + chatId);
                        System.out.println("🔍 [POAlert] Title: " + title);
                        System.out.println("🔍 [POAlert] New Status: " + newStatus);
                        System.out.println("🔍 [POAlert] Message length: " + (message != null ? message.length() : 0) + " characters");
                        System.out.println("🔍 [POAlert] Priority: " + priority);
                        
                        System.out.println("🔍 [POAlert] Loading Telegram bot token...");
                        String telegramToken = getTelegramBotToken();
                        if (telegramToken == null) {
                            System.err.println("❌ [POAlert] Telegram bot token not configured. Cannot send notification.");
                            System.err.println("❌ [POAlert] Checked .env file and system environment variable TELEGRAM_BOT_TOKEN");
                            continue;
                        }
                        System.out.println("✅ [POAlert] Telegram bot token loaded (length: " + telegramToken.length() + " characters)");
                        
                        System.out.println("🔍 [POAlert] Calling sendTelegramToUser for status update with:");
                        System.out.println("  - Chat ID: " + chatId);
                        System.out.println("  - Title: " + title);
                        System.out.println("  - Message length: " + (message != null ? message.length() : 0));
                        System.out.println("  - Priority: " + priority);
                        System.out.println("  - Token length: " + (telegramToken != null ? telegramToken.length() : 0));
                        
                        boolean sent = false;
                        try {
                            sent = notificationService.sendTelegramToUser(chatId, title, message, priority, telegramToken);
                            System.out.println("🔍 [POAlert] Send result: " + (sent ? "✅ SUCCESS" : "❌ FAILED"));
                        } catch (Exception e) {
                            System.err.println("❌ [POAlert] Exception while sending Telegram: " + e.getMessage());
                            e.printStackTrace();
                            sent = false;
                        }
                        
                        if (sent) {
                            System.out.println("✅ PO status update notification sent successfully to User " + userId + " | PO: " + poid + " | Status: " + newStatus);
                            successCount++;
                        } else {
                            System.err.println("❌ Failed to send PO status update notification to User " + userId);
                            failCount++;
                        }
                        
                    } catch (Exception e) {
                        System.err.println("❌ Error sending PO status update to user " + userId + ": " + e.getMessage());
                        System.err.println("   Exception type: " + e.getClass().getName());
                        e.printStackTrace();
                        failCount++;
                    }
                }
                
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println("📊 PO status update notification summary for POID: " + poid);
                System.out.println("  ✅ Success: " + successCount);
                System.out.println("  ❌ Failed: " + failCount);
                System.out.println("  📋 Total processed: " + userIdsToNotify.size());
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                
            } catch (Exception e) {
                System.err.println("❌ Error in PO status update notification: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (em != null && em.isOpen()) {
                    em.close();
                }
            }
        });
    }
    
    /**
     * Get list of user IDs to notify
     * If targetUserId is provided, only notify that user
     * Otherwise, notify all users with Telegram enabled
     */
    private List<UUID> getUsersToNotify(UUID targetUserId) {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🔍 getUsersToNotify() called");
        
        if (targetUserId != null) {
            System.out.println("  Target user specified: " + targetUserId);
            // Still check if this user has Telegram enabled and Chat ID
            UserAlertPreference preference = userAlertPreferenceDAO.getByUserId(targetUserId);
            if (preference != null) {
                System.out.println("  📋 UserAlertPreference found for target user:");
                System.out.println("    - EnableNotifications: " + preference.getEnableNotifications());
                System.out.println("    - EnableTelegram: " + preference.getEnableTelegram());
                System.out.println("    - TelegramUserID: " + (preference.getTelegramUserID() != null ? preference.getTelegramUserID() : "null"));
                
                if (Boolean.TRUE.equals(preference.getEnableTelegram()) && 
                    Boolean.TRUE.equals(preference.getEnableNotifications()) &&
                    preference.getTelegramUserID() != null && 
                    !preference.getTelegramUserID().isEmpty()) {
                    System.out.println("  ✅ Target user has Telegram enabled with Chat ID");
                    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    return List.of(targetUserId);
                } else {
                    System.err.println("  ⚠️ Target user does not have Telegram enabled or Chat ID - will try all users");
                    System.out.println("    Details:");
                    System.out.println("      - EnableTelegram: " + preference.getEnableTelegram());
                    System.out.println("      - EnableNotifications: " + preference.getEnableNotifications());
                    System.out.println("      - Has TelegramUserID: " + (preference.getTelegramUserID() != null && !preference.getTelegramUserID().isEmpty()));
                }
            } else {
                System.err.println("  ⚠️ Target user does not have UserAlertPreference - will try all users");
            }
        }
        
        // Get all users with Telegram enabled
        System.out.println("  Getting all users with Telegram enabled...");
        List<UserAlertPreference> preferences = userAlertPreferenceDAO.getUsersWithTelegramEnabled();
        System.out.println("  Found " + preferences.size() + " users with Telegram enabled in database");
        
        if (preferences.isEmpty()) {
            System.err.println("  ⚠️ No users found with Telegram enabled in database!");
            System.err.println("  💡 Check UserAlertPreferences table:");
            System.err.println("     SELECT * FROM UserAlertPreferences WHERE EnableTelegram = 1");
        }
        
        System.out.println("  Filtering users with valid Telegram Chat ID...");
        List<UUID> userIds = new java.util.ArrayList<>();
        for (UserAlertPreference p : preferences) {
            boolean hasChatId = p.getTelegramUserID() != null && !p.getTelegramUserID().isEmpty();
            boolean isEnabled = Boolean.TRUE.equals(p.getEnableTelegram()) && Boolean.TRUE.equals(p.getEnableNotifications());
            
            if (hasChatId && isEnabled) {
                System.out.println("    ✅ User " + p.getUserID() + " - Chat ID: " + p.getTelegramUserID());
                userIds.add(p.getUserID());
            } else {
                System.out.println("    ⚠️ User " + p.getUserID() + " - " + 
                    (!hasChatId ? "No Chat ID" : "") + 
                    (!isEnabled ? " Not enabled" : ""));
            }
        }
        
        System.out.println("  📊 Filtered to " + userIds.size() + " users with valid Telegram configuration");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        return userIds;
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

