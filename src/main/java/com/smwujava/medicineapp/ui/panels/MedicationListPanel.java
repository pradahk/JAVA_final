package com.smwujava.medicineapp.ui.panels;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.calendar.CalendarMedicineCard; // 수정된 카드 사용

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.time.LocalDate; // setSelectedDay 에서 사용

public class MedicationListPanel extends JPanel {
    private JPanel listContainer;
    private CardLayout cardLayout;
    private JPanel parentPanel;

    private CalendarPanel calendarPanel;
    // private int selectedDay = 4; // CalendarController가 선택된 날짜의 데이터를 직접 전달
    private JLabel titleLabel;


    public MedicationListPanel(CardLayout cardLayout, JPanel parentPanel) {
        this.cardLayout = cardLayout;
        this.parentPanel = parentPanel;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        titleLabel = new JLabel("복용 약 목록"); // 초기 제목
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton addButton = new JButton("+");
        addButton.setPreferredSize(new Dimension(35, 35));
        addButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        addButton.setFocusPainted(false);
        addButton.setBackground(new Color(220, 220, 220));
        addButton.addActionListener(e -> {
            if (this.cardLayout != null && this.parentPanel != null) {
                this.cardLayout.show(this.parentPanel, "SETTINGS");
            }
        });
        headerPanel.add(addButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(Color.WHITE);
        listContainer.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));

        // CountdownPanel 원래대로 추가
        CountdownPanel countdownPanel = new CountdownPanel(new DosageRecordDao(), 1); // userId는 Controller에서 받아오는 것이 이상적
        countdownPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // BoxLayout 사용 시 정렬 중요
        listContainer.add(countdownPanel);
        listContainer.add(Box.createVerticalStrut(10));


        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }

    // CalendarController에 의해 호출됨
    public void updateMedications(List<CalendarPanel.MedicationInfo> meds) {
        // CountdownPanel을 제외한 약물 카드들만 지우기
        Component[] components = listContainer.getComponents();
        for (int i = components.length - 1; i >= 0; i--) {
            if (components[i] instanceof CalendarMedicineCard || components[i] instanceof Box.Filler || components[i] instanceof JLabel && !"복용 약 목록".equals(((JLabel)components[i]).getText()) && !(components[i] instanceof CountdownPanel && i==0) ) {
                // CountdownPanel이 아니고, "약물 없음" 레이블이거나, 카드이거나, 카드 간 간격이면 제거
                // 더 간단하게는, CountdownPanel만 남기고 다 지운 후 다시 채우는 방식도 있음.
                // 여기서는 CountdownPanel을 제외하고 뒤에서부터 제거 시도
                if (!(components[i] instanceof CountdownPanel && i == 0) && !(components[i] instanceof Box.Filler && i == 1 && listContainer.getComponent(0) instanceof CountdownPanel) ) {
                    listContainer.remove(components[i]);
                }
            }
        }
        // 만약 CountdownPanel과 그 아래 strut만 남기는게 복잡하면, 아래처럼 처리
        /*
        Component countdownComp = null;
        if (listContainer.getComponentCount() > 0 && listContainer.getComponent(0) instanceof CountdownPanel) {
            countdownComp = listContainer.getComponent(0);
        }
        listContainer.removeAll();
        if (countdownComp != null) {
            listContainer.add(countdownComp);
            listContainer.add(Box.createVerticalStrut(10));
        } else { // Fallback
            CountdownPanel newCp = new CountdownPanel(new DosageRecordDao(), 1);
            newCp.setAlignmentX(Component.LEFT_ALIGNMENT);
            listContainer.add(newCp);
            listContainer.add(Box.createVerticalStrut(10));
        }
        */


        if (meds == null || meds.isEmpty()) {
            // "약물 없음" 메시지를 표시하기 전에, CountdownPanel 뒤에 있는 기존 카드/메시지가 확실히 제거되었는지 확인
            // 위에서 이미 제거 로직이 있으나, 더 확실히 하려면 CountdownPanel을 제외하고 다시 다 지울 수 있음
            boolean hasOtherComponents = false;
            for(Component c : listContainer.getComponents()){
                if(!(c instanceof CountdownPanel) && !(c instanceof Box.Filler && listContainer.getComponent(0) instanceof CountdownPanel && listContainer.getComponent(1) == c)){
                    hasOtherComponents = true;
                    break;
                }
            }
            if(!hasOtherComponents){ // CountdownPanel과 그 아래 strut 외에 다른게 없다면 메시지 추가
                JLabel noMedsLabel = new JLabel("선택된 날짜에 복용할 약물이 없습니다.");
                noMedsLabel.setHorizontalAlignment(SwingConstants.CENTER);
                noMedsLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
                noMedsLabel.setForeground(Color.GRAY);
                noMedsLabel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
                noMedsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                listContainer.add(noMedsLabel);
            }

        } else {
            for (CalendarPanel.MedicationInfo medInfo : meds) {
                // CalendarMedicineCard 사용
                CalendarMedicineCard card = new CalendarMedicineCard(medInfo);
                card.setAlignmentX(Component.LEFT_ALIGNMENT);

                card.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (calendarPanel != null) {
                            CalendarPanel.MedicationInfo clickedMedInfo = card.getMedicationInfo();
                            calendarPanel.notifyMedicationStatusChanged(
                                    clickedMedInfo.getMedId(),
                                    !clickedMedInfo.isTaken() // 현재 상태의 반대로 토글
                            );
                        }
                    }
                });
                listContainer.add(card);
                listContainer.add(Box.createVerticalStrut(10)); // 카드 사이 간격
            }
        }
        listContainer.revalidate();
        listContainer.repaint();
    }

    public void setCalendarPanel(CalendarPanel panel) {
        this.calendarPanel = panel;
    }

    public void setSelectedDay(int day) {
        if (titleLabel != null) {
            LocalDate dateToDisplay = LocalDate.now(); // 기본값
            if (calendarPanel != null && calendarPanel.getController() != null && calendarPanel.getController().getCurrentYearMonth() != null) {
                try {
                    dateToDisplay = calendarPanel.getController().getCurrentYearMonth().atDay(day);
                } catch (Exception e) {
                    // 유효하지 않은 날짜일 경우 (예: 2월 30일)
                }
                titleLabel.setText(dateToDisplay.getMonthValue() + "월 " + day + "일 복용 약물");
            } else {
                titleLabel.setText(day + "일 (정보 부족)");
            }
        }
    }
}