package com.smwujava.medicineapp.admin;

import javax.swing.*;

public class AdminDashboardTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("관리자 페이지");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null);

            frame.setContentPane(new AdminDashboardPanel());
            frame.setVisible(true);
        });
    }
}