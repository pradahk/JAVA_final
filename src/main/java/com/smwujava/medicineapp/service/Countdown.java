package com.smwujava.medicineapp.service;

import com.smwujava.medicineapp.dao.DosageRecordDao;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Consumer;

public class Countdown implements Runnable {
    private final DosageRecordDao dao;
    private final int userId;
    private final Consumer<String> uiCallback;

    public Countdown(DosageRecordDao dao, int userId, Consumer<String> uiCallback) {
        this.dao = dao;
        this.userId = userId;
        this.uiCallback = uiCallback;
    }

    @Override
    public void run() {
        while (true) {
            try {
                LocalDateTime targetTime = dao.findClosestUpcomingAlarmTime(userId);
                if (targetTime == null) {
                    uiCallback.accept(" 알람 없음 ");
                    Thread.sleep(60000); // 1분 후 다시 확인
                    continue;
                }

                while (true) {
                    Duration remaining = Duration.between(LocalDateTime.now(), targetTime);
                    if (remaining.isNegative()) break;

                    // 중간에 더 이른 알람이 생겼는지 확인
                    LocalDateTime latestTime = dao.findClosestUpcomingAlarmTime(userId);
                    if (latestTime != null && latestTime.isBefore(targetTime)) {
                        targetTime = latestTime;
                        uiCallback.accept("  카운트다운 재설정됨 ");
                        continue;
                    }

                    long minutes = remaining.toMinutes();
                    long seconds = remaining.getSeconds() % 60;
                    if (minutes == 0 && seconds <= 59) {
                        uiCallback.accept("COLOR: 남은 시간: " + String.format("%02d:%02d", minutes, seconds));
                    } else {
                        uiCallback.accept(" 남은 시간: " + String.format("%02d:%02d", minutes, seconds));
                    }


                    Thread.sleep(1000); // 1초 단위 갱신
                }

                uiCallback.accept(" 알람 도착! 약 복용하세요  ");
                Thread.sleep(5000); // 짧은 대기 후 다음 알람 확인
            } catch (Exception e) {
                uiCallback.accept("[오류 발생]");
                e.printStackTrace();
                break;
            }
        }
    }
}
