package com.smwujava.medicineapp.ui;

import com.smwujava.medicineapp.ui.panels.MedicationSettingsPanel;

import javax.swing.*;
import java.awt.*;

public class TestMedicationSettings {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("💊 약 설정 테스트");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 600);
            frame.setLocationRelativeTo(null); // 화면 중앙

            // ✅ 테스트용 CardLayout & mainPanel 생성
            CardLayout dummyLayout = new CardLayout();
            JPanel dummyMainPanel = new JPanel(dummyLayout);

            // ✅ 필요한 인자 전달
            MedicationSettingsPanel settingsPanel = new MedicationSettingsPanel(dummyLayout, dummyMainPanel);
            frame.add(settingsPanel);

            frame.setVisible(true);
        });
    }
}