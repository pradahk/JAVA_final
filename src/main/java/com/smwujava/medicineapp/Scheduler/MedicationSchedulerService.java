package com.smwujava.medicineapp.Scheduler;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.dao.UserPatternDao;
import com.smwujava.medicineapp.model.DosageRecord;
import com.smwujava.medicineapp.model.Medicine;
import com.smwujava.medicineapp.model.UserPattern;
import com.smwujava.medicineapp.service.AlarmManager;
import com.smwujava.medicineapp.service.SuggestAdjustedTime;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.time.format.DateTimeFormatter;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


public class MedicationSchedulerService {

    private final DosageRecordDao recordDao;
    private final AlarmManager alarmManager;
    private final SuggestAdjustedTime adjuster;


    public void scheduleDailyAlarms(int userId, DosageRecordDao dosageRecordDao, UserPatternDao userPatternDao) {
        // 여기에 알람 스케줄링 관련 로직을 작성
        System.out.println("[MedicationSchedulerService] 스케줄 실행됨: userId = " + userId);
    }

    private DosageRecordDao dosageRecordDao;
    private MedicineDao medicineDao;
    private UserPatternDao userPatternDao;

    public MedicationSchedulerService(DosageRecordDao dosageRecordDao, MedicineDao medicineDao, UserPatternDao userPatternDao) {

        this.recordDao = dosageRecordDao;
        this.adjuster = new SuggestAdjustedTime(dosageRecordDao);
        this.alarmManager = new AlarmManager();

        this.dosageRecordDao = dosageRecordDao;
        this.medicineDao = medicineDao;
        this.userPatternDao = userPatternDao;
    }

    public void scheduleTodayMedications(int userId, JFrame parentFrame) {
        UserPattern pattern = null;
        List<Medicine> medicines = null;

        try {
            pattern = userPatternDao.findPatternByUserId(userId);
            if (pattern == null) {
                System.err.println("사용자 생활 패턴이 없습니다: userId = " + userId);
                return;
            }

            medicines = medicineDao.findMedicinesByUserId(userId);
            if (medicines.isEmpty()) {
                System.out.println("등록된 약이 없습니다: userId = " + userId);
                return;
            }
        } catch (SQLException e) {
            System.err.println("스케줄링을 위한 데이터 로딩 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        LocalDate today = LocalDate.now();

        for (Medicine med : medicines) {
            List<String> days = Arrays.asList(med.getMedDays().split(","));
            if (!shouldTakeToday(days)) continue;

            LocalTime baseTime = getBaseTime(pattern, med);

            for (int i = 0; i < med.getMedDailyAmount(); i++) {
                // TODO: med.getMedDailyAmount()에 따른 정확한 예정 시간 계산 로직 필요
                LocalTime scheduledTime = baseTime.plusHours(i * 4); // 임시 로직
                LocalDateTime scheduledDateTime = LocalDateTime.of(today, scheduledTime);

                SuggestAdjustedTime adjuster = new SuggestAdjustedTime(dosageRecordDao);
                int delayMinutes = adjuster.suggestAndApplyAdjustedTime(userId, med.getMedId());

                if (delayMinutes >= 15) {
                    scheduledDateTime = scheduledDateTime.plusMinutes(delayMinutes);
                    System.out.println("⏰ 사용자 복약 패턴에 따라 " + delayMinutes + "분 보정됨: " + scheduledDateTime);
                }

                // DosageRecord 객체 생성 부분 수정
                DosageRecord record = new DosageRecord(); // 기본 생성자 사용
                record.setUserId(userId);
                record.setMedId(med.getMedId()); // 현재 처리 중인 Medicine 객체의 medId 사용
                record.setScheduledTime(scheduledDateTime);
                record.setActualTakenTime(null);
                record.setRescheduledTime(null);
                record.setSkipped(false); // 새로 생성하는 기록이므로 false로 설정

                try {
                    dosageRecordDao.insertDosageRecord(record);

                    int delayCount = userPatternDao.getLateCountLastWeek(userId);
                    int avgDelay = userPatternDao.getAverageDelayMinutesByUser(userId);
                    LocalDateTime adjustedTime = scheduledDateTime;

                    if (delayCount >= 4) {
                        adjustedTime = adjustedTime.plusMinutes(avgDelay);
                        System.out.println("⏰ 알람 시간 조정됨 → medId: " + med.getMedId()
                                + ", 기본 시각: " + scheduledDateTime
                                + ", 평균 지연: " + avgDelay + "분 → 조정된 시각: " + adjustedTime);
                    } else {
                        System.out.println("✅ 기본 시간으로 알람 예약 → medId: " + med.getMedId()
                                + ", 예정 시각: " + scheduledDateTime);
                    }


                    AlarmManager.scheduleAlarm(parentFrame, userId, med.getMedId(), adjustedTime);


                } catch (SQLException e) {
                    // SQLite의 UNIQUE 제약 조건 메시지 확인
                    if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed: DosageRecords.user_id, DosageRecords.med_id, record_date")) {
                        System.out.println("DEBUG: 이미 존재하는 복용 기록 (중복 삽입 방지): userId=" + userId + ", medId=" + med.getMedId() + ", date=" + today);
                    } else {
                        System.err.println("복용 기록 삽입 중 오류 발생: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("복용 스케줄 생성 완료: userId = " + userId);
    }

    public void scheduleTodayAlarms(int userId, JFrame parentFrame) {
        DosageRecordDao recordDao = new DosageRecordDao();


        try {
            List<DosageRecord> todayRecords = recordDao.findRecordsByUserIdAndDate(
                    userId,
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            );

            for (DosageRecord record : todayRecords) {
                int medId = record.getMedId();
                LocalDateTime scheduledTime = record.getScheduledTime();

                // ✅ 보정 시간 계산
                LocalDateTime adjustedTime = adjuster.getAdjustedTime(userId, medId, scheduledTime);


                // 보정된 시간으로 알람 예약
                AlarmManager.scheduleAlarm(parentFrame,userId, medId, adjustedTime);
            }

        } catch (Exception e) {
            System.err.println("오늘의 알람 예약 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private boolean shouldTakeToday(List<String> days) {
        if (days.contains("매일")) return true;

        java.time.DayOfWeek todayOfWeek = LocalDate.now().getDayOfWeek();
        String todayKor = "";
        switch (todayOfWeek) {
            case MONDAY: todayKor = "월"; break;
            case TUESDAY: todayKor = "화"; break;
            case WEDNESDAY: todayKor = "수"; break;
            case THURSDAY: todayKor = "목"; break;
            case FRIDAY: todayKor = "금"; break;
            case SATURDAY: todayKor = "토"; break;
            case SUNDAY: todayKor = "일"; break;
        }
        return days.contains(todayKor);
    }

    private LocalTime getBaseTime(UserPattern pattern, Medicine med) {
        LocalTime refTime;
        String medCondition = med.getMedCondition();

        if (pattern == null) {
            System.err.println("UserPattern is null. Cannot determine base time.");
            return LocalTime.of(9, 0);
        }

        if (medCondition == null) {
            System.err.println("Medication condition is null for medId: " + med.getMedId() + ". Defaulting to breakfast.");
            refTime = parseTime(pattern.getBreakfastStartTime());
        } else if ("식사".equals(medCondition)) {
            refTime = parseTime(pattern.getBreakfastStartTime()); // TODO: 실제 시나리오에 맞게 개선
        } else if ("잠자기".equals(medCondition)) {
            refTime = parseTime(pattern.getSleepStartTime());
        } else {
            System.err.println("Unknown medication condition: " + medCondition + ". Defaulting to breakfast.");
            refTime = parseTime(pattern.getBreakfastStartTime());
        }

        int offset = med.getMedMinutes();
        return "전".equals(med.getMedTiming()) ? refTime.minusMinutes(offset) : refTime.plusMinutes(offset);
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            System.err.println("시간 문자열이 비어있습니다. 기본 시간 '09:00' 반환.");
            return LocalTime.of(9, 0);
        }
        try {
            return LocalTime.parse(timeStr);
        } catch (java.time.format.DateTimeParseException e) {
            System.err.println("시간 문자열 파싱 오류: " + timeStr + ". 기본 시간 '09:00' 반환. 오류: " + e.getMessage());
            return LocalTime.of(9, 0);
        }
    }
}