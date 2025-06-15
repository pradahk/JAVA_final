package com.smwujava.medicineapp.service;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.model.DosageRecord;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuggestAdjustedTime { // 파일명 변경에 따라 클래스명 조정

    private DosageRecordDao dosageRecordDao;
    private static final int ANALYSIS_WINDOW_DAYS = 7;
    private static final int ACCEPTABLE_OFFSET_MINUTES = 15; // 추가: 허용 가능한 시간 편차 (15분)

    public SuggestAdjustedTime(DosageRecordDao dosageRecordDao) {
        this.dosageRecordDao = dosageRecordDao;
    }

    public int suggestAndApplyAdjustedTime(int userId, int medId) {
        List<DosageRecord> recentRecords = null;
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(ANALYSIS_WINDOW_DAYS);
            recentRecords = dosageRecordDao.findRecordsByUserIdAndDateRange(userId, startDate.toString(), endDate.toString());
        } catch (SQLException e) {
            System.err.println("Error fetching recent records for adjustment analysis: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }

        if (recentRecords == null || recentRecords.isEmpty()) {
            System.out.println("No recent records found for user " + userId + ", med " + medId + " to analyze.");
            return 0;
        }

        Map<LocalTime, LocalTime> adjustedTimeMap = suggestByTimeSlot(recentRecords);

        List<DosageRecord> futureRecords = null;
        try {
            LocalDate today = LocalDate.now();
            LocalDate futureEndDate = today.plusDays(30);
            futureRecords = dosageRecordDao.findRecordsByUserIdAndDateRangeForFuture(userId, today.toString(), futureEndDate.toString());
        } catch (SQLException e) {
            System.err.println("Error fetching future records for adjustment application: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }

        if (futureRecords == null || futureRecords.isEmpty()) {
            System.out.println("No future records found for user " + userId + ", med " + medId + " to apply adjustment.");
            return 0;
        }

        return applyRescheduledTimesToFutureRecords(futureRecords, adjustedTimeMap);
    }

    public Map<LocalTime, LocalTime> suggestByTimeSlot(List<DosageRecord> records) {
        Map<LocalTime, List<DosageRecord>> grouped = groupByScheduledTime(records);
        Map<LocalTime, LocalTime> adjustedMap = new HashMap<>();

        for (LocalTime scheduledTime : grouped.keySet()) {
            List<DosageRecord> slotRecords = grouped.get(scheduledTime);
            LocalTime adjusted = suggestAdjustedTimeForSlot(slotRecords);

            if (adjusted != null) {
                adjustedMap.put(scheduledTime, adjusted);
            }
        }
        return adjustedMap;
    }

    private Map<LocalTime, List<DosageRecord>> groupByScheduledTime(List<DosageRecord> records) {
        Map<LocalTime, List<DosageRecord>> grouped = new HashMap<>();

        for (DosageRecord record : records) {
            // scheduledTime이 null이 아닌 경우만 처리
            if (record.getScheduledTime() == null) {
                continue;
            }
            LocalDateTime scheduledDateTime = record.getScheduledTime();
            LocalTime timeKey = scheduledDateTime.toLocalTime();

            grouped.computeIfAbsent(timeKey, k -> new ArrayList<>()).add(record);
        }
        return grouped;
    }

    private LocalTime suggestAdjustedTimeForSlot(List<DosageRecord> records) {
        long totalOffsetMinutes = 0;
        int count = 0;

        for (DosageRecord record : records) {
            LocalDateTime scheduled = record.getScheduledTime();
            LocalDateTime actual = record.getActualTakenTime();

            if (actual != null && scheduled != null && !record.isSkipped()) {
                long offset = Duration.between(scheduled, actual).toMinutes();

                // 알람이 울려도 15분이 넘은 시간에 먹은 것은 카운트하지 않음 (절대값으로 비교)
                if (Math.abs(offset) <= ACCEPTABLE_OFFSET_MINUTES) {
                    totalOffsetMinutes += offset;
                    count++;
                }
            }
        }

        if (count == 0) {
            return null; // 유효한 기록이 없으면 보정할 수 없음
        }
        if (count < 4) { // 최소 4개 이상의 유효 기록 조건
            return records.get(0).getScheduledTime().toLocalTime();
        }

        long avgOffset = totalOffsetMinutes / count;
        LocalTime baseTime = records.get(0).getScheduledTime().toLocalTime();

        // 평균 편차가 15분 이상/이하일 경우에만 보정
        if (Math.abs(avgOffset) >= ACCEPTABLE_OFFSET_MINUTES) {
            return baseTime.plusMinutes(avgOffset);
        } else {
            return baseTime;
        }
    }

    private int applyRescheduledTimesToFutureRecords(List<DosageRecord> futureRecords, Map<LocalTime, LocalTime> adjustedMap) {
        int updatedCount = 0;
        for (DosageRecord record : futureRecords) {
            // 실제 복용 시간이 이미 있거나 (isTaken() 활용), 건너뛴 기록은 재조정하지 않음
            if (record.isTaken() || record.isSkipped()) {
                continue;
            }

            LocalTime originalTime = record.getScheduledTime().toLocalTime();
            LocalTime newTime = adjustedMap.get(originalTime);

            // 보정된 시간이 있고, 원래 시간과 다를 경우에만 업데이트
            if (newTime != null && !newTime.equals(originalTime)) {
                LocalDateTime rescheduled = LocalDateTime.of(record.getScheduledTime().toLocalDate(), newTime);

                try {
                    boolean success = dosageRecordDao.updateRescheduledTime(
                            record.getUserId(),
                            record.getMedId(),
                            record.getScheduledTime(),
                            rescheduled
                    );
                    if (success) {
                        updatedCount++;
                        record.setRescheduledTime(rescheduled);
                    }
                } catch (SQLException e) {
                    System.err.println("Error applying rescheduled time for record ID " + record.getRecordId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return updatedCount;
    }
}