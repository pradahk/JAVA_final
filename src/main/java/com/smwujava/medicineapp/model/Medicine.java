package com.smwujava.medicineapp.model;

// Medicine 모델 클래스는 Medicine 테이블의 한 행을 나타냅니다.
public class Medicine {
    // Medicine 테이블의 컬럼에 해당하는 필드들을 정의합니다.
    // Java 변수 이름은 데이터베이스 컬럼 이름의 스네이크 케이스(snake_case)를
    // 자바의 카멜 케이스(camelCase)로 변환하여 사용하는 것이 일반적입니다.
    private int medId;            // med_id (INTEGER) - DB 상에서는 PK
    private int userId;           // user_id (INTEGER) - DB 상에서는 FK
    private String medName;       // med_name (TEXT)
    private int medDailyAmount;   // med_daily_amount (INTEGER) - 하루 복용 횟수
    private String medDays;       // med_days (TEXT) - 복용 요일 (예: "월,화,수")
    private String medCondition;  // med_condition (TEXT) - 복용 조건 (예: "식사")
    private String medTiming;     // med_timing (TEXT) - 복용 시점 (예: "전", "후")
    private int medMinutes;       // med_minutes (INTEGER) - 조건 시점으로부터 몇 분 (예: 30)
    private String color;         // color (TEXT) - 캘린더 표시 색상 (예: "#FF0000")

    // -------------------- 생성자 --------------------
    // 기본 생성자 (필수: DB에서 읽어온 데이터를 객체에 매핑할 때 사용)
    public Medicine() {
    }

    // 데이터베이스에서 모든 필드 값을 읽어와 객체를 만들 때 사용하는 생성자
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
    // 이 생성자로 객체를 만든 후 DB에 저장하면 DB가 medId를 자동으로 할당합니다.
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


    // -------------------- Getter 메서드 --------------------
    // 각 필드의 값을 읽어오기 위한 메서드들
    public int getMedId() {
        return medId;
    }

    public int getUserId() {
        return userId;
    }

    public String getMedName() {
        return medName;
    }

    public int getMedDailyAmount() {
        return medDailyAmount;
    }

    public String getMedDays() {
        return medDays;
    }

    public String getMedCondition() {
        return medCondition;
    }

    public String getMedTiming() {
        return medTiming;
    }

    public int getMedMinutes() {
        return medMinutes;
    }

    public String getColor() {
        return color;
    }

    // -------------------- Setter 메서드 --------------------
    // 각 필드의 값을 설정하기 위한 메서드들
    public void setMedId(int medId) {
        this.medId = medId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setMedName(String medName) {
        this.medName = medName;
    }

    public void setMedDailyAmount(int medDailyAmount) {
        this.medDailyAmount = medDailyAmount;
    }

    public void setMedDays(String medDays) {
        this.medDays = medDays;
    }

    public void setMedCondition(String medCondition) {
        this.medCondition = medCondition;
    }

    public void setMedTiming(String medTiming) {
        this.medTiming = medTiming;
    }

    public void setMedMinutes(int medMinutes) {
        this.medMinutes = medMinutes;
    }

    public void setColor(String color) {
        this.color = color;
    }


    // -------------------- 기타 메서드 --------------------
    // 객체의 내용을 문자열로 표현하여 디버깅 등에 편리하게 사용
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
