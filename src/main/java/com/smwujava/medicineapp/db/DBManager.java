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

    // 설정 파일을 로드하는 정적 초기화 블록
    static {
        // config.properties 파일 경로 (resources 폴더에 있어야 함)
        // IDE에서 실행할 때는 프로젝트 루트의 resources 폴더에,
        // JAR 파일로 배포할 때는 JAR 파일 내부에 포함되어야 합니다.
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
                    "   breakfast TEXT," +
                    "   lunch TEXT," +
                    "   dinner TEXT," +
                    "   sleep TEXT," +
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
                    "   scheduled_time TEXT NOT NULL," + // SQLite는 DATETIME을 TEXT로 저장
                    "   actual_taken_time TEXT," +       // 실제 복용 시간 (NULL 허용)
                    "   rescheduled_time TEXT," +        // <-- 이 부분이 누락되었었습니다.
                    "   is_skipped INTEGER DEFAULT 0," + // <-- 이 부분이 누락되었었습니다.
                    "   FOREIGN KEY (user_id) REFERENCES Users (user_id) ON DELETE CASCADE ON UPDATE CASCADE," +
                    "   FOREIGN KEY (med_id) REFERENCES Medicine (med_id) ON DELETE CASCADE ON UPDATE CASCADE," +
                    "   UNIQUE (user_id, med_id, scheduled_time)" + // scheduled_time을 고유 제약 조건에 포함
                    ");";

    private DBManager() {
    }

    /**
     * SQLite 데이터베이스 연결을 설정하고 Connection 객체를 반환합니다.
     * DB 파일이 없으면 자동으로 생성됩니다.
     * @return 유효한 Connection 객체
     */
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
                    // initializeDatabase()가 처음 실행될 때 DosageRecords 테이블이 없으면 CREATE_SCHEMA_SQL에 의해 올바르게 생성됩니다.
                    // 그러나 기존에 테이블이 있었다면 이 마이그레이션 로직이 작동해야 합니다.
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
                }

                // 관리자 계정 생성 로직
                if (tableExists(conn, "Users")) { // Users 테이블이 존재하는지 다시 한번 확인
                    if (!adminAccountExists(conn)) { // 관리자 계정이 없는 경우
                        insertAdminAccount(conn); // 관리자 계정 삽입
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

    /**
     * 특정 테이블이 데이터베이스에 존재하는지 확인합니다.
     * @param conn 데이터베이스 연결 객체
     * @param tableName 확인할 테이블 이름
     * @return 테이블이 존재하면 true, 그렇지 않으면 false
     */
    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, tableName, new String[] {"TABLE"})) {
            return rs.next();
        }
    }

    /**
     * 특정 테이블에 특정 컬럼이 존재하는지 확인합니다.
     * @param conn 데이터베이스 연결 객체
     * @param tableName 확인할 테이블 이름
     * @param columnName 확인할 컬럼 이름
     * @return 컬럼이 존재하면 true, 그렇지 않으면 false
     */
    private static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }

    /**
     * 관리자 계정이 Users 테이블에 존재하는지 확인합니다.
     * @param conn 데이터베이스 연결 객체
     * @return 관리자 계정이 존재하면 true, 그렇지 않으면 false
     */
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

    /**
     * 관리자 계정을 Users 테이블에 삽입합니다.
     * @param conn 데이터베이스 연결 객체
     */
    private static void insertAdminAccount(Connection conn) throws SQLException {
        String sql = "INSERT INTO Users (username, password, auto_login, is_admin) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ADMIN_USERNAME);
            pstmt.setString(2, ADMIN_PASSWORD);
            pstmt.setInt(3, 0); // 관리자 계정은 자동 로그인 비활성화 (0)
            pstmt.setInt(4, 1); // 관리자 계정으로 설정 (1)
            pstmt.executeUpdate();
            System.out.println("Admin account created successfully: " + ADMIN_USERNAME);
        }
    }

    /**
     * 주어진 데이터베이스 연결을 닫습니다.
     * @param conn 닫을 Connection 객체 (null일 수 있음)
     */
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