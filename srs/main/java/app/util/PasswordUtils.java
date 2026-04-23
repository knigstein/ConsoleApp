package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class PasswordUtils {

    private PasswordUtils() {
    }

    public static String hashMD2(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD2");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD2 algorithm not available", e);
        }
    }

    public static boolean verifyMD2(String password, String hash) {
        return hashMD2(password).equalsIgnoreCase(hash);
    }
}