package cse.oop2.hotelflow.Client.net;


import java.io.*;
import java.net.Socket;



public class ClientConnection implements Closeable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientConnection(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public String sendAndReceive(String msg) throws IOException {
        out.println(msg);
        return in.readLine();
    }

    @Override
    public void close() throws IOException {
        if (socket != null) socket.close();
    }
}
