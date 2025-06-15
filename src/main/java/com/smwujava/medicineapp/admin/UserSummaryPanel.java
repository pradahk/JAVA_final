package com.smwujava.medicineapp.admin;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.dao.UserDao;  // 추가
import com.smwujava.medicineapp.model.User;
import com.smwujava.medicineapp.controller.UserSummary;
import com.smwujava.medicineapp.service.UserSummaryService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class UserSummaryPanel extends JPanel {
    private final UserListPanel userListPanel;
    private final UserSummaryService service;
    private List<Integer> userIds;

    public UserSummaryPanel() {
        setLayout(new BorderLayout());
        this.userListPanel = new UserListPanel();
        this.service = new UserSummaryService(new MedicineDao(), new DosageRecordDao());

        add(userListPanel, BorderLayout.CENTER);

        loadUserIdsAndUpdate();
        startDailyUpdateTimer();
    }

    private void loadUserIdsAndUpdate() {
        try {
            List<User> users = UserDao.getAllNormalUsers();
            this.userIds = users.stream()
                    .map(User::getUserId)
                    .collect(Collectors.toList());
            updateSummary(userIds);
        } catch (SQLException e) {
            System.err.println("Error loading user IDs: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void updateSummary(List<Integer> userIds) {
        List<UserSummary> summaries = service.getUserSummaries(userIds);
        userListPanel.updateTable(summaries);
    }

    private void startDailyUpdateTimer() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> loadUserIdsAndUpdate());
            }
        }, 24 * 60 * 60 * 1000L, 24 * 60 * 60 * 1000L);
    }
}