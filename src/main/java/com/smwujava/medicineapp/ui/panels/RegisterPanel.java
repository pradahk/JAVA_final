package com.smwujava.medicineapp.ui.panels;

import com.smwujava.medicineapp.model.UserRegistrationResult;
import com.smwujava.medicineapp.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class RegisterPanel extends JPanel {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private final UserService userService = UserService.getInstance();
    private JButton backToLoginButton;
    private JButton registerBtn;

    public RegisterPanel() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(Color.WHITE);
        container.add(Box.createVerticalStrut(20));

        // 제목
        JLabel title = new JLabel("회원 가입");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(new Color(120, 140, 255));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(title);
        container.add(Box.createVerticalStrut(30));

        // 사용자 이름 입력
        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(250, 40));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameField.setToolTipText("사용자 이름");
        container.add(usernameField);
        container.add(Box.createVerticalStrut(15));

        // 비밀번호 입력
        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(250, 40));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.setToolTipText("비밀번호");
        container.add(passwordField);
        container.add(Box.createVerticalStrut(20));

        // 회원가입 버튼
        registerBtn = new JButton("회원가입");
        registerBtn.setPreferredSize(new Dimension(250, 45));
        registerBtn.setMaximumSize(new Dimension(250, 45));
        registerBtn.setBackground(new Color(160, 180, 255));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setOpaque(true);
        registerBtn.setBorderPainted(false);
        registerBtn.setContentAreaFilled(true);
        registerBtn.setFocusPainted(false);
        registerBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(registerBtn);
        container.add(Box.createVerticalStrut(15));

        // 로그인으로 돌아가기 버튼
        backToLoginButton = new JButton("로그인 화면으로");
        backToLoginButton.setPreferredSize(new Dimension(250, 40));
        backToLoginButton.setMaximumSize(new Dimension(250, 40));
        backToLoginButton.setBackground(new Color(250, 250, 250));
        backToLoginButton.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        backToLoginButton.setFocusPainted(false);
        backToLoginButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
        backToLoginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(backToLoginButton);

        add(container, gbc);

        // 액션 연결
        registerBtn.addActionListener(e -> handleRegister());
    }

    public void addBackToLoginListener(ActionListener listener) {
        this.backToLoginButton.addActionListener(listener);
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        UserRegistrationResult result = userService.validateRegistration(username, password);

        if (result == null) {
            JOptionPane.showMessageDialog(this, "회원가입 중 DB 오류 발생", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        switch (result) {
            case DUPLICATE_USERNAME:
                JOptionPane.showMessageDialog(this, "이미 존재하는 사용자명입니다.", "회원가입 실패", JOptionPane.ERROR_MESSAGE);
                break;
            case PASSWORD_TOO_SHORT:
                JOptionPane.showMessageDialog(this, "비밀번호는 최소 7자 이상이어야 합니다.", "회원가입 실패", JOptionPane.ERROR_MESSAGE);
                break;
            case PASSWORD_NO_SPECIAL_CHAR:
                JOptionPane.showMessageDialog(this, "비밀번호에 특수문자를 포함해야 합니다.", "회원가입 실패", JOptionPane.ERROR_MESSAGE);
                break;
            case SUCCESS:
                if (userService.registerUser(username, password)) {
                    JOptionPane.showMessageDialog(this, "회원가입이 완료되었습니다!", "성공", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "회원가입 저장 중 오류 발생", "오류", JOptionPane.ERROR_MESSAGE);
                }
                break;
            default:
                JOptionPane.showMessageDialog(this, "알 수 없는 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}