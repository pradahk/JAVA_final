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

/**
 * 사용자의 실제 복용시간과 알람 시간의 편차를 분석해
 * 다음 알람 시간을 자동으로 보정해주는 서비스 클래스
 */
public class SuggestAdjustedTime {

    private DosageRecordDao dosageRecordDao;
    private static final int ANALYSIS_WINDOW_DAYS = 7;

    public SuggestAdjustedTime(DosageRecordDao dosageRecordDao) {
        this.dosageRecordDao = dosageRecordDao;
    }

    public int suggestAndApplyAdjustedTime(int userId, int medId) {
        // 1. 분석을 위한 최근 복용 기록 조회
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

        // 2. 최근 기록을 바탕으로 시간대별 보정 시간 계산
        // <<< 이 부분에서 suggestByTimeSlot 메서드를 호출합니다. >>>
        Map<LocalTime, LocalTime> adjustedTimeMap = suggestByTimeSlot(recentRecords);

        // 3. 보정 시간을 적용할 미래 복용 기록 조회
        List<DosageRecord> futureRecords = null;
        try {
            LocalDate today = LocalDate.now();
            LocalDate futureEndDate = today.plusDays(30);
            futureRecords = dosageRecordDao.findRecordsByUserIdAndDateRange(userId, today.toString(), futureEndDate.toString());
        } catch (SQLException e) {
            System.err.println("Error fetching future records for adjustment application: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }

        if (futureRecords == null || futureRecords.isEmpty()) {
            System.out.println("No future records found for user " + userId + ", med " + medId + " to apply adjustment.");
            return 0;
        }

        // 4. 계산된 보정 시간을 미래 기록에 적용하고 DB 업데이트
        return applyRescheduledTimesToFutureRecords(futureRecords, adjustedTimeMap);
    }

    /**
     * (누락된 메서드) 사용자의 복용 기록을 바탕으로 시간대별 알람 시간을 보정하여 제안합니다.
     * 이 메서드는 groupByScheduledTime과 suggestAdjustedTimeForSlot을 사용하여
     * 최종적으로 시간대별 보정된 시간 맵을 생성합니다.
     *
     * @param records 복용 기록 리스트( scheduledTime, actualTakenTime)
     * @return 보정된 알람 시간 맵 (KEY: 원래 예정 시간, VALUE: 보정된 시간)
     */
    // <<< 이 부분이 누락되었던 메서드입니다. 이전에 주신 코드를 기반으로 복원합니다. >>>
    public Map<LocalTime, LocalTime> suggestByTimeSlot(List<DosageRecord> records) {

        // LocalTime 단위로 묶은 그룹 객체
        Map<LocalTime, List<DosageRecord>> grouped = groupByScheduledTime(records);

        // KEY: scheduledTime, VALUE: 보정된 시간
        Map<LocalTime, LocalTime> adjustedMap = new HashMap<>();


        for (LocalTime scheduledTime : grouped.keySet()) {
            // 해당 시간대의 복용 기록 리스트 저장
            List<DosageRecord> slotRecords = grouped.get(scheduledTime);

            // 복용 기록 분산해서 보정된 시간 제안
            LocalTime adjusted = suggestAdjustedTimeForSlot(slotRecords);

            // (원래시간 , 보정시간) 식으로 맵 저장
            // suggestAdjustedTimeForSlot이 null을 반환할 수 있으므로, null이 아닌 경우에만 put
            if (adjusted != null) {
                adjustedMap.put(scheduledTime, adjusted);
            }
        }
        return adjustedMap;
    }

    /**
     * 주어진 복용 기록 리스트를 시각(LocalTime) 기준으로 그룹화하여 반환합니다.
     * @param records 그룹화할 복용 기록 리스트
     * @return 시각을 키로, 해당 시각의 복용 기록 리스트를 값으로 하는 맵
     */
    private Map<LocalTime, List<DosageRecord>> groupByScheduledTime(List<DosageRecord> records) {
        Map<LocalTime, List<DosageRecord>> grouped = new HashMap<>();

        for (DosageRecord record : records) {
            if (record.getScheduledTime() == null) {
                continue;
            }
            LocalDateTime scheduledDateTime = record.getScheduledTime();
            LocalTime timeKey = scheduledDateTime.toLocalTime();

            grouped.computeIfAbsent(timeKey, k -> new ArrayList<>()).add(record);
        }
        return grouped;
    }

    /**
     * 시간대별로 그룹화된 복용 기록을 바탕으로 보정된 시간을 제안합니다.
     * (최소 4개 이상의 유효 기록이 있고, 평균 편차가 15분 이상/이하일 경우에만 보정)
     * @param records 해당 시간대의 복용 기록 리스트 (실제 복용 시간 포함)
     * @return 보정된 LocalTime 객체, 보정이 필요 없으면 원래 시간, 기록이 없으면 null
     */
    private LocalTime suggestAdjustedTimeForSlot(List<DosageRecord> records) {
        long totalOffsetMinutes = 0;
        int count = 0;

        for (DosageRecord record : records) {
            LocalDateTime scheduled = record.getScheduledTime();
            LocalDateTime actual = record.getActualTakenTime();

            if (actual != null && scheduled != null) {
                long offset = Duration.between(scheduled, actual).toMinutes();
                totalOffsetMinutes += offset;
                count++;
            }
        }

        if (count == 0) {
            return null;
        }
        if (count < 4) {
            return records.get(0).getScheduledTime().toLocalTime();
        }

        long avgOffset = totalOffsetMinutes / count;
        LocalTime baseTime = records.get(0).getScheduledTime().toLocalTime();

        if (avgOffset >= 15 || avgOffset <= -15) {
            return baseTime.plusMinutes(avgOffset);
        } else {
            return baseTime;
        }
    }

    /**
     * 계산된 보정 시간을 미래의 복용 기록에 실제로 적용하고 데이터베이스를 업데이트합니다.
     * @param futureRecords 보정 시간을 적용할 미래 복용 기록 리스트
     * @param adjustedMap   (원래 시간, 보정 시간) 맵
     * @return 업데이트된 기록의 수
     */
    private int applyRescheduledTimesToFutureRecords(List<DosageRecord> futureRecords, Map<LocalTime, LocalTime> adjustedMap) {
        int updatedCount = 0;
        for (DosageRecord record : futureRecords) {
            if (record.getActualTakenTime() != null) {
                continue;
            }

            LocalTime originalTime = record.getScheduledTime().toLocalTime();
            LocalTime newTime = adjustedMap.get(originalTime);

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
                    }
                } catch (SQLException e) {
                    System.err.println("Error applying rescheduled time for record ID " + record.getRecordId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return updatedCount;
    }

    /**
     * 특정 사용자(userId)의 모든 복용 기록에서 재조정된 알람 시간을 NULL로 초기화합니다.
     * @param userId 재조정 시간을 초기화할 사용자의 ID
     * @return 초기화 성공 여부 (true: 성공, false: 실패)
     */
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