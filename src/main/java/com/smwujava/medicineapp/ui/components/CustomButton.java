package com.smwujava.medicineapp.ui.components;

import javax.swing.*;
import java.awt.*;


public class CustomButton extends JButton {
    public CustomButton(String text) {
        super(text); // 버튼 텍스트 설정
        setFocusPainted(false); // 선택 시 외곽선 제거
        setBackground(new Color(239, 243, 255)); // 배경색: 콘플라워 블루
        setForeground(Color.BLACK); // 글자색: 흰색
        setFont(new Font("SansSerif", Font.BOLD, 14)); // 폰트 설정
        // 안쪽 여백: 위10px, 좌우20px, 아래10px
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    }
}

