//UserPatternDao
package com.smwujava.medicineapp.dao;

import com.smwujava.medicineapp.db.DBManager;
import com.smwujava.medicineapp.model.UserPattern;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.smwujava.medicineapp.util.DBUtil;


public class UserPatternDao {

    // 최근 일주일간 복용 지연 횟수 조회 (예시 구현)
    public int getLateCountLastWeek(int userId) {
        String sql = "SELECT COUNT(*) FROM dosage_records " +
                "WHERE user_id = ? AND delay_minutes > 0 AND dosage_date >= DATE_SUB(NOW(), INTERVAL 7 DAY)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next())
                return rs.getInt(1); // COUNT(*) 결과

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0; // 오류 시 0 반환
    }

    // 사용자의 평균 지연 복용 시간 조회 (예시 구현)
    public int getAverageDelayMinutesByUser(int userId) {
        String sql = "SELECT AVG(delay_minutes) FROM dosage_records " +
                "WHERE user_id = ? AND delay_minutes > 0";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next())
                return rs.getInt(1); // 평균 minutes

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0; // 오류 시 0 반환
    }

    public boolean insertOrUpdatePattern(UserPattern pattern) {
        // SQL 쿼리 변경: 각 패턴 필드가 _start와 _end로 분리됨
        String sql = "INSERT OR REPLACE INTO UserPatterns (user_id, " +
                "breakfast_start, breakfast_end, lunch_start, lunch_end, " +
                "dinner_start, dinner_end, sleep_start, sleep_end) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // SQL 구문의 ?에 실제 값을 설정 (인덱스는 1부터 시작)
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
        // SQL 쿼리 변경: 각 패턴 필드가 _start와 _end로 분리됨
        String sql = "SELECT user_id, breakfast_start, breakfast_end, lunch_start, lunch_end, " +
                "dinner_start, dinner_end, sleep_start, sleep_end FROM UserPatterns WHERE user_id = ?";
        UserPattern pattern = null;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // ResultSet에서 변경된 컬럼 이름으로 값을 읽어옵니다.
                    int foundUserId = rs.getInt("user_id");
                    String breakfastStartTime = rs.getString("breakfast_start");
                    String breakfastEndTime = rs.getString("breakfast_end");
                    String lunchStartTime = rs.getString("lunch_start");
                    String lunchEndTime = rs.getString("lunch_end");
                    String dinnerStartTime = rs.getString("dinner_start");
                    String dinnerEndTime = rs.getString("dinner_end");
                    String sleepStartTime = rs.getString("sleep_start");
                    String sleepEndTime = rs.getString("sleep_end");

                    // UserPattern 객체 생성 시 변경된 생성자 또는 setter 사용
                    pattern = new UserPattern(foundUserId,
                            breakfastStartTime, breakfastEndTime,
                            lunchStartTime, lunchEndTime,
                            dinnerStartTime, dinnerEndTime,
                            sleepStartTime, sleepEndTime);
                } else {
                    // 해당 user_id에 대한 패턴 정보가 없는 경우
                    // System.out.println("No user pattern found for user ID: " + userId); // 디버깅용
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user pattern by user ID " + userId + ": " + e.getMessage());
            throw e; // 오류 발생 시 예외 다시 던지기
        }
        return pattern;
    }

    public boolean deletePatternByUserId(int userId) throws SQLException {
        String sql = "DELETE FROM UserPatterns WHERE user_id = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user pattern for user ID " + userId + ": " + e.getMessage());
            throw e;
        }
    }
}