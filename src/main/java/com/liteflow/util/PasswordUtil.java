package com.liteflow.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password hashing and verification using BCrypt.
 */
public class PasswordUtil {

    /**
     * Hash a raw password using BCrypt with given salt rounds.
     *
     * @param rawPassword the plaintext password
     * @param rounds      the log2 of the number of hashing rounds (e.g., 10)
     * @return the hashed password string
     */
    public static String hash(String rawPassword, int rounds) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(rounds));
    }

    /**
     * Verify a raw password against a hashed password.
     *
     * @param rawPassword the plaintext password
     * @param hashed      the previously hashed password
     * @return true if the password matches, false otherwise
     */
    public static boolean check(String rawPassword, String hashed) {
        if (rawPassword == null || hashed == null || hashed.isBlank()) {
            return false;
        }

        // normalize: BCrypt format differences ($2y$, $2b$ -> $2a$)
        if (hashed.startsWith("$2y$") || hashed.startsWith("$2b$")) {
            // replace prefix (4 chars) with $2a$
            hashed = "$2a$" + hashed.substring(4);
        }

        return BCrypt.checkpw(rawPassword, hashed);
    }
}
