package com.smwujava.medicineapp.ui.components;

import javax.swing.*;
import java.awt.*;

public class RoundedTextField extends JTextField {
    public RoundedTextField(int columns) {
        super(columns); // 열 개수 기준으로 크기 설정

        // 테두리: 연한 회색, 둥글게 (1픽셀)
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10) // 내부 여백
        ));

        // 폰트 설정
        setFont(new Font("SansSerif", Font.PLAIN, 14));
    }
}

