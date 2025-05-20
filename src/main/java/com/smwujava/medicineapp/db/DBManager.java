package com.smwujava.medicineapp.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class DBManager {
    private static final String DB_URL = "jdbc:sqlite:./pharm_reminder.db";

    // <<< 변경점 1: Connection 객체를 static 필드로 유지 >>>
    private static Connection currentConnection = null; // 현재 열린 DB 연결을 저장할 static 필드

    private static final String CREATE_SCHEMA_SQL =
            "PRAGMA foreign_keys = ON;" +
                    "CREATE TABLE IF NOT EXISTS Users (" +
                    "   user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "   username TEXT UNIQUE NOT NULL," +
                    "   password TEXT NOT NULL" +
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
                    "   record_date TEXT NOT NULL," +
                    "   scheduled_time TEXT NOT NULL," +
                    "   actual_taken_time TEXT," +
                    "   FOREIGN KEY (user_id) REFERENCES Users (user_id) ON DELETE CASCADE ON UPDATE CASCADE," +
                    "   FOREIGN KEY (med_id) REFERENCES Medicine (med_id) ON DELETE CASCADE ON UPDATE CASCADE," +
                    "   UNIQUE (user_id, med_id, record_date)" +
                    ");";

    private DBManager() {
        // Utility 클래스이므로 인스턴스화 방지
    }

    /**
     * SQLite 데이터베이스 연결을 설정하고 Connection 객체를 반환합니다.
     * DB 파일이 없으면 자동으로 생성됩니다.
     * 싱글턴 패턴으로 Connection을 관리합니다.
     * @return 유효한 Connection 객체
     * @throws SQLException 데이터베이스 접근 오류 발생 시
     */
    public static Connection getConnection() throws SQLException {
        // <<< 변경점 2: 기존 Connection이 없거나 닫혀있으면 새로 생성 >>>
        if (currentConnection == null || currentConnection.isClosed()) {
            try {
                currentConnection = DriverManager.getConnection(DB_URL);
                // System.out.println("New database connection established."); // 디버깅용
            } catch (SQLException e) {
                System.err.println("Database Connection Error: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }
        return currentConnection;
    }

    /**
     * 데이터베이스 스키마가 존재하지 않으면(Users 테이블 기준) 스키마를 생성합니다.
     * 애플리케이션 시작 시 한 번 호출하여 DB 파일 및 테이블을 초기화합니다.
     */
    public static void initializeDatabase() {
        System.out.println("Initializing database...");
        // initializeDatabase는 스키마 생성 여부만 확인하므로, 여기서는 별도의 Connection을 사용해도 무방합니다.
        // 하지만getConnection()이 싱글턴으로 변경되었으므로, 같은 Connection을 사용하게 됩니다.
        try (Connection conn = getConnection()) { // getConnection()이 이제 Connection을 생성하거나 기존 것을 반환
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                // 테이블 존재 여부 확인
                ResultSet tables = meta.getTables(null, null, "Users", null); // <<< 여기 "Users"로 수정

                if (!tables.next()) {
                    System.out.println("Database schema not found. Creating schema...");
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(CREATE_SCHEMA_SQL);
                        System.out.println("Database schema created successfully.");
                    } catch (SQLException e) {
                        System.err.println("Error creating database schema: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Database schema already exists.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during database initialization connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 현재 열려 있는 데이터베이스 연결을 닫습니다.
     * MainApp의 shutdown hook에서 호출될 매개변수 없는 메서드입니다.
     */
    // <<< 변경점 3: MainApp에서 호출할 매개변수 없는 closeConnection() 추가 >>>
    public static void closeConnection() {
        // 기존 closeConnection(Connection conn) 메서드를 내부적으로 활용합니다.
        // 또는 직접 currentConnection을 닫는 로직을 구현합니다.
        if (currentConnection != null) {
            try {
                currentConnection.close();
                currentConnection = null; // 연결 닫았으니 null로 설정
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // <<< 기존의 closeConnection(Connection conn) 메서드는 그대로 유지하거나,
    // 필요 없다면 제거할 수 있습니다. 여기서는 일단 유지하겠습니다. >>>
    /**
     * 주어진 데이터베이스 연결을 닫습니다.
     * (이 메서드는 이제 주로 내부적으로 사용되거나, 특정 Connection을 직접 닫을 때 사용)
     * @param conn 닫을 Connection 객체 (null일 수 있음)
     */
    public static void closeConnection(Connection conn) {
        if (conn != null && conn != currentConnection) { // currentConnection이 아닌 다른 Connection인 경우에만 닫음
            try {
                conn.close();
                // System.out.println("Separate database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing separate database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
        // currentConnection은 매개변수 없는 closeConnection()으로 관리합니다.
    }
}