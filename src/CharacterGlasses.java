import javax.swing.*;
import java.awt.*;

public class CharacterGlasses extends JPanel {

    private final Color frameColor = Color.decode("#975B00");
    private final int bodyStartX;
    private final int bodyWidth;

    public CharacterGlasses(int bodyStartX, int bodyWidth) {
        this.bodyStartX = bodyStartX;
        this.bodyWidth = bodyWidth;
        setOpaque(false); // 투명 배경
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(frameColor);

        // 프레임 설정
        int frameOuterW = 100;
        int frameOuterH = 50;
        int frameThickness = 10;
        int gap = 20;
        int bridgeW = 20;

        // 중앙 정렬
        int bodyCenterX = bodyStartX + bodyWidth / 2;
        int totalWidth = 2 * frameOuterW + bridgeW + gap;
        int xLeft = bodyCenterX - totalWidth / 2;
        int xRight = xLeft + frameOuterW + bridgeW + gap;
        int y = 200; // 수직 위치 (원하면 조정 가능)

        // 왼쪽 테
        g2d.fillRect(xLeft, y, frameOuterW, frameOuterH);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(xLeft + frameThickness, y + frameThickness,
                frameOuterW - 2 * frameThickness, frameOuterH - 2 * frameThickness);

        // 오른쪽 테
        g2d.setColor(frameColor);
        g2d.fillRect(xRight, y, frameOuterW, frameOuterH);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(xRight + frameThickness, y + frameThickness,
                frameOuterW - 2 * frameThickness, frameOuterH - 2 * frameThickness);

        // 브릿지
        g2d.setColor(frameColor);
        int bridgeX = xLeft + frameOuterW;
        int bridgeY = y + frameOuterH / 3;
        g2d.fillRect(bridgeX, bridgeY, gap * 2, frameOuterH / 3);
    }
}
