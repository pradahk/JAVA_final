package com.smwujava.medicineapp.test;

import com.smwujava.medicineapp.model.DosageRecord;
import com.smwujava.medicineapp.service.AlarmResponseHandler;
import com.smwujava.medicineapp.ui.panels.CalendarPanel;
import javax.swing.*;
import java.awt.*;

public class AlarmPopupWindow extends JDialog {
    private final JFrame parentFrame;
    private final DosageRecord record;
    private final AlarmResponseHandler handler;

    public AlarmPopupWindow(JFrame parentFrame, DosageRecord record, String medName, AlarmResponseHandler handler) {
        super(parentFrame, "복용 알림", true);
        this.parentFrame = parentFrame;
        this.record = record;
        this.handler = handler;

        JLabel label = new JLabel("<html><center><b>" + medName + "</b><br>복용 시간입니다!<br>어떻게 하시겠어요?</center></html>", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.PLAIN, 16));
        add(label, BorderLayout.CENTER);

        JButton nowButton = new JButton("지금 먹을게요");
        JButton laterButton = new JButton("좀 있다가 (5분 후)");
        JButton skipButton = new JButton("오늘은 스킵");

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(nowButton);
        buttonPanel.add(laterButton);
        buttonPanel.add(skipButton);
        add(buttonPanel, BorderLayout.SOUTH);

        nowButton.addActionListener(e -> {
            handler.handleUserResponse("1", this.record, this.parentFrame);
            refreshCalendarIfOpen();
            dispose();
        });
        laterButton.addActionListener(e -> {
            handler.handleUserResponse("2", this.record, this.parentFrame);
            dispose();
        });
        skipButton.addActionListener(e -> {
            handler.handleUserResponse("3", this.record, this.parentFrame);
            refreshCalendarIfOpen();
            dispose();
        });

        setSize(350, 250);
        setResizable(false);
        setLocationRelativeTo(parentFrame);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }
    private void refreshCalendarIfOpen() { /* 기존 코드 유지 */ }
}