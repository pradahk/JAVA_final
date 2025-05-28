package com.smwujava.medicineapp.admin;

import com.smwujava.medicineapp.model.UserSummary;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TestUserListPanel {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("🧪 사용자 정보 스크롤 테스트");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null);

            UserListPanel userListPanel = new UserListPanel();

            // 15명 이상 넣어서 스크롤 생기도록
            List<UserSummary> sampleData = new ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                sampleData.add(new UserSummary("user" + String.format("%02d", i), i % 6 + 1, Math.random() * 100));
            }

            userListPanel.updateTable(sampleData);
            frame.add(userListPanel, BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }
}