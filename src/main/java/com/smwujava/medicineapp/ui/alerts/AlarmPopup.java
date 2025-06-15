package com.smwujava.medicineapp.ui.alerts;

import javax.swing.*;
import java.time.LocalDateTime;
import com.smwujava.medicineapp.test.AlarmPopupWindow;

public class AlarmPopup {
    public static void show(JFrame parentFrame, int userId, int medId, LocalDateTime scheduledTime, String medName) {
        SwingUtilities.invokeLater(() -> {
            new AlarmPopupWindow(parentFrame, userId, medId, medName, scheduledTime);
        });
    }
}