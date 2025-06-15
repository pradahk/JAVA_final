package com.smwujava.medicineapp.ui.panels;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.calendar.CalendarMedicineCard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.time.LocalDate;

public class MedicationListPanel extends JPanel {
    private JPanel listContainer;
    private CardLayout cardLayout;
    private JPanel parentPanel;
    private CalendarPanel calendarPanel;
    private JLabel titleLabel;
    private CountdownPanel countdownPanel; // CountdownPanel을 필드로 유지
    private final int currentUserId;

    public MedicationListPanel(CardLayout cardLayout, JPanel parentPanel, int userId) {
        this.cardLayout = cardLayout;
        this.parentPanel = parentPanel;
        this.currentUserId = userId;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        titleLabel = new JLabel("복용 약 목록");
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

        // CountdownPanel 생성 및 listContainer에 초기 추가
        countdownPanel = new CountdownPanel(new DosageRecordDao(), this.currentUserId);
        countdownPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        listContainer.add(countdownPanel);
        listContainer.add(Box.createVerticalStrut(10));

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void updateMedications(List<CalendarPanel.MedicationInfo> meds) {
        listContainer.removeAll(); // 모든 컴포넌트 제거 (CountdownPanel 포함)

        // CountdownPanel 다시 추가 (항상 최상단에 위치)
        if (countdownPanel == null) { // 혹시 모를 경우 대비
            countdownPanel = new CountdownPanel(new DosageRecordDao(), this.currentUserId);
            countdownPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        listContainer.add(countdownPanel);
        listContainer.add(Box.createVerticalStrut(10));

        if (meds == null || meds.isEmpty()) {
            JLabel noMedsLabel = new JLabel("선택된 날짜에 복용할 약물이 없습니다.");
            noMedsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            noMedsLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            noMedsLabel.setForeground(Color.GRAY);
            noMedsLabel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
            noMedsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            listContainer.add(noMedsLabel);
        } else {
            for (CalendarPanel.MedicationInfo medInfo : meds) {
                CalendarMedicineCard card = new CalendarMedicineCard(medInfo);
                card.setAlignmentX(Component.LEFT_ALIGNMENT);

                card.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (calendarPanel != null) {
                            CalendarPanel.MedicationInfo clickedMedInfo = card.getMedicationInfo();
                            calendarPanel.notifyMedicationStatusChanged(
                                    clickedMedInfo.getMedId(),
                                    !clickedMedInfo.isTaken()
                            );
                        }
                    }
                });
                listContainer.add(card);
                listContainer.add(Box.createVerticalStrut(10));
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
            LocalDate dateToDisplay = LocalDate.now();
            if (calendarPanel != null && calendarPanel.getController() != null && calendarPanel.getController().getCurrentYearMonth() != null) {
                try {
                    dateToDisplay = calendarPanel.getController().getCurrentYearMonth().atDay(day);
                } catch (Exception e) { /* 유효하지 않은 날짜 예외 처리 */ }
                titleLabel.setText(dateToDisplay.getMonthValue() + "월 " + day + "일 복용 약물");
            } else {
                titleLabel.setText(day + "일 (정보 부족)");
            }
        }
    }
}