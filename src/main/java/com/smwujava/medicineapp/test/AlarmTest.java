package com.smwujava.medicineapp.test;

import com.smwujava.medicineapp.service.AlarmResponseHandler;

import java.time.LocalDateTime;
import java.util.Scanner;

public class AlarmTest {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int userId = 1;  // 예시 사용자 ID
        int medId = 101; // 예시 약 ID
        LocalDateTime scheduledTime = LocalDateTime.of(2025, 5, 24, 13, 0); // 예시 복용 예정 시각

        System.out.println("복용 알림입니다. 어떻게 하시겠어요?");
        System.out.println("1. 지금 먹을게요");
        System.out.println("2. 좀 있다가 먹을게요");
        System.out.println("3. 오늘은 스킵할게요");

        String choice = sc.nextLine();

        // 사용자의 응답 처리
        AlarmResponseHandler.handleUserResponse(choice, userId, medId, scheduledTime);
    }
}