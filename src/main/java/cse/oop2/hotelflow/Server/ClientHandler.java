package cse.oop2.hotelflow.Server;

import cse.oop2.hotelflow.Common.model.User;
import cse.oop2.hotelflow.Server.service.AuthService;

import java.io.*;
import java.net.Socket;
import java.util.Optional;

public class ClientHandler implements Runnable{
    private final Socket socket;
    private final AuthService authService;

    public ClientHandler(Socket socket, AuthService authService) {
        this.socket = socket;
        this.authService = authService;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                // 임시 프로토콜: LOGIN|id|password
                String[] parts = line.split("\\|");
                String command = parts[0];

                if ("PING".equals(command)) {
                    out.println("PONG");
                } else if ("LOGIN".equals(command) && parts.length >= 3) {
                    String id = parts[1];
                    String password = parts[2];

                    Optional<User> userOpt = authService.login(id, password);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        out.println("OK|" + user.getRole());
                    } else {
                        out.println("FAIL|아이디 또는 비밀번호가 올바르지 않습니다.");
                    }
                } else {
                    out.println("FAIL|알 수 없는 명령: " + command);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
