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
    private final int currentUserId = 1;
    private final Color NOT_TAKEN_COLOR = Color.LIGHT_GRAY; // 복용 전 약물 막대 표시용 회색

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

    public int getCurrentUserId() {
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
            Map<LocalDate, List<DosageRecord>> monthlyDosageRecords = getMonthlyDosageRecords(currentYearMonth, currentUserId);

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate currentDate = currentYearMonth.atDay(day);
                DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
                String currentDayOfWeekShortKorean = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN);
                List<Color> colorsForThisDay = new ArrayList<>();

                for (Medicine med : userMedicines) {
                    if (isMedicineScheduledForDay(med, currentDayOfWeekShortKorean)) {
                        // 해당 날짜, 해당 약에 대한 복용 기록을 찾아 isTaken 상태 확인
                        boolean isTakenToday = false;
                        List<DosageRecord> recordsForDate = monthlyDosageRecords.getOrDefault(currentDate, new ArrayList<>());
                        for (DosageRecord dr : recordsForDate) {
                            if (dr.getMedId() == med.getMedId() && dr.isTaken()) {
                                isTakenToday = true;
                                break;
                            }
                        }

                        if (isTakenToday) {
                            try { colorsForThisDay.add(Color.decode(med.getColor())); }
                            catch (NumberFormatException e) { colorsForThisDay.add(Color.GRAY); }
                        } else {
                            colorsForThisDay.add(NOT_TAKEN_COLOR); // 복용 전이면 지정된 회색
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

        if (calendarPanel != null) { // null 체크 추가
            calendarPanel.selectDayInUI(initialDayToSelect);
            loadMedicationsForDay(currentYearMonth.atDay(initialDayToSelect));
        }
    }

    private Map<LocalDate, List<DosageRecord>> getMonthlyDosageRecords(YearMonth yearMonth, int userId) throws SQLException {
        Map<LocalDate, List<DosageRecord>> monthlyRecords = new HashMap<>();
        String startDate = yearMonth.atDay(1).format(DateTimeFormatter.ISO_DATE);
        String endDate = yearMonth.atEndOfMonth().format(DateTimeFormatter.ISO_DATE);
        List<DosageRecord> recordsInMonth = dosageRecordDao.findRecordsByUserIdAndDateRange(userId, startDate, endDate); // 이 메서드는 '복용한' 기록 위주일 수 있음.

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            monthlyRecords.put(date, dosageRecordDao.findRecordsByUserIdAndDate(userId, date.format(DateTimeFormatter.ISO_DATE)));
        }
        return monthlyRecords;
    }


    private boolean isMedicineScheduledForDay(Medicine medicine, String dayOfWeekShortKorean) {
        if (medicine.getMedDays() == null || medicine.getMedDays().isEmpty()) return false;
        String[] scheduledDays = medicine.getMedDays().split(",");
        for (String day : scheduledDays) {
            if (day.trim().equals(dayOfWeekShortKorean)) return true;
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
                try { medColor = Color.decode(med.getColor()); }
                catch (NumberFormatException e) { medColor = Color.GRAY; }
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

            if (recordToUpdate == null) { // 해당 시간에 예정된 기록이 없다면 새로 생성
                recordToUpdate = new DosageRecord(0, currentUserId, medId, representativeScheduledTime,
                        isTakenNow ? LocalDateTime.now() : null,
                        null, // rescheduledTime
                        !isTakenNow // isSkipped: 복용했으면 안 건너뜀, 복용 안 했으면(취소) 건너뛴 것으로 간주 가능
                );
                int newRecordId = dosageRecordDao.insertDosageRecord(recordToUpdate);
                if (newRecordId == -1) {
                    if (calendarPanel != null) calendarPanel.showError("복용 기록 생성 실패.");
                    return;
                }
                recordToUpdate.setRecordId(newRecordId);
            } else { // 기존 기록 업데이트
                if (isTakenNow) {
                    recordToUpdate.setActualTakenTime(LocalDateTime.now());
                    recordToUpdate.setSkipped(false);
                } else { // 복용 취소 (isTakenNow = false)
                    recordToUpdate.setActualTakenTime(null);
                }
                dosageRecordDao.updateDosageRecord(recordToUpdate);
            }

            // 중요: UI 새로고침
            loadMedicationsForDay(date); // MedicationListPanel 새로고침
            loadCalendarData(); // CalendarDayPanel 막대 색상 새로고침 위해 전체 달력 데이터 다시 로드

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
        if (minutes > 0) { sb.append(" ").append(minutes).append("분"); }
        if (!timing.isEmpty() && (!"정각".equals(timing) || minutes == 0)) { sb.append(" ").append(timing); }
        else if ("정각".equals(timing) && minutes > 0) { sb.append(" ").append(timing); }
        String result = sb.toString().trim();
        return result.isEmpty() ? "시간 정보 없음" : result;
    }

    private LocalDateTime calculateScheduledTimeForMedicine(LocalDate date, Medicine medicine, int userIdForPattern) {
        LocalTime baseTime = LocalTime.NOON;
        try {
            UserPattern pattern = userPatternDao.findPatternByUserId(userIdForPattern);
            String medCondition = medicine.getMedCondition() != null ? medicine.getMedCondition().trim() : "";
            if (pattern != null) {
                // med_condition이 "아침", "점심", "저녁" 등을 포함하는지 확인하여 UserPattern의 해당 시간 사용
                if (medCondition.contains("아침") || (medCondition.equals("식사") && medicine.getMedDailyAmount() > 0 )) { // "식사"는 아침을 기본으로 가정
                    baseTime = (pattern.getBreakfastStartTime() != null && !pattern.getBreakfastStartTime().equals(":")) ? LocalTime.parse(pattern.getBreakfastStartTime()) : LocalTime.of(8,0);
                } else if (medCondition.contains("점심")) {
                    baseTime = (pattern.getLunchStartTime() != null && !pattern.getLunchStartTime().equals(":")) ? LocalTime.parse(pattern.getLunchStartTime()) : LocalTime.of(12,0);
                } else if (medCondition.contains("저녁")) {
                    baseTime = (pattern.getDinnerStartTime() != null && !pattern.getDinnerStartTime().equals(":")) ? LocalTime.parse(pattern.getDinnerStartTime()) : LocalTime.of(18,0);
                } else if (medCondition.contains("수면") || medCondition.contains("취침")) {
                    baseTime = (pattern.getSleepStartTime() != null && !pattern.getSleepStartTime().equals(":")) ? LocalTime.parse(pattern.getSleepStartTime()) : LocalTime.of(22,0);
                }
            } else {
                if (medCondition.contains("아침")) baseTime = LocalTime.of(8, 0);
                else if (medCondition.contains("점심")) baseTime = LocalTime.of(12, 0);
                else if (medCondition.contains("저녁")) baseTime = LocalTime.of(18, 0);
                else if (medCondition.contains("수면") || medCondition.contains("취침")) baseTime = LocalTime.of(22,0);
            }
        } catch (SQLException e) { System.err.println("UserPattern 조회 오류: " + e.getMessage());
        } catch (Exception e) { System.err.println("시간 변환 오류 ("+ medicine.getMedCondition() +"): " + e.getMessage()); }
        String medTiming = medicine.getMedTiming() != null ? medicine.getMedTiming() : "";
        int medMinutes = medicine.getMedMinutes();
        if ("전".equals(medTiming)) { baseTime = baseTime.minusMinutes(medMinutes); }
        else if ("후".equals(medTiming)) { baseTime = baseTime.plusMinutes(medMinutes); }
        return LocalDateTime.of(date, baseTime);
    }
}