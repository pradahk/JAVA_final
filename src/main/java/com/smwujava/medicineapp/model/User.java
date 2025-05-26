package com.smwujava.medicineapp.model;

public class User {
    private int userId;       // user_id (INTEGER)
    private String username;  // username (TEXT)
    private String password;  // password (TEXT)

    private boolean autoLogin; // autoLogin 자동 로그인 여부 저장

    public User(String username, String password) {
    }

    // 모든 필드를 받는 생성자 (객체 생성 시 필드 값을 한 번에 설정하기 편리)
    public User(int userId, String username, String password, boolean autoLogin) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.autoLogin = autoLogin;
    }

    // 사용자 등록 시 (DB에서 ID 자동 생성) 사용할 수 있는 생성자
    public User(String username, String password, boolean autoLogin) {
        this.username = username;
        this.password = password;
        this.autoLogin = autoLogin;
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
                ", password='[PROTECTED]'" +
                ", autoLogin=" + autoLogin +
                '}';
    }
}