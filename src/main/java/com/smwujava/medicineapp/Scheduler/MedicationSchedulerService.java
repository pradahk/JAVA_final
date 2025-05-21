package com.smwujava.medicineapp.Scheduler;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.dao.UserPatternDao;
import com.smwujava.medicineapp.model.DosageRecord;
import com.smwujava.medicineapp.model.Medicine;
import com.smwujava.medicineapp.model.UserPattern;

import java.sql.SQLException; // SQLException 임포트 추가
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
// import java.util.Calendar; // Calendar 대신 java.time 사용 권장
import java.util.List;

public class MedicationSchedulerService {

    // DAO 인스턴스를 필드로 유지 (생성자 주입)
    private DosageRecordDao dosageRecordDao;
    private MedicineDao medicineDao;
    private UserPatternDao userPatternDao;

    // 생성자 주입
    public MedicationSchedulerService(DosageRecordDao dosageRecordDao, MedicineDao medicineDao, UserPatternDao userPatternDao) {
        this.dosageRecordDao = dosageRecordDao;
        this.medicineDao = medicineDao;
        this.userPatternDao = userPatternDao;
    }

    public void scheduleTodayMedications(int userId) {
        UserPattern pattern = null;
        List<Medicine> medicines = null;

        try {
            // 사용자 패턴 불러오기
            pattern = userPatternDao.findPatternByUserId(userId);
            if (pattern == null) {
                System.err.println("사용자 생활 패턴이 없습니다: userId = " + userId);
                return;
            }

            // 사용자 약 목록 불러오기
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
                // 이 부분은 약의 '하루 복용 횟수'와 '복용 조건' (식전/식후/수면 전)에 따라
                // 여러 개의 예정 시간을 정확하게 계산해야 합니다.
                // 현재처럼 `baseTime.plusHours(i * 4)`는 단순한 예시이며, 실제 시나리오에는 맞지 않을 수 있습니다.
                // 예: 하루 3회 (아침 식후, 점심 식후, 저녁 식후) -> 각 식사 시간에 맞춰야 합니다.
                // 이를 위해 UserPatterns에 lunch, dinner 시간도 있고, medicine에도 해당 조건이 있다면
                // 복용 조건(med_condition)과 복용 횟수(med_daily_amount)를 조합하여
                // 정확한 scheduledTime 리스트를 생성하는 로직이 필요합니다.

                LocalTime scheduledTime = baseTime.plusHours(i * 4); // 임시 로직
                LocalDateTime scheduledDateTime = LocalDateTime.of(today, scheduledTime);

                DosageRecord record = new DosageRecord();
                record.setUserId(userId);
                record.setMedId(med.getMedId());
                // recordDate 필드 설정 제거. DosageRecordDao가 scheduledTime에서 날짜를 추출합니다.
                // record.setRecordDate(today.toString()); // 이 줄을 제거했습니다!
                record.setScheduledTime(scheduledDateTime); // LocalDateTime 객체 그대로 전달
                record.setActualTakenTime(null); // 복용 전
                record.setRescheduledTime(null); // 초기 스케줄링이므로 null로 설정

                try {
                    // DB의 UNIQUE 제약 조건(user_id, med_id, record_date)을 활용하여 중복 삽입 방지
                    // 만약 이미 존재하는 기록이라면 insertDosageRecord에서 SQLException이 발생합니다.
                    dosageRecordDao.insertDosageRecord(record);
                } catch (SQLException e) {
                    if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed: DosageRecords.user_id, DosageRecords.med_id, DosageRecords.record_date")) {
                        // SQLite의 경우 UNIQUE 제약 조건 위반 메시지 확인
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

        if (pattern == null) { // 패턴 객체가 null일 경우 방어 로직 추가
            System.err.println("UserPattern is null. Cannot determine base time.");
            return LocalTime.of(9, 0); // 기본값 반환
        }

        if (medCondition == null) { // null 체크 추가
            System.err.println("Medication condition is null for medId: " + med.getMedId() + ". Defaulting to breakfast.");
            refTime = parseTime(pattern.getBreakfast());
        } else if ("식사".equals(medCondition)) {
            // "식사" 조건의 경우 아침, 점심, 저녁 중 어떤 식사를 기준으로 할지 추가 로직 필요
            // 현재 UserPattern에는 세 가지 식사 시간이 모두 있습니다.
            // Medicine 모델에 '복용 시간대' (예: 아침, 점심, 저녁) 필드를 추가하거나,
            // 'med_daily_amount'를 기반으로 적절한 식사 시간을 선택하는 로직이 필요합니다.
            // 여기서는 임시로 아침 식사 시간으로 통일합니다.
            refTime = parseTime(pattern.getBreakfast()); // TODO: 실제 시나리오에 맞게 개선
        } else if ("잠자기".equals(medCondition)) {
            refTime = parseTime(pattern.getSleep());
        } else {
            // 알 수 없는 조건일 경우 기본값 설정
            System.err.println("Unknown medication condition: " + medCondition + ". Defaulting to breakfast.");
            refTime = parseTime(pattern.getBreakfast());
        }

        int offset = med.getMedMinutes();
        return "전".equals(med.getMedTiming()) ? refTime.minusMinutes(offset) : refTime.plusMinutes(offset);
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            System.err.println("시간 문자열이 비어있습니다. 기본 시간 '09:00' 반환.");
            return LocalTime.of(9, 0); // 기본값으로 9시 반환
        }
        try {
            return LocalTime.parse(timeStr); // "HH:mm" 형식
        } catch (java.time.format.DateTimeParseException e) {
            System.err.println("시간 문자열 파싱 오류: " + timeStr + ". 기본 시간 '09:00' 반환. 오류: " + e.getMessage());
            return LocalTime.of(9, 0); // 파싱 오류 시 기본값 반환
        }
    }
}