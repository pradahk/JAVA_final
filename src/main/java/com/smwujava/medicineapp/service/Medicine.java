package com.smwujava.medicineapp.model;

public class Medicine {
    private int medId;
    private int userId;
    private String medName;
    private int medDailyAmount;
    private String medDays;
    private String medCondition; // "식사" or "잠자기"
    private String medTiming;    // "전" or "후"
    private int medMinutes;
    private String color;

    public int getMedId() { return medId; }
    public void setMedId(int medId) { this.medId = medId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getMedName() { return medName; }
    public void setMedName(String medName) { this.medName = medName; }

    public int getMedDailyAmount() { return medDailyAmount; }
    public void setMedDailyAmount(int medDailyAmount) { this.medDailyAmount = medDailyAmount; }

    public String getMedDays() { return medDays; }
    public void setMedDays(String medDays) { this.medDays = medDays; }

    public String getMedCondition() { return medCondition; }
    public void setMedCondition(String medCondition) { this.medCondition = medCondition; }

    public String getMedTiming() { return medTiming; }
    public void setMedTiming(String medTiming) { this.medTiming = medTiming; }

    public int getMedMinutes() { return medMinutes; }
    public void setMedMinutes(int medMinutes) { this.medMinutes = medMinutes; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
