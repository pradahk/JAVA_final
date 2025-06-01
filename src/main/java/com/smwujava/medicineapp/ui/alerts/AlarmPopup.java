package com.smwujava.medicineapp.ui.alerts;

import com.smwujava.medicineapp.service.AlarmResponseHandler;
import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.service.AlarmManager;

import javax.swing.*;
import java.time.LocalDateTime;

public class AlarmPopup {
    public static void show(int userId, int medId, LocalDateTime scheduledTime) {
        Object[] options = {"지금 먹을게요", "좀 있다가 먹을게요", "오늘은 스킵할게요"};

        int choice = JOptionPane.showOptionDialog(
                null,
                "약 복용 시간입니다!\n어떻게 하시겠어요?",
                "복용 알림",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        String response = switch (choice) {
            case 0 -> "1"; // 지금
            case 1 -> {
                AlarmManager.snoozeAlarm(userId, medId, 5); // 5분 뒤 재알람
                yield "2";
            }
            case 2 -> {
                AlarmManager.cancelAlarm(medId); // 오늘 알람 취소
                yield "3";
            }
            default -> "3"; // 창 닫으면 스킵 처리
        };

        // 응답 처리
        DosageRecordDao dao = new DosageRecordDao();
        AlarmResponseHandler handler = new AlarmResponseHandler(dao);
        handler.handleUserResponse(response, userId, medId, scheduledTime);
    }
}