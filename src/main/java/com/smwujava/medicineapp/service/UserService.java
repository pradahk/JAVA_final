package com.smwujava.medicineapp.service;

import com.smwujava.medicineapp.dao.UserDao;
import com.smwujava.medicineapp.model.User;
import com.smwujava.medicineapp.util.AutoLoginUtil;
import java.sql.SQLException;

public class UserService {
    private UserService() {
    }

    /**
     * 새로운 사용자를 등록합니다 (회원가입).
     * @param username 사용자 이름
     * @param password 비밀번호
     * @return 회원가입 성공 시 true, 실패 시 false
     */
    public static boolean register(String username, String password) {
        try {
            // 사용자 이름 중복 확인
            if (UserDao.findUserByUsername(username) != null) { // 수정된 부분
                System.out.println("이미 존재하는 사용자 이름입니다.");
                return false;
            }
            // 비밀번호 유효성 검사 (길이, 특수문자 등)
            if (password.length() < 7) {
                System.out.println("비밀번호는 최소 7자 이상이어야 합니다.");
                return false;
            }
            if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
                System.out.println("비밀번호에는 특수문자가 포함되어야 합니다.");
                return false;
            }

            // 새로운 User 객체 생성 시 autoLogin은 기본값으로 false, isAdmin은 false 설정 (수정된 부분)
            User newUser = new User(username, password, false, false);
            // 사용자 정보를 DB에 삽입하고, 생성된 user_id를 받습니다.
            int generatedId = UserDao.insertUser(newUser);
            // generatedId가 -1이 아니면 성공으로 간주
            return generatedId != -1;

        } catch (SQLException e) {
            System.err.println("회원가입 중 데이터베이스 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 사용자 로그인 처리를 수행합니다.
     * @param username 사용자 이름
     * @param password 비밀번호 (DB에 해시화되어 있다면 해시화된 비밀번호를 전달해야 함)
     * @param rememberMe 자동 로그인 여부
     * @return 로그인 성공 시 true, 실패 시 false
     */
    public static boolean login(String username, String password, boolean rememberMe) {
        try {
            // 사용자 이름으로 사용자 찾기 (수정된 부분)
            User user = UserDao.findUserByUsername(username);

            if (user != null && user.getPassword().equals(password)) { // 비밀번호 비교 추가
                // 로그인 성공
                if (rememberMe) {
                    // 자동 로그인 정보 저장 (파일 시스템 등)
                    AutoLoginUtil.saveAutoLoginUser(user.getUserId(), user.getUsername(), user.getPassword());
                    // DB에도 해당 사용자의 자동 로그인 상태를 true로 업데이트
                    UserDao.updateAutoLoginStatus(user.getUserId(), true);
                } else {
                    // 자동 로그인 요청하지 않았으면 기존 자동 로그인 정보 삭제
                    AutoLoginUtil.clearAutoLoginUser();
                    // 혹시 DB에 auto_login이 true로 되어있다면 false로 업데이트 (일관성 유지)
                    UserDao.updateAutoLoginStatus(user.getUserId(), false);
                }
                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            System.err.println("로그인 중 데이터베이스 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 현재 자동 로그인 상태를 확인하고, 유효한 자동 로그인 사용자가 있으면 User 객체를 반환합니다.
     * @return 자동 로그인된 사용자 User 객체, 없으면 null
     */
    public static User checkAutoLogin() {
        try {
            User autoLoginUser = UserDao.findAutoLoginUser();
            if (autoLoginUser != null) {
                return autoLoginUser;
            }
        } catch (SQLException e) {
            System.err.println("자동 로그인 확인 중 데이터베이스 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 사용자의 비밀번호를 수정합니다.
     * @param username 비밀번호를 수정할 사용자 이름
     * @param currentPassword 현재 비밀번호 (확인용)
     * @param newPassword 새로 설정할 비밀번호
     * @return 비밀번호 수정 성공 시 true, 실패 시 false
     */
    public static boolean changePassword(String username, String currentPassword, String newPassword) {
        try {
            // 현재 비밀번호 일치 확인 (login 메서드의 로직을 재사용)
            // rememberMe는 false로 넘겨서 자동 로그인 상태를 변경하지 않도록 함.
            if (!login(username, currentPassword, false)) {
                System.out.println("현재 비밀번호가 일치하지 않습니다.");
                return false;
            }

            // 새 비밀번호 유효성 검사
            if (newPassword.length() < 7) {
                System.out.println("새 비밀번호는 최소 7자 이상이어야 합니다.");
                return false;
            }
            if (!newPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
                System.out.println("새 비밀번호에는 특수문자가 포함되어야 합니다.");
                return false;
            }

            // <<-- 비밀번호 업데이트 로직 변경 시작 -->>
            // 1. 현재 사용자 정보 조회
            User userToUpdate = UserDao.findUserByUsername(username);
            if (userToUpdate == null) {
                System.out.println("사용자를 찾을 수 없습니다.");
                return false;
            }

            // 2. User 객체의 비밀번호만 업데이트
            userToUpdate.setPassword(newPassword);

            // 3. updateUser 메서드를 사용하여 DB에 반영
            // TODO: 실제 앱에서는 newPassword를 해시화하여 DAO에 전달해야 합니다.
            return UserDao.updateUser(userToUpdate);
            // <<-- 비밀번호 업데이트 로직 변경 끝 -->>

        } catch (SQLException e) {
            System.err.println("비밀번호 수정 중 데이터베이스 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 사용자의 사용자 이름(username)을 수정합니다.
     * @param currentUsername 현재 사용자 이름
     * @param newUsername 새로 설정할 사용자 이름
     * @return 사용자 이름 수정 성공 시 true, 실패 시 false
     */
    public static boolean changeUsername(String currentUsername, String newUsername) {
        try {
            // 새 사용자 이름 중복 확인
            if (UserDao.findUserByUsername(newUsername) != null) { // 수정된 부분
                System.out.println("이미 존재하는 아이디입니다.");
                return false;
            }

            // 사용자 이름 업데이트를 위해 현재 사용자 정보 조회
            User userToUpdate = UserDao.findUserByUsername(currentUsername);
            if (userToUpdate == null) {
                System.out.println("현재 사용자를 찾을 수 없습니다.");
                return false;
            }

            // User 객체의 username만 업데이트
            userToUpdate.setUsername(newUsername);

            // updateUser 메서드를 사용하여 DB에 반영
            return UserDao.updateUser(userToUpdate);

        } catch (SQLException e) {
            System.err.println("사용자 이름 수정 중 데이터베이스 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 로그아웃 기능을 수행합니다. 자동 로그인 정보를 삭제하고, DB의 자동 로그인 상태를 업데이트합니다.
     * @param userId 현재 로그인된 사용자의 ID (DB 업데이트를 위해 필요)
     */
    public static void logout(int userId) {
        AutoLoginUtil.clearAutoLoginUser(); // 파일 시스템 등 저장된 자동 로그인 정보 삭제
        try {
            // DB에서 해당 사용자의 자동 로그인 상태를 false로 업데이트
            UserDao.updateAutoLoginStatus(userId, false);
            System.out.println("로그아웃되었습니다. 자동 로그인 정보가 삭제되었습니다.");
        } catch (SQLException e) {
            System.err.println("로그아웃 중 자동 로그인 상태 업데이트 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
}