package com.smwujava.medicineapp.Scheduler;

import com.smwujava.medicineapp.dao.*;
import com.smwujava.medicineapp.model.*;
import com.smwujava.medicineapp.service.*;
import javax.swing.JFrame;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;

public class MedicationSchedulerService {
    private final MedicineDao medicineDao;
    private final UserPatternDao userPatternDao;
    private final DosageRecordDao dosageRecordDao;
    private final AlarmManager alarmManager;

    public MedicationSchedulerService(MedicineDao mDao, UserPatternDao pDao, DosageRecordDao dDao, AlarmManager aManager) {
        this.medicineDao = mDao;
        this.userPatternDao = pDao;
        this.dosageRecordDao = dDao;
        this.alarmManager = aManager;
    }

    public void createAndScheduleTodayAlarms(JFrame parentFrame, AlarmResponseHandler handler) {
        UserPattern pattern;
        List<Medicine> medicines;
        try {
            pattern = userPatternDao.findPatternByUserId(1); // 예시 userId
            medicines = medicineDao.findMedicinesByUserId(1); // 예시 userId
            if (pattern == null || medicines.isEmpty()) return;
        } catch (SQLException e) { return; }

        for (Medicine med : medicines) {
            if (!shouldTakeToday(med.getMedDays())) continue;
            List<LocalTime> baseTimes = getBaseTimes(pattern, med);
            for (int i = 0; i < med.getMedDailyAmount() && i < baseTimes.size(); i++) {
                LocalTime scheduledTime = calculateFinalTime(baseTimes.get(i), med.getMedTiming(), med.getMedMinutes());
                createRecordAndScheduleAlarm(1, med.getMedId(), LocalDateTime.of(LocalDate.now(), scheduledTime), parentFrame, handler);
            }
        }
    }

    private void createRecordAndScheduleAlarm(int userId, int medId, LocalDateTime scheduledDateTime, JFrame parentFrame, AlarmResponseHandler handler) {
        try {
            DosageRecord record = new DosageRecord();
            record.setUserId(userId);
            record.setMedId(medId);
            record.setScheduledTime(scheduledDateTime);
            int generatedRecordId = dosageRecordDao.insertDosageRecord(record);
            if (generatedRecordId != -1) {
                record.setRecordId(generatedRecordId);
                this.alarmManager.scheduleAlarm(parentFrame, record, handler);
            }
        } catch (SQLException e) { /* ... */ }
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