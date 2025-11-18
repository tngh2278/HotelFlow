package cse.oop2.hotelflow.Server.file;

import cse.oop2.hotelflow.Common.model.User;
import cse.oop2.hotelflow.Common.model.UserRole;

import java.io.*;
import java.nio.file.*;
import java.util.*;



public class FileUserRepository{
    private final Path userFile;

    public FileUserRepository(String filePath){
        this.userFile = Paths.get(filePath);
    }

    public List<User> findAll() throws IOException {
        List<User> users = new ArrayList<>();
        if (!Files.exists(userFile)) return users;

        try (BufferedReader br = Files.newBufferedReader(userFile)) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null ){
                if(first){ //헤더 스킵
                    first = false;
                    continue;
                }
            String[] parts = line.split(",");
            if (parts.length < 4) continue;
            String id = parts[0];
            String name = parts[1];
            String password = parts[2];
            UserRole role = UserRole.valueOf(parts[3]);
            users.add(new User(id,name,password,role));
            }
        }
    return users;
    }

    public  Optional<User> findByIdAndPassword(String id, String password) throws IOException{
        return findAll().stream().filter(u -> u.getId().equals(id) && u.getPassword().equals(password)).findFirst();
    }
}