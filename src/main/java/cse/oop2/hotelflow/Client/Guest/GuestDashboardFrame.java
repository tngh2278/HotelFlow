package cse.oop2.hotelflow.Client.Guest;

import cse.oop2.hotelflow.Client.net.ClientConnection;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;

public class GuestDashboardFrame extends JFrame {
    private String guestName;
    private String bookingId;

    public GuestDashboardFrame(String guestName, String bookingId) {
        this.guestName = guestName;
        this.bookingId = bookingId;

        setTitle("나의 예약 관리 - " + guestName);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
    }

    private void initComponents() {
        JPanel gridPanel = new JPanel(new GridLayout(3, 2, 15, 15));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 버튼 생성
        JButton btnDetail = createBtn("예약 상세 조회", "예약된 방 정보 확인");
        JButton btnCheckIn = createBtn("온라인 체크인", "입실 요청");
        JButton btnBill = createBtn("청구 내역 조회", "결제 예상 금액 확인");
        JButton btnFeedback = createBtn("피드백 작성", "서비스 평가 및 건의");
        JButton btnFnB = createBtn("식음료 주문 내역", "레스토랑/룸서비스 이용 기록");
        JButton btnClose = new JButton("종료");
        
        btnClose.addActionListener(e -> dispose());

        // 예약 상세 조회 기능 연결
        btnDetail.addActionListener(e -> requestReservationDetail());

        // 온라인 체크인 기능 연결
        btnCheckIn.addActionListener(e -> requestOnlineCheckIn());

        // 나머지 기능 (준비 중)
        btnBill.addActionListener(e -> requestBill());
        btnFeedback.addActionListener(e -> new FeedbackDialog(this, bookingId).setVisible(true));
        btnFnB.addActionListener(e -> openFnBMenu());

        gridPanel.add(btnDetail);
        gridPanel.add(btnCheckIn);
        gridPanel.add(btnBill);
        gridPanel.add(btnFeedback);
        gridPanel.add(btnFnB);
        gridPanel.add(btnClose);

        // 상단 라벨
        JLabel topLabel = new JLabel(guestName + " 고객님의 예약 정보 (" + bookingId + ")");
        topLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        topLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        add(topLabel, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);
    }

    private JButton createBtn(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setToolTipText(tooltip);
        btn.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        btn.setBackground(new Color(240, 245, 255)); 
        btn.setFocusPainted(false);
        return btn;
    }

    // 예약 상세 조회 로직
    private void requestReservationDetail() {
        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            // 서버에 상세 정보 요청
            String command = "GUEST_GET_DETAIL|" + bookingId;
            String response = conn.sendAndReceive(command);

            if (response != null && response.startsWith("OK|")) {
                // 응답 형식: OK|ID|Room|Name|Phone|In|Out|Status
                String[] parts = response.split("\\|");
                if (parts.length < 8) {
                    JOptionPane.showMessageDialog(this, "데이터 형식이 올바르지 않습니다.");
                    return;
                }

                String id = parts[1];
                String room = parts[2];
                String name = parts[3];
                String phone = parts[4];
                String inDate = parts[5];
                String outDate = parts[6];
                String status = parts[7];

            
                String message = String.format(
                        "=== 예약 상세 정보 ===\n\n" +
                        "예약 번호: %s\n" +
                        "예약자 명: %s\n" +
                        "전화 번호: %s\n" +
                        "객실 번호: %s호\n" +
                        "체크인 날짜: %s\n" +
                        "체크아웃 날짜: %s\n" +
                        "현재 상태: %s",
                        id, name, phone, room, inDate, outDate, status
                );

                JOptionPane.showMessageDialog(this, message, "예약 상세 조회", JOptionPane.INFORMATION_MESSAGE);

            } else if (response != null && response.startsWith("FAIL|")) {
                String msg = response.substring("FAIL|".length());
                JOptionPane.showMessageDialog(this, "조회 실패: " + msg);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "서버와 연결할 수 없습니다.");
        }
    }

    // 온라인 체크인 요청 로직
    private void requestOnlineCheckIn() {
        int confirm = JOptionPane.showConfirmDialog(this, 
                "온라인 체크인을 진행하시겠습니까?\n(입실 예정일 당일 오후 3시부터 가능합니다.)", 
                "체크인 요청", JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) return;

        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            String command = "GUEST_CHECK_IN|" + bookingId;
            String response = conn.sendAndReceive(command);

            if (response != null && response.startsWith("OK|")) {
                JOptionPane.showMessageDialog(this, "체크인이 완료되었습니다!\n객실로 입실해 주세요.\n즐거운 시간 되세요.");
            } else if (response != null && response.startsWith("FAIL|")) {
                String msg = response.substring("FAIL|".length());
                JOptionPane.showMessageDialog(this, "체크인 실패: " + msg);
            } else {
                JOptionPane.showMessageDialog(this, "알 수 없는 응답: " + response);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "서버와 연결할 수 없습니다.");
        }
    }
    
    // 청구 내역 및 예상 결제 금액 조회
    private void requestBill() {
        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            String command = "GUEST_GET_BILL|" + bookingId;
            String response = conn.sendAndReceive(command);

            if (response != null && response.startsWith("OK|")) {
                // 응답 형식: OK|객실료|식음료비|총합
                String[] parts = response.split("\\|");
                if (parts.length < 4) return;

                long roomCost = Long.parseLong(parts[1]);
                long fnbCost = Long.parseLong(parts[2]);
                long totalCost = Long.parseLong(parts[3]);

                DecimalFormat df = new DecimalFormat("#,###"); // 금액에 콤마 찍기

                String message = String.format(
                        "=== 예상 결제 금액 (Bill) ===\n\n" +
                        "1. 객실 숙박비: %s원\n" +
                        "2. 식음료/룸서비스: %s원\n" +
                        "-------------------------------\n" +
                        "총 합계: %s원",
                        df.format(roomCost), df.format(fnbCost), df.format(totalCost)
                );

                JOptionPane.showMessageDialog(this, message, "청구 내역 조회", JOptionPane.INFORMATION_MESSAGE);

            } else if (response != null && response.startsWith("FAIL|")) {
                JOptionPane.showMessageDialog(this, "조회 실패: " + response.substring(5));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "서버 연결 실패");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "금액 데이터 오류");
        }
    }
    
    private void openFnBMenu() {
        // bookingId를 넘겨주며 주문 창을 엽니다.
        new FnBDialog(this, bookingId).setVisible(true);
    }
}