package com.smwujava.medicineapp.ui.panels;

import javax.swing.*;
import java.awt.*;

public class CalendarPanel extends JPanel {
    public CalendarPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 전체 여백
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createEmptyBorder(40, 30, 40, 30)); // 💡 더 넉넉한 여백

        JPanel mainContent = new JPanel(new GridLayout(1, 2, 40, 0)); // 간격도 증가
        mainContent.setBackground(Color.WHITE);

        // 달력
        JPanel calendar = new JPanel();
        calendar.setPreferredSize(new Dimension(340, 400)); // 💡 고정 크기
        calendar.setBackground(Color.LIGHT_GRAY);
        calendar.add(new JLabel("달력"));

        // 복약 목록
        MedicationListPanel meds = new MedicationListPanel();

        mainContent.add(calendar);
        mainContent.add(meds);

        wrapper.add(mainContent); // 가운데 정렬
        add(wrapper, BorderLayout.CENTER);
    }
}
