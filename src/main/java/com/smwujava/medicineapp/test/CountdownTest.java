package com.smwujava.medicineapp.test;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.db.DBManager;
import com.smwujava.medicineapp.ui.panels.CountdownPanel;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class CountdownTest {
    public static void main(String[] args) {
        DBManager.initializeDatabase();

        int testUserId = 1;
        int testMedicineId = 1;

        // 💊 테스트용 알림 데이터 삽입 (2분 뒤 알람)
        try (Connection conn = DBManager.getConnection()) {
            String sql = "INSERT INTO DosageRecords (user_id, medicine_id, scheduled_time, actual_taken_time, is_skipped, rescheduled_time) VALUES (?, ?, ?, NULL, 0, NULL)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, testUserId);
                pstmt.setInt(2, testMedicineId);
                pstmt.setString(3, LocalDateTime.now().plusMinutes(2).toString());
                pstmt.executeUpdate();
                System.out.println("[CountdownPanelTest] 테스트 알림 데이터 삽입 완료");
            }
        } catch (SQLException e) {
            System.err.println("[CountdownPanelTest] 알림 데이터 삽입 중 오류 발생: " + e.getMessage());
        }

        // ⏱️ UI 테스트 시작
        JFrame frame = new JFrame("Countdown Timer Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 150);

        DosageRecordDao dao = new DosageRecordDao();
        CountdownPanel panel = new CountdownPanel(dao, testUserId);

        frame.add(panel);
        frame.setVisible(true);

        // 🧹 프레임 종료 시 테스트 알림 삭제
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                try (Connection conn = DBManager.getConnection()) {
                    String deleteSql = "DELETE FROM DosageRecords WHERE user_id = ? AND medicine_id = ? AND actual_taken_time IS NULL";
                    try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                        pstmt.setInt(1, testUserId);
                        pstmt.setInt(2, testMedicineId);
                        int deleted = pstmt.executeUpdate();
                        System.out.println("[CountdownPanelTest] 테스트 알림 " + deleted + "개 삭제 완료");
                    }
                } catch (SQLException ex) {
                    System.err.println("[CountdownPanelTest] 테스트 알림 삭제 중 오류 발생: " + ex.getMessage());
                }
            }
        });
    }
}
