package com.smwujava.medicineapp.dao;

import com.smwujava.medicineapp.db.DBManager;
import com.smwujava.medicineapp.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

public class UserDao {
    private UserDao() {
    }

    /**
     * 새로운 사용자를 데이터베이스의 Users 테이블에 삽입합니다 (회원가입 시 사용).
     * @param user 삽입할 사용자 정보 (User 객체, username, password, autoLogin, isAdmin 필드 필요)
     * @return 데이터베이스에서 자동 생성된 사용자의 user_id. 오류 발생 시 -1을 반환합니다. */
    public static int insertUser(User user) throws SQLException {
        String sql = "INSERT INTO Users (username, password, auto_login, is_admin) VALUES (?, ?, ?, ?)";
        int generatedId = -1;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setInt(3, user.isAutoLogin() ? 1 : 0);
            pstmt.setInt(4, user.isAdmin() ? 1 : 0);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
            throw e;
        }
        return generatedId;
    }

    /**
     * 특정 사용자 이름으로 사용자를 데이터베이스에서 찾습니다. 로그인 시 사용됩니다.
     * @param username 찾을 사용자 이름
     * @return 데이터베이스에서 찾은 User 객체. 해당 사용자 이름의 사용자가 없으면 null을 반환합니다. */
    public static User findUserByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, username, password, auto_login, is_admin FROM Users WHERE username = ?";
        User user = null;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String foundUsername = rs.getString("username");
                    String foundPassword = rs.getString("password");
                    boolean autoLogin = rs.getBoolean("auto_login");
                    boolean isAdmin = rs.getBoolean("is_admin");

                    user = new User(userId, foundUsername, foundPassword, autoLogin, isAdmin);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by username " + username + ": " + e.getMessage());
            throw e;
        }
        return user;
    }

    /**
     * 특정 ID로 사용자를 데이터베이스에서 찾습니다.
     * @param userId 찾을 사용자 ID
     * @return 데이터베이스에서 찾은 User 객체. 해당 ID의 사용자가 없으면 null을 반환합니다. */
    public static User findUserById(int userId) throws SQLException {
        String sql = "SELECT user_id, username, password, auto_login, is_admin FROM Users WHERE user_id = ?";
        User user = null;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int foundUserId = rs.getInt("user_id");
                    String foundUsername = rs.getString("username");
                    String foundPassword = rs.getString("password");
                    boolean autoLogin = rs.getBoolean("auto_login");
                    boolean isAdmin = rs.getBoolean("is_admin");

                    user = new User(foundUserId, foundUsername, foundPassword, autoLogin, isAdmin);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by ID " + userId + ": " + e.getMessage());
            throw e;
        }
        return user;
    }

    /**
     * 사용자 정보를 업데이트합니다. (현재는 비밀번호와 자동 로그인 여부만 업데이트)
     * @param user 업데이트할 사용자 정보 (User 객체, userId, password, autoLogin 필드 필요)
     * @return 업데이트 성공 시 true, 실패 시 false */
    public static boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE Users SET username = ?, password = ?, auto_login = ? WHERE user_id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setInt(3, user.isAutoLogin() ? 1 : 0);
            pstmt.setInt(4, user.getUserId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user with ID " + user.getUserId() + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * 특정 사용자 ID에 해당하는 사용자를 데이터베이스에서 삭제합니다.
     * @param userId 삭제할 사용자 ID
     * @return 삭제 성공 시 true, 실패 시 false */
    public static boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM Users WHERE user_id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user with ID " + userId + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * 특정 사용자 ID의 자동 로그인 설정을 업데이트합니다.
     * @param userId 자동 로그인 설정을 변경할 사용자 ID
     * @param autoLogin true면 자동 로그인 활성화, false면 비활성화
     * @return 업데이트 성공 시 true, 실패 시 false */
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
     * @return 자동 로그인 설정된 사용자 User 객체, 없으면 null */
    public static User findAutoLoginUser() throws SQLException {
        String sql = "SELECT user_id, username, password, auto_login, is_admin FROM Users WHERE auto_login = 1 LIMIT 1";
        User user = null;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String foundUsername = rs.getString("username");
                    String foundPassword = rs.getString("password");
                    boolean autoLogin = rs.getBoolean("auto_login");
                    boolean isAdmin = rs.getBoolean("is_admin");
                    user = new User(userId, foundUsername, foundPassword, autoLogin, isAdmin);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding auto login user: " + e.getMessage());
            throw e;
        }
        return user;
    }

    /**
     * 모든 일반 사용자 계정 정보를 가져옵니다. 관리자 페이지에서 사용자 목록을 보여줄 때 사용합니다.
     * @return 일반 사용자 User 객체 리스트 */
    public static List<User> getAllNormalUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password, auto_login, is_admin FROM Users WHERE is_admin = 0 ORDER BY user_id ASC";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String username = rs.getString("username");
                String password = rs.getString("password");
                boolean autoLogin = rs.getBoolean("auto_login");
                boolean isAdmin = rs.getBoolean("is_admin");

                users.add(new User(userId, username, password, autoLogin, isAdmin));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all normal users: " + e.getMessage());
            throw e;
        }
        return users;
    }

    /**
     * 시스템에 등록된 모든 일반 사용자(관리자 제외)의 수를 가져옵니다.
     * @return 일반 사용자의 총 수 */
    public static int getTotalNormalUserCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE is_admin = 0";
        int count = 0;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting total normal user count: " + e.getMessage());
            throw e;
        }
        return count;
    }
}