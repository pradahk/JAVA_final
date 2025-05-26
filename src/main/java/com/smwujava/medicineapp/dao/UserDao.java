package com.smwujava.medicineapp.dao;

import com.smwujava.medicineapp.db.DBManager; // DBManager 사용
import com.smwujava.medicineapp.model.User; // User 모델 사용
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException; // SQLException 사용
import java.sql.Statement;   // 자동 생성된 키 가져올 때 필요

// 사용자(Users 테이블) 데이터 접근 객체 (DAO)
// 데이터베이스의 Users 테이블과 관련된 데이터 CRUD(Create, Read, Update, Delete) 작업을 수행합니다.
// 모든 메서드는 static으로 선언되어 있으며, 내부적으로 DBManager를 통해 Connection을 관리합니다.
// 각 메서드는 데이터베이스 오류 발생 시 SQLException을 던집니다.
public class UserDao {

    private UserDao() {
        // Utility 클래스처럼 사용되므로 인스턴스화 방지
    }

    /**
     * 새로운 사용자를 데이터베이스의 Users 테이블에 삽입합니다 (회원가입 시 사용).
     * @param user 삽입할 사용자 정보 (User 객체, username과 password 필드 필요)
     * @return 데이터베이스에서 자동 생성된 사용자의 user_id. 삽입 실패 시 (영향 받은 행 없음 등) -1.
     * @throws SQLException 데이터베이스 접근 또는 SQL 실행 중 오류 발생 시
     */
    public static int insertUser(User user) throws SQLException { // <<< throws SQLException 추가
        // SQL INSERT 구문. user_id는 AUTOINCREMENT이므로 데이터 삽입 시 값을 지정하지 않습니다.
        String sql = "INSERT INTO Users (username, password) VALUES (?, ?)";
        int generatedId = -1; // 생성된 user_id를 저장할 변수

        // try-with-resources 구문: Connection과 PreparedStatement를 사용 후 자동으로 닫아줍니다.
        try (Connection conn = DBManager.getConnection(); // DBManager에서 데이터베이스 연결 가져오기 (자동 닫힘)
             // PreparedStatement 생성 시 Statement.RETURN_GENERATED_KEYS 옵션으로 자동 생성된 키(user_id)를 받아올 수 있도록 함
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { // PreparedStatement 생성 (자동 닫힘)

            // SQL 구문의 ?에 실제 값을 설정 (인덱스는 1부터 시작)
            pstmt.setString(1, user.getUsername());
            // TODO: 실제 앱에서는 보안을 위해 비밀번호를 해시화하여 저장해야 합니다.
            pstmt.setString(2, user.getPassword()); // 현재는 평문으로 설정

            // SQL 실행 (INSERT, UPDATE, DELETE 구문은 executeUpdate() 메서드 사용)
            // executeUpdate()는 영향을 받은 행의 개수를 반환합니다.
            int affectedRows = pstmt.executeUpdate();

            // 삽입된 행이 있고 자동 생성된 키가 있다면 가져옴
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) { // 자동 생성된 키 결과 (자동 닫힘)
                    if (rs.next()) {
                        generatedId = rs.getInt(1); // 첫 번째 자동 생성된 키(user_id) 가져오기
                        // System.out.println("User inserted successfully with ID: " + generatedId); // 디버깅
                    }
                }
            }

        } // try-with-resources 블록을 벗어나면 Connection, PreparedStatement, ResultSet이 자동으로 닫힙니다.
        // SQLException이 발생하면 여기서 잡지 않고 호출한 곳(UserService)으로 던집니다.
        return generatedId; // 데이터베이스에서 생성된 user_id 또는 삽입 실패 시 -1 반환
    }

    /**
     * 사용자 이름(username)이 데이터베이스에 이미 존재하는지 확인합니다 (회원가입 시 중복 확인).
     * @param username 확인할 사용자 이름
     * @return 해당 username이 존재하면 true, 그렇지 않으면 false.
     * @throws SQLException 데이터베이스 접근 또는 SQL 실행 중 오류 발생 시
     */
    public static boolean existsByUsername(String username) throws SQLException { // <<< throws SQLException 추가
        String sql = "SELECT COUNT(*) FROM Users WHERE username = ?";
        // try-with-resources 구문으로 Connection, PreparedStatement, ResultSet 안전하게 관리
        try (Connection conn = DBManager.getConnection(); // DBManager에서 연결 가져오기 (자동 닫힘)
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // PreparedStatement 생성 (자동 닫힘)

            pstmt.setString(1, username); // SQL 구문의 (?)에 사용자 이름 설정

            try (ResultSet rs = pstmt.executeQuery()) { // 쿼리 결과 (자동 닫힘)
                // 결과가 있고 COUNT(*) 값이 0보다 크면 존재하는 것
                return rs.next() && rs.getInt(1) > 0;
            }

        } // try-with-resources 블록을 벗어나면 리소스 자동 닫힘. SQLException 발생 시 던짐.
    }

    /**
     * 사용자 이름과 비밀번호가 일치하는 사용자를 찾아 정보를 가져옵니다 (로그인 시 사용).
     * @param username 찾을 사용자 이름
     * @param password 확인할 비밀번호 (DB에 해시화되어 있다면 해시화된 비밀번호를 전달해야 함)
     * @return 일치하는 사용자 정보(User 객체) 또는 사용자가 없거나 비밀번호가 틀릴 경우 null.
     * @throws SQLException 데이터베이스 접근 또는 SQL 실행 중 오류 발생 시
     */
    public static User findUserByUsernameAndPassword(String username, String password) throws SQLException { // <<< throws SQLException 추가
        // SQL SELECT 구문. username과 password 모두 일치하는 행을 찾습니다.
        String sql = "SELECT user_id, username, password FROM Users WHERE username = ? AND password = ?";
        User user = null; // 찾은 사용자 정보를 담을 변수 (기본값 null)

        // try-with-resources 구문으로 Connection, PreparedStatement, ResultSet 안전하게 관리
        try (Connection conn = DBManager.getConnection(); // DBManager에서 연결 가져오기 (자동 닫힘)
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // PreparedStatement 생성 (자동 닫힘)

            pstmt.setString(1, username); // 첫 번째 (?)에 사용자 이름 설정
            // TODO: 실제 앱에서는 전달받은 password를 해시화하여 DB의 해시값과 비교해야 합니다.
            pstmt.setString(2, password); // 두 번째 (?)에 비밀번호 설정 (현재는 평문 비교)

            try (ResultSet rs = pstmt.executeQuery()) { // 쿼리 결과 (자동 닫힘)
                // 결과가 있다면 (사용자가 있다면)
                if (rs.next()) {
                    // ResultSet에서 컬럼 이름으로 값을 읽어와 User 객체 생성
                    int userId = rs.getInt("user_id");
                    String foundUsername = rs.getString("username");
                    String foundPassword = rs.getString("password"); // DB에 저장된 비밀번호 (해시값)

                    user = new User(userId, foundUsername, foundPassword); // User 객체 생성 (DB에서 읽어온 ID 포함)
                    // System.out.println("User found for login: " + foundUsername); // 디버깅
                } else {
                    // System.out.println("User not found or password incorrect for username: " + username); // 디버깅
                }
            }

        } // try-with-resources 블록을 벗어나면 리소스 자동 닫힘. SQLException 발생 시 던짐.
        return user; // 찾은 User 객체 또는 null 반환
    }

    /**
     * 특정 사용자의 비밀번호를 수정합니다.
     * @param username 비밀번호를 수정할 사용자의 이름
     * @param newPassword 새로 설정할 비밀번호 (DB에 해시화하여 저장해야 함)
     * @return 수정 성공 시 true, 실패 시 false (영향 받은 행 없음 등).
     * @throws SQLException 데이터베이스 접근 또는 SQL 실행 중 오류 발생 시
     */
    public static boolean updatePassword(String username, String newPassword) throws SQLException { // <<< throws SQLException 추가
        // SQL UPDATE 구문. username을 기준으로 해당 행을 찾아서 password 수정
        String sql = "UPDATE Users SET password = ? WHERE username = ?";

        try (Connection conn = DBManager.getConnection(); // DBManager에서 연결 가져오기 (자동 닫힘)
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // PreparedStatement 생성 (자동 닫힘)

            // SQL 구문의 ?에 실제 값 설정
            // TODO: 실제 앱에서는 newPassword를 해시화하여 저장해야 합니다.
            pstmt.setString(1, newPassword); // 새로운 비밀번호 설정
            pstmt.setString(2, username);    // 조건을 위한 username 설정

            // SQL 실행 (UPDATE)
            int affectedRows = pstmt.executeUpdate();

            // affectedRows가 0보다 크면 성공 (최소 1개 행이 수정됨)
            return affectedRows > 0;

        } // try-with-resources 블록을 벗어나면 리소스 자동 닫힘. SQLException 발생 시 던짐.
    }

    /**
     * 특정 사용자의 사용자 이름(username)을 수정합니다.
     * @param currentUsername 현재 사용자 이름
     * @param newUsername 새로 설정할 사용자 이름
     * @return 수정 성공 시 true, 실패 시 false (영향 받은 행 없음 등).
     * @throws SQLException 데이터베이스 접근 또는 SQL 실행 중 오류 발생 시
     */
    public static boolean updateUsername(String currentUsername, String newUsername) throws SQLException { // <<< throws SQLException 추가
        // SQL UPDATE 구문. username을 기준으로 해당 행을 찾아서 username 수정
        String sql = "UPDATE Users SET username = ? WHERE username = ?";

        try (Connection conn = DBManager.getConnection(); // DBManager에서 연결 가져오기 (자동 닫힘)
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // PreparedStatement 생성 (자동 닫힘)

            // SQL 구문의 ?에 실제 값 설정
            pstmt.setString(1, newUsername);     // 새로운 username 설정
            pstmt.setString(2, currentUsername); // 조건을 위한 현재 username 설정

            // SQL 실행 (UPDATE)
            int affectedRows = pstmt.executeUpdate();

            // affectedRows가 0보다 크면 성공 (최소 1개 행이 수정됨)
            return affectedRows > 0;

        } // try-with-resources 블록을 벗어나면 리소스 자동 닫힘. SQLException 발생 시 던짐.
    }

    // TODO: 필요에 따라 다른 데이터 접근 메서드들을 여기에 추가할 수 있습니다.
    // 예를 들어:
    // - 사용자 ID로 사용자를 찾는 메서드: findUserById(int userId) throws SQLException
    // - 사용자를 삭제하는 메서드: deleteUser(int userId) throws SQLException
}