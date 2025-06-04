package com.smwujava.medicineapp.test;

import com.smwujava.medicineapp.ui.panels.UserPatternInputPanel;

import javax.swing.*;
import java.awt.*;

public class UserPatternWindow extends JFrame {

    public UserPatternWindow(int userId) {
        setTitle("생활 패턴 입력 테스트");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 700);
        setLocationRelativeTo(null); // 화면 중앙에 위치

        // CardLayout 기반 메인 패널 생성
        CardLayout cardLayout = new CardLayout();
        JPanel mainPanel = new JPanel(cardLayout);

        // 패턴 입력 패널 생성 및 추가
        UserPatternInputPanel inputPanel = new UserPatternInputPanel(userId, mainPanel, cardLayout);
        mainPanel.add(inputPanel, "pattern");

        add(mainPanel);
        setVisible(true);
    }

    public static void main(String[] args) {
        // 예시 userId로 실행
        SwingUtilities.invokeLater(() -> new UserPatternWindow(1));
    }
}
