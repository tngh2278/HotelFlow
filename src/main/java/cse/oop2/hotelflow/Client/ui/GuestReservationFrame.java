package cse.oop2.hotelflow.Client.ui;

import javax.swing.*;
import java.awt.*;

public class GuestReservationFrame extends JFrame {

    private ReservationPanel reservationPanel;

    public GuestReservationFrame() {
        setTitle("HotelFlow - 비회원 객실 예약");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 비회원 모드용 ReservationPanel
        
        reservationPanel = new ReservationPanel(null, true);

        add(reservationPanel, BorderLayout.CENTER);
    }
}
