package cse.oop2.hotelflow.Client.Guest;

import cse.oop2.hotelflow.Client.net.ClientConnection;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FnBDialog extends JDialog {
    private String bookingId;
    private int roomNum = -1; // 서버에서 조회 후 저장할 객실 번호
    
    private JComboBox<String> menuCombo;
    private JTextArea orderLogArea;
    private JLabel statusLabel;

    public FnBDialog(Frame owner, String bookingId) {
        super(owner, "레스토랑 & 룸서비스 주문", true); // 모달 창
        this.bookingId = bookingId;
        
        setSize(450, 500);
        setLocationRelativeTo(owner);
        
        initComponents();
        
        // 창이 열리자마자 방 번호 조회 & 주문 내역 로드
        fetchRoomNumberAndHistory();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // 1. 상단: 메뉴 선택 패널
        JPanel orderPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        orderPanel.setBorder(BorderFactory.createTitledBorder("메뉴 주문"));

        statusLabel = new JLabel("객실 정보 확인 중...");
        statusLabel.setForeground(Color.BLUE);
        orderPanel.add(statusLabel);

        orderPanel.add(new JLabel("주문하실 메뉴를 선택하세요:"));
        
        // 메뉴 목록
        String[] menus = {
            "한우 스테이크 (50,000원)", 
            "해산물 파스타 (25,000원)", 
            "치킨 & 감자튀김 (30,000원)", 
            "레드 와인 1 Bottle (70,000원)",
            "탄산음료 (5,000원)",
            "조식 뷔페 예약 (40,000원)"
        };
        menuCombo = new JComboBox<>(menus);
        orderPanel.add(menuCombo);

        JButton btnOrder = new JButton("주문하기 (Room Charge)");
        btnOrder.addActionListener(e -> sendOrder());
        orderPanel.add(btnOrder);

        // 2. 중앙: 주문 내역 로그
        orderLogArea = new JTextArea();
        orderLogArea.setEditable(false);
        orderLogArea.setText("주문 내역을 불러오는 중...\n");
        JScrollPane scrollPane = new JScrollPane(orderLogArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("이용 내역 (My Orders)"));

        // 3. 하단: 닫기 버튼
        JButton btnClose = new JButton("닫기");
        btnClose.addActionListener(e -> dispose());

        add(orderPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(btnClose, BorderLayout.SOUTH);
    }

    // [핵심 1] 예약 번호로 방 번호를 먼저 알아내야 함
    private void fetchRoomNumberAndHistory() {
        new Thread(() -> {
            try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
                // 1. 예약 상세 조회로 방 번호 얻기
                String cmdDetail = "GUEST_GET_DETAIL|" + bookingId;
                String resDetail = conn.sendAndReceive(cmdDetail);

                if (resDetail.startsWith("OK|")) {
                    String[] parts = resDetail.split("\\|");
                    // OK|ID|Room|Name|...
                    this.roomNum = Integer.parseInt(parts[2]); 
                    SwingUtilities.invokeLater(() -> 
                        statusLabel.setText("현재 객실: " + roomNum + "호 (주문 가능)")
                    );
                    
                    // 2. 방 번호를 알았으니 주문 내역 불러오기
                    loadOrderHistory(conn);
                    
                } else {
                    SwingUtilities.invokeLater(() -> 
                        statusLabel.setText("객실 정보를 찾을 수 없습니다.")
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // [핵심 2] 주문 전송 (기존 룸서비스 시스템과 연동)
    private void sendOrder() {
        if (roomNum == -1) {
            JOptionPane.showMessageDialog(this, "객실 정보를 불러오는 중입니다. 잠시 후 다시 시도해주세요.");
            return;
        }

        String selectedMenu = (String) menuCombo.getSelectedItem();
        if (selectedMenu == null) return;

        int confirm = JOptionPane.showConfirmDialog(this, 
                selectedMenu + "\n해당 메뉴를 주문하시겠습니까?\n비용은 객실(" + roomNum + "호)로 청구됩니다.", 
                "주문 확인", JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) return;

        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            // 기존 서버 프로토콜: CREATE_ROOM_SERVICE_ORDER|방번호|내용
            String command = "CREATE_ROOM_SERVICE_ORDER|" + roomNum + "|" + selectedMenu;
            String response = conn.sendAndReceive(command);

            if (response.startsWith("OK|")) {
                JOptionPane.showMessageDialog(this, "주문이 정상적으로 접수되었습니다.");
                // 주문 내역 새로고침
                loadOrderHistory(conn);
            } else {
                String msg = response.startsWith("FAIL|") ? response.substring(5) : response;
                JOptionPane.showMessageDialog(this, "주문 실패: " + msg);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "서버 연결 실패");
        }
    }

    // [핵심 3] 내 방의 주문 내역만 필터링해서 보여주기
    private void loadOrderHistory(ClientConnection conn) throws IOException {
        // 서버는 모든 주문을 다 줍니다. (GET_ROOM_SERVICE_ORDERS)
        // 클라이언트에서 '내 방 번호'인 것만 골라내야 합니다.
        String response = conn.sendAndReceive("GET_ROOM_SERVICE_ORDERS");
        
        StringBuilder myLog = new StringBuilder();
        
        if (response.startsWith("ROOM_SERVICE_ORDERS|")) {
            String dataPart = response.substring("ROOM_SERVICE_ORDERS|".length());
            if (!dataPart.isEmpty()) {
                String[] tokens = dataPart.split(";");
                
                for (String token : tokens) {
                    String[] parts = token.split(",");
                    // ID, Room, Desc, Status, Date
                    if (parts.length >= 5) {
                        int oRoom = Integer.parseInt(parts[1].trim());
                        
                        // 내 방 주문만 표시
                        if (oRoom == this.roomNum) {
                            String desc = parts[2];
                            String status = parts[3];
                            String date = parts[4];
                            
                            myLog.append(String.format("[%s] %s\n - 상태: %s (%s)\n\n", 
                                    date, desc, status, parts[0])); // parts[0] is ID
                        }
                    }
                }
            }
        }

        String finalLog = myLog.length() == 0 ? "주문 내역이 없습니다." : myLog.toString();
        SwingUtilities.invokeLater(() -> {
            orderLogArea.setText(finalLog);
            orderLogArea.setCaretPosition(0); // 스크롤 맨 위로
        });
    }
}