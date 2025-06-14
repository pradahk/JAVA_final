package com.smwujava.medicineapp.calendar;

import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.dao.UserPatternDao;
import com.smwujava.medicineapp.model.Medicine;
import com.smwujava.medicineapp.model.DosageRecord;
import com.smwujava.medicineapp.model.UserPattern;
import com.smwujava.medicineapp.ui.panels.CalendarPanel;

import java.awt.Color;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class CalendarController {

    private final MedicineDao medicineDao;
    private final DosageRecordDao dosageRecordDao;
    private final UserPatternDao userPatternDao;
    private CalendarPanel calendarPanel;
    private YearMonth currentYearMonth;
    private final int currentUserId;
    private int lastSelectedDay = -1;
    private final Color NOT_TAKEN_COLOR = new Color(224, 224, 224);

    public CalendarController(int userId) {
        this.currentUserId = userId;
        this.medicineDao = new MedicineDao();
        this.dosageRecordDao = new DosageRecordDao();
        this.userPatternDao = new UserPatternDao();
        this.currentYearMonth = YearMonth.now();
    }

    public void setCalendarPanel(CalendarPanel calendarPanel) {
        this.calendarPanel = calendarPanel;
    }

    public YearMonth getCurrentYearMonth() {
        return currentYearMonth;
    }

    public int getCurrentUserId() {
        return this.currentUserId;
    }

    public void loadCalendarData() {
        if (calendarPanel == null) return;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MMMM", Locale.KOREAN);
        calendarPanel.updateMonthYearLabel(currentYearMonth.format(formatter));
        Map<Integer, List<Color>> dayPillColorsMap = preparePillColorMap();
        calendarPanel.displayCalendarGrid(currentYearMonth, dayPillColorsMap);

        int dayToSelect;
        if (lastSelectedDay != -1 && isDayInCurrentMonth(lastSelectedDay)) {
            dayToSelect = lastSelectedDay;
        } else {
            LocalDate today = LocalDate.now();
            dayToSelect = (today.getYear() == currentYearMonth.getYear() && today.getMonth() == currentYearMonth.getMonth())
                    ? today.getDayOfMonth() : 1;
        }

        calendarPanel.selectDayInUI(dayToSelect);
        loadMedicationsForDay(currentYearMonth.atDay(dayToSelect));
    }

    private Map<LocalDate, List<DosageRecord>> getMonthlyDosageRecords() throws SQLException {
        Map<LocalDate, List<DosageRecord>> monthlyRecords = new HashMap<>();
        for (int day = 1; day <= currentYearMonth.lengthOfMonth(); day++) {
            LocalDate date = currentYearMonth.atDay(day);
            monthlyRecords.put(date, dosageRecordDao.findRecordsByUserIdAndDate(currentUserId, date.format(DateTimeFormatter.ISO_DATE)));
        }
        return monthlyRecords;
    }

    private Map<Integer, List<Color>> preparePillColorMap() {
        Map<Integer, List<Color>> dayPillColorsMap = new HashMap<>();
        try {
            List<Medicine> userMedicines = medicineDao.findMedicinesByUserId(currentUserId);
            Map<LocalDate, List<DosageRecord>> monthlyDosageRecords = getMonthlyDosageRecords();

            for (int day = 1; day <= currentYearMonth.lengthOfMonth(); day++) {
                LocalDate currentDate = currentYearMonth.atDay(day);
                String dayOfWeek = currentDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
                List<Color> colorsForDay = new ArrayList<>();
                List<DosageRecord> recordsForDate = monthlyDosageRecords.getOrDefault(currentDate, new ArrayList<>());

                for (Medicine med : userMedicines) {
                    if (isMedicineScheduledForDay(med, dayOfWeek)) {
                        boolean isTaken = recordsForDate.stream()
                                .anyMatch(dr -> dr.getMedId() == med.getMedId() && dr.isTaken());

                        if (isTaken) {
                            try {
                                colorsForDay.add(Color.decode(med.getColor()));
                            } catch (Exception e) {
                                colorsForDay.add(Color.GRAY);
                            }
                        } else {
                            colorsForDay.add(NOT_TAKEN_COLOR);
                        }
                    }
                }
                if (!colorsForDay.isEmpty()) {
                    dayPillColorsMap.put(day, colorsForDay);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (calendarPanel != null) calendarPanel.showError("달력 정보 로딩 오류: " + e.getMessage());
        }
        return dayPillColorsMap;
    }

    private boolean isMedicineScheduledForDay(Medicine medicine, String dayOfWeekShortKorean) {
        if (medicine.getMedDays() == null || medicine.getMedDays().isEmpty()) return false;
        return List.of(medicine.getMedDays().split(",")).stream().anyMatch(day -> day.trim().equals(dayOfWeekShortKorean));
    }

    public void changeMonth(int amount) {
        currentYearMonth = currentYearMonth.plusMonths(amount);
        lastSelectedDay = -1;
        loadCalendarData();
    }

    public void loadMedicationsForDay(LocalDate date) {
        this.lastSelectedDay = date.getDayOfMonth();
        if (calendarPanel == null) return;
        List<CalendarPanel.MedicationInfo> medicationInfos = new ArrayList<>();
        try {
            String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
            List<Medicine> scheduledMeds = medicineDao.findMedicinesByUserId(currentUserId)
                    .stream()
                    .filter(med -> isMedicineScheduledForDay(med, dayOfWeek))
                    .collect(Collectors.toList());
            List<DosageRecord> dosageRecords = dosageRecordDao.findRecordsByUserIdAndDate(currentUserId, date.format(DateTimeFormatter.ISO_DATE));

            for (Medicine med : scheduledMeds) {
                boolean isTaken = dosageRecords.stream()
                        .filter(dr -> dr.getMedId() == med.getMedId())
                        .anyMatch(DosageRecord::isTaken);
                String timeStr = formatMedicationTime(med);
                Color color;
                try {
                    color = Color.decode(med.getColor());
                } catch (Exception e) {
                    color = Color.GRAY;
                }
                medicationInfos.add(new CalendarPanel.MedicationInfo(med.getMedId(), med.getMedName(), timeStr, color, isTaken));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (calendarPanel != null) calendarPanel.showError("약물 정보 로딩 오류: " + e.getMessage());
        }
        calendarPanel.updateMedicationList(medicationInfos);
    }

    public void handleMedicationTakenStatusChange(int medId, LocalDate date, boolean isTakenNow) {
        Medicine medicine;
        try {
            medicine = medicineDao.findMedicineById(medId);
            if (medicine == null) {
                if (calendarPanel != null) calendarPanel.showError("약물 정보를 찾을 수 없습니다.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (calendarPanel != null) calendarPanel.showError("약물 정보 조회 중 DB 오류.");
            return;
        }
        LocalDateTime scheduledTime = calculateScheduledTimeForMedicine(date, medicine);
        try {
            DosageRecord record = dosageRecordDao.findRecordByUserIdMedIdAndScheduledTime(currentUserId, medId, scheduledTime);
            if (record == null) {
                record = new DosageRecord(0, currentUserId, medId, scheduledTime,
                        isTakenNow ? LocalDateTime.now() : null, null, !isTakenNow);
                record.setRecordId(dosageRecordDao.insertDosageRecord(record));
            } else {
                record.setActualTakenTime(isTakenNow ? LocalDateTime.now() : null);
                record.setSkipped(!isTakenNow);
                dosageRecordDao.updateDosageRecord(record);
            }
            loadCalendarData();
        } catch (SQLException e) {
            e.printStackTrace();
            if (calendarPanel != null) calendarPanel.showError("복용 기록 업데이트 중 DB 오류.");
        }
    }

    private String formatMedicationTime(Medicine medicine) {
        if (medicine == null) return "시간 정보 없음";
        String condition = medicine.getMedCondition() != null ? medicine.getMedCondition() : "";
        String timing = medicine.getMedTiming() != null ? medicine.getMedTiming() : "";
        int minutes = medicine.getMedMinutes();
        StringBuilder sb = new StringBuilder(condition);
        if (minutes > 0) sb.append(" ").append(minutes).append("분");
        if (!timing.isEmpty() && (!"정각".equals(timing) || minutes == 0)) sb.append(" ").append(timing);
        return sb.toString().trim();
    }

    private LocalDateTime calculateScheduledTimeForMedicine(LocalDate date, Medicine medicine) {
        LocalTime baseTime = LocalTime.NOON;
        try {
            UserPattern pattern = userPatternDao.findPatternByUserId(currentUserId);
            String medCondition = medicine.getMedCondition() != null ? medicine.getMedCondition().trim() : "";
            if (pattern != null) {
                if (medCondition.contains("아침"))
                    baseTime = (pattern.getBreakfastStartTime() != null && !pattern.getBreakfastStartTime().isEmpty()) ? LocalTime.parse(pattern.getBreakfastStartTime()) : LocalTime.of(8,0);
                else if (medCondition.contains("점심"))
                    baseTime = (pattern.getLunchStartTime() != null && !pattern.getLunchStartTime().isEmpty()) ? LocalTime.parse(pattern.getLunchStartTime()) : LocalTime.of(12,0);
                else if (medCondition.contains("저녁"))
                    baseTime = (pattern.getDinnerStartTime() != null && !pattern.getDinnerStartTime().isEmpty()) ? LocalTime.parse(pattern.getDinnerStartTime()) : LocalTime.of(18,0);
                else if (medCondition.contains("취침"))
                    baseTime = (pattern.getSleepStartTime() != null && !pattern.getSleepStartTime().isEmpty()) ? LocalTime.parse(pattern.getSleepStartTime()) : LocalTime.of(22,0);
            }
        } catch (Exception e) { System.err.println("시간 계산 오류: " + e.getMessage()); }
        int minutes = medicine.getMedMinutes();
        String timing = medicine.getMedTiming();
        if ("전".equals(timing)) baseTime = baseTime.minusMinutes(minutes);
        else if ("후".equals(timing)) baseTime = baseTime.plusMinutes(minutes);
        return LocalDateTime.of(date, baseTime);
    }

    private boolean isDayInCurrentMonth(int day) {
        return day > 0 && day <= currentYearMonth.lengthOfMonth();
    }
}