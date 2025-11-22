package cse.oop2.hotelflow.Client.ui;

import cse.oop2.hotelflow.Client.Guest.GuestPortalFrame;

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

        // 버튼 4개
        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        JButton staffButton = new JButton("직원 / 관리자 로그인");
        JButton customerButton = new JButton("고객 로그인");
        JButton guestButton = new JButton("비회원 예약 / 포털");
        JButton customerRegisterButton = new JButton("고객 회원가입");
        
        //회원가입 창 열기
        customerRegisterButton.addActionListener(e -> {
            new CustomerRegisterFrame().setVisible(true);
        });

        Font btnFont = new Font("SansSerif", Font.PLAIN, 18);
        staffButton.setFont(btnFont);
        customerButton.setFont(btnFont);
        guestButton.setFont(btnFont);

        centerPanel.add(staffButton);
        centerPanel.add(customerButton);
        centerPanel.add(guestButton);
        centerPanel.add(customerRegisterButton);

        add(centerPanel, BorderLayout.CENTER);

        // 하단 안내 문구
        JLabel info = new JLabel("로그인/회원가입", SwingConstants.CENTER);
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
        // dispose(); // 직원 로그인은 창을 닫고 이동
    }

    // 고객 로그인으로 이동 
    private void openCustomerLogin() {
        LoginFrame login = new LoginFrame(LoginMode.CUSTOMER);
        login.setVisible(true);
        // dispose();
    }

    // 비회원 예약 및 투숙객 포털로 이동
    private void openGuestReservation() {
        new GuestPortalFrame().setVisible(true);
    }
}