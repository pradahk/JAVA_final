package com.smwujava.medicineapp.service;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.model.DosageRecord;
import javax.swing.JFrame;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class AlarmResponseHandler {
    private final DosageRecordDao dosageRecordDao;
    private final AlarmManager alarmManager;

    public AlarmResponseHandler(DosageRecordDao dosageRecordDao, AlarmManager alarmManager) {
        this.dosageRecordDao = dosageRecordDao;
        this.alarmManager = alarmManager;
    }

    public void handleUserResponse(String response, DosageRecord record, JFrame parentFrame) {
        if (record == null) return;
        try {
            switch (response) {
                case "1":
                    record.setActualTakenTime(LocalDateTime.now());
                    record.setSkipped(false);
                    dosageRecordDao.updateDosageRecord(record);
                    break;
                case "2":
                    alarmManager.snooze(parentFrame, record, 5, this); // 스누즈 요청
                    break;
                case "3":
                    record.setSkipped(true);
                    record.setActualTakenTime(LocalDateTime.now());
                    dosageRecordDao.updateDosageRecord(record);
                    break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}