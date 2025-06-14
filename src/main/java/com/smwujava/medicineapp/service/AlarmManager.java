package com.smwujava.medicineapp.service;

import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.model.DosageRecord;
import com.smwujava.medicineapp.ui.alerts.AlarmPopup;
import javax.swing.JFrame;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class AlarmManager {
    private final ScheduledExecutorService scheduler;
    private final Map<Integer, ScheduledFuture<?>> scheduledTasks;
    private final MedicineDao medicineDao;

    public AlarmManager(MedicineDao medicineDao) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduledTasks = new HashMap<>();
        this.medicineDao = medicineDao;
    }

    public void scheduleAlarm(JFrame parentFrame, DosageRecord record, AlarmResponseHandler handler) {
        if (record == null || record.getRecordId() == 0) return;
        LocalDateTime effectiveTime = record.getRescheduledTime() != null ? record.getRescheduledTime() : record.getScheduledTime();
        if (effectiveTime == null) return;
        int recordId = record.getRecordId();
        cancelAlarm(recordId);
        long delayMillis = Duration.between(LocalDateTime.now(), effectiveTime).toMillis();
        if (delayMillis <= 0) return;
        Runnable alarmTask = () -> {
            String medName = medicineDao.findMedicineNameById(record.getMedId());
            AlarmPopup.show(parentFrame, record, medName, handler);
            scheduledTasks.remove(recordId);
        };
        ScheduledFuture<?> future = scheduler.schedule(alarmTask, delayMillis, TimeUnit.MILLISECONDS);
        scheduledTasks.put(recordId, future);
    }

    public void snooze(JFrame parentFrame, DosageRecord originalRecord, int snoozeMinutes, AlarmResponseHandler handler) {
        Runnable snoozeTask = () -> {
            String medName = medicineDao.findMedicineNameById(originalRecord.getMedId());
            AlarmPopup.show(parentFrame, originalRecord, medName, handler);
        };
        scheduler.schedule(snoozeTask, snoozeMinutes, TimeUnit.MINUTES);
    }

    public void cancelAlarm(int recordId) {
        ScheduledFuture<?> task = scheduledTasks.get(recordId);
        if (task != null) {
            task.cancel(false);
            scheduledTasks.remove(recordId);
        }
    }

    public void shutdown() {
        scheduler.shutdown();
        try { if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) scheduler.shutdownNow(); }
        catch (InterruptedException e) { scheduler.shutdownNow(); }
    }
}