package com.smwujava.medicineapp.Scheduler;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.dao.UserPatternDao;
import com.smwujava.medicineapp.model.DosageRecord;
import com.smwujava.medicineapp.service.AlarmManager;
import com.smwujava.medicineapp.service.SuggestAdjustedTime;

import javax.swing.JFrame;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AlarmScheduler {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final int userId;
    private final JFrame parentFrame;

    private final AlarmManager alarmManager;
    private final MedicationSchedulerService medicationSchedulerService;
    private final SuggestAdjustedTime suggestAdjustedTime;
    private final DosageRecordDao dosageRecordDao;

    public AlarmScheduler(JFrame parentFrame, int userId, MedicineDao medicineDao, UserPatternDao userPatternDao, DosageRecordDao dosageRecordDao) {
        this.parentFrame = parentFrame;
        this.userId = userId;
        this.dosageRecordDao = dosageRecordDao;

        this.alarmManager = new AlarmManager(medicineDao);
        this.suggestAdjustedTime = new SuggestAdjustedTime(dosageRecordDao);
        this.medicationSchedulerService = new MedicationSchedulerService(medicineDao, userPatternDao, dosageRecordDao, this.alarmManager);
    }

    public void start() {
        scheduleDailyInitialAlarms();
        scheduleDailyAlarmAdjustments();
        System.out.println("⏰ 알람 스케줄러가 시작되었습니다. (userId: " + userId + ")");
    }

    private void scheduleDailyInitialAlarms() {
        Runnable createInitialAlarmsTask = () -> {
            System.out.println("[스케줄러] 오늘의 기본 알람 생성을 시작합니다.");
            medicationSchedulerService.createAndScheduleTodayAlarms(userId, parentFrame);
        };
        scheduler.scheduleAtFixedRate(createInitialAlarmsTask, 0, 24, TimeUnit.HOURS);
    }

    private void scheduleDailyAlarmAdjustments() {
        Runnable adjustAlarmsTask = () -> {
            System.out.println("[스케줄러] 사용자 패턴 분석 및 알람 재조정을 시작합니다.");

            // 1. DB의 rescheduled_time을 업데이트합니다.
            suggestAdjustedTime.suggestAndApplyAdjustedTime(userId, 0);

            // 2. DB가 업데이트 되었으므로, 오늘 이후의 알람들을 다시 불러와 AlarmManager에 재등록합니다.
            try {
                LocalDate today = LocalDate.now();
                List<DosageRecord> futureRecords = dosageRecordDao.findRecordsByUserIdAndDateRangeForFuture(userId, today.toString(), today.plusDays(30).toString());

                System.out.println("[스케줄러] " + futureRecords.size() + "개의 미래 알람에 대해 재조정된 시간을 적용합니다.");
                for(DosageRecord record : futureRecords) {
                    alarmManager.scheduleAlarm(parentFrame, record);
                }
            } catch (SQLException e) {
                System.err.println("오류: 알람 재조정 후 재등록 작업 중 예외 발생 - " + e.getMessage());
            }
        };

        long initialDelay = calculateInitialDelay(1, 0); // 새벽 1시에 실행
        scheduler.scheduleAtFixedRate(adjustAlarmsTask, initialDelay, 24 * 60, TimeUnit.MINUTES);
    }

    public void stop() {
        alarmManager.shutdown();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        System.out.println("🔌 모든 스케줄러가 안전하게 종료되었습니다.");
    }

    private long calculateInitialDelay(int targetHour, int targetMinute) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRunTime = now.with(LocalTime.of(targetHour, targetMinute));
        if (now.isAfter(nextRunTime)) {
            nextRunTime = nextRunTime.plusDays(1);
        }
        return Duration.between(now, nextRunTime).toMinutes();
    }
}