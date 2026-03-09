package dataaccess;

import java.sql.*;
import java.util.Properties;
import java.io.InputStream;

public class DatabaseManager {

    private static String databaseName;
    private static String dbUsername;
    private static String dbPassword;
    private static String connectionUrl;

    // ===================== STATIC BLOCK TO LOAD PROPERTIES =====================
    static {
        loadPropertiesFromResources();
    }

    // ===================== LOAD PROPERTIES =====================
    private static void loadPropertiesFromResources() {
        try (InputStream propStream = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream("db.properties")) {
            if (propStream == null) {
                throw new Exception("Unable to load db.properties");
            }

            Properties props = new Properties();
            props.load(propStream);

            databaseName = props.getProperty("db.name");
            dbUsername = props.getProperty("db.user");
            dbPassword = props.getProperty("db.password");

            String host = props.getProperty("db.host");
            int port = Integer.parseInt(props.getProperty("db.port"));
            connectionUrl = String.format("jdbc:mysql://%s:%d?serverTimezone=UTC", host, port);

        } catch (Exception ex) {
            throw new RuntimeException("Unable to process db.properties", ex);
        }
    }

    // ===================== CREATE DATABASE =====================
    public static void createDatabase() throws DataAccessException {
        String sql = "CREATE DATABASE IF NOT EXISTS " + databaseName;
        try (Connection conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create database", ex);
        }
    }

    // ===================== GET CONNECTION =====================
    public static Connection getConnection() throws DataAccessException {
        try {
            Connection conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
            conn.setCatalog(databaseName);
            return conn;
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get connection", ex);
        }
    }

    // ===================== INITIALIZE TABLES =====================
    public static void initializeTables() throws DataAccessException {
        String createUsers =
                "CREATE TABLE IF NOT EXISTS users (" +
                        "username VARCHAR(50) PRIMARY KEY," +
                        "password VARCHAR(255) NOT NULL," +
                        "email VARCHAR(255) NOT NULL" +
                        ")";
        String createAuth =
                "CREATE TABLE IF NOT EXISTS auth (" +
                        "token VARCHAR(255) PRIMARY KEY," +
                        "username VARCHAR(50) NOT NULL," +
                        "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE" +
                        ")";
        String createGames =
                "CREATE TABLE IF NOT EXISTS games (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "whiteUsername VARCHAR(50)," +
                        "blackUsername VARCHAR(50)," +
                        "gameName VARCHAR(100)," +
                        "gameState BLOB," +
                        "FOREIGN KEY (whiteUsername) REFERENCES users(username) ON DELETE CASCADE," +
                        "FOREIGN KEY (blackUsername) REFERENCES users(username) ON DELETE CASCADE" +
                        ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createUsers);
            stmt.executeUpdate(createAuth);
            stmt.executeUpdate(createGames);
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to initialize tables", ex);
        }
    }

    // ===================== HELPER TO INITIALIZE DATABASE + TABLES =====================
    public static void initialize() throws DataAccessException {
        createDatabase();
        initializeTables();
    }
}