package com.smwujava.medicineapp.service;

import com.smwujava.medicineapp.dao.DosageRecordDao;

import java.time.Duration;
import java.time.LocalDateTime;

public class Countdown implements Runnable {
    private final DosageRecordDao dao;
    private final int userId;

    public Countdown(DosageRecordDao dao, int userId) {
        this.dao = dao;
        this.userId = userId;
    }

    @Override
    public void run() {
        while (true) {
            try {
                LocalDateTime alarmTime = dao.findClosestUpcomingAlarmTime(userId);

                if (alarmTime == null) {
                    System.out.println("[Countdown] 예정된 알람이 없습니다. 1분 후 재확인합니다.");
                    Thread.sleep(60_000); // 알람 없으면 1분 대기
                    continue;
                }

                System.out.println("[Countdown] 알람 예정 시간: " + alarmTime);

                while (true) {
                    Duration remaining = Duration.between(LocalDateTime.now(), alarmTime);

                    if (remaining.isNegative() || remaining.isZero()) break;

                    long hours = remaining.toHours();
                    long minutes = remaining.toMinutes() % 60;
                    long seconds = remaining.getSeconds() % 60;

                    System.out.printf(" 남은 시간: %02d:%02d:%02d\n", hours, minutes, seconds);

                    Thread.sleep(1000); // 1초 대기
                }

                System.out.println(" 알람 도착! (사용자 ID: " + userId + ")");
                //  알람 발생 후 필요한 처리 위치 ( UI 알림 신호)

            } catch (Exception e) {
                System.err.println("[Countdown] 오류 발생: " + e.getMessage());
                try {
                    Thread.sleep(5000); // 에러 발생 시 5초 대기 후 재시도
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
