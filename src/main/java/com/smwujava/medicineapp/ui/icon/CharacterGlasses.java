package com.smwujava.medicineapp.ui.icon;

import javax.swing.*;
import java.awt.*;

public class CharacterGlasses extends JPanel {

    private final Color frameColor = Color.decode("#7B5315");

    // 안경 프레임 설정
    private final int frameOuterW = 115;
    private final int frameOuterH = 75;
    private final int frameThickness = 13;
    private final int gap = 15;
    private final int arc = 50;
    private final int y = 200;

    public CharacterGlasses() {
        setOpaque(false); // 투명 배경
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(frameColor);

        // 전체 너비 계산
        int totalWidth = 2 * frameOuterW + gap;
        int centerX = getWidth() / 2;

        int xLeft = centerX - totalWidth / 2;
        int xRight = xLeft + frameOuterW + gap;

        // 왼쪽 프레임
        g2d.setColor(frameColor);
        g2d.fillRoundRect(xLeft, y, frameOuterW, frameOuterH, arc, arc);
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(xLeft + frameThickness, y + frameThickness,
                frameOuterW - 2 * frameThickness, frameOuterH - 2 * frameThickness, arc, arc);

        // 오른쪽 프레임
        g2d.setColor(frameColor);
        g2d.fillRoundRect(xRight, y, frameOuterW, frameOuterH, arc, arc);
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(xRight + frameThickness, y + frameThickness,
                frameOuterW - 2 * frameThickness, frameOuterH - 2 * frameThickness, arc, arc);

        // 브릿지
        g2d.setColor(frameColor);
        int bridgeX = xLeft + frameOuterW;
        int bridgeY = y + frameOuterH / 3;
        g2d.fillRect(bridgeX, bridgeY, gap * 2 - 3, frameOuterH / 3 - 5);
    }
}