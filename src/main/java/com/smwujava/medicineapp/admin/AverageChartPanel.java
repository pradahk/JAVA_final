package com.smwujava.medicineapp.admin;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.dao.UserDao;
import com.smwujava.medicineapp.model.User;
import com.smwujava.medicineapp.model.DosageRecord;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class AverageChartPanel extends JPanel {

    // 시간대별 평균 성공률 저장용 변수
    // 이 변수는 "시간대"를 키(String, 예: "08시")로 하고,
    // 그 시간대의 평균 성공률을 값(Double)으로 저장
    private Map<String, Double> successRatesByTimeSlot = new LinkedHashMap<>();

    public AverageChartPanel() {
        setLayout(new BorderLayout());

        DefaultCategoryDataset dataset = createDataset();

        JFreeChart chart = ChartFactory.createLineChart(
                "시간대별 전체 사용자 평균 복용 성공률 (최근 7일)",
                "시간대",
                "평균 성공률 (%)",
                dataset
        );

        // 제목, 축, 범례 폰트 설정
        Font font = new Font("맑은 고딕", Font.BOLD, 14);
        chart.getTitle().setFont(font);
        chart.getLegend().setItemFont(new Font("맑은 고딕", Font.PLAIN, 12));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.getDomainAxis().setTickLabelFont(new Font("맑은 고딕", Font.PLAIN, 10));
        plot.getDomainAxis().setLabelFont(new Font("맑은 고딕", Font.BOLD, 13));
        plot.getRangeAxis().setTickLabelFont(new Font("맑은 고딕", Font.PLAIN, 10));
        plot.getRangeAxis().setLabelFont(new Font("맑은 고딕", Font.BOLD, 13));
        plot.getRangeAxis().setRange(0.0, 100.0); // Y축 고정

        CategoryAxis xAxis = plot.getDomainAxis();
        xAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);

        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.gray);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(900, 500));

        add(chartPanel, BorderLayout.CENTER);
    }

    private DefaultCategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, List<Double>> slotToRates = new HashMap<>();

        DosageRecordDao dao = new DosageRecordDao();
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6); // 최근 7일

        List<Integer> userIds = new ArrayList<>();
        try {
            for (User user : UserDao.getAllNormalUsers()) {
                userIds.add(user.getUserId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int userId : userIds) {
            List<DosageRecord> records;
            try {
                records = dao.findRecordsByUserIdAndDateRange(userId, start.toString(), end.toString());
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            Map<String, Integer> total = new HashMap<>();
            Map<String, Integer> success = new HashMap<>();

            for (DosageRecord record : records) {
                String slot = getTimeSlot(record.getScheduledTime().toLocalTime());
                total.put(slot, total.getOrDefault(slot, 0) + 1);
                if (record.isTaken()) {
                    success.put(slot, success.getOrDefault(slot, 0) + 1);
                }
            }

            for (String slot : total.keySet()) {
                int t = total.get(slot);
                int s = success.getOrDefault(slot, 0);
                double rate = t == 0 ? 0 : (double) s / t * 100;
                slotToRates.putIfAbsent(slot, new ArrayList<>());
                slotToRates.get(slot).add(rate);
            }
        }

        // 평균 계산 및 차트 데이터 세팅
        for (int hour = 0; hour < 24; hour++) {
            String slot = String.format("%02d시", hour);
            List<Double> rates = slotToRates.getOrDefault(slot, new ArrayList<>());
            double avg = rates.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            dataset.addValue(avg, "전체 평균 성공률", slot);
            successRatesByTimeSlot.put(slot, avg); // Map에도 저장
            // avg는 각 시간대의 평균 성공률입니다.
            // 이걸 Map에 넣어서 나중에 꺼내 쓸 수 있게 저장
        }

        return dataset;
    }

    private String getTimeSlot(LocalTime time) {
        int hour = time.getHour();
        return String.format("%02d시", hour);
    }

    // 외부 접근용 Getter
    public Map<String, Double> getSuccessRatesByTimeSlot() {
        return successRatesByTimeSlot;
    }
}