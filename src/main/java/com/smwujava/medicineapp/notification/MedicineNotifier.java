package com.smwujava.medicineapp.notification;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.service.AlarmManager;
import com.smwujava.medicineapp.service.AlarmResponseHandler;
import com.smwujava.medicineapp.service.AlarmAdjustmentService;
import java.util.Optional;



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
                AlarmAdjustmentService adjustmentService = new AlarmAdjustmentService(new DosageRecordDao());
                Optional<LocalDateTime> adjustedTime = adjustmentService.suggestAdjustedTime(userId, medId);
                LocalDateTime finalTime = adjustedTime.orElse(scheduledTime);

                if (adjustedTime.isPresent()) {
                    System.out.println("⏰ 알람 시각 조정됨 → " + adjustedTime.get());
                } else {
                    System.out.println("📌 기본 알람 시각 유지 → " + scheduledTime);
                }
                showPopup(userId, medId, finalTime);
            }
        }, delayMillis);
    }
    /**
     * 팝업 알림 띄우고 사용자 응답 처리 */
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

        AlarmResponseHandler handler = new AlarmResponseHandler(new DosageRecordDao());

        switch (choice) {
            case 0 -> {
                handler.handleUserResponse("1", userId, medId, scheduledTime);
                AlarmManager.cancelAlarm(medId);
            }
            case 1 -> {
                handler.handleUserResponse("2", userId, medId, scheduledTime);
                AlarmManager.rescheduleAlarm(userId, medId, LocalDateTime.now().plusMinutes(5)); // ✅

            }
            case 2 -> {
                handler.handleUserResponse("3", userId, medId, scheduledTime);
                AlarmManager.cancelAlarm(medId);
            }
            default -> System.out.println("사용자가 알림을 무시했습니다.");
        }
    }
    public void cancelAll() {
        timer.cancel();
    }
}