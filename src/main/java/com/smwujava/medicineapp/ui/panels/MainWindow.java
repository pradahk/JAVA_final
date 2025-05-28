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

        // CardLayout 설정
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // 1. 로그인 화면 (하단바 없음)
        LoginPanel loginPanel = new LoginPanel(e -> {
            cardLayout.show(contentPanel, "CALENDAR");
            add(bottomNav, BorderLayout.SOUTH);   // 하단바를 이 시점에 추가
            revalidate();                         // 레이아웃 갱신
            repaint();
        });
        contentPanel.add(loginPanel, "LOGIN");

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