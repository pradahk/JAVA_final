package com.smwujava.medicineapp.test;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.service.UserSummaryService;
import com.smwujava.medicineapp.controller.UserSummary;

import java.util.Arrays;
import java.util.List;

public class UserSummaryServiceTest {
    public static void main(String[] args) {
        MedicineDao medicineDao = new MedicineDao();
        DosageRecordDao dosageRecordDao = new DosageRecordDao();
        UserSummaryService service = new UserSummaryService(medicineDao, dosageRecordDao);

        // 테스트용 user_id 리스트 (1, 2 등 존재할 가능성이 있는 값)
        List<Integer> testUserIds = Arrays.asList(1, 2, 3);

        List<UserSummary> summaries = service.getUserSummaries(testUserIds);

        for (UserSummary summary : summaries) {
            System.out.println("User ID: " + summary.getUserId());
            System.out.println("Medicine Count: " + summary.getMedicineCount());
            System.out.println("Success Rate: " + summary.getSuccessRate() + "%");
            System.out.println("------------");
        }
    }
}

