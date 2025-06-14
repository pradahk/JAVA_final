package com.smwujava.medicineapp;

import com.smwujava.medicineapp.admin.AdminDashboardPanel;
import com.smwujava.medicineapp.db.DBManager;
import com.smwujava.medicineapp.model.User;
import com.smwujava.medicineapp.music.BGMPlayer;
import com.smwujava.medicineapp.Scheduler.AlarmScheduler;
import com.smwujava.medicineapp.service.UserService;
import com.smwujava.medicineapp.ui.panels.*;
import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.dao.UserPatternDao;
import com.smwujava.medicineapp.ui.panels.CharacterPanel;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class MainApp {
    private static JFrame mainFrameInstance;
    private CardLayout appCardLayout;
    private JPanel mainContainerPanel;
    private BGMPlayer bgmPlayer;
    private User loggedInUser;

    public MainApp() {
        mainFrameInstance = new JFrame("나의 약 복용 캘린더");
        mainFrameInstance.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrameInstance.setLayout(new BorderLayout());

        appCardLayout = new CardLayout();
        mainContainerPanel = new JPanel(appCardLayout);

        CharacterPanel characterPanel = new CharacterPanel(appCardLayout, mainContainerPanel);
        LoginPanel loginPanel = new LoginPanel();
        RegisterPanel registerPanel = new RegisterPanel();

        mainContainerPanel.add(characterPanel, "CHARACTER");
        mainContainerPanel.add(loginPanel, "LOGIN");
        mainContainerPanel.add(registerPanel, "REGISTER");

        loginPanel.addLoginActionListener(e -> handleLogin());
        loginPanel.addRegisterActionListener(e -> showPanel("REGISTER"));
        registerPanel.addBackToLoginListener(e -> showPanel("LOGIN"));

        mainFrameInstance.add(mainContainerPanel, BorderLayout.CENTER);

        showPanel("CHARACTER");

        mainFrameInstance.setSize(new Dimension(500, 800));
        mainFrameInstance.setMinimumSize(new Dimension(450, 700));
        mainFrameInstance.setLocationRelativeTo(null);
        mainFrameInstance.setVisible(true);
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
            JOptionPane.showMessageDialog(mainFrameInstance, "아이디 또는 비밀번호가 일치하지 않습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupMainUserView() {
        JPanel mainViewPanel = new JPanel(new BorderLayout());

        CardLayout pageCardLayout = new CardLayout();
        JPanel pageContainer = new JPanel(pageCardLayout);

        int userId = loggedInUser.getUserId();

        CalendarPanel calendarPage = new CalendarPanel(pageCardLayout, pageContainer, userId);
        LifestylePanel lifestylePage = new LifestylePanel(userId, pageContainer, pageCardLayout);

        Runnable refreshCalendarAction = () -> calendarPage.refresh();
        MedicationSettingsPanel settingsPage = new MedicationSettingsPanel(userId, pageCardLayout, pageContainer, refreshCalendarAction);

        pageContainer.add(calendarPage, "CALENDAR");
        pageContainer.add(lifestylePage, "LIFESTYLE");
        pageContainer.add(settingsPage, "SETTINGS");

        Consumer<String> navigator = panelName -> pageCardLayout.show(pageContainer, panelName);
        BottomNavPanel bottomNav = new BottomNavPanel(navigator);

        mainViewPanel.add(pageContainer, BorderLayout.CENTER);
        mainViewPanel.add(bottomNav, BorderLayout.SOUTH);

        mainContainerPanel.add(mainViewPanel, "MAIN_VIEW");

        AlarmScheduler scheduler = new AlarmScheduler(
                mainFrameInstance,
                userId,
                new DosageRecordDao(),
                new UserPatternDao(),
                new MedicineDao()
        );
        scheduler.start();
    }

    private void setupAdminView() {
        AdminDashboardPanel adminPage = new AdminDashboardPanel();
        mainContainerPanel.add(adminPage, "ADMIN");
    }

    private void startBGM() {
        bgmPlayer = new BGMPlayer("/music/background_music.wav");
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

    public static JFrame getFrame() {
        return mainFrameInstance;
    }

    public static boolean isRunning() {
        return mainFrameInstance != null && mainFrameInstance.isVisible();
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
