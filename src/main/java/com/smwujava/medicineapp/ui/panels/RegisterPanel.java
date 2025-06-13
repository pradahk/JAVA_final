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

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        formPanel.add(new JLabel("사용자 이름:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);

        formPanel.add(new JLabel("비밀번호:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        registerBtn = new JButton("회원가입");
        formPanel.add(registerBtn);

        backToLoginButton = new JButton("로그인 화면으로");
        formPanel.add(backToLoginButton);

        add(formPanel);

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