package cse.oop2.hotelflow.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import cse.oop2.hotelflow.Server.file.FileUserRepository;
import cse.oop2.hotelflow.Server.file.FileRoomRepository;
import cse.oop2.hotelflow.Server.service.AuthService;
import cse.oop2.hotelflow.Server.service.RoomService;

public class ServerMain {
    public static void main(String[] args) {
        int port = 5555;

        FileUserRepository userRepository = new FileUserRepository("data/user.csv");
        AuthService authService = new AuthService(userRepository);

        FileRoomRepository roomRepository = new FileRoomRepository("data/rooms.csv");
        RoomService roomService = new RoomService(roomRepository);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("HotelFlow 서버 시작, 포트: " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("클라이언트 접속: " + clientSocket);

                ClientHandler handler = new ClientHandler(clientSocket, authService, roomService);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}