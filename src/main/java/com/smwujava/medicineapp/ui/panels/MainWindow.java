package com.smwujava.medicineapp.ui.panels;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JPanel bottomNav;

    public MainWindow() {
        super("Medicine App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // 로그인 화면
        LoginPanel loginPanel = new LoginPanel(e -> {
            cardLayout.show(contentPanel, "CALENDAR");
            add(bottomNav, BorderLayout.SOUTH);
            revalidate();
            repaint();
        });
        contentPanel.add(loginPanel, "LOGIN");

        // 캐릭터 첫 화면 (마우스 클릭 시 로그인으로)
        CharacterPanel characterPanel = new CharacterPanel(cardLayout, contentPanel);
        contentPanel.add(characterPanel, "CHARACTER");

        // 나머지 화면 구성
        CalendarPanel calendarPanel = new CalendarPanel(cardLayout, contentPanel);
        MedicationListPanel medicationListPanel = new MedicationListPanel(cardLayout, contentPanel);
        MedicationSettingsPanel medicationSettingsPanel = new MedicationSettingsPanel(cardLayout, contentPanel);
        LifestylePanel lifestylePanel = new LifestylePanel();
        UserPatternInputPanel inputPanel = new UserPatternInputPanel();
        inputPanel.setOnSaveCallback(() -> cardLayout.show(contentPanel, "LIFESTYLE"));

        contentPanel.add(calendarPanel, "CALENDAR");
        contentPanel.add(medicationListPanel, "LIST");
        contentPanel.add(medicationSettingsPanel, "SETTINGS");
        contentPanel.add(lifestylePanel, "LIFESTYLE");
        contentPanel.add(inputPanel, "INPUT");

        add(contentPanel, BorderLayout.CENTER);
        bottomNav = new BottomNavPanel(this::switchTo);

        setSize(400, 600);
        setLocationRelativeTo(null);
        setVisible(true);

        // 최초 화면은 캐릭터 인트로
        cardLayout.show(contentPanel, "CHARACTER");
    }

    private void switchTo(String name) {
        cardLayout.show(contentPanel, name);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainWindow::new);
    }
}