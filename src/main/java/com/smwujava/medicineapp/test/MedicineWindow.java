package com.smwujava.medicineapp.test;

import com.smwujava.medicineapp.ui.panels.MedicationSettingsPanel;

import javax.swing.*;
import java.awt.*;

public class MedicineWindow extends JFrame {
    private final int userId;

    public MedicineWindow(int userId) {
        this.userId = userId;

        setTitle("💊 약 정보 관리");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // 화면 중앙 정렬

        // CardLayout 기반 메인 패널 생성
        CardLayout layout = new CardLayout();
        JPanel mainPanel = new JPanel(layout);

        // MedicationSettingsPanel에 실제 로그인된 사용자 ID 넘김
        MedicationSettingsPanel medPanel = new MedicationSettingsPanel(userId, layout, mainPanel);
        mainPanel.add(medPanel, "medication");

        // 나중에 다른 화면 붙일 수 있음: 예) mainPanel.add(homePanel, "home");

        // 창 구성
        setContentPane(mainPanel);
        layout.show(mainPanel, "medication");
    }

    // 예시용 main: 실제 앱에서는 로그인 후 이걸 호출함
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            int loggedInUserId = 1; // 진짜로는 로그인 결과 받아야 함
            new MedicineWindow(loggedInUserId).setVisible(true);
        });
    }
}