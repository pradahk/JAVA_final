package com.smwujava.medicineapp.model;

public class UserPattern {
    private int userId;
    private String breakfastStartTime; // 아침 식사 시작 시간 (HH:MM)
    private String breakfastEndTime;   // 아침 식사 종료 시간 (HH:MM)
    private String lunchStartTime;     // 점심 식사 시작 시간 (HH:MM)
    private String lunchEndTime;       // 점심 식사 종료 시간 (HH:MM)
    private String dinnerStartTime;    // 저녁 식사 시작 시간 (HH:MM)
    private String dinnerEndTime;      // 저녁 식사 종료 시간 (HH:MM)
    private String sleepStartTime;     // 수면 시작 시간 (HH:MM)
    private String sleepEndTime;       // 수면 종료 시간 (HH:MM)

    // 기본 생성자
    public UserPattern() {
    }

    // 모든 필드를 받는 생성자 (데이터베이스에서 읽어올 때 편리)
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

    // 사용자 패턴 정보를 새로 설정할 때 (user_id는 별도로 설정) 사용할 수 있는 생성자
    // 이 생성자는 UI에서 시작 및 종료 시간을 입력받을 때 사용될 수 있습니다.
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
        // userId는 이 생성자로 객체를 만든 후 setUserId() 메서드로 설정하거나 DAO에서 DB 삽입 시 반환된 ID로 설정합니다.
    }

    // Getter 메서드
    public int getUserId() {
        return userId;
    }

    public String getBreakfastStartTime() {
        return breakfastStartTime;
    }

    public String getBreakfastEndTime() {
        return breakfastEndTime;
    }

    public String getLunchStartTime() {
        return lunchStartTime;
    }

    public String getLunchEndTime() {
        return lunchEndTime;
    }

    public String getDinnerStartTime() {
        return dinnerStartTime;
    }

    public String getDinnerEndTime() {
        return dinnerEndTime;
    }

    public String getSleepStartTime() {
        return sleepStartTime;
    }

    public String getSleepEndTime() {
        return sleepEndTime;
    }

    // Setter 메서드
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setBreakfastStartTime(String breakfastStartTime) {
        this.breakfastStartTime = breakfastStartTime;
    }

    public void setBreakfastEndTime(String breakfastEndTime) {
        this.breakfastEndTime = breakfastEndTime;
    }

    public void setLunchStartTime(String lunchStartTime) {
        this.lunchStartTime = lunchStartTime;
    }

    public void setLunchEndTime(String lunchEndTime) {
        this.lunchEndTime = lunchEndTime;
    }

    public void setDinnerStartTime(String dinnerStartTime) {
        this.dinnerStartTime = dinnerStartTime;
    }

    public void setDinnerEndTime(String dinnerEndTime) {
        this.dinnerEndTime = dinnerEndTime;
    }

    public void setSleepStartTime(String sleepStartTime) {
        this.sleepStartTime = sleepStartTime;
    }

    public void setSleepEndTime(String sleepEndTime) {
        this.sleepEndTime = sleepEndTime;
    }

    // 기타 메서드 (toString 오버라이드)
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