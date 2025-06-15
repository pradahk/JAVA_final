package com.smwujava.medicineapp.ui.panels;

import com.smwujava.medicineapp.calendar.CalendarController;
import com.smwujava.medicineapp.ui.components.CalendarDayPanel;
import com.smwujava.medicineapp.Scheduler.MedicationSchedulerService;
import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.dao.UserPatternDao;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CalendarPanel extends JPanel {
    DosageRecordDao recordDao = new DosageRecordDao();
    MedicineDao medicineDao = new MedicineDao();
    UserPatternDao userPatternDao = new UserPatternDao();

    private MedicationListPanel medicationListPanel;
    private final CalendarController controller;
    private JLabel monthLabelCurrent;
    private JPanel dateGridPanel;
    private CalendarDayPanel currentlyHighlightedDayPanel = null;
    private int currentSelectedDayForList = -1;

    public CalendarPanel(CardLayout cardLayout, JPanel parentPanel, int userId) {
        this.controller = new CalendarController(userId);
        this.controller.setCalendarPanel(this);

        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weighty = 1.0;

        JPanel calendarContainer = new JPanel(new BorderLayout(0,10));
        calendarContainer.setOpaque(false);

        JPanel topNavPanel = new JPanel(new BorderLayout());
        topNavPanel.setOpaque(false);
        JButton prevButton = new JButton("‹");
        prevButton.setFont(new Font("SansSerif", Font.BOLD, 18));
        prevButton.addActionListener(e -> controller.changeMonth(-1));
        monthLabelCurrent = new JLabel("", SwingConstants.CENTER);
        monthLabelCurrent.setFont(new Font("SansSerif", Font.BOLD, 20));
        JButton nextButton = new JButton("›");
        nextButton.setFont(new Font("SansSerif", Font.BOLD, 18));
        nextButton.addActionListener(e -> controller.changeMonth(1));
        topNavPanel.add(prevButton, BorderLayout.WEST);
        topNavPanel.add(monthLabelCurrent, BorderLayout.CENTER);
        topNavPanel.add(nextButton, BorderLayout.EAST);
        calendarContainer.add(topNavPanel, BorderLayout.NORTH);

        dateGridPanel = new JPanel(new GridLayout(0, 7, 4, 4));
        dateGridPanel.setBackground(Color.WHITE);
        calendarContainer.add(dateGridPanel, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.weightx = 0.7;
        add(calendarContainer, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.3;
        medicationListPanel = new MedicationListPanel(cardLayout, parentPanel, this.controller.getCurrentUserId());
        medicationListPanel.setCalendarPanel(this);
        add(medicationListPanel, gbc);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                if (controller != null) {
                    controller.loadCalendarData();
                }
            }
        });
    }

    public CalendarController getController() {
        return this.controller;
    }

    public void refresh() {
        if (controller != null) {
            controller.loadCalendarData();
        }
    }

    public void updateMonthYearLabel(String formattedMonthYear) {
        monthLabelCurrent.setText(formattedMonthYear);
    }

    public void displayCalendarGrid(YearMonth yearMonthToDisplay, Map<Integer, List<Color>> dayPillColorsMap) {
        dateGridPanel.removeAll();

        String[] headers = {"일", "월", "화", "수", "목", "금", "토"};
        for (String h : headers) {
            JLabel lbl = new JLabel(h, SwingConstants.CENTER);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
            if (h.equals("일")) lbl.setForeground(Color.RED);
            else if (h.equals("토")) lbl.setForeground(Color.BLUE);
            dateGridPanel.add(lbl);
        }

        LocalDate firstDayOfCurrentMonth = yearMonthToDisplay.atDay(1);
        DayOfWeek firstDayOfWeekEnum = firstDayOfCurrentMonth.getDayOfWeek();
        int startColumnOffset = firstDayOfWeekEnum.getValue() % 7;

        YearMonth prevYearMonth = yearMonthToDisplay.minusMonths(1);
        int daysInPrevMonth = prevYearMonth.lengthOfMonth();
        for (int i = 0; i < startColumnOffset; i++) {
            int dayVal = daysInPrevMonth - startColumnOffset + 1 + i;
            CalendarDayPanel cell = new CalendarDayPanel(dayVal, new ArrayList<>(), false);
            dateGridPanel.add(cell);
        }

        int daysInCurrentMonth = yearMonthToDisplay.lengthOfMonth();
        for (int dayVal = 1; dayVal <= daysInCurrentMonth; dayVal++) {
            List<Color> pillColors = dayPillColorsMap.getOrDefault(dayVal, new ArrayList<>());
            final LocalDate cellDate = yearMonthToDisplay.atDay(dayVal);
            final int currentDayForListener = dayVal;

            CalendarDayPanel cell = new CalendarDayPanel(currentDayForListener, pillColors, true);
            cell.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (currentlyHighlightedDayPanel != null) {
                        currentlyHighlightedDayPanel.setBackground(Color.WHITE);
                        currentlyHighlightedDayPanel.setBorder(null);
                    }
                    cell.setBackground(new Color(200, 220, 255));
                    cell.setBorder(new LineBorder(Color.BLUE, 1));
                    currentlyHighlightedDayPanel = cell;
                    currentSelectedDayForList = currentDayForListener;

                    if(medicationListPanel != null) medicationListPanel.setSelectedDay(currentSelectedDayForList);
                    controller.loadMedicationsForDay(cellDate);
                }
            });
            dateGridPanel.add(cell);
        }

        int totalDayCellsInGrid = 42;
        int currentGridCells = startColumnOffset + daysInCurrentMonth;
        int remainingCells = totalDayCellsInGrid - currentGridCells;

        int dayForNextMonth = 1;
        for (int i = 0; i < remainingCells; i++) {
            CalendarDayPanel cell = new CalendarDayPanel(dayForNextMonth++, new ArrayList<>(), false);
            dateGridPanel.add(cell);
        }

        dateGridPanel.revalidate();
        dateGridPanel.repaint();
    }

    public void updateMedicationList(List<CalendarPanel.MedicationInfo> medicationDetails) {
        if(medicationListPanel != null) medicationListPanel.updateMedications(medicationDetails);
    }

    public void selectDayInUI(int dayToSelect) {
        if (currentlyHighlightedDayPanel != null) {
            currentlyHighlightedDayPanel.setBackground(Color.WHITE);
            currentlyHighlightedDayPanel.setBorder(null);
            currentlyHighlightedDayPanel = null;
        }
        currentSelectedDayForList = -1;

        Component[] components = dateGridPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof CalendarDayPanel) {
                CalendarDayPanel panel = (CalendarDayPanel) comp;
                if (panel.isInMonth() && panel.getDay() == dayToSelect) {
                    panel.setBackground(new Color(200, 220, 255));
                    panel.setBorder(new LineBorder(Color.BLUE, 1));
                    currentlyHighlightedDayPanel = panel;
                    currentSelectedDayForList = dayToSelect;
                    break;
                }
            }
        }
        if (medicationListPanel != null) {
            medicationListPanel.setSelectedDay(dayToSelect);
        }
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "오류", JOptionPane.ERROR_MESSAGE);
    }

    public void notifyMedicationStatusChanged(int medId, boolean isTakenNow) {
        if (currentSelectedDayForList != -1 && controller.getCurrentYearMonth() != null) {
            LocalDate dateOfMedication = controller.getCurrentYearMonth().atDay(currentSelectedDayForList);
            controller.handleMedicationTakenStatusChange(medId, dateOfMedication, isTakenNow);
        } else {
            showError("오류: 날짜가 선택되지 않았거나 월 정보가 없습니다.");
        }
    }

    public static class MedicationInfo {
        private final int medId;
        private final String name;
        private final String time;
        private final Color color;
        private boolean isTaken;

        public MedicationInfo(int medId, String name, String time, Color color, boolean isTaken) {
            this.medId = medId;
            this.name = name;
            this.time = time;
            this.color = color;
            this.isTaken = isTaken;
        }

        public int getMedId() { return medId; }
        public String getName() { return name; }
        public String getTime() { return time; }
        public Color getColor() { return color; }
        public boolean isTaken() { return isTaken; }
        public void setTaken(boolean taken) { isTaken = taken; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MedicationInfo that = (MedicationInfo) o;
            return medId == that.medId && isTaken == that.isTaken && Objects.equals(name, that.name) && Objects.equals(time, that.time) && Objects.equals(color, that.color);
        }

        @Override
        public int hashCode() {
            return Objects.hash(medId, name, time, color, isTaken);
        }
    }
}