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
                System.err.println("Unable to find " + CONFIG_FILE + ". Using default database settings: pharm_reminder.db");
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
            System.err.println("Error loading configuration: " + ex.getMessage() + ". Using default database settings.");
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
                        System.err.println("Error creating database schema: " + e.getMessage());
                    }
                } else {
                    if (!columnExists(conn, "Users", "is_admin")) {
                        try (Statement stmt = conn.createStatement()) {
                            stmt.executeUpdate("ALTER TABLE Users ADD COLUMN is_admin INTEGER DEFAULT 0;");
                        } catch (SQLException e) { System.err.println("Error adding 'is_admin' column: " + e.getMessage());}
                    }
                    if (!columnExists(conn, "DosageRecords", "rescheduled_time")) {
                        try (Statement stmt = conn.createStatement()) {
                            stmt.executeUpdate("ALTER TABLE DosageRecords ADD COLUMN rescheduled_time TEXT;");
                        } catch (SQLException e) { System.err.println("Error adding 'rescheduled_time' column: " + e.getMessage());}
                    }
                    if (!columnExists(conn, "DosageRecords", "is_skipped")) {
                        try (Statement stmt = conn.createStatement()) {
                            stmt.executeUpdate("ALTER TABLE DosageRecords ADD COLUMN is_skipped INTEGER DEFAULT 0;");
                        } catch (SQLException e) { System.err.println("Error adding 'is_skipped' column: " + e.getMessage());}
                    }
                    if (tableExists(conn, "UserPatterns")) {
                        if (!columnExists(conn, "UserPatterns", "breakfast_start")) {
                            try (Statement stmt = conn.createStatement()) {
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN breakfast_start TEXT;");
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN breakfast_end TEXT;");
                            } catch (SQLException e) {System.err.println("Error adding breakfast columns: " + e.getMessage());}
                        }
                        if (!columnExists(conn, "UserPatterns", "lunch_start")) {
                            try (Statement stmt = conn.createStatement()) {
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN lunch_start TEXT;");
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN lunch_end TEXT;");
                            } catch (SQLException e) {System.err.println("Error adding lunch columns: " + e.getMessage());}
                        }
                        if (!columnExists(conn, "UserPatterns", "dinner_start")) {
                            try (Statement stmt = conn.createStatement()) {
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN dinner_start TEXT;");
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN dinner_end TEXT;");
                            } catch (SQLException e) {System.err.println("Error adding dinner columns: " + e.getMessage());}
                        }
                        if (!columnExists(conn, "UserPatterns", "sleep_start")) {
                            try (Statement stmt = conn.createStatement()) {
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN sleep_start TEXT;");
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN sleep_end TEXT;");
                            } catch (SQLException e) {System.err.println("Error adding sleep columns: " + e.getMessage());}
                        }
                    }
                }

                if (tableExists(conn, "Users")) {
                    if (!adminAccountExists(conn)) {
                        insertAdminAccount(conn);
                    }
                }

                if (tableExists(conn, "Medicine")) {
                    insertSampleMedicineDataIfNeeded(conn, 1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during database initialization: " + e.getMessage());
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
        String checkSql = "SELECT COUNT(*) AS count FROM Medicine WHERE user_id = ?";
        boolean dataExists = false;
        try (PreparedStatement pstmtCheck = conn.prepareStatement(checkSql)) {
            pstmtCheck.setInt(1, userId);
            try (ResultSet rs = pstmtCheck.executeQuery()) {
                if (rs.next() && rs.getInt("count") > 0) {
                    dataExists = true;
                }
            }
        }

        if (!dataExists) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO Medicine (user_id, med_name, med_daily_amount, med_days, med_condition, med_timing, med_minutes, color) VALUES ("+userId+", '테스트약A (월수금)', 1, '월,수,금', '식후', '정각', 0, '#FF0000');");
                stmt.execute("INSERT INTO Medicine (user_id, med_name, med_daily_amount, med_days, med_condition, med_timing, med_minutes, color) VALUES ("+userId+", '테스트약B (매일)', 1, '월,화,수,목,금,토,일', '식전', '30분', 30, '#00FF00');");
                stmt.execute("INSERT INTO Medicine (user_id, med_name, med_daily_amount, med_days, med_condition, med_timing, med_minutes, color) VALUES ("+userId+", '오메프라졸 (매일 아침)', 1, '월,화,수,목,금,토,일', '아침 식사', '전', 30, '#B5A9FF');");
                stmt.execute("INSERT INTO Medicine (user_id, med_name, med_daily_amount, med_days, med_condition, med_timing, med_minutes, color) VALUES ("+userId+", '판피린Q (필요시)', 1, '월,화,수,목,금,토,일', '필요시', '정각', 0, '#E8FD94');");
                stmt.execute("INSERT INTO Medicine (user_id, med_name, med_daily_amount, med_days, med_condition, med_timing, med_minutes, color) VALUES ("+userId+", '비타민C (오전)', 1, '월,화,수,목,금,토,일', '오전', '정각', 0, '#FFA500');");
                stmt.execute("INSERT INTO Medicine (user_id, med_name, med_daily_amount, med_days, med_condition, med_timing, med_minutes, color) VALUES ("+userId+", '알레르기약 (저녁)', 1, '월,화,수,목,금,토,일', '저녁 식사', '후', 0, '#00FFFF');");
            }
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
}