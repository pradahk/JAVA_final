package com.smwujava.medicineapp.model;

public class UserSummary {
    private String userId;
    private int medicineCount;
    private double successRate;

    public UserSummary(String userId, int medicineCount, double successRate) {
        this.userId = userId;
        this.medicineCount = medicineCount;
        this.successRate = successRate;
    }

    public String getUserId() { return userId; }
    public int getMedicineCount() { return medicineCount; }
    public double getSuccessRate() { return successRate; }
}