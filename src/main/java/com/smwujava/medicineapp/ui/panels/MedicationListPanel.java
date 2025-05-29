package com.smwujava.medicineapp.ui.panels;

import com.smwujava.medicineapp.model.DosageRecord; // DosageRecord 추가
import com.smwujava.medicineapp.model.Medicine; // Medicine 추가
import com.smwujava.medicineapp.calendar.MedicineCard; // 새로 만든 MedicineCard 임포트
import com.smwujava.medicineapp.calendar.Calendar; // Calendar (컨트롤러) 임포트

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter; // MouseAdapter는 이제 MedicineCard 내부에서만 사용되므로 필요 없으면 제거 가능
import java.awt.event.MouseEvent; // MouseEvent도 마찬가지
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors; // Collectors 임포트 추가

public class MedicationListPanel extends JPanel {
    private JPanel listContainer;
    private CardLayout cardLayout;
    private JPanel parentPanel;

    private CalendarPanel calendarPanel;
    private int selectedDay = 4; // 기본 선택 날짜
    // Calendar 클래스와의 연결을 위한 리스너 필드 추가
    private MedicineCard.MedicineCardClickListener medicineCardClickListener;

    public MedicationListPanel(CardLayout cardLayout, JPanel parentPanel) {
        this.cardLayout = cardLayout;
        this.parentPanel = parentPanel;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 상단 패널
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("복용 약 목록");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        topPanel.add(titleLabel, BorderLayout.WEST);

        JButton addButton = new JButton("+");
        addButton.setPreferredSize(new Dimension(40, 40));
        addButton.setFocusPainted(false);
        addButton.setBackground(Color.WHITE);
        addButton.addActionListener(e -> cardLayout.show(parentPanel, "SETTINGS"));
        topPanel.add(addButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // 약 카드 리스트 컨테이너
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        add(scrollPane, BorderLayout.CENTER);
    }

    // Calendar.java에서 클릭 리스너를 설정할 수 있도록 setter 추가
    public void setMedicineCardClickListener(MedicineCard.MedicineCardClickListener listener) {
        this.medicineCardClickListener = listener;
    }

    // 새로운 addMedicationCard 메서드 (이것으로 대체합니다)
    public void addMedicationCard(Medicine medicine, String timing, boolean isTaken) {
        // 기존 익명 클래스 대신 MedicineCard 사용
        MedicineCard card = new MedicineCard(medicine, timing, isTaken);

        // MedicineCard의 클릭 리스너를 설정
        // Calendar.java의 onMedicineCardClick 메서드가 이 리스너를 구현하게 됩니다.
        card.setClickListener((clickedMedicine, isTakenStatus) -> { // 두 개의 매개변수를 받도록 수정
            if (medicineCardClickListener != null) {
                // MedicineCard의 클릭 리스너가 호출될 때, MedicineCard 내부에서 토글된 상태를 `isTakenStatus`로 받습니다.
                // 이 값을 외부 리스너에 그대로 전달합니다.
                medicineCardClickListener.onMedicineCardClicked(clickedMedicine, isTakenStatus);
            }
        });

        listContainer.add(Box.createVerticalStrut(10));
        listContainer.add(card);
        // 약물 카드를 추가할 때마다 리스트를 다시 계산하고 그립니다.
        listContainer.revalidate();
        listContainer.repaint();
    }

    // 약물 목록과 복용 상태를 함께 받아와서 MedicineCard를 생성하고 업데이트하는 새로운 메서드
    public void updateMedicationsWithStatus(List<CalendarPanel.MedicationInfo> medsForDisplay, // CalendarPanel.MedicationInfo는 display용
                                            List<Medicine> medicinesForToday, // 오늘 복용할 약물 Medicine 객체 리스트
                                            List<DosageRecord> todayDosageRecords, // 오늘 복용 기록 리스트
                                            MedicineCard.MedicineCardClickListener listener) { // 클릭 리스너
        listContainer.removeAll(); // 기존 카드 모두 제거
        this.medicineCardClickListener = listener; // 전달받은 리스너를 설정

        // medsForDisplay는 CalendarPanel.MedicationInfo이므로,
        // 실제 MedicineCard를 생성하려면 Medicine 객체가 필요합니다.
        // 따라서 medicinesForToday를 순회하며 카드를 생성합니다.
        for (Medicine med : medicinesForToday) {
            // 해당 약물이 오늘 복용되었는지 확인
            boolean isTaken = todayDosageRecords.stream()
                    .anyMatch(dr -> dr.getMedId() == med.getMedId() && dr.isTaken());

            // Medicine의 복용 조건/시점/분을 바탕으로 Time String 생성
            // 이 부분은 Calendar.java의 formatMedicationTime 로직과 유사해야 합니다.
            String timeString = formatMedicationTimeForCard(med.getMedCondition(), med.getMedTiming(), med.getMedMinutes());

            // MedicineCard를 생성하고 listContainer에 추가
            addMedicationCard(med, timeString, isTaken);
        }

        revalidate();
        repaint();
    }

    public void setCalendarPanel(CalendarPanel panel) {
        this.calendarPanel = panel;
    }

    public void setSelectedDay(int day) {
        this.selectedDay = day;
    }

    // Medicine 모델의 복용 시간 정보를 문자열로 포맷하는 헬퍼 메서드 (카드 표시에 특화)
    private String formatMedicationTimeForCard(String condition, String timing, int minutes) {
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
            default:
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
                if (minutes == 0) {
                    timeString += "정각";
                } else {
                    timeString += minutes + "분 정각";
                }
                break;
            default:
                timeString += timing;
                if (minutes != 0) {
                    timeString += " (" + minutes + "분)";
                }
                break;
        }
        return timeString.trim();
    }
}