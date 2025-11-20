package cse.oop2.hotelflow.Server.service;

import java.io.IOException;
import java.util.Optional;

import cse.oop2.hotelflow.Common.model.User;
import cse.oop2.hotelflow.Server.file.FileUserRepository;


public class AuthService {
    private final FileUserRepository userRepository;

    public AuthService(FileUserRepository userRepository){
        this.userRepository = userRepository;
    }

    public Optional<User> login(String id, String password){
        try {
            return userRepository.findByIdAndPassword(id, password);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
