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

    private static final Timer timer = new Timer("AlarmTimer", true); // 데몬 스레드로 변경
    private static final Map<Integer, TimerTask> scheduledTasks = new HashMap<>();

    /**
     * 특정 약(medId)에 대한 알람을 예약합니다.
     * 이 메서드는 더 이상 시간을 스스로 조정하지 않고, 전달받은 시간을 100% 신뢰하여 알람을 설정합니다.
     *
     * @param parentFrame     알람 팝업의 부모 프레임
     * @param userId          사용자 ID
     * @param medId           약 ID
     * @param time            알람이 울릴 정확한 시간 (이미 모든 조정이 완료된 최종 시간)
     */
    public static void scheduleAlarm(JFrame parentFrame, int userId, int medId, LocalDateTime time) {
        // 1. 동일한 약에 대해 예약된 기존 알람이 있다면 취소하여 중복을 방지합니다.
        cancelAlarm(medId);

        // 2. 전달받은 시간이 이미 과거라면 알람을 설정하지 않고 종료합니다.
        if (time.isBefore(LocalDateTime.now())) {
            System.out.println("[AlarmManager] 알람 시간이 과거이므로 예약하지 않음: medId=" + medId + ", time=" + time);
            return;
        }

        long delayMillis = java.time.Duration.between(LocalDateTime.now(), time).toMillis();

        // 3. 실행할 작업을 정의합니다. (지정된 시간에 알람 팝업을 띄웁니다)
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println("[알림] User " + userId + "님, 약(" + medId + ")을 복용할 시간입니다!");
                triggerAlarm(parentFrame, userId, medId, time);
                scheduledTasks.remove(medId); // 실행된 알람은 맵에서 제거
            }
        };

        // 4. 계산된 지연 시간 이후에 작업이 실행되도록 스케줄링하고, 관리 맵에 추가합니다.
        timer.schedule(task, delayMillis);
        scheduledTasks.put(medId, task);
        System.out.println("[AlarmManager] 알람 예약 완료 → medId=" + medId + ", 실행 시각=" + time);
    }

    /**
     * 알람을 실제로 화면에 표시합니다.
     *
     * @param parentFrame     알람 팝업의 부모 프레임
     * @param userId          사용자 ID
     * @param medId           약 ID
     * @param scheduledTime   원래 예정되었던 시간
     */
    public static void triggerAlarm(JFrame parentFrame, int userId, int medId, LocalDateTime scheduledTime) {
        // DAO는 필요할 때만 생성하여 사용합니다.
        MedicineDao medicineDao = new MedicineDao();
        String medName = medicineDao.findMedicineNameById(medId);
        AlarmPopup.show(parentFrame, userId, medId, scheduledTime, medName);
    }

    /**
     * 예약된 알람을 취소합니다.
     *
     * @param medId 취소할 약 ID
     */
    public static void cancelAlarm(int medId) {
        TimerTask task = scheduledTasks.get(medId);
        if (task != null) {
            task.cancel();
            scheduledTasks.remove(medId);
            System.out.println("[AlarmManager] 알람이 취소되었습니다. medId=" + medId);
        }
    }

    /**
     * 알람을 다른 시간으로 다시 예약합니다. (주로 '나중에 먹기' 기능에 사용)
     *
     * @param parentFrame 팝업의 부모 프레임
     * @param userId      사용자 ID
     * @param medId       약 ID
     * @param newTime     다시 알람이 울릴 새로운 시간
     */
    public static void rescheduleAlarm(JFrame parentFrame, int userId, int medId, LocalDateTime newTime) {
        System.out.println("[AlarmManager] 재예약 요청 → medId=" + medId + ", 새로운 시간=" + newTime);
        // 단순히 scheduleAlarm을 다시 호출하여 새로운 시간에 알람을 설정합니다.
        scheduleAlarm(parentFrame, userId, medId, newTime);
    }

    // `rescheduleAlarm` 과 역할이 중복되고 혼동을 줄 수 있는 `snoozeAlarm` 메서드는 제거했습니다.
}