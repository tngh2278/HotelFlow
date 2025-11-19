package cse.oop2.hotelflow.Client.ui;

import javax.swing.*;
import java.awt.*;

public class GuestReservationFrame extends JFrame {

    public GuestReservationFrame() {
        setTitle("HotelFlow - 비회원 예약");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JLabel label = new JLabel("비회원 예약 화면 (추후 ReservationPanel 연동 예정)", 
                                  SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.PLAIN, 18));
        add(label, BorderLayout.CENTER);

        // TODO:
        // 비회원용 예약 패널 구현 및 연동
    }
}
