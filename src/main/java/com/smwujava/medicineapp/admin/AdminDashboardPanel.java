package com.smwujava.medicineapp.admin;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import com.smwujava.medicineapp.admin.AverageChartPanel;

public class AdminDashboardPanel extends JPanel {

    public AdminDashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 상단 영역
        JPanel topPanel = new JPanel(new GridLayout(1, 2));
        topPanel.setPreferredSize(new Dimension(1000, 300));
        topPanel.setBackground(Color.LIGHT_GRAY);
        topPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));

        JPanel userInfoPanel = new UserListPanel();
        JPanel versionPanel = new VersionPanel();  // 분리된 버전 패널 사용

        topPanel.add(userInfoPanel);
        topPanel.add(versionPanel);

        // 하단 영역
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));

        JPanel chartPanel = new JPanel();
        chartPanel.setLayout(new BoxLayout(chartPanel, BoxLayout.Y_AXIS));
        chartPanel.setPreferredSize(new Dimension(1000, 400));
        chartPanel.setBackground(Color.WHITE);
        //chartPanel.setBorder(BorderFactory.createTitledBorder("시간대별 복용 성공률 차트"));

        // 차트 생성 및 추가
        AverageChartPanel avgChart = new AverageChartPanel();
        avgChart.setAlignmentX(Component.CENTER_ALIGNMENT);
        chartPanel.add(avgChart);

        // 성공률 정보 추출
        Map<String, Double> rates = avgChart.getSuccessRatesByTimeSlot();
        String bestTimeSlot = null;
        double bestRate = -1;
        for (var entry : rates.entrySet()) {
            if (entry.getValue() > bestRate) {
                bestRate = entry.getValue();
                bestTimeSlot = entry.getKey();
            }
        }
        double overallAvg = rates.values().stream().mapToDouble(Double::doubleValue).average().orElse(0);

        // 설명 패널 추가
        ChartDescriptionPanel descPanel = new ChartDescriptionPanel(bestTimeSlot, overallAvg);
        chartPanel.add(descPanel);
        bottomPanel.add(chartPanel, BorderLayout.CENTER);

        // 전체 구성
        add(topPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.CENTER);
    }
}