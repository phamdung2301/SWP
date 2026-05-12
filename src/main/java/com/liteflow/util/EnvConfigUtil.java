package com.liteflow.util;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.File;

/**
 * Utility class for reading environment variables from .env file
 * Supports reading MaSoThue (Tax Code) and VAT_RATE
 */
public class EnvConfigUtil {
    
    private static final String DEFAULT_VAT_RATE = "10.0";
    
    /**
     * Find project root by looking for pom.xml using multiple strategies
     */
    private static String findProjectRoot() {
        // Strategy 1: Try class location
        try {
            String classPath = EnvConfigUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            if (classPath != null) {
                if (classPath.startsWith("file:")) {
                    classPath = classPath.substring(5);
                }
                if (classPath.startsWith("/") && System.getProperty("os.name").toLowerCase().contains("win")) {
                    classPath = classPath.substring(1);
                }
                
                File classFile = new File(java.net.URLDecoder.decode(classPath, "UTF-8"));
                
                if (classFile.getName().endsWith(".jar") || classFile.getName().endsWith(".war")) {
                    classFile = classFile.getParentFile();
                }
                
                if (classFile.getName().equals("classes") && classFile.getParentFile() != null && 
                    classFile.getParentFile().getName().equals("WEB-INF")) {
                    classFile = classFile.getParentFile().getParentFile();
                }
                
                File dir = classFile;
                for (int i = 0; i < 10 && dir != null; i++) {
                    File pomFile = new File(dir, "pom.xml");
                    if (pomFile.exists() && pomFile.isFile()) {
                        return dir.getAbsolutePath();
                    }
                    dir = dir.getParentFile();
                }
            }
        } catch (Exception e) {
            // Continue to next strategy
        }
        
        // Strategy 2: Try Tomcat webapps path
        try {
            String catalinaBase = System.getProperty("catalina.base");
            if (catalinaBase != null) {
                File webappsDir = new File(catalinaBase, "webapps/LiteFlow");
                if (webappsDir.exists() && webappsDir.isDirectory()) {
                    File dir = webappsDir;
                    for (int i = 0; i < 5 && dir != null; i++) {
                        File pomFile = new File(dir, "pom.xml");
                        if (pomFile.exists() && pomFile.isFile()) {
                            return dir.getAbsolutePath();
                        }
                        dir = dir.getParentFile();
                    }
                }
            }
        } catch (Exception e) {
            // Continue to next strategy
        }
        
        // Strategy 3: Try current working directory
        try {
            String userDir = System.getProperty("user.dir");
            if (userDir != null) {
                File currentDir = new File(userDir);
                File dir = currentDir;
                for (int i = 0; i < 5 && dir != null; i++) {
                    File pomFile = new File(dir, "pom.xml");
                    if (pomFile.exists() && pomFile.isFile()) {
                        return dir.getAbsolutePath();
                    }
                    dir = dir.getParentFile();
                }
            }
        } catch (Exception e) {
            // Continue
        }
        
        return null;
    }
    
    /**
     * Load .env file from multiple possible locations
     */
    private static Dotenv loadDotenv() {
        java.util.List<String> pathList = new java.util.ArrayList<>();
        
        // Add project root
        String projectRoot = findProjectRoot();
        if (projectRoot != null) {
            pathList.add(projectRoot);
        }
        
        // Add Tomcat webapps path
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null) {
            pathList.add(catalinaBase + "/webapps/LiteFlow");
            pathList.add(catalinaBase + "/webapps/LiteFlow/WEB-INF");
        }
        
        // Add current working directory
        String userDir = System.getProperty("user.dir");
        if (userDir != null && !pathList.contains(userDir)) {
            pathList.add(userDir);
        }
        
        // Try each path
        for (String path : pathList) {
            if (path == null) continue;
            
            try {
                Dotenv dotenv = Dotenv.configure()
                    .directory(path)
                    .ignoreIfMissing()
                    .load();
                
                // Test if we can read any value (check if .env exists)
                if (dotenv.get("MaSoThue") != null || dotenv.get("VAT_RATE") != null || 
                    new File(path, ".env").exists()) {
                    return dotenv;
                }
            } catch (Exception e) {
                // Try next path
            }
        }
        
        // Return empty dotenv if not found
        return Dotenv.configure().ignoreIfMissing().load();
    }
    
    /**
     * Get MaSoThue (Tax Code) from .env file
     * Format in .env: MaSoThue:0123456789
     * @return Tax code string or null if not found
     */
    public static String getMaSoThue() {
        try {
            // Try .env file first
            Dotenv dotenv = loadDotenv();
            String maSoThue = dotenv.get("MaSoThue");
            
            if (maSoThue != null && !maSoThue.trim().isEmpty()) {
                // Remove colon if present (format: "MaSoThue:0123456789")
                maSoThue = maSoThue.trim();
                if (maSoThue.contains(":")) {
                    maSoThue = maSoThue.substring(maSoThue.indexOf(":") + 1).trim();
                }
                System.out.println("✅ [EnvConfigUtil] MaSoThue loaded from .env: " + maSoThue);
                return maSoThue;
            }
            
            // Try system environment variable
            maSoThue = System.getenv("MaSoThue");
            if (maSoThue != null && !maSoThue.trim().isEmpty()) {
                maSoThue = maSoThue.trim();
                if (maSoThue.contains(":")) {
                    maSoThue = maSoThue.substring(maSoThue.indexOf(":") + 1).trim();
                }
                System.out.println("✅ [EnvConfigUtil] MaSoThue loaded from system environment");
                return maSoThue;
            }
            
            System.out.println("⚠️ [EnvConfigUtil] MaSoThue not found in .env or system environment");
            return null;
        } catch (Exception e) {
            System.err.println("❌ [EnvConfigUtil] Error loading MaSoThue: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get VAT rate from .env file
     * Format in .env: VAT_RATE=10.0
     * @return VAT rate as double (default: 10.0)
     */
    public static double getVATRate() {
        try {
            // Try .env file first
            Dotenv dotenv = loadDotenv();
            String vatRateStr = dotenv.get("VAT_RATE");
            
            if (vatRateStr != null && !vatRateStr.trim().isEmpty()) {
                double vatRate = Double.parseDouble(vatRateStr.trim());
                System.out.println("✅ [EnvConfigUtil] VAT_RATE loaded from .env: " + vatRate + "%");
                return vatRate;
            }
            
            // Try system environment variable
            vatRateStr = System.getenv("VAT_RATE");
            if (vatRateStr != null && !vatRateStr.trim().isEmpty()) {
                double vatRate = Double.parseDouble(vatRateStr.trim());
                System.out.println("✅ [EnvConfigUtil] VAT_RATE loaded from system environment: " + vatRate + "%");
                return vatRate;
            }
            
            // Return default
            System.out.println("ℹ️ [EnvConfigUtil] VAT_RATE not found, using default: " + DEFAULT_VAT_RATE + "%");
            return Double.parseDouble(DEFAULT_VAT_RATE);
        } catch (Exception e) {
            System.err.println("❌ [EnvConfigUtil] Error loading VAT_RATE, using default: " + e.getMessage());
            return Double.parseDouble(DEFAULT_VAT_RATE);
        }
    }
}

