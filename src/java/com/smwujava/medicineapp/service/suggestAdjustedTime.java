package java.com.smwujava.medicineapp.service;

import java.com.smwujava.medicineapp.dao.DosageRecordDAO;
import java.com.smwujava.medicineapp.model.DosageRecord;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 사용자의 실제 복용시간과 알람 시간의 편차를 분석해
 * 다음 알람 시간을 자동을 보정해주는 클래스
 */
public class suggestAdjustedTime {
    /**
     * 사용자의 복용 기록을 바탕으로 알람 시간을 보정하는 메서드
     * @param records 복용 기록 리스트( scheduledTime, actualTakenTime)
     * @return 보정된 알람 시간 or 기존 알람 시간(보정이 필요 없는경우)
     */

    // 보정된 알람시간을 제안하는 메서드
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
            adjustedMap.put(scheduledTime, adjusted);
        }

        return adjustedMap;
    }

    // 복용 기록을 시각 기준으로 분류
    private Map<LocalTime, List<DosageRecord>> groupByScheduledTime(List<DosageRecord> records) {
        Map<LocalTime, List<DosageRecord>> grouped = new HashMap<>();

        for (DosageRecord record : records) {
            LocalDateTime scheduledDateTime = record.getScheduledTime();
            LocalTime timeKey = scheduledDateTime.toLocalTime();

            // 해당하는 리스트가 있으면 추가, 없으면 생성
            grouped.computeIfAbsent(timeKey, k -> new ArrayList<>()).add(record);
        }

        return grouped;
    }

    // 복용 기록을 바탕으로 보정된 시간 제안
    private LocalTime suggestAdjustedTimeForSlot(List<DosageRecord> records) {
        long totalOffsetMinutes = 0;
        int count = 0;

        for (DosageRecord record : records) {
            LocalDateTime scheduled = record.getScheduledTime();
            LocalDateTime actual = record.getActualTakenTime();

            if (actual != null) {
                long offset = Duration.between(scheduled, actual).toMinutes();
                totalOffsetMinutes += offset;
                count++;
            }
        }
        if (count == 0) return null; // 기록 없음
        if (count < 4) return records.get(0).getScheduledTime().toLocalTime(); // 최소 3개 조건

        long avgOffset = totalOffsetMinutes / count;
        LocalTime baseTime = records.get(0).getScheduledTime().toLocalTime();

        if (avgOffset >= 15 || avgOffset <= -15) {
            return baseTime.plusMinutes(avgOffset);
        } else {
            return baseTime; // 보정 필요 없음
        }
    }

    public void applyRescheduledTimes(List<DosageRecord> records, DosageRecordDAO dao) {
        Map<LocalTime, LocalTime> adjustedMap = suggestByTimeSlot(records);

        for (DosageRecord record : records) {
            LocalTime originalTime = record.getScheduledTime().toLocalTime();
            LocalTime newTime = adjustedMap.get(originalTime);

            if (newTime != null && !newTime.equals(originalTime)) {
                LocalDateTime rescheduled = LocalDateTime.of(record.getScheduledTime().toLocalDate(), newTime);
                dao.updateRescheduledTime(
                        record.getUserId(),
                        record.getMedId(),
                        record.getScheduledTime(),
                        rescheduled
                );
            }
        }
    }

}
