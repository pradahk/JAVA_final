package com.smwujava.medicineapp.Scheduler;

import com.smwujava.medicineapp.dao.*;
import com.smwujava.medicineapp.model.DosageRecord;
import com.smwujava.medicineapp.service.*;
import javax.swing.JFrame;
import java.sql.SQLException;
import java.time.*;
import java.util.List;
import java.util.concurrent.*;

public class AlarmScheduler {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final JFrame parentFrame;
    private final int userId;

    private final AlarmManager alarmManager;
    private final AlarmResponseHandler responseHandler;
    private final MedicationSchedulerService medicationSchedulerService;
    private final SuggestAdjustedTime suggestAdjustedTime;
    private final DosageRecordDao dosageRecordDao;

    public AlarmScheduler(JFrame parentFrame, int userId, MedicineDao medicineDao, UserPatternDao userPatternDao, DosageRecordDao dosageRecordDao) {
        this.parentFrame = parentFrame;
        this.userId = userId;
        this.dosageRecordDao = dosageRecordDao;

        this.alarmManager = new AlarmManager(medicineDao);
        this.responseHandler = new AlarmResponseHandler(dosageRecordDao, this.alarmManager);
        this.medicationSchedulerService = new MedicationSchedulerService(medicineDao, userPatternDao, dosageRecordDao, this.alarmManager);
        this.suggestAdjustedTime = new SuggestAdjustedTime(dosageRecordDao);
    }

    public void start() {
        Runnable createInitialAlarmsTask = () -> {
            medicationSchedulerService.createAndScheduleTodayAlarms(parentFrame, responseHandler);
        };
        scheduler.scheduleAtFixedRate(createInitialAlarmsTask, 0, 24, TimeUnit.HOURS);

        Runnable adjustAlarmsTask = () -> {
            suggestAdjustedTime.suggestAndApplyAdjustedTime(userId, 0);
            try {
                LocalDate today = LocalDate.now();
                List<DosageRecord> futureRecords = dosageRecordDao.findRecordsByUserIdAndDateRangeForFuture(userId, today.toString(), today.plusDays(30).toString());
                for(DosageRecord record : futureRecords) {
                    alarmManager.scheduleAlarm(parentFrame, record, responseHandler);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        };
        scheduler.scheduleAtFixedRate(adjustAlarmsTask, calculateInitialDelay(1, 0), 24 * 60, TimeUnit.MINUTES);
    }

    public void stop() {
        alarmManager.shutdown();
        scheduler.shutdown();
    }

    private long calculateInitialDelay(int targetHour, int targetMinute) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRunTime = now.with(LocalTime.of(targetHour, targetMinute));
        if (now.isAfter(nextRunTime)) nextRunTime = nextRunTime.plusDays(1);
        return Duration.between(now, nextRunTime).toMinutes();
    }
}