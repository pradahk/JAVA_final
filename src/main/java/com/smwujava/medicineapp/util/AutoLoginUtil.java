package com.smwujava.medicineapp.util;

import java.io.*;

public class AutoLoginUtil {
    private static final String FILE_NAME = "auto_login.txt";

    // 자동 로그인 유저 저장
    public static void saveAutoLoginUser(String username) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            writer.write(username);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 자동 로그인 유저 불러오기
    public static String loadAutoLoginUser() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            return reader.readLine();
        } catch (IOException e) {
            return null; // 파일 없거나 에러 시 자동 로그인 안 함
        }
    }

    // 자동 로그인 정보 삭제
    public static void clearAutoLoginUser() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
    }
}

