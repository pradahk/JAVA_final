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

        JLabel title = new JLabel("사용자 정보");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // 읽기 전용 모델 정의
        String[] columns = {"사용자 ID", "약 개수", "성공률 (%)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 모든 셀 읽기 전용
            }
        };

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void updateTable(List<UserSummary> summaries) {
        tableModel.setRowCount(0);
        for (UserSummary u : summaries) {
            Object[] row = {
                    u.getUsername(),
                    u.getMedicineCount(),
                    String.format("%.1f%%", u.getSuccessRate())
            };
            tableModel.addRow(row);
        }
    }
}
