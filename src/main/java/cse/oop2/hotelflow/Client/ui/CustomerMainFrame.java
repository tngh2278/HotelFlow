package cse.oop2.hotelflow.Client.ui;

import cse.oop2.hotelflow.Common.model.UserRole;
import cse.oop2.hotelflow.Client.net.ClientConnection;

import javax.swing.*;

import java.awt.*;
import java.io.IOException;

import cse.oop2.hotelflow.Client.Guest.FeedbackDialog;

// 고객 전용 메인 화면
// - 객실 조회
// - 예약 관리
// - 내 예약 / 청구 / 피드백
public class CustomerMainFrame extends JFrame {

    private final UserRole role;

    private RoomPanel roomPanel;
    private ReservationPanel reservationPanel;

    public CustomerMainFrame(UserRole role) {
        this.role = role;

        setTitle("HotelFlow - 고객 화면");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        initComponents();
    }

    // 기본 생성자
    public CustomerMainFrame() {
        this(UserRole.CUSTOMER);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 객실 목록 확인 패널
        roomPanel = new RoomPanel();

        // 예약 관리 패널 (고객 모드: 체크인/체크아웃 버튼 숨김)
        reservationPanel = new ReservationPanel(roomPanel, UserRole.CUSTOMER);

        // 탭 구성
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("객실 조회", roomPanel);
        tabs.addTab("예약 관리", reservationPanel);

        //고객용 룸서비스 탭 추가
        CustomerRoomServicePanel roomServicePanel = new CustomerRoomServicePanel();
        tabs.addTab("룸서비스", roomServicePanel);

        //  내 예약 / 청구 / 피드백 탭 생성
        JPanel myPagePanel = createMyPagePanel();
        tabs.addTab("내 예약 / 청구 / 피드백", myPagePanel);

        add(tabs, BorderLayout.CENTER);
    }

    // 로그인 직후 초기 데이터 로딩용 메서드
    public void loadInitialData() {
        if (roomPanel != null) {
            roomPanel.loadRooms();
        }
        if (reservationPanel != null) {
            reservationPanel.loadReservations();
        }
    }

    /**
     * 고객이 예약번호로
     * - 예약 상세 조회
     * - 온라인 체크인
     * - 청구 내역(객실+룸서비스) 조회
     * - 체크아웃 후 피드백 작성하는 패널 */
    private JPanel createMyPagePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // 상단: 예약번호 입력 + 버튼들
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel label = new JLabel("예약번호:");
        JTextField bookingIdField = new JTextField(15);

        JButton detailButton = new JButton("예약 상세 조회");
        JButton checkInButton = new JButton("온라인 체크인");
        JButton billButton = new JButton("청구 내역 조회");
        JButton feedbackButton = new JButton("피드백 작성");

        topPanel.add(label);
        topPanel.add(bookingIdField);
        topPanel.add(detailButton);
        topPanel.add(checkInButton);
        topPanel.add(billButton);
        topPanel.add(feedbackButton);

        panel.add(topPanel, BorderLayout.NORTH);

        // 중앙: 안내 텍스트
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setText(
                "※ 사용 방법\n" +
                "1) '예약 관리' 탭에서 예약을 생성하고, 발급된 예약번호를 기억해 두세요.\n" +
                "2) 위 입력칸에 예약번호를 적고 아래 버튼들을 이용해 기능을 이용할 수 있습니다.\n" +
                "   - 예약 상세 조회: 예약 정보 확인\n" +
                "   - 온라인 체크인: 미리 체크인 처리\n" +
                "   - 청구 내역 조회: 객실 요금 + 룸서비스 예상 금액 확인\n" +
                "   - 피드백 작성: 체크아웃 완료된 예약에 한해서 의견 남기기\n"
        );

        panel.add(new JScrollPane(infoArea), BorderLayout.CENTER);

        // 버튼 동작 구현 

        // 1) 예약 상세 조회 (GUEST_GET_DETAIL)
        detailButton.addActionListener(e -> {
            String bookingId = bookingIdField.getText().trim();
            if (bookingId.isEmpty()) {
                JOptionPane.showMessageDialog(CustomerMainFrame.this, "예약번호를 먼저 입력하세요.");
                return;
            }

            try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
                String cmd = "GUEST_GET_DETAIL|" + bookingId;
                String response = conn.sendAndReceive(cmd);

                if (response == null) {
                    JOptionPane.showMessageDialog(CustomerMainFrame.this, "서버 응답이 없습니다.");
                    return;
                }

                if (response.startsWith("OK|")) {
                    // OK|id|room|name|phone|checkIn|checkOut|status
                    String[] parts = response.split("\\|");
                    if (parts.length >= 8) {
                        String msg = String.format(
                                "예약번호: %s\n객실: %s\n고객명: %s\n전화: %s\n체크인: %s\n체크아웃: %s\n상태: %s",
                                parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]
                        );
                        JOptionPane.showMessageDialog(CustomerMainFrame.this, msg,
                                "예약 상세", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(CustomerMainFrame.this,
                                "응답 형식이 올바르지 않습니다: " + response);
                    }
                } else if (response.startsWith("FAIL|")) {
                    String msg = response.substring("FAIL|".length());
                    JOptionPane.showMessageDialog(CustomerMainFrame.this, msg);
                } else {
                    JOptionPane.showMessageDialog(CustomerMainFrame.this,
                            "알 수 없는 응답: " + response);
                }

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(CustomerMainFrame.this,
                        "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인해주세요.");
            }
        });

        // 2) 온라인 체크인 (GUEST_CHECK_IN)
        checkInButton.addActionListener(e -> {
            String bookingId = bookingIdField.getText().trim();
            if (bookingId.isEmpty()) {
                JOptionPane.showMessageDialog(CustomerMainFrame.this, "예약번호를 먼저 입력하세요.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    CustomerMainFrame.this,
                    "해당 예약으로 온라인 체크인 하시겠습니까?\n예약번호: " + bookingId,
                    "온라인 체크인 확인",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
                String cmd = "GUEST_CHECK_IN|" + bookingId;
                String response = conn.sendAndReceive(cmd);

                if (response == null) {
                    JOptionPane.showMessageDialog(CustomerMainFrame.this, "서버 응답이 없습니다.");
                    return;
                }

                if (response.startsWith("OK|")) {
                    String msg = response.substring("OK|".length());
                    JOptionPane.showMessageDialog(CustomerMainFrame.this,
                            msg.isEmpty() ? "온라인 체크인 완료" : msg);
                } else if (response.startsWith("FAIL|")) {
                    String msg = response.substring("FAIL|".length());
                    JOptionPane.showMessageDialog(CustomerMainFrame.this,
                            "체크인 실패: " + msg);
                } else {
                    JOptionPane.showMessageDialog(CustomerMainFrame.this,
                            "알 수 없는 응답: " + response);
                }

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(CustomerMainFrame.this,
                        "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인해주세요.");
            }
        });

        // 3) 청구 내역 조회 (GUEST_GET_BILL)
        billButton.addActionListener(e -> {
            String bookingId = bookingIdField.getText().trim();
            if (bookingId.isEmpty()) {
                JOptionPane.showMessageDialog(CustomerMainFrame.this, "예약번호를 먼저 입력하세요.");
                return;
            }

            try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
                String cmd = "GUEST_GET_BILL|" + bookingId;
                String response = conn.sendAndReceive(cmd);

                if (response == null) {
                    JOptionPane.showMessageDialog(CustomerMainFrame.this, "서버 응답이 없습니다.");
                    return;
                }

                if (response.startsWith("OK|")) {
                    // OK|roomCost|fnbCost|total
                    String[] parts = response.split("\\|");
                    if (parts.length >= 4) {
                        long roomCost = Long.parseLong(parts[1]);
                        long fnbCost = Long.parseLong(parts[2]);
                        long total = Long.parseLong(parts[3]);

                        String msg = String.format(
                                "객실 요금: %,d원\n" +
                                "룸서비스(식음료): %,d원\n" +
                                "------------------------\n" +
                                "예상 결제 금액: %,d원",
                                roomCost, fnbCost, total
                        );
                        JOptionPane.showMessageDialog(CustomerMainFrame.this,
                                msg, "청구 내역", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(CustomerMainFrame.this,
                                "응답 형식이 올바르지 않습니다: " + response);
                    }
                } else if (response.startsWith("FAIL|")) {
                    String msg = response.substring("FAIL|".length());
                    JOptionPane.showMessageDialog(CustomerMainFrame.this,
                            "조회 실패: " + msg);
                } else {
                    JOptionPane.showMessageDialog(CustomerMainFrame.this,
                            "알 수 없는 응답: " + response);
                }

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(CustomerMainFrame.this,
                        "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인해주세요.");
            }
        });

        // 4) 피드백 작성 (FeedbackDialog 사용, GUEST_SEND_FEEDBACK는 FeedbackDialog 안에서 전송)
        feedbackButton.addActionListener(e -> {
            String bookingId = bookingIdField.getText().trim();
            if (bookingId.isEmpty()) {
                JOptionPane.showMessageDialog(CustomerMainFrame.this, "예약번호를 먼저 입력하세요.");
                return;
            }

            // 체크아웃 전이면 서버가 FAIL|체크아웃 완료된 예약에만... 메시지를 돌려줌
            FeedbackDialog dialog = new FeedbackDialog(CustomerMainFrame.this, bookingId);
            dialog.setVisible(true);
        });

        return panel;
    }
}
