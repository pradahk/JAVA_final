package com.smwujava.medicineapp.admin;

import com.smwujava.medicineapp.controller.UserSummary;
import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.dao.UserDao;
import com.smwujava.medicineapp.model.DosageRecord;
import com.smwujava.medicineapp.model.User;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminDashboardPanel extends JPanel {

    public AdminDashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 상단 영역
        JPanel topPanel = new JPanel(new GridLayout(1, 2));
        topPanel.setPreferredSize(new Dimension(1000, 300));
        topPanel.setBackground(Color.LIGHT_GRAY);
        topPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));

        UserListPanel userInfoPanel = new UserListPanel(); // JPanel → 실제 타입으로 변경
        VersionPanel versionPanel = new VersionPanel();

        topPanel.add(userInfoPanel);
        topPanel.add(versionPanel);

        // 사용자 요약 데이터 주입
        try {
            List<UserSummary> summaries = new ArrayList<>();
            List<User> users = UserDao.getAllNormalUsers();
            DosageRecordDao recordDao = new DosageRecordDao();
            MedicineDao medicineDao = new MedicineDao();
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusDays(6);

            for (User user : users) {
                int uid = user.getUserId();
                int medCount = medicineDao.getMedicineCountByUserId(uid);
                List<DosageRecord> records = recordDao.findRecordsByUserIdAndDateRange(uid, start.toString(), end.toString());
                int total = records.size();
                int success = (int) records.stream().filter(DosageRecord::isTaken).count();
                double rate = total == 0 ? 0.0 : (success * 100.0 / total);
                summaries.add(new UserSummary(String.valueOf(uid), medCount, rate));
            }

            userInfoPanel.updateTable(summaries);
        } catch (Exception e) {
            e.printStackTrace();
            userInfoPanel.add(new JLabel("사용자 요약 정보 불러오기 실패"), BorderLayout.SOUTH);
        }

        // 하단 영역
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));

        JPanel chartPanel = new JPanel();
        chartPanel.setLayout(new BoxLayout(chartPanel, BoxLayout.Y_AXIS));
        chartPanel.setPreferredSize(new Dimension(1000, 400));
        chartPanel.setBackground(Color.WHITE);

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

        //복용 기록 보기 버튼 추가
        JButton showRecordButton = new JButton("복용 기록 보기");
        showRecordButton.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        showRecordButton.addActionListener(e -> {
            DosageRecordDao dao = new DosageRecordDao();
            DosageRecordDialog dialog = new DosageRecordDialog((JFrame) SwingUtilities.getWindowAncestor(this), dao);
            dialog.setVisible(true);
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(showRecordButton);
        add(buttonPanel, BorderLayout.SOUTH);

    }
}
