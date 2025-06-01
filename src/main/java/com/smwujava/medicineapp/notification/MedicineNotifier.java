package com.smwujava.medicineapp.notification;

import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

public class MedicineNotifier {
    private final Timer timer = new Timer();

    // 약 복용 알림 예약
    // message: 사용자에게 보여줄 알림 문구
    // delayMillis: 현재 시점으로부터 얼마나 뒤에 알림을 띄울지
    public void scheduleNotification(String message, long delayMillis) {
        timer.schedule(new TimerTask() {
            public void run() {
                showPopup(message);
            }
        }, delayMillis);
    }

    // 알림 즉시 표시
    private void showPopup(String message) {
        JOptionPane.showMessageDialog(null, message, "약 복용 알림", JOptionPane.INFORMATION_MESSAGE);
    }

    // 타이머 정리 (필요시)
    public void cancelAll() {
        timer.cancel();
    }
}