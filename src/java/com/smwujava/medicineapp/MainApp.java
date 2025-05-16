package com.smwujava.medicineapp;

import javax.swing.*;
// import com.yourcompany.dementiaapp.dao.DatabaseManager; // 이 줄을 주석 처리 또는 삭제
// import com.smwujav.medicineapp.dao.DatabaseManager; // 여러분의 실제 패키지명으로 수정 예정

public class MainApp { // 파일 이름과 클래스 이름이 같아야 함

    public static void main(String[] args) {
        // 데이터베이스 초기화 (필요시 테이블 생성 등)
        // 1주차 DB 구현 시 여기에 호출 코드 추가 예정
        // DatabaseManager.initializeDatabase(); // 이 줄을 주석 처리

        // Swing UI는 EDT에서 실행되어야 합니다.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // 메인 윈도우 생성 및 설정
                JFrame frame = new JFrame("치매 환자 도우미"); // 창 제목 설정
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 닫기 버튼 누르면 앱 종료
                frame.setSize(800, 600); // 창 크기 설정 (임시)
                frame.setLocationRelativeTo(null); // 화면 중앙에 표시

                // TODO: 여기에 Figma 디자인 기반의 메인 패널(들)을 추가합니다.
                // 예: JPanel mainPanel = new JPanel(); frame.add(mainPanel);

                frame.setVisible(true); // 창을 보이게 설정
            }
        });

        // 애플리케이션 종료 시 DB 연결 해제 (선택 사항이지만 좋은 습관)
        // Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        //     public void run() {
        //         DatabaseManager.closeConnection(); // 이 줄을 주석 처리
        //     }
        // }));
    }
}
