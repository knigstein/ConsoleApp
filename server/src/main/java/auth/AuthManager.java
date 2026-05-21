package auth;

import database.UserRepository;
import model.User;
import util.PasswordUtils;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис аутентификации: регистрация, вход и управление сессиями в памяти.
 */
public class AuthManager {

    private static final Map<String, User> sessions = new ConcurrentHashMap<>();
    private static UserRepository userRepository = new UserRepository();

    /**
     * Регистрирует нового пользователя с хэшированием пароля по MD2.
     *
     * @param login логин пользователя
     * @param password пароль в открытом виде
     * @return созданный пользователь
     * @throws SQLException если операция с БД завершилась ошибкой
     */
    public static User register(String login, String password) throws SQLException {
        String normalizedLogin = normalizeLogin(login);
        if (normalizedLogin == null) {
            throw new IllegalArgumentException("Логин не может быть пустым.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым.");
        }

        String hash = PasswordUtils.hashMD2(password);
        try {
            User user = userRepository.create(normalizedLogin, hash);
            sessions.put(normalizedLogin, user);
            return user;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Этот логин уже занят.", e);
            }
            throw e;
        }
    }

    /**
     * Выполняет попытку входа по паре логин/пароль.
     *
     * @param login логин пользователя
     * @param password пароль в открытом виде
     * @return {@link Optional} с аутентифицированным пользователем
     * @throws SQLException если операция с БД завершилась ошибкой
     */
    public static Optional<User> login(String login, String password) throws SQLException {
        String normalizedLogin = normalizeLogin(login);
        if (normalizedLogin == null || password == null) {
            return Optional.empty();
        }

        Optional<User> userOpt = userRepository.findByLogin(normalizedLogin);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        User user = userOpt.get();
        if (PasswordUtils.verifyMD2(password, user.getPasswordHash())) {
            sessions.put(normalizedLogin, user);
            return Optional.of(user);
        }
        return Optional.empty();
    }

    /**
     * Удаляет сессию пользователя из памяти по логину.
     *
     * @param login логин пользователя
     */
    public static void logout(String login) {
        String normalizedLogin = normalizeLogin(login);
        if (normalizedLogin != null) {
            sessions.remove(normalizedLogin);
        }
    }

    /**
     * Возвращает активную сессию пользователя по логину.
     *
     * @param login логин пользователя
     * @return {@link Optional} с пользователем из сессии
     */
    public static Optional<User> getSession(String login) {
        String normalizedLogin = normalizeLogin(login);
        if (normalizedLogin == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(sessions.get(normalizedLogin));
    }

    /**
     * Проверяет, есть ли у пользователя активная сессия в памяти.
     *
     * @param login логин пользователя
     * @return {@code true}, если сессия существует
     */
    public static boolean isAuthenticated(String login) {
        String normalizedLogin = normalizeLogin(login);
        return normalizedLogin != null && sessions.containsKey(normalizedLogin);
    }

    /**
     * Ищет пользователя в постоянном хранилище по логину.
     *
     * @param login логин пользователя
     * @return {@link Optional} с найденным пользователем
     * @throws SQLException если запрос завершился ошибкой
     */
    public static Optional<User> findUserByLogin(String login) throws SQLException {
        String normalizedLogin = normalizeLogin(login);
        if (normalizedLogin == null) {
            return Optional.empty();
        }
        return userRepository.findByLogin(normalizedLogin);
    }

    private static String normalizeLogin(String login) {
        if (login == null) {
            return null;
        }
        String normalized = login.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}