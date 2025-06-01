package com.smwujava.medicineapp.ui.icon;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;

public class CharacterLid extends JPanel {

    // 뚜껑 자체의 상대 크기
    private final int bodyWidth = 190;            // 기준이 되는 몸통 폭
    private final int width = (int) (bodyWidth * 0.75);
    private final int heightOval = 25;            // 타원 절반 높이
    private final int heightRect = 40;
    private final int bottomOffset = 190;         // 아래에서 얼마나 위로 띄울지

    private final Color faceColor = Color.decode("#F5C76C");

    public CharacterLid() {
        setOpaque(false); // 배경 투명
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int startX = getWidth() / 2 - width / 2;

        // 목표 bottomOvalY = 125에 맞춰 topY 역산
        int bottomOvalY = 125;
        int rectY = bottomOvalY - heightRect + heightOval / 2;
        int topY = rectY - heightOval / 2;
        int endX = startX + width;

        // 뚜껑 채우기
        g2d.setColor(faceColor);
        g2d.fillOval(startX, topY, width, heightOval);         // 위 타원
        g2d.fillRect(startX, rectY, width, heightRect);        // 직사각형
        g2d.fillOval(startX, bottomOvalY, width, heightOval);  // 아래 타원

        // 테두리
        g2d.setColor(Color.decode("#D4AD61"));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(startX, topY, width, heightOval);              // 위 타원 테두리
        g2d.drawLine(startX, rectY, startX, rectY + heightRect);    // 좌측 선
        g2d.drawLine(endX, rectY, endX, rectY + heightRect);        // 우측 선
        g2d.draw(new Arc2D.Double(startX, bottomOvalY, width, heightOval, 0, -180, Arc2D.OPEN)); // 아래 타원 위쪽만
    }
}
