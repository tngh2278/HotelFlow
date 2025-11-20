package cse.oop2.hotelflow.Client.ui;

import cse.oop2.hotelflow.Common.model.UserRole;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final UserRole role;
    private final RoomPanel roomPanel;
    private final ReservationPanel reservationPanel;
    private final RoomServicePanel roomServicePanel;

    public MainFrame(UserRole role) {
        this.role = role;
        this.roomPanel = new RoomPanel();
        this.reservationPanel = new ReservationPanel(roomPanel);
        this.roomServicePanel = new RoomServicePanel(); 

        setTitle("HotelFlow 메인 - " + role);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JTabbedPane tabs = new JTabbedPane();

        // 객실 현황 탭
        tabs.addTab("객실 현황", roomPanel);
        tabs.addTab("예약", reservationPanel);
        tabs.addTab("룸서비스", roomServicePanel);

        add(tabs, BorderLayout.CENTER);
    }

    public void loadInitialData() {
        roomPanel.loadRooms();
        reservationPanel.loadReservations();
        roomServicePanel.loadOrders();
    }
}