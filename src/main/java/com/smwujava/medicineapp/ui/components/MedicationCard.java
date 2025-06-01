package com.smwujava.medicineapp.ui.components;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class MedicationCard extends JPanel {
    private static final int ARC_WIDTH = 30;
    private static final int ARC_HEIGHT = 30;

    public MedicationCard(String name, String time, Color bgColor) {
        setLayout(new BorderLayout());
        setBackground(bgColor);
        setOpaque(false); // 직접 배경을 그릴 것이므로 불투명 설정 해제
        setBorder(new EmptyBorder(10, 15, 10, 15)); // 여백

        setPreferredSize(new Dimension(340, 67)); // 너비 340, 높이 67

        JLabel titleLabel = new JLabel(name);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);

        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        timeLabel.setForeground(Color.WHITE);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(timeLabel);

        add(textPanel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(getBackground());
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC_WIDTH, ARC_HEIGHT);
        g2.dispose();
        super.paintComponent(g);
    }
}