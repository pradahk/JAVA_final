package com.smwujava.medicineapp;

import com.smwujava.medicineapp.dao.UserDAO;
import com.smwujava.medicineapp.service.UserService;
import com.smwujava.medicineapp.util.DBUtil;

import javax.swing.*;
import java.sql.Connection;

public class MainApp {
    public static void main(String[] args) {
        try {
            Connection conn = DBUtil.getConnection();
            UserDAO userDAO = new UserDAO(conn);
            userDAO.createTableIfNotExists();
            System.out.println("Users 테이블 초기화 완료");

            // [UserService 테스트 추가]
            UserService userService = new UserService(conn);

            // 테스트 1: 회원가입
            boolean registered = userService.register("testuser", "pass123");
            System.out.println("회원가입 성공? " + registered);

            // 테스트 2: 로그인
            boolean loggedIn = userService.login("testuser", "pass123");
            System.out.println("로그인 성공? " + loggedIn);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // [UI는 그대로 유지]
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("치매 환자 도우미");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
