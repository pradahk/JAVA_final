package com.smwujava.medicineapp.test;

import com.smwujava.medicineapp.ui.panels.MedicationSettingsPanel;

import javax.swing.*;
import java.awt.*;

public class MedicineWindow extends JFrame {
    private final int userId;

    public MedicineWindow(int userId) {
        this.userId = userId;

        setTitle("💊 약 정보 관리");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);

        CardLayout layout = new CardLayout();
        JPanel mainPanel = new JPanel(layout);

        Runnable doNothingOnSave = () -> {};
        MedicationSettingsPanel medPanel = new MedicationSettingsPanel(userId, layout, mainPanel, doNothingOnSave);
        mainPanel.add(medPanel, "SETTINGS");

        setContentPane(mainPanel);
        layout.show(mainPanel, "SETTINGS");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            int loggedInUserId = 1;
            new MedicineWindow(loggedInUserId).setVisible(true);
        });
    }
}