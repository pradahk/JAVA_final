package com.smwujava.medicineapp.service;

import com.smwujava.medicineapp.dao.UserDAO;
import com.smwujava.medicineapp.model.User;

import java.sql.Connection;
import java.sql.SQLException;

public class UserService {
    private final UserDAO userDAO;

    public UserService(Connection conn) {
        this.userDAO = new UserDAO(conn);
    }

    // 회원가입
    public boolean register(String username, String password) throws SQLException {
        // 중복 체크 로직은 생략 (필요하면 findByUsername 추가 가능)
        User user = new User(0, username, password);
        return userDAO.insertUser(user);
    }

    // 로그인
    public boolean login(String username, String password) throws SQLException {
        User user = userDAO.findUserByUsernameAndPassword(username, password);
        return user != null;
    }

    // 아이디 찾기 (비밀번호로)
    public String findUsername(String password) throws SQLException {
        return userDAO.findUsernameByPassword(password);
    }

    // 비밀번호 찾기 (아이디로)
    public String findPassword(String username) throws SQLException {
        return userDAO.findPasswordByUsername(username);
    }
}
