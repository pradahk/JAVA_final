package com.smwujava.medicineapp.service;

import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import com.smwujava.medicineapp.ui.alerts.AlarmPopup;
import com.smwujava.medicineapp.dao.UserPatternDao;
import com.smwujava.medicineapp.dao.MedicineDao;


public class AlarmManager {

    private static final Timer timer = new Timer();
    private static final Map<Integer, TimerTask> scheduledTasks = new HashMap<>();

    public static void triggerAlarm(int userId, int medId, LocalDateTime scheduledTime) {
        MedicineDao medicineDao = new MedicineDao();
        String medName = medicineDao.findMedicineNameById(medId);  // 약 이름 조회
        AlarmPopup.show(userId, medId, scheduledTime, medName);  // 약 이름 포함해서 팝업 실행
    }

    public static void snoozeAlarm(int userId, int medId, int minutes) {
        LocalDateTime newTime = LocalDateTime.now().plusMinutes(minutes);
        scheduleAlarm(userId, medId, newTime);
    }

    public static void cancelAlarm(int medId) {
        TimerTask task = scheduledTasks.get(medId);
        if (task != null) {
            task.cancel();
            scheduledTasks.remove(medId);
            System.out.println("알람이 취소되었습니다. medId=" + medId);
        } else {
            System.out.println("취소할 알람이 존재하지 않습니다. medId=" + medId);
        }
    }

    // 🔧 수정: userId와 scheduledTime도 전달받아 다시 알림 실행
    public static void rescheduleAlarm(int userId, int medId, LocalDateTime newTime) {
        cancelAlarm(medId);  // 기존 알람이 있다면 제거

        long delayMillis = java.sql.Timestamp.valueOf(newTime).getTime() - System.currentTimeMillis();

        if (delayMillis <= 0) {
            System.out.println("이미 지난 시간이므로 재알림 생략: " + newTime);
            return;
        }

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println("[재알림] User " + userId + "님, 약(" + medId + ")을 복용할 시간입니다! (재알림)");
                triggerAlarm(userId, medId, newTime);
            }
        };

        timer.schedule(task, delayMillis);
        scheduledTasks.put(medId, task);
        System.out.println("재알림 예약 완료 → userId=" + userId + ", medId=" + medId + ", 시간=" + newTime);
    }

    public static void scheduleAlarm(int userId, int medId, LocalDateTime time) {
        cancelAlarm(medId);  // 중복 방지

        // 🔽 사용자 복약 패턴 기반 알람 시간 자동 조정 추가
        UserPatternDao patternDao = new UserPatternDao();
        int delayCount = patternDao.getLateCountLastWeek(userId);
        int averageDelay = patternDao.getAverageDelayMinutesByUser(userId);

        if (delayCount >= 4) {
            time = time.plusMinutes(averageDelay);  // 알람 시간 자동 보정
            System.out.println(" 사용자 패턴 기반으로 알람 시간이 조정됨: " + time);
        }

        long delayMillis = java.sql.Timestamp.valueOf(time).getTime() - System.currentTimeMillis();

        if (delayMillis <= 0) {
            System.out.println("이미 지난 시간이므로 알람 예약 생략: " + time);
            return;
        }

        final LocalDateTime scheduledTime = time;

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println("[알림] User " + userId + "님, 약(" + medId + ")을 복용할 시간입니다!");
                triggerAlarm(userId, medId, scheduledTime);
            }
        };

        timer.schedule(task, delayMillis);
        scheduledTasks.put(medId, task);
        System.out.println("[AlarmManager] 알람 예약 완료 → userId=" + userId + ", medId=" + medId + ", 시간=" + time);
    }
}