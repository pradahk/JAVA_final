package com.smwujava.medicineapp.ui.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class MedicationListPanel extends JPanel {
    private JPanel listContainer;
    private CardLayout cardLayout;
    private JPanel parentPanel;

    private CalendarPanel calendarPanel;
    private int selectedDay = 4; // 기본 선택 날짜

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

        // 예시 카드
        addMedicationCard("Omeprazol (오메프라졸)", "아침 30분 전", new Color(181, 169, 255));
        addMedicationCard("Panpyrin-Q (판피린-Q)", "점심 30분 전", new Color(232, 253, 148));
        addMedicationCard("Ibuprofen (이부프로펜)", "저녁 30분 후", Color.LIGHT_GRAY);
    }

    public void addMedicationCard(String name, String timing, Color trueColor) {
        Color defaultGray = new Color(245, 245, 245);

        var card = new JPanel() {
            private Color currentColor = defaultGray;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(currentColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }

            public void switchToTrueColor() {
                this.currentColor = trueColor;
                repaint();
            }

            public void switchToDefaultColor() {
                this.currentColor = defaultGray;
                repaint();
            }

            public boolean isTrueColor() {
                return this.currentColor.equals(trueColor);
            }
        };

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(430, 60));
        card.setMaximumSize(new Dimension(430, 60));
        card.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        JLabel timeLabel = new JLabel(timing);
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        card.add(nameLabel);
        card.add(timeLabel);
        card.putClientProperty("clicked", false); // 클릭 여부 저장

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                boolean clicked = (boolean) card.getClientProperty("clicked");
                if (calendarPanel != null) {
                    if (!clicked) {
                        card.switchToTrueColor();
                        card.putClientProperty("clicked", true);
                        calendarPanel.addMedicationToDay(selectedDay,
                                new CalendarPanel.MedicationInfo(name, timing, trueColor));
                    } else {
                        card.switchToDefaultColor();
                        card.putClientProperty("clicked", false);
                        calendarPanel.removeMedicationColorFromDay(selectedDay, trueColor);
                    }
                }
            }
        });

        listContainer.add(Box.createVerticalStrut(10));
        listContainer.add(card);
        listContainer.revalidate();
        listContainer.repaint();
    }

    public void updateMedications(List<CalendarPanel.MedicationInfo> meds) {
        listContainer.removeAll();
        for (CalendarPanel.MedicationInfo med : meds) {
            addMedicationCard(med.getName(), med.getTime(), med.getColor());
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
}