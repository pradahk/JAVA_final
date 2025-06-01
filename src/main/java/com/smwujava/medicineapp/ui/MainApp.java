package com.smwujava.medicineapp.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.smwujava.medicineapp.ui.panels.MainWindow;
import com.smwujava.medicineapp.Scheduler.AlarmScheduler;
import com.smwujava.medicineapp.dao.DosageRecordDao;

import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ✅ 알람 스케줄러 시작
        AlarmScheduler scheduler = new AlarmScheduler(new DosageRecordDao());
        scheduler.start();  // 약 복용 시간 주기적으로 확인

        SwingUtilities.invokeLater(() -> {
            new MainWindow(); // 메인 창 실행
        });
    }
}