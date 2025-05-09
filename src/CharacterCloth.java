import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;

public class CharacterCloth extends JPanel {

    public final int startX = 100;
    public final int rectY = 230;
    public final int width = 190;
    public final int heightOval = 50;
    public final int heightRect = 100;

    private final Color clothColor = Color.decode("#A7D3F2");          // 옷 색상 A7D3F2
    private final Color skinColor = Color.decode("#F8E5AA");       // 몸통 색상 (윗 타원용)

    // 생성자에서 setOpaque 처리
    public CharacterCloth() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int topOvalY = rectY - heightOval / 2;
        int bottomOvalY = rectY + heightRect - heightOval / 2;
        int endX = startX + width;

        // 1. 아래쪽 반 타원 (옷색 + 테두리)
        g2d.setColor(clothColor);
        g2d.fillOval(startX, bottomOvalY, width, heightOval);

        // 2. 사각형 (몸통)
        g2d.fillRect(startX, rectY, width, heightRect);

        // 3. 윗 타원 — 몸 색상으로 덮어주는 효과
        g2d.setColor(skinColor);
        g2d.fillOval(startX, topOvalY, width, heightOval);

        // 4. 아래쪽 테두리만 그림
        g2d.setColor(Color.decode("#81A9C6"));
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(new Arc2D.Double(startX, bottomOvalY, width, heightOval, 0, -180, Arc2D.OPEN));

        // 5. 윗 타원의 아래쪽 반원 테두리만
        g2d.draw(new Arc2D.Double(startX, topOvalY, width, heightOval, 0, -180, Arc2D.OPEN));

        // 6. 직사각형 양옆 테두리
        g2d.drawLine(startX, rectY, startX, rectY + heightRect);
        g2d.drawLine(endX, rectY, endX, rectY + heightRect);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("CharacterCloth Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.add(new CharacterCloth());
        frame.setVisible(true);
    }
}
