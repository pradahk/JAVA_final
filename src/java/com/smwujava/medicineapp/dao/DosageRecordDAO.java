package java.com.smwujava.medicineapp.dao;

import java.com.smwujava.medicineapp.model.DosageRecord;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface DosageRecordDAO {

    /**
     * 특정 사용자(userId), 특정 약(medId), 특정 시간대(scheduledTime 기준)에 대해
     * 최근 복용 기록 7개를 scheduled_time 기준 내림차순으로 가져오는 메서드
     * 최윤서 - suggestAdjustedTime.java에서 사용됨
     * 최근 일곱개의 날짜만을 받아오는 상황
     */
    List<DosageRecord> getRecentDosageRecords(
            int userId,
            int medId,
            LocalTime scheduledTime
    );

    // 새로 계산한 복용시간을 DB에 저장하는 메서드
    void updateRescheduledTime(int userId, int medId, LocalDateTime scheduledTime, LocalDateTime rescheduledTime);

    // 사용자가 생활패턴을 수정했을 떄 rescheduled_time을 null로 초기화하는 함수
    void resetAllRescheduledTimes(int userId);

}

