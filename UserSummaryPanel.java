package com.smwujava.medicineapp.admin;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.dao.UserDao;  // 추가
import com.smwujava.medicineapp.model.User;
import com.smwujava.medicineapp.model.UserSummary;
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
    private List<Integer> userIds;  // final 제거: DB에서 매번 새로 가져올 수도 있으니

    public UserSummaryPanel() {
        setLayout(new BorderLayout());
        this.userListPanel = new UserListPanel();
        this.service = new UserSummaryService(new MedicineDao(), new DosageRecordDao());

        add(userListPanel, BorderLayout.CENTER);

        loadUserIdsAndUpdate();    // 최초 데이터 로드 및 갱신
        startDailyUpdateTimer();   // 24시간마다 자동 갱신
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
        Timer timer = new Timer(true); // 데몬 쓰레드
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> loadUserIdsAndUpdate());  // 매일 전체 유저 리스트부터 다시 로드 후 갱신
            }
        }, 24 * 60 * 60 * 1000L, 24 * 60 * 60 * 1000L); // 24시간마다 실행
    }
}
