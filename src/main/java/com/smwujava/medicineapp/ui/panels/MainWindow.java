package com.smwujava.medicineapp.ui.panels;

import javax.swing.*;
import java.awt.*;
import com.smwujava.medicineapp.service.UserService;
import com.smwujava.medicineapp.dao.UserDao;
import com.smwujava.medicineapp.model.User;


public class MainWindow extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JPanel bottomNav;

    public void showLoginPanel() {
        cardLayout.show(contentPanel, "LOGIN");
    }

    // MainWindow.java 내에 추가
    public void showRegisterPanel() {
        cardLayout.show(contentPanel, "REGISTER");
    }


    public MainWindow() {
        super("Medicine App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // CardLayout 설정
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // 1. 로그인 화면 (하단바 없음)
// 먼저 LoginPanel을 안전하게 초기화
        final LoginPanel loginPanel = new LoginPanel(this, null);  // 나중에 리스너 추가

// 로그인 화면으로 등록
        contentPanel.add(loginPanel, "LOGIN");

// 로그인 버튼에 리스너 따로 추가
        loginPanel.addLoginActionListener(e -> {
            String id = loginPanel.getUsername();
            String pw = loginPanel.getPassword();

            // UserService는 싱글턴일 가능성 높음
            UserService userService = UserService.getInstance();  // 예시
            boolean success = userService.login(id, pw, false);          // 예시: 3번째 인자 추가

            if (success) {
                try{
                User user = UserDao.findUserByUsername(id);// 로그인 성공 시 User 직접 가져오기
                    if (user != null) {
                        System.out.println("✅ 로그인 성공: " + user.getUsername());

                        cardLayout.show(contentPanel, "CALENDAR");
                        add(bottomNav, BorderLayout.SOUTH);
                        revalidate();
                        repaint();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "사용자 정보 조회 중 오류 발생");
                }
            } else {
                JOptionPane.showMessageDialog(this, "로그인 실패");
            }
        });

        // 로그인 화면
        contentPanel.add(loginPanel, "LOGIN");

        RegisterPanel registerPanel = new RegisterPanel(this);
        contentPanel.add(registerPanel, "REGISTER");


        // 2. 나머지 화면 구성
        CalendarPanel calendarPanel = new CalendarPanel(cardLayout, contentPanel);
        MedicationListPanel medicationListPanel = new MedicationListPanel(cardLayout, contentPanel);
        MedicationSettingsPanel medicationSettingsPanel = new MedicationSettingsPanel(cardLayout, contentPanel);
        LifestylePanel lifestylePanel = new LifestylePanel();

        contentPanel.add(calendarPanel, "CALENDAR");
        contentPanel.add(medicationListPanel, "LIST");
        contentPanel.add(medicationSettingsPanel, "SETTINGS");
        contentPanel.add(lifestylePanel, "LIFESTYLE");

        add(contentPanel, BorderLayout.CENTER);

        // 3. 하단 네비게이션 바 생성 (초기에는 추가 X)
        bottomNav = new BottomNavPanel(this::switchTo);

        setSize(400, 600);
        setLocationRelativeTo(null);
        setVisible(true);

        // 최초에는 로그인 화면으로 시작 (하단바 없음)

        cardLayout.show(contentPanel, "LOGIN");
    }

    private void switchTo(String name) {
        cardLayout.show(contentPanel, name);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainWindow::new);
    }
}