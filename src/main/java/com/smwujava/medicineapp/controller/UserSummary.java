package com.smwujava.medicineapp.controller;

public class UserSummary {
    private String username;
    private int medicineCount;
    private double successRate;

    public UserSummary(String username, int medicineCount, double successRate) {
        this.username = username;
        this.medicineCount = medicineCount;
        this.successRate = successRate;
    }

    public String getUsername() { return username; }
    public int getMedicineCount() { return medicineCount; }
    public double getSuccessRate() { return successRate; }
}
