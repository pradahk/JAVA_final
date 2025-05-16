package com.smwujava.medicineapp.ui.panels;

import com.smwujava.medicineapp.ui.components.CustomButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class BottomNavPanel extends JPanel {

    public BottomNavPanel(ActionListener listener) {
        setLayout(new GridLayout(1, 3)); // 버튼 3개 가로 정렬
        setBackground(new Color(230, 230, 250)); // 연보라 배경

        // 홈 버튼
        CustomButton homeBtn = new CustomButton("홈");
        homeBtn.setActionCommand("home");
        homeBtn.addActionListener(listener);

        // 복약 버튼
        CustomButton userBtn = new CustomButton("사용자");
        userBtn.setActionCommand("user");
        userBtn.addActionListener(listener);

        add(homeBtn);
        add(userBtn);
    }
}
