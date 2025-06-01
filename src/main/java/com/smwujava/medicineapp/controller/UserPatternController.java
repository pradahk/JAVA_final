package com.smwujava.medicineapp.controller;

import com.smwujava.medicineapp.dao.UserPatternDao;
import com.smwujava.medicineapp.model.UserPattern;
import com.smwujava.medicineapp.ui.panels.UserPatternInputPanel;

import javax.swing.*;
import java.awt.*;

public class UserPatternController {
    private final int userId;
    private final UserPatternDao userPatternDao;
    private final UserPatternInputPanel panel;
    private final JPanel mainPanel;
    private final CardLayout cardLayout;

    public UserPatternController(int userId, UserPatternInputPanel panel, JPanel mainPanel, CardLayout cardLayout) {
        this.userId = userId;
        this.panel = panel;
        this.userPatternDao = new UserPatternDao();
        this.mainPanel = mainPanel;
        this.cardLayout = cardLayout;
    }

    public void save() {
        if (isAnyFieldEmpty()) {
            JOptionPane.showMessageDialog(panel, "입력되지 않은 항목이 있어요!", "⚠️ 입력 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }

        UserPattern pattern = new UserPattern(
                panel.getBreakfastStartTime(), panel.getBreakfastEndTime(),
                panel.getLunchStartTime(), panel.getLunchEndTime(),
                panel.getDinnerStartTime(), panel.getDinnerEndTime(),
                panel.getSleepStartTime(), panel.getSleepEndTime()
        );
        pattern.setUserId(userId);

        boolean success = userPatternDao.insertOrUpdatePattern(pattern);

        if (success) {
            JOptionPane.showMessageDialog(panel, "저장되었습니다 😊", "✅ 저장 완료", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(mainPanel, "home");
        } else {
            JOptionPane.showMessageDialog(panel, "저장에 실패했어요 😢", "❌ 오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isAnyFieldEmpty() {
        String[] values = {
                panel.getBreakfastStartTime(), panel.getBreakfastEndTime(),
                panel.getLunchStartTime(), panel.getLunchEndTime(),
                panel.getDinnerStartTime(), panel.getDinnerEndTime(),
                panel.getSleepStartTime(), panel.getSleepEndTime()
        };
        for (String value : values) {
            if (value.trim().isEmpty() || value.trim().equals(":")) {
                return true;
            }
        }
        return false;
    }
}
