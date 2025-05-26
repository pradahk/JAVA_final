package com.smwujava.medicineapp.dao;

import com.smwujava.medicineapp.db.DBManager;
import com.smwujava.medicineapp.model.DosageRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map; // 통계를 위해 추가
import java.util.HashMap; // 통계를 위해 추가

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
     * @return DB 형식의 문자열 또는 null
     */
    private static String toDbString(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }

    /**
     * DB에서 읽어온 문자열을 LocalDateTime 객체로 변환합니다. null이거나 비어있으면 null을 반환합니다.
     * @param dbString DB에서 읽어온 문자열
     * @return LocalDateTime 객체 또는 null
     */
    private static LocalDateTime fromDbString(String dbString) {
        return (dbString != null && !dbString.trim().isEmpty()) ? LocalDateTime.parse(dbString, DATETIME_FORMATTER) : null;
    }

    /**
     * 새로운 복용 기록을 데이터베이스의 DosageRecords 테이블에 삽입합니다.
     * @param record 삽입할 복용 기록 정보 (DosageRecord 객체)
     * @return 데이터베이스에서 자동 생성된 복용 기록의 record_id. 오류 발생 시 -1을 반환합니다.
     */
    public static int insertDosageRecord(DosageRecord record) throws SQLException {
        //변경점 1: INSERT 쿼리에 is_skipped, rescheduled_time 컬럼 추가
        String sql = "INSERT INTO DosageRecords (user_id, med_id, scheduled_time, actual_taken_time, rescheduled_time, is_skipped) VALUES (?, ?, ?, ?, ?, ?)";
        int generatedId = -1;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, record.getUserId());
            pstmt.setInt(2, record.getMedId());
            pstmt.setString(3, toDbString(record.getScheduledTime()));
            pstmt.setString(4, toDbString(record.getActualTakenTime()));
            pstmt.setString(5, toDbString(record.getRescheduledTime())); //rescheduled_time 설정
            pstmt.setInt(6, record.isSkipped() ? 1 : 0); //is_skipped 설정

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
     * 특정 사용자, 약 ID, 예정 시간으로 복용 기록을 조회합니다.
     * @param userId 사용자 ID
     * @param medId 약 ID
     * @param scheduledTime 예정 복용 시간
     * @return 해당 복용 기록이 존재하면 DosageRecord 객체, 없으면 null
     */
    public static DosageRecord getDosageRecord(int userId, int medId, LocalDateTime scheduledTime) throws SQLException {
        //변경점 2: SELECT 쿼리에 is_skipped, rescheduled_time 컬럼 추가
        String sql = "SELECT record_id, user_id, med_id, scheduled_time, actual_taken_time, rescheduled_time, is_skipped FROM DosageRecords WHERE user_id = ? AND med_id = ? AND scheduled_time = ?";
        DosageRecord record = null;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, medId);
            pstmt.setString(3, toDbString(scheduledTime));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int recordId = rs.getInt("record_id");
                    LocalDateTime actualTakenTime = fromDbString(rs.getString("actual_taken_time"));
                    LocalDateTime rescheduledTime = fromDbString(rs.getString("rescheduled_time")); //rescheduled_time 읽어오기
                    boolean isSkipped = rs.getBoolean("is_skipped"); //is_skipped 읽어오기

                    record = new DosageRecord(recordId, userId, medId, scheduledTime, actualTakenTime, rescheduledTime, isSkipped); //생성자 업데이트
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting dosage record for user ID " + userId + ", med ID " + medId + ", scheduled time " + scheduledTime + ": " + e.getMessage());
            throw e;
        }
        return record;
    }

    /**
     * 특정 사용자와 특정 날짜 범위 내의 모든 복용 기록을 조회합니다.
     * @param userId 사용자 ID
     * @param startDate 조회 시작 날짜 (YYYY-MM-DD)
     * @param endDate 조회 종료 날짜 (YYYY-MM-DD)
     * @return 복용 기록 리스트
     */
    public static List<DosageRecord> getDosageRecordsForUser(int userId, LocalDate startDate, LocalDate endDate) throws SQLException {
        List<DosageRecord> records = new ArrayList<>();
        //변경점 3: SELECT 쿼리에 is_skipped, rescheduled_time 컬럼 추가
        // 날짜 범위는 scheduled_time의 날짜 부분만 비교하도록 합니다.
        String sql = "SELECT record_id, user_id, med_id, scheduled_time, actual_taken_time, rescheduled_time, is_skipped FROM DosageRecords " +
                "WHERE user_id = ? AND SUBSTR(scheduled_time, 1, 10) BETWEEN ? AND ? ORDER BY scheduled_time ASC";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, startDate.format(DATE_FORMATTER));
            pstmt.setString(3, endDate.format(DATE_FORMATTER));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int recordId = rs.getInt("record_id");
                    int medId = rs.getInt("med_id");
                    LocalDateTime scheduledTime = fromDbString(rs.getString("scheduled_time"));
                    LocalDateTime actualTakenTime = fromDbString(rs.getString("actual_taken_time"));
                    LocalDateTime rescheduledTime = fromDbString(rs.getString("rescheduled_time")); //rescheduled_time 읽어오기
                    boolean isSkipped = rs.getBoolean("is_skipped"); //is_skipped 읽어오기

                    records.add(new DosageRecord(recordId, userId, medId, scheduledTime, actualTakenTime, rescheduledTime, isSkipped)); //생성자 업데이트
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting dosage records for user ID " + userId + " from " + startDate + " to " + endDate + ": " + e.getMessage());
            throw e;
        }
        return records;
    }


    /**
     * 복용 기록의 실제 복용 시간과 건너뛰기 여부를 업데이트합니다.
     * @param recordId 업데이트할 기록의 ID
     * @param actualTakenTime 실제 복용 시간 (NULL일 수 있음)
     * @param isSkipped 건너뛰기 여부
     * @return 업데이트 성공 시 true, 실패 시 false
     */
    public static boolean updateDosageRecordStatus(int recordId, LocalDateTime actualTakenTime, boolean isSkipped) throws SQLException {
        //변경점 4: UPDATE 쿼리에 is_skipped 컬럼 추가
        String sql = "UPDATE DosageRecords SET actual_taken_time = ?, is_skipped = ? WHERE record_id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, toDbString(actualTakenTime));
            pstmt.setInt(2, isSkipped ? 1 : 0); //is_skipped 값 설정
            pstmt.setInt(3, recordId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating dosage record status for record ID " + recordId + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * 특정 복용 기록의 재조정된 알람 시간을 업데이트합니다.
     * @param userId 사용자 ID
     * @param medId 약 ID
     * @param originalScheduledTime 원래 예정된 복용 시간
     * @param newRescheduledTime 새로 재조정된 시간
     * @return 업데이트 성공 시 true, 실패 시 false
     */
    public static boolean updateRescheduledTime(int userId, int medId, LocalDateTime originalScheduledTime, LocalDateTime newRescheduledTime) throws SQLException {
        //변경점 5: 이 메서드는 기존 DosageRecord 모델에 `rescheduled_time`이 추가되었을 때 같이 추가된 것으로 보입니다.
        // 현재 이 메서드 자체는 변경할 필요가 없습니다.
        String sql = "UPDATE DosageRecords SET rescheduled_time = ? WHERE user_id = ? AND med_id = ? AND scheduled_time = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, toDbString(newRescheduledTime));
            pstmt.setInt(2, userId);
            pstmt.setInt(3, medId);
            pstmt.setString(4, toDbString(originalScheduledTime));

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating rescheduled time for user ID " + userId + ", med ID " + medId + ", scheduled time " + originalScheduledTime + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 특정 사용자(userId)의 모든 복용 기록에서 재조정된 알람 시간을 NULL로 초기화합니다.
     * (예: 사용자가 생활 패턴을 수정했을 때 이전에 계산된 재조정 시간을 모두 지울 때 사용)
     * @param userId 재조정 시간을 초기화할 사용자의 ID
     * @return 초기화된 행이 1개 이상 있으면 true, 아니면 false
     */
    public static boolean resetAllRescheduledTimes(int userId) throws SQLException {
        String sql = "UPDATE DosageRecords SET rescheduled_time = NULL WHERE user_id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error resetting all rescheduled times for user ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    //변경점 6: 특정 사용자의 특정 기간 동안 생성된 총 알람 개수 가져오기
    /**
     * 특정 사용자의 특정 기간 동안 (오늘로부터 `days`일 전까지) 생성된 모든 알람(예정 기록)의 개수를 가져옵니다.
     * @param userId 사용자 ID
     * @param days 오늘로부터 과거로 계산할 일수 (예: 7이면 오늘 포함 지난 7일)
     * @return 해당 기간 동안 생성된 총 알람 개수
     */
    public static int getTotalScheduledAlarmsCount(int userId, int days) throws SQLException {
        String sql = "SELECT COUNT(*) FROM DosageRecords WHERE user_id = ? AND scheduled_time >= datetime('now', ? || ' days')";
        int count = 0;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, "-" + (days - 1)); //오늘을 포함하여 N일 전까지이므로 (N-1) days 사용
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting total scheduled alarms count for user ID " + userId + " for last " + days + " days: " + e.getMessage());
            throw e;
        }
        return count;
    }

    //변경점 7: 특정 사용자의 특정 기간 동안 복용 성공한 알람 개수 가져오기
    /**
     * 특정 사용자의 특정 기간 동안 (오늘로부터 `days`일 전까지) 성공적으로 복용된 알람의 개수를 가져옵니다.
     * 성공 기준: actual_taken_time이 NULL이 아니고 is_skipped가 0(false)인 경우.
     * @param userId 사용자 ID
     * @param days 오늘로부터 과거로 계산할 일수 (예: 7이면 오늘 포함 지난 7일)
     * @return 해당 기간 동안 복용 성공한 알람 개수
     */
    public static int getSuccessfulDosageAlarmsCount(int userId, int days) throws SQLException {
        String sql = "SELECT COUNT(*) FROM DosageRecords WHERE user_id = ? AND actual_taken_time IS NOT NULL AND is_skipped = 0 AND scheduled_time >= datetime('now', ? || ' days')";
        int count = 0;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, "-" + (days - 1));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting successful dosage alarms count for user ID " + userId + " for last " + days + " days: " + e.getMessage());
            throw e;
        }
        return count;
    }

    /**
     * 특정 약 ID와 사용자 ID에 대한 지난 `days`일 동안의 실제 복용 기록을 가져옵니다.
     * 실제 복용 기록만 필요하며, is_skipped가 false인 기록만 포함합니다.
     * @param userId 사용자 ID
     * @param medId 약 ID
     * @param days 과거로부터 가져올 일수 (예: 7이면 오늘 포함 지난 7일)
     * @return 복용 기록 리스트
     */
    public static List<DosageRecord> getRecentTakenRecords(int userId, int medId, int days) throws SQLException {
        //변경점 8: SELECT 쿼리에 is_skipped, rescheduled_time 컬럼 추가
        // is_skipped = false 조건 추가
        String sql = "SELECT record_id, user_id, med_id, scheduled_time, actual_taken_time, rescheduled_time, is_skipped " +
                "FROM DosageRecords " +
                "WHERE user_id = ? AND med_id = ? AND actual_taken_time IS NOT NULL AND is_skipped = 0 " + // is_skipped 조건 추가
                "AND scheduled_time >= datetime('now', ? || ' days') " +
                "ORDER BY scheduled_time DESC";

        List<DosageRecord> records = new ArrayList<>();

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, medId);
            pstmt.setString(3, "-" + (days - 1)); //오늘을 포함하여 N일 전까지이므로 (N-1) days 사용

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int recordId = rs.getInt("record_id");
                    LocalDateTime scheduledTime = fromDbString(rs.getString("scheduled_time"));
                    LocalDateTime actualTakenTime = fromDbString(rs.getString("actual_taken_time"));
                    LocalDateTime rescheduledTime = fromDbString(rs.getString("rescheduled_time"));
                    boolean isSkipped = rs.getBoolean("is_skipped");

                    DosageRecord record = new DosageRecord(recordId, userId, medId,
                            scheduledTime, actualTakenTime, rescheduledTime, isSkipped);
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting recent taken records for user ID " + userId + ", med ID " + medId + ": " + e.getMessage());
            throw e;
        }
        return records;
    }

    /**
     * 특정 약 ID와 사용자 ID에 대한 지난 `days`일 동안의 모든 예정/재조정 알람 기록을 가져옵니다.
     * actual_taken_time이 NULL인 기록만 포함합니다.
     * @param userId 사용자 ID
     * @param medId 약 ID
     * @param days 과거로부터 가져올 일수 (예: 7이면 오늘 포함 지난 7일)
     * @return 복용 기록 리스트 (예정/재조정 알람)
     */
    public static List<DosageRecord> getRecentScheduledAndRescheduledRecords(int userId, int medId, int days) throws SQLException {
        //변경점 9: SELECT 쿼리에 is_skipped, rescheduled_time 컬럼 추가
        String sql = "SELECT record_id, user_id, med_id, scheduled_time, actual_taken_time, rescheduled_time, is_skipped " +
                "FROM DosageRecords " +
                "WHERE user_id = ? AND med_id = ? AND actual_taken_time IS NULL " + // 실제 복용 시간이 NULL인 경우
                "AND (scheduled_time >= datetime('now', ? || ' days') OR rescheduled_time >= datetime('now', ? || ' days')) " + // 예정 또는 재조정 시간이 범위 내인 경우
                "ORDER BY scheduled_time DESC";

        List<DosageRecord> records = new ArrayList<>();

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, medId);
            pstmt.setString(3, "-" + (days - 1)); // 예정 시간 범위
            pstmt.setString(4, "-" + (days - 1)); // 재조정 시간 범위

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int recordId = rs.getInt("record_id");
                    LocalDateTime scheduledTime = fromDbString(rs.getString("scheduled_time"));
                    LocalDateTime actualTakenTime = fromDbString(rs.getString("actual_taken_time"));
                    LocalDateTime rescheduledTime = fromDbString(rs.getString("rescheduled_time"));
                    boolean isSkipped = rs.getBoolean("is_skipped");

                    DosageRecord record = new DosageRecord(recordId, userId, medId,
                            scheduledTime, actualTakenTime, rescheduledTime, isSkipped);
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting recent scheduled/rescheduled records for user ID " + userId + ", med ID " + medId + ": " + e.getMessage());
            throw e;
        }
        return records;
    }

    // <<-- 변경점 10: 시간대별 생성된 알람 수 가져오기 -->>
    /**
     * 특정 기간 동안 (오늘로부터 `days`일 전까지) 모든 사용자에 대해 시간대별로 생성된 총 알람(예정 기록)의 개수를 가져옵니다.
     * 반환되는 맵의 키는 시간대(0-23), 값은 해당 시간대에 생성된 알람 개수입니다.
     * @param days 오늘로부터 과거로 계산할 일수 (예: 7이면 오늘 포함 지난 7일)
     * @return 시간대별 총 알람 개수를 담은 Map<Integer, Integer>
     * @throws SQLException 데이터베이스 접근 오류 발생 시
     */
    public static Map<Integer, Integer> getTotalScheduledAlarmsByHour(int days) throws SQLException {
        // SQLite의 strftime('%H', datetime) 함수는 24시간 형식의 시간(00-23)을 반환합니다.
        // scheduled_time 또는 rescheduled_time 중 더 늦은 시간을 기준으로 합니다.
        // 복용 예정이거나 재조정된 알람만 카운트합니다.
        String sql = "SELECT CAST(strftime('%H', COALESCE(rescheduled_time, scheduled_time)) AS INTEGER) AS hour_of_day, COUNT(*) AS count " +
                "FROM DosageRecords " +
                "WHERE COALESCE(rescheduled_time, scheduled_time) >= datetime('now', ? || ' days') " +
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
            System.err.println("Error getting total scheduled alarms by hour for last " + days + " days: " + e.getMessage());
            throw e;
        }
        return hourlyCounts;
    }

    // <<-- 변경점 11: 시간대별 약 복용 성공 횟수 가져오기 -->>
    /**
     * 특정 기간 동안 (오늘로부터 `days`일 전까지) 모든 사용자에 대해 시간대별로 성공적으로 복용된 알람의 개수를 가져옵니다.
     * 성공 기준: actual_taken_time이 NULL이 아니고 is_skipped가 0(false)인 경우.
     * 반환되는 맵의 키는 시간대(0-23), 값은 해당 시간대에 성공한 복용 개수입니다.
     * @param days 오늘로부터 과거로 계산할 일수 (예: 7이면 오늘 포함 지난 7일)
     * @return 시간대별 성공 복용 개수를 담은 Map<Integer, Integer>
     * @throws SQLException 데이터베이스 접근 오류 발생 시
     */
    public static Map<Integer, Integer> getSuccessfulDosagesByHour(int days) throws SQLException {
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
            System.err.println("Error getting successful dosages by hour for last " + days + " days: " + e.getMessage());
            throw e;
        }
        return hourlyCounts;
    }
}