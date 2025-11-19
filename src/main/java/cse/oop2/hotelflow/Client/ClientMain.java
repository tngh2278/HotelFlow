package cse.oop2.hotelflow.Client;

import javax.swing.SwingUtilities;

import cse.oop2.hotelflow.Client.ui.LoginFrame;

public class ClientMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {           // Runnable 인터페이스의 run() 메서드 구현 람다
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);

        });
    }    
}
