package com.smwujava.medicineapp.service;

import com.smwujava.medicineapp.model.Medicine;
import com.smwujava.medicineapp.model.UserPattern;
import com.smwujava.medicineapp.model.DosageRecord;
import com.smwujava.medicineapp.dao.DosageRecordDao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 약 복용 시간을 계산하고 DB에 저장하는 서비스 클래스
 * - 사용자의 생활 패턴 및 약 정보 기반
 * - 복용 요일 및 조건(식전/식후/잠자기 전후 등)을 고려
 * - DB에 복용 스케줄 저장 (DosageRecords 테이블)
 */
public class MedicationSchedulerService {

    public void scheduleAllMedications(int userId, UserPattern pattern, List<Medicine> medicines) {
        // 1. 기존 알람 초기화
        resultAllRescheduledTimes(userId);

        LocalDate today = LocalDate.now();

        for (Medicine med : medicines) {
            List<String> days = Arrays.asList(med.getMedDays().split(","));

            if (!shouldTakeToday(days)) continue; // 오늘 복용 안 해도 되는 약은 패스

            LocalTime baseTime = getBaseTime(pattern, med);

            // 2. 하루 복용 횟수만큼 시간 분산 (4시간 간격 예시)
            for (int i = 0; i < med.getMedDailyAmount(); i++) {
                LocalTime scheduled = baseTime.plusHours(i * 4);
                LocalDateTime scheduledDateTime = LocalDateTime.of(today, scheduled);

                DosageRecord record = new DosageRecord();
                record.setUserId(userId);
                record.setMedId(med.getMedId());
                record.setRecordDate(today.toString()); // "YYYY-MM-DD"
                record.setScheduledTime(scheduledDateTime.toString()); // "YYYY-MM-DDTHH:MM"
                record.setActualTakenTime(null); // 아직 복용 안 함

                try {
                    DosageRecordDao.insertDosageRecord(record); // 정적 메서드 호출
                } catch (Exception e) {
                    System.err.println("복용 기록 저장 실패: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 오늘이 복용 요일에 해당하는지 판단
     */
    private boolean shouldTakeToday(List<String> days) {
        if (days.contains("매일")) return true;

        String[] korDays = {"일", "월", "화", "수", "목", "금", "토"};
        int todayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        String todayKor = korDays[todayIndex];

        return days.contains(todayKor);
    }

    // 복용 기준 시간 계산: 식사 or 수면 + 전/후 + 분 단위 offset
    private LocalTime getBaseTime(UserPattern pattern, Medicine med) {
        LocalTime refTime = switch (med.getMedCondition()) {
            case "식사" -> LocalTime.parse(pattern.getBreakfast());
            case "잠자기" -> LocalTime.parse(pattern.getSleep());
            default -> LocalTime.parse(pattern.getBreakfast());
        };

        int offset = med.getMedMinutes();
        return med.getMedTiming().equals("전") ? refTime.minusMinutes(offset) : refTime.plusMinutes(offset);
    }

    /**
     * 기존 재조정된 알람 초기화 (실제 동작은 타 팀 구현 예정)
     */
    private void resultAllRescheduledTimes(int userId) {
        System.out.println("사용자 [" + userId + "]의 기존 알람 초기화 호출됨");
    }
}
