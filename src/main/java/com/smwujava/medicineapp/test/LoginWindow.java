package com.smwujava.medicineapp.test;

import com.smwujava.medicineapp.model.User;
import com.smwujava.medicineapp.service.UserService;
import com.smwujava.medicineapp.ui.icon.MainCharacter;
import com.smwujava.medicineapp.ui.panels.LoginPanel;

import javax.swing.*;
import java.awt.*;

public class LoginWindow extends JFrame {

    public LoginWindow() {
        super("캐릭터 포함 로그인");

        // 기본 창 설정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 상단 캐릭터 패널
        JPanel characterPanel = new JPanel();
        characterPanel.setBackground(Color.WHITE);
        characterPanel.setPreferredSize(new Dimension(400, 200));
        characterPanel.setLayout(new GridBagLayout());

        // 캐릭터 추가
        MainCharacter mainCharacter = new MainCharacter();
        characterPanel.add(mainCharacter);

        // 로그인 패널 생성
        LoginPanel loginPanel = new LoginPanel();

        // 로그인 버튼 동작
        loginPanel.addLoginActionListener(e -> {
            String id = loginPanel.getUsername();
            String pw = loginPanel.getPassword();

            UserService userService = UserService.getInstance();
            User user = userService.loginAndGetUser(id, pw, false);

            if (user != null) {
                System.out.println("✅ 로그인 성공: " + user.getUsername());
                JOptionPane.showMessageDialog(this, "로그인 성공: " + user.getUsername());
            } else {
                JOptionPane.showMessageDialog(this, "아이디 또는 비밀번호가 틀렸습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 회원가입 버튼 테스트 메시지
        loginPanel.addRegisterActionListener(e -> {
            JOptionPane.showMessageDialog(this, "회원가입 화면은 아직 구현되지 않았습니다.");
        });

        // 패널 조립
        add(characterPanel, BorderLayout.NORTH);
        add(loginPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginWindow::new);
    }
}