package com.smwujava.medicineapp.calendar; // 패키지 경로 수정됨

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.db.DBManager;
import com.smwujava.medicineapp.model.DosageRecord;
import com.smwujava.medicineapp.model.Medicine;
import com.smwujava.medicineapp.ui.panels.CalendarPanel;
import com.smwujava.medicineapp.ui.panels.MedicationListPanel;
import com.smwujava.medicineapp.ui.components.CalendarDayPanel; // CalendarDayPanel은 components 패키지에 있음
import java.time.format.DateTimeFormatter;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Calendar {
    private CalendarPanel calendarPanel;
    private MedicationListPanel medicationListPanel;
    private MedicineDao medicineDao;
    private DosageRecordDao dosageRecordDao;
    private final int currentUserId = 1; // 임시 사용자 ID

    public Calendar(CardLayout cardLayout, JPanel parentPanel) {
        DBManager.initializeDatabase();

        this.medicineDao = new MedicineDao();
        this.dosageRecordDao = new DosageRecordDao();
        this.calendarPanel = new CalendarPanel(cardLayout, parentPanel);
        this.medicationListPanel = new MedicationListPanel(cardLayout, parentPanel);
        // 이 연결은 MedicationListPanel이 CalendarPanel의 add/remove 메서드를 호출하는 데 필요합니다.
        this.medicationListPanel.setCalendarPanel(this.calendarPanel);

        // 초기 더미 약물 데이터 삽입 (테스트용)
        insertDummyMedicineData();
        // 오늘 날짜의 약물 목록과 캘린더를 초기화 및 표시
        loadAndDisplayTodayMedications();
        loadAndDisplayCalendarPillColors();
    }

    // 메인 애플리케이션에서 CalendarPanel을 가져갈 수 있도록
    public CalendarPanel getCalendarPanel() {
        return calendarPanel;
    }

    // 메인 애플리케이션에서 MedicationListPanel을 가져갈 수 있도록
    public MedicationListPanel getMedicationListPanel() {
        return medicationListPanel;
    }

    // 테스트를 위한 더미 약물 데이터 삽입 메서드
    private void insertDummyMedicineData() {
        try {
            // 이미 데이터가 있는지 확인하여 중복 삽입 방지
            if (medicineDao.getMedicineCountByUserId(currentUserId) == 0) {
                System.out.println("Dummy medicine data not found, inserting...");
                medicineDao.insertMedicine(new Medicine(currentUserId, "오메프라졸", 1, "월,화,수,목,금,토,일", "식사", "전", 30, "#B5A9FF")); // 보라색
                medicineDao.insertMedicine(new Medicine(currentUserId, "판피린-Q", 1, "월,수,금", "식사", "후", 30, "#E8FD94")); // 연두색
                medicineDao.insertMedicine(new Medicine(currentUserId, "이부프로펜", 1, "화,목", "식사", "후", 60, "#C0C0C0")); // 회색
                medicineDao.insertMedicine(new Medicine(currentUserId, "타이레놀", 1, "월,화,수,목,금,토,일", "식사", "후", 0, "#FF6666")); // 빨간색
                System.out.println("Dummy medicine data inserted.");
            } else {
                System.out.println("Dummy medicine data already exists.");
            }
        } catch (SQLException e) {
            System.err.println("Error inserting dummy medicine data: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // 오늘 날짜의 약물 목록을 DB에서 로드하여 MedicationListPanel에 표시
    private void loadAndDisplayTodayMedications() {
        LocalDate today = LocalDate.now();
        int todayDayOfMonth = today.getDayOfMonth();
        DayOfWeek todayDayOfWeek = today.getDayOfWeek();
        String todayDayOfWeekString = todayDayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN);

        try {
            List<Medicine> allMedicines = medicineDao.findMedicinesByUserId(currentUserId);

            // LocalDate를 String (yyyy-MM-dd)으로 변환하여 전달
            String todayDateString = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<DosageRecord> todayDosageRecords = dosageRecordDao.findRecordsByUserIdAndDate(currentUserId, todayDateString);

            List<Medicine> medicinesForToday = allMedicines.stream()
                    .filter(med -> {
                        String[] days = med.getMedDays().split(",");
                        for (String day : days) {
                            if (day.trim().equals(todayDayOfWeekString.replace("요일", ""))) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .collect(Collectors.toList());

            // MedicationListPanel에 전달할 MedicationInfo 리스트 준비
            List<CalendarPanel.MedicationInfo> medsForDisplay = new ArrayList<>();
            for (Medicine med : medicinesForToday) {
                // 해당 약물이 오늘 복용되었는지 확인
                boolean isTaken = todayDosageRecords.stream()
                        .anyMatch(dr -> dr.getMedId() == med.getMedId() && dr.isTaken());

                String timeString = formatMedicationTime(med.getMedCondition(), med.getMedTiming(), med.getMedMinutes());

                medsForDisplay.add(new CalendarPanel.MedicationInfo(med.getMedName(), timeString, Color.decode(med.getColor())));
            }

            medicationListPanel.updateMedicationsWithStatus(medsForDisplay, medicinesForToday, todayDosageRecords, this::onMedicineCardClick);

            calendarPanel.setSelectedDay(todayDayOfMonth);

        } catch (SQLException e) {
            System.err.println("Error loading today's medications: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "오늘의 약물 정보를 불러오는 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }


    // CalendarPanel의 날짜별 알약 색상을 DB 복용 기록에 따라 업데이트
    // CalendarPanel의 날짜별 알약 색상을 DB 복용 기록에 따라 업데이트
    private void loadAndDisplayCalendarPillColors() {
        LocalDate startOfMonth = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

        try {
            // LocalDate를 String (yyyy-MM-dd)으로 변환
            String startDateString = startOfMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String endDateString = endOfMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // 해당 월 전체의 실제 복용 완료된 기록을 가져옴
            List<DosageRecord> monthlyDosageRecords = dosageRecordDao.findRecordsByUserIdAndDateRange(
                    currentUserId, startDateString, endDateString // 여기에 String으로 변환된 값을 전달
            );

            // ... (나머지 코드) ...

        } catch (SQLException e) {
            System.err.println("Error loading calendar pill colors: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "달력 정보를 불러오는 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    // MedicineCard에서 클릭 이벤트 발생 시 호출될 콜백 메서드
    // 이 메서드는 MedicineCardClickListener 인터페이스의 구현체가 됩니다.
    public void onMedicineCardClick(Medicine medicine, boolean isTaken) {
        LocalDate today = LocalDate.now();
        // 실제로는 약물의 복용 패턴 (med_days, med_condition, med_timing, med_minutes)을 기반으로
        // DosageRecord의 scheduled_time을 정확히 계산하여 찾거나 생성해야 합니다.
        // 현재는 편의상 해당 약물의 오늘 자정 기준 예정 기록을 찾거나, 없으면 새로 생성합니다.

        // 오늘 날짜의 해당 약물에 대한 DosageRecord를 찾기
        // 실제로는 DosageRecord의 scheduled_time이 약의 복용 패턴과 일치해야 하지만
        // 여기서는 오늘 날짜에 해당 약물에 대한 가장 최근의 DosageRecord를 찾거나,
        // 아니면 해당 약물에 대한 스케줄링이 된 가장 가까운 미래의 DosageRecord를 찾아야 합니다.
        // 현재 DB 스키마는 스케줄링 타임이 유니크 제약이 있으므로,
        // 미리 스케줄링된 특정 복용 시점 (예: 아침 식전 30분)의 레코드를 찾아야 합니다.
        // 이를 위해 'ScheduledTime'을 정확히 계산해야 합니다.

        // 예시: 오늘 날짜에 대한 이 약물의 예정된 첫 번째 복용 시간 (간단화)
        LocalDateTime scheduledTimeForToday = calculateScheduledTimeForMedicine(today, medicine);


        try {
            DosageRecord record = dosageRecordDao.findRecordByUserIdMedIdAndScheduledTime(
                    currentUserId, medicine.getMedId(), scheduledTimeForToday
            );

            if (record == null) {
                // 해당 예정 시간에 기록이 없으면 새로 생성 (이는 약물 스케줄링 로직이 미리 해야 할 일)
                // 현재는 약물 카드를 클릭했을 때만 해당 기록을 만듭니다.
                record = new DosageRecord(currentUserId, medicine.getMedId(), scheduledTimeForToday);
                dosageRecordDao.insertDosageRecord(record);
                // 삽입 후 recordId를 얻기 위해 DosageRecordDao의 insertDosageRecord 리턴 값 사용
                record.setRecordId(dosageRecordDao.findRecordByUserIdMedIdAndScheduledTime(currentUserId, medicine.getMedId(), scheduledTimeForToday).getRecordId()); // 다시 조회
            }

            // 실제 복용 시간 업데이트 또는 제거
            if (isTaken) {
                dosageRecordDao.updateActualTakenTime(record.getRecordId(), LocalDateTime.now(), false);
                // 캘린더에 색상 추가
                calendarPanel.addMedicationToDay(today.getDayOfMonth(),
                        new CalendarPanel.MedicationInfo(medicine.getMedName(), formatMedicationTime(medicine.getMedCondition(), medicine.getMedTiming(), medicine.getMedMinutes()), Color.decode(medicine.getColor())));
            } else {
                dosageRecordDao.updateActualTakenTime(record.getRecordId(), null, false); // null로 설정하여 복용 취소
                // 캘린더에서 색상 제거
                calendarPanel.removeMedicationColorFromDay(today.getDayOfMonth(), Color.decode(medicine.getColor()));
            }

        } catch (SQLException e) {
            System.err.println("Error updating dosage record for med ID " + medicine.getMedId() + ": " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "복용 기록 업데이트 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Medicine 모델의 복용 시간 정보를 문자열로 포맷하는 헬퍼 메서드
    private String formatMedicationTime(String condition, String timing, int minutes) {
        if (condition == null || timing == null) {
            return "시간 정보 없음";
        }
        String timeString = "";
        switch (condition) {
            case "식사":
                timeString = "식사 ";
                break;
            case "취침":
                timeString = "취침 ";
                break;
            default: // 다른 조건들도 처리
                timeString = condition + " ";
                break;
        }

        switch (timing) {
            case "전":
                timeString += minutes + "분 전";
                break;
            case "후":
                timeString += minutes + "분 후";
                break;
            case "정각":
                if (minutes == 0) { // minutes가 0이면 '정각'이 더 자연스러움
                    timeString += "정각";
                } else { // '정각'인데 minutes가 있으면 추가 정보
                    timeString += minutes + "분 정각"; // 이 경우는 드물지만 처리
                }
                break;
            default: // 다른 시점들도 처리
                timeString += timing; // 예를 들어 "점심" 같은 경우
                if (minutes != 0) {
                    timeString += " (" + minutes + "분)";
                }
                break;
        }
        return timeString.trim();
    }

    // Medicine 객체의 복용 조건/시점/분을 기반으로 예정 시간을 계산하는 헬퍼 메서드
    // 이 메서드는 실제 약물 스케줄링 로직의 일부가 됩니다.
    private LocalDateTime calculateScheduledTimeForMedicine(LocalDate date, Medicine medicine) {
        LocalTime baseTime;
        // 복용 조건에 따라 기준 시간 설정
        switch (medicine.getMedCondition()) {
            case "식사":
                // 아침, 점심, 저녁 식사 시간을 가정한 임의의 시간 (실제 앱에서는 사용자 설정에 따라 달라짐)
                // 여기서는 편의상 하루의 첫 복용 시간을 기준으로 합니다.
                baseTime = LocalTime.of(8, 0); // 예: 오전 8시 (아침 식사 시간)
                break;
            case "취침":
                baseTime = LocalTime.of(22, 0); // 예: 오후 10시 (취침 시간)
                break;
            default:
                baseTime = LocalTime.NOON; // 기본값으로 정오
                break;
        }

        // 복용 시점과 분에 따라 최종 예정 시간 계산
        if ("전".equals(medicine.getMedTiming())) {
            baseTime = baseTime.minusMinutes(medicine.getMedMinutes());
        } else if ("후".equals(medicine.getMedTiming())) {
            baseTime = baseTime.plusMinutes(medicine.getMedMinutes());
        }
        // "정각"의 경우 baseTime 그대로 사용

        return LocalDateTime.of(date, baseTime);
    }
}