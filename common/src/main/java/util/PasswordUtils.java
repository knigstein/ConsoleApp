package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Утилитный класс для хэширования и проверки паролей.
 *
 * <p>По требованиям лабораторной работы при хранении паролей используется MD2,
 * поэтому данный класс намеренно реализует именно этот алгоритм.</p>
 */
public final class PasswordUtils {

    private PasswordUtils() {
    }

    /**
     * Вычисляет MD2-хэш для переданного пароля.
     *
     * @param password пароль в открытом виде
     * @return MD2-хэш в виде шестнадцатеричной строки
     * @throws RuntimeException если алгоритм MD2 недоступен в текущей JVM
     */
    public static String hashMD2(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD2");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD2 algorithm not available", e);
        }
    }

    /**
     * Проверяет соответствие пароля сохраненному MD2-хэшу.
     *
     * @param password пароль в открытом виде
     * @param hash сохраненный MD2-хэш
     * @return {@code true}, если пароль соответствует хэшу; иначе {@code false}
     */
    public static boolean verifyMD2(String password, String hash) {
        return hashMD2(password).equalsIgnoreCase(hash);
    }
}