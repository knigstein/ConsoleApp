package database;

import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

/**
 * Репозиторий для CRUD-операций над таблицей {@code users}.
 */
public class UserRepository {
    private static final String INSERT_SQL =
        "INSERT INTO users (login, password_hash) VALUES (?, ?) RETURNING id, created_at";
    private static final String FIND_BY_LOGIN_SQL =
        "SELECT id, login, password_hash, created_at FROM users WHERE login = ?";
    private static final String FIND_BY_ID_SQL =
        "SELECT id, login, password_hash, created_at FROM users WHERE id = ?";
    private static final String UPDATE_PASSWORD_SQL =
        "UPDATE users SET password_hash = ? WHERE id = ?";


    /**
     * Ищет пользователя по логину.
     *
     * @param login логин пользователя
     * @return {@link Optional} с найденным пользователем
     * @throws SQLException если выполнение запроса завершилось ошибкой
     */
    public Optional<User> findByLogin(String login) throws SQLException {
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(FIND_BY_LOGIN_SQL)) {
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToUser(rs));
            }
            return Optional.empty();
        }
    }

    /**
     * Ищет пользователя по первичному ключу.
     *
     * @param id идентификатор пользователя
     * @return {@link Optional} с найденным пользователем
     * @throws SQLException если выполнение запроса завершилось ошибкой
     */
    public Optional<User> findById(Integer id) throws SQLException {
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(FIND_BY_ID_SQL)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToUser(rs));
            }
            return Optional.empty();
        }
    }

    /**
     * Создает нового пользователя и фиксирует транзакцию при успехе.
     *
     * @param login уникальный логин
     * @param passwordHash заранее вычисленный хэш пароля
     * @return сохраненный пользователь с сгенерированным id и временем создания
     * @throws SQLException если вставка не удалась
     */
    public User create(String login, String passwordHash) throws SQLException {
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            ps.setString(1, login);
            ps.setString(2, passwordHash);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = new User(
                    rs.getInt("id"),
                    login,
                    passwordHash,
                    rs.getTimestamp("created_at")
                );
                DatabaseManager.getInstance().commit();
                return user;
            }
            throw new SQLException("Failed to create user");
        } catch (SQLException e) {
            DatabaseManager.getInstance().rollback();
            throw e;
        }
    }

    /**
     * Обновляет хэш пароля пользователя.
     *
     * @param userId идентификатор пользователя
     * @param newPasswordHash новый хэш пароля
     * @throws SQLException если обновление не удалось
     */
    public void updatePassword(Integer userId, String newPasswordHash) throws SQLException {
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_PASSWORD_SQL)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
            DatabaseManager.getInstance().commit();
        }
    }

    /**
     * Преобразует строку {@link ResultSet} в объект {@link User}.
     *
     * @param rs текущая строка результата запроса
     * @return объект пользователя
     * @throws SQLException если чтение колонок завершилось ошибкой
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("login"),
            rs.getString("password_hash"),
            rs.getTimestamp("created_at")
        );
    }
}