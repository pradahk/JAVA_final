package com.smwujava.medicineapp.ui;

import com.smwujava.medicineapp.ui.panels.MedicationSettingsPanel;

import javax.swing.*;

public class TestMedicationSettings {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("약 설정 테스트");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 600);
            frame.setLocationRelativeTo(null); // 화면 중앙
            frame.add(new MedicationSettingsPanel());
            frame.setVisible(true);
        });
    }
}
