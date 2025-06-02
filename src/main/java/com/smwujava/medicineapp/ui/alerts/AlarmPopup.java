package com.smwujava.medicineapp.ui.alerts;

import com.smwujava.medicineapp.service.AlarmResponseHandler;
import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.service.AlarmManager;

import javax.swing.*;
import java.time.LocalDateTime;

public class AlarmPopup {

    // 1. 복용 시간 조정 알림 (원래 시간에 표시)
    public static void showAdjustedNotification(int userId, int medId, LocalDateTime originalTime, LocalDateTime adjustedTime) {
        SwingUtilities.invokeLater(() -> {
            String message = "📢 약 복용 시간 변경 안내\n\n"
                    + "기존 복용 시간: " + originalTime.toLocalTime() + "\n"
                    + "조정된 복용 시간: " + adjustedTime.toLocalTime();

            JOptionPane.showMessageDialog(null, message, "복용 시간 조정", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    // 2. 실제 복용 알람 창
    public static void show(int userId, int medId, LocalDateTime scheduledTime) {
        Object[] options = {"지금 먹을게요", "좀 있다가 먹을게요", "오늘은 스킵할게요"};

        int choice = JOptionPane.showOptionDialog(
                null,
                "💊 약 복용 시간입니다!\n어떻게 하시겠어요?",
                "복용 알림",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        String response = switch (choice) {
            case 0 -> "1"; // 지금 복용
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

        // 3. 응답 결과 DB 반영
        DosageRecordDao dao = new DosageRecordDao();
        AlarmResponseHandler handler = new AlarmResponseHandler(dao);
        handler.handleUserResponse(response, userId, medId, scheduledTime);
    }
}

