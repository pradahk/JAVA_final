package com.smwujava.medicineapp.ui.panels;

import com.smwujava.medicineapp.ui.components.CustomButton;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class BottomNavPanel extends JPanel {
    public BottomNavPanel(Consumer<String> navigator) {
        setLayout(new GridLayout(1, 2)); // 버튼이 2개이므로 1행 2열
        setPreferredSize(new Dimension(0, 60));

        add(createNavButton("/icons/home.png", () -> navigator.accept("CALENDAR")));
        add(createNavButton("/icons/user.png", () -> navigator.accept("LIFESTYLE"))); // ✅ 여기 수정됨
    }

    private JButton createNavButton(String iconPath, Runnable action) {
        JButton btn = new CustomButton();

        java.net.URL imageUrl = getClass().getResource(iconPath);
        if (imageUrl == null) {
            System.err.println("아이콘 경로를 찾을 수 없습니다: " + iconPath);
        } else {
            ImageIcon originalIcon = new ImageIcon(imageUrl);
            Image resizedImage = originalIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(resizedImage));
        }

        btn.addActionListener(e -> action.run());
        return btn;
    }
}