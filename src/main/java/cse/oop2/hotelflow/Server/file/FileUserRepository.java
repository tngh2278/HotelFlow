package cse.oop2.hotelflow.Server.file;

import cse.oop2.hotelflow.Common.model.User;
import cse.oop2.hotelflow.Common.model.UserRole;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileUserRepository {

    private final Path userFile;

    public FileUserRepository(String filePath) {
        this.userFile = Paths.get(filePath);
    }

    /**
     * CSV 전체를 User 리스트로 읽기 포맷: id,name,password,role,phone 예전 데이터(4컬럼)도 읽히도록
     * phone은 옵션 처리
     */
    public List<User> findAll() throws IOException {
        List<User> users = new ArrayList<>();
        if (!Files.exists(userFile)) {
            return users;
        }

        try (BufferedReader br = Files.newBufferedReader(userFile)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length < 4) {
                    continue; // id,name,password,role 최소 4개
                }
                String id = parts[0].trim();
                String name = parts[1].trim();
                String password = parts[2].trim();
                String roleStr = parts[3].trim();
                String phone = (parts.length >= 5) ? parts[4].trim() : "";

                try {
                    UserRole role = UserRole.valueOf(roleStr);
                    //  User 생성자 모양에 맞게 조정
                    User user = new User(id, name, password, role, phone);
                    users.add(user);
                } catch (IllegalArgumentException e) {
                    // 잘못된 role, 잘못된 한 줄은 무시
                    e.printStackTrace();
                }
            }
        }
        return users;
    }

    /**
     * 로그인용: id + password 로 사용자 찾기
     */
    public Optional<User> findByIdAndPassword(String id, String password) throws IOException {
        for (User u : findAll()) {
            if (u.getId().equals(id) && u.getPassword().equals(password)) {
                return Optional.of(u);
            }
        }
        return Optional.empty();
    }

    /**
     * 아이디 존재 여부 확인 (회원가입 중복 체크용)
     */
    public boolean existsById(String id) throws IOException {
        for (User u : findAll()) {
            if (u.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 전체를 덮어쓰는 방식으로 저장
     */
    public void saveAll(List<User> users) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (User u : users) {
            sb.append(u.getId())
                    .append(',')
                    .append(u.getName())
                    .append(',')
                    .append(u.getPassword())
                    .append(',')
                    .append(u.getRole().name())
                    .append(',');
            // phone 컬럼이 항상 존재하도록
            if (u.getPhone() != null) {
                sb.append(u.getPhone());
            }
            sb.append('\n');
        }
        FileUtils.writeWithLock(userFile, sb.toString());
    }

    /**
     * 한 명 추가 저장 (기존 전체 읽고 + append 후 saveAll)
     */
    public void addUser(User user) throws IOException {
        List<User> users = findAll();
        users.add(user);
        saveAll(users);
    }
        /*  ID로 유저 삭제
         */
    public boolean deleteById(String id) throws IOException {
        List<User> users = findAll();
        boolean removed = users.removeIf(u -> u.getId().equals(id));
        if (removed) {
            saveAll(users);
        }
        return removed;
    }

    public void save(User user) throws IOException {
        addUser(user);
    }
}
