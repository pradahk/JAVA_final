package com.smwujava.medicineapp.dao;

import com.smwujava.medicineapp.db.DBManager;
import com.smwujava.medicineapp.model.DosageRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map; // 통계를 위해 추가
import java.util.HashMap; // 통계를 위해 추가
import java.sql.Timestamp; // 06.01 수정


/**
 * 복용 기록(DosageRecords 테이블) 데이터 접근 객체 (DAO)
 * 데이터베이스의 DosageRecords 테이블과 관련된 데이터 CRUD 작업을 수행합니다.
 */
public class DosageRecordDao {
    // 날짜/시간 문자열 포맷터 상수 정의
    // DB의 DATETIME 컬럼 형식에 맞춰 포맷터를 정의합니다. (예: "yyyy-MM-dd HH:mm:ss")
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public DosageRecordDao() {
    }

    /**
     * LocalDateTime 객체를 DB에 저장할 수 있는 문자열 형식으로 변환합니다. null이면 null을 반환합니다.
     * @param dateTime 변환할 LocalDateTime 객체
     * @return DB 저장 형식의 문자열 또는 null
     */
    private String convertLocalDateTimeToString(LocalDateTime dateTime) {
        return (dateTime != null) ? dateTime.format(DATETIME_FORMATTER) : null;
    }

    /**
     * DB에서 읽어온 문자열을 LocalDateTime 객체로 변환합니다. null이면 null을 반환합니다.
     * @param dateTimeString DB에서 읽어온 날짜/시간 문자열
     * @return LocalDateTime 객체 또는 null
     */
    private LocalDateTime convertStringToLocalDateTime(String dateTimeString) {
        return (dateTimeString != null && !dateTimeString.isEmpty()) ? LocalDateTime.parse(dateTimeString, DATETIME_FORMATTER) : null;
    }

    // 06.01 정모아 수정
    public List<DosageRecord> findScheduledAlarmsWithin(LocalDateTime start, LocalDateTime end) throws SQLException {
        List<DosageRecord> alarms = new ArrayList<>();

        String sql = "SELECT * FROM DosageRecords " +
                "WHERE scheduled_time BETWEEN ? AND ? " +
                "AND actual_taken_time IS NULL AND is_skipped = 0";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(start));
            pstmt.setTimestamp(2, Timestamp.valueOf(end));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    DosageRecord record = new DosageRecord(
                            rs.getInt("record_id"),
                            rs.getInt("user_id"),
                            rs.getInt("med_id"),
                            convertStringToLocalDateTime(rs.getString("scheduled_time")),
                            convertStringToLocalDateTime(rs.getString("actual_taken_time")),
                            convertStringToLocalDateTime(rs.getString("rescheduled_time")),
                            rs.getBoolean("is_skipped")
                    );
                    alarms.add(record);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error while fetching scheduled alarms: " + e.getMessage());
            throw e;
        }

        return alarms;
    }
    /**
     * 새로운 복용 기록을 데이터베이스의 DosageRecords 테이블에 삽입합니다.
     * recordId는 DB에서 자동 생성됩니다.
     * @param record 삽입할 복용 기록 (DosageRecord 객체)
     * @return 데이터베이스에서 자동 생성된 복용 기록의 recordId. 오류 발생 시 -1을 반환합니다.
     * @throws SQLException 데이터베이스 접근 또는 SQL 실행 중 오류 발생 시
     */
    public int insertDosageRecord(DosageRecord record) throws SQLException {
        String sql = "INSERT INTO DosageRecords (user_id, med_id, scheduled_time, actual_taken_time, rescheduled_time, is_skipped) VALUES (?, ?, ?, ?, ?, ?)";
        int generatedId = -1;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, record.getUserId());
            pstmt.setInt(2, record.getMedId());
            pstmt.setString(3, convertLocalDateTimeToString(record.getScheduledTime()));
            pstmt.setString(4, convertLocalDateTimeToString(record.getActualTakenTime()));
            pstmt.setString(5, convertLocalDateTimeToString(record.getRescheduledTime())); // rescheduled_time 추가
            pstmt.setBoolean(6, record.isSkipped()); // is_skipped 추가

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

    /**
     * 특정 recordId를 가진 복용 기록을 데이터베이스에서 조회합니다.
     *
     * @param recordId 조회할 복용 기록의 ID
     * @return 해당 recordId에 해당하는 DosageRecord 객체. 없으면 null을 반환합니다.
     * @throws SQLException 데이터베이스 접근 또는 SQL 실행 중 오류 발생 시
     */
    public DosageRecord findDosageRecordById(int recordId) throws SQLException {
        String sql = "SELECT * FROM DosageRecords WHERE record_id = ?";
        DosageRecord record = null;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, recordId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    record = new DosageRecord(
                            rs.getInt("record_id"),
                            rs.getInt("user_id"),
                            rs.getInt("med_id"),
                            convertStringToLocalDateTime(rs.getString("scheduled_time")),
                            convertStringToLocalDateTime(rs.getString("actual_taken_time")),
                            convertStringToLocalDateTime(rs.getString("rescheduled_time")), // rescheduled_time 추가
                            rs.getBoolean("is_skipped") // is_skipped 추가
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding dosage record by ID " + recordId + ": " + e.getMessage());
            throw e;
        }
        return record;
    }

    /**
     * 특정 사용자의 특정 날짜에 대한 모든 복용 기록을 조회합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @param date   조회할 날짜 (yyyy-MM-dd 형식의 문자열)
     * @return 해당 사용자와 날짜에 해당하는 DosageRecord 리스트
     * @throws SQLException 데이터베이스 접근 또는 SQL 실행 중 오류 발생 시
     */
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
                            convertStringToLocalDateTime(rs.getString("rescheduled_time")), // rescheduled_time 추가
                            rs.getBoolean("is_skipped") // is_skipped 추가
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding records for user " + userId + " on date " + date + ": " + e.getMessage());
            throw e;
        }
        return records;
    }

    /**
     * 특정 사용자 ID, 약 ID, 예정 시간으로 복용 기록을 조회합니다.
     * 이는 주로 특정 알람 시간에 대한 기록을 찾을 때 사용될 수 있습니다.
     *
     * @param userId        사용자 ID
     * @param medId         약 ID
     * @param scheduledTime 예정 시간 (정확히 일치하는 시간을 찾음)
     * @return 해당하는 DosageRecord 객체, 없으면 null
     * @throws SQLException 데이터베이스 오류 발생 시
     */
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


    /**
     * 특정 사용자의 특정 날짜 범위에 대한 모든 복용 기록을 조회합니다.
     * `actual_taken_time`이 NULL이 아니며 `is_skipped`가 0인 (즉, 실제로 복용했고 건너뛰지 않은) 기록만 가져옵니다.
     *
     * @param userId    조회할 사용자의 ID
     * @param startDate 조회 시작 날짜 (yyyy-MM-dd 형식의 문자열)
     * @param endDate   조회 종료 날짜 (yyyy-MM-dd 형식의 문자열)
     * @return 해당 사용자 ID와 날짜 범위에 해당하는 DosageRecord 리스트
     * @throws SQLException 데이터베이스 접근 또는 SQL 실행 중 오류 발생 시
     */
    public List<DosageRecord> findRecordsByUserIdAndDateRange(int userId, String startDate, String endDate) throws SQLException {
        List<DosageRecord> records = new ArrayList<>();
        // actual_taken_time 이 null이 아니고, is_skipped 가 0인 (즉, 건너뛰지 않은) 기록만 가져오도록 쿼리 수정
        String sql = "SELECT * FROM DosageRecords WHERE user_id = ? AND scheduled_time BETWEEN ? AND ? AND actual_taken_time IS NOT NULL AND is_skipped = 0 ORDER BY scheduled_time ASC";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, startDate + " 00:00:00"); // 시작일의 00시 00분 00초부터
            pstmt.setString(3, endDate + " 23:59:59");   // 종료일의 23시 59분 59초까지

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


    /**
     * 기존 복용 기록을 데이터베이스에서 업데이트합니다.
     * recordId는 변경되지 않습니다.
     *
     * @param record 업데이트할 복용 기록 (DosageRecord 객체)
     * @return 업데이트 성공 시 true, 실패 시 false
     * @throws SQLException 데이터베이스 접근 또는 SQL 실행 중 오류 발생 시
     */
    public boolean updateDosageRecord(DosageRecord record) throws SQLException {
        String sql = "UPDATE DosageRecords SET user_id = ?, med_id = ?, scheduled_time = ?, actual_taken_time = ?, rescheduled_time = ?, is_skipped = ? WHERE record_id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, record.getUserId());
            pstmt.setInt(2, record.getMedId());
            pstmt.setString(3, convertLocalDateTimeToString(record.getScheduledTime()));
            pstmt.setString(4, convertLocalDateTimeToString(record.getActualTakenTime()));
            pstmt.setString(5, convertLocalDateTimeToString(record.getRescheduledTime())); // rescheduled_time 추가
            pstmt.setBoolean(6, record.isSkipped()); // is_skipped 추가
            pstmt.setInt(7, record.getRecordId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating dosage record with ID " + record.getRecordId() + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * 특정 복용 기록의 실제 복용 시간을 업데이트합니다.
     *
     * @param recordId        업데이트할 복용 기록의 ID
     * @param actualTakenTime 실제 복용 시간 (LocalDateTime 객체)
     * @param isSkipped       복용 건너뛰기 여부
     * @return 업데이트 성공 시 true, 실패 시 false
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    public boolean updateActualTakenTime(int recordId, LocalDateTime actualTakenTime, boolean isSkipped) throws SQLException {
        String sql = "UPDATE DosageRecords SET actual_taken_time = ?, is_skipped = ? WHERE record_id = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, convertLocalDateTimeToString(actualTakenTime));
            pstmt.setBoolean(2, isSkipped);
            pstmt.setInt(3, recordId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating actual taken time for record ID " + recordId + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * 예정된 복용 시간을 기준으로 재조정된 복용 시간을 업데이트합니다.
     *
     * @param userId           사용자 ID
     * @param medId            약 ID
     * @param originalScheduledTime 원본 예정 시간
     * @param newRescheduledTime    새로운 재조정 시간
     * @return 업데이트 성공 시 true, 실패 시 false
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    public boolean updateRescheduledTime(int userId, int medId, LocalDateTime originalScheduledTime, LocalDateTime newRescheduledTime) throws SQLException {
        // 기존 scheduled_time, user_id, med_id를 조건으로 사용하여 해당 레코드를 찾고 rescheduled_time만 업데이트합니다.
        // 이 때 actual_taken_time이 NULL인 경우에만 업데이트하도록 조건을 추가하여, 이미 복용한 기록은 재조정되지 않도록 합니다.
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

    /**
     * 특정 사용자의 모든 복용 기록에서 재조정된 알람 시간을 NULL로 초기화합니다.
     *
     * @param userId 재조정 시간을 초기화할 사용자의 ID
     * @return 초기화 성공 여부 (true: 성공, false: 실패)
     * @throws SQLException 데이터베이스 오류 발생 시
     */
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


    /**
     * 특정 recordId를 가진 복용 기록을 데이터베이스에서 삭제합니다.
     *
     * @param recordId 삭제할 복용 기록의 ID
     * @return 삭제 성공 시 true, 실패 시 false
     * @throws SQLException 데이터베이스 접근 또는 SQL 실행 중 오류 발생 시
     */
    public boolean deleteDosageRecord(int recordId) throws SQLException {
        String sql = "DELETE FROM DosageRecords WHERE record_id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, recordId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting dosage record with ID " + recordId + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * 특정 약(medId)에 대한 모든 복용 기록을 삭제합니다.
     * 약이 삭제될 때 해당 약에 대한 모든 복용 기록도 함께 삭제할 때 사용됩니다.
     *
     * @param medId 삭제할 약의 ID
     * @return 삭제된 기록의 수
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    public int deleteRecordsByMedId(int medId) throws SQLException {
        String sql = "DELETE FROM DosageRecords WHERE med_id = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, medId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting dosage records for med ID " + medId + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * 특정 사용자(userId)의 모든 복용 기록을 삭제합니다.
     * 사용자 계정이 삭제될 때 관련 복용 기록도 삭제할 때 사용됩니다.
     *
     * @param userId 삭제할 사용자의 ID
     * @return 삭제된 기록의 수
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    public int deleteRecordsByUserId(int userId) throws SQLException {
        String sql = "DELETE FROM DosageRecords WHERE user_id = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting dosage records for user ID " + userId + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * 지정된 기간 동안의 시간대별 복용 성공 횟수를 조회합니다.
     * 실제 복용 시간(actual_taken_time)이 존재하고, 건너뛰지 않은(`is_skipped = 0`) 기록만 카운트합니다.
     *
     * @param days 최근 N일
     * @return 시간대(0-23)를 키로, 해당 시간대의 복용 성공 횟수를 값으로 하는 맵
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    public Map<Integer, Integer> getSuccessfulDosageCountsByHour(int days) throws SQLException {
        // SQLite의 strftime('%H', datetime) 함수는 24시간 형식의 시간(00-23)을 반환합니다.
        // 실제 복용 시간(actual_taken_time)을 기준으로 합니다.
        String sql = "SELECT CAST(strftime('%H', actual_taken_time) AS INTEGER) AS hour_of_day, COUNT(*) AS count " +
                "FROM DosageRecords " +
                "WHERE actual_taken_time IS NOT NULL AND is_skipped = 0 " + // 성공 및 건너뛰지 않은 기록만
                "AND scheduled_time >= datetime('now', ? || ' days') " + // scheduled_time을 기준으로 기간 필터링
                "GROUP BY hour_of_day ORDER BY hour_of_day";

        Map<Integer, Integer> hourlyCounts = new HashMap<>();
        // 0부터 23까지 모든 시간대를 0으로 초기화
        for (int i = 0; i < 24; i++) {
            hourlyCounts.put(i, 0);
        }

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "-" + (days - 1)); // 오늘을 포함하여 N일 전까지
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int hour = rs.getInt("hour_of_day");
                    int count = rs.getInt("count");
                    hourlyCounts.put(hour, count);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting successful dosage counts by hour: " + e.getMessage());
            throw e;
        }
        return hourlyCounts;
    }

    /**
     * 특정 사용자의 특정 날짜 범위에 대한 아직 복용하지 않은 (actual_taken_time이 NULL인) 복용 기록을 조회합니다.
     * 이 메서드는 미래의 예정된 알람 시간을 가져올 때 사용됩니다.
     *
     * @param userId    조회할 사용자의 ID
     * @param startDate 조회 시작 날짜 (yyyy-MM-dd 형식의 문자열)
     * @param endDate   조회 종료 날짜 (yyyy-MM-dd 형식의 문자열)
     * @return 해당 사용자 ID와 날짜 범위에 해당하는 DosageRecord 리스트
     * @throws SQLException 데이터베이스 접근 또는 SQL 실행 중 오류 발생 시
     */
    public List<DosageRecord> findRecordsByUserIdAndDateRangeForFuture(int userId, String startDate, String endDate) throws SQLException {
        List<DosageRecord> records = new ArrayList<>();
        // actual_taken_time 이 null인 기록만 가져오도록 쿼리 수정
        String sql = "SELECT * FROM DosageRecords WHERE user_id = ? AND scheduled_time BETWEEN ? AND ? AND actual_taken_time IS NULL ORDER BY scheduled_time ASC";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, startDate + " 00:00:00"); // 시작일의 00시 00분 00초부터
            pstmt.setString(3, endDate + " 23:59:59");   // 종료일의 23시 59분 59초까지

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

    //DosageRecordDao.getTodaySchedules() 구현 (06.01 정모아 수정)
    public List<DosageRecord> getTodaySchedules() {
        List<DosageRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM DosageRecords " +
                "WHERE DATE(scheduled_time) = DATE('now', 'localtime') " +
                "AND actual_taken_time IS NULL";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                DosageRecord record = new DosageRecord(
                        rs.getInt("user_id"),
                        rs.getInt("medicine_id"),
                        rs.getTimestamp("scheduled_time").toLocalDateTime()
                );
                record.setId(rs.getInt("id"));
                // 필요한 경우 추가 필드도 설정 가능
                records.add(record);
            }

        } catch (Exception e) {
            System.err.println("오늘의 복용 스케줄 조회 중 오류 발생: " + e.getMessage());
        }

        return records;
    }



    /**
     * 현재 시각 기준으로, 아직 복용하지 않았고 건너뛰지 않은 복용 기록 중
     * 가장 가까운 예정 알람 시간을 반환합니다.
     * rescheduled_time이 있으면 그것을 우선 사용하고, 없으면 scheduled_time을 사용합니다.
     *
     * @param userId 사용자 ID
     * @return 가장 가까운 알람 시간 (LocalDateTime), 없으면 null
     * @throws SQLException DB 오류 발생 시
     */

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
}