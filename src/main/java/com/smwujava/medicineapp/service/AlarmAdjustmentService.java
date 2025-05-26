package com.smwujava.medicineapp.service;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.model.DosageRecord;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class AlarmAdjustmentService {

    private static final int THRESHOLD_COUNT = 4; // 주당 최소 4회 이상
    private static final int ANALYSIS_DAYS = 7;   // 최근 7일

    /**
     * 최근 복약 기록을 분석하여 반복적으로 지연된 시간이 있다면 알림 시각을 조정 제안함.
     *
     * @param userId 사용자 ID
     * @param medId  약 ID
     * @return Optional<조정된 알림 시각> (조건 만족 시), 만족하지 않으면 Optional.empty()
     */
    public static Optional<LocalDateTime> suggestAdjustedTime(int userId, int medId) {
        List<DosageRecord> records;
        try {
            records = DosageRecordDao.getRecentDosageRecords(userId, medId, ANALYSIS_DAYS);
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }

        Map<Duration, Integer> delayFrequency = new HashMap<>();

        for (DosageRecord record : records) {
            LocalDateTime scheduled = record.getScheduledTime();
            LocalDateTime taken = record.getActualTakenTime();

            if (scheduled == null || taken == null) continue;

            Duration delay = Duration.between(scheduled, taken);

            if (!delay.isNegative() && !delay.isZero()) {
                delayFrequency.put(delay, delayFrequency.getOrDefault(delay, 0) + 1);
            }
        }

        for (Map.Entry<Duration, Integer> entry : delayFrequency.entrySet()) {
            if (entry.getValue() >= THRESHOLD_COUNT) {
                return Optional.of(LocalDateTime.now().withHour(1).plus(entry.getKey()));
            }
        }

        return Optional.empty();
    }
}