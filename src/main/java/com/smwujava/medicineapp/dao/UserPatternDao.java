package com.smwujava.medicineapp.dao;

import com.smwujava.medicineapp.db.DBManager;
import com.smwujava.medicineapp.model.UserPattern;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserPatternDao {
    public boolean insertOrUpdatePattern(UserPattern pattern) {
        String sql = "INSERT OR REPLACE INTO UserPatterns (user_id, " +
                "breakfast_start, breakfast_end, lunch_start, lunch_end, " +
                "dinner_start, dinner_end, sleep_start, sleep_end) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pattern.getUserId());
            pstmt.setString(2, pattern.getBreakfastStartTime()); // breakfast_start
            pstmt.setString(3, pattern.getBreakfastEndTime());   // breakfast_end
            pstmt.setString(4, pattern.getLunchStartTime());     // lunch_start
            pstmt.setString(5, pattern.getLunchEndTime());       // lunch_end
            pstmt.setString(6, pattern.getDinnerStartTime());    // dinner_start
            pstmt.setString(7, pattern.getDinnerEndTime());      // dinner_end
            pstmt.setString(8, pattern.getSleepStartTime());     // sleep_start
            pstmt.setString(9, pattern.getSleepEndTime());       // sleep_end

            int affectedRows = pstmt.executeUpdate();
            System.out.println("패턴저장" + pattern);
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting or updating user pattern: " + e.getMessage());

            return false;
        }
    }

    public UserPattern findPatternByUserId(int userId) throws SQLException {
        String sql = "SELECT user_id, breakfast_start, breakfast_end, lunch_start, lunch_end, " +
                "dinner_start, dinner_end, sleep_start, sleep_end FROM UserPatterns WHERE user_id = ?";
        UserPattern pattern = null;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int foundUserId = rs.getInt("user_id");
                    String breakfastStartTime = rs.getString("breakfast_start");
                    String breakfastEndTime = rs.getString("breakfast_end");
                    String lunchStartTime = rs.getString("lunch_start");
                    String lunchEndTime = rs.getString("lunch_end");
                    String dinnerStartTime = rs.getString("dinner_start");
                    String dinnerEndTime = rs.getString("dinner_end");
                    String sleepStartTime = rs.getString("sleep_start");
                    String sleepEndTime = rs.getString("sleep_end");

                    pattern = new UserPattern(foundUserId,
                            breakfastStartTime, breakfastEndTime,
                            lunchStartTime, lunchEndTime,
                            dinnerStartTime, dinnerEndTime,
                            sleepStartTime, sleepEndTime);
                } else {
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user pattern by user ID " + userId + ": " + e.getMessage());
            throw e;
        }
        return pattern;
    }
}