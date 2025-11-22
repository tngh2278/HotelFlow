package cse.oop2.hotelflow.Client.ui;

import cse.oop2.hotelflow.Client.net.ClientConnection;
import cse.oop2.hotelflow.Client.Guest.FeedbackDialog;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

// 고객 전용 메인 화면
// - 객실 조회
// - 예약 관리 (내 예약만 보기)
// - 내 예약 / 청구 / 피드백 / 결제
public class CustomerMainFrame extends JFrame {

    // 로그인한 고객 정보
    private final String customerName;
    private final String customerPhone;

    private RoomPanel roomPanel;
    private ReservationPanel reservationPanel;

    // 마지막으로 청구 내역을 조회한 예약과 금액 저장용 변수
    private String lastBillingBookingId = null;
    private Long lastRoomCost = null;
    private Long lastFnbCost = null;
    private Long lastTotalCost = null;

    //  LoginFrame에서 사용하는 생성자
    public CustomerMainFrame(String customerName, String customerPhone) {
        this.customerName = customerName;
        this.customerPhone = customerPhone;

        setTitle("HotelFlow - 고객 화면");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 객실 목록 확인 패널
        roomPanel = new RoomPanel();

        // 고객 모드 예약 패널: 내 이름/전화에 해당하는 예약만 보이도록 필터 전달
        reservationPanel = new ReservationPanel(
                roomPanel,
                false, // isGuestMode 아님 (회원 고객)
                customerName,
                customerPhone
        );

        // 탭 구성
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("객실 조회", roomPanel);
        tabs.addTab("예약 관리", reservationPanel);

        // 고객용 룸서비스 탭 (생성자가 기본 생성자라면 이렇게)
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
     * 고객이 예약번호로 - 예약 상세 조회 - 온라인 체크인 - 청구 내역(객실+룸서비스) 조회 - 결제 - 체크아웃 후 피드백 작성 -
     * 결제 내역 조회 하는 UI
     */
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
        JButton payButton = new JButton("결제 하기");
        JButton paymentHistoryButton = new JButton("결제 내역 조회");

        topPanel.add(label);
        topPanel.add(bookingIdField);
        topPanel.add(detailButton);
        topPanel.add(checkInButton);
        topPanel.add(billButton);
        topPanel.add(feedbackButton);
        topPanel.add(payButton);
        topPanel.add(paymentHistoryButton);

        panel.add(topPanel, BorderLayout.NORTH);

        // 중앙: 안내 텍스트
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setText(
                "※ 사용 방법\n"
                + "1) '예약 관리' 탭에서 예약을 생성하고, 발급된 예약번호를 기억해 두세요.\n"
                + "2) 위 입력칸에 예약번호를 적고 아래 버튼들을 이용해 기능을 이용할 수 있습니다.\n"
                + "   - 예약 상세 조회: 예약 정보 확인\n"
                + "   - 온라인 체크인: 미리 체크인 처리\n"
                + "   - 청구 내역 조회: 객실 요금 + 룸서비스 예상 금액 확인\n"
                + "   - 피드백 작성: 체크아웃 완료된 예약에 한해서 의견 남기기\n"
        );

        panel.add(new JScrollPane(infoArea), BorderLayout.CENTER);

        // ===== 버튼 동작 구현 =====
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
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

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

                        // 마지막 청구 내역 저장
                        lastBillingBookingId = bookingId;
                        lastRoomCost = roomCost;
                        lastFnbCost = fnbCost;
                        lastTotalCost = total;

                        String msg = String.format(
                                "객실 요금: %,d원\n"
                                + "룸서비스(식음료): %,d원\n"
                                + "------------------------\n"
                                + "예상 결제 금액: %,d원",
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

        // 4) 피드백 작성 (FeedbackDialog 안에서 GUEST_SEND_FEEDBACK 전송)
        feedbackButton.addActionListener(e -> {
            String bookingId = bookingIdField.getText().trim();
            if (bookingId.isEmpty()) {
                JOptionPane.showMessageDialog(CustomerMainFrame.this, "예약번호를 먼저 입력하세요.");
                return;
            }

            FeedbackDialog dialog = new FeedbackDialog(CustomerMainFrame.this, bookingId);
            dialog.setVisible(true);
        });

        // 5) 결제하기 (PAY_BILL)
        payButton.addActionListener(e -> {
            String bookingId = bookingIdField.getText().trim();
            if (bookingId.isEmpty()) {
                JOptionPane.showMessageDialog(CustomerMainFrame.this, "예약번호를 먼저 입력하세요.");
                return;
            }

            // 먼저 청구 내역을 조회한 적이 있는지 확인
            if (lastBillingBookingId == null
                    || !bookingId.equals(lastBillingBookingId)
                    || lastRoomCost == null || lastFnbCost == null || lastTotalCost == null) {
                JOptionPane.showMessageDialog(CustomerMainFrame.this,
                        "먼저 '청구 내역 조회'를 통해 최신 금액을 확인해주세요.");
                return;
            }

            String[] methods = {"CARD", "CASH", "TRANSFER"}; // PaymentMethod enum과 이름 맞춰야 함
            String selected = (String) JOptionPane.showInputDialog(
                    CustomerMainFrame.this,
                    "결제 수단을 선택하세요.",
                    "결제 수단 선택",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    methods,
                    methods[0]
            );

            if (selected == null) {
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    CustomerMainFrame.this,
                    String.format("결제 금액: %,d원\n결제 수단: %s\n결제하시겠습니까?",
                            lastTotalCost, selected),
                    "결제 확인",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
                String cmd = String.format("PAY_BILL|%s|%d|%d|%d|%s",
                        lastBillingBookingId,
                        lastRoomCost,
                        lastFnbCost,
                        lastTotalCost,
                        selected
                );

                String response = conn.sendAndReceive(cmd);

                if (response == null) {
                    JOptionPane.showMessageDialog(CustomerMainFrame.this, "서버 응답이 없습니다.");
                    return;
                }

                if (response.startsWith("OK|")) {
                    String paymentId = response.substring("OK|".length());
                    JOptionPane.showMessageDialog(CustomerMainFrame.this,
                            "결제가 완료되었습니다.\n결제번호: " + paymentId);
                } else if (response.startsWith("FAIL|")) {
                    String msg = response.substring("FAIL|".length());
                    JOptionPane.showMessageDialog(CustomerMainFrame.this,
                            "결제 실패: " + msg);
                } else {
                    JOptionPane.showMessageDialog(CustomerMainFrame.this,
                            "알 수 없는 응답: " + response);
                }

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(CustomerMainFrame.this,
                        "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인해주세요.");
            }
        });

        // 6) 결제 내역 조회 (GET_PAYMENTS_BY_RESERVATION)
        paymentHistoryButton.addActionListener(e -> {
            String bookingId = bookingIdField.getText().trim();
            if (bookingId.isEmpty()) {
                JOptionPane.showMessageDialog(CustomerMainFrame.this, "예약번호를 먼저 입력하세요.");
                return;
            }

            try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
                String cmd = "GET_PAYMENTS_BY_RESERVATION|" + bookingId;
                String resp = conn.sendAndReceive(cmd);

                if (resp == null) {
                    JOptionPane.showMessageDialog(CustomerMainFrame.this, "서버 응답이 없습니다.");
                    return;
                }

                if (!resp.startsWith("PAYMENTS_BY_RES|")) {
                    if (resp.startsWith("FAIL|")) {
                        JOptionPane.showMessageDialog(CustomerMainFrame.this,
                                "결제 내역 조회 실패: " + resp.substring("FAIL|".length()));
                    } else {
                        JOptionPane.showMessageDialog(CustomerMainFrame.this,
                                "알 수 없는 응답: " + resp);
                    }
                    return;
                }

                String data = resp.substring("PAYMENTS_BY_RES|".length());
                if (data.isEmpty()) {
                    JOptionPane.showMessageDialog(CustomerMainFrame.this, "해당 예약에 대한 결제 내역이 없습니다.");
                    return;
                }

                StringBuilder sb = new StringBuilder();
                String[] rows = data.split(";");
                for (String row : rows) {
                    String[] parts = row.split(",");
                    if (parts.length < 6) {
                        continue;
                    }

                    String payId = parts[0].trim();
                    String roomAmt = parts[1].trim();
                    String fnbAmt = parts[2].trim();
                    String total = parts[3].trim();
                    String method = parts[4].trim();
                    String created = parts[5].trim();

                    sb.append("결제ID: ").append(payId).append("\n")
                            .append("객실요금: ").append(roomAmt).append("원\n")
                            .append("룸서비스: ").append(fnbAmt).append("원\n")
                            .append("총액: ").append(total).append("원\n")
                            .append("수단: ").append(method).append("\n")
                            .append("일시: ").append(created).append("\n")
                            .append("----------------------\n");
                }

                JTextArea area = new JTextArea(sb.toString());
                area.setEditable(false);
                JScrollPane scroll = new JScrollPane(area);
                scroll.setPreferredSize(new Dimension(400, 300));

                JOptionPane.showMessageDialog(CustomerMainFrame.this,
                        scroll, "결제 내역", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(CustomerMainFrame.this,
                        "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인하세요.");
            }
        });

        return panel;
    }
}
