package com.smwujava.medicineapp.dao;

import com.smwujava.medicineapp.model.DosageRecord;

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

    void updateRescheduledTime(int userId, int medId, LocalDateTime scheduledTime, LocalDateTime rescheduledTime);

}

