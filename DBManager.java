package com.smwujava.medicineapp.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet; // 테이블 존재 여부 확인에 사용

public class DBManager {
    // SQLite JDBC URL - './'는 현재 애플리케이션이 실행되는 디렉토리를 의미합니다.
    // 실제 배포 시에는 사용자의 문서 폴더 등 더 적합한 위치를 사용하는 것이 좋습니다.
    private static final String DB_URL = "jdbc:sqlite:./pharm_reminder.db";

    private static final String CREATE_SCHEMA_SQL =
            "PRAGMA foreign_keys = ON;" +
                    "CREATE TABLE IF NOT EXISTS Users (" +
                    "   user_id INTEGER PRIMARY KEY AUTOINCREMENT," + // 사용자 고유 ID (자동 증가)
                    "   username TEXT UNIQUE NOT NULL," +             // 사용자 로그인 ID (고유하며 비어있으면 안됨)
                    "   password TEXT NOT NULL," +                   // 비밀번호 (비어있으면 안됨)
                    "   auto_login INTEGER DEFAULT 0" +               // 자동 로그인 여부 (0: false, 1: true) <<-- 추가된 컬럼
                    ");" +
                    "CREATE TABLE IF NOT EXISTS UserPatterns (" +
                    "   user_id INTEGER PRIMARY KEY," + // Users 테이블의 user_id를 참조하는 기본 키이자 외래 키
                    "   breakfast TEXT," +               // 아침 식사 시간 (예: "08:00")
                    "   lunch TEXT," +                   // 점심 식사 시간 (예: "12:30")
                    "   dinner TEXT," +                  // 저녁 식사 시간 (예: "19:00")
                    "   sleep TEXT," +                   // 잠자는 시간 (예: "23:00")
                    "   FOREIGN KEY (user_id) REFERENCES Users (user_id) ON DELETE CASCADE ON UPDATE CASCADE" +
                    ");" +
                    "CREATE TABLE IF NOT EXISTS Medicine (" +
                    "   med_id INTEGER PRIMARY KEY AUTOINCREMENT," + // 약 정보 고유 ID (자동 증가)
                    "   user_id INTEGER NOT NULL," +                 // 이 약을 등록한 사용자 ID (비어있으면 안됨)
                    "   med_name TEXT NOT NULL," +                   // 약 이름 (비어있으면 안됨)
                    "   med_daily_amount INTEGER NOT NULL," +       // 하루 복용 횟수 (비어있으면 안됨)
                    "   med_days TEXT NOT NULL," +                   // 복용하는 요일 (예: "월,화,수", "매일") (비어있으면 안됨)
                    "   med_condition TEXT NOT NULL," +             // 복용 조건 타입 (예: "식사", "잠자기") (비어있으면 안됨)
                    "   med_timing TEXT NOT NULL," +                 // 복용 시점 (예: "전", "후") (비어있으면 안됨)
                    "   med_minutes INTEGER NOT NULL," +             // 조건 시점으로부터 몇 분 (예: 30) (비어있으면 안됨)
                    "   color TEXT NOT NULL," +                     // 캘린더 표시 색상 코드 (예: "#FF0000") (비어있으면 안됨)
                    "   FOREIGN KEY (user_id) REFERENCES Users (user_id) ON DELETE CASCADE ON UPDATE CASCADE" +
                    ");" +
                    "CREATE TABLE IF NOT EXISTS DosageRecords (" +
                    "   record_id INTEGER PRIMARY KEY AUTOINCREMENT," + // 복용 기록 고유 ID (자동 증가)
                    "   user_id INTEGER NOT NULL," +                 // 이 기록의 사용자 ID (비어있으면 안됨)
                    "   med_id INTEGER NOT NULL," +                   // 이 기록 대상 약의 ID (비어있으면 안됨)
                    "   record_date TEXT NOT NULL," +                 // 복용 기록 날짜 (예: "YYYY-MM-DD") (비어있으면 안됨)
                    "   scheduled_time TEXT NOT NULL," +             // 복용 예정 시간 (예: "YYYY-MM-DD HH:MM") (비어있으면 안됨)
                    "   actual_taken_time TEXT," +                   // 실제 복용한 시간 (예: "YYYY-MM-DD HH:MM", 복용 안 했으면 NULL)
                    "   FOREIGN KEY (user_id) REFERENCES Users (user_id) ON DELETE CASCADE ON UPDATE CASCADE," +
                    "   FOREIGN KEY (med_id) REFERENCES Medicine (med_id) ON DELETE CASCADE ON UPDATE CASCADE," +
                    "   UNIQUE (user_id, med_id, record_date)" +     // 같은 사용자가 같은 날 같은 약 기록은 중복 불가
                    ");";

    private DBManager() {
    }

    /**
     * SQLite 데이터베이스 연결을 설정하고 Connection 객체를 반환합니다.
     * DB 파일이 없으면 자동으로 생성됩니다.
     * @return 유효한 Connection 객체
     * @throws SQLException 데이터베이스 접근 오류 발생 시
     */
    public static Connection getConnection() throws SQLException {
        Connection con = null;
        try {
            // DriverManager를 통해 DB_URL로 연결 시도
            con = DriverManager.getConnection(DB_URL);

            // 외래 키 제약을 활성화 (새로운 Connection마다 활성화하는 것이 안전)
            try (Statement stmt = con.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }

        } catch (SQLException e) {
            System.err.println("Database Connection Error: " + e.getMessage());
            e.printStackTrace(); // 오류 내용을 자세히 출력
            throw e; // 호출하는 곳에서 오류를 처리하도록 다시 던짐
        }
        return con;
    }

    /**
     * 데이터베이스 스키마가 존재하지 않으면(Users 테이블 기준) 스키마를 생성합니다.
     * 애플리케이션 시작 시 한 번 호출하여 DB 파일 및 테이블을 초기화합니다.
     */
    public static void initializeDatabase() {
        System.out.println("Initializing database..."); // 디버깅용 출력
        // try-with-resources 구문으로 Connection 객체를 자동으로 닫도록 처리
        try (Connection conn = getConnection()) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet tables = meta.getTables(null, null, "Users", null);

                if (!tables.next()) {
                    // "Users" 테이블이 존재하지 않으면 스키마 생성 구문 실행
                    System.out.println("Database schema not found. Creating schema..."); // 디버깅용 출력

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
                    System.out.println("Database schema already exists."); // 디버깅용 출력
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during database initialization connection: " + e.getMessage());
            e.printStackTrace();
            // 초기화 중 연결 오류 발생 시 처리
        }
    }

    /**
     * 주어진 데이터베이스 연결을 닫습니다.
     * 가능하다면 Connection 객체는 try-with-resources 구문으로 관리하는 것이 더 안전합니다.
     * @param conn 닫을 Connection 객체 (null일 수 있음)
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                // System.out.println("Database connection closed."); // 디버깅용 출력
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}