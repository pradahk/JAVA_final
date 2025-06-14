package com.smwujava.medicineapp.ui.panels;

import com.smwujava.medicineapp.controller.LifestyleController;
import com.smwujava.medicineapp.model.UserPattern;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class LifestylePanel extends JPanel {
    private final int userId;
    private final JPanel mainPanel;
    private final CardLayout cardLayout;
    private final LifestyleController controller;

    private JLabel nameLabel;
    private JLabel breakfastTimeLabel, lunchTimeLabel, dinnerTimeLabel, sleepTimeLabel;

    public LifestylePanel(int userId, JPanel mainPanel, CardLayout cardLayout) {
        this.userId = userId;
        this.mainPanel = mainPanel;
        this.cardLayout = cardLayout;
        this.controller = new LifestyleController(userId, this);

        setBackground(new Color(250, 250, 250));
        setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        nameLabel = new JLabel("사용자 로딩 중...");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 생년월일 레이블은 User 모델에 해당 필드가 없으므로 제거합니다.

        content.add(Box.createVerticalStrut(20));
        content.add(nameLabel);
        content.add(Box.createVerticalStrut(30));

        JPanel mealBox = createRoundedBox();
        mealBox.setLayout(new GridLayout(3, 2, 10, 10));
        breakfastTimeLabel = new JLabel("정보 없음");
        lunchTimeLabel = new JLabel("정보 없음");
        dinnerTimeLabel = new JLabel("정보 없음");
        mealBox.add(new JLabel("● 아침"));
        mealBox.add(breakfastTimeLabel);
        mealBox.add(new JLabel("● 점심"));
        mealBox.add(lunchTimeLabel);
        mealBox.add(new JLabel("● 저녁"));
        mealBox.add(dinnerTimeLabel);

        content.add(mealBox);
        content.add(Box.createVerticalStrut(20));

        JPanel sleepBox = createRoundedBox();
        sleepBox.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        sleepTimeLabel = new JLabel("정보 없음");
        sleepBox.add(new JLabel("🌙"));
        sleepBox.add(sleepTimeLabel);
        content.add(sleepBox);

        content.add(Box.createVerticalStrut(20));

        JButton editButton = new JButton("생활 패턴 수정하기");
        editButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        editButton.setBackground(new Color(102, 204, 153));
        editButton.setForeground(Color.WHITE);
        editButton.setFocusPainted(false);
        editButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        editButton.addActionListener(e -> {
            UserPatternInputPanel inputPanel = new UserPatternInputPanel(userId, mainPanel, cardLayout);
            mainPanel.add(inputPanel, "PATTERN_INPUT");
            cardLayout.show(mainPanel, "PATTERN_INPUT");
        });

        content.add(editButton);
        add(content, BorderLayout.CENTER);

        // 이 패널이 화면에 보일 때마다 데이터를 새로고침하도록 리스너 추가
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                controller.loadData();
            }
        });
    }

    public void updateDisplay(String username, UserPattern pattern) {
        nameLabel.setText(username);

        breakfastTimeLabel.setText(formatTimeRange(pattern.getBreakfastStartTime(), pattern.getBreakfastEndTime()));
        lunchTimeLabel.setText(formatTimeRange(pattern.getLunchStartTime(), pattern.getLunchEndTime()));
        dinnerTimeLabel.setText(formatTimeRange(pattern.getDinnerStartTime(), pattern.getDinnerEndTime()));
        sleepTimeLabel.setText(formatTimeRange(pattern.getSleepStartTime(), pattern.getSleepEndTime()));
    }

    private String formatTimeRange(String start, String end) {
        if (start == null || end == null || start.trim().equals(":") || end.trim().equals(":")) {
            return "설정되지 않음";
        }
        return start + " ~~ " + end;
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "오류", JOptionPane.ERROR_MESSAGE);
    }

    private JPanel createRoundedBox() {
        JPanel box = new JPanel();
        box.setBackground(new Color(245, 245, 245));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15),
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true)
        ));
        return box;
    }
}