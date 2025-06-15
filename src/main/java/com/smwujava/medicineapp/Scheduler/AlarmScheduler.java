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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AlarmScheduler {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final MedicationSchedulerService medicationSchedulerService;
    private final SuggestAdjustedTime suggestAdjustedTime;
    private final DosageRecordDao dosageRecordDao;
    private final int userId;
    private final JFrame parentFrame;

    public AlarmScheduler(JFrame parentFrame, int userId, DosageRecordDao dosageRecordDao, UserPatternDao userPatternDao, MedicineDao medicineDao) {
        this.parentFrame = parentFrame;
        this.userId = userId;
        this.dosageRecordDao = dosageRecordDao;
        this.medicationSchedulerService = new MedicationSchedulerService(dosageRecordDao, medicineDao, userPatternDao);
        // 시간 조정을 위한 서비스 인스턴스 생성
        this.suggestAdjustedTime = new SuggestAdjustedTime(dosageRecordDao);
    }

    /**
     * 주기적인 알람 스케줄링 작업을 시작합니다.
     */
    public void start() {
        // 작업 1: 매일 새벽 2시에 '일일 설정' 작업을 실행합니다.
        scheduler.scheduleAtFixedRate(this::runDailySetup, calculateInitialDelay(2, 0), 24, TimeUnit.HOURS);

        // 작업 2: 1시간마다 '상태 동기화' 작업을 실행하여 알람을 최신 상태로 유지합니다.
        scheduler.scheduleAtFixedRate(this::rescheduleUpcomingAlarms, 1, 1, TimeUnit.HOURS);

        System.out.println("✅ AlarmScheduler가 시작되었습니다. 일일 작업 및 시간별 동기화 작업이 예약되었습니다.");
    }

    /**
     * 매일 한 번 실행되는 메인 작업입니다.
     * 오늘의 복용 기록 생성, 사용자 패턴 분석 및 시간 조정, 최종 알람 설정을 순차적으로 수행합니다.
     */
    private void runDailySetup() {
        System.out.println("--- [일일 설정 작업 시작] ---");
        // 1. 오늘의 기본 복용 기록을 생성합니다.
        System.out.println("1. 오늘의 기본 복용 기록을 생성합니다...");
        medicationSchedulerService.scheduleTodayMedications(userId, parentFrame);

        // 2. 사용자의 최신 복용 패턴을 분석하여 앞으로의 복용 시간을 조정하고 DB에 업데이트합니다.
        System.out.println("2. 사용자 패턴을 분석하여 향후 복용 시간을 조정합니다...");
        suggestAdjustedTime.suggestAndApplyAdjustedTime(userId, 0); // medId는 내부에서 사용하지 않으므로 0으로 전달

        // 3. 최종적으로 결정된 시간으로 오늘의 모든 알람을 다시 설정합니다.
        System.out.println("3. 조정된 시간을 포함하여 오늘의 모든 알람을 다시 설정합니다...");
        rescheduleUpcomingAlarms();
        System.out.println("--- [일일 설정 작업 완료] ---");
    }

    /**
     * 현재 시간을 기준으로 오늘 남은 모든 복용 기록에 대한 알람을 (재)설정합니다.
     * DB에 저장된 시간을 기준으로 하므로, 자체적인 조정 로직이 없습니다.
     */
    private void rescheduleUpcomingAlarms() {
        System.out.println("🔄 [알람 상태 동기화] 오늘 남은 알람들을 다시 설정합니다...");
        try {
            List<DosageRecord> todayRecords = dosageRecordDao.findRecordsByUserIdAndDate(
                    userId,
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            );

            int scheduledCount = 0;
            for (DosageRecord record : todayRecords) {
                // 이미 복용했거나 건너뛴 기록은 제외
                if (record.isTaken() || record.isSkipped()) {
                    continue;
                }

                // DB에 조정된 시간이 저장되어 있다면 그 시간을 사용하고, 없다면 기본 예정 시간을 사용합니다.
                LocalDateTime alarmTime = record.getRescheduledTime() != null ? record.getRescheduledTime() : record.getScheduledTime();

                // 현재 시간 이후의 알람만 설정합니다.
                if (alarmTime.isAfter(LocalDateTime.now())) {
                    AlarmManager.scheduleAlarm(parentFrame, userId, record.getMedId(), alarmTime);
                    scheduledCount++;
                }
            }
            System.out.println("✅ " + scheduledCount + "개의 남은 알람이 성공적으로 설정/갱신되었습니다.");

        } catch (SQLException e) {
            System.err.println("오류: 알람을 다시 예약하는 중 DB 오류가 발생했습니다. " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 프로그램 시작 시점으로부터 가장 가까운 특정 시각까지의 지연시간(delay)을 계산합니다.
     * @param targetHour 실행될 시 (0-23)
     * @param targetMin 실행될 분 (0-59)
     * @return 초기 지연 시간 (단위: 시간)
     */
    private long calculateInitialDelay(int targetHour, int targetMin) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.with(LocalTime.of(targetHour, targetMin));
        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }
        return Duration.between(now, nextRun).toHours();
    }

    /**
     * 스케줄러를 종료합니다.
     */
    public void stop() {
        scheduler.shutdown();
    }
}