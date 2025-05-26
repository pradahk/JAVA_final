package com.smwujava.medicineapp;

import com.smwujava.medicineapp.db.DBManager; // 저희가 만든 DBManager 클래스 import
import javax.swing.*; // Swing UI 관련 import는 유지
import com.formdev.flatlaf.FlatLightLaf;
import com.smwujava.medicineapp.ui.panels.MainWindow;

public class MainApp {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new MainWindow(); // 메인 창 실행
        });

        // TODO: 애플리케이션 시작 시 데이터베이스 초기화 부분을 여기에 추가합니다.
        System.out.println("애플리케이션 초기화 시작..."); // 디버깅용 출력

        // 올바른 데이터베이스 초기화 호출:
        DBManager.initializeDatabase(); // <-- 이 한 줄만 있으면 됩니다.
        // 이 initializeDatabase() 메서드 내부에서 DB 파일이 없으면 만들고 테이블 스키마를 적용합니다.
        System.out.println("데이터베이스 초기화 완료.");

        // TODO: 데이터베이스 초기화가 완료된 후, 여기에 애플리케이션의 실제 시작 로직 작성
        // 예: 로그인 창 표시 등의 초기 UI 로직 시작
        System.out.println("애플리케이션의 메인 로직 시작.");

        // [UI는 그대로 유지] - Swing UI 시작 코드
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("치매 환자 도우미");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}