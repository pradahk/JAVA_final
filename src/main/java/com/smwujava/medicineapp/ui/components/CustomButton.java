package com.smwujava.medicineapp.ui.components;

import javax.swing.*;
import java.awt.*;

public class CustomButton extends JButton {
    public CustomButton() {
        super();
        setFocusPainted(false);
        setBackground(new Color(239, 243, 255));
        setForeground(Color.BLACK);
        setFont(new Font("SansSerif", Font.BOLD, 14));
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    }
}