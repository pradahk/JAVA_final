package com.smwujava.medicineapp.test; // 패키지 선언 변경

import com.smwujava.medicineapp.db.DBManager;
import com.smwujava.medicineapp.ui.panels.CalendarPanel;

import javax.swing.*;
import java.awt.*;

public class CalendarWindow {

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
        });
    }
}