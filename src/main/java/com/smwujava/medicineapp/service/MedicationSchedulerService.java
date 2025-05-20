package com.smwujava.medicineapp.service;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.dao.UserPatternDao;
import com.smwujava.medicineapp.model.DosageRecord;
import com.smwujava.medicineapp.model.Medicine;
import com.smwujava.medicineapp.model.UserPattern;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MedicationSchedulerService {

    public void scheduleTodayMedications(int userId) {
        // 사용자 패턴 불러오기
        UserPattern pattern = UserPatternDao.findPatternByUserId(userId);
        if (pattern == null) {
            System.err.println("사용자 생활 패턴이 없습니다: userId = " + userId);
            return;
        }

        // 사용자 약 목록 불러오기
        List<Medicine> medicines = MedicineDao.findMedicinesByUserId(userId);
        if (medicines.isEmpty()) {
            System.out.println("등록된 약이 없습니다: userId = " + userId);
            return;
        }

        LocalDate today = LocalDate.now();

        for (Medicine med : medicines) {
            List<String> days = Arrays.asList(med.getMedDays().split(","));
            if (!shouldTakeToday(days)) continue;

            LocalTime baseTime = getBaseTime(pattern, med);

            for (int i = 0; i < med.getMedDailyAmount(); i++) {
                LocalTime scheduledTime = baseTime.plusHours(i * 4);
                LocalDateTime scheduledDateTime = LocalDateTime.of(today, scheduledTime);

                DosageRecord record = new DosageRecord();
                record.setUserId(userId);
                record.setMedId(med.getMedId());
                record.setRecordDate(today.toString());
                record.setScheduledTime(scheduledDateTime.toString());
                record.setActualTakenTime(null); // 복용 전

                DosageRecordDao.insertDosageRecord(record);
            }
        }

        System.out.println("복용 스케줄 생성 완료: userId = " + userId);
    }

    private boolean shouldTakeToday(List<String> days) {
        if (days.contains("매일")) return true;

        String[] korDays = {"일", "월", "화", "수", "목", "금", "토"};
        int todayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        String todayKor = korDays[todayIndex];

        return days.contains(todayKor);
    }

    private LocalTime getBaseTime(UserPattern pattern, Medicine med) {
        LocalTime refTime;
        if ("식사".equals(med.getMedCondition())) {
            refTime = parseTime(pattern.getBreakfast());
        } else if ("잠자기".equals(med.getMedCondition())) {
            refTime = parseTime(pattern.getSleep());
        } else {
            refTime = parseTime(pattern.getBreakfast());
        }

        int offset = med.getMedMinutes();
        return "전".equals(med.getMedTiming()) ? refTime.minusMinutes(offset) : refTime.plusMinutes(offset);
    }

    private LocalTime parseTime(String timeStr) {
        return LocalTime.parse(timeStr); // "HH:mm" 형식
    }
}
