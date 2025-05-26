package com.smwujava.medicineapp.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement; // PreparedStatement 추가 (관리자 계정 삽입에 사용)

public class DBManager {
    private static final String DB_URL = "jdbc:sqlite:./pharm_reminder.db";

    private static final String CREATE_SCHEMA_SQL =
            "PRAGMA foreign_keys = ON;" +
                    "CREATE TABLE IF NOT EXISTS Users (" +
                    "   user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "   username TEXT UNIQUE NOT NULL," +
                    "   password TEXT NOT NULL," +
                    "   auto_login INTEGER DEFAULT 0," +
                    "   is_admin INTEGER DEFAULT 0" + // <<-- is_admin 컬럼 추가 (0: 일반 사용자, 1: 관리자)
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
                    "   rescheduled_time TEXT," +        // <<-- reschedule_time 컬럼 추가 (NULL 허용)
                    "   is_skipped INTEGER DEFAULT 0," + // <<-- is_skipped 컬럼 추가 (0: false, 1: true)
                    "   FOREIGN KEY (user_id) REFERENCES Users (user_id) ON DELETE CASCADE ON UPDATE CASCADE," +
                    "   FOREIGN KEY (med_id) REFERENCES Medicine (med_id) ON DELETE CASCADE ON UPDATE CASCADE," +
                    "   UNIQUE (user_id, med_id, scheduled_time)" + // scheduled_time을 고유 제약 조건에 포함
                    ");";

    // --- [변경점 2: 관리자 계정 정보 상수 정의] ---
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin_password";

    private DBManager() {
        // 모든 메서드는 static으로 접근하므로 생성자는 private
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
            // 외래 키 제약을 활성화 (새로운 Connection마다 활성화하는 것이 안전)
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
                    // <<-- 변경점 3: 기존 Users 테이블에 is_admin 컬럼이 없으면 추가하는 로직 (Migration) -->>
                    // 실제 운영 환경에서는 더 견고한 마이그레이션 도구를 사용해야 하지만,
                    // 데스크톱 앱의 경우 간단하게 컬럼 추가 여부를 확인하여 처리할 수 있습니다.
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
                    // <<-- DosageRecords 테이블의 새 컬럼들 추가 로직 (Migration) -->>
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

                // --- [변경점 4: 관리자 계정 생성 로직] ---
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

    // --- [변경점 5: columnExists 메서드 추가] ---
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
        String sql = "SELECT COUNT(*) FROM Users WHERE username = ? AND is_admin = 1"; // <<-- is_admin 조건 추가
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
        String sql = "INSERT INTO Users (username, password, auto_login, is_admin) VALUES (?, ?, ?, ?)"; // <<-- is_admin 컬럼 포함
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ADMIN_USERNAME);
            pstmt.setString(2, ADMIN_PASSWORD);
            pstmt.setInt(3, 0); // 관리자 계정은 자동 로그인 비활성화 (0)
            pstmt.setInt(4, 1); // <<-- 관리자 계정으로 설정 (1)
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