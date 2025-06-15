package com.smwujava.medicineapp.test;

import com.smwujava.medicineapp.MainApp;
import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.service.AlarmResponseHandler;
import com.smwujava.medicineapp.ui.panels.CalendarPanel;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

public class AlarmPopupWindow extends JDialog {

    public AlarmPopupWindow(JFrame parentFrame, int userId, int medId, String medName, LocalDateTime scheduledTime) {
        super(parentFrame, "복용 알림", true);

        // UI 요소 생성
        JLabel label = new JLabel("<html><center><b>" + medName + "</b><br>복용 시간입니다!<br>어떻게 하시겠어요?</center></html>", JLabel.CENTER);
        label.setFont(new Font("SansSerif", Font.PLAIN, 16));
        add(label, BorderLayout.CENTER);

        JButton nowButton = new JButton("지금 먹을게요");
        JButton laterButton = new JButton("좀 있다가 먹을게요");
        JButton skipButton = new JButton("오늘은 건너뛸게요");

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(nowButton);
        buttonPanel.add(laterButton);
        buttonPanel.add(skipButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 사용자 입력 처리
        AlarmResponseHandler responseHandler = new AlarmResponseHandler(new DosageRecordDao());

        // 버튼 이벤트 리스너
        nowButton.addActionListener(e -> {
            responseHandler.handleUserResponse("1", userId, medId, scheduledTime);
            refreshCalendarIfOpen();
            dispose();
        });

        laterButton.addActionListener(e -> {
            responseHandler.handleUserResponse("2", userId, medId, scheduledTime);
            dispose();
        });

        skipButton.addActionListener(e -> {
            responseHandler.handleUserResponse("3", userId, medId, scheduledTime);
            refreshCalendarIfOpen();
            dispose();
        });

        // 팝업창 설정
        setSize(350, 250);
        setResizable(false);
        setLocationRelativeTo(parentFrame);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    /**
     * 메인 캘린더 창이 열려있을 경우, 그 안의 CalendarPanel을 찾아 새로고침합니다.
     */
    private void refreshCalendarIfOpen() {
        // MainApp을 통해 메인 프레임이 열려있는지 확인합니다.
        if (MainApp.isRunning()) {
            JFrame mainFrame = MainApp.getFrame();
            if (mainFrame != null) {
                // 프레임의 모든 구성 요소를 재귀적으로 탐색하여 CalendarPanel을 찾습니다.
                findAndRefreshCalendarPanel(mainFrame);
            }
        }
    }

    /**
     * 지정된 컨테이너(프레임 또는 패널)와 그 자식들 안에서 CalendarPanel을 찾아 refresh()를 호출합니다.
     * @param container 탐색을 시작할 컨테이너
     */
    private void findAndRefreshCalendarPanel(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof CalendarPanel) {
                // CalendarPanel을 찾았으면 즉시 새로고침하고 탐색을 종료합니다.
                ((CalendarPanel) comp).refresh();
                return;
            } else if (comp instanceof Container) {
                // 하위 컨테이너가 있다면 재귀적으로 계속 탐색합니다.
                findAndRefreshCalendarPanel((Container) comp);
            }
        }
    }
}