package com.smwujava.medicineapp;

import com.smwujava.medicineapp.db.DBManager;
import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.service.SuggestAdjustedTime;

import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;
import com.smwujava.medicineapp.ui.panels.MainWindow;

public class MainApp {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Error setting Look and Feel: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("애플리케이션 초기화 시작...");

        // 1. 데이터베이스 초기화 (Connection 객체를 DBManager 내부에서 관리)
        DBManager.initializeDatabase();
        System.out.println("데이터베이스 초기화 완료.");

        // 2. DAO 및 서비스 인스턴스 생성
        DosageRecordDao dosageRecordDao = new DosageRecordDao();
        SuggestAdjustedTime suggestAdjustedTimeService = new SuggestAdjustedTime(dosageRecordDao);

        System.out.println("애플리케이션의 메인 로직 시작.");

        SwingUtilities.invokeLater(() -> {
            new MainWindow();
        });

        // 애플리케이션 종료 시 DB 연결 해제
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("애플리케이션 종료 중. 데이터베이스 연결 해제...");
            DBManager.closeConnection(); // <<< 수정: 매개변수 없이 호출 >>>
            System.out.println("데이터베이스 연결 해제 완료.");
        }));
    }
}