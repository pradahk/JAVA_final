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
    private final int currentUserId = 1; // 애플리케이션 전체에서 관리될 사용자 ID

    public CalendarController() {
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

    public int getCurrentUserId() { // MedicationListPanel에 userId 전달용
        return this.currentUserId;
    }

    public void loadCalendarData() {
        if (calendarPanel == null) return;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MMMM", Locale.KOREAN);
        calendarPanel.updateMonthYearLabel(currentYearMonth.format(formatter));

        Map<Integer, List<Color>> dayPillColorsMap = new HashMap<>();
        int daysInMonth = currentYearMonth.lengthOfMonth();
        try {
            List<Medicine> userMedicines = medicineDao.findMedicinesByUserId(currentUserId);

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate currentDate = currentYearMonth.atDay(day);
                DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
                String currentDayOfWeekShortKorean = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN);

                List<Color> colorsForThisDay = new ArrayList<>();
                for (Medicine med : userMedicines) {
                    if (isMedicineScheduledForDay(med, currentDayOfWeekShortKorean)) {
                        try {
                            colorsForThisDay.add(Color.decode(med.getColor()));
                        } catch (NumberFormatException e) {
                            colorsForThisDay.add(Color.GRAY);
                        }
                    }
                }
                if (!colorsForThisDay.isEmpty()) {
                    dayPillColorsMap.put(day, colorsForThisDay);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (calendarPanel != null) calendarPanel.showError("달력 약물 정보 로딩 오류: " + e.getMessage());
        }

        calendarPanel.displayCalendarGrid(currentYearMonth, dayPillColorsMap);

        LocalDate today = LocalDate.now();
        int initialDayToSelect = (today.getYear() == currentYearMonth.getYear() && today.getMonth() == currentYearMonth.getMonth())
                ? today.getDayOfMonth() : 1;

        calendarPanel.selectDayInUI(initialDayToSelect);
        loadMedicationsForDay(currentYearMonth.atDay(initialDayToSelect));
    }

    private boolean isMedicineScheduledForDay(Medicine medicine, String dayOfWeekShortKorean) {
        if (medicine.getMedDays() == null || medicine.getMedDays().isEmpty()) {
            return false;
        }
        String[] scheduledDays = medicine.getMedDays().split(",");
        for (String day : scheduledDays) {
            if (day.trim().equals(dayOfWeekShortKorean)) {
                return true;
            }
        }
        return false;
    }

    public void changeMonth(int amount) {
        currentYearMonth = currentYearMonth.plusMonths(amount);
        loadCalendarData();
    }

    public void loadMedicationsForDay(LocalDate date) {
        if (calendarPanel == null) return;

        List<CalendarPanel.MedicationInfo> medicationInfosForDay = new ArrayList<>();
        try {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            String currentDayOfWeekShortKorean = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN);

            List<Medicine> scheduledMedicinesOnDate = medicineDao.findMedicinesByUserId(currentUserId)
                    .stream()
                    .filter(med -> isMedicineScheduledForDay(med, currentDayOfWeekShortKorean))
                    .collect(Collectors.toList());

            List<DosageRecord> dosageRecordsForDate = dosageRecordDao.findRecordsByUserIdAndDate(currentUserId, date.format(DateTimeFormatter.ISO_DATE));

            for (Medicine med : scheduledMedicinesOnDate) {
                boolean isTaken = dosageRecordsForDate.stream()
                        .filter(dr -> dr.getMedId() == med.getMedId())
                        .anyMatch(DosageRecord::isTaken);

                String timeStr = formatMedicationTime(med);
                Color medColor;
                try {
                    medColor = Color.decode(med.getColor());
                } catch (NumberFormatException e) {
                    medColor = Color.GRAY;
                }

                medicationInfosForDay.add(new CalendarPanel.MedicationInfo(
                        med.getMedId(), med.getMedName(), timeStr, medColor, isTaken));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (calendarPanel != null) calendarPanel.showError("선택일 약물 정보 로딩 오류: " + e.getMessage());
        }
        calendarPanel.updateMedicationList(medicationInfosForDay);
    }

    public void handleMedicationTakenStatusChange(int medId, LocalDate date, boolean isTakenNow) {
        Medicine medicine;
        try {
            medicine = medicineDao.findMedicineById(medId);
            if (medicine == null) {
                if (calendarPanel != null) calendarPanel.showError("약물 정보를 찾을 수 없습니다 (ID: " + medId + ")");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (calendarPanel != null) calendarPanel.showError("약물 정보 조회 중 DB 오류: " + e.getMessage());
            return;
        }

        LocalDateTime representativeScheduledTime = calculateScheduledTimeForMedicine(date, medicine, currentUserId);

        try {
            DosageRecord recordToUpdate = dosageRecordDao.findRecordByUserIdMedIdAndScheduledTime(currentUserId, medId, representativeScheduledTime);

            if (recordToUpdate == null) {
                recordToUpdate = new DosageRecord(0, currentUserId, medId, representativeScheduledTime,
                        isTakenNow ? LocalDateTime.now() : null,
                        null,
                        !isTakenNow
                );
                int newRecordId = dosageRecordDao.insertDosageRecord(recordToUpdate);
                if (newRecordId == -1) {
                    if (calendarPanel != null) calendarPanel.showError("복용 기록 생성 실패.");
                    return;
                }
                recordToUpdate.setRecordId(newRecordId);
            } else {
                if (isTakenNow) {
                    recordToUpdate.setActualTakenTime(LocalDateTime.now());
                    recordToUpdate.setSkipped(false);
                } else {
                    recordToUpdate.setActualTakenTime(null);
                }
                dosageRecordDao.updateDosageRecord(recordToUpdate);
            }
            loadMedicationsForDay(date);
        } catch (SQLException e) {
            e.printStackTrace();
            if (calendarPanel != null) calendarPanel.showError("복용 기록 업데이트 중 DB 오류: " + e.getMessage());
        }
    }

    private String formatMedicationTime(Medicine medicine) {
        if (medicine == null) return "시간 정보 없음";
        String condition = medicine.getMedCondition() != null ? medicine.getMedCondition() : "";
        String timing = medicine.getMedTiming() != null ? medicine.getMedTiming() : "";
        int minutes = medicine.getMedMinutes();

        StringBuilder sb = new StringBuilder();
        sb.append(condition);

        if (minutes > 0) {
            sb.append(" ").append(minutes).append("분");
        }

        if (!timing.isEmpty() && (!"정각".equals(timing) || minutes == 0)) {
            sb.append(" ").append(timing);
        } else if ("정각".equals(timing) && minutes > 0) {
            sb.append(" ").append(timing);
        }

        String result = sb.toString().trim();
        return result.isEmpty() ? "시간 정보 없음" : result;
    }

    private LocalDateTime calculateScheduledTimeForMedicine(LocalDate date, Medicine medicine, int userIdForPattern) {
        LocalTime baseTime = LocalTime.NOON;
        try {
            UserPattern pattern = userPatternDao.findPatternByUserId(userIdForPattern);
            String medCondition = medicine.getMedCondition() != null ? medicine.getMedCondition().trim() : "";

            if (pattern != null) {
                if (medCondition.contains("아침") || (medCondition.equals("식사") && medicine.getMedDailyAmount() > 0 )) {
                    baseTime = pattern.getBreakfastStartTime() != null && !pattern.getBreakfastStartTime().equals(":") ? LocalTime.parse(pattern.getBreakfastStartTime()) : LocalTime.of(8,0);
                } else if (medCondition.contains("점심")) {
                    baseTime = pattern.getLunchStartTime() != null && !pattern.getLunchStartTime().equals(":") ? LocalTime.parse(pattern.getLunchStartTime()) : LocalTime.of(12,0);
                } else if (medCondition.contains("저녁")) {
                    baseTime = pattern.getDinnerStartTime() != null && !pattern.getDinnerStartTime().equals(":") ? LocalTime.parse(pattern.getDinnerStartTime()) : LocalTime.of(18,0);
                } else if (medCondition.contains("수면") || medCondition.contains("취침")) {
                    baseTime = pattern.getSleepStartTime() != null && !pattern.getSleepStartTime().equals(":") ? LocalTime.parse(pattern.getSleepStartTime()) : LocalTime.of(22,0);
                }
            } else {
                if (medCondition.contains("아침")) baseTime = LocalTime.of(8, 0);
                else if (medCondition.contains("점심")) baseTime = LocalTime.of(12, 0);
                else if (medCondition.contains("저녁")) baseTime = LocalTime.of(18, 0);
                else if (medCondition.contains("수면") || medCondition.contains("취침")) baseTime = LocalTime.of(22,0);
            }
        } catch (SQLException e) {
            System.err.println("UserPattern 조회 오류 in calculateScheduledTime: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("시간 변환 오류 in calculateScheduledTime ("+ medicine.getMedCondition() +"): " + e.getMessage());
        }

        String medTiming = medicine.getMedTiming() != null ? medicine.getMedTiming() : "";
        int medMinutes = medicine.getMedMinutes();

        if ("전".equals(medTiming)) {
            baseTime = baseTime.minusMinutes(medMinutes);
        } else if ("후".equals(medTiming)) {
            baseTime = baseTime.plusMinutes(medMinutes);
        }
        return LocalDateTime.of(date, baseTime);
    }
}