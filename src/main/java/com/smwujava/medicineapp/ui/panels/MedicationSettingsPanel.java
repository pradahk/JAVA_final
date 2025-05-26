package com.smwujava.medicineapp.ui.panels;

import com.smwujava.medicineapp.controller.MedicationSettingsController;

import javax.swing.*;
import java.awt.*;

public class MedicationSettingsPanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel colorPanel;
    private JTextField nameField;
    private JCheckBox[] dayCheckboxes;
    private JComboBox<String> periodBox;
    private JComboBox<String> offsetBox;
    private JComboBox<String> directionBox;
    private JLabel countLabel;
    private Color selectedColor;


    public MedicationSettingsPanel(CardLayout layout, JPanel panel) {
        this.cardLayout = layout;
        this.mainPanel = panel;

        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        JPanel container = new JPanel();
        container.setPreferredSize(new Dimension(500, 450));
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        nameField = new JTextField("");
        nameField.setMaximumSize(new Dimension(400, 30));
        nameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(Box.createVerticalStrut(20));
        container.add(nameField);

        JLabel daysLabel = new JLabel("복용 주기");
        daysLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(Box.createVerticalStrut(20));
        container.add(daysLabel);

        JPanel daysPanel = new JPanel(new GridLayout(1, 7, 5, 0));
        String[] days = {"일", "월", "화", "수", "목", "금", "토"};
        dayCheckboxes = new JCheckBox[7];
        for (int i = 0; i < days.length; i++) {
            JCheckBox cb = new JCheckBox(days[i]);
            cb.setOpaque(false);
            dayCheckboxes[i] = cb;
            daysPanel.add(cb);
        }
        daysPanel.setOpaque(false);
        container.add(daysPanel);

        container.add(Box.createVerticalStrut(20));
        JLabel timeLabel = new JLabel("복용 시간대");
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(timeLabel);

        JPanel timePanel = new JPanel();
        timePanel.setOpaque(false);
        periodBox = new JComboBox<>(new String[]{"식사", "수면"});
        offsetBox = new JComboBox<>(new String[]{"0분", "5분", "10분", "15분", "30분", "1시간"});
        directionBox = new JComboBox<>(new String[]{"전", "후"});
        timePanel.add(periodBox);
        timePanel.add(offsetBox);
        timePanel.add(directionBox);
        container.add(timePanel);

        container.add(Box.createVerticalStrut(20));
        JLabel doseLabel = new JLabel("하루 복용량");
        doseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(doseLabel);

        JPanel dosePanel = new JPanel();
        dosePanel.setOpaque(false);
        JButton minus = new JButton("-");
        countLabel = new JLabel("1");
        JButton plus = new JButton("+");
        dosePanel.add(minus);
        dosePanel.add(countLabel);
        dosePanel.add(plus);
        container.add(dosePanel);

        minus.addActionListener(e -> {
            int current = Integer.parseInt(countLabel.getText());
            if (current > 1) countLabel.setText(String.valueOf(current - 1));
        });
        plus.addActionListener(e -> {
            int current = Integer.parseInt(countLabel.getText());
            countLabel .setText(String.valueOf(current + 1));
        });

        container.add(Box.createVerticalStrut(20));
        JLabel colorLabel = new JLabel("색상 선택:");
        colorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(colorLabel);

        colorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
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


            colorBtn.addActionListener(e -> {;
                selectedColor = color;

                for (Component comp : colorPanel.getComponents()) {
                    if (comp instanceof JButton) {
                        JButton btn = (JButton) comp;
                        boolean isSelected = btn == colorBtn;
                        btn.setBorder(BorderFactory.createLineBorder(
                                isSelected ? Color.BLACK : Color.WHITE, isSelected ? 2 : 1
                        ));
                    }
                }
            });
            colorPanel.add(colorBtn);
        }
        container.add(colorPanel);

        container.add(Box.createVerticalStrut(30));
        JButton saveButton = new JButton("저장");
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(saveButton);

        saveButton.addActionListener(e -> {
            boolean saved = MedicationSettingsController.saveMedicine(
                    1,  // 임시 userId
                    nameField,
                    dayCheckboxes,
                    periodBox,
                    offsetBox,
                    directionBox,
                    countLabel,
                    selectedColor
            );
            if (saved) {
                JOptionPane.showMessageDialog(this, "약 정보가 저장되었습니다!");

                nameField.setText("");
                for (JCheckBox cb : dayCheckboxes) {
                    cb.setSelected(false);
                }
                periodBox.setSelectedIndex(0);
                offsetBox.setSelectedIndex(0);
                directionBox.setSelectedIndex(0);
                countLabel.setText("1");
                selectedColor = null;
                for (Component comp : colorPanel.getComponents()) {
                    if (comp instanceof JButton) {
                        ((JButton) comp).setBorder(BorderFactory.createLineBorder(Color.WHITE));
                    }
                }

                // 화면 전환 아직 안되어있음.
                cardLayout.show(mainPanel, "home");
            } else {
                JOptionPane.showMessageDialog(this, "저장에 실패했습니다.");
            }
        });

        add(container);
    }
}