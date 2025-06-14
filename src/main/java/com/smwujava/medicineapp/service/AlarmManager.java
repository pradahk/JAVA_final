package com.smwujava.medicineapp.service;

import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.model.DosageRecord;
import com.smwujava.medicineapp.ui.alerts.AlarmPopup;

import javax.swing.JFrame;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 알람 실행을 예약하고 관리하는 클래스입니다.
 * 이 클래스는 외부로부터 전달받은 정확한 시간에 알람을 실행하는 역할만 수행합니다.
 * 모든 시간 계산 및 보정 로직은 이 클래스 외부에서 처리되어야 합니다.
 */
public class AlarmManager {

    private final ScheduledExecutorService scheduler;
    private final Map<Integer, ScheduledFuture<?>> scheduledTasks;
    private final MedicineDao medicineDao; // 생성자를 통해 의존성 주입

    /**
     * AlarmManager 생성자.
     * @param medicineDao 약 이름 조회를 위해 필요한 DAO 객체.
     */
    public AlarmManager(MedicineDao medicineDao) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduledTasks = new HashMap<>();
        this.medicineDao = medicineDao;
    }

    /**
     * 지정된 복용 기록(DosageRecord)에 대한 알람을 예약합니다.
     * 재조정된 시간(rescheduled_time)이 있다면 그 시간을 우선적으로 사용합니다.
     * @param parentFrame 알람 팝업의 부모가 될 프레임
     * @param record 알람을 예약할 복용 기록 객체 (recordId, scheduledTime 등 포함)
     */
    public void scheduleAlarm(JFrame parentFrame, DosageRecord record) {
        if (record == null) {
            System.err.println("오류: 유효하지 않은 복용 기록으로 알람을 예약할 수 없습니다.");
            return;
        }

        // 재조정된 시간이 있으면 그것을, 없으면 원래 예정 시간을 사용
        LocalDateTime effectiveTime = record.getRescheduledTime() != null
                ? record.getRescheduledTime()
                : record.getScheduledTime();

        if (effectiveTime == null) {
            System.err.println("오류: 예약 시간이 존재하지 않습니다. recordId=" + record.getRecordId());
            return;
        }

        int recordId = record.getRecordId();
        cancelAlarm(recordId); // 이 복용 기록(recordId)에 대해 이미 예약된 알람이 있다면 취소

        long delayMillis = Duration.between(LocalDateTime.now(), effectiveTime).toMillis();

        if (delayMillis <= 0) {
            return; // 이미 지난 시간이면 예약하지 않음
        }

        // 실행할 알람 작업을 정의
        Runnable alarmTask = () -> {
            System.out.println("[알림 실행] User " + record.getUserId() + "님, 약(" + record.getMedId() + ") 복용 시간입니다!");
            triggerAlarm(parentFrame, record);
            scheduledTasks.remove(recordId); // 실행된 작업은 맵에서 제거
        };

        // 스케줄러에 작업을 예약하고, 취소할 수 있도록 ScheduledFuture 객체를 맵에 저장
        ScheduledFuture<?> future = scheduler.schedule(alarmTask, delayMillis, TimeUnit.MILLISECONDS);
        scheduledTasks.put(recordId, future);

        System.out.println("[알람 예약 완료] recordId=" + recordId + ", medId=" + record.getMedId() + ", 최종 시간=" + effectiveTime);
    }

    /**
     * 특정 복용 기록(recordId)에 예약된 알람을 취소합니다.
     * @param recordId 취소할 알람의 복용 기록 ID
     */
    public void cancelAlarm(int recordId) {
        ScheduledFuture<?> future = scheduledTasks.get(recordId);
        if (future != null) {
            future.cancel(false);
            scheduledTasks.remove(recordId);
        }
    }

    /**
     * 알람을 스누즈(일시 중지 후 다시 알림)합니다.
     * @param parentFrame 알람 팝업의 부모 프레임
     * @param originalRecord 스누즈의 대상이 된 원본 복용 기록
     * @param snoozeMinutes 다시 알림까지의 시간(분)
     */
    public void snoozeAlarm(JFrame parentFrame, DosageRecord originalRecord, int snoozeMinutes) {
        System.out.println("[스누즈] " + snoozeMinutes + "분 후에 다시 알립니다. recordId=" + originalRecord.getRecordId());

        Runnable snoozeTask = () -> {
            System.out.println("[스누즈 알림 실행] User " + originalRecord.getUserId() + "님, 아까 미룬 약(" + originalRecord.getMedId() + ") 복용 시간입니다!");
            triggerAlarm(parentFrame, originalRecord);
        };

        scheduler.schedule(snoozeTask, snoozeMinutes, TimeUnit.MINUTES);
    }

    /**
     * 실제 알람 팝업을 화면에 표시합니다.
     */
    private void triggerAlarm(JFrame parentFrame, DosageRecord record) {
        String medName = medicineDao.findMedicineNameById(record.getMedId());
        AlarmPopup.show(parentFrame, record.getUserId(), record.getMedId(), record.getScheduledTime(), medName);
    }

    /**
     * 애플리케이션 종료 시 스케줄러를 안전하게 종료합니다.
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        System.out.println("🔌 AlarmManager가 안전하게 종료되었습니다.");
    }
}