package com.smwujava.medicineapp.ui.panels;

import com.smwujava.medicineapp.calendar.CalendarController; // 컨트롤러 임포트
import com.smwujava.medicineapp.ui.components.CalendarDayPanel;
import com.smwujava.medicineapp.Scheduler.MedicationSchedulerService;
import com.smwujava.medicineapp.dao.DosageRecordDao;


import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.dao.UserPatternDao;



public class CalendarPanel extends JPanel {
    DosageRecordDao recordDao = new DosageRecordDao();
    MedicineDao medicineDao = new MedicineDao();
    UserPatternDao userPatternDao = new UserPatternDao();



    // private final Map<Integer, CalendarDayPanel> dayPanels = new HashMap<>(); // Controller가 데이터를 관리하므로 직접 참조 불필요할 수 있음
    private MedicationListPanel medicationListPanel; // 원본 MedicationListPanel 타입
    // private int selectedDay = 4; // Controller가 선택된 날짜 관리

    private final CalendarController controller;
    private JLabel monthLabelCurrent; // 동적으로 월 표시할 레이블
    private JPanel dateGridPanel;     // 날짜 셀들이 들어갈 패널
    private CalendarDayPanel currentlyHighlightedDayPanel = null;
    private int currentSelectedDayForList = -1; // MedicationListPanel에 전달할 날짜

    public CalendarPanel(CardLayout cardLayout, JPanel parentPanel) {
        this.controller = new CalendarController();
        this.controller.setCalendarPanel(this);

        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10); // 여백 조정
        gbc.weighty = 1.0;

        JPanel calendarContainer = new JPanel(new BorderLayout(0,10)); // 상하 간격
        calendarContainer.setOpaque(false); // 배경 투명하게 하여 CalendarPanel 배경색 사용

        // 상단 컨트롤 (이전/다음 버튼, 월 표시)
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

        dateGridPanel = new JPanel(new GridLayout(0, 7, 4, 4)); // 행 자동, 7열
        dateGridPanel.setBackground(Color.WHITE);
        calendarContainer.add(dateGridPanel, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.weightx = 0.7; // 캘린더 영역 비율
        add(calendarContainer, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.3; // 약물 목록 영역 비율
        // 원본 MedicationListPanel 사용
        medicationListPanel = new MedicationListPanel(cardLayout, parentPanel);
        medicationListPanel.setCalendarPanel(this); // MedicationListPanel이 CalendarPanel을 참조해야 할 경우
        add(medicationListPanel, gbc);

        controller.loadCalendarData(); // 컨트롤러 통해 초기 데이터 로드
    }

    public CalendarController getController() {
        return this.controller;
    }

    public void updateMonthYearLabel(String formattedMonthYear) {
        monthLabelCurrent.setText(formattedMonthYear);
    }

    private void setupCalendar() {
        controller.loadCalendarData();  // 컨트롤러가 날짜 셀을 다시 구성해줌
    }

    public void refresh() {
        System.out.println("[CalendarPanel] refresh() 호출됨 - 복약 기록을 다시 불러옵니다.");
        this.removeAll();        // 기존 컴포넌트 제거
        this.revalidate();       // 레이아웃 재계산
        this.repaint();        //다시 그리기
        setupCalendar();      // 날짜 그리드 다시 구성
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
                    cell.setBackground(new Color(200, 220, 255)); // 하이라이트 색상
                    cell.setBorder(new LineBorder(Color.BLUE, 1));
                    currentlyHighlightedDayPanel = cell;
                    currentSelectedDayForList = currentDayForListener;

                    medicationListPanel.setSelectedDay(currentSelectedDayForList); // MedicationListPanel에 날짜 알림 (제목 변경 등)
                    controller.loadMedicationsForDay(cellDate); // 컨트롤러에 약물 로드 요청
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
        medicationListPanel.updateMedications(medicationDetails);
    }

    public void triggerTodayAlarm(int userId) {
        // 현재 CalendarPanel이 붙어 있는 최상위 JFrame을 찾음
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

        // 서비스 객체 생성 및 알람 예약 요청
        MedicationSchedulerService scheduler = new MedicationSchedulerService(recordDao, medicineDao, userPatternDao);
        scheduler.scheduleTodayMedications(userId, parentFrame);
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

    // MedicationInfo DTO (medId, isTaken 필드 포함된 버전)
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
        public void setTaken(boolean taken) { isTaken = taken; } // 필요시 상태 변경용

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


