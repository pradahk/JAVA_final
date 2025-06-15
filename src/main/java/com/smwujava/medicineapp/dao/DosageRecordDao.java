package com.smwujava.medicineapp.dao;

import com.smwujava.medicineapp.db.DBManager;
import com.smwujava.medicineapp.model.DosageRecord;
import com.smwujava.medicineapp.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

// 복용 기록(DosageRecords 테이블) 데이터 접근 객체 (DAO)
public class DosageRecordDao {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DosageRecordDao() {
    }

    // LocalDateTime 객체를 DB에 저장할 수 있는 문자열 형식으로 변환
    private String convertLocalDateTimeToString(LocalDateTime dateTime) {
        return (dateTime != null) ? dateTime.format(DATETIME_FORMATTER) : null;
    }

    // DB에서 읽어온 문자열을 LocalDateTime 객체로 변환
    private LocalDateTime convertStringToLocalDateTime(String dateTimeString) {
        return (dateTimeString != null && !dateTimeString.isEmpty()) ? LocalDateTime.parse(dateTimeString, DATETIME_FORMATTER) : null;
    }

    // 새로운 복용 기록을 데이터베이스의 DosageRecords 테이블에 삽입
    public int insertDosageRecord(DosageRecord record) throws SQLException {
        String sql = "INSERT INTO DosageRecords (user_id, med_id, scheduled_time, actual_taken_time, rescheduled_time, is_skipped) VALUES (?, ?, ?, ?, ?, ?)";
        int generatedId = -1;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, record.getUserId());
            pstmt.setInt(2, record.getMedId());
            pstmt.setString(3, convertLocalDateTimeToString(record.getScheduledTime()));
            pstmt.setString(4, convertLocalDateTimeToString(record.getActualTakenTime()));
            pstmt.setString(5, convertLocalDateTimeToString(record.getRescheduledTime()));
            pstmt.setBoolean(6, record.isSkipped());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting dosage record: " + e.getMessage());
            throw e;
        }
        return generatedId;
    }

    // 특정 사용자의 특정 날짜에 대한 모든 복용 기록을 조회
    public List<DosageRecord> findRecordsByUserIdAndDate(int userId, String date) throws SQLException {
        List<DosageRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM DosageRecords WHERE user_id = ? AND SUBSTR(scheduled_time, 1, 10) = ? ORDER BY scheduled_time ASC";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, date);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    records.add(new DosageRecord(
                            rs.getInt("record_id"),
                            rs.getInt("user_id"),
                            rs.getInt("med_id"),
                            convertStringToLocalDateTime(rs.getString("scheduled_time")),
                            convertStringToLocalDateTime(rs.getString("actual_taken_time")),
                            convertStringToLocalDateTime(rs.getString("rescheduled_time")),
                            rs.getBoolean("is_skipped")

                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding records for user " + userId + " on date " + date + ": " + e.getMessage());
            throw e;
        }
        return records;
    }

    // 특정 사용자 ID, 약 ID, 예정 시간으로 복용 기록을 조회
    public DosageRecord findRecordByUserIdMedIdAndScheduledTime(int userId, int medId, LocalDateTime scheduledTime) throws SQLException {
        String sql = "SELECT * FROM DosageRecords WHERE user_id = ? AND med_id = ? AND scheduled_time = ?";
        DosageRecord record = null;
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, medId);
            pstmt.setString(3, scheduledTime.format(DATETIME_FORMATTER));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    record = new DosageRecord(
                            rs.getInt("record_id"),
                            rs.getInt("user_id"),
                            rs.getInt("med_id"),
                            convertStringToLocalDateTime(rs.getString("scheduled_time")),
                            convertStringToLocalDateTime(rs.getString("actual_taken_time")),
                            convertStringToLocalDateTime(rs.getString("rescheduled_time")),
                            rs.getBoolean("is_skipped")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding record by user, med, and scheduled time: " + e.getMessage());
            throw e;
        }
        return record;
    }


    // 특정 사용자의 특정 날짜 범위에 대한 모든 복용 기록을 조회
    public List<DosageRecord> findRecordsByUserIdAndDateRange(int userId, String startDate, String endDate) throws SQLException {
        List<DosageRecord> records = new ArrayList<>();
        // actual_taken_time 이 null이 아니고, is_skipped 가 0인 (즉, 건너뛰지 않은) 기록만 가져오도록 함
        String sql = "SELECT * FROM DosageRecords WHERE user_id = ? AND scheduled_time BETWEEN ? AND ? AND actual_taken_time IS NOT NULL AND is_skipped = 0 ORDER BY scheduled_time ASC";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, startDate + " 00:00:00");
            pstmt.setString(3, endDate + " 23:59:59");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    records.add(new DosageRecord(
                            rs.getInt("record_id"),
                            rs.getInt("user_id"),
                            rs.getInt("med_id"),
                            convertStringToLocalDateTime(rs.getString("scheduled_time")),
                            convertStringToLocalDateTime(rs.getString("actual_taken_time")),
                            convertStringToLocalDateTime(rs.getString("rescheduled_time")),
                            rs.getBoolean("is_skipped")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding records for user " + userId + " in date range " + startDate + " to " + endDate + ": " + e.getMessage());
            throw e;
        }
        return records;
    }


    // 기존 복용 기록을 데이터베이스에서 업데이트
    public boolean updateDosageRecord(DosageRecord record) throws SQLException {
        String sql = "UPDATE DosageRecords SET user_id = ?, med_id = ?, scheduled_time = ?, actual_taken_time = ?, rescheduled_time = ?, is_skipped = ? WHERE record_id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, record.getUserId());
            pstmt.setInt(2, record.getMedId());
            pstmt.setString(3, convertLocalDateTimeToString(record.getScheduledTime()));
            pstmt.setString(4, convertLocalDateTimeToString(record.getActualTakenTime()));
            pstmt.setString(5, convertLocalDateTimeToString(record.getRescheduledTime()));
            pstmt.setBoolean(6, record.isSkipped());
            pstmt.setInt(7, record.getRecordId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating dosage record with ID " + record.getRecordId() + ": " + e.getMessage());
            throw e;
        }
    }


    // 예정된 복용 시간을 기준으로 재조정된 복용 시간을 업데이트
    public boolean updateRescheduledTime(int userId, int medId, LocalDateTime originalScheduledTime, LocalDateTime newRescheduledTime) throws SQLException {
        // 기존 scheduled_time, user_id, med_id를 조건으로 사용하여 해당 레코드를 찾고 rescheduled_time만 업데이트하도록 함
        String sql = "UPDATE DosageRecords SET rescheduled_time = ? WHERE user_id = ? AND med_id = ? AND scheduled_time = ? AND actual_taken_time IS NULL";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, convertLocalDateTimeToString(newRescheduledTime));
            pstmt.setInt(2, userId);
            pstmt.setInt(3, medId);
            pstmt.setString(4, convertLocalDateTimeToString(originalScheduledTime));
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating rescheduled time for user " + userId + ", med " + medId + ": " + e.getMessage());
            throw e;
        }
    }

    // 특정 사용자의 모든 복용 기록에서 재조정된 알람 시간을 NULL로 초기화
    public boolean resetAllRescheduledTimes(int userId) throws SQLException {
        String sql = "UPDATE DosageRecords SET rescheduled_time = NULL WHERE user_id = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error resetting all rescheduled times for user ID " + userId + ": " + e.getMessage());
            throw e;
        }
    }

    // 특정 사용자의 특정 날짜 범위에 대한 아직 복용하지 않은 (actual_taken_time이 NULL인) 복용 기록을 조회
    public List<DosageRecord> findRecordsByUserIdAndDateRangeForFuture(int userId, String startDate, String endDate) throws SQLException {
        List<DosageRecord> records = new ArrayList<>();
        // actual_taken_time 이 null인 기록만 가져오도록 함
        String sql = "SELECT * FROM DosageRecords WHERE user_id = ? AND scheduled_time BETWEEN ? AND ? AND actual_taken_time IS NULL ORDER BY scheduled_time ASC";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, startDate + " 00:00:00");
            pstmt.setString(3, endDate + " 23:59:59");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    records.add(new DosageRecord(
                            rs.getInt("record_id"),
                            rs.getInt("user_id"),
                            rs.getInt("med_id"),
                            convertStringToLocalDateTime(rs.getString("scheduled_time")),
                            convertStringToLocalDateTime(rs.getString("actual_taken_time")),
                            convertStringToLocalDateTime(rs.getString("rescheduled_time")),
                            rs.getBoolean("is_skipped")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding future records for user " + userId + " in date range " + startDate + " to " + endDate + ": " + e.getMessage());
            throw e;
        }
        return records;
    }


    // 현재 시각 기준으로, 아직 복용하지 않았고 건너뛰지 않은 복용 기록 중 가장 가까운 예정 알람 시간을 반환
    public LocalDateTime findClosestUpcomingAlarmTime(int userId) throws SQLException {
        String sql = "SELECT * FROM DosageRecords " +
                "WHERE user_id = ? AND is_skipped = 0 AND actual_taken_time IS NULL " +
                "AND (COALESCE(rescheduled_time, scheduled_time) > ?) " +
                "ORDER BY COALESCE(rescheduled_time, scheduled_time) ASC LIMIT 1";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, LocalDateTime.now().format(DATETIME_FORMATTER));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String rescheduledStr = rs.getString("rescheduled_time");
                    String scheduledStr = rs.getString("scheduled_time");
                    return (rescheduledStr != null && !rescheduledStr.isEmpty())
                            ? convertStringToLocalDateTime(rescheduledStr)
                            : convertStringToLocalDateTime(scheduledStr);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding next alarm: " + e.getMessage());
            throw e;
        }
        return null;
    }

    // 데이터베이스의 모든 복용 기록을 조회하여 리스트로 반환
    public List<DosageRecord> findAll() throws SQLException {
        List<DosageRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM DosageRecords ORDER BY scheduled_time DESC"; // 최신 기록부터 정렬

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                records.add(new DosageRecord(
                        rs.getInt("record_id"),
                        rs.getInt("user_id"),
                        rs.getInt("med_id"),
                        convertStringToLocalDateTime(rs.getString("scheduled_time")),
                        convertStringToLocalDateTime(rs.getString("actual_taken_time")),
                        convertStringToLocalDateTime(rs.getString("rescheduled_time")),
                        rs.getBoolean("is_skipped")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error while fetching all dosage records: " + e.getMessage());
            throw e;
        }
        return records;
    }
}