package com.liteflow.util;

/**
 * Small CLI utility to generate bcrypt hashes from plaintext passwords.
 * Usage (from project root):
 *   mvn -q -Dexec.args="password" exec:java -Dexec.mainClass="com.liteflow.util.HashGenerator"
 * Or run via your IDE by passing a single argument (the plaintext password).
 */
public class HashGenerator {

    public static void main(String[] args) {
        String pw = null;
        if (args != null && args.length > 0 && args[0] != null && !args[0].isBlank()) {
            pw = args[0];
        } else {
            // Try to read from Console (preferred)
            try {
                java.io.Console console = System.console();
                if (console != null) {
                    char[] pass = console.readPassword("Enter password to hash: ");
                    if (pass != null) pw = new String(pass);
                } else {
                    // Fall back to stdin (works in IDE terminals)
                    System.out.print("Enter password to hash: ");
                    java.util.Scanner scanner = new java.util.Scanner(System.in);
                    if (scanner.hasNextLine()) pw = scanner.nextLine();
                    scanner.close();
                }
            } catch (Exception e) {
                System.err.println("Failed to read password from console/stdin: " + e.getMessage());
            }
        }

        if (pw == null || pw.isBlank()) {
            System.err.println("No password provided. Exiting.");
            System.exit(2);
        }

        String hash = PasswordUtil.hash(pw, 12);
        System.out.println(hash);
    }
}
