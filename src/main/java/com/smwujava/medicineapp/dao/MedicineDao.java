package com.smwujava.medicineapp.dao;

import com.smwujava.medicineapp.db.DBManager;
import com.smwujava.medicineapp.model.Medicine;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MedicineDao {
    /**
     * 새로운 약 정보를 데이터베이스의 Medicine 테이블에 삽입합니다.
     * @param medicine 삽입할 약 정보 (Medicine 객체, medId 제외)
     * @return 데이터베이스에서 자동 생성된 약의 med_id. 오류 발생 시 -1을 반환합니다. */
    public static int insertMedicine(Medicine medicine) throws SQLException {
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
        } catch (SQLException e) {
            System.err.println("Error inserting medicine: " + e.getMessage());
            throw e;
        }
        return generatedId;
    }

    /**
     * 특정 medId를 가진 약 정보를 데이터베이스에서 조회합니다.
     * @param medId 조회할 약의 ID
     * @return 해당 medId에 해당하는 Medicine 객체. 없으면 null을 반환합니다. */
    public Medicine findMedicineById(int medId) throws SQLException {
        String sql = "SELECT * FROM Medicine WHERE med_id = ?";
        Medicine medicine = null;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, medId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    medicine = new Medicine(
                            rs.getInt("med_id"),
                            rs.getInt("user_id"),
                            rs.getString("med_name"),
                            rs.getInt("med_daily_amount"),
                            rs.getString("med_days"),
                            rs.getString("med_condition"),
                            rs.getString("med_timing"),
                            rs.getInt("med_minutes"),
                            rs.getString("color")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding medicine by ID " + medId + ": " + e.getMessage());
            throw e;
        }
        return medicine;
    }

    /**
     * 특정 사용자 ID에 해당하는 모든 약 정보를 데이터베이스에서 조회합니다.
     * @param userId 조회할 사용자의 ID
     * @return 해당 userId에 해당하는 Medicine 객체 리스트. 없으면 빈 리스트를 반환합니다. */
    public List<Medicine> findMedicinesByUserId(int userId) throws SQLException {
        List<Medicine> medicines = new ArrayList<>();
        String sql = "SELECT * FROM Medicine WHERE user_id = ? ORDER BY med_id ASC";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    medicines.add(new Medicine(
                            rs.getInt("med_id"),
                            rs.getInt("user_id"),
                            rs.getString("med_name"),
                            rs.getInt("med_daily_amount"),
                            rs.getString("med_days"),
                            rs.getString("med_condition"),
                            rs.getString("med_timing"),
                            rs.getInt("med_minutes"),
                            rs.getString("color")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding medicines by user ID " + userId + ": " + e.getMessage());
            throw e;
        }
        return medicines;
    }

    /**
     * 기존 약 정보를 데이터베이스에서 업데이트합니다.
     * @param medicine 업데이트할 약 정보 (Medicine 객체)
     * @return 업데이트 성공 시 true, 실패 시 false */
    public boolean updateMedicine(Medicine medicine) throws SQLException {
        String sql = "UPDATE Medicine SET user_id = ?, med_name = ?, med_daily_amount = ?, med_days = ?, med_condition = ?, med_timing = ?, med_minutes = ?, color = ? WHERE med_id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, medicine.getUserId());
            pstmt.setString(2, medicine.getMedName());
            pstmt.setInt(3, medicine.getMedDailyAmount());
            pstmt.setString(4, medicine.getMedDays());
            pstmt.setString(5, medicine.getMedCondition());
            pstmt.setString(6, medicine.getMedTiming());
            pstmt.setInt(7, medicine.getMedMinutes());
            pstmt.setString(8, medicine.getColor());
            pstmt.setInt(9, medicine.getMedId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating medicine with ID " + medicine.getMedId() + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * 특정 medId를 가진 약 정보를 데이터베이스에서 삭제합니다.
     * @param medId 삭제할 약의 ID
     * @return 삭제 성공 시 true, 실패 시 false */
    public boolean deleteMedicine(int medId) throws SQLException {
        String sql = "DELETE FROM Medicine WHERE med_id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, medId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting medicine with ID " + medId + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * 특정 사용자가 등록한 약의 총 개수를 가져옵니다.
     * @param userId 약 개수를 조회할 사용자의 ID
     * @return 해당 사용자가 등록한 약의 총 개수 */
    public int getMedicineCountByUserId(int userId) throws SQLException {
        String sql = "SELECT COUNT(med_id) FROM Medicine WHERE user_id = ?";
        int count = 0;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting medicine count for user ID " + userId + ": " + e.getMessage());
            throw e;
        }
        return count;
    }
}