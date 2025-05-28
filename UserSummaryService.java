package com.smwujava.medicineapp.service;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.model.DosageRecord;
import com.smwujava.medicineapp.model.UserSummary;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserSummaryService {
    private final MedicineDao medicineDao;
    private final DosageRecordDao dosageRecordDao;

    public UserSummaryService(MedicineDao medicineDao, DosageRecordDao dosageRecordDao) {
        this.medicineDao = medicineDao;
        this.dosageRecordDao = dosageRecordDao;
    }

    public List<UserSummary> getUserSummaries(List<Integer> userIds) {
        List<UserSummary> summaries = new ArrayList<>();

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
                    if (record.getActualTakenTime() != null && !record.isSkipped()) {
                        var baseTime = (record.getRescheduledTime() != null)
                                ? record.getRescheduledTime()
                                : record.getScheduledTime();

                        long diff = Math.abs(Duration.between(baseTime, record.getActualTakenTime()).toMinutes());
                        if (diff <= 15) {
                            success++;
                        }
                    }
                }

                double successRate = (total == 0) ? 0.0 : (double) success / total;

                summaries.add(new UserSummary(String.valueOf(userId), medCount, successRate*100));
            } catch (SQLException e) {
                System.err.println("Error generating summary for user ID " + userId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        return summaries;
    }
}
