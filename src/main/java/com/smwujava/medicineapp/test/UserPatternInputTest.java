package com.smwujava.medicineapp.test;

import javax.swing.JFrame;
import com.smwujava.medicineapp.ui.panels.UserPatternInputPanel;

public class UserPatternInputTest {
    public static void main(String[] args) {
        JFrame frame = new JFrame("생활 패턴 입력");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 450);
        frame.setLocationRelativeTo(null); // 화면 중앙

        UserPatternInputPanel panel = new UserPatternInputPanel();
        frame.add(panel);

        frame.setVisible(true);
    }
}
