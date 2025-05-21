package com.smwujava.medicineapp.model;

public class User {
    // 데이터 타입은 DB 컬럼 타입(INTEGER, TEXT)에 맞춰 Java 타입(int, String)으로 선언합니다.
    private int userId;       // user_id (INTEGER)
    private String username;  // username (TEXT)
    private String password;  // password (TEXT)

    private boolean autoLogin; // autoLogin 자동 로그인 여부 저장 <<-- 추가된 필드

    // 기본 생성자 (필수: 일부 라이브러리에서 객체를 생성할 때 사용)
    public User(String username, String password) {
    }

    // 모든 필드를 받는 생성자 (객체 생성 시 필드 값을 한 번에 설정하기 편리)
    public User(int userId, String username, String password, boolean autoLogin) { // <<-- autoLogin 매개변수 추가
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.autoLogin = autoLogin; // <<-- 필드 초기화
    }

    // 사용자 등록 시 (DB에서 ID 자동 생성) 사용할 수 있는 생성자
    public User(String username, String password, boolean autoLogin) { // <<-- autoLogin 매개변수 추가
        this.username = username;
        this.password = password;
        this.autoLogin = autoLogin; // <<-- 필드 초기화
        // userId는 DB에서 할당될 예정이므로 설정하지 않거나 0 등으로 초기화
    }

    //Getter 메서드
    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAutoLogin() { // <<-- 추가된 Getter
        return autoLogin;
    }

    //Setter 메서드
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAutoLogin(boolean autoLogin) { // <<-- 추가된 Setter
        this.autoLogin = autoLogin;
    }

    //기타 메서드
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                // 보안상 password는 출력하지 않거나, 보호된 형식으로 출력합니다.
                ", password='[PROTECTED]'" +
                ", autoLogin=" + autoLogin + // <<-- toString에 추가
                '}';
    }
}