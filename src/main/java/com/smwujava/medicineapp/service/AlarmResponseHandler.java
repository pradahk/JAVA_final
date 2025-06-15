package com.smwujava.medicineapp.service;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.model.DosageRecord;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class AlarmResponseHandler {

    private final DosageRecordDao dosageRecordDao;

    public AlarmResponseHandler(DosageRecordDao dosageRecordDao) {
        this.dosageRecordDao = dosageRecordDao;
    }

    /**
     * 알람 팝업에 대한 사용자의 응답을 받아 처리합니다.
     *
     * @param response      사용자 선택 ("1", "2", "3")
     * @param userId        사용자 ID
     * @param medId         약 ID
     * @param scheduledTime 원래 예정된 복용 시각
     */
    public void handleUserResponse(String response, int userId, int medId, LocalDateTime scheduledTime) {
        switch (response) {
            case "1":  // "지금 먹을게요"
                // 혹시 모를 재알람을 확실히 방지하기 위해 알람을 취소합니다.
                AlarmManager.cancelAlarm(medId);

                // 복용 기록을 생성하고 DB에 저장합니다.
                DosageRecord takenRecord = new DosageRecord(userId, medId, scheduledTime);
                takenRecord.setActualTakenTime(LocalDateTime.now());
                takenRecord.setSkipped(false);
                saveRecord(takenRecord, "복용");
                break;

            case "2":  // "좀 있다가 먹을게요"
                // AlarmManager에 5분 뒤 재알람을 요청합니다.
                AlarmManager.rescheduleAlarm(null, userId, medId, LocalDateTime.now().plusMinutes(5));
                System.out.println("5분 후에 다시 알림이 울릴 예정입니다.");
                break;

            case "3":  // "오늘은 건너뛸게요"
                // 개선점: 건너뛰기를 선택했으므로, 재알람 예약을 확실히 취소합니다.
                AlarmManager.cancelAlarm(medId);

                // 건너뛰기 기록을 생성하고 DB에 저장합니다.
                DosageRecord skippedRecord = new DosageRecord(userId, medId, scheduledTime);
                skippedRecord.setSkipped(true);
                saveRecord(skippedRecord, "건너뛰기");
                break;

            default:
                System.out.println("유효하지 않은 선택입니다: " + response);
        }
    }

    /**
     * DosageRecord를 데이터베이스에 저장하는 공통 메서드
     * @param record 저장할 복용 기록
     * @param type   로그에 표시할 기록 타입 (예: "복용", "건너뛰기")
     */
    private void saveRecord(DosageRecord record, String type) {
        try {
            dosageRecordDao.updateDosageRecord(record); // 기존 기록을 업데이트하는 방식이 더 적합할 수 있습니다.
            System.out.println(type + " 기록이 저장되었습니다.");
        } catch (Exception e) {
            System.err.println(type + " 기록 저장 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}