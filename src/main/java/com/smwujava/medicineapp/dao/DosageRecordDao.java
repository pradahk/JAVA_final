package com.smwujava.medicineapp.dao;

import com.smwujava.medicineapp.db.DBManager;
import com.smwujava.medicineapp.model.DosageRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime; // <<< 변경점 1: LocalDateTime 임포트
import java.time.format.DateTimeFormatter; // <<< 변경점 1: DateTimeFormatter 임포트
import java.util.ArrayList;
import java.util.List;

/**
 * 복용 기록(DosageRecords 테이블) 데이터 접근 객체 (DAO)
 * 데이터베이스의 DosageRecords 테이블과 관련된 데이터 CRUD 작업을 수행합니다.
 * 이 클래스는 static 메서드로 구성되며, 내부적으로 DBManager를 통해 Connection을 관리합니다.
 */
public class DosageRecordDao {

    // <<< 변경점 2: 날짜/시간 문자열 포맷터 상수 정의 >>>
    // 데이터베이스의 DATETIME 컬럼 형식에 맞춰 포맷터를 정의합니다. (예: "yyyy-MM-dd HH:mm:ss")
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DosageRecordDao() {
        // Utility 클래스처럼 사용되므로 인스턴스화 방지
    }

    // <<< 변경점 3: LocalDateTime <-> String 변환을 위한 도우미 메서드 (null 처리 포함) >>>
    /**
     * LocalDateTime 객체를 DB에 저장할 수 있는 문자열 형식으로 변환합니다. null이면 null을 반환합니다.
     * @param dateTime 변환할 LocalDateTime 객체
     * @return DB 형식의 문자열 또는 null
     */
    private static String toDbString(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(FORMATTER);
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
        return LocalDateTime.parse(dateTimeString, FORMATTER);
    }

    /**
     * 새로운 복용 기록을 데이터베이스의 DosageRecords 테이블에 삽입합니다 (예: 복용 스케줄 생성).
     *
     * @param record 삽입할 복용 기록 (DosageRecord 객체)
     * @return 데이터베이스에서 자동 생성된 기록의 record_id. 오류 발생 시 -1을 반환합니다.
     * @throws SQLException 데이터베이스 접근 오류 발생 시
     */
    public static int insertDosageRecord(DosageRecord record) throws SQLException { // <<< SQLException 던지도록 변경
        // <<< 변경점 4: SQL INSERT 구문 변경 (record_date 제거, rescheduled_time 추가) >>>
        String sql = "INSERT INTO DosageRecords (user_id, med_id, scheduled_time, actual_taken_time, rescheduled_time) VALUES (?, ?, ?, ?, ?)";
        int generatedId = -1;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, record.getUserId());
            pstmt.setInt(2, record.getMedId());
            // <<< 변경점 4: LocalDateTime을 String으로 변환하여 설정 >>>
            pstmt.setString(3, toDbString(record.getScheduledTime()));
            pstmt.setString(4, toDbString(record.getActualTakenTime())); // actualTakenTime이 null이면 DB에 NULL로 저장
            pstmt.setString(5, toDbString(record.getRescheduledTime())); // rescheduledTime이 null이면 DB에 NULL로 저장

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
            throw e; // 오류 발생 시 서비스 레이어로 예외를 던짐
        }
        return generatedId;
    }

    /**
     * 특정 사용자 ID에 해당하는 복용 기록을 특정 날짜 범위로 조회합니다.
     * (예: 캘린더에 표시할 한 달치 기록 등)
     *
     * @param userId 조회할 사용자의 ID
     * @param startDate 조회 시작 날짜 ("YYYY-MM-DD" 형태의 문자열)
     * @param endDate 조회 종료 날짜 ("YYYY-MM-DD" 형태의 문자열)
     * @return 해당 사용자의 지정된 기간 내 복용 기록 목록 (List<DosageRecord>). 기록이 없으면 빈 목록을 반환합니다.
     * @throws SQLException 데이터베이스 접근 오류 발생 시
     */
    public static List<DosageRecord> findRecordsByUserIdAndDateRange(int userId, String startDate, String endDate) throws SQLException { // <<< SQLException 던지도록 변경
        // <<< 변경점 5: SQL SELECT 구문 변경 (record_date 제거, rescheduled_time 추가, 정렬 기준 변경) >>>
        // `scheduled_time` 컬럼을 사용하여 날짜 범위를 지정합니다. 날짜만 주어지면 해당 날짜의 시작과 끝으로 변환합니다.
        String sql = "SELECT record_id, user_id, med_id, scheduled_time, actual_taken_time, rescheduled_time FROM DosageRecords WHERE user_id = ? AND scheduled_time BETWEEN ? AND ? ORDER BY scheduled_time";
        List<DosageRecord> recordList = new ArrayList<>();

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            // <<< 변경점 5: 날짜 범위 매개변수를 'YYYY-MM-DD 00:00:00' ~ 'YYYY-MM-DD 23:59:59' 형식으로 변환 >>>
            pstmt.setString(2, startDate + " 00:00:00"); // 시작 날짜의 자정
            pstmt.setString(3, endDate + " 23:59:59");   // 종료 날짜의 마지막 시간

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int recordId = rs.getInt("record_id");
                    int foundUserId = rs.getInt("user_id");
                    int medId = rs.getInt("med_id");
                    // String recordDate = rs.getString("record_date"); // <<< 변경점 5: record_date 필드 제거 (모델과 일치) >>>

                    // <<< 변경점 5: DB 문자열을 LocalDateTime으로 변환하여 읽기 >>>
                    LocalDateTime scheduledTime = fromDbString(rs.getString("scheduled_time"));
                    LocalDateTime actualTakenTime = fromDbString(rs.getString("actual_taken_time"));
                    LocalDateTime rescheduledTime = fromDbString(rs.getString("rescheduled_time")); // <<< 변경점 5: rescheduled_time 읽기 >>>

                    // <<< 변경점 5: DosageRecord 객체 생성 시 LocalDateTime 타입 생성자 사용 >>>
                    DosageRecord record = new DosageRecord(recordId, foundUserId, medId, scheduledTime, actualTakenTime, rescheduledTime);
                    recordList.add(record);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding dosage records for user ID " + userId + " between " + startDate + " and " + endDate + ": " + e.getMessage());
            e.printStackTrace();
            throw e; // 오류 발생 시 서비스 레이어로 예외를 던짐
        }
        return recordList;
    }

    /**
     * 특정 복용 기록의 실제 복용 시간을 업데이트합니다 (예: 사용자가 복용 완료 버튼 클릭).
     * actualTakenTime을 null로 설정하면 복용 완료 상태를 취소할 수도 있습니다.
     *
     * @param recordId 업데이트할 복용 기록의 ID
     * @param actualTakenTime 새로 설정할 실제 복용 시간 (LocalDateTime 객체 또는 null)
     * @return 업데이트 성공 시 true, 실패 시 false
     * @throws SQLException 데이터베이스 접근 오류 발생 시
     */
    public static boolean updateActualTakenTime(int recordId, LocalDateTime actualTakenTime) throws SQLException { // <<< 변경점 6: 매개변수 타입 LocalDateTime으로 변경
        String sql = "UPDATE DosageRecords SET actual_taken_time = ? WHERE record_id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // <<< 변경점 6: LocalDateTime을 String으로 변환하여 설정 >>>
            pstmt.setString(1, toDbString(actualTakenTime)); // 실제 복용 시간 (null 허용)
            pstmt.setInt(2, recordId); // 업데이트할 기록의 ID

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating actual taken time for record ID " + recordId + ": " + e.getMessage());
            e.printStackTrace();
            throw e; // 오류 발생 시 서비스 레이어로 예외를 던짐
        }
    }

    // -------------------- <<< 변경점 7: 새로운 메서드 추가 >>> --------------------

    /**
     * 특정 복용 기록의 재조정된 알람 시간을 업데이트합니다.
     *
     * @param userId 특정 사용자의 ID
     * @param medId 특정 약의 ID
     * @param originalScheduledTime 해당 기록의 원래 예정된 시간 (기록을 찾기 위한 조건)
     * @param newRescheduledTime 새로 설정할 재조정 시간 (LocalDateTime 객체 또는 null)
     * @return 업데이트 성공 시 true, 실패 시 false
     * @throws SQLException 데이터베이스 접근 오류 발생 시
     */
    public static boolean updateRescheduledTime(int userId, int medId, LocalDateTime originalScheduledTime, LocalDateTime newRescheduledTime) throws SQLException {
        // user_id, med_id, scheduled_time 세 가지를 기준으로 특정 기록을 찾습니다.
        String sql = "UPDATE DosageRecords SET rescheduled_time = ? WHERE user_id = ? AND med_id = ? AND scheduled_time = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, toDbString(newRescheduledTime)); // LocalDateTime을 String으로 변환하여 설정
            pstmt.setInt(2, userId);
            pstmt.setInt(3, medId);
            pstmt.setString(4, toDbString(originalScheduledTime)); // 원래 예정 시간을 조건으로 사용

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
     *
     * @param userId 재조정 시간을 초기화할 사용자의 ID
     * @return 초기화된 행이 1개 이상 있으면 true, 아니면 false
     * @throws SQLException 데이터베이스 접근 오류 발생 시
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
}