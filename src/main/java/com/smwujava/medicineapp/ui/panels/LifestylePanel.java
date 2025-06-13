package com.smwujava.medicineapp.ui.panels;

import javax.swing.*;
import java.awt.*;

public class LifestylePanel extends JPanel {
    private final int userId;
    private final JPanel mainPanel;
    private final CardLayout cardLayout;

    public LifestylePanel(int userId, JPanel mainPanel, CardLayout cardLayout) {
        this.userId = userId;
        this.mainPanel = mainPanel;
        this.cardLayout = cardLayout;

        setBackground(new Color(250, 250, 250));
        setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel nameLabel = new JLabel("엄청난 감자");
        JLabel birthLabel = new JLabel("2005. 04. 01");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        birthLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(Box.createVerticalStrut(20));
        content.add(nameLabel);
        content.add(birthLabel);
        content.add(Box.createVerticalStrut(30));

        JPanel mealBox = createRoundedBox();
        mealBox.setLayout(new GridLayout(3, 2, 10, 10));
        mealBox.add(new JLabel("● 아침"));
        mealBox.add(new JLabel("07:30 ~~ 08:00"));
        mealBox.add(new JLabel("● 점심"));
        mealBox.add(new JLabel("12:00 ~~ 13:00"));
        mealBox.add(new JLabel("○ 저녁"));
        mealBox.add(new JLabel(" "));

        content.add(mealBox);
        content.add(Box.createVerticalStrut(20));

        JPanel sleepBox = createRoundedBox();
        sleepBox.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        sleepBox.add(new JLabel("🌙"));
        sleepBox.add(new JLabel("23:00 ~~ 07:00"));
        content.add(sleepBox);

        content.add(Box.createVerticalStrut(20));

        JButton addButton = new JButton("+");
        addButton.setFont(new Font("SansSerif", Font.BOLD, 18));
        addButton.setBackground(new Color(102, 204, 153));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.addActionListener(e -> {
            UserPatternInputPanel inputPanel = new UserPatternInputPanel(userId, mainPanel, cardLayout);
            mainPanel.add(inputPanel, "INPUT");
            cardLayout.show(mainPanel, "INPUT");
        });


        content.add(addButton);
        add(content, BorderLayout.CENTER);
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