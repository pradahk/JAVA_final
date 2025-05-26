package com.smwujava.medicineapp.model;

public class Medicine {
    private int medId;            // med_id (INTEGER) - DB 상에서는 PK
    private int userId;           // user_id (INTEGER) - DB 상에서는 FK
    private String medName;       // med_name (TEXT)
    private int medDailyAmount;   // med_daily_amount (INTEGER) - 하루 복용 횟수
    private String medDays;       // med_days (TEXT) - 복용 요일 (예: "월,화,수")
    private String medCondition;  // med_condition (TEXT) - 복용 조건 (예: "식사")
    private String medTiming;     // med_timing (TEXT) - 복용 시점 (예: "전", "후")
    private int medMinutes;       // med_minutes (INTEGER) - 조건 시점으로부터 몇 분 (예: 30)
    private String color;         // color (TEXT) - 캘린더 표시 색상 (예: "#FF0000")

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

    // 새로운 약 정보를 DB에 삽입할 때 (medId는 DB에서 자동 생성) 사용할 수 있는 생성자
    public Medicine(int userId, String medName, int medDailyAmount, String medDays, String medCondition, String medTiming, int medMinutes, String color) {
        this.userId = userId;
        this.medName = medName;
        this.medDailyAmount = medDailyAmount;
        this.medDays = medDays;
        this.medCondition = medCondition;
        this.medTiming = medTiming;
        this.medMinutes = medMinutes;
        this.color = color;
        // medId는 DB에서 자동 할당되므로 여기서는 설정하지 않습니다.
    }


    //Getter 메서드
    public int getMedId() {return medId;}
    public int getUserId() {return userId;}
    public String getMedName() {return medName;}
    public int getMedDailyAmount() {return medDailyAmount;}
    public String getMedDays() {return medDays;}
    public String getMedCondition() {return medCondition;}
    public String getMedTiming() {return medTiming;}
    public int getMedMinutes() {return medMinutes;}
    public String getColor() {return color;}

    // Setter 메서드 -
    public void setMedId(int medId) {this.medId = medId;}
    public void setUserId(int userId) {this.userId = userId;}
    public void setMedName(String medName) {this.medName = medName;}
    public void setMedDailyAmount(int medDailyAmount) {this.medDailyAmount = medDailyAmount;}
    public void setMedDays(String medDays) {this.medDays = medDays;}
    public void setMedCondition(String medCondition) {this.medCondition = medCondition;}
    public void setMedTiming(String medTiming) {this.medTiming = medTiming;}
    public void setMedMinutes(int medMinutes) {this.medMinutes = medMinutes;}
    public void setColor(String color) {this.color = color;}

    // 기타 메서드
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