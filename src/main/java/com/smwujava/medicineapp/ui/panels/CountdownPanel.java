package com.smwujava.medicineapp.ui.panels;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.service.Countdown;

import javax.swing.*;
import java.awt.*;

public class CountdownPanel extends JPanel {
    private final JLabel titleLabel;
    private final JLabel timerLabel;

    public CountdownPanel(DosageRecordDao dao, int userId) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));


        titleLabel = new JLabel("💊 약 복용 타이머", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        timerLabel = new JLabel("⏳ 초기화 중...", SwingConstants.CENTER);
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        add(titleLabel, BorderLayout.NORTH);
        add(timerLabel, BorderLayout.CENTER);

        Countdown countdown = new Countdown(dao, userId, this::updateTimerText);
        new Thread(countdown).start();
    }

    private void updateTimerText(String text) {
        SwingUtilities.invokeLater(() -> {
            if (text.startsWith("COLOR:")) {
                timerLabel.setForeground(Color.decode("#5A9FFF"));
                timerLabel.setText(text.substring(4)); // "RED:" 제거하고 표시
            } else {
                timerLabel.setForeground(Color.BLACK); // 기본색 복원
                timerLabel.setText(text);
            }
        });
    }

}
