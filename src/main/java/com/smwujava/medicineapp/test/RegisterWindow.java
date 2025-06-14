package com.smwujava.medicineapp.test;

import com.smwujava.medicineapp.ui.panels.RegisterPanel;

import javax.swing.*;

public class RegisterWindow extends JFrame {

    public RegisterWindow() {
        setTitle("회원가입");
        setSize(500, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        RegisterPanel registerPanel = new RegisterPanel();

        registerPanel.addBackToLoginListener(e -> {
            JOptionPane.showMessageDialog(this, "로그인 화면으로 이동합니다.");
        });

        setContentPane(registerPanel);
        revalidate(); // ✅ 새로고침
        repaint();    // ✅ 강제 리렌더링
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RegisterWindow::new);
    }
}