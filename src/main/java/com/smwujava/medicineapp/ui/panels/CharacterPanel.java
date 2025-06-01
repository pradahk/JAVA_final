package com.smwujava.medicineapp.ui.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.smwujava.medicineapp.ui.icon.MainCharacter;

public class CharacterPanel extends JPanel {
    public CharacterPanel(CardLayout cardLayout, JPanel parentPanel) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);

        // 캐릭터 중앙 배치
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        MainCharacter character = new MainCharacter();
        character.setPreferredSize(new Dimension(500, 500));
        centerPanel.add(character);
        add(Box.createVerticalStrut(60)); // 전체 위 여백
        add(centerPanel);

        // 바로 아래 문구 배치 (적절한 여백 포함)
        JLabel caption = new JLabel("함께 만드는 건강한 습관");
        caption.setFont(new Font("SansSerif", Font.BOLD, 16));
        caption.setForeground(Color.GRAY);
        caption.setAlignmentX(Component.CENTER_ALIGNMENT);
        caption.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0)); // 캐릭터와 간격 조절
        add(caption);

        add(Box.createVerticalStrut(150));

        // 전체 패널 클릭 시 로그인으로 이동
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(parentPanel, "LOGIN");
            }
        });
    }
}
