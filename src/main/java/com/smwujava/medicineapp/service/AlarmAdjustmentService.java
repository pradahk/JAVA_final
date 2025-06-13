package com.smwujava.medicineapp.service;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.model.DosageRecord;
import com.smwujava.medicineapp.dao.UserPatternDao;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate; // LocalDate 임포트 추가
import java.time.LocalDateTime;
import java.util.*;

public class AlarmAdjustmentService {

    private static UserPatternDao userPatternDao = new UserPatternDao();

    public static LocalDateTime adjustAlarmBasedOnPattern(int userId, int medId, LocalDateTime scheduledTime) {
        int lateCount = userPatternDao.getLateCountLastWeek(userId);
        int averageDelay = userPatternDao.getAverageDelayMinutesByUser(userId);

        if (lateCount >= 4) {
            // 평균 지연 시간만큼 뒤로 미룸
            LocalDateTime adjustedTime = scheduledTime.plusMinutes(averageDelay);
            System.out.println("🔔 알람 조정: " + scheduledTime + " → " + adjustedTime + " (" + averageDelay + "분 지연)");
            return adjustedTime;
        } else {
            return scheduledTime;
        }
    }

    private DosageRecordDao dosageRecordDao; // DosageRecordDao 인스턴스 필드 추가
    private static final int THRESHOLD_COUNT = 4; // 주당 최소 4회 이상
    private static final int ANALYSIS_DAYS = 7;   // 최근 7일

    public AlarmAdjustmentService(DosageRecordDao dosageRecordDao) {
        this.dosageRecordDao = dosageRecordDao;
    }

    /**
     * 최근 복약 기록을 분석하여 반복적으로 지연된 시간이 있다면 알림 시각을 조정 제안함.
     * @param userId 사용자 ID
     * @param medId  약 ID
     * @return Optional<조정된 알림 시각> (조건 만족 시), 만족하지 않으면 Optional.empty()
     */
    public Optional<LocalDateTime> suggestAdjustedTime(int userId, int medId) { // static 메서드 제거
        List<DosageRecord> records;
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(ANALYSIS_DAYS);

            records = dosageRecordDao.findRecordsByUserIdAndDateRange(userId, startDate.toString(), endDate.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }

        // 특정 medId에 대한 기록만 필터링 (findRecordsByUserIdAndDateRange는 특정 약에 대한 필터링이 없으므로 직접 수행)
        List<DosageRecord> filteredRecords = new ArrayList<>();
        for (DosageRecord record : records) {
            if (record.getMedId() == medId) {
                filteredRecords.add(record);
            }
        }

        Map<Duration, Integer> delayFrequency = new HashMap<>();

        for (DosageRecord record : filteredRecords) { // 필터링된 기록 사용
            LocalDateTime scheduled = record.getScheduledTime();
            LocalDateTime taken = record.getActualTakenTime();

            if (scheduled == null || taken == null) continue;

            Duration delay = Duration.between(scheduled, taken);

            if (Math.abs(delay.toMinutes()) > 15) {
                continue; // 15분 이상 편차 나는 기록은 분석에서 제외
            }

            if (!delay.isNegative() && !delay.isZero()) {
                delayFrequency.put(delay, delayFrequency.getOrDefault(delay, 0) + 1);
            }
        }

        Duration mostFrequentDelay = null;
        int maxFrequency = 0;

        for (Map.Entry<Duration, Integer> entry : delayFrequency.entrySet()) {
            if (entry.getValue() >= THRESHOLD_COUNT) {
                if (entry.getValue() > maxFrequency) {
                    maxFrequency = entry.getValue();
                    mostFrequentDelay = entry.getKey();
                }
            }
        }

        if (mostFrequentDelay != null) {
            return Optional.of(LocalDateTime.now().plus(mostFrequentDelay));
        }

        return Optional.empty();
    }
}