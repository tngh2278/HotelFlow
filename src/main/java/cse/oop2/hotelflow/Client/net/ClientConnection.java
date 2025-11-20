package cse.oop2.hotelflow.Client.net;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets; //UTF-8 사용을 위한 임포트

public class ClientConnection implements AutoCloseable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientConnection(String host, int port) throws IOException {
        this.socket = new Socket(host, port);

        //한글 깨짐 방지를 위해 UTF-8 인코딩을 강제로 지정
        this.out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        
        this.in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    }

    public String sendAndReceive(String message) throws IOException {
        out.println(message);
        return in.readLine();
    }

    @Override
    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}