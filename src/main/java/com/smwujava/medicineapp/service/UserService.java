package com.smwujava.medicineapp.service;

import com.smwujava.medicineapp.dao.UserDAO;
import com.smwujava.medicineapp.model.User;
import com.smwujava.medicineapp.util.AutoLoginUtil;

import java.sql.Connection;
import java.sql.SQLException;

public class UserService {
    private final UserDAO userDAO;

    // 생성자: DAO 초기화
    public UserService(Connection conn) {
        this.userDAO = new UserDAO(conn);
    }

    // 회원가입
    public boolean register(String username, String password) throws SQLException {
        if (userDAO.existsByUsername(username)) {
            System.out.println("이미 존재하는 사용자 이름입니다.");
            return false;
        }

        if (password.length() < 7) {
            System.out.println("비밀번호는 최소 7자 이상이어야 합니다.");
            return false;
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            System.out.println("비밀번호에는 특수문자가 포함되어야 합니다.");
            return false;
        }

        User user = new User(0, username, password);
        return userDAO.insertUser(user);
    }

    // 로그인
    public boolean login(String username, String password) throws SQLException {
        User user = userDAO.findUserByUsernameAndPassword(username, password);
        return user != null;
    }

    // 비밀번호로 사용자 이름 찾기
    public String findUsername(String password) throws SQLException {
        return userDAO.findUsernameByPassword(password);
    }

    // 아이디로 비밀번호 찾기
    public String findPassword(String username) throws SQLException {
        return userDAO.findPasswordByUsername(username);
    }

    // 비밀번호 수정
    public boolean changePassword(String username, String currentPassword, String newPassword) throws SQLException {
        if (!login(username, currentPassword)) {
            System.out.println("현재 비밀번호가 일치하지 않습니다.");
            return false;
        }

        if (newPassword.length() < 7) {
            System.out.println("새 비밀번호는 최소 7자 이상이어야 합니다.");
            return false;
        }

        if (!newPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            System.out.println("새 비밀번호에는 특수문자가 포함되어야 합니다.");
            return false;
        }

        return userDAO.updatePassword(username, newPassword);
    }

    // 사용자 이름 수정
    public boolean changeUsername(String currentUsername, String newUsername) throws SQLException {
        if (userDAO.existsByUsername(newUsername)) {
            System.out.println("이미 존재하는 아이디입니다.");
            return false;
        }

        return userDAO.updateUsername(currentUsername, newUsername);
    }

    // 로그아웃 기능 (자동 로그인 정보 삭제)
    public void logout() {
        AutoLoginUtil.clearAutoLoginUser();
        System.out.println("로그아웃되었습니다. 자동 로그인 정보가 삭제되었습니다.");
    }
}


