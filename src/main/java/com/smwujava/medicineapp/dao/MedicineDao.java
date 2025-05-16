package com.smwujava.medicineapp.dao;

import com.smwujava.medicineapp.db.DBManager; // DBManager 사용
import com.smwujava.medicineapp.model.Medicine; // Medicine 모델 사용
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Generated keys 가져올 때 필요
import java.util.ArrayList; // 목록을 담을 ArrayList
import java.util.List;      // 목록 인터페이스

// 약 정보(Medicine 테이블) 데이터 접근 객체 (DAO)
// 데이터베이스의 Medicine 테이블과 관련된 데이터 CRUD 작업을 수행합니다.
// 이 클래스는 static 메서드로 구성되며, 내부적으로 DBManager를 통해 Connection을 관리합니다.
public class MedicineDao {
    private MedicineDao() {
        // Utility 클래스처럼 사용되므로 인스턴스화 방지
    }

    /**
     * 새로운 약 정보를 데이터베이스의 Medicine 테이블에 삽입합니다.
     * @param medicine 삽입할 약 정보 (Medicine 객체, medId 제외)
     * @return 데이터베이스에서 자동 생성된 약의 med_id. 오류 발생 시 -1을 반환합니다.
     */
    public static int insertMedicine(Medicine medicine) { // static 메서드
        // SQL INSERT 구문. med_id는 AUTOINCREMENT이므로 제외.
        String sql = "INSERT INTO Medicine (user_id, med_name, med_daily_amount, med_days, med_condition, med_timing, med_minutes, color) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        int generatedId = -1; // 생성된 med_id를 저장할 변수

        try (Connection conn = DBManager.getConnection(); // DBManager에서 연결 가져오기 (자동 닫힘)
             // 자동 생성된 키(med_id)를 받아올 수 있도록 옵션 지정
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // SQL 구문의 ?에 실제 값을 설정 (인덱스는 1부터 시작)
            pstmt.setInt(1, medicine.getUserId());
            pstmt.setString(2, medicine.getMedName());
            pstmt.setInt(3, medicine.getMedDailyAmount());
            pstmt.setString(4, medicine.getMedDays());
            pstmt.setString(5, medicine.getMedCondition());
            pstmt.setString(6, medicine.getMedTiming());
            pstmt.setInt(7, medicine.getMedMinutes());
            pstmt.setString(8, medicine.getColor());

            // SQL 실행 (INSERT)
            int affectedRows = pstmt.executeUpdate();

            // 삽입된 행이 있고 자동 생성된 키가 있다면 가져옴
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) { // 자동 생성된 키 결과 (자동 닫힘)
                    if (rs.next()) {
                        generatedId = rs.getInt(1); // 첫 번째 자동 생성된 키(med_id) 가져오기
                        // System.out.println("Medicine inserted successfully with ID: " + generatedId); // 디버깅
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error inserting medicine for user ID " + medicine.getUserId() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return generatedId; // 생성된 ID 반환
    }

    /**
     * 특정 사용자 ID에 해당하는 모든 약 정보를 데이터베이스에서 조회합니다.
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자의 약 목록 (List<Medicine>). 약이 없으면 빈 목록을 반환합니다.
     */
    public static List<Medicine> findMedicinesByUserId(int userId) { // static 메서드
        // SQL SELECT 구문. 특정 user_id를 가진 모든 약 정보를 가져옴.
        String sql = "SELECT med_id, user_id, med_name, med_daily_amount, med_days, med_condition, med_timing, med_minutes, color FROM Medicine WHERE user_id = ?";
        List<Medicine> medicineList = new ArrayList<>(); // 조회된 약 정보를 담을 목록

        try (Connection conn = DBManager.getConnection(); // DBManager에서 연결 가져오기 (자동 닫힘)
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // PreparedStatement 생성 (자동 닫힘)

            pstmt.setInt(1, userId); // SQL 구문의 (?)에 사용자 ID 설정

            try (ResultSet rs = pstmt.executeQuery()) { // 쿼리 실행 및 결과 (자동 닫힘)
                // 조회된 결과 행이 있는 동안 반복하여 데이터를 읽어옴
                while (rs.next()) {
                    // ResultSet에서 컬럼 이름으로 값을 읽어와 Medicine 객체 생성
                    int medId = rs.getInt("med_id");
                    int foundUserId = rs.getInt("user_id");
                    String medName = rs.getString("med_name");
                    int medDailyAmount = rs.getInt("med_daily_amount");
                    String medDays = rs.getString("med_days");
                    String medCondition = rs.getString("med_condition");
                    String medTiming = rs.getString("med_timing");
                    int medMinutes = rs.getInt("med_minutes");
                    String color = rs.getString("color");

                    // Medicine 객체 생성 (DB에서 읽어온 모든 필드 포함)
                    Medicine medicine = new Medicine(medId, foundUserId, medName, medDailyAmount, medDays, medCondition, medTiming, medMinutes, color);
                    medicineList.add(medicine); // 목록에 추가
                }
                // System.out.println("Found " + medicineList.size() + " medicines for user ID " + userId); // 디버깅

            }

        } catch (SQLException e) {
            System.err.println("Error finding medicines by user ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
            // 오류 발생 시 빈 목록 반환하거나 예외 처리
        }
        return medicineList; // 약 목록 반환 (없으면 빈 목록)
    }

    /**
     * 기존 약 정보를 데이터베이스에서 수정합니다.
     * @param medicine 수정할 약 정보 (Medicine 객체, medId 필드 필수)
     * @return 수정 성공 시 true, 실패 시 false
     */
    public static boolean updateMedicine(Medicine medicine) { // static 메서드
        // SQL UPDATE 구문. med_id를 기준으로 해당 약 정보를 수정합니다.
        String sql = "UPDATE Medicine SET med_name = ?, med_daily_amount = ?, med_days = ?, med_condition = ?, med_timing = ?, med_minutes = ?, color = ? WHERE med_id = ?";

        try (Connection conn = DBManager.getConnection(); // DBManager에서 연결 가져오기 (자동 닫힘)
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // PreparedStatement 생성 (자동 닫힘)

            // SQL 구문의 ?에 실제 값을 설정
            pstmt.setString(1, medicine.getMedName());
            pstmt.setInt(2, medicine.getMedDailyAmount());
            pstmt.setString(3, medicine.getMedDays());
            pstmt.setString(4, medicine.getMedCondition());
            pstmt.setString(5, medicine.getMedTiming());
            pstmt.setInt(6, medicine.getMedMinutes());
            pstmt.setString(7, medicine.getColor());
            // WHERE 절의 조건을 위한 med_id 설정 (마지막 ?)
            pstmt.setInt(8, medicine.getMedId()); // 업데이트할 약의 ID

            // SQL 실행 (UPDATE)
            int affectedRows = pstmt.executeUpdate();

            // System.out.println("Medicine update affected rows for med ID " + medicine.getMedId() + ": " + affectedRows); // 디버깅
            return affectedRows > 0; // 1개 이상의 행에 영향이 있었다면 성공 (UPDATE는 1개 행에 영향을 줌)

        } catch (SQLException e) {
            System.err.println("Error updating medicine with ID " + medicine.getMedId() + ": " + e.getMessage());
            e.printStackTrace();
            return false; // 오류 발생 시 실패
        }
    }

    /**
     * 특정 약 ID에 해당하는 약 정보를 데이터베이스에서 삭제합니다.
     * @param medId 삭제할 약의 ID
     * @return 삭제 성공 시 true, 실패 시 false
     */
    public static boolean deleteMedicine(int medId) { // static 메서드
        // SQL DELETE 구문. 특정 med_id를 가진 행을 삭제합니다.
        String sql = "DELETE FROM Medicine WHERE med_id = ?";

        try (Connection conn = DBManager.getConnection(); // DBManager에서 연결 가져오기 (자동 닫힘)
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // PreparedStatement 생성 (자동 닫힘)

            // SQL 구문의 (?)에 삭제할 약의 ID 설정
            pstmt.setInt(1, medId);

            // SQL 실행 (DELETE)
            int affectedRows = pstmt.executeUpdate();

            // System.out.println("Medicine delete affected rows for med ID " + medId + ": " + affectedRows); // 디버깅
            return affectedRows > 0; // 1개 이상의 행에 영향이 있었다면 성공 (DELETE는 1개 행에 영향을 줌)

        } catch (SQLException e) {
            System.err.println("Error deleting medicine with ID " + medId + ": " + e.getMessage());
            e.printStackTrace();
            return false; // 오류 발생 시 실패
        }
    }
    // TODO: 필요에 따라 다른 데이터 접근 메서드를 추가할 수 있습니다.
    // 예를 들어:
    // - 약 ID로 하나의 약 정보를 조회하는 메서드: findMedicineById(int medId)
}
