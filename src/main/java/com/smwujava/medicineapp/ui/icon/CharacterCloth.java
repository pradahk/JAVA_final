package com.smwujava.medicineapp.ui.icon;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;

public class CharacterCloth extends JPanel {

    public final int rectY = 230;
    public final int width = 190;
    public final int heightOval = 50;
    public final int heightRect = 100;

    private final Color clothColor = Color.decode("#A7D3F2");    // 옷 색상
    private final Color skinColor = Color.decode("#F8E5AA");     // 몸 색상 (윗 타원)

    public CharacterCloth() {
        setOpaque(false); // 배경 투명
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 중심 기준 startX 계산
        int startX = getWidth() / 2 - width / 2;
        int topOvalY = rectY - heightOval / 2;
        int bottomOvalY = rectY + heightRect - heightOval / 2;
        int endX = startX + width;

        // 1. 아래쪽 반 타원 (옷색 + 테두리)
        g2d.setColor(clothColor);
        g2d.fillOval(startX, bottomOvalY, width, heightOval);

        // 2. 사각형 (옷 몸통 부분)
        g2d.fillRect(startX, rectY, width, heightRect);

        // 3. 윗 타원 — 몸 색상으로 덮기
        g2d.setColor(skinColor);
        g2d.fillOval(startX, topOvalY, width, heightOval);

        // 4. 아래쪽 테두리만 그림
        g2d.setColor(Color.decode("#81A9C6"));
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(new Arc2D.Double(startX, bottomOvalY, width, heightOval, 0, -180, Arc2D.OPEN));

        // 5. 윗 타원의 아래쪽 반 테두리
        g2d.draw(new Arc2D.Double(startX, topOvalY, width, heightOval, 0, -180, Arc2D.OPEN));

        // 6. 직사각형 양쪽 선
        g2d.drawLine(startX, rectY, startX, rectY + heightRect);
        g2d.drawLine(endX, rectY, endX, rectY + heightRect);
    }
}
