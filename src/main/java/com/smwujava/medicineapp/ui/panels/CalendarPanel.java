package com.smwujava.medicineapp.ui.panels;

import com.smwujava.medicineapp.ui.components.CalendarDayPanel;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class CalendarPanel extends JPanel {
    private final Map<Integer, CalendarDayPanel> dayPanels = new HashMap<>();
    private final MedicationListPanel medicationListPanel;
    private int selectedDay = 4; // 기본 선택 날짜

    public CalendarPanel(CardLayout cardLayout, JPanel parentPanel) {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.weighty = 1.0;

        JPanel calendarContainer = new JPanel(new BorderLayout());
        calendarContainer.setBackground(new Color(250, 250, 250));

        JLabel monthLabel = new JLabel("5월", SwingConstants.CENTER);
        monthLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        calendarContainer.add(monthLabel, BorderLayout.NORTH);

        JPanel dateGrid = new JPanel(new GridLayout(7, 7, 4, 4));
        dateGrid.setBackground(Color.WHITE);

        String[] headers = {"일", "월", "화", "수", "목", "금", "토"};
        for (String h : headers) {
            JLabel lbl = new JLabel(h, SwingConstants.CENTER);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
            dateGrid.add(lbl);
        }

        int[] dates = {
                27, 28, 29, 30, 1, 2, 3,
                4, 5, 6, 7, 8, 9, 10,
                11,12,13,14,15,16,17,
                18,19,20,21,22,23,24,
                25,26,27,28,29,30,31,
                1, 2, 3, 4, 5, 6, 7
        };

        Map<Integer, List<Color>> dummyData = new HashMap<>();
        dummyData.put(4, Arrays.asList(new Color(181, 169, 255), new Color(232, 253, 148)));
        dummyData.put(10, List.of(Color.LIGHT_GRAY));

        for (int i = 0; i < 42; i++) {
            int day = dates[i];
            boolean isInMonth = (i >= 4 && i < 35);
            List<Color> pillColors = dummyData.getOrDefault(day, new ArrayList<>());

            CalendarDayPanel cell = new CalendarDayPanel(day, pillColors, isInMonth);
            if (isInMonth) dayPanels.put(day, cell);
            dateGrid.add(cell);
        }

        calendarContainer.add(dateGrid, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.weightx = 0.8;
        add(calendarContainer, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.2;
        medicationListPanel = new MedicationListPanel(cardLayout, parentPanel);
        medicationListPanel.setCalendarPanel(this); // 연결
        medicationListPanel.setSelectedDay(selectedDay); // 기본 설정
        add(medicationListPanel, gbc);

        List<MedicationInfo> initialMeds = Arrays.asList(
                new MedicationInfo("Omeprazol", "아침 30분 전", new Color(181, 169, 255)),
                new MedicationInfo("Panpyrin-Q", "점심 30분 전", new Color(232, 253, 148)),
                new MedicationInfo("Ibuprofen", "저녁 30분 후", Color.LIGHT_GRAY)
        );
        updateDay(4, initialMeds);
        medicationListPanel.updateMedications(initialMeds);
    }

    public void updateDay(int day, List<MedicationInfo> meds) {
        CalendarDayPanel panel = dayPanels.get(day);
        if (panel != null) {
            List<Color> colors = new ArrayList<>();
            for (MedicationInfo med : meds) {
                colors.add(med.getColor());
            }
            panel.updatePillColors(colors);
        }
    }

    public void addMedicationToDay(int day, MedicationInfo med) {
        CalendarDayPanel panel = dayPanels.get(day);
        if (panel != null) {
            panel.addOnePillColor(med.getColor());
        }
    }

    // MedicationListPanel에서 색상을 제거할 때 호출됨
    public void removeMedicationColorFromDay(int day, Color color) {
        CalendarDayPanel panel = dayPanels.get(day);
        if (panel != null) {
            panel.removePillColor(color);
        }
    }

    public void setSelectedDay(int day) {
        this.selectedDay = day;
        medicationListPanel.setSelectedDay(day);
    }

    public static class MedicationInfo {
        private final String name;
        private final String time;
        private final Color color;

        public MedicationInfo(String name, String time, Color color) {
            this.name = name;
            this.time = time;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public String getTime() {
            return time;
        }

        public Color getColor() {
            return color;
        }
    }
}
