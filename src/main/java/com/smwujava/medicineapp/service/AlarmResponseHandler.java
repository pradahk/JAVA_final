package com.smwujava.medicineapp.service;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.model.DosageRecord;
import com.smwujava.medicineapp.service.AlarmManager; // 이 부분은 AlarmManager가 static 메서드를 가지고 있다고 가정합니다.

import java.time.LocalDateTime;

public class AlarmResponseHandler {

    private DosageRecordDao dosageRecordDao; // DosageRecordDao 인스턴스 필드 추가

    // 생성자를 통해 DosageRecordDao 인스턴스를 주입받도록 변경
    public AlarmResponseHandler(DosageRecordDao dosageRecordDao) {
        this.dosageRecordDao = dosageRecordDao;
    }

    /**
     * 사용자의 응답에 따라 알림을 처리하고 복용 기록을 DB에 저장합니다.
     * 이 메서드는 이제 static이 아니므로 AlarmResponseHandler 인스턴스를 통해 호출해야 합니다.
     *
     * @param response       사용자 선택 ("1", "2", "3")
     * @param userId         사용자 ID
     * @param medId          약 ID
     * @param scheduledTime  원래 예정된 복용 시각
     */
    public void handleUserResponse(String response, int userId, int medId, LocalDateTime scheduledTime) { // static 제거
        switch (response) {
            case "1":  // 지금 먹을게요
                AlarmManager.cancelAlarm(medId);  // 알람 종료

                DosageRecord record1 = new DosageRecord(userId, medId, scheduledTime);
                record1.setActualTakenTime(LocalDateTime.now());
                record1.setSkipped(false);
                try {
                    // 이제 인스턴스를 통해 호출합니다.
                    this.dosageRecordDao.insertDosageRecord(record1); // DosageRecordDao.insertDosageRecord -> this.dosageRecordDao.insertDosageRecord
                    System.out.println("복용 기록이 저장되었습니다.");
                } catch (Exception e) {
                    System.out.println("복용 기록 저장 실패: " + e.getMessage());
                }
                break;

            case "2":  // 좀 있다가 먹을게요
                AlarmManager.rescheduleAlarm(medId, LocalDateTime.now().plusMinutes(5));
                System.out.println("5분 후에 다시 알림이 울릴 예정입니다.");
                break;

            case "3":  // 오늘은 스킵할게요
                DosageRecord record2 = new DosageRecord(userId, medId, scheduledTime);
                record2.setActualTakenTime(LocalDateTime.now());
                record2.setSkipped(true);
                try {
                    // 이제 인스턴스를 통해 호출합니다.
                    this.dosageRecordDao.insertDosageRecord(record2); // DosageRecordDao.insertDosageRecord -> this.dosageRecordDao.insertDosageRecord
                    System.out.println("스킵 기록이 저장되었습니다.");
                } catch (Exception e) {
                    System.out.println("스킵 기록 저장 실패: " + e.getMessage());
                }
                break;

            default:
                System.out.println("유효하지 않은 선택입니다. 1, 2, 3 중 하나를 입력해주세요.");
        }
    }
}