package com.smwujava.medicineapp.service;

import com.smwujava.medicineapp.dao.UserDao; // UserDao import
import com.smwujava.medicineapp.model.User; // User 모델 import
import com.smwujava.medicineapp.util.AutoLoginUtil; // AutoLoginUtil 사용 시 import 유지
import java.sql.SQLException; // SQLException 처리는 필요

// 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스
// DAO를 사용하여 데이터베이스와 상호작용합니다.
// DAO가 static 메서드 방식이므로, UserService도 static 메서드 방식으로 구성합니다.
public class UserService {

    private UserService() {
    }

    /**
     * 새로운 사용자를 등록합니다 (회원가입).
     * @param username 사용자 이름
     * @param password 비밀번호
     * @return 회원가입 성공 시 true, 실패 시 false
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    public static boolean register(String username, String password) { // static 메서드
        try { // SQLException 처리를 여기서 합니다.

            // 사용자 이름 중복 확인 (UserDao의 static 메서드 호출)
            if (UserDao.existsByUsername(username)) {
                System.out.println("이미 존재하는 사용자 이름입니다.");
                return false;
            }

            // 비밀번호 유효성 검사 (길이, 특수문자 등)
            if (password.length() < 7) {
                System.out.println("비밀번호는 최소 7자 이상이어야 합니다.");
                return false;
            }
            // TODO: 특수문자 포함 정규식 패턴 검토 및 강화 필요
            // 현재 정규식 패턴은 일부 특수문자만 포함하는 등 완벽하지 않을 수 있습니다.
            // 실제 앱에서는 비밀번호 정책에 맞는 더 정확한 정규식을 사용해야 합니다.
            if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
                System.out.println("비밀번호에는 특수문자가 포함되어야 합니다.");
                return false;
            }

            // User 객체 생성 (새 사용자이므로 userId는 0 또는 DB에서 자동 생성될 값으로 간주)
            // DAO의 insertUser 메서드는 User 객체를 받으므로 User 객체를 만듭니다.
            // User 모델의 (String username, String password) 생성자 사용
            User newUser = new User(username, password);

            // 사용자 정보를 DB에 삽입 (UserDao의 static 메서드 호출)
            // insertUser는 생성된 ID를 반환하지만, 여기서는 성공 여부만 확인
            int generatedId = UserDao.insertUser(newUser);

            // 생성된 ID가 -1이 아니면 성공한 것으로 간주
            return generatedId != -1;

        } catch (SQLException e) {
            System.err.println("회원가입 중 데이터베이스 오류 발생: " + e.getMessage());
            e.printStackTrace();
            // 실제 앱에서는 이 오류를 UI로 전달
            return false; // 오류 발생 시 실패
        }
    }

    /**
     * 사용자 로그인 처리를 수행합니다.
     * @param username 사용자 이름
     * @param password 비밀번호 (DB에 해시화되어 있다면 해시화된 비밀번호를 전달해야 함)
     * @return 로그인 성공 시 true, 실패 시 false
     * @throws SQLException 데이터베이스 오류 발생 시 (SQLException을 여기서 처리하도록 변경)
     */
    public static boolean login(String username, String password) { // static 메서드
        try { // SQLException 처리를 여기서 합니다.
            // 사용자 이름과 비밀번호로 사용자 찾기 (UserDao의 static 메서드 호출)
            User user = UserDao.findUserByUsernameAndPassword(username, password);

            // 사용자를 찾았으면 (null이 아니면) 로그인 성공
            return user != null;

        } catch (SQLException e) {
            System.err.println("로그인 중 데이터베이스 오류 발생: " + e.getMessage());
            e.printStackTrace();
            // 실제 앱에서는 이 오류를 UI로 전달
            return false; // 오류 발생 시 실패
        }
    }

    // 비밀번호로 사용자 이름 찾기 - 보안상 권장되지 않는 기능이지만, 사용자님 기존 코드를 반영
    // 실제 구현 시 필요성 재검토 및 보안 강화 필요 (예: 특정 질문/답변 등 추가 확인)
    public static String findUsername(String password) { // static 메서드
        try { // SQLException 처리를 여기서 합니다.
            // UserDao에 findUsernameByPassword 메서드를 다시 추가하거나,
            // 아니면 findUserByUsernameAndPassword 등을 활용하여 구현해야 합니다.
            // 저희가 통합한 UserDao에서는 findUsernameByPassword 메서드를 제거했습니다.
            // TODO: findUsernameByPassword 기능이 정말 필요하다면 UserDao에 해당 메서드를 추가해야 합니다.
            System.err.println("UserDao에 findUsernameByPassword 메서드가 없습니다. 기능 구현 필요.");
            return null; // 현재는 기능 미지원
        } catch (Exception e) { // UserDao 메서드에서 SQLException이 발생하지 않으므로 catch(Exception)으로 변경 가능
            e.printStackTrace();
            return null;
        }
    }

    // 아이디로 비밀번호 찾기 - 보안상 절대 권장되지 않는 기능. 사용자에게 비밀번호를 알려주면 안 됨.
    // 비밀번호 재설정 기능을 구현해야 합니다.
    public static String findPassword(String username) { // static 메서드
        // TODO: 이 기능은 보안상 삭제하거나 비밀번호 재설정 로직으로 변경해야 합니다.
        System.err.println("보안상 아이디로 비밀번호를 찾는 기능은 권장되지 않습니다. 비밀번호 재설정 로직을 구현하세요.");
        return null; // 보안상 반환하지 않음
    }

    /**
     * 사용자의 비밀번호를 수정합니다.
     * @param username 비밀번호를 수정할 사용자 이름
     * @param currentPassword 현재 비밀번호 (확인용)
     * @param newPassword 새로 설정할 비밀번호
     * @return 비밀번호 수정 성공 시 true, 실패 시 false
     * @throws SQLException 데이터베이스 오류 발생 시 (SQLException을 여기서 처리하도록 변경)
     */
    public static boolean changePassword(String username, String currentPassword, String newPassword) { // static 메서드
        try { // SQLException 처리를 여기서 합니다.
            // 현재 비밀번호 일치 확인
            if (!login(username, currentPassword)) { // login 메서드 활용
                System.out.println("현재 비밀번호가 일치하지 않습니다.");
                return false;
            }

            // 새 비밀번호 유효성 검사
            if (newPassword.length() < 7) {
                System.out.println("새 비밀번호는 최소 7자 이상이어야 합니다.");
                return false;
            }
            // TODO: 특수문자 포함 정규식 패턴 검토 및 강화 필요
            if (!newPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
                System.out.println("새 비밀번호에는 특수문자가 포함되어야 합니다.");
                return false;
            }

            // 비밀번호 업데이트 (UserDao의 static 메서드 호출)
            // TODO: 실제 앱에서는 newPassword를 해시화하여 DAO에 전달해야 합니다.
            return UserDao.updatePassword(username, newPassword);

        } catch (SQLException e) {
            System.err.println("비밀번호 수정 중 데이터베이스 오류 발생: " + e.getMessage());
            e.printStackTrace();
            // 실제 앱에서는 이 오류를 UI로 전달
            return false; // 오류 발생 시 실패
        }
    }

    /**
     * 사용자의 사용자 이름(username)을 수정합니다.
     * @param currentUsername 현재 사용자 이름
     * @param newUsername 새로 설정할 사용자 이름
     * @return 사용자 이름 수정 성공 시 true, 실패 시 false
     * @throws SQLException 데이터베이스 오류 발생 시 (SQLException을 여기서 처리하도록 변경)
     */
    public static boolean changeUsername(String currentUsername, String newUsername) { // static 메서드
        try { // SQLException 처리를 여기서 합니다.
            // 새 사용자 이름 중복 확인 (UserDao의 static 메서드 호출)
            if (UserDao.existsByUsername(newUsername)) {
                System.out.println("이미 존재하는 아이디입니다.");
                return false;
            }

            // 사용자 이름 업데이트 (UserDao의 static 메서드 호출)
            return UserDao.updateUsername(currentUsername, newUsername);

        } catch (SQLException e) {
            System.err.println("사용자 이름 수정 중 데이터베이스 오류 발생: " + e.getMessage());
            e.printStackTrace();
            // 실제 앱에서는 이 오류를 UI로 전달
            return false; // 오류 발생 시 실패
        }
    }

    // 로그아웃 기능 (자동 로그인 정보 삭제) - 이 로직은 DB와 직접 관련 없으므로 그대로 유지
    public static void logout() { // static 메서드로 변경 (UserService가 static 방식이므로)
        AutoLoginUtil.clearAutoLoginUser();
        System.out.println("로그아웃되었습니다. 자동 로그인 정보가 삭제되었습니다.");
    }

    // TODO: 필요에 따라 사용자 관련 비즈니스 로직 메서드를 추가합니다.
    // - 회원 탈퇴 (DB에서 사용자 및 관련 데이터 삭제 - UserDao.deleteUser 호출 등)
}