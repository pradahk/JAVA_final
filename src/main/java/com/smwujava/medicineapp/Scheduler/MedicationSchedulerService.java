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

/**
 * 사용자의 약 복용 스케줄을 생성하고 오늘 날짜의 알람을 예약하는 서비스 클래스입니다.
 * 이 클래스는 오직 등록된 정보(생활 패턴, 약 정보)를 바탕으로 초기 알람 시간을 계산하는 역할만 수행합니다.
 */
public class MedicationSchedulerService {

    private final MedicineDao medicineDao;
    private final UserPatternDao userPatternDao;
    private final DosageRecordDao dosageRecordDao;

    public MedicationSchedulerService(MedicineDao medicineDao, UserPatternDao userPatternDao, DosageRecordDao dosageRecordDao) {
        this.medicineDao = medicineDao;
        this.userPatternDao = userPatternDao;
        this.dosageRecordDao = dosageRecordDao;
    }

    /**
     * 특정 사용자의 오늘 복용해야 할 모든 약에 대한 알람을 생성하고 예약합니다.
     * 이미 복용 기록이 있는 경우 중복 생성하지 않습니다.
     * @param userId      알람을 생성할 사용자 ID
     * @param parentFrame 알람 다이얼로그를 표시할 부모 프레임
     */
    public void createAndScheduleTodayAlarms(int userId, JFrame parentFrame) {
        UserPattern pattern;
        List<Medicine> medicines;

        try {
            // 1. 스케줄링에 필요한 사용자 생활 패턴과 약 목록을 DB에서 가져옵니다.
            pattern = userPatternDao.findPatternByUserId(userId);
            if (pattern == null) {
                System.err.println("스케줄링 실패: 사용자 생활 패턴이 설정되지 않았습니다. (userId: " + userId + ")");
                return;
            }

            medicines = medicineDao.findMedicinesByUserId(userId);
            if (medicines.isEmpty()) {
                System.out.println("정보: 사용자가 등록한 약이 없습니다. (userId: " + userId + ")");
                return;
            }
        } catch (SQLException e) {
            System.err.println("오류: 스케줄링을 위한 데이터 로딩 중 DB 오류 발생. " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // 2. 각 약에 대해 오늘 복용해야 하는지 확인하고 알람 시간을 계산합니다.
        for (Medicine med : medicines) {
            if (!shouldTakeToday(med.getMedDays())) {
                continue; // 오늘 복용 요일이 아니면 건너뜁니다.
            }

            // 2-1. 약의 복용 횟수와 조건에 따라 기준 시간 목록을 가져옵니다.
            List<LocalTime> baseTimes = getBaseTimes(pattern, med);

            // 2-2. 하루 복용 횟수만큼 반복하며 알람 시간을 계산하고 설정합니다.
            for (int i = 0; i < med.getMedDailyAmount() && i < baseTimes.size(); i++) {
                LocalTime baseTime = baseTimes.get(i);

                // 최종 알람 시간 계산 (예: 식전 30분, 식후 30분)
                LocalTime scheduledTime = calculateFinalTime(baseTime, med.getMedTiming(), med.getMedMinutes());
                LocalDateTime scheduledDateTime = LocalDateTime.of(LocalDate.now(), scheduledTime);

                // 3. 계산된 알람을 DosageRecords 테이블에 기록하고 실제 알람을 예약합니다.
                createRecordAndScheduleAlarm(userId, med.getMedId(), scheduledDateTime, parentFrame);
            }
        }
        System.out.println("✅ 오늘의 약 복용 스케줄 생성이 완료되었습니다. (userId: " + userId + ")");
    }

    /**
     * 계산된 알람 정보를 DB에 기록하고, AlarmManager를 통해 실제 알람을 예약합니다.
     * DB에 동일한 약, 동일한 날짜의 기록이 이미 존재하면 중복으로 생성하지 않습니다.
     */
    private void createRecordAndScheduleAlarm(int userId, int medId, LocalDateTime scheduledDateTime, JFrame parentFrame) {
        try {
            // DosageRecord 객체 생성
            DosageRecord record = new DosageRecord();
            record.setUserId(userId);
            record.setMedId(medId);
            record.setScheduledTime(scheduledDateTime);
            record.setSkipped(false); // 초기 상태는 '건너뛰지 않음'

            // DB에 복용 기록 삽입
            dosageRecordDao.insertDosageRecord(record);
            System.out.println("알람 기록 생성: userId=" + userId + ", medId=" + medId + ", time=" + scheduledDateTime);

            // 실제 알람 예약
            AlarmManager.scheduleAlarm(parentFrame, userId, medId, scheduledDateTime);

        } catch (SQLException e) {
            // SQLite의 UNIQUE 제약 조건 위반 오류인지 확인 (중복 삽입 방지)
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println("정보: 이미 존재하는 복용 기록이므로 건너뜁니다. (userId=" + userId + ", medId=" + medId + ", time=" + scheduledDateTime + ")");
            } else {
                System.err.println("오류: 복용 기록 삽입 또는 알람 예약 중 DB 오류 발생. " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 약의 복용 조건(식사/잠자기)에 따라 기준이 될 시간 목록을 반환합니다.
     * @param pattern 사용자 생활 패턴
     * @param med 약 정보
     * @return 기준 시간(LocalTime) 리스트
     */
    private List<LocalTime> getBaseTimes(UserPattern pattern, Medicine med) {
        List<LocalTime> baseTimes = new ArrayList<>();
        String medCondition = med.getMedCondition();

        if ("식사".equals(medCondition)) {
            // "식사" 조건일 경우, 아침/점심/저녁 식사 시작 시간을 기준 시간으로 추가합니다.
            baseTimes.add(parseTime(pattern.getBreakfastStartTime(), "아침"));
            baseTimes.add(parseTime(pattern.getLunchStartTime(), "점심"));
            baseTimes.add(parseTime(pattern.getDinnerStartTime(), "저녁"));
        } else if ("잠자기".equals(medCondition)) {
            // "잠자기" 조건일 경우, 취침 시작 시간을 기준 시간으로 추가합니다.
            baseTimes.add(parseTime(pattern.getSleepStartTime(), "취침"));
        } else {
            // 알 수 없는 조건일 경우, 아침 식사 시간을 기본값으로 사용합니다.
            System.err.println("경고: 알 수 없는 복용 조건 '" + medCondition + "' 입니다. 아침 식사 시간을 기준으로 설정합니다.");
            baseTimes.add(parseTime(pattern.getBreakfastStartTime(), "아침"));
        }
        return baseTimes;
    }

    /**
     * 기준 시간과 약의 복용 시점(전/후), 분(offset)을 바탕으로 최종 알람 시간을 계산합니다.
     * @param baseTime 기준 시간 (예: 아침 식사 시간)
     * @param timing 복용 시점 ("전" 또는 "후")
     * @param minutes 오프셋(분)
     * @return 최종 계산된 알람 시간
     */
    private LocalTime calculateFinalTime(LocalTime baseTime, String timing, int minutes) {
        if ("전".equals(timing)) {
            return baseTime.minusMinutes(minutes);
        } else { // "후" 또는 명시되지 않은 경우 모두 '후'로 처리
            return baseTime.plusMinutes(minutes);
        }
    }

    /**
     * 약의 복용 요일 설정에 따라 오늘 복용해야 하는지를 판단합니다.
     * @param medDays 약에 설정된 복용 요일 문자열 (예: "월,수,금" 또는 "매일")
     * @return 오늘 복용해야 하면 true, 아니면 false
     */
    private boolean shouldTakeToday(String medDays) {
        if (medDays == null || medDays.trim().isEmpty()) {
            return false;
        }
        if (medDays.contains("매일")) {
            return true;
        }

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
        // "월,수,금" 과 같은 문자열에 오늘 요일이 포함되어 있는지 확인
        return Arrays.asList(medDays.split(",")).contains(todayKor);
    }

    /**
     * 시간 문자열(HH:mm)을 LocalTime 객체로 변환합니다. 파싱 실패 시 기본 시간을 반환합니다.
     * @param timeStr 파싱할 시간 문자열
     * @param context 어떤 시간인지 명시 (오류 메시지용)
     * @return 변환된 LocalTime 객체
     */
    private LocalTime parseTime(String timeStr, String context) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            System.err.println("경고: " + context + " 시간 정보가 비어있습니다. 기본 시간 '09:00'을 사용합니다.");
            return LocalTime.OF(9, 0);
        }
        try {
            return LocalTime.parse(timeStr);
        } catch (DateTimeParseException e) {
            System.err.println("오류: " + context + " 시간 문자열 파싱 중 오류 발생 (" + timeStr + "). 기본 시간 '09:00'을 사용합니다.");
            return LocalTime.OF(9, 0);
        }
    }
}