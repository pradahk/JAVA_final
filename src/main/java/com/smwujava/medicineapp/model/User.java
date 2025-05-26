package com.smwujava.medicineapp.model;

// User 모델 클래스는 Users 테이블의 한 행을 나타냅니다.
public class User {
    // Users 테이블의 컬럼에 해당하는 필드들을 정의합니다.
    // 데이터 타입은 DB 컬럼 타입(INTEGER, TEXT)에 맞춰 Java 타입(int, String)으로 선언합니다.
    private int userId;       // user_id (INTEGER)
    private String username;  // username (TEXT)
    private String password;  // password (TEXT)

    // -------------------- 생성자 --------------------
    // 기본 생성자 (필수: 일부 라이브러리에서 객체를 생성할 때 사용)
    public User() {
    }

    // 모든 필드를 받는 생성자 (객체 생성 시 필드 값을 한 번에 설정하기 편리)
    // userId는 DB에서 자동 생성되므로, 사용자 등록 시에는 username과 password만 받는 생성자가 더 유용할 수 있습니다.
    public User(int userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }

    // 사용자 등록 시 (DB에서 ID 자동 생성) 사용할 수 있는 생성자
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        // userId는 DB에서 할당될 예정이므로 설정하지 않거나 0 등으로 초기화
    }


    // -------------------- Getter 메서드 --------------------
    // 각 필드의 값을 읽어오기 위한 메서드들
    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    // -------------------- Setter 메서드 --------------------
    // 각 필드의 값을 설정하기 위한 메서드들
    // user_id는 보통 DB에서 자동 생성되므로 Setter가 필요 없을 수도 있지만, DB에서 읽어온 값을 설정하기 위해 필요할 수 있습니다.
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // -------------------- 기타 메서드 --------------------
    // 객체의 내용을 문자열로 표현하여 디버깅 등에 편리하게 사용
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                // 보안상 password는 출력하지 않거나, 보호된 형식으로 출력합니다.
                ", password='[PROTECTED]'" +
                '}';
    }
}