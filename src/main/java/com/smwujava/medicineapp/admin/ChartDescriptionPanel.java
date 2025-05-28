package com.smwujava.medicineapp.admin;

import javax.swing.*;
import java.awt.*;

public class ChartDescriptionPanel extends JPanel {

    public ChartDescriptionPanel(String bestTimeSlot, double overallAverageRate) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);

        // 설명 1: 차트 안내
        JLabel baseLabel = new JLabel("※ 이 차트는 최근 7일 동안 시간대별 평균 복약 성공률을 나타냅니다.");
        baseLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        baseLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 설명 2: 성공률 높은 시간대
        JLabel bestSlotLabel = new JLabel("▶ 복용 성공률이 가장 높은 시간대: " + bestTimeSlot);
        bestSlotLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        bestSlotLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 설명 3: 전체 평균
        JLabel avgRateLabel = new JLabel("▶ 전체 평균 복용 성공률: " + String.format("%.1f", overallAverageRate) + "%");
        avgRateLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        avgRateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 여백 및 조립
        add(Box.createVerticalStrut(8));
        add(baseLabel);
        add(Box.createVerticalStrut(4));
        add(bestSlotLabel);
        add(Box.createVerticalStrut(2));
        add(avgRateLabel);
    }
}