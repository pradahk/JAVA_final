package com.smwujava.medicineapp;

import com.smwujava.medicineapp.db.DBManager;
import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.service.SuggestAdjustedTime;

import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;
import com.smwujava.medicineapp.ui.panels.MainWindow;
import java.sql.Connection; // Connection 임포트
import java.sql.SQLException; // SQLException 임포트

public class MainApp {

    // MainApp이 관리할 Connection 객체 선언
    private static Connection appConnection = null;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Error setting Look and Feel: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("애플리케이션 초기화 시작...");

        try {
            // 1. 데이터베이스 초기화 (스키마 생성 등)
            DBManager.initializeDatabase();
            System.out.println("데이터베이스 초기화 완료.");

            // 2. MainApp에서 Connection 객체를 얻어와 유지
            appConnection = DBManager.getConnection();
            System.out.println("메인 애플리케이션 DB 연결 설정 완료.");


            // 3. DAO 및 서비스 인스턴스 생성 (이들은 내부적으로 DBManager.getConnection()을 호출)
            DosageRecordDao dosageRecordDao = new DosageRecordDao();
            SuggestAdjustedTime suggestAdjustedTimeService = new SuggestAdjustedTime(dosageRecordDao);

            System.out.println("애플리케이션의 메인 로직 시작.");

            SwingUtilities.invokeLater(() -> {
                new MainWindow();
            });

            // 애플리케이션 종료 시 DB 연결 해제
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("애플리케이션 종료 중. 데이터베이스 연결 해제...");
                // MainApp이 유지하고 있던 Connection을 닫습니다.
                if (appConnection != null) {
                    DBManager.closeConnection(appConnection); // DBManager의 closeConnection(Connection conn) 호출
                }
                System.out.println("데이터베이스 연결 해제 완료.");
            }));

        } catch (SQLException e) {
            System.err.println("애플리케이션 초기화 중 데이터베이스 오류 발생: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "데이터베이스 연결 오류가 발생하여 애플리케이션을 시작할 수 없습니다.\n" + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            System.exit(1); // 오류 발생 시 애플리케이션 종료
        }
    }
}