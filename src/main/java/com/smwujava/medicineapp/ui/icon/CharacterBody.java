package com.smwujava.medicineapp.ui.icon;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;

public class CharacterBody extends JPanel {

    // 캐릭터의 크기 정보 (중앙 정렬 계산에 사용됨)
    public final int rectY = 150;
    public final int width = 190;
    public final int heightOval = 50;
    public final int heightRect = 200;

    private final Color faceColor = Color.decode("#F8E5AA");

    public CharacterBody() {
        setOpaque(false); // 배경 투명
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 중앙 정렬 기준 좌표 계산
        int startX = getWidth() / 2 - width / 2;
        int topOvalY = rectY - heightOval / 2;
        int bottomOvalY = rectY + heightRect - heightOval / 2;
        int endX = startX + width;

        // 몸체 채우기
        g2d.setColor(faceColor);
        g2d.fillOval(startX, topOvalY, width, heightOval);       // 위 타원
        g2d.fillRect(startX, rectY, width, heightRect);           // 사각형
        g2d.fillOval(startX, bottomOvalY, width, heightOval);    // 아래 타원

        // 테두리
        g2d.setColor(Color.decode("#CDBD8A"));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(startX, topOvalY, width, heightOval);       // 위 타원
        g2d.drawLine(startX, rectY, startX, rectY + heightRect); // 왼쪽 선
        g2d.drawLine(endX, rectY, endX, rectY + heightRect);     // 오른쪽 선
        g2d.draw(new Arc2D.Double(startX, bottomOvalY, width, heightOval, 0, -180, Arc2D.OPEN)); // 아래 타원 위쪽 절반
    }

/*
    public static void main(String[] args) {
        JFrame frame = new JFrame("CharacterBody2");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(390, 500);
        frame.add(new CharacterBody());
        frame.setVisible(true);
    }

 */
}