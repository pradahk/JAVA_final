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
            // DosageRecordDao에서 실제 복용했으며 건너뛰지 않은 기록만 가져오도록 이미 수정되었습니다.
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
            return baseTime; // 보정 필요 없음
        }
    }

    public LocalDateTime getAdjustedTime(int userId, int medId, LocalDateTime scheduledTime) {
        DosageRecordDao dao = new DosageRecordDao();

        try {
            // 최근 7일간 복용 기록 조회
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(7);
            List<DosageRecord> records = dao.findRecordsByUserIdAndDateRange(userId, startDate.toString(), endDate.toString());

            long totalOffset = 0;
            int count = 0;

            for (DosageRecord record : records) {
                if (record.getMedId() == medId &&
                        record.getScheduledTime() != null &&
                        record.getActualTakenTime() != null &&
                        !record.isSkipped()) {

                    long offset = Duration.between(record.getScheduledTime(), record.getActualTakenTime()).toMinutes();
                    if (Math.abs(offset) <= 15) {  // 허용 편차 이내
                        totalOffset += offset;
                        count++;
                    }
                }
            }

            // 평균 지연시간이 존재하고 4회 이상이면 보정
            if (count >= 4) {
                long averageOffset = totalOffset / count;
                return scheduledTime.plusMinutes(averageOffset);
            }

        } catch (SQLException e) {
            System.err.println("알람 시간 보정 실패: " + e.getMessage());
        }

        // 보정하지 않고 원래 시간 그대로 반환
        return scheduledTime;
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
                            record.getScheduledTime(), // 원본 scheduledTime을 기준으로 업데이트
                            rescheduled // 새로 계산된 rescheduledTime 설정
                    );
                    if (success) {
                        updatedCount++;
                        // 모델 객체도 업데이트 (선택 사항, 그러나 일관성을 위해 권장)
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

    public boolean resetAdjustedTimes(int userId) {
        try {
            boolean success = dosageRecordDao.resetAllRescheduledTimes(userId);
            if (!success) {
                System.out.println("No rescheduled times found to reset for user ID: " + userId);
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error resetting all adjusted times for user ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}