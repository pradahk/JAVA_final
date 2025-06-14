package com.smwujava.medicineapp.ui.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton joinButton;

    public LoginPanel() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(Color.WHITE);
        container.add(Box.createVerticalStrut(20));

        // 캐릭터 이미지 삽입 (비율 유지: 200 x 259)
        ImageIcon icon = new ImageIcon(getClass().getResource("/icons/character.png"));
        Image scaledImage = icon.getImage().getScaledInstance(200, 259, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(imageLabel);
        container.add(Box.createVerticalStrut(15));

        // 앱 제목
        JLabel title = new JLabel("복용익");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(new Color(120, 140, 255));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(title);
        container.add(Box.createVerticalStrut(30));

        // 입력 필드
        usernameField = new JTextField("사용자 이름");
        usernameField.setMaximumSize(new Dimension(250, 40));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(usernameField);
        container.add(Box.createVerticalStrut(15));

        passwordField = new JPasswordField("비밀번호");
        passwordField.setMaximumSize(new Dimension(250, 40));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(passwordField);
        container.add(Box.createVerticalStrut(20));

        // 로그인 버튼
        loginButton = new JButton("로그인");
        loginButton.setPreferredSize(new Dimension(250, 45));
        loginButton.setMaximumSize(new Dimension(250, 45));
        loginButton.setBackground(new Color(160, 180, 255));
        loginButton.setForeground(Color.WHITE);
        loginButton.setOpaque(true);
        loginButton.setBorderPainted(false);
        loginButton.setContentAreaFilled(true);
        loginButton.setFocusPainted(false);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(loginButton);
        container.add(Box.createVerticalStrut(15));

        // 비밀번호 찾기 라벨
        JLabel forgotLabel = new JLabel("비밀번호를 잊으셨나요?");
        forgotLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        forgotLabel.setForeground(Color.GRAY);
        forgotLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(forgotLabel);
        container.add(Box.createVerticalStrut(15));

        // 회원가입 버튼
        joinButton = new JButton("회원 가입하기");
        joinButton.setPreferredSize(new Dimension(250, 40));
        joinButton.setMaximumSize(new Dimension(250, 40));
        joinButton.setBackground(new Color(250, 250, 250));
        joinButton.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        joinButton.setFocusPainted(false);
        joinButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
        joinButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(joinButton);

        add(container, gbc);
    }

    // 로그인 버튼 액션 등록
    public void addLoginActionListener(ActionListener listener) {
        this.loginButton.addActionListener(listener);
    }

    // 회원가입 버튼 액션 등록
    public void addRegisterActionListener(ActionListener listener) {
        this.joinButton.addActionListener(listener);
    }

    public String getUsername() {
        return usernameField.getText();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }
}