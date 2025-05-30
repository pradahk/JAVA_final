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
                System.err.println("Sorry, unable to find " + CONFIG_FILE + ". Using default database settings.");
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
            ex.printStackTrace();
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
                    "   breakfast_start TEXT," + // 변경된 컬럼
                    "   breakfast_end TEXT," +   // 변경된 컬럼
                    "   lunch_start TEXT," +     // 변경된 컬럼
                    "   lunch_end TEXT," +       // 변경된 컬럼
                    "   dinner_start TEXT," +    // 변경된 컬럼
                    "   dinner_end TEXT," +      // 변경된 컬럼
                    "   sleep_start TEXT," +     // 변경된 컬럼
                    "   sleep_end TEXT," +       // 변경된 컬럼
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

    private DBManager() {
    }

    public static Connection getConnection() throws SQLException {
        Connection con = null;
        try {
            con = DriverManager.getConnection(DB_URL);
            try (Statement stmt = con.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
        } catch (SQLException e) {
            System.err.println("Database Connection Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return con;
    }

    /**
     * 데이터베이스 스키마가 존재하지 않으면(Users 테이블 기준) 스키마를 생성하고,
     * 관리자 계정이 없으면 생성합니다.
     * 애플리케이션 시작 시 한 번 호출하여 DB 파일 및 테이블을 초기화합니다.
     * 또한, 기존 테이블의 컬럼 변경이 필요한 경우 마이그레이션 로직을 수행합니다.
     */
    public static void initializeDatabase() {
        System.out.println("Initializing database...");

        try (Connection conn = getConnection()) {
            if (conn != null) {
                // Users 테이블 존재 여부 확인
                if (!tableExists(conn, "Users")) {
                    System.out.println("Database schema not found. Creating schema...");
                    try (Statement stmt = conn.createStatement()) {
                        String[] statements = CREATE_SCHEMA_SQL.split(";");
                        for (String sql : statements) {
                            sql = sql.trim();
                            if (!sql.isEmpty()) {
                                stmt.executeUpdate(sql + ";");
                            }
                        }
                        System.out.println("Database schema created successfully.");
                    } catch (SQLException e) {
                        System.err.println("Error creating database schema: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Database schema already exists.");
                    // 기존 Users 테이블에 is_admin 컬럼이 없으면 추가하는 로직 (Migration)
                    if (!columnExists(conn, "Users", "is_admin")) {
                        System.out.println("Adding 'is_admin' column to Users table...");
                        try (Statement stmt = conn.createStatement()) {
                            stmt.executeUpdate("ALTER TABLE Users ADD COLUMN is_admin INTEGER DEFAULT 0;");
                            System.out.println("'is_admin' column added to Users table.");
                        } catch (SQLException e) {
                            System.err.println("Error adding 'is_admin' column: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    // DosageRecords 테이블의 새 컬럼들 추가 로직 (Migration)
                    if (!columnExists(conn, "DosageRecords", "rescheduled_time")) {
                        System.out.println("Adding 'rescheduled_time' column to DosageRecords table...");
                        try (Statement stmt = conn.createStatement()) {
                            stmt.executeUpdate("ALTER TABLE DosageRecords ADD COLUMN rescheduled_time TEXT;");
                            System.out.println("'rescheduled_time' column added to DosageRecords table.");
                        } catch (SQLException e) {
                            System.err.println("Error adding 'rescheduled_time' column: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    if (!columnExists(conn, "DosageRecords", "is_skipped")) {
                        System.out.println("Adding 'is_skipped' column to DosageRecords table...");
                        try (Statement stmt = conn.createStatement()) {
                            stmt.executeUpdate("ALTER TABLE DosageRecords ADD COLUMN is_skipped INTEGER DEFAULT 0;");
                            System.out.println("'is_skipped' column added to DosageRecords table.");
                        } catch (SQLException e) {
                            System.err.println("Error adding 'is_skipped' column: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }

                    // UserPatterns 테이블의 새 컬럼들 추가 로직 (Migration)
                    // 기존 컬럼 (breakfast, lunch, dinner, sleep)이 있다면 삭제하고 새로운 컬럼을 추가하는 것이 일반적이지만,
                    // 데이터 손실을 방지하기 위해 여기서는 간단히 새로운 컬럼만 추가합니다.
                    // 실제 운영 환경에서는 데이터 마이그레이션 전략을 신중히 고려해야 합니다.
                    if (tableExists(conn, "UserPatterns")) {
                        if (!columnExists(conn, "UserPatterns", "breakfast_start")) {
                            System.out.println("Adding 'breakfast_start' and 'breakfast_end' columns to UserPatterns table...");
                            try (Statement stmt = conn.createStatement()) {
                                // 기존 컬럼들을 먼저 삭제하거나 (데이터 손실 주의)
                                // ALTER TABLE UserPatterns DROP COLUMN breakfast; (SQLite는 DROP COLUMN 지원하지 않음, 테이블 재생성 필요)
                                // 대신 새로운 컬럼만 추가하고, 기존 데이터를 새 컬럼으로 옮기는 로직이 필요할 수 있습니다.
                                // 이 예시에서는 단순히 새 컬럼만 추가합니다.
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN breakfast_start TEXT;");
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN breakfast_end TEXT;");
                                System.out.println("'breakfast_start' and 'breakfast_end' columns added.");
                            } catch (SQLException e) {
                                System.err.println("Error adding breakfast columns: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        if (!columnExists(conn, "UserPatterns", "lunch_start")) {
                            System.out.println("Adding 'lunch_start' and 'lunch_end' columns to UserPatterns table...");
                            try (Statement stmt = conn.createStatement()) {
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN lunch_start TEXT;");
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN lunch_end TEXT;");
                                System.out.println("'lunch_start' and 'lunch_end' columns added.");
                            } catch (SQLException e) {
                                System.err.println("Error adding lunch columns: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        if (!columnExists(conn, "UserPatterns", "dinner_start")) {
                            System.out.println("Adding 'dinner_start' and 'dinner_end' columns to UserPatterns table...");
                            try (Statement stmt = conn.createStatement()) {
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN dinner_start TEXT;");
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN dinner_end TEXT;");
                                System.out.println("'dinner_start' and 'dinner_end' columns added.");
                            } catch (SQLException e) {
                                System.err.println("Error adding dinner columns: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        if (!columnExists(conn, "UserPatterns", "sleep_start")) {
                            System.out.println("Adding 'sleep_start' and 'sleep_end' columns to UserPatterns table...");
                            try (Statement stmt = conn.createStatement()) {
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN sleep_start TEXT;");
                                stmt.executeUpdate("ALTER TABLE UserPatterns ADD COLUMN sleep_end TEXT;");
                                System.out.println("'sleep_start' and 'sleep_end' columns added.");
                            } catch (SQLException e) {
                                System.err.println("Error adding sleep columns: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // 관리자 계정 생성 로직
                if (tableExists(conn, "Users")) {
                    if (!adminAccountExists(conn)) {
                        insertAdminAccount(conn);
                    } else {
                        System.out.println("Admin account already exists.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during database initialization connection or admin account setup: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("Admin account created successfully: " + ADMIN_USERNAME);
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}