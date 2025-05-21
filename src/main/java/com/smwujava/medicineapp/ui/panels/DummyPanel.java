// DummyPanel.java
package com.smwujava.medicineapp.ui.panels;

import javax.swing.*;
import java.awt.*;

public class DummyPanel extends JPanel {  // ← 클래스에 public 추가
    public DummyPanel() {
        setBackground(Color.LIGHT_GRAY);
        add(new JLabel("더미 화면입니다")); // 또는 원하는 텍스트
    }
}