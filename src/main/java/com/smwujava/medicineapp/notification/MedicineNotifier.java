package com.smwujava.medicineapp.notification;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.service.AlarmResponseHandler;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

public class MedicineNotifier {
    private final Timer timer = new Timer();

    /**
     * 알림 예약 메서드 - 사용자의 복용 알림을 일정 시간 후에 표시함
     * @param message 알림 문구
     * @param delayMillis 지연 시간 (밀리초)
     * @param userId 사용자 ID
     * @param medId 약 ID
     * @param scheduledTime 원래 알림 예정 시간
     */
    public void scheduleNotification(String message, long delayMillis, int userId, int medId, LocalDateTime scheduledTime) {
        timer.schedule(new TimerTask() {
            public void run() {
                showPopup(message, userId, medId, scheduledTime);
            }
        }, delayMillis);
    }

    /**
     * 팝업 표시 및 사용자 응답 처리
     * @param message 알림 문구
     * @param userId 사용자 ID
     * @param medId 약 ID
     * @param scheduledTime 원래 알림 예정 시간
     */
    private void showPopup(String message, int userId, int medId, LocalDateTime scheduledTime) {
        String[] options = {"지금 먹을게요", "좀 있다가", "오늘은 스킵할게요"};
        int choice = JOptionPane.showOptionDialog(
                null,
                message,
                "약 복용 알림",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        AlarmResponseHandler handler = new AlarmResponseHandler(new DosageRecordDao());
        switch (choice) {
            case 0 -> handler.handleUserResponse("1", userId, medId, scheduledTime);
            case 1 -> handler.handleUserResponse("2", userId, medId, scheduledTime);
            case 2 -> handler.handleUserResponse("3", userId, medId, scheduledTime);
            default -> System.out.println("사용자가 응답하지 않았습니다.");
        }
    }

    // 예약된 모든 알림 취소
    public void cancelAll() {
        timer.cancel();
    }
}
