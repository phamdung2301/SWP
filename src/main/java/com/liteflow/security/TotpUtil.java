package com.liteflow.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.time.Instant;

/**
 * TOTP utilities (RFC 6238) – 6 digits, 30s time-step, HMAC-SHA1. Dùng Base32
 * secret (Google Authenticator/Authenticator apps).
 */
public final class TotpUtil {

    private static final String HMAC_ALGO = "HmacSHA1";
    private static final int DIGITS = 6;         // 6 số
    private static final int TIME_STEP_SEC = 30; // 30s/step
    private static final String BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    private TotpUtil() {
    }

    /**
     * Sinh mã TOTP hiện tại cho secret Base32.
     *
     * @param base32Secret secret mã hoá Base32 (không null/blank)
     * @return mã 6 số hoặc null nếu secret không hợp lệ
     */
    public static String generate(String base32Secret) {
        if (base32Secret == null || base32Secret.isBlank()) {
            return null;
        }
        long counter = Instant.now().getEpochSecond() / TIME_STEP_SEC;
        return generateAt(base32Secret, counter);
    }

    /**
     * Sinh mã TOTP tại time-step chỉ định (dùng cho verify/kiểm thử).
     *
     * @param base32Secret secret Base32
     * @param counter UnixTimeSeconds/30
     * @return mã 6 số hoặc null nếu lỗi
     */
    public static String generateAt(String base32Secret, long counter) {
        try {
            byte[] key = base32Decode(base32Secret);
            byte[] data = ByteBuffer.allocate(8).putLong(counter).array();

            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(key, HMAC_ALGO));
            byte[] hmac = mac.doFinal(data);

            int offset = hmac[hmac.length - 1] & 0x0F;
            int bin = ((hmac[offset] & 0x7F) << 24)
                    | ((hmac[offset + 1] & 0xFF) << 16)
                    | ((hmac[offset + 2] & 0xFF) << 8)
                    | (hmac[offset + 3] & 0xFF);

            int otp = bin % (int) Math.pow(10, DIGITS);
            return String.format("%0" + DIGITS + "d", otp);
        } catch (GeneralSecurityException e) {
            return null;
        }
    }

    /**
     * Xác minh mã TOTP với cửa sổ lệch bước (mặc định nên dùng 1).
     *
     * @param base32Secret secret Base32
     * @param code mã người dùng nhập
     * @param window số bước cho phép lệch (ví dụ 1: chấp nhận step hiện tại
     * +/-1)
     * @return true nếu hợp lệ
     */
    public static boolean verify(String base32Secret, String code, int window) {
        if (base32Secret == null || base32Secret.isBlank() || code == null || code.length() != DIGITS) {
            return false;
        }
        long now = Instant.now().getEpochSecond() / TIME_STEP_SEC;
        for (long c = now - window; c <= now + window; c++) {
            String expect = generateAt(base32Secret, c);
            if (expect != null && expect.equals(code)) {
                return true;
            }
        }
        return false;
    }

    // --- Base32 decode tối giản (không phụ thuộc thư viện ngoài) ---
    private static byte[] base32Decode(String s) {
        String input = s.toUpperCase().replaceAll("[=\\s]", "");
        int buffer = 0, bitsLeft = 0;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < input.length(); i++) {
            int val = BASE32.indexOf(input.charAt(i));
            if (val < 0) {
                continue;           // bỏ ký tự lạ
            }
            buffer = (buffer << 5) | (val & 31);
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                out.write((buffer >> (bitsLeft - 8)) & 0xFF);
                bitsLeft -= 8;
            }
        }
        return out.toByteArray();
    }
}
