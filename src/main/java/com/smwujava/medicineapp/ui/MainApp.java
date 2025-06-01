package com.smwujava.medicineapp.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.smwujava.medicineapp.ui.panels.MainWindow;

import javax.swing.*;

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
    }
}