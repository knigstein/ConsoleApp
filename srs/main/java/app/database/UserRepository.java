package database;

import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class UserRepository {
    private static final String INSERT_SQL =
        "INSERT INTO users (login, password_hash) VALUES (?, ?) RETURNING id, created_at";
    private static final String FIND_BY_LOGIN_SQL =
        "SELECT id, login, password_hash, created_at FROM users WHERE login = ?";
    private static final String FIND_BY_ID_SQL =
        "SELECT id, login, password_hash, created_at FROM users WHERE id = ?";
    private static final String UPDATE_PASSWORD_SQL =
        "UPDATE users SET password_hash = ? WHERE id = ?";


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

    public void updatePassword(Integer userId, String newPasswordHash) throws SQLException {
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_PASSWORD_SQL)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
            DatabaseManager.getInstance().commit();
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("login"),
            rs.getString("password_hash"),
            rs.getTimestamp("created_at")
        );
    }
}