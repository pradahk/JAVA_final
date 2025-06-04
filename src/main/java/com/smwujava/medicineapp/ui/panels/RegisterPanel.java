package com.smwujava.medicineapp.ui.panels;

import com.smwujava.medicineapp.model.UserRegistrationResult;
import com.smwujava.medicineapp.service.UserService;

import javax.swing.*;
import java.awt.*;

public class RegisterPanel extends JPanel {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private final UserService userService = UserService.getInstance();

    public RegisterPanel(Container parent) {
        setLayout(new GridLayout(4, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        add(new JLabel("사용자 이름:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("비밀번호:"));
        passwordField = new JPasswordField();
        add(passwordField);

        add(new JLabel()); // 빈 셀
        JButton registerBtn = new JButton("회원가입");
        add(registerBtn);

        registerBtn.addActionListener(e -> handleRegister());
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        UserRegistrationResult result = userService.validateRegistration(username, password);

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
                userService.registerUser(username, password);
                JOptionPane.showMessageDialog(this, "회원가입이 완료되었습니다!", "성공", JOptionPane.INFORMATION_MESSAGE);
                break;
            default:
                JOptionPane.showMessageDialog(this, "알 수 없는 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}



