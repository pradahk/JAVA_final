package com.smwujava.medicineapp.ui.components;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CalendarDayPanel extends JPanel {
    private final int day;
    private List<Color> pillColors;
    private final boolean isInMonth;
    private final JLabel dayLabel;

    public CalendarDayPanel(int day, List<Color> pillColors, boolean isInMonth) {
        this.day = day;
        this.pillColors = pillColors;
        this.isInMonth = isInMonth;

        setOpaque(true);
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 날짜 라벨
        dayLabel = new JLabel(String.valueOf(day), SwingConstants.CENTER);
        dayLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        dayLabel.setForeground(isInMonth ? Color.BLACK : Color.LIGHT_GRAY);
        add(dayLabel, BorderLayout.NORTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 색상 바 렌더링 (최대 4칸)
        if (pillColors != null && !pillColors.isEmpty()) {
            Graphics2D g2 = (Graphics2D) g;
            int barHeight = 6;
            int spacing = 4;
            int numBars = 4;

            int totalHeight = (barHeight + spacing) * numBars - spacing;
            int yStart = (getHeight() - totalHeight) / 2;

            for (int i = 0; i < numBars; i++) {
                Color barColor = (i < pillColors.size()) ? pillColors.get(i) : new Color(245, 245, 245);
                g2.setColor(barColor);
                int y = yStart + i * (barHeight + spacing);
                g2.fillRoundRect(10, y, getWidth() - 20, barHeight, 5, 5);
            }
        }
    }

    public void updatePillColors(List<Color> newColors) {
        this.pillColors = newColors;
        repaint();
    }

    public void addOnePillColor(Color color) {
        if (pillColors != null && pillColors.size() < 4) {
            pillColors.add(color);
            repaint();
        }
    }

    public void removePillColor(Color color) {
        if (pillColors != null) {
            pillColors.remove(color);
            repaint();
        }
    }

    public int getDay() {
        return day;
    }

    public boolean isInMonth() {
        return isInMonth;
    }
}