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
        return dateTime == null ? null : dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * DB에서 읽어온 문자열을 LocalDateTime 객체로 변환합니다. null이거나 빈 문자열이면 null을 반환합니다.
     * @param dateTimeString DB에서 읽어온 날짜/시간 문자열
     * @return LocalDateTime 객체 또는 null
     */
    private static LocalDateTime fromDbString(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeString, DATETIME_FORMATTER);
    }

    /**
     * 새로운 복용 기록을 데이터베이스의 DosageRecords 테이블에 삽입합니다 (예: 복용 스케줄 생성).
     * @param record 삽입할 복용 기록 (DosageRecord 객체)
     * @return 데이터베이스에서 자동 생성된 기록의 record_id. 오류 발생 시 -1을 반환합니다.
     */
    public static int insertDosageRecord(DosageRecord record) throws SQLException {
        // DB 스키마에 record_date 컬럼이 여전히 존재하므로, 해당 컬럼에도 값을 삽입해야 합니다.
        // record_date는 scheduled_time의 날짜 부분에서 추출하여 사용합니다.
        String sql = "INSERT INTO DosageRecords (user_id, med_id, record_date, scheduled_time, actual_taken_time, rescheduled_time, is_skipped) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int generatedId = -1;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, record.getUserId());
            pstmt.setInt(2, record.getMedId());

            // record_date 컬럼에 scheduledTime의 날짜 부분만 저장
            if (record.getScheduledTime() != null) {
                pstmt.setString(3, record.getScheduledTime().toLocalDate().format(DATE_FORMATTER));
                pstmt.setString(4, toDbString(record.getScheduledTime())); // 전체 시간 저장
            } else {
                // record.getScheduledTime()이 null인 경우, record_date와 scheduled_time 모두 NULL로 설정
                pstmt.setNull(3, java.sql.Types.VARCHAR);
                pstmt.setNull(4, java.sql.Types.VARCHAR);
            }

            pstmt.setString(5, toDbString(record.getActualTakenTime())); // actualTakenTime이 null이면 DB에 NULL로 저장
            pstmt.setString(6, toDbString(record.getRescheduledTime())); // rescheduledTime이 null이면 DB에 NULL로 저장
            pstmt.setBoolean(7, record.isSkipped());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting dosage record for user ID " + record.getUserId() + ", med ID " + record.getMedId() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return generatedId;
    }

    /**
     * 특정 사용자 ID에 해당하는 복용 기록을 특정 날짜 범위로 조회합니다.
     * (예: 캘린더에 표시할 한 달치 기록 등)
     * @param userId 조회할 사용자의 ID
     * @param startDate 조회 시작 날짜 ("YYYY-MM-DD" 형태의 문자열)
     * @param endDate 조회 종료 날짜 ("YYYY-MM-DD" 형태의 문자열)
     * @return 해당 사용자의 지정된 기간 내 복용 기록 목록 (List<DosageRecord>). 기록이 없으면 빈 목록을 반환합니다.
     */
    public static List<DosageRecord> findRecordsByUserIdAndDateRange(int userId, String startDate, String endDate) throws SQLException {
        // `record_date` 컬럼을 사용하여 날짜 범위를 조회합니다.
        String sql = "SELECT record_id, user_id, med_id, scheduled_time, actual_taken_time, rescheduled_time, is skipped FROM DosageRecords WHERE user_id = ? AND record_date BETWEEN ? AND ? ORDER BY scheduled_time";
        List<DosageRecord> recordList = new ArrayList<>();

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, startDate);
            pstmt.setString(3, endDate);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int recordId = rs.getInt("record_id");
                    int foundUserId = rs.getInt("user_id");
                    int medId = rs.getInt("med_id");

                    // DB 문자열을 LocalDateTime으로 변환하여 읽기
                    LocalDateTime scheduledTime = fromDbString(rs.getString("scheduled_time"));
                    LocalDateTime actualTakenTime = fromDbString(rs.getString("actual_taken_time"));
                    LocalDateTime rescheduledTime = fromDbString(rs.getString("rescheduled_time"));
                    boolean isSkipped = rs.getBoolean("is_skipped");

                    // DosageRecord 객체 생성 시 LocalDateTime 타입 생성자 사용
                    DosageRecord record = new DosageRecord(recordId, foundUserId, medId, scheduledTime, actualTakenTime, rescheduledTime, isSkipped);
                    recordList.add(record);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding dosage records for user ID " + userId + " between " + startDate + " and " + endDate + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return recordList;
    }

    /**
     * 특정 복용 기록의 실제 복용 시간을 업데이트합니다 (예: 사용자가 복용 완료 버튼 클릭).
     * actualTakenTime을 null로 설정하면 복용 완료 상태를 취소할 수도 있습니다.
     * @param recordId 업데이트할 복용 기록의 ID
     * @param actualTakenTime 새로 설정할 실제 복용 시간 (LocalDateTime 객체 또는 null)
     * @return 업데이트 성공 시 true, 실패 시 false
     */
    public static boolean updateActualTakenTime(int recordId, LocalDateTime actualTakenTime) throws SQLException {
        String sql = "UPDATE DosageRecords SET actual_taken_time = ? WHERE record_id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, toDbString(actualTakenTime)); // 실제 복용 시간 (null 허용)
            pstmt.setInt(2, recordId); // 업데이트할 기록의 ID

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating actual taken time for record ID " + recordId + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 특정 복용 기록의 재조정된 알람 시간을 업데이트합니다.
     * @param userId 특정 사용자의 ID
     * @param medId 특정 약의 ID
     * @param originalScheduledTime 해당 기록의 원래 예정된 시간 (기록을 찾기 위한 조건)
     * @param newRescheduledTime 새로 설정할 재조정 시간 (LocalDateTime 객체 또는 null)
     * @return 업데이트 성공 시 true, 실패 시 false
     */
    public static boolean updateRescheduledTime(int userId, int medId, LocalDateTime originalScheduledTime, LocalDateTime newRescheduledTime) throws SQLException {
        // user_id, med_id, scheduled_time 세 가지를 기준으로 특정 기록을 찾습니다.
        // 참고: record_date는 unique 제약조건의 일부이므로, scheduled_time만으로는 정확한 레코드를 찾기 어려울 수 있습니다.
        // 하지만 여기서는 이미 모델에 record_date 필드가 없고, scheduled_time 자체가 정확한 시점(날짜+시간)을 나타내므로 이를 사용합니다.
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

    /**
     * 특정 사용자와 약 ID에 대해 최근 N일 동안 복용한 기록을 조회합니다.
     * 스킵한 기록은 제외되며, 실제 복용된 시간 기준으로 최근 기록을 가져옵니다.
     *
     * @param userId 사용자 ID
     * @param medId 약 ID
     * @param days 최근 N일
     * @return 최근 복용 기록 리스트
     * @throws SQLException DB 접근 오류 시
     */
    public static List<DosageRecord> getRecentDosageRecords(int userId, int medId, int days) throws SQLException {
        String sql = "SELECT record_id, user_id, med_id, scheduled_time, actual_taken_time, rescheduled_time, is_skipped " +
                "FROM DosageRecords " +
                "WHERE user_id = ? AND med_id = ? AND is_skipped = false " +
                "AND actual_taken_time >= datetime('now', ? || ' days') " +
                "ORDER BY actual_taken_time DESC";

        List<DosageRecord> records = new ArrayList<>();

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, medId);
            pstmt.setString(3, "-" + days);

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
        }
        return records;
    }
}