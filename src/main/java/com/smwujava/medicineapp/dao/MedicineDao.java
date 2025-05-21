package com.smwujava.medicineapp.dao;

import com.smwujava.medicineapp.db.DBManager; // DBManager 사용
import com.smwujava.medicineapp.model.Medicine; // Medicine 모델 사용
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException; // SQLException 필요
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
     * @throws SQLException 데이터베이스 접근 오류 발생 시 추가 (throws 선언)
     */
    public static int insertMedicine(Medicine medicine) throws SQLException { // throws SQLException 추가
        String sql = "INSERT INTO Medicine (user_id, med_name, med_daily_amount, med_days, med_condition, med_timing, med_minutes, color) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        int generatedId = -1;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, medicine.getUserId());
            pstmt.setString(2, medicine.getMedName());
            pstmt.setInt(3, medicine.getMedDailyAmount());
            pstmt.setString(4, medicine.getMedDays());
            pstmt.setString(5, medicine.getMedCondition());
            pstmt.setString(6, medicine.getMedTiming());
            pstmt.setInt(7, medicine.getMedMinutes());
            pstmt.setString(8, medicine.getColor());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) { // 예외를 잡아서 로깅 후 다시 던집니다.
            System.err.println("Error inserting medicine for user ID " + medicine.getUserId() + ": " + e.getMessage());
            e.printStackTrace();
            throw e; // 예외를 호출하는 곳으로 다시 던집니다.
        }
        return generatedId;
    }

    /**
     * 특정 사용자 ID에 해당하는 모든 약 정보를 데이터베이스에서 조회합니다.
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자의 약 목록 (List<Medicine>). 약이 없으면 빈 목록을 반환합니다.
     * @throws SQLException 데이터베이스 접근 오류 발생 시 추가 (throws 선언)
     */
    public static List<Medicine> findMedicinesByUserId(int userId) throws SQLException { // throws SQLException 추가
        String sql = "SELECT med_id, user_id, med_name, med_daily_amount, med_days, med_condition, med_timing, med_minutes, color FROM Medicine WHERE user_id = ?";
        List<Medicine> medicineList = new ArrayList<>();

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int medId = rs.getInt("med_id");
                    int foundUserId = rs.getInt("user_id");
                    String medName = rs.getString("med_name");
                    int medDailyAmount = rs.getInt("med_daily_amount");
                    String medDays = rs.getString("med_days");
                    String medCondition = rs.getString("med_condition");
                    String medTiming = rs.getString("med_timing");
                    int medMinutes = rs.getInt("med_minutes");
                    String color = rs.getString("color");

                    Medicine medicine = new Medicine(medId, foundUserId, medName, medDailyAmount, medDays, medCondition, medTiming, medMinutes, color);
                    medicineList.add(medicine);
                }
            }
        } catch (SQLException e) { // 예외를 잡아서 로깅 후 다시 던집니다.
            System.err.println("Error finding medicines by user ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
            throw e; // 예외를 호출하는 곳으로 다시 던집니다.
        }
        return medicineList;
    }

    /**
     * 기존 약 정보를 데이터베이스에서 수정합니다.
     * @param medicine 수정할 약 정보 (Medicine 객체, medId 필드 필수)
     * @return 수정 성공 시 true, 실패 시 false
     * @throws SQLException 데이터베이스 접근 오류 발생 시 추가 (throws 선언)
     */
    public static boolean updateMedicine(Medicine medicine) throws SQLException { // throws SQLException 추가
        String sql = "UPDATE Medicine SET med_name = ?, med_daily_amount = ?, med_days = ?, med_condition = ?, med_timing = ?, med_minutes = ?, color = ? WHERE med_id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, medicine.getMedName());
            pstmt.setInt(2, medicine.getMedDailyAmount());
            pstmt.setString(3, medicine.getMedDays());
            pstmt.setString(4, medicine.getMedCondition());
            pstmt.setString(5, medicine.getMedTiming());
            pstmt.setInt(6, medicine.getMedMinutes());
            pstmt.setString(7, medicine.getColor());
            pstmt.setInt(8, medicine.getMedId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) { // 예외를 잡아서 로깅 후 다시 던집니다.
            System.err.println("Error updating medicine with ID " + medicine.getMedId() + ": " + e.getMessage());
            e.printStackTrace();
            throw e; // 예외를 호출하는 곳으로 다시 던집니다.
        }
    }

    /**
     * 특정 약 ID에 해당하는 약 정보를 데이터베이스에서 삭제합니다.
     * @param medId 삭제할 약의 ID
     * @return 삭제 성공 시 true, 실패 시 false
     * @throws SQLException 데이터베이스 접근 오류 발생 시 추가 (throws 선언)
     */
    public static boolean deleteMedicine(int medId) throws SQLException { // throws SQLException 추가
        String sql = "DELETE FROM Medicine WHERE med_id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, medId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) { // 예외를 잡아서 로깅 후 다시 던집니다.
            System.err.println("Error deleting medicine with ID " + medId + ": " + e.getMessage());
            e.printStackTrace();
            throw e; // 예외를 호출하는 곳으로 다시 던집니다.
        }
    }
}