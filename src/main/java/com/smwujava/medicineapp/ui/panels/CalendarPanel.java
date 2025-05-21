package com.smwujava.medicineapp.ui.panels;

import com.smwujava.medicineapp.ui.components.CalendarDayPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Arrays;
import java.awt.Color;


public class CalendarPanel extends JPanel {
    public CalendarPanel() {
        setLayout(new BorderLayout());

        // 상단: 월 제목
        JLabel monthLabel = new JLabel("4월", SwingConstants.CENTER);
        add(monthLabel, BorderLayout.NORTH);

        // 중앙: 요일 + 날짜 그리드
        JPanel grid = new JPanel(new GridLayout(7, 7, 5, 5));
        String[] headers = {"일", "월", "화", "수", "목", "금", "토"};
        for (String h : headers) grid.add(new JLabel(h, SwingConstants.CENTER));
        for (int i = 1; i <= 30; i++) {
            // ① 날짜 숫자
            // ② 각 날짜에 표시할 블록 색상들 (여기서는 예제로 두 가지 색을 지정)
            List<Color> blockColors = Arrays.asList(
                    new Color(135, 206, 250),
                    new Color(144, 238, 144)
            );
            // ③ 선택 여부 (true/false)
            boolean isSelected = false;

            grid.add(new CalendarDayPanel(i, blockColors, isSelected));
        }
    }
}