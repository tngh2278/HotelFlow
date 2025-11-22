package cse.oop2.hotelflow.Server.service;

import cse.oop2.hotelflow.Common.model.User;
import cse.oop2.hotelflow.Common.model.UserRole;
import cse.oop2.hotelflow.Server.log.HotelLogger;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import cse.oop2.hotelflow.Server.file.FileUserRepository;

public class AuthService {

    private final FileUserRepository userRepository;

    public AuthService(FileUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 1 로그인 
    public Optional<User> login(String id, String password) {
        try {
            Optional<User> result = userRepository.findByIdAndPassword(id, password);
            //  로그인 성공 시 감사 로그 기록
            result.ifPresent(user ->
                    HotelLogger.audit(
                            user.getId(),                  // actor: 로그인한 사용자 ID
                            "LOGIN",                       // action
                            "role=" + user.getRole()      // detail
                    )
            );
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    // 2 고객 회원가입 
    public User registerCustomer(String id,
            String name,
            String password,
            String phone) throws IOException {

        if (userRepository.existsById(id)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        User user = new User(id, name, password, UserRole.CUSTOMER, phone);

        userRepository.save(user);   // CSV에 한 줄 추가

        // 회원가입 감사 로그
        HotelLogger.audit(
                id,                           // actor: 새로 가입한 고객 ID
                "REGISTER_CUSTOMER",          // action
                "name=" + name + ", phone=" + phone
        );
        return user;
    }
    // 3 전체 사용자 조회
    public List<User> findAllUsers() throws IOException {
        return userRepository.findAll();
    }

    // 4 사용자 추가
    public void addUser(String id,
            String name,
            String password,
            String roleStr,
            String phone) throws IOException {

        UserRole role;
        try {
            // "ADMIN", "STAFF", "CUSTOMER" 같은 문자열을 enum으로
            role = UserRole.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("역할 값이 올바르지 않습니다: " + roleStr);
        }

        // ID 중복 체크
        boolean exists = userRepository.findAll().stream()
                .anyMatch(u -> u.getId().equals(id));
        if (exists) {
            throw new IllegalArgumentException("이미 존재하는 ID입니다.");
        }

        User newUser = new User(id, name, password, role, phone);
        userRepository.addUser(newUser);

         // 관리자/직원 사용자 추가 감사 로그
        HotelLogger.audit(
                id,                          // 일단은 생성된 사용자 ID 기준으로
                "ADD_USER",
                "name=" + name + ", role=" + role + ", phone=" + phone
        );
    }

    // 5 사용자 삭제
    public void deleteUser(String id) throws IOException {
        boolean removed = userRepository.deleteById(id);
        if (!removed) {
            throw new IllegalArgumentException("해당 ID를 가진 사용자가 없습니다: " + id);
        }
        // 사용자 삭제 감사 로그
        HotelLogger.audit(
                id,                        // 삭제 대상 ID를 actor로 기록
                "DELETE_USER",
                "사용자 계정 삭제"
        );
    }
    
}

