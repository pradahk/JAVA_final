package com.smwujava.medicineapp;

import com.smwujava.medicineapp.db.DBManager;
import com.smwujava.medicineapp.model.User;
import com.smwujava.medicineapp.music.BGMPlayer;
import com.smwujava.medicineapp.service.UserService;
import com.smwujava.medicineapp.ui.panels.*;
import com.smwujava.medicineapp.admin.AdminDashboardPanel;
import com.smwujava.medicineapp.Scheduler.AlarmScheduler; // 누락된 import 추가
import com.smwujava.medicineapp.dao.DosageRecordDao; // 누락된 import 추가
import com.smwujava.medicineapp.dao.MedicineDao; // 누락된 import 추가
import com.smwujava.medicineapp.dao.UserPatternDao; // 누락된 import 추가

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class MainApp {
    private JFrame mainFrame;
    private CardLayout appCardLayout;
    private JPanel mainContainerPanel;
    private BGMPlayer bgmPlayer;
    private User loggedInUser;

    public MainApp() {
        mainFrame = new JFrame("나의 약 복용 캘린더");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());

        appCardLayout = new CardLayout();
        mainContainerPanel = new JPanel(appCardLayout);

        LoginPanel loginPanel = new LoginPanel();
        RegisterPanel registerPanel = new RegisterPanel();

        mainContainerPanel.add(loginPanel, "LOGIN");
        mainContainerPanel.add(registerPanel, "REGISTER");

        loginPanel.addLoginActionListener(e -> handleLogin());
        loginPanel.addRegisterActionListener(e -> showPanel("REGISTER"));
        registerPanel.addBackToLoginListener(e -> showPanel("LOGIN"));

        mainFrame.add(mainContainerPanel, BorderLayout.CENTER);

        showPanel("LOGIN");

        mainFrame.setSize(new Dimension(500, 800));
        mainFrame.setMinimumSize(new Dimension(450, 700));
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    private void handleLogin() {
        LoginPanel loginPanel = findLoginPanel();
        if (loginPanel == null) return;

        UserService userService = UserService.getInstance();
        User user = userService.loginAndGetUser(loginPanel.getUsername(), loginPanel.getPassword(), false);

        if (user != null) {
            this.loggedInUser = user;
            startBGM();
            if (user.isAdmin()) {
                setupAdminView();
                showPanel("ADMIN");
            } else {
                setupMainUserView();
                showPanel("MAIN_VIEW");
            }
        } else {
            JOptionPane.showMessageDialog(mainFrame, "아이디 또는 비밀번호가 일치하지 않습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupMainUserView() {
        JPanel mainViewPanel = new JPanel(new BorderLayout());

        CardLayout pageCardLayout = new CardLayout();
        JPanel pageContainer = new JPanel(pageCardLayout);

        int userId = loggedInUser.getUserId();

        CalendarPanel calendarPage = new CalendarPanel(pageCardLayout, pageContainer);
        LifestylePanel lifestylePage = new LifestylePanel(userId, pageContainer, pageCardLayout);
        MedicationSettingsPanel settingsPage = new MedicationSettingsPanel(userId, pageCardLayout, pageContainer);

        pageContainer.add(calendarPage, "CALENDAR");
        pageContainer.add(lifestylePage, "LIFESTYLE");
        pageContainer.add(settingsPage, "SETTINGS");

        // --- 여기 부분을 수정했습니다 ---
        Consumer<String> navigator = panelName -> pageCardLayout.show(pageContainer, panelName);
        BottomNavPanel bottomNav = new BottomNavPanel(navigator);

        mainViewPanel.add(pageContainer, BorderLayout.CENTER);
        mainViewPanel.add(bottomNav, BorderLayout.SOUTH);

        mainContainerPanel.add(mainViewPanel, "MAIN_VIEW");
    }

    private void setupAdminView() {
        AdminDashboardPanel adminPage = new AdminDashboardPanel();
        mainContainerPanel.add(adminPage, "ADMIN");
    }

    private void startBGM() {
        bgmPlayer = new BGMPlayer("/music/bgm.wav");
        bgmPlayer.start();
    }

    private void showPanel(String panelName) {
        appCardLayout.show(mainContainerPanel, panelName);
    }

    private LoginPanel findLoginPanel() {
        for(Component comp : mainContainerPanel.getComponents()) {
            if (comp instanceof LoginPanel) {
                return (LoginPanel) comp;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        try {
            DBManager.initializeDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "데이터베이스 초기화 실패.", "실행 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SwingUtilities.invokeLater(MainApp::new);
    }
}