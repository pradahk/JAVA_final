package com.smwujava.medicineapp.service;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.model.DosageRecord;
import java.time.LocalDateTime;

public class AlarmResponseHandler {

    private final DosageRecordDao dosageRecordDao;

    public AlarmResponseHandler(DosageRecordDao dosageRecordDao) {
        this.dosageRecordDao = dosageRecordDao;
    }

    // 알람 팝업에 대한 사용자의 응답을 받아 처리
    public void handleUserResponse(String response, int userId, int medId, LocalDateTime scheduledTime) {
        switch (response) {
            case "1":  // "지금 먹을게요"
                AlarmManager.cancelAlarm(medId);
                DosageRecord takenRecord = new DosageRecord(userId, medId, scheduledTime);
                takenRecord.setActualTakenTime(LocalDateTime.now());
                takenRecord.setSkipped(false);
                saveRecord(takenRecord, "복용");
                break;

            case "2":  // "좀 있다가 먹을게요"
                AlarmManager.rescheduleAlarm(null, userId, medId, LocalDateTime.now().plusMinutes(5));
                System.out.println("5분 후에 다시 알림이 울릴 예정입니다.");
                break;

            case "3":  // "오늘은 건너뛸게요"
                AlarmManager.cancelAlarm(medId);
                DosageRecord skippedRecord = new DosageRecord(userId, medId, scheduledTime);
                skippedRecord.setSkipped(true);
                saveRecord(skippedRecord, "건너뛰기");
                break;

            default:
                System.out.println("유효하지 않은 선택입니다: " + response);
        }
    }

    // DosageRecord를 데이터베이스에 저장
    private void saveRecord(DosageRecord record, String type) {
        try {
            dosageRecordDao.updateDosageRecord(record);
            System.out.println(type + " 기록이 저장되었습니다.");
        } catch (Exception e) {
            System.err.println(type + " 기록 저장 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}