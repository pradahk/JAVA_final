package com.smwujava.medicineapp.test;

import com.smwujava.medicineapp.db.DBManager;
import com.smwujava.medicineapp.ui.panels.BottomNavPanel;
import com.smwujava.medicineapp.ui.panels.CalendarPanel;
import com.smwujava.medicineapp.ui.panels.LifestylePanel;
import com.smwujava.medicineapp.ui.panels.MedicationSettingsPanel;
import com.smwujava.medicineapp.Scheduler.AlarmScheduler;
import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.dao.UserPatternDao;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class CalendarWindow {
    private static JFrame mainFrameInstance;

    public static void main(String[] args) {
        try {
            DBManager.initializeDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "데이터베이스 초기화 실패.", "실행 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            mainFrameInstance = new JFrame("나의 약 복용 캘린더 (테스트)");
            mainFrameInstance.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrameInstance.setLayout(new BorderLayout());

            CardLayout appCardLayout = new CardLayout();
            JPanel mainContentPanel = new JPanel(appCardLayout);

            final int currentUserId = 1;

            CalendarPanel calendarPage = new CalendarPanel(appCardLayout, mainContentPanel);
            LifestylePanel lifestylePage = new LifestylePanel(currentUserId, mainContentPanel, appCardLayout);

            Runnable refreshCalendarAction = () -> calendarPage.refresh();
            MedicationSettingsPanel settingsPage = new MedicationSettingsPanel(currentUserId, appCardLayout, mainContentPanel, refreshCalendarAction);

            mainContentPanel.add(calendarPage, "CALENDAR");
            mainContentPanel.add(lifestylePage, "LIFESTYLE");
            mainContentPanel.add(settingsPage, "SETTINGS");

            Consumer<String> navigator = panelName -> appCardLayout.show(mainContentPanel, panelName);
            BottomNavPanel bottomNav = new BottomNavPanel(navigator);

            mainFrameInstance.add(mainContentPanel, BorderLayout.CENTER);
            mainFrameInstance.add(bottomNav, BorderLayout.SOUTH);

            appCardLayout.show(mainContentPanel, "CALENDAR");

            mainFrameInstance.setSize(new Dimension(500, 800));
            mainFrameInstance.setMinimumSize(new Dimension(450, 700));
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

    public static JFrame getFrame() {
        return mainFrameInstance;
    }

    public static boolean isOpen() {
        return mainFrameInstance != null && mainFrameInstance.isVisible();
    }
}