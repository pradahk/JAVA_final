package com.smwujava.medicineapp.calendar; // 또는 com.smwujava.medicineapp.ui.components;

import com.smwujava.medicineapp.ui.panels.CalendarPanel; // MedicationInfo DTO 사용
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CalendarMedicineCard extends JPanel {
    private static final int ARC_WIDTH = 20;
    private static final int ARC_HEIGHT = 20;

    private CalendarPanel.MedicationInfo medicationInfo;
    private Color currentColor;
    private final Color defaultGray = new Color(245, 245, 245);

    private JLabel nameLabel;
    private JLabel timeLabel;

    public CalendarMedicineCard(CalendarPanel.MedicationInfo medInfo) {
        this.medicationInfo = medInfo;
        updateAppearanceBasedOnState();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        // MedicationListPanel의 너비에 맞추기 위해 PreferredSize의 너비는 느슨하게 설정하거나,
        // MedicationListPanel에서 카드 추가 시 크기를 조정할 수 있도록 합니다.
        // 여기서는 높이만 고정하고 너비는 BoxLayout에 의해 조절되도록 시도합니다.
        setPreferredSize(new Dimension(100, 65)); // 너비는 listContainer에 의해 결정, 최소 높이
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 65)); // 너비는 최대한, 높이 고정
        setBorder(new EmptyBorder(10, 15, 10, 15));

        nameLabel = new JLabel(this.medicationInfo.getName());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        timeLabel = new JLabel("<html>" + this.medicationInfo.getTime().replaceAll("\n", "<br>") + "</html>");
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        updateTextColors(); // 텍스트 색상 초기 설정

        add(nameLabel);
        add(Box.createVerticalStrut(2));
        add(timeLabel);
    }

    private void updateAppearanceBasedOnState() {
        if (this.medicationInfo.isTaken()) {
            this.currentColor = this.medicationInfo.getColor();
        } else {
            this.currentColor = defaultGray;
        }
        updateTextColors();
    }

    private void updateTextColors() {
        if (nameLabel == null || timeLabel == null) return;

        if (this.medicationInfo.isTaken()) {
            // 약물 고유색 배경일 때의 텍스트 색상 (예: 흰색 또는 검은색 자동 결정)
            nameLabel.setForeground(determineForegroundColor(this.currentColor));
            timeLabel.setForeground(determineForegroundColor(this.currentColor));
        } else {
            // 기본 회색 배경일 때의 텍스트 색상
            nameLabel.setForeground(Color.BLACK);
            timeLabel.setForeground(Color.DARK_GRAY);
        }
    }

    private Color determineForegroundColor(Color backgroundColor) {
        double luminance = (0.299 * backgroundColor.getRed() + 0.587 * backgroundColor.getGreen() + 0.114 * backgroundColor.getBlue()) / 255;
        return luminance > 0.5 ? Color.BLACK : Color.WHITE;
    }

    public void setMedicationInfo(CalendarPanel.MedicationInfo medInfo) {
        this.medicationInfo = medInfo;
        updateAppearanceBasedOnState();
        nameLabel.setText(this.medicationInfo.getName());
        timeLabel.setText("<html>" + this.medicationInfo.getTime().replaceAll("\n", "<br>") + "</html>");
        repaint();
    }

    public CalendarPanel.MedicationInfo getMedicationInfo() {
        return this.medicationInfo;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(this.currentColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC_WIDTH, ARC_HEIGHT);
        g2.dispose();
    }
}