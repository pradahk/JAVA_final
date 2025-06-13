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
        // RegisterPanel에 추가된 "로그인 화면으로" 버튼을 테스트하려면,
        // 이 버튼의 리스너를 여기서 설정해주어야 합니다.
        // 예: registerPanel.addBackToLoginListener(e -> System.out.println("Back to Login clicked!"));

        setContentPane(registerPanel);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RegisterWindow::new);
    }
}