package com.smwujava.medicineapp.ui.components;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {
    private final int radius;

    public RoundedPanel(int radius) {
        this.radius = radius;
        setOpaque(false); // 배경 투명화로 둥근 모양 제대로 보이게
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(getBackground());
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2.dispose();
    }
}
