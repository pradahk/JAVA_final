package com.smwujava.medicineapp.controller;

import com.smwujava.medicineapp.dao.UserDao;
import com.smwujava.medicineapp.dao.UserPatternDao;
import com.smwujava.medicineapp.model.User;
import com.smwujava.medicineapp.model.UserPattern;
import com.smwujava.medicineapp.ui.panels.LifestylePanel;

import java.sql.SQLException;

public class LifestyleController {
    private final int userId;
    private final LifestylePanel view;
    private final UserDao userDao;
    private final UserPatternDao userPatternDao;

    public LifestyleController(int userId, LifestylePanel view) {
        this.userId = userId;
        this.view = view;
        this.userDao = new UserDao();
        this.userPatternDao = new UserPatternDao();
    }

    public void loadData() {
        try {
            User user = userDao.findUserById(userId);
            UserPattern pattern = userPatternDao.findPatternByUserId(userId);

            String username = (user != null) ? user.getUsername() : "사용자 정보 없음";

            if (pattern == null) {
                pattern = new UserPattern(); // 빈 패턴 객체
            }

            view.updateDisplay(username, pattern);

        } catch (SQLException e) {
            e.printStackTrace();
            view.showError("사용자 정보를 불러오는 중 오류가 발생했습니다.");
        }
    }
}