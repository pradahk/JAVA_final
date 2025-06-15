package com.smwujava.medicineapp.service;

import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.ui.alerts.AlarmPopup;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class AlarmManager {

    private static final Timer timer = new Timer("AlarmTimer", true);
    private static final Map<Integer, TimerTask> scheduledTasks = new HashMap<>();

    // 특정 약(medId)에 대한 알람을 예약
    public static void scheduleAlarm(JFrame parentFrame, int userId, int medId, LocalDateTime time) {
        cancelAlarm(medId);

        if (time.isBefore(LocalDateTime.now())) {
            System.out.println("[AlarmManager] 알람 시간이 과거이므로 예약하지 않음: medId=" + medId + ", time=" + time);
            return;
        }

        long delayMillis = java.time.Duration.between(LocalDateTime.now(), time).toMillis();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println("[알림] User " + userId + "님, 약(" + medId + ")을 복용할 시간입니다!");
                triggerAlarm(parentFrame, userId, medId, time);
                scheduledTasks.remove(medId);
            }
        };

        timer.schedule(task, delayMillis);
        scheduledTasks.put(medId, task);
        System.out.println("[AlarmManager] 알람 예약 완료 → medId=" + medId + ", 실행 시각=" + time);
    }

    // 알람을 실제로 화면에 표시
    public static void triggerAlarm(JFrame parentFrame, int userId, int medId, LocalDateTime scheduledTime) {
        MedicineDao medicineDao = new MedicineDao();
        String medName = medicineDao.findMedicineNameById(medId);
        AlarmPopup.show(parentFrame, userId, medId, scheduledTime, medName);
    }

    // 예약된 알람을 취소
    public static void cancelAlarm(int medId) {
        TimerTask task = scheduledTasks.get(medId);
        if (task != null) {
            task.cancel();
            scheduledTasks.remove(medId);
            System.out.println("[AlarmManager] 알람이 취소되었습니다. medId=" + medId);
        }
    }

    // 알람을 다른 시간으로 다시 예약(주로 '나중에 먹기'에서 사용)
    public static void rescheduleAlarm(JFrame parentFrame, int userId, int medId, LocalDateTime newTime) {
        System.out.println("[AlarmManager] 재예약 요청 → medId=" + medId + ", 새로운 시간=" + newTime);
        scheduleAlarm(parentFrame, userId, medId, newTime);
    }
}