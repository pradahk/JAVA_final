package com.smwujava.medicineapp.model;

import java.time.LocalDateTime;

public class DosageRecord {
    private int id;
    private int recordId;
    private int userId;
    private int medId;
    private LocalDateTime scheduledTime;
    private LocalDateTime actualTakenTime;
    private LocalDateTime rescheduledTime;
    private boolean isSkipped;

    public DosageRecord() {
    }

    public DosageRecord(int userId, int medId, LocalDateTime scheduledTime) {
    }

    public DosageRecord(int recordId, int userId, int medId, LocalDateTime scheduledTime, LocalDateTime actualTakenTime, LocalDateTime rescheduledTime, boolean isSkipped) {
        this.recordId = recordId;
        this.userId = userId;
        this.medId = medId;
        this.scheduledTime = scheduledTime;
        this.actualTakenTime = actualTakenTime;
        this.rescheduledTime = rescheduledTime;
        this.isSkipped = isSkipped;
    }

    public DosageRecord(int userId, int medId, LocalDateTime scheduledTime, LocalDateTime actualTakenTime, LocalDateTime rescheduledTime, boolean isSkipped) {
        this.userId = userId;
        this.medId = medId;
        this.scheduledTime = scheduledTime;
        this.actualTakenTime = actualTakenTime;
        this.rescheduledTime = rescheduledTime;
        this.isSkipped = isSkipped;

    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRecordId() {return recordId;}
    public int getUserId() {return userId;}
    public int getMedId() {return medId;}
    public LocalDateTime getScheduledTime() {return scheduledTime;}
    public LocalDateTime getActualTakenTime() {return actualTakenTime;}
    public LocalDateTime getRescheduledTime() {return rescheduledTime;}

    /**
     * 복용 여부를 확인합니다.
     * 실제 복용 시간이 기록되어 있고, 건너뛰지 않았을 경우에만 true를 반환합니다.
     * @return 복용했으면 true, 아니면 false
     */
    public boolean isTaken() {
        return actualTakenTime != null && !isSkipped;
    }

    public boolean isSkipped() { //변경점 7: isSkipped Getter 추가
        return isSkipped;
    }

    public void setRecordId(int recordId) {this.recordId = recordId;}
    public void setUserId(int userId) {this.userId = userId;}
    public void setMedId(int medId) {this.medId = medId;}
    public void setScheduledTime(LocalDateTime scheduledTime) {this.scheduledTime = scheduledTime;}
    public void setActualTakenTime(LocalDateTime actualTakenTime) {this.actualTakenTime = actualTakenTime;}
    public void setRescheduledTime(LocalDateTime rescheduledTime) {this.rescheduledTime = rescheduledTime;}
    public void setSkipped(boolean skipped) { this.isSkipped = skipped; }

    @Override
    public String toString() {
        return "DosageRecord{" +
                "recordId=" + recordId +
                ", userId=" + userId +
                ", medId=" + medId +
                ", scheduledTime=" + scheduledTime +
                ", actualTakenTime=" + actualTakenTime +
                ", rescheduledTime=" + rescheduledTime +
                ", isSkipped=" + isSkipped +
                '}';
    }
}