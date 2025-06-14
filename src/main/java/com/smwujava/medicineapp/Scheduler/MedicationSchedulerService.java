package com.smwujava.medicineapp.Scheduler;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.dao.UserPatternDao;
import com.smwujava.medicineapp.model.DosageRecord;
import com.smwujava.medicineapp.model.Medicine;
import com.smwujava.medicineapp.model.UserPattern;
import com.smwujava.medicineapp.service.AlarmManager;

import javax.swing.JFrame;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MedicationSchedulerService {

    private final MedicineDao medicineDao;
    private final UserPatternDao userPatternDao;
    private final DosageRecordDao dosageRecordDao;
    private final AlarmManager alarmManager;

    public MedicationSchedulerService(MedicineDao medicineDao, UserPatternDao userPatternDao, DosageRecordDao dosageRecordDao, AlarmManager alarmManager) {
        this.medicineDao = medicineDao;
        this.userPatternDao = userPatternDao;
        this.dosageRecordDao = dosageRecordDao;
        this.alarmManager = alarmManager;
    }

    public void createAndScheduleTodayAlarms(int userId, JFrame parentFrame) {
        UserPattern pattern;
        List<Medicine> medicines;

        try {
            pattern = userPatternDao.findPatternByUserId(userId);
            if (pattern == null) {
                System.err.println("스케줄링 실패: 사용자 생활 패턴이 설정되지 않았습니다. (userId: " + userId + ")");
                return;
            }
            medicines = medicineDao.findMedicinesByUserId(userId);
            if (medicines.isEmpty()) {
                return;
            }
        } catch (SQLException e) {
            System.err.println("오류: 스케줄링을 위한 데이터 로딩 중 DB 오류 발생. " + e.getMessage());
            return;
        }

        for (Medicine med : medicines) {
            if (!shouldTakeToday(med.getMedDays())) {
                continue;
            }

            List<LocalTime> baseTimes = getBaseTimes(pattern, med);

            for (int i = 0; i < med.getMedDailyAmount() && i < baseTimes.size(); i++) {
                LocalTime baseTime = baseTimes.get(i);
                LocalTime scheduledTime = calculateFinalTime(baseTime, med.getMedTiming(), med.getMedMinutes());
                LocalDateTime scheduledDateTime = LocalDateTime.of(LocalDate.now(), scheduledTime);
                createRecordAndScheduleAlarm(userId, med.getMedId(), scheduledDateTime, parentFrame);
            }
        }
    }

    private void createRecordAndScheduleAlarm(int userId, int medId, LocalDateTime scheduledDateTime, JFrame parentFrame) {
        try {
            DosageRecord record = new DosageRecord();
            record.setUserId(userId);
            record.setMedId(medId);
            record.setScheduledTime(scheduledDateTime);
            record.setSkipped(false);

            int generatedRecordId = dosageRecordDao.insertDosageRecord(record);
            if (generatedRecordId != -1) {
                record.setRecordId(generatedRecordId);
                System.out.println("알람 기록 생성: recordId=" + record.getRecordId());
                this.alarmManager.scheduleAlarm(parentFrame, record);
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed")) {
                // 중복 기록은 조용히 넘어감
            } else {
                System.err.println("오류: 복용 기록 삽입 또는 알람 예약 중 DB 오류 발생. " + e.getMessage());
            }
        }
    }

    private List<LocalTime> getBaseTimes(UserPattern pattern, Medicine med) {
        List<LocalTime> baseTimes = new ArrayList<>();
        String medCondition = med.getMedCondition();

        if ("식사".equals(medCondition)) {
            baseTimes.add(parseTime(pattern.getBreakfastStartTime(), "아침"));
            baseTimes.add(parseTime(pattern.getLunchStartTime(), "점심"));
            baseTimes.add(parseTime(pattern.getDinnerStartTime(), "저녁"));
        } else if ("잠자기".equals(medCondition)) {
            baseTimes.add(parseTime(pattern.getSleepStartTime(), "취침"));
        } else {
            baseTimes.add(parseTime(pattern.getBreakfastStartTime(), "아침"));
        }
        return baseTimes;
    }

    private LocalTime calculateFinalTime(LocalTime baseTime, String timing, int minutes) {
        return "전".equals(timing) ? baseTime.minusMinutes(minutes) : baseTime.plusMinutes(minutes);
    }

    private boolean shouldTakeToday(String medDays) {
        if (medDays == null || medDays.trim().isEmpty()) return false;
        if (medDays.contains("매일")) return true;
        DayOfWeek todayOfWeek = LocalDate.now().getDayOfWeek();
        String todayKor;
        switch (todayOfWeek) {
            case MONDAY: todayKor = "월"; break;
            case TUESDAY: todayKor = "화"; break;
            case WEDNESDAY: todayKor = "수"; break;
            case THURSDAY: todayKor = "목"; break;
            case FRIDAY: todayKor = "금"; break;
            case SATURDAY: todayKor = "토"; break;
            case SUNDAY: todayKor = "일"; break;
            default: return false;
        }
        return Arrays.asList(medDays.split(",")).contains(todayKor);
    }

    private LocalTime parseTime(String timeStr, String context) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return LocalTime.of(9, 0);
        }
        try {
            return LocalTime.parse(timeStr);
        } catch (DateTimeParseException e) {
            return LocalTime.of(9, 0);
        }
    }
}