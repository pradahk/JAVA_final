package com.smwujava.medicineapp.ui.panels;

import com.smwujava.medicineapp.ui.components.CustomButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;  // Navigator를 위해 Consumer import

public class BottomNavPanel extends JPanel {
    public BottomNavPanel(Consumer<String> navigator) {
        setLayout(new GridLayout(1, 3));
        setPreferredSize(new Dimension(0, 60));

        add(createNavButton("/icons/home.png", () -> navigator.accept("CALENDAR")));
        add(createNavButton("/icons/list.png", () -> navigator.accept("LIST")));
        add(createNavButton("/icons/user.png", () -> navigator.accept("DUMMY")));
    }

    private JButton createNavButton(String iconPath, Runnable action) {
        JButton btn = new CustomButton();
        btn.setIcon(new ImageIcon(getClass().getResource(iconPath)));
        btn.addActionListener(e -> action.run());
        return btn;
    }
}