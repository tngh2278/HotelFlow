package cse.oop2.hotelflow.Client.Guest;

import cse.oop2.hotelflow.Client.ui.ReservationPanel; 

import javax.swing.*;
import java.awt.*;

public class GuestPortalFrame extends JFrame {

    public GuestPortalFrame() {
        setTitle("HotelFlow - 투숙객 서비스 포털");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new GridLayout(3, 1, 20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel titleLabel = new JLabel("원하시는 서비스를 선택해주세요");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // 객실 예약 버튼
        JButton btnNewReservation = new JButton("객실 예약하기 (New Reservation)");
        btnNewReservation.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        btnNewReservation.setBackground(new Color(230, 240, 255));
        btnNewReservation.addActionListener(e -> openNewReservationWindow());

        // 내 예약 관리 버튼 버튼
        JButton btnMyBooking = new JButton("내 예약 관리 (My Booking)");
        btnMyBooking.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        btnMyBooking.addActionListener(e -> {
            dispose(); 
            new GuestLoginDialog(null).setVisible(true); // 로그인 다이얼로그 열기
        });

        mainPanel.add(titleLabel);
        mainPanel.add(btnNewReservation);
        mainPanel.add(btnMyBooking);

        add(mainPanel);
    }

    private void openNewReservationWindow() {
        JFrame frame = new JFrame("객실 예약 진행 (비회원)");
        frame.setSize(900, 700);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ★ [수정됨] RoomPanel은 null, isGuestMode는 true로 설정!
        // 이렇게 하면 예약 목록(테이블)이 숨겨집니다.
        ReservationPanel reservationPanel = new ReservationPanel(null, true); 
        
        frame.add(reservationPanel);
        frame.setVisible(true);
    }
}