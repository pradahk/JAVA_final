package com.smwujava.medicineapp.ui.panels;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import com.smwujava.medicineapp.controller.UserPatternController;

public class UserPatternInputPanel extends JPanel {
    private JTextField breakfastStartHour, breakfastStartMinute;
    private JTextField breakfastEndHour, breakfastEndMinute;
    private JTextField lunchStartHour, lunchStartMinute;
    private JTextField lunchEndHour, lunchEndMinute;
    private JTextField dinnerStartHour, dinnerStartMinute;
    private JTextField dinnerEndHour, dinnerEndMinute;
    private JTextField sleepStartHour, sleepStartMinute;
    private JTextField sleepEndHour, sleepEndMinute;

    private final UserPatternController controller;

    public UserPatternInputPanel(int userId, JPanel mainPanel, CardLayout cardLayout) {
        this.controller = new UserPatternController(userId, this, mainPanel, cardLayout);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // 상단 제목
        JLabel title = new JLabel("📆 생활 패턴을 입력해주세요");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        add(title);

        // 설명 문구
        JLabel desc = new JLabel("24시간 형식으로 입력해주세요 (예: 07:30, 22:15)");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 13));
        desc.setForeground(Color.DARK_GRAY);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(desc);
        add(Box.createVerticalStrut(15));

        // 시간대별 입력 구간
        add(createTimeRangeSection("🍚 아침",
                breakfastStartHour = createTimeField(true), breakfastStartMinute = createTimeField(false),
                breakfastEndHour = createTimeField(true), breakfastEndMinute = createTimeField(false),
                new Color(255, 250, 240))); // 연노랑

        add(Box.createVerticalStrut(10));

        add(createTimeRangeSection("🍱 점심",
                lunchStartHour = createTimeField(true), lunchStartMinute = createTimeField(false),
                lunchEndHour = createTimeField(true), lunchEndMinute = createTimeField(false),
                new Color(240, 255, 255))); // 하늘

        add(Box.createVerticalStrut(10));

        add(createTimeRangeSection("🍜 저녁",
                dinnerStartHour = createTimeField(true), dinnerStartMinute = createTimeField(false),
                dinnerEndHour = createTimeField(true), dinnerEndMinute = createTimeField(false),
                new Color(245, 255, 250))); // 민트

        add(Box.createVerticalStrut(10));

        add(createTimeRangeSection("🌙 취침",
                sleepStartHour = createTimeField(true), sleepStartMinute = createTimeField(false),
                sleepEndHour = createTimeField(true), sleepEndMinute = createTimeField(false),
                new Color(250, 240, 255))); // 연보라

        add(Box.createVerticalStrut(20));

        // 저장 버튼
        JButton saveButton = new JButton("저장하기");
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveButton.setPreferredSize(new Dimension(100, 40));
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        saveButton.setBackground(new Color(102, 204, 153));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(e->controller.save());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false); // 배경 투명
        buttonPanel.add(saveButton);
        add(buttonPanel); // 원래 add(saveButton) 대신 이걸 추가!

    }

    private JPanel createTimeRangeSection(String labelText, JTextField startHour, JTextField startMinute,
                                          JTextField endHour, JTextField endMinute, Color bgColor) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.CENTER_ALIGNMENT);
        section.setBackground(bgColor);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        section.add(createTimeRangeRow(labelText, startHour, startMinute, endHour, endMinute));
        return section;
    }

    private JPanel createTimeRangeRow(String labelText, JTextField startHour, JTextField startMinute,
                                      JTextField endHour, JTextField endMinute) {
        JPanel row = new JPanel();
        row.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        row.setBackground(null);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        JPanel labelWrapper = new JPanel();
        labelWrapper.setLayout(new BoxLayout(labelWrapper, BoxLayout.Y_AXIS));
        labelWrapper.setOpaque(false);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelWrapper.add(Box.createVerticalGlue());
        labelWrapper.add(label);
        labelWrapper.add(Box.createVerticalGlue());

        row.add(labelWrapper);

        row.add(startHour);
        row.add(new JLabel(":"));
        row.add(startMinute);

        row.add(new JLabel(" ~~ "));

        row.add(endHour);
        row.add(new JLabel(":"));
        row.add(endMinute);

        return row;
    }

    private JTextField createTimeField(boolean isHour) {
        JTextField field = new JTextField(2);
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setPreferredSize(new Dimension(70, 30));
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new NumericRangeFilter(field, isHour ? 0 : 0, isHour ? 23 : 59));
        return field;
    }

    private static class NumericRangeFilter extends DocumentFilter {
        private final JTextField field;
        private final int minValue;
        private final int maxValue;

        public NumericRangeFilter(JTextField field, int minValue, int maxValue) {
            this.field = field;
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string != null && string.matches("\\d+")) {
                StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
                sb.insert(offset, string);
                validateAndInsert(fb, offset, 0, string, attr, sb.toString());
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text != null && text.matches("\\d*")) {
                StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
                sb.replace(offset, offset + length, text);
                validateAndInsert(fb, offset, length, text, attrs, sb.toString());
            }
        }

        private void validateAndInsert(FilterBypass fb, int offset, int length, String text, AttributeSet attrs, String candidate) throws BadLocationException {
            if (candidate.length() > 2) return;
            if (candidate.isEmpty()) {
                fb.replace(offset, length, "", attrs);
                return;
            }
            try {
                int value = Integer.parseInt(candidate);
                if (value >= minValue && value <= maxValue) {
                    fb.replace(offset, length, text, attrs);
                }
            } catch (NumberFormatException ignored) { }
        }
    }

    // Public getter methods
    public String getBreakfastStartTime() { return breakfastStartHour.getText() + ":" + breakfastStartMinute.getText(); }
    public String getBreakfastEndTime() { return breakfastEndHour.getText() + ":" + breakfastEndMinute.getText(); }
    public String getLunchStartTime() { return lunchStartHour.getText() + ":" + lunchStartMinute.getText(); }
    public String getLunchEndTime() { return lunchEndHour.getText() + ":" + lunchEndMinute.getText(); }
    public String getDinnerStartTime() { return dinnerStartHour.getText() + ":" + dinnerStartMinute.getText(); }
    public String getDinnerEndTime() { return dinnerEndHour.getText() + ":" + dinnerEndMinute.getText(); }
    public String getSleepStartTime() { return sleepStartHour.getText() + ":" + sleepStartMinute.getText(); }
    public String getSleepEndTime() { return sleepEndHour.getText() + ":" + sleepEndMinute.getText(); }
}