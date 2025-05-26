package com.smwujava.medicineapp.ui.panels;

import javax.swing.*;
import java.awt.*;

public class MedicationSettingsPanel extends JPanel {
    public MedicationSettingsPanel() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        // 회색 박스 (컨테이너, 시각적으로는 안 보이게)
        JPanel container = new JPanel();
        container.setPreferredSize(new Dimension(500, 450));
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);  // 회색 박스 숨기기

        // 약 이름 입력 필드
        JTextField nameField = new JTextField("");
        nameField.setMaximumSize(new Dimension(400, 30));
        nameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(Box.createVerticalStrut(20));
        container.add(nameField);

        // 복용 주기
        JLabel daysLabel = new JLabel("복용 주기");
        daysLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(Box.createVerticalStrut(20));
        container.add(daysLabel);

        JPanel daysPanel = new JPanel(new GridLayout(1, 7, 5, 0));
        String[] days = {"일", "월", "화", "수", "목", "금", "토"};
        for (String d : days) {
            JCheckBox cb = new JCheckBox(d);
            cb.setOpaque(false);
            daysPanel.add(cb);
        }
        daysPanel.setOpaque(false);
        container.add(daysPanel);

        // 복용 시간대
        container.add(Box.createVerticalStrut(20));
        JLabel timeLabel = new JLabel("복용 시간대");
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(timeLabel);

        JPanel timePanel = new JPanel();
        timePanel.setOpaque(false);
        JComboBox<String> periodBox = new JComboBox<>(new String[]{"식사", "수면"});
        JComboBox<String> offsetBox = new JComboBox<>(new String[]{"0분", "5분", "10분", "15분", "30분", "1시간"});
        JComboBox<String> directionBox = new JComboBox<>(new String[]{"전", "후"});
        timePanel.add(periodBox);
        timePanel.add(offsetBox);
        timePanel.add(directionBox);
        container.add(timePanel);

        // 하루 복용량
        container.add(Box.createVerticalStrut(20));
        JLabel doseLabel = new JLabel("하루 복용량");
        doseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(doseLabel);

        JPanel dosePanel = new JPanel();
        dosePanel.setOpaque(false);
        JButton minus = new JButton("-");
        JLabel count = new JLabel("1");
        JButton plus = new JButton("+");
        dosePanel.add(minus);
        dosePanel.add(count);
        dosePanel.add(plus);
        container.add(dosePanel);

        // 이벤트 처리
        minus.addActionListener(e -> {
            int current = Integer.parseInt(count.getText());
            if (current > 1) count.setText(String.valueOf(current - 1));
        });
        plus.addActionListener(e -> {
            int current = Integer.parseInt(count.getText());
            count.setText(String.valueOf(current + 1));
        });

        // 색상 선택
        container.add(Box.createVerticalStrut(20));
        JLabel colorLabel = new JLabel("색상 선택:");
        colorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(colorLabel);

        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        colorPanel.setOpaque(false);
        Color[] colors = {
                new Color(153, 153, 255),
                new Color(204, 255, 153),
                new Color(255, 204, 204),
                new Color(255, 255, 153),
                new Color(204, 204, 255),
                new Color(224, 224, 224)
        };
        for (Color color : colors) {
            JButton colorBtn = new JButton();
            colorBtn.setBackground(color);
            colorBtn.setPreferredSize(new Dimension(30, 30));
            colorBtn.setBorder(BorderFactory.createLineBorder(Color.WHITE));
            colorPanel.add(colorBtn);
        }
        container.add(colorPanel);

        // 최종 UI 조립
        add(container);
    }
}
