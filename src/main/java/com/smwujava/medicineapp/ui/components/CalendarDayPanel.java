package com.smwujava.medicineapp.ui.components;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CalendarDayPanel extends JPanel {
    private JLabel dayLabel;
    private JPanel colorBarPanel;

    public CalendarDayPanel(int day, List<Color> pillColors, boolean isInMonth) {
        setLayout(new BorderLayout());
        setOpaque(false);

        dayLabel = new JLabel(day > 0 ? String.valueOf(day) : "");
        dayLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dayLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        dayLabel.setForeground(isInMonth ? Color.BLACK : new Color(220, 220, 220));

        colorBarPanel = new JPanel();
        colorBarPanel.setLayout(new GridLayout(pillColors.size(), 1, 0, 2));
        colorBarPanel.setOpaque(false);

        for (Color color : pillColors) {
            JPanel colorBlock = new JPanel();
            colorBlock.setBackground(color);
            colorBlock.setPreferredSize(new Dimension(24, 5));
            colorBarPanel.add(colorBlock);
        }

        add(colorBarPanel, BorderLayout.NORTH);
        add(dayLabel, BorderLayout.CENTER);

        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
    }
}