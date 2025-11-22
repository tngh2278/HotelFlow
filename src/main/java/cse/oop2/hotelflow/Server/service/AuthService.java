package cse.oop2.hotelflow.Server.service;

import cse.oop2.hotelflow.Common.model.User;
import cse.oop2.hotelflow.Common.model.UserRole;

import java.io.IOException;
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
            return userRepository.findByIdAndPassword(id, password);
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
        return user;
    }
}
