package com.smwujava.medicineapp.dao;

import com.smwujava.medicineapp.db.DBManager; // DBManager 클래스를 사용하기 위해 import
import com.smwujava.medicineapp.model.UserPattern; // UserPattern 모델 클래스를 사용하기 위해 import
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// 사용자 생활 패턴(UserPatterns 테이블) 데이터 접근 객체 (DAO)
// 데이터베이스의 UserPatterns 테이블과 관련된 데이터 CRUD 작업을 수행합니다.
// 이 클래스는 static 메서드로 구성되며, 내부적으로 DBManager를 통해 Connection을 관리합니다.
public class UserPatternDao {

    private UserPatternDao() {
        // Utility 클래스처럼 사용되므로 인스턴스화 방지
    }
    /**
     * 사용자의 생활 패턴 정보를 데이터베이스에 삽입하거나 이미 해당 user_id의 패턴이 존재하면 업데이트합니다.
     * UserPatterns 테이블은 user_id를 기본 키로 가지며 1:1 관계이므로,
     * SQLite의 INSERT OR REPLACE 구문을 사용하여 간편하게 처리합니다.
     * @param pattern 삽입 또는 업데이트할 생활 패턴 정보 (UserPattern 객체, 반드시 userId 필드 포함)
     * @return 데이터베이스 작업 성공 시 true, 실패 시 false
     */
    public static boolean insertOrUpdatePattern(UserPattern pattern) { // static 메서드
        // SQLite의 INSERT OR REPLACE 구문:
        // user_id가 이미 존재하는 행이 있다면 해당 행을 삭제하고 새 값을 가진 새 행을 삽입합니다.
        // user_id가 존재하지 않으면 일반 INSERT처럼 새 행을 삽입합니다.
        String sql = "INSERT OR REPLACE INTO UserPatterns (user_id, breakfast, lunch, dinner, sleep) VALUES (?, ?, ?, ?, ?)";

        // try-with-resources 구문으로 Connection과 PreparedStatement를 안전하게 관리
        try (Connection conn = DBManager.getConnection(); // DBManager에서 연결 가져오기 (자동 닫힘)
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // PreparedStatement 생성 (자동 닫힘)

            // SQL 구문의 ?에 실제 값을 설정 (인덱스는 1부터 시작)
            pstmt.setInt(1, pattern.getUserId());       // 첫 번째 (?)에 user_id 설정
            pstmt.setString(2, pattern.getBreakfast()); // 두 번째 (?)에 breakfast 시간 설정
            pstmt.setString(3, pattern.getLunch());     // 세 번째 (?)에 lunch 시간 설정
            pstmt.setString(4, pattern.getDinner());    // 네 번째 (?)에 dinner 시간 설정
            pstmt.setString(5, pattern.getSleep());     // 다섯 번째 (?)에 sleep 시간 설정

            // SQL 실행 (INSERT 또는 REPLACE)
            // executeUpdate()는 영향을 받은 행 개수 (INSERT 또는 REPLACE된 행 개수)를 반환
            int affectedRows = pstmt.executeUpdate();

            // System.out.println("Pattern insert/update affected rows for user ID " + pattern.getUserId() + ": " + affectedRows); // 디버깅
            return affectedRows > 0; // 1개 이상의 행에 영향이 있었다면 성공 (INSERT/REPLACE는 1개 행에 영향을 줌)

        } catch (SQLException e) {
            // 데이터베이스 오류 발생 시 처리
            System.err.println("Error inserting or updating user pattern for user ID " + pattern.getUserId() + ": " + e.getMessage());
            e.printStackTrace(); // 오류 내용을 자세히 출력
            return false; // 오류 발생 시 실패
        }
    }

    /**
     * 특정 사용자 ID에 해당하는 생활 패턴 정보를 데이터베이스에서 조회합니다.
     * @param userId 조회할 사용자의 ID
     * @return 데이터베이스에서 찾은 UserPattern 객체. 해당 사용자의 패턴 정보가 없으면 null을 반환합니다.
     */
    public static UserPattern findPatternByUserId(int userId) { // static 메서드
        // SQL SELECT 구문. user_id가 일치하는 행을 찾습니다.
        String sql = "SELECT user_id, breakfast, lunch, dinner, sleep FROM UserPatterns WHERE user_id = ?";
        UserPattern pattern = null; // 데이터베이스에서 찾은 패턴 정보를 담을 변수 (기본값 null)

        // try-with-resources 구문으로 Connection, PreparedStatement, ResultSet 안전하게 관리
        try (Connection conn = DBManager.getConnection(); // DBManager에서 연결 가져오기 (자동 닫힘)
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // PreparedStatement 생성 (자동 닫힘)

            // SQL 구문의 (?)에 찾을 사용자 ID 설정
            pstmt.setInt(1, userId);

            // SQL 실행 (SELECT)
            try (ResultSet rs = pstmt.executeQuery()) { // 쿼리 결과 (자동 닫힘)
                // user_id는 기본 키이므로 결과는 최대 한 행입니다.
                if (rs.next()) {
                    // ResultSet에서 컬럼 이름으로 값을 읽어와 UserPattern 객체 생성
                    int foundUserId = rs.getInt("user_id");
                    String breakfast = rs.getString("breakfast");
                    String lunch = rs.getString("lunch");
                    String dinner = rs.getString("dinner");
                    String sleep = rs.getString("sleep");

                    pattern = new UserPattern(foundUserId, breakfast, lunch, dinner, sleep); // UserPattern 객체 생성
                    // System.out.println("Pattern found for user ID: " + foundUserId); // 디버깅
                } else {
                    // System.out.println("Pattern not found for user ID: " + userId); // 디버깅
                }
            }

        } catch (SQLException e) {
            // 데이터베이스 오류 발생 시 처리
            System.err.println("Error finding user pattern by user ID " + userId + ": " + e.getMessage());
            e.printStackTrace(); // 오류 내용을 자세히 출력
            return null; // 오류 발생 시 null 반환
        }
        return pattern; // 찾은 UserPattern 객체 또는 null 반환
    }
    // TODO: 필요에 따라 다른 데이터 접근 메서드를 추가할 수 있습니다.
    // 예를 들어:
    // - 사용자 패턴 삭제 메서드: deletePatternByUserId(int userId) - Users 테이블 삭제 시 ON DELETE CASCADE에 의해 자동 삭제되므로 별도로 필요 없을 수도 있습니다.
}