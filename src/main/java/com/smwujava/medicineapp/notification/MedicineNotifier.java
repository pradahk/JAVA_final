package com.smwujava.medicineapp.notification;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.service.AlarmManager;
import com.smwujava.medicineapp.service.AlarmResponseHandler;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

public class MedicineNotifier {

    private final Timer timer = new Timer();

    /**
     * 알림 예약 - 일정 시간 뒤 사용자에게 팝업 띄우기
     */
    public void scheduleNotification(long delayMillis, int userId, int medId, LocalDateTime scheduledTime) {
        timer.schedule(new TimerTask() {
            public void run() {
                showPopup(userId, medId, scheduledTime);
            }
        }, delayMillis);
    }

    /**
     * 팝업 알림 띄우고 사용자 응답 처리
     */
    private void showPopup(int userId, int medId, LocalDateTime scheduledTime) {
        String[] options = {"지금 먹을게요", "좀 있다가", "오늘은 스킵할게요"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "약 복용 시간입니다! 선택해주세요.",
                "약 복용 알림",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        // 응답 핸들러 호출
        AlarmResponseHandler handler = new AlarmResponseHandler(new DosageRecordDao());

        switch (choice) {
            case 0 -> {
                handler.handleUserResponse("1", userId, medId, scheduledTime);
                AlarmManager.cancelAlarm(medId);
            }
            case 1 -> {
                handler.handleUserResponse("2", userId, medId, scheduledTime);
                AlarmManager.rescheduleAlarm(medId, LocalDateTime.now().plusMinutes(5));
            }
            case 2 -> {
                handler.handleUserResponse("3", userId, medId, scheduledTime);
                AlarmManager.cancelAlarm(medId);
            }
            default -> System.out.println("사용자가 알림을 무시했습니다.");
        }
    }

    // 전체 예약 취소 (필요시)
    public void cancelAll() {
        timer.cancel();
    }
}