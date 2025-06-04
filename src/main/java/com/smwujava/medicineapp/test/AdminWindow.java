package com.smwujava.medicineapp.test;
import com.smwujava.medicineapp.admin.AdminDashboardPanel;

import javax.swing.*;
import java.awt.*;

public class AdminWindow extends JFrame {

    public AdminWindow() {
        setTitle("관리자 대시보드");
        setSize(1200, 800);
        setLocationRelativeTo(null); // 화면 가운데 정렬
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 관리자 대시보드 패널 추가
        AdminDashboardPanel dashboardPanel = new AdminDashboardPanel();
        add(dashboardPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminWindow());
    }
}
