package com.smwujava.medicineapp.test;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.db.DBManager;
import com.smwujava.medicineapp.service.Countdown;

public class CountdownTest {
    public static void main(String[] args) {
        DBManager.initializeDatabase();

        // 테스트할 사용자 ID 입력
        int testUserId = 1;

        // DAO 인스턴스 생성
        DosageRecordDao dao = new DosageRecordDao();

        // Countdown 실행
        Countdown countdown = new Countdown(dao, testUserId);
        Thread countdownThread = new Thread(countdown);
        countdownThread.start();

        System.out.println("[CountdownTest] 카운트다운 스레드를 시작했습니다.");
    }
}
