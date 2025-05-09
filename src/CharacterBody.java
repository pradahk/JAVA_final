import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;

public class CharacterBody extends JPanel {

    public final int startX = 100;
    public final int rectY = 150;
    public final int width = 190;
    public final int heightOval = 50;
    public final int heightRect = 200;

    private final Color faceColor = Color.decode("#F8E5AA");

    // 생성자에서 setOpaque 처리
    public CharacterBody() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 정확히 절반 겹치는 위치 계산
        int topOvalY = rectY - heightOval / 2;
        int bottomOvalY = rectY + heightRect - heightOval / 2;
        int endX = startX + width;

        // 도형 채우기
        g2d.setColor(faceColor);
        g2d.fillOval(startX, topOvalY, width, heightOval);       // 위 타원 (반쯤 덮음)
        g2d.fillRect(startX, rectY, width, heightRect);           // 사각형
        g2d.fillOval(startX, bottomOvalY, width, heightOval);    // 아래 타원 (반쯤 올라옴)

        // 테두리
        g2d.setColor(Color.decode("#CDBD8A"));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(startX, topOvalY, width, heightOval);       // 위 타원 전체
        g2d.drawLine(startX, rectY, startX, rectY + heightRect); // 좌측 선
        g2d.drawLine(endX, rectY, endX, rectY + heightRect);     // 우측 선
        g2d.draw(new Arc2D.Double(startX, bottomOvalY, width, heightOval, 0, -180, Arc2D.OPEN)); // 아래 타원 위쪽만
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("CharacterBody2");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.add(new CharacterBody());
        frame.setVisible(true);
    }
}