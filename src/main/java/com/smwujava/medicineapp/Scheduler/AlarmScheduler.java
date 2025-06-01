package com.smwujava.medicineapp.Scheduler;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.model.DosageRecord;
import com.smwujava.medicineapp.service.AlarmManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AlarmScheduler {

    private final DosageRecordDao dosageRecordDao;
    private final ScheduledExecutorService scheduler;

    public AlarmScheduler(DosageRecordDao dosageRecordDao) {
        this.dosageRecordDao = dosageRecordDao;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            List<DosageRecord> records = dosageRecordDao.getTodaySchedules();

            for (DosageRecord record : records) {
                LocalDateTime now = LocalDateTime.now();

                if (now.isAfter(record.getScheduledTime()) && !record.isTaken()) {
                    System.out.println("[자동 알림] 시간 도달: userId=" + record.getUserId() + ", medId=" + record.getMedId());
                    AlarmManager.triggerAlarm(record.getUserId(), record.getMedId(), record.getScheduledTime());
                }
            }
        }, 0, 1, TimeUnit.MINUTES);
    }
}