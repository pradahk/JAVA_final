package com.smwujava.medicineapp.model;

// UserPattern 모델 클래스는 UserPatterns 테이블의 한 행을 나타냅니다.
public class UserPattern {
    // UserPatterns 테이블의 컬럼에 해당하는 필드들을 정의합니다.
    // user_id는 Users 테이블의 ID를 참조하는 외래 키이자 이 테이블의 기본 키입니다.
    private int userId;       // user_id (INTEGER) - DB 상에서는 PK, FK 역할
    private String breakfast; // breakfast (TEXT) - "HH:MM" 형태의 문자열 (예: "08:00")
    private String lunch;     // lunch (TEXT)     - "HH:MM" 형태의 문자열 (예: "12:30")
    private String dinner;    // dinner (TEXT)    - "HH:MM" 형태의 문자열 (예: "19:00")
    private String sleep;     // sleep (TEXT)     - "HH:MM" 형태의 문자열 (예: "23:00")

    // -------------------- 생성자 --------------------
    // 기본 생성자 (필수: DB에서 읽어온 데이터를 객체에 매핑할 때 사용)
    public UserPattern() {
    }

    // 모든 필드를 받는 생성자 (데이터베이스에서 읽어올 때 편리)
    public UserPattern(int userId, String breakfast, String lunch, String dinner, String sleep) {
        this.userId = userId;
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.dinner = dinner;
        this.sleep = sleep;
    }

    // 사용자 패턴 정보를 새로 설정할 때 (user_id는 별도로 설정) 사용할 수 있는 생성자
    public UserPattern(String breakfast, String lunch, String dinner, String sleep) {
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.dinner = dinner;
        this.sleep = sleep;
        // userId는 이 생성자로 객체를 만든 후 setUserId() 메서드로 설정하거나
        // DAO에서 DB 삽입 시 반환된 ID로 설정합니다.
    }


    // -------------------- Getter 메서드 --------------------
    // 각 필드의 값을 읽어오기 위한 메서드들
    public int getUserId() {
        return userId;
    }

    public String getBreakfast() {
        return breakfast;
    }

    public String getLunch() {
        return lunch;
    }

    public String getDinner() {
        return dinner;
    }

    public String getSleep() {
        return sleep;
    }

    // -------------------- Setter 메서드 --------------------
    // 각 필드의 값을 설정하기 위한 메서드들
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setBreakfast(String breakfast) {
        this.breakfast = breakfast;
    }

    public void setLunch(String lunch) {
        this.lunch = lunch;
    }

    public void setDinner(String dinner) {
        this.dinner = dinner;
    }

    public void setSleep(String sleep) {
        this.sleep = sleep;
    }

    // -------------------- 기타 메서드 --------------------
    // 객체의 내용을 문자열로 표현하여 디버깅 등에 편리하게 사용
    @Override
    public String toString() {
        return "UserPattern{" +
                "userId=" + userId +
                ", breakfast='" + breakfast + '\'' +
                ", lunch='" + lunch + '\'' +
                ", dinner='" + dinner + '\'' +
                ", sleep='" + sleep + '\'' +
                '}';
    }
}