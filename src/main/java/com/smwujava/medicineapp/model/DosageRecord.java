package com.smwujava.medicineapp.model;

import java.time.LocalDateTime;

public class DosageRecord {
    private int recordId;         // 복용 기록 고유 ID, DB 상에서는 PK
    private int userId;           // 사용자 ID, DB 상에서는 FK
    private int medId;            // 약 ID, DB 상에서는 FK
    // 변경점 1, 2: 날짜/시간 필드를 String에서 LocalDateTime으로 변경 (이전 작업에서 이미 수행됨)
    private LocalDateTime scheduledTime;    // 예정된 복용 시간
    private LocalDateTime actualTakenTime;  // 실제 복용 시간 (DB에서 NULL 허용)
    private LocalDateTime rescheduledTime;  // <<-- 변경점 3: 재조정된 복용 시간 (DATETIME, DB에서 NULL 허용) (이전 작업에서 이미 수행됨)

    private boolean isSkipped; // <<-- 변경점 4: 복용 건너뛰기 여부 필드 추가 (true: 건너뜀, false: 건너뛰지 않음)

    // 생성자
    public DosageRecord() {
    }

    // 변경점 5: 모든 필드 값을 받는 생성자 업데이트 (LocalDateTime 타입 반영 및 isSkipped 필드 추가)
    public DosageRecord(int recordId, int userId, int medId, LocalDateTime scheduledTime, LocalDateTime actualTakenTime, LocalDateTime rescheduledTime, boolean isSkipped) {
        this.recordId = recordId;
        this.userId = userId;
        this.medId = medId;
        this.scheduledTime = scheduledTime;
        this.actualTakenTime = actualTakenTime;
        this.rescheduledTime = rescheduledTime;
        this.isSkipped = isSkipped; // <<-- isSkipped 초기화
    }

    /**
     * 새로운 복용 기록을 '예정' 상태로 생성할 때 사용하는 생성자입니다.
     * recordId는 DB에서 자동 생성되고, 실제 복용 시간과 재조정 시간은 아직 없습니다.
     * isSkipped는 기본적으로 false로 설정됩니다.
     */
    public DosageRecord(int userId, int medId, LocalDateTime scheduledTime) {
        this.userId = userId;
        this.medId = medId;
        this.scheduledTime = scheduledTime;
        this.actualTakenTime = null; // 초기에는 실제 복용 시간 없음
        this.rescheduledTime = null; // 초기에는 재조정 시간 없음
        this.isSkipped = false;      // <<-- isSkipped 기본값 설정
    }

    //Getter 메서드
    public int getRecordId() {return recordId;}
    public int getUserId() {return userId;}
    public int getMedId() {return medId;}
    public LocalDateTime getScheduledTime() {return scheduledTime;}
    public LocalDateTime getActualTakenTime() {return actualTakenTime;}
    public LocalDateTime getRescheduledTime() { //변경점 6: 새로 추가된 필드의 Getter (이전 작업에서 이미 수행됨)
        return rescheduledTime;
    }
    public boolean isSkipped() { //변경점 7: isSkipped Getter 추가
        return isSkipped;
    }

    //Setter 메서드
    public void setRecordId(int recordId) {this.recordId = recordId;}
    public void setUserId(int userId) {this.userId = userId;}
    public void setMedId(int medId) {this.medId = medId;}
    public void setScheduledTime(LocalDateTime scheduledTime) { //변경점 8: Setter 매개변수 타입을 LocalDateTime으로 변경 (이전 작업에서 이미 수행됨)
        this.scheduledTime = scheduledTime;
    }
    public void setActualTakenTime(LocalDateTime actualTakenTime) { //변경점 9: Setter 매개변수 타입을 LocalDateTime으로 변경 (이전 작업에서 이미 수행됨)
        this.actualTakenTime = actualTakenTime;
    }
    public void setRescheduledTime(LocalDateTime rescheduledTime) { //변경점 10: 새로 추가된 필드의 Setter (이전 작업에서 이미 수행됨)
        this.rescheduledTime = rescheduledTime;
    }
    public void setSkipped(boolean skipped) { this.isSkipped = skipped; } //변경점 11: isSkipped Setter 추가

    //기타 메서드
    @Override
    public String toString() {
        return "DosageRecord{" +
                "recordId=" + recordId +
                ", userId=" + userId +
                ", medId=" + medId +
                ", scheduledTime=" + scheduledTime +
                ", actualTakenTime=" + actualTakenTime + // null일 수 있음을 고려
                ", rescheduledTime=" + rescheduledTime +
                ", isSkipped=" + isSkipped + // <<-- toString에도 추가
                '}';
    }
}