package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class DatabaseManager implements AutoCloseable {

    private static final String HOST = "jdbc:postgresql://pg:5432/studs";
    private static DatabaseManager instance;

    private final AtomicBoolean connected = new AtomicBoolean(false);
    private Connection connection;
    private String lastLogin;
    private String lastPassword;

    private DatabaseManager(String login, String password) throws SQLException {
        this.lastLogin = login;
        this.lastPassword = password;
        connect();
    }

    private void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        Properties props = new Properties();
        props.setProperty("user", lastLogin);
        if (lastPassword != null && !lastPassword.isEmpty()) {
            props.setProperty("password", lastPassword);
        }
        props.setProperty("connectTimeout", "10");
        props.setProperty("socketTimeout", "30");
        props.setProperty("ssl", "false");
        props.setProperty("sslmode", "disable");

        try {
            this.connection = DriverManager.getConnection(HOST, props);
        } catch (SQLException e) {
            if (lastPassword == null || lastPassword.isEmpty()) {
                props.setProperty("password", "");
                try {
                    this.connection = DriverManager.getConnection(HOST, props);
                } catch (SQLException e2) {
                    throw e;
                }
            } else {
                throw e;
            }
        }
        this.connection.setAutoCommit(false);
        connected.set(true);

        System.err.println("Database connected");
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DatabaseManager not initialized. Call init() first.");
        }
        if (!instance.isConnected()) {
            throw new IllegalStateException("Database connection lost. Reconnect required.");
        }
        return instance;
    }

    public static synchronized void init(String login, String password) throws SQLException {
        if (instance != null) {
            try {
                instance.close();
            } catch (Exception ignored) {}
        }
        instance = new DatabaseManager(login, password);
    }

    public static synchronized boolean isInitialized() {
        return instance != null;
    }

    public static synchronized void closeInstance() {
        if (instance == null) {
            return;
        }
        try {
            instance.close();
        } finally {
            instance = null;
        }
    }

    public boolean isConnected() {
        try {
            return connected.get() && connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            connected.set(false);
            return false;
        }
    }

    public Connection getConnection() {
        if (!isConnected()) {
            reconnect();
        }
        return connection;
    }

    private synchronized void reconnect() {
        try {
            connect();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reconnect: " + e.getMessage(), e);
        }
    }

    public void commit() throws SQLException {
        Connection conn = getConnection();
        conn.commit();
    }

    public void rollback() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.rollback();
            }
        } catch (SQLException e) {
            System.err.println("Rollback error: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        connected.set(false);
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}