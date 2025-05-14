package com.smwujava.medicineapp.dao;

import com.smwujava.medicineapp.model.User;

import java.sql.*;

public class UserDAO {
    private final Connection conn;

    public UserDAO(Connection conn) {
        this.conn = conn;
    }

    // Users 테이블이 없으면 자동 생성
    public void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS Users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL UNIQUE, " +
                "password TEXT NOT NULL)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    // 특정 username이 이미 존재하는지 확인 (중복 가입 방지용)
    public boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // 회원 정보 DB에 추가
    public boolean insertUser(User user) throws SQLException {
        String sql = "INSERT INTO Users (username, password) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            return pstmt.executeUpdate() > 0;
        }
    }

    // 로그인 (username + password 일치 시 User 반환)
    public User findUserByUsernameAndPassword(String username, String password) throws SQLException {
        String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password")
                    );
                }
            }
        }
        return null;
    }

    // 비밀번호로 아이디 찾기
    public String findUsernameByPassword(String password) throws SQLException {
        String sql = "SELECT username FROM Users WHERE password = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        }
        return null;
    }

    // 아이디로 비밀번호 찾기
    public String findPasswordByUsername(String username) throws SQLException {
        String sql = "SELECT password FROM Users WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password");
                }
            }
        }
        return null;
    }

    // 사용자 비밀번호 수정
    public boolean updatePassword(String username, String newPassword) throws SQLException {
        String sql = "UPDATE Users SET password = ? WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        }
    }

    // 사용자 이름 수정
    public boolean updateUsername(String currentUsername, String newUsername) throws SQLException {
        String sql = "UPDATE Users SET username = ? WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newUsername);
            pstmt.setString(2, currentUsername);
            return pstmt.executeUpdate() > 0;
        }
    }
}





