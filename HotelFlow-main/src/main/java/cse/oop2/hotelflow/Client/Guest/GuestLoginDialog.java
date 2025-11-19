package cse.oop2.hotelflow.Client.guest;

import cse.oop2.hotelflow.Client.net.ClientConnection;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class GuestLoginDialog extends JDialog {

    public GuestLoginDialog(Frame owner) {
        super(owner, "내 예약 조회 (비회원)", true); // 모달 창
        setSize(350, 200);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField(); 

        inputPanel.add(new JLabel("예약자 성함:"));
        inputPanel.add(nameField);
        
        inputPanel.add(new JLabel("전화번호:"));
        inputPanel.add(phoneField);
        
        phoneField.setToolTipText("예약 시 입력한 전화번호 (예: 010-1234-5678)");

        JButton btnLogin = new JButton("조회하기");
        btnLogin.addActionListener(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            
            if(name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "이름과 전화번호를 모두 입력해주세요.");
                return;
            }
            
            doGuestLogin(name, phone);
        });

        add(inputPanel, BorderLayout.CENTER);
        add(btnLogin, BorderLayout.SOUTH);
    }

    private void doGuestLogin(String name, String phone) {
        // 서버 통신 로직
        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            String command = "GUEST_LOGIN|" + name + "|" + phone;
            String response = conn.sendAndReceive(command);
            
            if (response.startsWith("OK|")) {
                // 서버 응답: OK|예약번호
                String[] parts = response.split("\\|");
                String bookingId = parts[1]; // 예약 번호를 받아옵니다.
                
                JOptionPane.showMessageDialog(this, "예약 확인 완료! (" + name + "님)");
                dispose(); // 로그인 창 닫기
                
                // ★ [연결 완료] 대시보드 화면을 엽니다.
                new GuestDashboardFrame(name, bookingId).setVisible(true); 
                
            } else if (response.startsWith("FAIL|")) {
                String msg = response.substring("FAIL|".length());
                JOptionPane.showMessageDialog(this, "조회 실패: " + msg);
            } else {
                JOptionPane.showMessageDialog(this, "일치하는 예약이 없습니다.\n이름과 전화번호를 확인해주세요.");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "서버와 연결할 수 없습니다.");
        }
    }
}