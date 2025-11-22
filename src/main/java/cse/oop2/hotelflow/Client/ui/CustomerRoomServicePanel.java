package cse.oop2.hotelflow.Client.ui;

import cse.oop2.hotelflow.Client.net.ClientConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;

// 고객용 룸서비스 주문 패널
// - 예약번호로 객실 찾기
// - 메뉴 주문
// - 해당 객실의 룸서비스 주문 내역 조회
public class CustomerRoomServicePanel extends JPanel {

    private JTextField bookingIdField;
    private JComboBox<String> menuCombo;

    private JTable ordersTable;
    private DefaultTableModel ordersModel;

    public CustomerRoomServicePanel() {
        setLayout(new BorderLayout(10, 10));
        initComponents();
    }

    private void initComponents() {
        // 상단: 예약번호 + 메뉴 선택
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        //  예약번호 입력
        JPanel bookingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bookingPanel.add(new JLabel("예약번호:"));
        bookingIdField = new JTextField(15);
        bookingPanel.add(bookingIdField);

        //  메뉴 선택
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        menuPanel.add(new JLabel("메뉴 선택:"));

        String[] menus = {
                "한식 정식 (15,000원)",
                "클럽 샌드위치 (12,000원)",
                "시저 샐러드 (9,000원)",
                "콜라 (3,000원)",
                "생수 (2,000원)"
        };
        menuCombo = new JComboBox<>(menus);
        menuPanel.add(menuCombo);

        topPanel.add(bookingPanel);
        topPanel.add(menuPanel);

        add(topPanel, BorderLayout.NORTH);

        // 중앙: 룸서비스 주문 내역 테이블
        String[] cols = { "주문번호", "객실", "내용", "상태", "주문시간" };
        ordersModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ordersTable = new JTable(ordersModel);
        JScrollPane scroll = new JScrollPane(ordersTable);
        scroll.setBorder(BorderFactory.createTitledBorder("룸서비스 주문 내역"));

        add(scroll, BorderLayout.CENTER);

        // 하단: 버튼
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("주문 내역 새로고침");
        JButton orderButton = new JButton("룸서비스 주문");

        refreshButton.addActionListener(e -> loadOrdersForCurrentBooking());
        orderButton.addActionListener(e -> sendRoomServiceOrder());

        buttonPanel.add(refreshButton);
        buttonPanel.add(orderButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * 예약번호로 객실 번호를 조회하는 헬퍼 메서드.
     * 성공하면 roomNum(int)을 리턴, 실패하면 예외 던짐.
     */
    private int resolveRoomNumByBookingId(ClientConnection conn, String bookingId) throws IOException {
        String detailCmd = "GUEST_GET_DETAIL|" + bookingId;
        String detailResp = conn.sendAndReceive(detailCmd);

        if (detailResp == null) {
            throw new IOException("서버 응답이 없습니다.");
        }

        if (!detailResp.startsWith("OK|")) {
            if (detailResp.startsWith("FAIL|")) {
                String msg = detailResp.substring("FAIL|".length());
                throw new IOException("예약 조회 실패: " + msg);
            } else {
                throw new IOException("알 수 없는 응답: " + detailResp);
            }
        }

        // OK|id|room|name|phone|checkIn|checkOut|status
        String[] parts = detailResp.split("\\|");
        if (parts.length < 3) {
            throw new IOException("예약 상세 응답 형식 오류: " + detailResp);
        }

        String roomNumStr = parts[2].trim();
        try {
            return Integer.parseInt(roomNumStr);
        } catch (NumberFormatException ex) {
            throw new IOException("객실 번호 형식이 올바르지 않습니다: " + roomNumStr);
        }
    }

    // 룸서비스 주문 전송
    private void sendRoomServiceOrder() {
        String bookingId = bookingIdField.getText().trim();
        if (bookingId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "예약번호를 먼저 입력하세요.");
            return;
        }

        String menu = (String) menuCombo.getSelectedItem();
        if (menu == null || menu.isEmpty()) {
            JOptionPane.showMessageDialog(this, "메뉴를 선택하세요.");
            return;
        }

        // 메뉴명 그대로 사용
        String description = menu;

        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            // 예약번호로 객실 번호 조회
            int roomNum = resolveRoomNumByBookingId(conn, bookingId);

            // 룸서비스 주문 생성 요청
            String orderCmd = "CREATE_ROOM_SERVICE_ORDER|" + roomNum + "|" + description.replace("\n", " ");
            String orderResp = conn.sendAndReceive(orderCmd);

            if (orderResp == null) {
                JOptionPane.showMessageDialog(this, "서버 응답이 없습니다.(주문)");
                return;
            }

            if (orderResp.startsWith("OK|")) {
                String orderId = orderResp.substring("OK|".length());
                JOptionPane.showMessageDialog(this,
                        "룸서비스 주문이 접수되었습니다.\n주문번호: " + orderId,
                        "주문 완료",
                        JOptionPane.INFORMATION_MESSAGE);

                // 주문 내역 갱신
                loadOrdersForCurrentBooking();

            } else if (orderResp.startsWith("FAIL|")) {
                String msg = orderResp.substring("FAIL|".length());
                JOptionPane.showMessageDialog(this, "주문 실패: " + msg);
            } else {
                JOptionPane.showMessageDialog(this, "알 수 없는 응답: " + orderResp);
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "오류",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // 현재 입력된 예약번호 기준으로 룸서비스 주문 내역 로딩
    private void loadOrdersForCurrentBooking() {
        String bookingId = bookingIdField.getText().trim();
        if (bookingId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "예약번호를 먼저 입력하세요.");
            return;
        }

        ordersModel.setRowCount(0); // 기존 데이터 초기화

        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            // 예약번호로 객실 번호 조회
            int roomNum = resolveRoomNumByBookingId(conn, bookingId);

            // 전체 룸서비스 주문 목록 요청
            String resp = conn.sendAndReceive("GET_ROOM_SERVICE_ORDERS");

            if (resp == null) {
                JOptionPane.showMessageDialog(this, "서버 응답이 없습니다.");
                return;
            }

            if (!resp.startsWith("ROOM_SERVICE_ORDERS|")) {
                JOptionPane.showMessageDialog(this, "응답 형식이 올바르지 않습니다: " + resp);
                return;
            }

            String dataPart = resp.substring("ROOM_SERVICE_ORDERS|".length());
            if (dataPart.isEmpty()) {
                return; // 주문 내역 없음
            }

            String[] tokens = dataPart.split(";");
            for (String token : tokens) {
                String[] parts = token.split(",");
                if (parts.length < 5) continue;

                String orderId = parts[0].trim();
                int orderRoomNum;
                try {
                    orderRoomNum = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException ex) {
                    continue;
                }
                String desc = parts[2].trim();
                String status = parts[3].trim();
                String createdAt = parts[4].trim();

                // 같은 객실의 주문만 보여주기
                if (orderRoomNum == roomNum) {
                    ordersModel.addRow(new Object[]{
                            orderId, orderRoomNum, desc, status, createdAt
                    });
                }
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "오류",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
