package com.smwujava.medicineapp.test;

import com.smwujava.medicineapp.ui.panels.CharacterPanel;
import com.smwujava.medicineapp.ui.panels.LoginPanel;

import javax.swing.*;
import java.awt.*;

public class CharacterStartWindow extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public CharacterStartWindow() {
        setTitle("복용익 시작 화면");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 카드 레이아웃 설정
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 1. 캐릭터 시작 화면 (필요한 인자 전달)
        CharacterPanel characterScreen = new CharacterPanel(cardLayout, mainPanel);
        mainPanel.add(characterScreen, "CHARACTER");

        // 2. 로그인 화면
        LoginPanel loginPanel = new LoginPanel();
        loginPanel.addLoginActionListener(e -> {
            String id = loginPanel.getUsername();
            String pw = loginPanel.getPassword();
            JOptionPane.showMessageDialog(this, "로그인 시도: " + id);
        });
        mainPanel.add(loginPanel, "LOGIN");

        // 프레임에 패널 추가 및 기본 화면 표시
        add(mainPanel);
        cardLayout.show(mainPanel, "CHARACTER");

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CharacterStartWindow::new);
    }
}