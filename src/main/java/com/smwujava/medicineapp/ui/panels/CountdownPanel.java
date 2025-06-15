package com.smwujava.medicineapp.ui.panels;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.service.Countdown;

import javax.swing.*;
import java.awt.*;

public class CountdownPanel extends JPanel {
    private final JLabel titleLabel;
    private final JLabel timerLabel;

    public CountdownPanel(DosageRecordDao dao, int userId) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS)); // 한 줄 정렬 + 자연스러운 너비
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 20, 5, 0)); // 복용약 제목과 정렬 맞춤

        // 약 복용 타이머 타이틀
        titleLabel = new JLabel("💊 약 복용 타이머");
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        // 타이머 텍스트
        timerLabel = new JLabel("⏳ 알람 없음");
        timerLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        timerLabel.setForeground(Color.GRAY);
        timerLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0)); // 간격

        // 추가
        add(titleLabel);
        add(timerLabel);

        // 카운트다운 스레드 시작
        Countdown countdown = new Countdown(dao, userId, this::updateTimerText);
        new Thread(countdown).start();

        // 사이즈 제한 (리스트 밀리는 거 방지)
        setMaximumSize(new Dimension(300, 25));
        setPreferredSize(new Dimension(300, 25));
    }

    private void updateTimerText(String text) {
        SwingUtilities.invokeLater(() -> {
            if (text.startsWith("COLOR:")) {
                timerLabel.setForeground(Color.decode("#5A9FFF"));
                timerLabel.setText(text.substring(6)); // COLOR: 제거
            } else {
                timerLabel.setForeground(Color.GRAY);
                timerLabel.setText(text);
            }
        });
    }
}