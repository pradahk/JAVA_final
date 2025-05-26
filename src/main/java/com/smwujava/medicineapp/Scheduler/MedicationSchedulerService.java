package com.smwujava.medicineapp.Scheduler;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.dao.UserPatternDao;
import com.smwujava.medicineapp.model.DosageRecord;
import com.smwujava.medicineapp.model.Medicine;
import com.smwujava.medicineapp.model.UserPattern;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class MedicationSchedulerService {

    private DosageRecordDao dosageRecordDao;
    private MedicineDao medicineDao;
    private UserPatternDao userPatternDao;
    // private int medId; // 이 필드는 필요 없습니다. 반복문 내에서 med.getMedId()를 사용합니다.

    public MedicationSchedulerService(DosageRecordDao dosageRecordDao, MedicineDao medicineDao, UserPatternDao userPatternDao) {
        this.dosageRecordDao = dosageRecordDao;
        this.medicineDao = medicineDao;
        this.userPatternDao = userPatternDao;
    }

    public void scheduleTodayMedications(int userId) {
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
                } catch (SQLException e) {
                    // SQLite의 UNIQUE 제약 조건 메시지 확인
                    if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed: DosageRecords.user_id, DosageRecords.med_id, record_date")) {
                        // 참고: 'record_date'는 DB에 직접 저장되는 컬럼 이름이 아니라,
                        // scheduled_time에서 날짜 부분만 추출하여 중복 체크하는 로직에서 사용될 수 있는 개념입니다.
                        // SQLite의 UNIQUE 인덱스 정의가 (user_id, med_id, DATE(scheduled_time)) 형태일 때 유용합니다.
                        // 현재 DBManager.java에 정의된 CREATE TABLE DosageRecords 쿼리에서는
                        // PRIMARY KEY (record_id)만 정의되어 있습니다.
                        // 만약 실제 DB에 (user_id, med_id, DATE(scheduled_time))에 대한 UNIQUE 인덱스가 없다면
                        // 이 조건문은 항상 false가 될 것입니다.
                        // DB 스키마에 이 UNIQUE 인덱스를 추가해야 정확하게 동작합니다.
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
            refTime = parseTime(pattern.getBreakfast());
        } else if ("식사".equals(medCondition)) {
            refTime = parseTime(pattern.getBreakfast()); // TODO: 실제 시나리오에 맞게 개선
        } else if ("잠자기".equals(medCondition)) {
            refTime = parseTime(pattern.getSleep());
        } else {
            System.err.println("Unknown medication condition: " + medCondition + ". Defaulting to breakfast.");
            refTime = parseTime(pattern.getBreakfast());
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