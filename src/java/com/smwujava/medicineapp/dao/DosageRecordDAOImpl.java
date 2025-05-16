package java.com.smwujava.medicineapp.dao;

import java.com.smwujava.medicineapp.model.DosageRecord;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 데이터베이스에서 복용 기록을 조회하는 DAO 구현체입니다.
 * 사용자 ID, 약 ID, scheduled_time 기준으로 최근 7개의 복용 기록을 가져옵니다.
 *
 * 💡 주의사항:
 * - scheduled_time 은 'HH:MM' 포맷으로 시각만 비교합니다.
 * - actual_taken_time 이 null일 수 있으니 예외 처리 포함되어 있음.
 * - DB 파일명은 'medicine.db'로 되어 있으니 환경에 따라 수정 가능.
 */
public class DosageRecordDAOImpl implements DosageRecordDAO {

    private static final String DB_URL = "jdbc:sqlite:medicine.db"; // SQLite DB 파일 경로

    @Override
    public List<DosageRecord> getRecentDosageRecords(int userId, int medId, LocalTime scheduledTime) {
        List<DosageRecord> records = new ArrayList<>();

        // 특정 사용자/약/시각에 대해 최근 7개의 복용 기록을 조회
        String sql = "SELECT * FROM DosageRecords " +
                "WHERE user_id = ? AND med_id = ? AND strftime('%H:%M', scheduled_time) = ? " +
                "ORDER BY scheduled_time DESC LIMIT 7";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);                         // 사용자 ID 설정
            pstmt.setInt(2, medId);                          // 약 ID 설정
            pstmt.setString(3, scheduledTime.toString());    // 시각 비교 ('HH:MM')

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // 문자열인지 DATETIME인지 확인 필요 -> 현재 문자열로 가져옴
                LocalDateTime scheduled = LocalDateTime.parse(rs.getString("scheduled_time"));
                String actualTimeStr = rs.getString("actual_taken_time");
                LocalDateTime actual = actualTimeStr != null ? LocalDateTime.parse(actualTimeStr) : null;

                // 조회된 데이터를 DosageRecord 객체로 변환하여 리스트에 추가
                DosageRecord record = new DosageRecord(
                        rs.getInt("user_id"),
                        rs.getInt("med_id"),
                        scheduled,
                        actual
                );
                records.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // 예외 출력
        }

        return records;
    }

    @Override
    public void updateRescheduledTime(int userId, int medId, LocalDateTime scheduledTime, LocalDateTime rescheduledTime) {
        String sql = "UPDATE DosageRecords SET rescheduled_time = ? " +
                "WHERE user_id = ? AND med_id = ? AND scheduled_time = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rescheduledTime.toString());
            pstmt.setInt(2, userId);
            pstmt.setInt(3, medId);
            pstmt.setString(4, scheduledTime.toString());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resetAllRescheduledTimes(int userId) {
        String sql = "UPDATE DosageRecords SET rescheduled_time = NULL WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
