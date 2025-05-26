package com.smwujava.medicineapp.dao;

import com.smwujava.medicineapp.db.DBManager; // DBManager 사용
import com.smwujava.medicineapp.model.DosageRecord; // DosageRecord 모델 사용
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Generated keys 가져올 때 필요
import java.util.ArrayList; // 목록을 담을 ArrayList
import java.util.List;      // 목록 인터페이스

// 복용 기록(DosageRecords 테이블) 데이터 접근 객체 (DAO)
// 데이터베이스의 DosageRecords 테이블과 관련된 데이터 CRUD 작업을 수행합니다.
// 이 클래스는 static 메서드로 구성되며, 내부적으로 DBManager를 통해 Connection을 관리합니다.
public class DosageRecordDao {
    private DosageRecordDao() {
        // Utility 클래스처럼 사용되므로 인스턴스화 방지
    }

    /**
     * 새로운 복용 기록을 데이터베이스의 DosageRecords 테이블에 삽입합니다 (예: 복용 스케줄 생성).
     * @param record 삽입할 복용 기록 (DosageRecord 객체, recordId 및 actualTakenTime은 DB에서 처리)
     * @return 데이터베이스에서 자동 생성된 기록의 record_id. 오류 발생 시 -1을 반환합니다.
     */
    public static int insertDosageRecord(DosageRecord record) { // static 메서드
        // SQL INSERT 구문. record_id는 AUTOINCREMENT이므로 제외.
        // actual_taken_time은 처음에는 null로 삽입됩니다.
        String sql = "INSERT INTO DosageRecords (user_id, med_id, record_date, scheduled_time, actual_taken_time) VALUES (?, ?, ?, ?, ?)";
        int generatedId = -1; // 생성된 record_id를 저장할 변수

        try (Connection conn = DBManager.getConnection(); // DBManager에서 연결 가져오기 (자동 닫힘)
             // 자동 생성된 키(record_id)를 받아올 수 있도록 옵션 지정
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // SQL 구문의 ?에 실제 값을 설정 (인덱스는 1부터 시작)
            pstmt.setInt(1, record.getUserId());
            pstmt.setInt(2, record.getMedId());
            pstmt.setString(3, record.getRecordDate());
            pstmt.setString(4, record.getScheduledTime());
            // actual_taken_time은 처음 삽입 시 null일 가능성이 높음
            // setString(index, null)을 사용하면 DB에 NULL로 저장됩니다.
            pstmt.setString(5, record.getActualTakenTime()); // null 또는 문자열

            // SQL 실행 (INSERT)
            int affectedRows = pstmt.executeUpdate();

            // 삽입된 행이 있고 자동 생성된 키가 있다면 가져옴
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) { // 자동 생성된 키 결과 (자동 닫힘)
                    if (rs.next()) {
                        generatedId = rs.getInt(1); // 첫 번째 자동 생성된 키(record_id) 가져오기
                        // System.out.println("DosageRecord inserted successfully with ID: " + generatedId); // 디버깅
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error inserting dosage record for user ID " + record.getUserId() + ", med ID " + record.getMedId() + ", date " + record.getRecordDate() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return generatedId; // 생성된 ID 반환
    }

    /**
     * 특정 사용자 ID에 해당하는 복용 기록을 특정 날짜 범위로 조회합니다.
     * (예: 캘린더에 표시할 한 달치 기록 등)
     * @param userId 조회할 사용자의 ID
     * @param startDate 조회 시작 날짜 ("YYYY-MM-DD" 형태의 문자열)
     * @param endDate 조회 종료 날짜 ("YYYY-MM-DD" 형태의 문자열)
     * @return 해당 사용자의 지정된 기간 내 복용 기록 목록 (List<DosageRecord>). 기록이 없으면 빈 목록을 반환합니다.
     */
    public static List<DosageRecord> findRecordsByUserIdAndDateRange(int userId, String startDate, String endDate) { // static 메서드
        // SQL SELECT 구문. 특정 user_id를 갖고, record_date가 날짜 범위에 포함되는 모든 기록을 가져옴.
        // BETWEEN 연산자로 날짜 범위 지정. record_date와 scheduled_time 순으로 정렬.
        String sql = "SELECT record_id, user_id, med_id, record_date, scheduled_time, actual_taken_time FROM DosageRecords WHERE user_id = ? AND record_date BETWEEN ? AND ? ORDER BY record_date, scheduled_time";
        List<DosageRecord> recordList = new ArrayList<>(); // 조회된 기록들을 담을 목록

        try (Connection conn = DBManager.getConnection(); // DBManager에서 연결 가져오기 (자동 닫힘)
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // PreparedStatement 생성 (자동 닫힘)

            // SQL 구문의 ?에 실제 값 설정
            pstmt.setInt(1, userId);      // 첫 번째 (?)에 사용자 ID 설정
            pstmt.setString(2, startDate); // 두 번째 (?)에 시작 날짜 설정
            pstmt.setString(3, endDate);   // 세 번째 (?)에 종료 날짜 설정

            try (ResultSet rs = pstmt.executeQuery()) { // 쿼리 실행 및 결과 (자동 닫힘)
                // 조회된 결과 행이 있는 동안 반복하여 데이터를 읽어옴
                while (rs.next()) {
                    // ResultSet에서 컬럼 이름으로 값을 읽어와 DosageRecord 객체 생성
                    int recordId = rs.getInt("record_id");
                    int foundUserId = rs.getInt("user_id");
                    int medId = rs.getInt("med_id");
                    String recordDate = rs.getString("record_date");
                    String scheduledTime = rs.getString("scheduled_time");
                    String actualTakenTime = rs.getString("actual_taken_time"); // 실제 복용 시간 (null일 수 있음)

                    // DosageRecord 객체 생성 (DB에서 읽어온 모든 필드 포함)
                    DosageRecord record = new DosageRecord(recordId, foundUserId, medId, recordDate, scheduledTime, actualTakenTime);
                    recordList.add(record); // 목록에 추가
                }
                // System.out.println("Found " + recordList.size() + " dosage records for user ID " + userId + " between " + startDate + " and " + endDate); // 디버깅

            }

        } catch (SQLException e) {
            System.err.println("Error finding dosage records for user ID " + userId + " between " + startDate + " and " + endDate + ": " + e.getMessage());
            e.printStackTrace();
            // 오류 발생 시 빈 목록 반환하거나 예외 처리
        }
        return recordList; // 복용 기록 목록 반환 (없으면 빈 목록)
    }

    /**
     * 특정 복용 기록의 실제 복용 시간을 업데이트합니다 (예: 사용자가 복용 완료 버튼 클릭).
     * actualTakenTime을 null로 설정하면 복용 완료 상태를 취소할 수도 있습니다.
     * @param recordId 업데이트할 복용 기록의 ID
     * @param actualTakenTime 새로 설정할 실제 복용 시간 ("YYYY-MM-DD HH:MM" 형태 문자열 또는 null)
     * @return 업데이트 성공 시 true, 실패 시 false
     */
    public static boolean updateActualTakenTime(int recordId, String actualTakenTime) { // static 메서드
        // SQL UPDATE 구문. record_id를 기준으로 해당 기록을 찾아서 actual_taken_time만 수정합니다.
        String sql = "UPDATE DosageRecords SET actual_taken_time = ? WHERE record_id = ?";

        try (Connection conn = DBManager.getConnection(); // DBManager에서 연결 가져오기 (자동 닫힘)
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // PreparedStatement 생성 (자동 닫힘)

            // SQL 구문의 ?에 실제 값 설정
            // actualTakenTime이 null이면 DB에도 NULL로 저장됩니다.
            pstmt.setString(1, actualTakenTime); // 실제 복용 시간 (null 허용)
            // WHERE 절의 조건을 위한 record_id 설정
            pstmt.setInt(2, recordId); // 업데이트할 기록의 ID

            // SQL 실행 (UPDATE)
            int affectedRows = pstmt.executeUpdate();

            // System.out.println("DosageRecord update affected rows for record ID " + recordId + ": " + affectedRows); // 디버깅
            return affectedRows > 0; // 1개 이상의 행에 영향이 있었다면 성공 (UPDATE는 1개 행에 영향을 줌)

        } catch (SQLException e) {
            System.err.println("Error updating actual taken time for record ID " + recordId + ": " + e.getMessage());
            e.printStackTrace();
            return false; // 오류 발생 시 실패
        }
    }

    // TODO: 필요에 따라 다른 데이터 접근 메서드를 추가할 수 있습니다.
    // 예를 들어:
    // - 특정 복용 기록 ID로 기록을 찾는 메서드: findRecordById(int recordId)
    // - 복용 기록 삭제 메서드: deleteDosageRecord(int recordId)
    // - 특정 약의 모든 복용 기록 삭제 메서드 (Medicine 테이블 삭제 시 ON DELETE CASCADE에 의해 자동 삭제될 수도 있습니다): deleteRecordsByMedicineId(int medId)
    // - 특정 사용자의 모든 복용 기록 삭제 메서드 (Users 테이블 삭제 시 ON DELETE CASCADE에 의해 자동 삭제될 수도 있습니다): deleteRecordsByUserId(int userId)
}
