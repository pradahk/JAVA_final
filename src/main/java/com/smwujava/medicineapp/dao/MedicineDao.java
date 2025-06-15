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
    // 새로운 약 정보를 데이터베이스의 Medicine 테이블에 삽입
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

    // 특정 medId를 가진 약 정보를 데이터베이스에서 조회
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

    // 특정 사용자 ID에 해당하는 모든 약 정보를 데이터베이스에서 조회
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

    public String findMedicineNameById(int medId) {
        String medName = "";
        String sql = "SELECT med_name FROM Medicine WHERE med_id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, medId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                medName = rs.getString("med_name");
            }
        } catch (SQLException e) {
            System.err.println("💥 약 이름 조회 실패: " + e.getMessage());
        }

        return medName;
    }


    // 특정 사용자가 등록한 약의 총 개수를 가져옴
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