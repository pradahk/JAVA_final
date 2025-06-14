package com.smwujava.medicineapp.ui.alerts;

import com.smwujava.medicineapp.model.DosageRecord;
import com.smwujava.medicineapp.service.AlarmResponseHandler;
import com.smwujava.medicineapp.test.AlarmPopupWindow;
import javax.swing.*;
import java.time.LocalDateTime;

public class AlarmPopup {
    public static void show(JFrame parentFrame, DosageRecord record, String medName, AlarmResponseHandler handler) {
        SwingUtilities.invokeLater(() -> new AlarmPopupWindow(parentFrame, record, medName, handler));
    }
    public static void showAdjustedNotification(int userId, int medId, LocalDateTime originalTime, LocalDateTime adjustedTime) { /* 기존 코드 유지 */ }
}