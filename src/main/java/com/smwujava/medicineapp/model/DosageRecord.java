package com.smwujava.medicineapp.model;

// DosageRecord 모델 클래스는 DosageRecords 테이블의 한 행을 나타냅니다.
// 사용자의 특정 약에 대한 특정 날짜의 복용 기록입니다.
public class DosageRecord {
    // DosageRecords 테이블의 컬럼에 해당하는 필드들을 정의합니다.
    // actual_taken_time은 DB에서 NULL이 될 수 있으므로, Java 필드도 String 타입으로 선언하고 null 값을 가질 수 있습니다.
    private int recordId;         // record_id (INTEGER) - DB 상에서는 PK
    private int userId;           // user_id (INTEGER) - DB 상에서는 FK
    private int medId;            // med_id (INTEGER) - DB 상에서는 FK
    private String recordDate;    // record_date (TEXT) - "YYYY-MM-DD" 형태
    private String scheduledTime; // scheduled_time (TEXT) - "YYYY-MM-DD HH:MM" 형태 (계산된 복용 예정 시간)
    private String actualTakenTime; // actual_taken_time (TEXT) - "YYYY-MM-DD HH:MM" 형태 (실제 복용 시간) - DB에서 NULL 허용

    // -------------------- 생성자 --------------------
    // 기본 생성자 (필수: DB에서 읽어온 데이터를 객체에 매핑할 때 사용)
    public DosageRecord() {
    }

    // 데이터베이스에서 모든 필드 값을 읽어와 객체를 만들 때 사용하는 생성자
    // recordId와 actualTakenTime (null일 수 있음) 포함
    public DosageRecord(int recordId, int userId, int medId, String recordDate, String scheduledTime, String actualTakenTime) {
        this.recordId = recordId;
        this.userId = userId;
        this.medId = medId;
        this.recordDate = recordDate;
        this.scheduledTime = scheduledTime;
        this.actualTakenTime = actualTakenTime; // DB에서 읽어온 실제 복용 시간 값 (null 또는 문자열)
    }

    // 새로운 복용 기록을 '예정' 상태로 생성할 때 (recordId는 DB에서 자동 생성, 실제 복용 시간은 아직 없음)
    // DB에 INSERT 할 때 주로 사용됩니다.
    public DosageRecord(int userId, int medId, String recordDate, String scheduledTime) {
        this.userId = userId;
        this.medId = medId;
        this.recordDate = recordDate;
        this.scheduledTime = scheduledTime;
        this.actualTakenTime = null; // 실제 복용 시간은 아직 없으므로 null로 설정
        // recordId는 DB에서 자동 할당될 예정이므로 이 생성자에서는 설정하지 않습니다.
    }

    // -------------------- Getter 메서드 --------------------
    // 각 필드의 값을 읽어오기 위한 메서드들
    public int getRecordId() {
        return recordId;
    }

    public int getUserId() {
        return userId;
    }

    public int getMedId() {
        return medId;
    }

    public String getRecordDate() {
        return recordDate;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public String getActualTakenTime() {
        return actualTakenTime;
    }

    // -------------------- Setter 메서드 --------------------
    // 각 필드의 값을 설정하기 위한 메서드들
    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setMedId(int medId) {
        this.medId = medId;
    }

    public void setRecordDate(String recordDate) {
        this.recordDate = recordDate;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    // 실제 복용 시간을 설정합니다 (복용 완료 처리 시 사용)
    public void setActualTakenTime(String actualTakenTime) {
        this.actualTakenTime = actualTakenTime;
    }


    // -------------------- 기타 메서드 --------------------
    // 객체의 내용을 문자열로 표현하여 디버깅 등에 편리하게 사용
    @Override
    public String toString() {
        return "DosageRecord{" +
                "recordId=" + recordId +
                ", userId=" + userId +
                ", medId=" + medId +
                ", recordDate='" + recordDate + '\'' +
                ", scheduledTime='" + scheduledTime + '\'' +
                ", actualTakenTime='" + actualTakenTime + '\'' + // null일 수 있음을 고려
                '}';
    }

    // 편의 메서드: 복용 완료 여부 확인
    public boolean isTaken() {
        return this.actualTakenTime != null && !this.actualTakenTime.trim().isEmpty();
    }
}