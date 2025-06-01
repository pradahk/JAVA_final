package com.smwujava.medicineapp.model;

public class Medicine {
    private int medId;
    private int userId;
    private String medName;
    private int medDailyAmount;
    private String medDays;
    private String medCondition;
    private String medTiming;
    private int medMinutes;
    private String color;

    public Medicine() {
    }

    public Medicine(int medId, int userId, String medName, int medDailyAmount, String medDays, String medCondition, String medTiming, int medMinutes, String color) {
        this.medId = medId;
        this.userId = userId;
        this.medName = medName;
        this.medDailyAmount = medDailyAmount;
        this.medDays = medDays;
        this.medCondition = medCondition;
        this.medTiming = medTiming;
        this.medMinutes = medMinutes;
        this.color = color;
    }

    public Medicine(int userId, String medName, int medDailyAmount, String medDays, String medCondition, String medTiming, int medMinutes, String color) {
        this.userId = userId;
        this.medName = medName;
        this.medDailyAmount = medDailyAmount;
        this.medDays = medDays;
        this.medCondition = medCondition;
        this.medTiming = medTiming;
        this.medMinutes = medMinutes;
        this.color = color;
    }

    public int getMedId() {return medId;}
    public int getUserId() {return userId;}
    public String getMedName() {return medName;}
    public int getMedDailyAmount() {return medDailyAmount;}
    public String getMedDays() {return medDays;}
    public String getMedCondition() {return medCondition;}
    public String getMedTiming() {return medTiming;}
    public int getMedMinutes() {return medMinutes;}
    public String getColor() {return color;}

    public void setMedId(int medId) {this.medId = medId;}
    public void setUserId(int userId) {this.userId = userId;}
    public void setMedName(String medName) {this.medName = medName;}
    public void setMedDailyAmount(int medDailyAmount) {this.medDailyAmount = medDailyAmount;}
    public void setMedDays(String medDays) {this.medDays = medDays;}
    public void setMedCondition(String medCondition) {this.medCondition = medCondition;}
    public void setMedTiming(String medTiming) {this.medTiming = medTiming;}
    public void setMedMinutes(int medMinutes) {this.medMinutes = medMinutes;}
    public void setColor(String color) {this.color = color;}

    @Override
    public String toString() {
        return "Medicine{" +
                "medId=" + medId +
                ", userId=" + userId +
                ", medName='" + medName + '\'' +
                ", medDailyAmount=" + medDailyAmount +
                ", medDays='" + medDays + '\'' +
                ", medCondition='" + medCondition + '\'' +
                ", medTiming='" + medTiming + '\'' +
                ", medMinutes=" + medMinutes +
                ", color='" + color + '\'' +
                '}';
    }
}