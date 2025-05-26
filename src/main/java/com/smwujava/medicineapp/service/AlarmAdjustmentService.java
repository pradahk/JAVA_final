package com.smwujava.medicineapp.service;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.model.DosageRecord;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate; // LocalDate 임포트 추가
import java.time.LocalDateTime;
import java.util.*;

public class AlarmAdjustmentService {

    private DosageRecordDao dosageRecordDao; // DosageRecordDao 인스턴스 필드 추가

    private static final int THRESHOLD_COUNT = 4; // 주당 최소 4회 이상
    private static final int ANALYSIS_DAYS = 7;   // 최근 7일

    // 생성자를 통해 DosageRecordDao 인스턴스를 주입받도록 수정
    public AlarmAdjustmentService(DosageRecordDao dosageRecordDao) {
        this.dosageRecordDao = dosageRecordDao;
    }

    /**
     * 최근 복약 기록을 분석하여 반복적으로 지연된 시간이 있다면 알림 시각을 조정 제안함.
     *
     * @param userId 사용자 ID
     * @param medId  약 ID
     * @return Optional<조정된 알림 시각> (조건 만족 시), 만족하지 않으면 Optional.empty()
     */
    public Optional<LocalDateTime> suggestAdjustedTime(int userId, int medId) { // static 메서드 제거
        List<DosageRecord> records;
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(ANALYSIS_DAYS);

            // DosageRecordDao 인스턴스를 통해 findRecordsByUserIdAndDateRange 메서드 호출
            // 이 메서드는 이미 is_skipped = 0 이고 actual_taken_time이 NULL이 아닌 기록만 가져오도록 되어 있습니다.
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

            // isTaken() 메서드를 사용하여 복용 여부를 판단하고, actualTakenTime이 존재하는 경우만 고려
            // isSkipped = true 인 경우는 이미 DosageRecordDao에서 필터링되었으므로 별도 체크 필요 없음.
            // 다만, 여기서 "알람이 울려도 15분이 넘은 시간에 먹은거는 카운트를 안해야 한다"는 로직을 추가하고 싶다면
            // 아래 Duration 계산 후 해당 조건 추가.
            if (scheduled == null || taken == null) continue;

            Duration delay = Duration.between(scheduled, taken);

            // "알람이 울려도 15분이 넘은 시간에 먹은 것은 카운트를 안해야 한다"는 로직을 여기에 적용
            // 즉, 편차가 15분 이내인 경우만 통계에 포함
            if (Math.abs(delay.toMinutes()) > 15) {
                continue; // 15분 이상 편차 나는 기록은 분석에서 제외
            }


            // 지연 시간만 고려 (음수는 무시, 0은 지연 없으므로 무시)
            if (!delay.isNegative() && !delay.isZero()) {
                // 정확히 같은 Duration 객체로 맵에 넣기보다는, 분 단위로 통일하여 사용하는 것이 더 좋습니다.
                // 예를 들어, 5분 지연, 6분 지연은 개별로 카운트되지만, 유사한 지연 시간을 그룹화할 수 있습니다.
                // 여기서는 일단 Duration 객체 그대로 사용합니다.
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
            // 가장 빈번한 지연 시간을 기준으로 현재 시간을 보정하여 제안합니다.
            // LocalDateTime.now()에 가장 빈번한 지연을 더하는 방식으로 변경합니다.
            // LocalDateTime.now().withHour(1)은 임의의 기준 시간으로 보정하는 것이므로,
            // 실제 사용자의 현재 알람 시간과 연관 지어 보정하는 로직이 더 합리적일 수 있습니다.
            // 여기서는 단순히 가장 빈번한 지연을 현재 시간에 더하는 것으로 예시를 듭니다.
            // 실제 알람 시간을 가져와서 보정하는 로직이 필요하다면, 해당 알람 시간을 조회하는 기능이 추가되어야 합니다.
            return Optional.of(LocalDateTime.now().plus(mostFrequentDelay));
        }

        return Optional.empty();
    }
}