package com.smwujava.medicineapp.test;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.service.AlarmManager;
import com.smwujava.medicineapp.service.AlarmResponseHandler;
import com.smwujava.medicineapp.ui.panels.CalendarPanel;


import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

public class AlarmPopupWindow extends JDialog {

    public AlarmPopupWindow(JFrame parentFrame, int userId, int medId, String medName, LocalDateTime scheduledTime) {
        super(parentFrame, "복용 알림", true); // true: 모달창 (필요시 false로 변경 가능)

        // 메시지 라벨
        JLabel label = new JLabel("<html><center><b>" + medName + "</b><br>복용 시간입니다!<br>어떻게 하시겠어요?</center></html>", JLabel.CENTER);
        label.setFont(new Font("SansSerif", Font.PLAIN, 16));
        add(label, BorderLayout.CENTER);

        // 버튼 구성
        JButton nowButton = new JButton("지금 먹을게요");
        JButton laterButton = new JButton("좀 있다가");
        JButton skipButton = new JButton("오늘은 스킵할게요");

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        buttonPanel.add(nowButton);
        buttonPanel.add(laterButton);
        buttonPanel.add(skipButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 응답 처리 객체
        AlarmResponseHandler responseHandler = new AlarmResponseHandler(new DosageRecordDao());

        // 버튼 이벤트 연결
        nowButton.addActionListener(e -> {
            responseHandler.handleUserResponse("1", userId, medId, scheduledTime);
            refreshCalendarIfOpen();
            dispose();
        });

        laterButton.addActionListener(e -> {
            AlarmManager.snoozeAlarm(parentFrame, userId, medId, 5);
            responseHandler.handleUserResponse("2", userId, medId, scheduledTime);
            refreshCalendarIfOpen();
            dispose();
        });

        skipButton.addActionListener(e -> {
            AlarmManager.cancelAlarm(medId);
            responseHandler.handleUserResponse("3", userId, medId, scheduledTime);
            refreshCalendarIfOpen();
            dispose();
        });

        // 팝업 설정
        setSize(350, 220);
        setResizable(false);
        setLocationRelativeTo(parentFrame);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void refreshCalendarIfOpen() {
        if (CalendarWindow.isOpen()) {
            JFrame frame = CalendarWindow.getFrame();
            if (frame != null) {
                for (Component comp : frame.getContentPane().getComponents()) {
                    if (comp instanceof JPanel panel) {
                        for (Component inner : panel.getComponents()) {
                            if (inner instanceof CalendarPanel cp) {
                                cp.refresh(); // 복약 상태 반영됨
                            }
                        }
                    }
                }
            }
        }
    }
}


