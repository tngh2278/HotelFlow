package cse.oop2.hotelflow.Client.ui;

import cse.oop2.hotelflow.Client.net.ClientConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;

public class ReservationPanel extends JPanel {

    private final DefaultTableModel tableModel;
    private final JTable table;
    private RoomPanel roomPanel;
    private boolean isGuestMode = false; // ★ 게스트 모드 여부

    private final JTextField roomField;
    private final JTextField nameField;
    private final JTextField phoneField;
    private final JTextField checkInField;
    private final JTextField checkOutField;
    
    // ★ [중요] 관리자용 생성자 (기존 코드와 호환성 유지)
    public ReservationPanel(RoomPanel roomPanel) {
        this(roomPanel, false); // 기본값은 관리자 모드
    }

    // ★ [중요] 게스트 모드 지원 생성자
    public ReservationPanel(RoomPanel roomPanel, boolean isGuestMode) {
        this.roomPanel = roomPanel;
        this.isGuestMode = isGuestMode;
        
        setLayout(new BorderLayout(10, 10));

        // 상단: 예약 입력 폼
        JPanel formPanel = new JPanel(new GridLayout(2, 5, 5, 5));
        roomField = new JTextField();
        nameField = new JTextField();
        phoneField = new JTextField();
        checkInField = new JTextField("2025-01-01");
        checkOutField = new JTextField("2025-01-02");

        formPanel.add(new JLabel("객실 번호"));
        formPanel.add(new JLabel("고객 이름"));
        formPanel.add(new JLabel("전화번호"));
        formPanel.add(new JLabel("체크인(yyyy-MM-dd)"));
        formPanel.add(new JLabel("체크아웃(yyyy-MM-dd)"));

        formPanel.add(roomField);
        formPanel.add(nameField);
        formPanel.add(phoneField);
        formPanel.add(checkInField);
        formPanel.add(checkOutField);

        // 중앙: 예약 목록 테이블 (게스트 모드에선 안 보이거나 숨김)
        String[] cols = {"예약번호", "객실", "고객명", "전화", "체크인", "체크아웃", "상태"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // ★ 게스트 모드일 경우 테이블 대신 안내 문구 표시
        if (isGuestMode) {
            scrollPane.setVisible(false); 
            JLabel guestLabel = new JLabel("<html><center><h2>비회원 객실 예약</h2><br>"
                    + "원하시는 객실 번호와 정보를 입력 후 '예약 등록'을 눌러주세요.<br>"
                    + "예약 후 발급되는 <b>예약번호</b>를 꼭 기억해주세요!</center></html>", SwingConstants.CENTER);
            add(guestLabel, BorderLayout.CENTER);
        } else {
            add(scrollPane, BorderLayout.CENTER);
        }

        // 하단: 버튼들
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton createButton = new JButton("예약 등록");
        JButton refreshButton = new JButton("새로고침");
        JButton cancelButton = new JButton("예약 취소");
        JButton checkInButton = new JButton("체크인");
        JButton checkOutButton = new JButton("체크아웃");

        createButton.addActionListener(e -> createReservation());
        refreshButton.addActionListener(e -> loadReservations());
        cancelButton.addActionListener(e -> cancelReservation());
        checkInButton.addActionListener(e -> checkIn());
        checkOutButton.addActionListener(e -> checkOut());
        
        buttonPanel.add(createButton);

        // ★ 게스트 모드면 관리자용 버튼(새로고침, 취소, 체크인/아웃) 숨김
        if (!isGuestMode) {
            buttonPanel.add(refreshButton);
            buttonPanel.add(cancelButton);
            buttonPanel.add(checkInButton);
            buttonPanel.add(checkOutButton);
        }

        add(formPanel, BorderLayout.NORTH);
        // ScrollPane add는 위쪽 if문에서 처리함
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 관리자 모드라면 시작하자마자 데이터 로드
        if (!isGuestMode) {
            loadReservations();
        }
    }

    // 예약 생성 메서드
    private void createReservation() {
        String roomText = roomField.getText().trim();
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String checkIn = checkInField.getText().trim();
        String checkOut = checkOutField.getText().trim();

        if (roomText.isEmpty() || name.isEmpty() || phone.isEmpty()
                || checkIn.isEmpty() || checkOut.isEmpty()) {
            JOptionPane.showMessageDialog(this, "모든 필드를 입력하세요.");
            return;
        }

        int roomNum;
        try {
            roomNum = Integer.parseInt(roomText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "객실 번호는 숫자여야 합니다.");
            return;
        }

        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            String cmd = String.format("CREATE_RESERVATION|%d|%s|%s|%s|%s",
                    roomNum, name, phone, checkIn, checkOut);
            String response = conn.sendAndReceive(cmd);

            if (response != null && response.startsWith("OK|")) {
                String id = response.substring("OK|".length());
                JOptionPane.showMessageDialog(this, "예약이 성공적으로 등록되었습니다.\n[예약번호: " + id + "]");
                
                // ★ 게스트 모드라면 목록 갱신 없이 필드만 비움
                if (isGuestMode) {
                    roomField.setText("");
                    nameField.setText("");
                    phoneField.setText("");
                    // 날짜는 유지하거나 초기화
                } else {
                    loadReservations();
                }
                
            } else if (response != null && response.startsWith("FAIL|")) {
                String msg = response.substring("FAIL|".length());
                JOptionPane.showMessageDialog(this, "예약 실패: " + msg);
            } else {
                JOptionPane.showMessageDialog(this, "알 수 없는 응답: " + response);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인하세요.");
        }
    }

    // 예약 목록 불러오기 메서드
    public void loadReservations() {
        // ★ 게스트 모드에선 전체 목록 조회를 막음
        if (isGuestMode) return; 

        tableModel.setRowCount(0);

        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            String response = conn.sendAndReceive("GET_RESERVATIONS");

            if (response == null || !response.startsWith("RESERVATIONS|")) {
                return;
            }

            String dataPart = response.substring("RESERVATIONS|".length());
            if (dataPart.isEmpty()) return;

            String[] tokens = dataPart.split(";");
            for (String token : tokens) {
                String[] parts = token.split(",");
                if (parts.length < 7) continue;

                // 데이터 파싱 및 테이블 추가...
                tableModel.addRow(parts);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "서버 연결 실패");
        }
    }

    // 예약 취소, 체크인, 체크아웃 메서드들은 
    // 버튼이 숨겨져 있어서 게스트가 호출할 수 없으므로 기존 로직 유지해도 안전함
    private void cancelReservation() { /* 기존 코드 유지 */ }
    private void checkIn() { /* 기존 코드 유지 */ }
    private void checkOut() { /* 기존 코드 유지 */ }
}