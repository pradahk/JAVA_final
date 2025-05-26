package com.smwujava.medicineapp.dao;

import com.smwujava.medicineapp.db.DBManager;
import com.smwujava.medicineapp.model.UserPattern;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserPatternDao {

    private UserPatternDao() {
    }
    /**
     * 사용자의 생활 패턴 정보를 데이터베이스에 삽입하거나 이미 해당 user_id의 패턴이 존재하면 업데이트합니다.
     * UserPatterns 테이블은 user_id를 기본 키로 가지며 1:1 관계이므로, SQLite의 INSERT OR REPLACE 구문으로 간편하게 처리합니다.
     * @param pattern 삽입 또는 업데이트할 생활 패턴 정보 (UserPattern 객체, 반드시 userId 필드 포함)
     * @return 데이터베이스 작업 성공 시 true, 실패 시 false
     */
    public static boolean insertOrUpdatePattern(UserPattern pattern) {
        String sql = "INSERT OR REPLACE INTO UserPatterns (user_id, breakfast, lunch, dinner, sleep) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // SQL 구문의 ?에 실제 값을 설정 (인덱스는 1부터 시작)
            pstmt.setInt(1, pattern.getUserId());       // 첫 번째 (?)에 user_id 설정
            pstmt.setString(2, pattern.getBreakfast()); // 두 번째 (?)에 breakfast 시간 설정
            pstmt.setString(3, pattern.getLunch());     // 세 번째 (?)에 lunch 시간 설정
            pstmt.setString(4, pattern.getDinner());    // 네 번째 (?)에 dinner 시간 설정
            pstmt.setString(5, pattern.getSleep());     // 다섯 번째 (?)에 sleep 시간 설정

            // executeUpdate()는 영향을 받은 행 개수 (INSERT 또는 REPLACE된 행 개수)를 반환
            int affectedRows = pstmt.executeUpdate();

            return affectedRows > 0; // 1개 이상의 행에 영향이 있었다면 성공 (INSERT/REPLACE는 1개 행에 영향을 줌)

        } catch (SQLException e) {
            System.err.println("Error inserting or updating user pattern for user ID " + pattern.getUserId() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 특정 사용자 ID에 해당하는 생활 패턴 정보를 데이터베이스에서 조회합니다.
     * @param userId 조회할 사용자의 ID
     * @return 데이터베이스에서 찾은 UserPattern 객체. 해당 사용자의 패턴 정보가 없으면 null을 반환합니다.
     */
    public static UserPattern findPatternByUserId(int userId) {
        String sql = "SELECT user_id, breakfast, lunch, dinner, sleep FROM UserPatterns WHERE user_id = ?";
        UserPattern pattern = null; // 데이터베이스에서 찾은 패턴 정보를 담을 변수 (기본값 null)

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // SQL 구문의 (?)에 찾을 사용자 ID 설정
            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                // user_id는 기본 키이므로 결과는 최대 한 행입니다.
                if (rs.next()) {
                    // ResultSet에서 컬럼 이름으로 값을 읽어와 UserPattern 객체 생성
                    int foundUserId = rs.getInt("user_id");
                    String breakfast = rs.getString("breakfast");
                    String lunch = rs.getString("lunch");
                    String dinner = rs.getString("dinner");
                    String sleep = rs.getString("sleep");

                    pattern = new UserPattern(foundUserId, breakfast, lunch, dinner, sleep);
                } else {
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user pattern by user ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return pattern;
    }
}