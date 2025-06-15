package com.smwujava.medicineapp.Scheduler;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.dao.UserPatternDao;
import com.smwujava.medicineapp.model.DosageRecord;
import com.smwujava.medicineapp.model.Medicine;
import com.smwujava.medicineapp.model.UserPattern;
import com.smwujava.medicineapp.service.AlarmManager;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;

public class MedicationSchedulerService {
    private final DosageRecordDao dosageRecordDao;
    private final MedicineDao medicineDao;
    private final UserPatternDao userPatternDao;
    // 이 클래스는 더 이상 직접 시간 조정을 책임지지 않으므로, 조정 관련 필드는 제거합니다.

    public MedicationSchedulerService(DosageRecordDao dosageRecordDao, MedicineDao medicineDao, UserPatternDao userPatternDao) {
        this.dosageRecordDao = dosageRecordDao;
        this.medicineDao = medicineDao;
        this.userPatternDao = userPatternDao;
    }

    /**
     * 오늘의 복용 일정을 생성하고, 각 복용 건에 대한 기본 알람을 예약합니다.
     * 시간 조정 로직은 모두 제거되고, 순수한 초기 예정 시간만 계산하여 사용합니다.
     */
    public void scheduleTodayMedications(int userId, JFrame parentFrame) {
        UserPattern pattern;
        List<Medicine> medicines;

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
            if (!shouldTakeToday(med.getMedDays())) continue;

            LocalTime baseTime = getBaseTime(pattern, med);

            for (int i = 0; i < med.getMedDailyAmount(); i++) {
                // TODO: med.getMedDailyAmount()에 따른 정확한 예정 시간 계산 로직 필요 (현재는 임시)
                LocalTime scheduledTime = baseTime.plusHours(i * 4);
                LocalDateTime scheduledDateTime = LocalDateTime.of(today, scheduledTime);

                // DosageRecord 객체 생성: 순수하게 계산된 초기 예정 시간을 기록합니다.
                DosageRecord record = new DosageRecord();
                record.setUserId(userId);
                record.setMedId(med.getMedId());
                record.setScheduledTime(scheduledDateTime);
                record.setActualTakenTime(null);
                record.setRescheduledTime(null);
                record.setSkipped(false);

                try {
                    // 복용 기록을 DB에 삽입합니다.
                    dosageRecordDao.insertDosageRecord(record);

                    // 알람 예약: 조정 로직 없이, 생성된 초기 예정 시간으로 바로 알람을 설정합니다.
                    System.out.println("✅ 기본 시간으로 복용 기록 생성 및 알람 예약 → medId: " + med.getMedId() + ", 예정 시각: " + scheduledDateTime);
                    AlarmManager.scheduleAlarm(parentFrame, userId, med.getMedId(), scheduledDateTime);

                } catch (SQLException e) {
                    // SQLite의 UNIQUE 제약 조건 메시지 확인
                    if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed")) {
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

    /**
     * 오늘 날짜로 DB에 저장된 모든 복용 기록을 찾아 알람을 설정합니다.
     * 이 메서드는 앱 재시작 등의 상황에서 알람을 다시 활성화할 때 사용될 수 있습니다.
     */
    public void scheduleTodayAlarms(int userId, JFrame parentFrame) {
        try {
            List<DosageRecord> todayRecords = dosageRecordDao.findRecordsByUserIdAndDate(
                    userId,
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            );

            for (DosageRecord record : todayRecords) {
                // 조정 로직 없이, DB에 저장된 예정 시간(scheduledTime)으로 알람을 설정합니다.
                // 만약 재조정된 시간(rescheduledTime)이 있다면 그것을 사용하도록 AlarmManager에서 처리할 수 있습니다.
                LocalDateTime alarmTime = record.getRescheduledTime() != null ? record.getRescheduledTime() : record.getScheduledTime();
                AlarmManager.scheduleAlarm(parentFrame, record.getUserId(), record.getMedId(), alarmTime);
            }

        } catch (Exception e) {
            System.err.println("오늘의 알람 재예약 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private boolean shouldTakeToday(String medDays) {
        if (medDays == null || medDays.trim().isEmpty()) return false;
        List<String> days = Arrays.asList(medDays.split(","));

        if (days.contains("매일")) return true;

        String todayKor;
        switch (LocalDate.now().getDayOfWeek()) {
            case MONDAY: todayKor = "월"; break;
            case TUESDAY: todayKor = "화"; break;
            case WEDNESDAY: todayKor = "수"; break;
            case THURSDAY: todayKor = "목"; break;
            case FRIDAY: todayKor = "금"; break;
            case SATURDAY: todayKor = "토"; break;
            case SUNDAY: todayKor = "일"; break;
            default: return false;
        }
        return days.contains(todayKor);
    }

    private LocalTime getBaseTime(UserPattern pattern, Medicine med) {
        LocalTime refTime;
        String medCondition = med.getMedCondition();

        // pattern 또는 medCondition이 null일 경우의 기본값 처리 강화
        if (pattern == null) {
            System.err.println("UserPattern이 null이므로 기본 시간(09:00)을 사용합니다.");
            return LocalTime.of(9, 0);
        }

        if ("식사".equals(medCondition)) {
            // TODO: 아침, 점심, 저녁을 구분하는 로직 필요. 현재는 아침으로 고정.
            refTime = parseTime(pattern.getBreakfastStartTime(), "아침 식사 시간");
        } else if ("잠자기".equals(medCondition)) {
            refTime = parseTime(pattern.getSleepStartTime(), "취침 시간");
        } else {
            System.err.println("알 수 없는 복용 조건(" + medCondition + ")입니다. 기본적으로 아침 식사 시간을 사용합니다.");
            refTime = parseTime(pattern.getBreakfastStartTime(), "아침 식사 시간");
        }

        int offset = med.getMedMinutes();
        return "전".equals(med.getMedTiming()) ? refTime.minusMinutes(offset) : refTime.plusMinutes(offset);
    }

    private LocalTime parseTime(String timeStr, String context) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            System.err.println(context + " 정보가 비어있어 기본 시간 '09:00'을 반환합니다.");
            return LocalTime.of(9, 0);
        }
        try {
            return LocalTime.parse(timeStr);
        } catch (java.time.format.DateTimeParseException e) {
            System.err.println(context + "의 시간 형식('" + timeStr + "')이 잘못되었습니다. 기본 시간 '09:00'을 반환합니다.");
            return LocalTime.of(9, 0);
        }
    }

    // scheduleDailyAlarms 메서드는 현재 사용되지 않거나 역할이 불분명하여 제거하거나 주석 처리할 수 있습니다.
    // public void scheduleDailyAlarms(int userId, DosageRecordDao dosageRecordDao, UserPatternDao userPatternDao) {
    //     System.out.println("[MedicationSchedulerService] 스케줄 실행됨: userId = " + userId);
    // }
}