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
    private int userId = -1;  // 기본값: 로그인 전

    public void showLoginPanel() {
        cardLayout.show(contentPanel, "LOGIN");
    }

    public void showRegisterPanel() {
        cardLayout.show(contentPanel, "REGISTER");
    }

    public MainWindow() {
        super("Medicine App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. CardLayout 설정
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // 2. 로그인 패널
        final LoginPanel loginPanel = new LoginPanel(this, null);
        contentPanel.add(loginPanel, "LOGIN");

        // 로그인 버튼 리스너 등록
        loginPanel.addLoginActionListener(e -> {
            String id = loginPanel.getUsername();
            String pw = loginPanel.getPassword();

            UserService userService = UserService.getInstance();
            boolean success = userService.login(id, pw, false);

            if (success) {
                try {
                    User user = UserDao.findUserByUsername(id);
                    if (user != null) {
                        this.userId = user.getUserId();  // ✅ userId 저장
                        System.out.println("✅ 로그인 성공: " + user.getUsername());

                        // 이후 화면 구성
                        setupPanelsAfterLogin();

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

        // 3. 회원가입 패널 (로그인 전에도 필요)
        RegisterPanel registerPanel = new RegisterPanel(this);
        contentPanel.add(registerPanel, "REGISTER");

        // 4. 기본 contentPanel 배치
        add(contentPanel, BorderLayout.CENTER);

        // 5. 하단 네비게이션은 로그인 후에만 추가
        bottomNav = new BottomNavPanel(this::switchTo);

        setSize(400, 600);
        setLocationRelativeTo(null);
        setVisible(true);

        cardLayout.show(contentPanel, "LOGIN"); // 시작 화면
    }

    //  로그인 후에만 호출되도록 나머지 패널 구성 분리
    private void setupPanelsAfterLogin() {
        CalendarPanel calendarPanel = new CalendarPanel(cardLayout, contentPanel);
        //MedicationListPanel medicationListPanel = new MedicationListPanel(cardLayout, contentPanel);
        MedicationSettingsPanel medicationSettingsPanel = new MedicationSettingsPanel(userId, cardLayout, contentPanel);
        LifestylePanel lifestylePanel = new LifestylePanel();

        contentPanel.add(calendarPanel, "CALENDAR");
        //contentPanel.add(medicationListPanel, "LIST");
        contentPanel.add(medicationSettingsPanel, "SETTINGS");
        contentPanel.add(lifestylePanel, "LIFESTYLE");
    }

    private void switchTo(String name) {
        cardLayout.show(contentPanel, name);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainWindow::new);
    }
}