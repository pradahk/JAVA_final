package com.smwujava.medicineapp.model;

public class UserPattern {
    private int userId;
    private String breakfastStartTime;
    private String breakfastEndTime;
    private String lunchStartTime;
    private String lunchEndTime;
    private String dinnerStartTime;
    private String dinnerEndTime;
    private String sleepStartTime;
    private String sleepEndTime;

    public UserPattern() {
    }

    public UserPattern(int userId,
                       String breakfastStartTime, String breakfastEndTime,
                       String lunchStartTime, String lunchEndTime,
                       String dinnerStartTime, String dinnerEndTime,
                       String sleepStartTime, String sleepEndTime) {
        this.userId = userId;
        this.breakfastStartTime = breakfastStartTime;
        this.breakfastEndTime = breakfastEndTime;
        this.lunchStartTime = lunchStartTime;
        this.lunchEndTime = lunchEndTime;
        this.dinnerStartTime = dinnerStartTime;
        this.dinnerEndTime = dinnerEndTime;
        this.sleepStartTime = sleepStartTime;
        this.sleepEndTime = sleepEndTime;
    }

    public UserPattern(String breakfastStartTime, String breakfastEndTime,
                       String lunchStartTime, String lunchEndTime,
                       String dinnerStartTime, String dinnerEndTime,
                       String sleepStartTime, String sleepEndTime) {
        this.breakfastStartTime = breakfastStartTime;
        this.breakfastEndTime = breakfastEndTime;
        this.lunchStartTime = lunchStartTime;
        this.lunchEndTime = lunchEndTime;
        this.dinnerStartTime = dinnerStartTime;
        this.dinnerEndTime = dinnerEndTime;
        this.sleepStartTime = sleepStartTime;
        this.sleepEndTime = sleepEndTime;
    }

    public int getUserId() {return userId;}
    public String getBreakfastStartTime() {return breakfastStartTime;}
    public String getBreakfastEndTime() {return breakfastEndTime;}
    public String getLunchStartTime() {return lunchStartTime;}
    public String getLunchEndTime() {return lunchEndTime;}
    public String getDinnerStartTime() {return dinnerStartTime;}
    public String getDinnerEndTime() {return dinnerEndTime;}
    public String getSleepStartTime() {return sleepStartTime;}
    public String getSleepEndTime() {return sleepEndTime;}

    public void setUserId(int userId) {this.userId = userId;}
    public void setBreakfastStartTime(String breakfastStartTime) {this.breakfastStartTime = breakfastStartTime;}
    public void setBreakfastEndTime(String breakfastEndTime) {this.breakfastEndTime = breakfastEndTime;}
    public void setLunchStartTime(String lunchStartTime) {this.lunchStartTime = lunchStartTime;}
    public void setLunchEndTime(String lunchEndTime) {this.lunchEndTime = lunchEndTime;}
    public void setDinnerStartTime(String dinnerStartTime) {this.dinnerStartTime = dinnerStartTime;}
    public void setDinnerEndTime(String dinnerEndTime) {this.dinnerEndTime = dinnerEndTime;}
    public void setSleepStartTime(String sleepStartTime) {this.sleepStartTime = sleepStartTime;}
    public void setSleepEndTime(String sleepEndTime) {this.sleepEndTime = sleepEndTime;}

    @Override
    public String toString() {
        return "UserPattern{" +
                "userId=" + userId +
                ", breakfast='" + breakfastStartTime + "-" + breakfastEndTime + '\'' +
                ", lunch='" + lunchStartTime + "-" + lunchEndTime + '\'' +
                ", dinner='" + dinnerStartTime + "-" + dinnerEndTime + '\'' +
                ", sleep='" + sleepStartTime + "-" + sleepEndTime + '\'' +
                '}';
    }
}