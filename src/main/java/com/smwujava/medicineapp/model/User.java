package com.smwujava.medicineapp.model;

public class User {
    private int userId;
    private String username;
    private String password;
    private boolean autoLogin;
    private boolean isAdmin;

    public User(int userId, String username, String password, boolean autoLogin, boolean isAdmin) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.autoLogin = autoLogin;
        this.isAdmin = isAdmin;
    }

    public User(String username, String password, boolean autoLogin, boolean isAdmin) {
        this.username = username;
        this.password = password;
        this.autoLogin = autoLogin;
        this.isAdmin = isAdmin;
    }

    public int getUserId() {return userId;}
    public String getUsername() {return username;}
    public String getPassword() {return password;}
    public boolean isAutoLogin() { //기존 Getter
        return autoLogin;
    }
    public boolean isAdmin() {//변경점 4: isAdmin Getter 추가
        return isAdmin;
    }

    public void setUserId(int userId) {this.userId = userId;}
    public void setAdmin(boolean admin) { //변경점 5: isAdmin Setter 추가
        isAdmin = admin;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", autoLogin=" + autoLogin +
                ", isAdmin=" + isAdmin +
                '}';
    }
}