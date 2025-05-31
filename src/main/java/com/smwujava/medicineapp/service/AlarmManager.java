package com.smwujava.medicineapp.service;

import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import com.smwujava.medicineapp.ui.alerts.AlarmPopup;

public class AlarmManager {

    private static final Timer timer = new Timer();

    public static void triggerAlarm(int userId, int medId, LocalDateTime scheduledTime) {
        // 알람 조건이 충족되면 알림창 띄움
        AlarmPopup.show(userId, medId, scheduledTime);
    }

    public static void cancelAlarm(int medId) {
        // 실제 알람 시스템이 있다면 타이머 task 취소 코드 필요
        System.out.println("알람이 종료되었습니다.");
    }

    public static void rescheduleAlarm(int medId, LocalDateTime newTime) {
        long delayMillis = java.sql.Timestamp.valueOf(newTime).getTime() - System.currentTimeMillis();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("[재알림] 복용하실 시간입니다! (재알림)");
            }
        }, delayMillis);
    }

    // 새로 추가된 알람 예약 기능
    public static void scheduleAlarm(int userId, int medId, LocalDateTime time) {
        long delayMillis = java.sql.Timestamp.valueOf(time).getTime() - System.currentTimeMillis();

        if (delayMillis <= 0) {
            System.out.println("이미 지난 시간이므로 알람 예약 생략: " + time);
            return;
        }

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("[알림] User " + userId + "님, 약(" + medId + ")을 복용할 시간입니다!");
            }
        }, delayMillis);

        System.out.println("알람 예약 완료 → userId=" + userId + ", medId=" + medId + ", 시간=" + time);
    }
}
