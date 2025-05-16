package java.com.smwujava.medicineapp.model;

import java.time.LocalDateTime;

public class DosageRecord {
    private int userId;
    private int medId;
    private LocalDateTime scheduledTime;
    private LocalDateTime actualTakenTime;

    public DosageRecord(int userId, int medId, LocalDateTime scheduledTime, LocalDateTime actualTakenTime) {
        this.userId = userId;
        this.medId = medId;
        this.scheduledTime = scheduledTime;
        this.actualTakenTime = actualTakenTime;
    }

    public int getUserId() {
        return userId;
    }

    public int getMedId() {
        return medId;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public LocalDateTime getActualTakenTime() {
        return actualTakenTime;
    }

    public void setActualTakenTime(LocalDateTime actualTakenTime) {
        this.actualTakenTime = actualTakenTime;
    }
}

