package com.smwujava.medicineapp.ui.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import com.smwujava.medicineapp.ui.panels.BottomNavPanel;

import com.smwujava.medicineapp.ui.panels.CalendarPanel;
import com.smwujava.medicineapp.ui.panels.MedicationListPanel;
import com.smwujava.medicineapp.ui.panels.MedicationSettingsPanel;
import com.smwujava.medicineapp.ui.panels.LifestylePanel;
import com.smwujava.medicineapp.ui.panels.DummyPanel;


public class MainWindow extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPanel;

    public MainWindow() {
        super("Medicine App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1) 메인 콘텐츠 (CardLayout)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.add(new CalendarPanel(), "CALENDAR");
        contentPanel.add(new MedicationListPanel(), "LIST");
        contentPanel.add(
                new MedicationSettingsPanel(cardLayout, contentPanel),
                "SETTINGS"
        );
        contentPanel.add(new LifestylePanel(), "LIFESTYLE");
        contentPanel.add(new DummyPanel(), "DUMMY");
        add(contentPanel, BorderLayout.CENTER);

        // 2) 바텀 네비게이션
        add(new BottomNavPanel(this::switchTo), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void switchTo(String name) {
        cardLayout.show(contentPanel, name);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainWindow::new);
    }
}