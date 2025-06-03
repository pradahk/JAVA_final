package com.smwujava.medicineapp.test; // 패키지 선언 변경

import com.smwujava.medicineapp.db.DBManager;
import com.smwujava.medicineapp.ui.panels.CalendarPanel;
import com.smwujava.medicineapp.Scheduler.AlarmScheduler;
import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.dao.UserPatternDao;
import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.service.AlarmManager;


import java.time.LocalDateTime;
import javax.swing.*;
import java.awt.*;

public class CalendarWindow {
    private static JFrame frame;

    public static void launch() {
        if (frame != null && frame.isVisible()) {
            return; // 이미 열려 있음
        }

        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("나의 약 복용 캘린더");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            CardLayout appCardLayout = new CardLayout();
            JPanel mainContentPanel = new JPanel(appCardLayout);

            CalendarPanel calendarPage = new CalendarPanel(appCardLayout, mainContentPanel);
            mainContentPanel.add(calendarPage, "CALENDAR_PAGE");

            frame.add(mainContentPanel, BorderLayout.CENTER);
            appCardLayout.show(mainContentPanel, "CALENDAR_PAGE");

            frame.setMinimumSize(new Dimension(900, 700));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public static JFrame getFrame() {
        return frame;
    }

    public static boolean isOpen() {
        return frame != null && frame.isVisible();
    }


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
            JFrame frame = new JFrame("나의 약 복용 캘린더 (Test Window)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            CardLayout appCardLayout = new CardLayout();
            JPanel mainContentPanel = new JPanel(appCardLayout);

            CalendarPanel calendarPage = new CalendarPanel(appCardLayout, mainContentPanel);

            mainContentPanel.add(calendarPage, "CALENDAR_PAGE");
            frame.add(mainContentPanel, BorderLayout.CENTER);
            appCardLayout.show(mainContentPanel, "CALENDAR_PAGE");

            frame.setMinimumSize(new Dimension(900, 700));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // ✅ 알람 스케줄러 실행
            AlarmScheduler scheduler = new AlarmScheduler(
                    frame,
                    1,
                    new DosageRecordDao(),
                    new UserPatternDao(),
                    new MedicineDao()
            );
            scheduler.start();  // 알람이 캘린더와 함께 작동됨!

            // ✅ 테스트용 알람: 5초 뒤 자동 알람 (medId = 101)
            AlarmManager.scheduleAlarm(
                    frame,
                    1, // userId
                    101, // medId (DB에 존재하는 값이면 좋음)
                    LocalDateTime.now().plusSeconds(5)
            );// 5초 뒤 알람
        });
    }
}

