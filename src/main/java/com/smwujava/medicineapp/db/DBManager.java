package com.smwujava.medicineapp.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class DBManager {
    private static String DB_URL;
    private static String ADMIN_USERNAME;
    private static String ADMIN_PASSWORD;

    static {
        final String CONFIG_FILE = "config.properties";
        try (InputStream input = DBManager.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            Properties prop = new Properties();
            if (input == null) {
                System.err.println("Unable to find " + CONFIG_FILE + ". Using default: pharm_reminder.db");
                DB_URL = "jdbc:sqlite:./pharm_reminder.db";
                ADMIN_USERNAME = "admin";
                ADMIN_PASSWORD = "admin_password";
            } else {
                prop.load(input);
                DB_URL = prop.getProperty("db.url", "jdbc:sqlite:./pharm_reminder.db");
                ADMIN_USERNAME = prop.getProperty("admin.username", "admin");
                ADMIN_PASSWORD = prop.getProperty("admin.password", "admin_password");
            }
        } catch (IOException ex) {
            System.err.println("Error loading configuration: " + ex.getMessage() + ". Using defaults.");
            DB_URL = "jdbc:sqlite:./pharm_reminder.db";
            ADMIN_USERNAME = "admin";
            ADMIN_PASSWORD = "admin_password";
        }
    }

    private static final String CREATE_SCHEMA_SQL =
            "PRAGMA foreign_keys = ON;" +
                    "CREATE TABLE IF NOT EXISTS Users (" +
                    "   user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "   username TEXT UNIQUE NOT NULL," +
                    "   password TEXT NOT NULL," +
                    "   auto_login INTEGER DEFAULT 0," +
                    "   is_admin INTEGER DEFAULT 0" +
                    ");" +
                    "CREATE TABLE IF NOT EXISTS UserPatterns (" +
                    "   user_id INTEGER PRIMARY KEY," +
                    "   breakfast_start TEXT," +
                    "   breakfast_end TEXT," +
                    "   lunch_start TEXT," +
                    "   lunch_end TEXT," +
                    "   dinner_start TEXT," +
                    "   dinner_end TEXT," +
                    "   sleep_start TEXT," +
                    "   sleep_end TEXT," +
                    "   FOREIGN KEY (user_id) REFERENCES Users (user_id) ON DELETE CASCADE ON UPDATE CASCADE" +
                    ");" +
                    "CREATE TABLE IF NOT EXISTS Medicine (" +
                    "   med_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "   user_id INTEGER NOT NULL," +
                    "   med_name TEXT NOT NULL," +
                    "   med_daily_amount INTEGER NOT NULL," +
                    "   med_days TEXT NOT NULL," +
                    "   med_condition TEXT NOT NULL," +
                    "   med_timing TEXT NOT NULL," +
                    "   med_minutes INTEGER NOT NULL," +
                    "   color TEXT NOT NULL," +
                    "   FOREIGN KEY (user_id) REFERENCES Users (user_id) ON DELETE CASCADE ON UPDATE CASCADE" +
                    ");" +
                    "CREATE TABLE IF NOT EXISTS DosageRecords (" +
                    "   record_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "   user_id INTEGER NOT NULL," +
                    "   med_id INTEGER NOT NULL," +
                    "   scheduled_time TEXT NOT NULL," +
                    "   actual_taken_time TEXT," +
                    "   rescheduled_time TEXT," +
                    "   is_skipped INTEGER DEFAULT 0," +
                    "   FOREIGN KEY (user_id) REFERENCES Users (user_id) ON DELETE CASCADE ON UPDATE CASCADE," +
                    "   FOREIGN KEY (med_id) REFERENCES Medicine (med_id) ON DELETE CASCADE ON UPDATE CASCADE," +
                    "   UNIQUE (user_id, med_id, scheduled_time)" +
                    ");";

    private DBManager() {}

    public static Connection getConnection() throws SQLException {
        Connection con = DriverManager.getConnection(DB_URL);
        try (Statement stmt = con.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return con;
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                if (!tableExists(conn, "Users")) {
                    try (Statement stmt = conn.createStatement()) {
                        String[] statements = CREATE_SCHEMA_SQL.split(";");
                        for (String sql : statements) {
                            sql = sql.trim();
                            if (!sql.isEmpty()) {
                                stmt.executeUpdate(sql + ";");
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("Error creating schema: " + e.getMessage());
                    }
                } else {
                    // Column alteration checks
                    if (!columnExists(conn, "Users", "is_admin")) {
                        try (Statement stmt = conn.createStatement()) { stmt.executeUpdate("ALTER TABLE Users ADD COLUMN is_admin INTEGER DEFAULT 0;"); }
                        catch (SQLException e) { System.err.println("Error adding 'is_admin': " + e.getMessage());}
                    }
                    if (!columnExists(conn, "DosageRecords", "rescheduled_time")) {
                        try (Statement stmt = conn.createStatement()) { stmt.executeUpdate("ALTER TABLE DosageRecords ADD COLUMN rescheduled_time TEXT;"); }
                        catch (SQLException e) { System.err.println("Error adding 'rescheduled_time': " + e.getMessage());}
                    }
                    if (!columnExists(conn, "DosageRecords", "is_skipped")) {
                        try (Statement stmt = conn.createStatement()) { stmt.executeUpdate("ALTER TABLE DosageRecords ADD COLUMN is_skipped INTEGER DEFAULT 0;"); }
                        catch (SQLException e) { System.err.println("Error adding 'is_skipped': " + e.getMessage());}
                    }
                    if (tableExists(conn, "UserPatterns")) {
                        if (!columnExists(conn, "UserPatterns", "breakfast_start")) {
                            try (Statement stmt = conn.createStatement()) {
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN breakfast_start TEXT;");
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN breakfast_end TEXT;");
                            } catch (SQLException e) {System.err.println("Error adding breakfast_start/end: " + e.getMessage());}
                        }
                        if (!columnExists(conn, "UserPatterns", "lunch_start")) {
                            try (Statement stmt = conn.createStatement()) {
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN lunch_start TEXT;");
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN lunch_end TEXT;");
                            } catch (SQLException e) {System.err.println("Error adding lunch_start/end: " + e.getMessage());}
                        }
                        if (!columnExists(conn, "UserPatterns", "dinner_start")) {
                            try (Statement stmt = conn.createStatement()) {
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN dinner_start TEXT;");
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN dinner_end TEXT;");
                            } catch (SQLException e) {System.err.println("Error adding dinner_start/end: " + e.getMessage());}
                        }
                        if (!columnExists(conn, "UserPatterns", "sleep_start")) {
                            try (Statement stmt = conn.createStatement()) {
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN sleep_start TEXT;");
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN sleep_end TEXT;");
                            } catch (SQLException e) {System.err.println("Error adding sleep_start/end: " + e.getMessage());}
                        }
                    }
                }

                if (tableExists(conn, "Users")) {
                    if (!adminAccountExists(conn)) {
                        insertAdminAccount(conn);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during DB init: " + e.getMessage());
        }
    }

    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, tableName, new String[] {"TABLE"})) {
            return rs.next();
        }
    }

    private static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }

    private static boolean adminAccountExists(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE username = ? AND is_admin = 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ADMIN_USERNAME);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private static void insertAdminAccount(Connection conn) throws SQLException {
        String sql = "INSERT INTO Users (username, password, auto_login, is_admin) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ADMIN_USERNAME);
            pstmt.setString(2, ADMIN_PASSWORD);
            pstmt.setInt(3, 0);
            pstmt.setInt(4, 1);
            pstmt.executeUpdate();
        }
    }

    private static void insertSampleMedicineDataIfNeeded(Connection conn, int userId) throws SQLException {
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing DB connection: " + e.getMessage());
            }
        }
    }
}