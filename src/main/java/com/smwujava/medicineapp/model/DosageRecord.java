package com.smwujava.medicineapp.model;

import java.time.LocalDateTime; // LocalDateTime 클래스 임포트
// import java.time.LocalDate; // 만약 recordDate 필드를 LocalDate로 유지하고 싶다면 이 줄을 임포트

/**
 * DosageRecord 모델 클래스는 DosageRecords 테이블의 한 행을 나타냅니다.
 * 사용자의 특정 약에 대한 특정 날짜의 복용 기록입니다.
 */
public class DosageRecord {
    // DosageRecords 테이블의 컬럼에 해당하는 필드들을 정의합니다.
    private int recordId;         // DB 상에서는 PK
    private int userId;           // DB 상에서는 FK
    private int medId;            // DB 상에서는 FK

    // --- 변경점 1, 2: 날짜/시간 필드를 String에서 LocalDateTime으로 변경 ---
    // DB의 DATETIME 값을 Java의 LocalDateTime으로 매핑합니다.
    private LocalDateTime scheduledTime;    // 예정된 복용 시간
    private LocalDateTime actualTakenTime;  // 실제 복용 시간 (DB에서 NULL 허용)
    private LocalDateTime rescheduledTime;  // <<< 변경점 3: 새로 추가된 필드: 재조정된 복용 시간 (DATETIME, DB에서 NULL 허용)

    // -------------------- 생성자 --------------------
    // 기본 생성자 (필수: DB에서 읽어온 데이터를 객체에 매핑할 때 사용)
    public DosageRecord() {
    }

    /**
     * 데이터베이스에서 모든 필드 값을 읽어와 객체를 만들 때 사용하는 생성자입니다.
     * recordId, scheduledTime, actualTakenTime, rescheduledTime (null일 수 있음)을 포함합니다.
     *
     * @param recordId 복용 기록 고유 ID
     * @param userId 사용자 ID
     * @param medId 약 ID
     * @param scheduledTime 예정된 복용 시간
     * @param actualTakenTime 실제 복용 시간 (null 가능)
     * @param rescheduledTime 재조정된 복용 시간 (null 가능)
     */
    // --- 변경점 4: 모든 필드 값을 받는 생성자 업데이트 (LocalDateTime 타입 반영) ---
    public DosageRecord(int recordId, int userId, int medId, LocalDateTime scheduledTime, LocalDateTime actualTakenTime, LocalDateTime rescheduledTime) {
        this.recordId = recordId;
        this.userId = userId;
        this.medId = medId;
        this.scheduledTime = scheduledTime;
        this.actualTakenTime = actualTakenTime;
        this.rescheduledTime = rescheduledTime; // 새로 추가된 필드 초기화
    }

    /**
     * 새로운 복용 기록을 '예정' 상태로 생성할 때 사용하는 생성자입니다.
     * recordId는 DB에서 자동 생성되고, 실제 복용 시간과 재조정 시간은 아직 없습니다.
     * DB에 INSERT 할 때 주로 사용됩니다.
     *
     * @param userId 사용자 ID
     * @param medId 약 ID
     * @param scheduledTime 예정된 복용 시간 (LocalDateTime)
     */
    // --- 변경점 4: 새로운 복용 기록 생성 생성자 업데이트 (LocalDateTime 타입 반영) ---
    public DosageRecord(int userId, int medId, LocalDateTime scheduledTime) {
        this.userId = userId;
        this.medId = medId;
        this.scheduledTime = scheduledTime;
        this.actualTakenTime = null; // 실제 복용 시간은 아직 없으므로 null로 설정
        this.rescheduledTime = null; // 재조정된 시간도 처음에는 null로 설정
        // recordId는 DB에서 자동 할당될 예정이므로 이 생성자에서는 설정하지 않습니다.
    }

    // -------------------- Getter 메서드 --------------------
    public int getRecordId() {
        return recordId;
    }

    public int getUserId() {
        return userId;
    }

    public int getMedId() {
        return medId;
    }

    // --- 변경점 5: Getter 반환 타입을 LocalDateTime으로 변경 ---
    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    // --- 변경점 6: Getter 반환 타입을 LocalDateTime으로 변경 ---
    public LocalDateTime getActualTakenTime() {
        return actualTakenTime;
    }

    // --- 변경점 7: 새로 추가된 필드의 Getter ---
    public LocalDateTime getRescheduledTime() {
        return rescheduledTime;
    }

    // -------------------- Setter 메서드 --------------------
    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setMedId(int medId) {
        this.medId = medId;
    }

    // --- 변경점 8: Setter 매개변수 타입을 LocalDateTime으로 변경 ---
    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    // --- 변경점 9: Setter 매개변수 타입을 LocalDateTime으로 변경 ---
    public void setActualTakenTime(LocalDateTime actualTakenTime) {
        this.actualTakenTime = actualTakenTime;
    }

    // --- 변경점 10: 새로 추가된 필드의 Setter ---
    public void setRescheduledTime(LocalDateTime rescheduledTime) {
        this.rescheduledTime = rescheduledTime;
    }


    // -------------------- 기타 메서드 --------------------
    @Override
    public String toString() {
        return "DosageRecord{" +
                "recordId=" + recordId +
                ", userId=" + userId +
                ", medId=" + medId +
                ", scheduledTime=" + scheduledTime +
                ", actualTakenTime=" + actualTakenTime + // null일 수 있음을 고려
                ", rescheduledTime=" + rescheduledTime + // 새로 추가된 필드 포함
                '}';
    }

    // --- 변경점 11: 복용 완료 여부 확인 메서드 (LocalDateTime에 맞게 수정) ---
    public boolean isTaken() {
        return this.actualTakenTime != null; // actualTakenTime이 null이 아니면 복용 완료로 간주
    }
}