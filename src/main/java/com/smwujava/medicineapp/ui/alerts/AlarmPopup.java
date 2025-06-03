package com.smwujava.medicineapp.ui.alerts;

import javax.swing.*;
import java.time.LocalDateTime;
import com.smwujava.medicineapp.test.AlarmPopupWindow;

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
    public static void show(JFrame parentFrame, int userId, int medId, LocalDateTime scheduledTime, String medName) {
        SwingUtilities.invokeLater(() -> {
            new AlarmPopupWindow(parentFrame, userId, medId, medName, scheduledTime);
        });
    }
}
