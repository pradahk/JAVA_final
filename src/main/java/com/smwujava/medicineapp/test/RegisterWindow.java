package com.smwujava.medicineapp.test;

import com.smwujava.medicineapp.ui.panels.RegisterPanel;

import javax.swing.*;

public class RegisterWindow extends JFrame {

    public RegisterWindow() {
        setTitle("회원가입");
        setSize(500, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // RegisterPanel 생성
        RegisterPanel registerPanel = new RegisterPanel();

        // "로그인 화면으로" 버튼 클릭 테스트 리스너
        registerPanel.addBackToLoginListener(e -> {
            JOptionPane.showMessageDialog(this, "로그인 화면으로 이동합니다.");
        });

        setContentPane(registerPanel);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RegisterWindow::new);
    }
}