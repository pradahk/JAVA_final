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