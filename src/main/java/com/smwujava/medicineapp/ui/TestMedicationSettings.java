package com.smwujava.medicineapp.ui;

import com.smwujava.medicineapp.db.DBManager;
import com.smwujava.medicineapp.ui.panels.MedicationSettingsPanel;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TestMedicationSettings {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DBManager.initializeDatabase();

            // 테스트용 사용자 생성
            try (Connection conn = DBManager.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("INSERT INTO Users (username, password, auto_login) VALUES ('testuser', 'password', 1);");
                System.out.println("Test user created.");
            } catch (SQLException e) {
                System.err.println("User insert failed: " + e.getMessage());
            }


            JFrame frame = new JFrame("약 설정 테스트");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 600);
            frame.setLocationRelativeTo(null); // 화면 중앙

            //  테스트용 CardLayout & mainPanel 생성
            CardLayout dummyLayout = new CardLayout();
            JPanel dummyMainPanel = new JPanel(dummyLayout);

            // 필요한 인자 전달
            MedicationSettingsPanel settingsPanel = new MedicationSettingsPanel(dummyLayout, dummyMainPanel);
            frame.add(settingsPanel);

            frame.setVisible(true);
        });
    }
}