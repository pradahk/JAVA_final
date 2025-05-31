import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;

public class CharacterLid extends JPanel {

    private final int startX;
    private final int topY;
    private final int width;
    private final int heightOval;
    private final int heightRect;

    private final Color faceColor = Color.decode("#C2CBD9");

    public CharacterLid(int bodyStartX, int bodyTopOvalY, int bodyWidth, int bodyHeightOval) {
        this.width = (int)(bodyWidth * 0.75);
        this.heightOval = bodyHeightOval/2;
        this.heightRect = 40;
        this.startX = bodyStartX + (bodyWidth - width) / 2;
        this.topY = bodyTopOvalY - heightRect - heightOval / 2 +4 ;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int rectY = topY + heightOval / 2;
        int bottomOvalY = rectY + heightRect - heightOval / 2; // ✅ 딱 붙게!
        int endX = startX + width;

        g2d.setColor(faceColor);
        g2d.fillOval(startX, topY, width, heightOval);         // 위 타원
        g2d.fillRect(startX, rectY, width, heightRect);        // 몸통
        g2d.fillOval(startX, bottomOvalY, width, heightOval); // 아래 타원 (겹침)

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(startX, topY, width, heightOval);         // 위 타원 테두리
        g2d.drawLine(startX, rectY, startX, rectY + heightRect); // 좌
        g2d.drawLine(endX, rectY, endX, rectY + heightRect);     // 우
        g2d.draw(new Arc2D.Double(startX, bottomOvalY, width, heightOval, 0, -180, Arc2D.OPEN)); // 아래 타원 위만
    }
/*
    public static void main(String[] args) {
        // CharacterBody2와 같은 기준값
        int bodyStartX = 100;
        int bodyRectY = 150;
        int bodyHeightOval = 50;
        int bodyTopOvalY = bodyRectY - bodyHeightOval / 2;
        int bodyWidth = 190;

        JFrame frame = new JFrame("CharacterLid");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);

        CharacterLid lid = new CharacterLid(bodyStartX, bodyTopOvalY, bodyWidth, bodyHeightOval);
        lid.setBounds(0, 0, 500, 500); // 크기 설정 (null layout 쓸 경우)

        JPanel panel = new JPanel(null); // 수동 배치
        panel.setBackground(Color.WHITE);
        panel.add(lid);

        frame.add(panel);
        frame.setVisible(true);
    }
 */
}



