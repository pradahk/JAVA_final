package com.smwujava.medicineapp.test;

import com.smwujava.medicineapp.ui.panels.RegisterPanel;

import javax.swing.*;

public class RegisterWindow extends JFrame {

    public RegisterWindow() {
        setTitle("회원가입");
        setSize(500, 450);
        setLocationRelativeTo(null); // 화면 중앙
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // RegisterPanel을 전체 창에 추가
        RegisterPanel registerPanel = new RegisterPanel(null); // mainWindow 없이 실행
        setContentPane(registerPanel);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RegisterWindow::new);
    }
}