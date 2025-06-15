package com.smwujava.medicineapp.service;

import com.smwujava.medicineapp.dao.UserDao;
import com.smwujava.medicineapp.model.User;
import com.smwujava.medicineapp.util.AutoLoginUtil;
import com.smwujava.medicineapp.model.UserRegistrationResult;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class UserService {

    private static final UserService instance = new UserService();
    private UserService() {}

    public static UserService getInstance() {
        return instance;
    }

    public UserRegistrationResult validateRegistration(String username, String password) {
        try {
            if (UserDao.findUserByUsername(username) != null) {
                return UserRegistrationResult.DUPLICATE_USERNAME;
            }
            if (password.length() < 7) {
                return UserRegistrationResult.PASSWORD_TOO_SHORT;
            }
            if (!Pattern.compile("[!@#$%^&*(),.?\":{}|<>]").matcher(password).find()) {
                return UserRegistrationResult.PASSWORD_NO_SPECIAL_CHAR;
            }
            return UserRegistrationResult.SUCCESS;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean registerUser(String username, String password) {
        try {
            User newUser = new User(username, password, false, false);
            int generatedId = UserDao.insertUser(newUser);
            return generatedId != -1;
        } catch (SQLException e) {
            System.err.println("회원가입 중 DB 오류: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public User loginAndGetUser(String username, String password, boolean rememberMe) {
        try {
            User user = UserDao.findUserByUsername(username);
            if (user != null && user.getPassword().equals(password)) {
                if (rememberMe) {
                    AutoLoginUtil.saveAutoLoginUser(user.getUserId(), user.getUsername(), user.getPassword());
                    UserDao.updateAutoLoginStatus(user.getUserId(), true);
                } else {
                    AutoLoginUtil.clearAutoLoginUser();
                    if (user.isAutoLogin()) {
                        UserDao.updateAutoLoginStatus(user.getUserId(), false);
                    }
                }
                return user; // 로그인 성공 시 User 객체 반환
            } else {
                return null; // 로그인 실패 시 null 반환
            }
        } catch (SQLException e) {
            System.err.println("로그인 중 DB 오류: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}