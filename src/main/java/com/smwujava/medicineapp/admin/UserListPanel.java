package com.smwujava.medicineapp.admin;

import com.smwujava.medicineapp.controller.UserSummary;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserListPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public UserListPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // 상단 타이틀 라벨
        JLabel title = new JLabel("사용자 정보");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // 테이블 헤더
        String[] columns = {"사용자 ID", "약 개수", "성공률 (%)"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);

        // 스크롤 가능한 테이블 패널
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        add(scrollPane, BorderLayout.CENTER);
    }

    // 표 업데이트용 외부 메서드
    public void updateTable(List<UserSummary> summaries) {
        tableModel.setRowCount(0); // 기존 행 제거
        for (UserSummary u : summaries) {
            Object[] row = {
                    u.getUserId(),
                    u.getMedicineCount(),
                    String.format("%.1f%%", u.getSuccessRate())
            };
            tableModel.addRow(row);
        }
    }
}