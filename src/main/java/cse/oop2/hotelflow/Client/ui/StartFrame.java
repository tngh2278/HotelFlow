package cse.oop2.hotelflow.Client.ui;

import javax.swing.*;
import java.awt.*;

public class StartFrame extends JFrame {

    public StartFrame() {
        setTitle("HotelFlow - 시작 화면");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 350);
        setLocationRelativeTo(null); // 화면 중앙

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // 상단 제목
        JLabel title = new JLabel("HotelFlow - 호텔 관리 시스템", SwingConstants.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        add(title, BorderLayout.NORTH);

        // 가운데 버튼 3개
        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        JButton staffButton = new JButton("직원 / 관리자 로그인");
        JButton customerButton = new JButton("고객 로그인");
        JButton guestButton = new JButton("비회원 예약");

        Font btnFont = new Font("SansSerif", Font.PLAIN, 18);
        staffButton.setFont(btnFont);
        customerButton.setFont(btnFont);
        guestButton.setFont(btnFont);

        centerPanel.add(staffButton);
        centerPanel.add(customerButton);
        centerPanel.add(guestButton);

        add(centerPanel, BorderLayout.CENTER);

        // 하단 안내 문구
        JLabel info = new JLabel("로그인 방식을 선택하세요.", SwingConstants.CENTER);
        info.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(info, BorderLayout.SOUTH);

        // 버튼 액션
        staffButton.addActionListener(e -> openStaffLogin());
        customerButton.addActionListener(e -> openCustomerLogin());
        guestButton.addActionListener(e -> openGuestReservation());
    }

    // 직원 / 관리자 로그인으로 이동
    private void openStaffLogin() {
        LoginFrame login = new LoginFrame(LoginMode.STAFF_ADMIN);
        login.setVisible(true);
        dispose(); // 시작 화면 닫기
    }

    // 고객 로그인으로 이동 
    private void openCustomerLogin() {
        LoginFrame login = new LoginFrame(LoginMode.CUSTOMER);
        login.setVisible(true);
        dispose();
    }

    // 비회원 예약 화면으로 이동
    private void openGuestReservation() {
        GuestReservationFrame guest = new GuestReservationFrame();
        guest.setVisible(true);
        dispose();
    }
}
