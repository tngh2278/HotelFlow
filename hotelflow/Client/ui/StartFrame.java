package cse.oop2.hotelflow.Client.ui;

// ★ 여기가 중요합니다! 방금 만든 투숙객 패키지의 포털 화면을 가져옵니다.
import cse.oop2.hotelflow.Client.guest.GuestPortalFrame;

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
        JButton guestButton = new JButton("비회원 예약 / 투숙객 포털"); // 이름 살짝 변경 추천

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
        
        // ★ 여기가 수정된 부분입니다.
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
        // 예전 코드: GuestReservationFrame guest = new GuestReservationFrame();
        
        // 새 코드: 새로 만든 포털(GuestPortalFrame)을 엽니다.
        new GuestPortalFrame().setVisible(true);
        
        // StartFrame을 끄지 않고 그대로 둘지, 끌지는 선택입니다.
        // 보통 키오스크라면 시작 화면은 안 끄는 게 좋습니다.
        // dispose(); 
    }
}