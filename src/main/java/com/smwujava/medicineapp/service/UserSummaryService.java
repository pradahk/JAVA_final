package com.smwujava.medicineapp.service;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.model.DosageRecord;
import com.smwujava.medicineapp.controller.UserSummary;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.smwujava.medicineapp.dao.UserDao;
import com.smwujava.medicineapp.model.User;
import java.util.Map;
import java.util.HashMap;

public class UserSummaryService {
    private final MedicineDao medicineDao;
    private final DosageRecordDao dosageRecordDao;

    public UserSummaryService(MedicineDao medicineDao, DosageRecordDao dosageRecordDao) {
        this.medicineDao = medicineDao;
        this.dosageRecordDao = dosageRecordDao;
    }

    public List<UserSummary> getUserSummaries(List<Integer> userIds) {
        List<UserSummary> summaries = new ArrayList<>();

        Map<Integer, String> userIdToName = new HashMap<>();
        try {
            for (User user : UserDao.getAllNormalUsers()) {
                userIdToName.put(user.getUserId(), user.getUsername());
            }
        } catch (SQLException e) {
            System.err.println("Error loading usernames: " + e.getMessage());
            e.printStackTrace();
        }

        for (int userId : userIds) {
            try {
                int medCount = medicineDao.getMedicineCountByUserId(userId);

                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minusDays(6); // 일주일간 데이터

                List<DosageRecord> records = dosageRecordDao.findRecordsByUserIdAndDateRange(
                        userId,
                        startDate.toString(),
                        endDate.toString()
                );

                int total = records.size();
                int success = 0;

                for (DosageRecord record : records) {
                    System.out.println("검사 중 → " + record); // 전체 기록 출력

                    if (record.getActualTakenTime() != null && !record.isSkipped()) {
                        var baseTime = (record.getRescheduledTime() != null)
                                ? record.getRescheduledTime()
                                : record.getScheduledTime();

                        long diff = Math.abs(Duration.between(baseTime, record.getActualTakenTime()).toMinutes());
                        System.out.println(" - 시간 차이 (분): " + diff);

                        if (diff <= 15) {
                            success++;
                            System.out.println(" -  성공");
                        } else {
                            System.out.println(" -  실패: 시간 초과");
                        }
                    } else {
                        System.out.println(" -  실패: 복용 안함 or 건너뜀");
                    }
                }

                double successRate = (total == 0) ? 0.0 : (double) success / total;

                String username = userIdToName.getOrDefault(userId, "Unknown");
                summaries.add(new UserSummary(username, medCount, successRate * 100));
            } catch (SQLException e) {
                System.err.println("Error generating summary for user ID " + userId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        return summaries;
    }
}
