package com.smwujava.medicineapp.model;

public class User {
    private int userId;       // user_id (INTEGER)
    private String username;  // username (TEXT)
    private String password;  // password (TEXT)

    private boolean autoLogin; // autoLogin 자동 로그인 여부 저장
    private boolean isAdmin;   // <<-- 변경점 1: 관리자 여부 필드 추가 (true: 관리자, false: 일반 사용자)

    // 변경점 2: 모든 필드를 받는 생성자 업데이트 (isAdmin 필드 추가)
    public User(int userId, String username, String password, boolean autoLogin, boolean isAdmin) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.autoLogin = autoLogin;
        this.isAdmin = isAdmin; // <<-- isAdmin 초기화
    }

    // 변경점 3: 사용자 등록 시 (DB에서 ID 자동 생성) 사용할 수 있는 생성자 업데이트 (isAdmin 필드 추가)
    public User(String username, String password, boolean autoLogin, boolean isAdmin) {
        this.username = username;
        this.password = password;
        this.autoLogin = autoLogin;
        this.isAdmin = isAdmin; // <<-- isAdmin 초기화
        // userId는 DB에서 할당될 예정이므로 설정하지 않거나 0 등으로 초기화
    }

    //Getter 메서드
    public int getUserId() {return userId;}
    public String getUsername() {return username;}
    public String getPassword() {return password;}
    public boolean isAutoLogin() { //기존 Getter
        return autoLogin;
    }
    public boolean isAdmin() {//변경점 4: isAdmin Getter 추가
        return isAdmin;
    }

    //Setter 메서드
    public void setUserId(int userId) {this.userId = userId;}
    public void setUsername(String username) {this.username = username;}
    public void setPassword(String password) {this.password = password;}
    public void setAutoLogin(boolean autoLogin) { //기존 Setter
        this.autoLogin = autoLogin;
    }
    public void setAdmin(boolean admin) { //변경점 5: isAdmin Setter 추가
        isAdmin = admin;
    }

    //기타 메서드
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", autoLogin=" + autoLogin +
                ", isAdmin=" + isAdmin + // <<-- toString에도 추가
                '}';
    }
}