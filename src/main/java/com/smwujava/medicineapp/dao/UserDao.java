package com.smwujava.medicineapp.dao;

import com.smwujava.medicineapp.db.DBManager;
import com.smwujava.medicineapp.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // 자동 생성된 키 가져올 때 필요

public class UserDao {

    private UserDao() {
    }

    /**
     * Users 테이블이 없으면 생성합니다. (자동 로그인 기능을 위한 auto_login 컬럼 추가)
     * 이 메서드는 애플리케이션 시작 시 한 번 호출되어야 합니다.
     */
    public static void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS Users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL UNIQUE, " +
                "password TEXT NOT NULL, " +
                "auto_login INTEGER DEFAULT 0" + // SQLite에서 BOOLEAN은 INTEGER로 저장됩니다 (0: false, 1: true)
                ")";
        try (Connection conn = DBManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * 새로운 사용자를 데이터베이스의 Users 테이블에 삽입합니다 (회원가입 시 사용).
     * @param user 삽입할 사용자 정보 (User 객체, username, password, autoLogin 필드 필요)
     * @return 데이터베이스에서 자동 생성된 사용자의 user_id. 삽입 실패 시 (영향 받은 행 없음 등) -1.
     */
    public static int insertUser(User user) throws SQLException {
        String sql = "INSERT INTO Users (username, password, auto_login) VALUES (?, ?, ?)";
        int generatedId = -1;

        try (Connection conn = DBManager.getConnection();
             // PreparedStatement 생성 (자동 생성된 키를 받아올 수 있도록 Statement.RETURN_GENERATED_KEYS 옵션 명시적으로 추가)
             // SQLite는 일반적으로 필요 없지만, 명시적인 것은 다른 DB 시스템에서도 호환성을 높입니다.
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // SQL 구문의 ?에 실제 값을 설정 (인덱스는 1부터 시작)
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setBoolean(3, user.isAutoLogin());

            int affectedRows = pstmt.executeUpdate();

            // 삽입된 행이 있고 자동 생성된 키가 있다면 가져옴
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                    }
                }
            }
        }
        return generatedId;
    }

    /**
     * 사용자 이름(username)이 데이터베이스에 이미 존재하는지 확인합니다 (회원가입 시 중복 확인).
     * @param username 확인할 사용자 이름
     * @return 해당 username이 존재하면 true, 그렇지 않으면 false.
     */
    public static boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE username = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        }
    }

    /**
     * 사용자 이름과 비밀번호가 일치하는 사용자를 찾아 정보를 가져옵니다 (로그인 시 사용).
     * @param username 찾을 사용자 이름
     * @param password 확인할 비밀번호 (DB에 해시화되어 있다면 해시화된 비밀번호를 전달해야 함)
     * @return 일치하는 사용자 정보(User 객체) 또는 사용자가 없거나 비밀번호가 틀릴 경우 null.
     */
    public static User findUserByUsernameAndPassword(String username, String password) throws SQLException {
        String sql = "SELECT user_id, username, password, auto_login FROM Users WHERE username = ? AND password = ?";
        User user = null;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String foundUsername = rs.getString("username");
                    String foundPassword = rs.getString("password");
                    boolean autoLogin = rs.getBoolean("auto_login");

                    user = new User(userId, foundUsername, foundPassword, autoLogin);
                }
            }

        }
        return user;
    }

    /**
     * 특정 사용자의 비밀번호를 수정합니다.
     * @param username 비밀번호를 수정할 사용자의 이름
     * @param newPassword 새로 설정할 비밀번호 (DB에 해시화하여 저장해야 함)
     * @return 수정 성공 시 true, 실패 시 false (영향 받은 행 없음 등).
     */
    public static boolean updatePassword(String username, String newPassword) throws SQLException {
        String sql = "UPDATE Users SET password = ? WHERE username = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        }
    }

    /**
     * 특정 사용자의 사용자 이름(username)을 수정합니다.
     * @param currentUsername 현재 사용자 이름
     * @param newUsername 새로 설정할 사용자 이름
     * @return 수정 성공 시 true, 실패 시 false (영향 받은 행 없음 등).
     */
    public static boolean updateUsername(String currentUsername, String newUsername) throws SQLException {
        String sql = "UPDATE Users SET username = ? WHERE username = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newUsername);
            pstmt.setString(2, currentUsername);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        }
    }

    /**
     * 특정 사용자의 자동 로그인 설정을 업데이트합니다.
     * @param userId 업데이트할 사용자의 ID
     * @param autoLogin 자동 로그인 설정 여부 (true/false)
     * @return 업데이트 성공 시 true, 실패 시 false
     */
    public static boolean updateAutoLoginStatus(int userId, boolean autoLogin) throws SQLException {
        String sql = "UPDATE Users SET auto_login = ? WHERE user_id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, autoLogin);
            pstmt.setInt(2, userId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 자동 로그인 설정이 되어 있는 사용자를 찾습니다. (가장 최근에 자동 로그인한 사용자)
     * @return 자동 로그인 설정된 사용자 User 객체, 없으면 null
     * @throws SQLException 데이터베이스 접근 또는 SQL 실행 중 오류 발생 시
     */
    public static User findAutoLoginUser() throws SQLException {
        String sql = "SELECT user_id, username, password, auto_login FROM Users WHERE auto_login = 1 LIMIT 1";
        User user = null;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String foundUsername = rs.getString("username");
                    String foundPassword = rs.getString("password");
                    boolean autoLogin = rs.getBoolean("auto_login");
                    user = new User(userId, foundUsername, foundPassword, autoLogin);
                }
            }
        }
        return user;
    }
}