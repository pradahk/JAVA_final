package com.smwujava.medicineapp.Scheduler;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.model.DosageRecord;
import com.smwujava.medicineapp.dao.UserPatternDao;
import com.smwujava.medicineapp.service.AlarmManager;
import com.smwujava.medicineapp.dao.MedicineDao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.sql.SQLException;
import javax.swing.JFrame;




public class AlarmScheduler {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final MedicationSchedulerService medicationSchedulerService;
    private final DosageRecordDao dosageRecordDao;
    private final UserPatternDao userPatternDao;
    private final int userId; // 사용자 ID를 실제로 넣어줘야 함
    private final JFrame parentFrame;

    public AlarmScheduler(JFrame parentFrame, int userId,
                          DosageRecordDao dosageRecordDao,
                          UserPatternDao userPatternDao,
                          MedicineDao medicineDao) {
        this.parentFrame = parentFrame;
        this.userId = userId;
        this.dosageRecordDao = dosageRecordDao;
        this.userPatternDao = userPatternDao;
        this.medicationSchedulerService = new MedicationSchedulerService(dosageRecordDao, medicineDao, userPatternDao);
    }

    public void start() {
        // 1️⃣ 매시간 복용 예정 알람 스케줄링 (지연 패턴 반영)
        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<DosageRecord> records = dosageRecordDao.getTodaySchedules(userId);
                int delayCount = userPatternDao.getLateCountLastWeek(userId);
                int avgDelay = userPatternDao.getAverageDelayMinutesByUser(userId);

                for (DosageRecord record : records) {
                    LocalDateTime adjustedTime = record.getScheduledTime();
                    if (delayCount >= 4) {
                        adjustedTime = adjustedTime.plusMinutes(avgDelay);
                    }
                    AlarmManager.scheduleAlarm(parentFrame, userId, record.getMedId(), adjustedTime);
                }
            } catch (Exception e) {
                System.err.println("복용 알람 예약 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.HOURS); // 매시간 반복

        // 2️⃣ 하루 1회 전체 알람 스케줄러 실행
        scheduler.scheduleAtFixedRate(() -> {
            try {
                medicationSchedulerService.scheduleDailyAlarms(userId, dosageRecordDao, userPatternDao);
                System.out.println("MedicationSchedulerService: 오늘의 전체 알람 등록 완료");
            } catch (Exception e) {
                System.err.println("scheduleDailyAlarms() 실행 오류: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, 24, TimeUnit.HOURS); // 하루 한 번 실행
    }
}