package cse.oop2.hotelflow.Client;

import javax.swing.SwingUtilities;

import cse.oop2.hotelflow.Client.ui.StartFrame;

public class ClientMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StartFrame frame = new StartFrame();
            frame.setVisible(true);
        });
    }
}
