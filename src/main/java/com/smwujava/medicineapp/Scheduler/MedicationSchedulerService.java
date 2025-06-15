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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class MedicationSchedulerService {
    private final DosageRecordDao dosageRecordDao;
    private final MedicineDao medicineDao;
    private final UserPatternDao userPatternDao;

    public MedicationSchedulerService(DosageRecordDao dosageRecordDao, MedicineDao medicineDao, UserPatternDao userPatternDao) {
        this.dosageRecordDao = dosageRecordDao;
        this.medicineDao = medicineDao;
        this.userPatternDao = userPatternDao;
    }

    public void scheduleTodayMedications(int userId, JFrame parentFrame) {
        UserPattern pattern;
        List<Medicine> medicines;

        try {
            pattern = userPatternDao.findPatternByUserId(userId);
            if (pattern == null) {
                System.err.println("❌ 사용자 생활 패턴이 없습니다. 스케줄링을 중단합니다.");
                return;
            }

            medicines = medicineDao.findMedicinesByUserId(userId);
            if (medicines.isEmpty()) {
                System.out.println("ℹ️ 등록된 약이 없습니다.");
                return;
            }
        } catch (SQLException e) {
            System.err.println("❌ 스케줄링 데이터 로딩 중 DB 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        System.out.println("--- 오늘의 복용 스케줄 생성을 시작합니다 ---");
        LocalDate today = LocalDate.now();

        for (Medicine med : medicines) {
            System.out.println("\n[처리 시작] medId: " + med.getMedId() + ", 약 이름: " + med.getMedName());

            if (!shouldTakeToday(med.getMedDays())) {
                System.out.println("  -> 오늘은 복용 요일이 아닙니다. 건너뜁니다.");
                continue;
            }

            LocalTime scheduledTime = getBaseTime(pattern, med);

            if (scheduledTime == null) {
                System.err.println("  -> ❌ 복용 시간을 계산할 수 없습니다. 이 약의 스케줄링을 건너뜁니다.");
                continue;
            }
            System.out.println("  -> 계산된 복용 시각: " + scheduledTime);

            LocalDateTime scheduledDateTime = LocalDateTime.of(today, scheduledTime);

            DosageRecord record = new DosageRecord();
            record.setUserId(userId);
            record.setMedId(med.getMedId());
            record.setScheduledTime(scheduledDateTime);
            record.setActualTakenTime(null);
            record.setRescheduledTime(null);
            record.setSkipped(false);

            try {
                dosageRecordDao.insertDosageRecord(record);
                System.out.println("  -> DB에 복용 기록 생성 완료.");
                System.out.println("  -> AlarmManager에 알람 예약을 요청합니다: " + scheduledDateTime);
                AlarmManager.scheduleAlarm(parentFrame, userId, med.getMedId(), scheduledDateTime);
            } catch (SQLException e) {
                if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed")) {
                    System.out.println("  -> DEBUG: 이미 존재하는 복용 기록입니다. 새로 생성하지 않습니다.");
                } else {
                    System.err.println("  -> ❌ 복용 기록 삽입 중 오류 발생: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        System.out.println("\n--- 모든 약의 스케줄 생성이 완료되었습니다 ---");
    }

    private LocalTime getBaseTime(UserPattern pattern, Medicine med) {
        LocalTime refTime;
        String medCondition = med.getMedCondition();
        boolean isBefore = "전".equals(med.getMedTiming());
        int offset = med.getMedMinutes();

        System.out.println("  -> getBaseTime 호출됨. 조건: '" + medCondition + "', 타이밍: " + (isBefore ? "식전" : "식후") + ", 오프셋: " + offset + "분");

        // 'medCondition' 문자열에 "아침", "점심", "저녁" 키워드가 있는지 직접 확인
        if (medCondition != null && medCondition.contains("아침")) {
            refTime = isBefore ? parseTime(pattern.getBreakfastStartTime(), "아침 시작") : parseTime(pattern.getBreakfastEndTime(), "아침 종료");
        } else if (medCondition != null && medCondition.contains("점심")) {
            refTime = isBefore ? parseTime(pattern.getLunchStartTime(), "점심 시작") : parseTime(pattern.getLunchEndTime(), "점심 종료");
        } else if (medCondition != null && medCondition.contains("저녁")) {
            refTime = isBefore ? parseTime(pattern.getDinnerStartTime(), "저녁 시작") : parseTime(pattern.getDinnerEndTime(), "저녁 종료");
        } else if (medCondition != null && medCondition.contains("잠자기")) {
            refTime = parseTime(pattern.getSleepStartTime(), "취침 시간");
        } else {
            System.err.println("  -> ❌ 알 수 없는 복용 조건입니다: " + medCondition);
            return null;
        }

        if (refTime == null) return null;

        return isBefore ? refTime.minusMinutes(offset) : refTime.plusMinutes(offset);
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

    private LocalTime parseTime(String timeStr, String context) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            System.err.println("  -> ❌ " + context + " 시간이 설정되지 않아 계산에 실패했습니다.");
            return null;
        }
        try {
            return LocalTime.parse(timeStr);
        } catch (java.time.format.DateTimeParseException e) {
            System.err.println("  -> ❌ " + context + "의 시간 형식('" + timeStr + "')이 잘못되었습니다.");
            return null;
        }
    }
}