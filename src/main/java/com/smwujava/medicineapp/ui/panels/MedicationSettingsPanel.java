package com.smwujava.medicineapp.ui.panels;

import com.smwujava.medicineapp.controller.MedicationSettingsController;

import javax.swing.*;
import java.awt.*;

public class MedicationSettingsPanel extends JPanel {
    private final int userId;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final Runnable onSaveCallback;

    private JTextField nameField;
    private JCheckBox[] dayCheckboxes;
    private JComboBox<String> periodBox;
    private JComboBox<String> offsetBox;
    private JComboBox<String> directionBox;
    private JLabel countLabel;
    private Color selectedColor;
    private JPanel colorPanel;

    public MedicationSettingsPanel(int userId, CardLayout layout, JPanel panel, Runnable onSaveCallback) {
        this.userId = userId;
        this.cardLayout = layout;
        this.mainPanel = panel;
        this.onSaveCallback = onSaveCallback;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setOpaque(false);

        nameField = new JTextField();
        dayCheckboxes = new JCheckBox[7];

        formContainer.add(createLabelAndField("약 이름", nameField));

        JPanel daysPanel = new JPanel(new GridLayout(1, 7, 5, 0));
        daysPanel.setOpaque(false);
        String[] days = {"일", "월", "화", "수", "목", "금", "토"};
        for (int i = 0; i < days.length; i++) {
            dayCheckboxes[i] = new JCheckBox(days[i]);
            dayCheckboxes[i].setOpaque(false);
            daysPanel.add(dayCheckboxes[i]);
        }
        formContainer.add(createLabelAndComponent("복용 주기", daysPanel));

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timePanel.setOpaque(false);
        periodBox = new JComboBox<>(new String[]{"아침 식사", "점심 식사", "저녁 식사", "취침"});
        offsetBox = new JComboBox<>(new String[]{"0분", "5분", "10분", "15분", "30분", "1시간"});
        directionBox = new JComboBox<>(new String[]{"전", "후", "정각"});
        timePanel.add(periodBox);
        timePanel.add(offsetBox);
        timePanel.add(directionBox);
        formContainer.add(createLabelAndComponent("복용 시간대", timePanel));

        JPanel dosePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dosePanel.setOpaque(false);
        JButton minus = new JButton("-");
        countLabel = new JLabel("1");
        JButton plus = new JButton("+");
        minus.addActionListener(e -> { int c = Integer.parseInt(countLabel.getText()); if (c > 1) countLabel.setText(String.valueOf(c - 1)); });
        plus.addActionListener(e -> countLabel.setText(String.valueOf(Integer.parseInt(countLabel.getText()) + 1)));
        dosePanel.add(minus);
        dosePanel.add(countLabel);
        dosePanel.add(plus);
        formContainer.add(createLabelAndComponent("하루 복용량", dosePanel));

        colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        colorPanel.setOpaque(false);
        Color[] colors = {new Color(153, 153, 255), new Color(204, 255, 153), new Color(255, 204, 204), new Color(255, 255, 153), new Color(204, 204, 255), new Color(224, 224, 224)};
        for (Color color : colors) {
            JButton colorBtn = new JButton();
            colorBtn.setBackground(color);
            colorBtn.setPreferredSize(new Dimension(30, 30));
            colorBtn.setBorder(BorderFactory.createLineBorder(Color.WHITE));
            colorBtn.addActionListener(e -> selectColor(colorBtn));
            colorPanel.add(colorBtn);
        }
        formContainer.add(createLabelAndComponent("색상 선택", colorPanel));

        JButton saveButton = new JButton("저장");
        JButton cancelButton = new JButton("취소");

        saveButton.addActionListener(e -> handleSave());
        cancelButton.addActionListener(e -> cardLayout.show(mainPanel, "CALENDAR"));

        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonContainer.setOpaque(false);
        buttonContainer.add(cancelButton);
        buttonContainer.add(saveButton);
        formContainer.add(Box.createVerticalStrut(20));
        formContainer.add(buttonContainer);

        add(new JScrollPane(formContainer));
    }

    private JPanel createLabelAndField(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        panel.add(field);
        panel.add(Box.createVerticalStrut(10));
        return panel;
    }

    private JPanel createLabelAndComponent(String labelText, JComponent component) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        panel.add(component);
        panel.add(Box.createVerticalStrut(10));
        return panel;
    }

    private void selectColor(JButton selectedBtn) {
        selectedColor = selectedBtn.getBackground();
        for (Component comp : colorPanel.getComponents()) {
            if (comp instanceof JButton) {
                ((JButton) comp).setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
            }
        }
        selectedBtn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
    }

    private void handleSave() {
        boolean saved = MedicationSettingsController.saveMedicine(
                userId, nameField, dayCheckboxes, periodBox, offsetBox, directionBox, countLabel, selectedColor, onSaveCallback
        );
        if (saved) {
            JOptionPane.showMessageDialog(this, "약 정보가 저장되었습니다!");
            clearFields();
            cardLayout.show(mainPanel, "CALENDAR");
        }
    }

    private void clearFields() {
        nameField.setText("");
        for (JCheckBox cb : dayCheckboxes) if(cb != null) cb.setSelected(false);
        if(periodBox != null) periodBox.setSelectedIndex(0);
        if(offsetBox != null) offsetBox.setSelectedIndex(0);
        if(directionBox != null) directionBox.setSelectedIndex(0);
        if(countLabel != null) countLabel.setText("1");
        selectedColor = null;
        if(colorPanel != null) {
            for (Component comp : colorPanel.getComponents()) {
                if (comp instanceof JButton) ((JButton) comp).setBorder(BorderFactory.createLineBorder(Color.WHITE));
            }
        }
    }
}