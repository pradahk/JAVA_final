package com.smwujava.medicineapp.ui.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainWindow extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public MainWindow() {
        setTitle("Dementia App");
        setSize(800, 600); // 💡 더 넓은 화면 비율
        setLocationRelativeTo(null); // 화면 중앙 정렬
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 🔄 CardLayout 설정
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 🧩 화면 패널 등록
        mainPanel.add(new CalendarPanel(), "home");
        mainPanel.add(new DummyPanel(), "user");

        add(mainPanel, BorderLayout.CENTER);

        // ⬇️ 하단 네비게이션 바
        BottomNavPanel bottomNav = new BottomNavPanel(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand(); // "home", "user"
                cardLayout.show(mainPanel, cmd);
            }
        });

        add(bottomNav, BorderLayout.SOUTH);
        setVisible(true);
    }
}
