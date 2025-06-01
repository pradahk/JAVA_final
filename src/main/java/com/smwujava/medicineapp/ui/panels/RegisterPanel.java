package com.smwujava.medicineapp.ui.panels;

import com.smwujava.medicineapp.model.User;
import com.smwujava.medicineapp.service.UserService;

import javax.swing.*;
import java.awt.*;

public class RegisterPanel extends JPanel {

    private MainWindow mainWindow;

    public RegisterPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);

        add(Box.createVerticalStrut(30));

        JLabel titleLabel = new JLabel("회원가입");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(titleLabel);

        add(Box.createVerticalStrut(30));

        JTextField usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(250, 40));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameField.setBorder(BorderFactory.createTitledBorder("아이디"));
        add(usernameField);

        add(Box.createVerticalStrut(15));

        JPasswordField passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(250, 40));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.setBorder(BorderFactory.createTitledBorder("비밀번호"));
        add(passwordField);

        add(Box.createVerticalStrut(30));

        JButton registerButton = new JButton("회원가입 완료");
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setBackground(new Color(120, 140, 255));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setPreferredSize(new Dimension(250, 40));
        registerButton.setMaximumSize(new Dimension(250, 40));
        add(registerButton);
        registerButton.addActionListener(e -> {
            // 입력값 검증 및 회원가입 처리 생략...
            JOptionPane.showMessageDialog(this, "회원가입이 완료되었습니다.");
            mainWindow.showLoginPanel(); // 로그인 화면으로 전환

        registerButton.addActionListener(ev -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 모두 입력해주세요.");
                return;
            }

            UserService userService = UserService.getInstance();
            boolean success = userService.registerUser(username, password);

            if (success) {
                JOptionPane.showMessageDialog(this, "회원가입이 완료되었습니다. 로그인 화면으로 이동합니다.");
                mainWindow.showLoginPanel();  // 로그인 화면으로 전환
            } else {
                JOptionPane.showMessageDialog(this, "회원가입 실패: 이미 존재하는 아이디입니다.");
            }
        });
    });
}}



