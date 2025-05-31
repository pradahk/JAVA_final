import javax.swing.*;
import java.awt.*;

public class MainCharacter extends JPanel {
    public MainCharacter() {
        setLayout(null);
        setBackground(Color.WHITE);

        CharacterBody body = new CharacterBody();
        int topOvalY = body.rectY - body.heightOval / 2;

        CharacterLid lid = new CharacterLid(body.startX, topOvalY, body.width, body.heightOval);
        CharacterGlasses glasses = new CharacterGlasses(body.startX, body.width);
        CharacterCloth cloth = new CharacterCloth();

        // 위치 고정
        body.setBounds(0, 0, 500, 600);
        lid.setBounds(0, 0, 500, 600);
        glasses.setBounds(0, 0, 500, 600);
        cloth.setBounds(0, 0, 500, 600);

        // 위로 올릴 순서대로 add
        add(body);
        add(cloth);
        add(lid);
        add(glasses);

        // 명확하게 Z-Order 고정
        setComponentZOrder(glasses, 0); // 맨 위
        setComponentZOrder(lid, 1);
        setComponentZOrder(cloth, 2);
        setComponentZOrder(body, 3);    // 맨 뒤
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("캐릭터 조립");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);
        frame.add(new MainCharacter());
        frame.setVisible(true);
    }
}
