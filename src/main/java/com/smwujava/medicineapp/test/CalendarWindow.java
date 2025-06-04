package com.smwujava.medicineapp.test; // 또는 com.smwujava.medicineapp;

import com.smwujava.medicineapp.db.DBManager;
import com.smwujava.medicineapp.ui.panels.CalendarPanel;
import com.smwujava.medicineapp.ui.panels.MedicationSettingsPanel;
import com.smwujava.medicineapp.Scheduler.AlarmScheduler;
import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.dao.UserPatternDao;
import com.smwujava.medicineapp.dao.MedicineDao;
// import com.smwujava.medicineapp.service.AlarmManager; // AlarmPopupWindow에서 직접 사용
// import java.time.LocalDateTime; // AlarmPopupWindow에서 직접 사용

import javax.swing.*;
import java.awt.*;

public class CalendarWindow {

    private static JFrame mainFrameInstance; // 정적 프레임 인스턴스

    public static void main(String[] args) {
        try {
            DBManager.initializeDatabase();
        } catch (Exception e) {
            System.err.println("DB 초기화 중 심각한 오류 발생. 애플리케이션을 시작할 수 없습니다.");
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "데이터베이스 초기화 실패: " + e.getMessage() + "\n프로그램을 종료합니다.", "실행 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            // mainFrameInstance에 생성된 프레임 할당
            mainFrameInstance = new JFrame("나의 약 복용 캘린더");
            mainFrameInstance.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrameInstance.setLayout(new BorderLayout());

            CardLayout appCardLayout = new CardLayout();
            JPanel mainContentPanel = new JPanel(appCardLayout);

            final int currentUserId = 1;

            CalendarPanel calendarPage = new CalendarPanel(appCardLayout, mainContentPanel);
            MedicationSettingsPanel settingsPage = new MedicationSettingsPanel(currentUserId, appCardLayout, mainContentPanel);

            mainContentPanel.add(calendarPage, "CALENDAR");
            mainContentPanel.add(settingsPage, "SETTINGS");

            mainFrameInstance.add(mainContentPanel, BorderLayout.CENTER);

            // BottomNavPanel이 있다면 여기에 추가
            // Consumer<String> navigator = panelName -> appCardLayout.show(mainContentPanel, panelName);
            // BottomNavPanel bottomNav = new BottomNavPanel(navigator);
            // mainFrameInstance.add(bottomNav, BorderLayout.SOUTH);

            appCardLayout.show(mainContentPanel, "CALENDAR");

            mainFrameInstance.setMinimumSize(new Dimension(900, 700));
            mainFrameInstance.pack();
            mainFrameInstance.setLocationRelativeTo(null);
            mainFrameInstance.setVisible(true);

            AlarmScheduler scheduler = new AlarmScheduler(
                    mainFrameInstance,
                    currentUserId,
                    new DosageRecordDao(),
                    new UserPatternDao(),
                    new MedicineDao()
            );
            scheduler.start();
        });
    }

    // AlarmPopupWindow에서 사용할 수 있도록 getFrame() 메서드 복구
    public static JFrame getFrame() {
        return mainFrameInstance;
    }

    // AlarmPopupWindow에서 사용할 수 있도록 isOpen() 메서드 복구
    public static boolean isOpen() {
        return mainFrameInstance != null && mainFrameInstance.isVisible();
    }
}