package com.smwujava.medicineapp.ui.icon;

import javax.swing.*;
import java.awt.*;

public class MainCharacter extends JPanel {
    public MainCharacter() {
        setLayout(null);
        setOpaque(false); // 배경 투명하게
        setPreferredSize(new Dimension(500, 600)); // 고정 크기

        // 캐릭터 중심 위치 기준 (width=190 이라고 가정)
        int bodyWidth = 190;
        int bodyX = 500 / 2 - bodyWidth / 2; // = 155
        int rectY = 150;
        int heightOval = 50;
        int topOvalY = rectY - heightOval / 2;

        CharacterBody body = new CharacterBody();
        CharacterLid lid = new CharacterLid();
        CharacterGlasses glasses = new CharacterGlasses();
        CharacterCloth cloth = new CharacterCloth();

        // 전체 동일한 좌표 영역 내에서 겹쳐지도록 설정
        body.setBounds(0, 0, 500, 600);
        lid.setBounds(0, 0, 500, 600);
        glasses.setBounds(0, 0, 500, 600);
        cloth.setBounds(0, 0, 500, 600);

        // 투명 설정
        body.setOpaque(false);
        lid.setOpaque(false);
        glasses.setOpaque(false);
        cloth.setOpaque(false);

        // Z-Order 순서대로 추가
        add(body);
        add(cloth);
        add(lid);
        add(glasses);

        setComponentZOrder(glasses, 0); // 맨 위
        setComponentZOrder(lid, 1);
        setComponentZOrder(cloth, 2);
        setComponentZOrder(body, 3);    // 맨 뒤
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("캐릭터 조립");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 700); // 넉넉한 창 크기

            // 중앙 정렬을 위한 wrapper
            JPanel wrapper = new JPanel(new GridBagLayout());
            wrapper.setBackground(Color.LIGHT_GRAY); // 확인용 배경

            MainCharacter character = new MainCharacter();
            wrapper.add(character);

            frame.setContentPane(wrapper);
            frame.setLocationRelativeTo(null); // 화면 중앙에 JFrame 위치
            frame.setVisible(true);
        });
    }
}
