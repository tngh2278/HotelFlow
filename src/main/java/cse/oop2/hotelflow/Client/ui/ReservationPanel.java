package cse.oop2.hotelflow.Client.ui;

import cse.oop2.hotelflow.Client.net.ClientConnection;
import cse.oop2.hotelflow.Common.model.UserRole;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;

public class ReservationPanel extends JPanel {

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final RoomPanel roomPanel;
    private final boolean isGuestMode; //  비회원 모드 여부
    private final UserRole role;       // 로그인 사용자 역할 (ADMIN / STAFF / CUSTOMER / null)

    private final JTextField roomField;
    private final JTextField nameField;
    private final JTextField phoneField;
    private final JTextField checkInField;
    private final JTextField checkOutField;

    private JButton checkInButton;
    private JButton checkOutButton;
    private JButton refreshButton;
    private JButton cancelButton;

    //  로그인 사용자용 (직원/관리자/고객)
    public ReservationPanel(RoomPanel roomPanel, UserRole role) {
        this(roomPanel, false, role); // 게스트 모드 아님
    }

    //  비회원(게스트)용
    public ReservationPanel(RoomPanel roomPanel, boolean isGuestMode) {
        this(roomPanel, isGuestMode, null); // 게스트는 역할 없음
    }

    //  실제 필드 초기화용 공통 생성자
    private ReservationPanel(RoomPanel roomPanel, boolean isGuestMode, UserRole role) {
        this.roomPanel = roomPanel;
        this.isGuestMode = isGuestMode;
        this.role = role;

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

        // 중앙: 예약 목록 테이블
        String[] cols = { "예약번호", "객실", "고객명", "전화", "체크인", "체크아웃", "상태" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        if (isGuestMode) {
            scrollPane.setVisible(false);
            JLabel guestLabel = new JLabel(
                    "<html><center><h2>비회원 객실 예약</h2><br>"
                    + "원하시는 객실 번호와 정보를 입력 후 '예약 등록'을 눌러주세요.<br>"
                    + "예약 후 발급되는 <b>예약번호</b>를 꼭 기억해주세요!</center></html>",
                    SwingConstants.CENTER);
            add(guestLabel, BorderLayout.CENTER);
        } else {
            add(scrollPane, BorderLayout.CENTER);
        }

        // 하단: 버튼들
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton createButton = new JButton("예약 등록");
        refreshButton = new JButton("새로고침");
        cancelButton = new JButton("예약 취소");
        checkInButton = new JButton("체크인");
        checkOutButton = new JButton("체크아웃");

        createButton.addActionListener(e -> createReservation());
        refreshButton.addActionListener(e -> loadReservations());
        cancelButton.addActionListener(e -> cancelReservation());
        checkInButton.addActionListener(e -> checkIn());
        checkOutButton.addActionListener(e -> checkOut());

        buttonPanel.add(createButton);

        if (!isGuestMode) {
            // 로그인 사용자 (직원/관리자/고객)
            buttonPanel.add(refreshButton);
            buttonPanel.add(cancelButton);

            // 고객이면 체크인/체크아웃 버튼 숨김
            if (role == UserRole.CUSTOMER) {
                checkInButton.setVisible(false);
                checkOutButton.setVisible(false);
            } else {
                // 직원/관리자(또는 role 미사용) → 버튼 표시
                buttonPanel.add(checkInButton);
                buttonPanel.add(checkOutButton);
            }
        }
        // 게스트 모드면 refresh/cancel/check-in/out 버튼 숨김

        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        // 로그인 사용자 모드는 처음에 예약 목록 로드
        if (!isGuestMode) {
            loadReservations();
        }
    }


    // 예약 생성
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
                JOptionPane.showMessageDialog(this,
                        "예약이 성공적으로 등록되었습니다.\n[예약번호: " + id + "]");

                if (isGuestMode) {
                    roomField.setText("");
                    nameField.setText("");
                    phoneField.setText("");
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

    // 예약 목록
    public void loadReservations() {
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

                tableModel.addRow(parts);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "서버 연결 실패");
        }
    }

    // 예약 취소
    private void cancelReservation() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "취소할 예약을 먼저 선택하세요.");
            return;
        }

        String reservationId = (String) tableModel.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "선택한 예약을 취소하시겠습니까?\n예약번호: " + reservationId,
                "예약 취소 확인",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            String cmd = "CANCEL_RESERVATION|" + reservationId;
            String response = conn.sendAndReceive(cmd);

            if (response != null && response.startsWith("OK|")) {
                JOptionPane.showMessageDialog(this, "예약이 취소되었습니다.");
                loadReservations();
            } else if (response != null && response.startsWith("FAIL|")) {
                String msg = response.substring("FAIL|".length());
                JOptionPane.showMessageDialog(this, "예약 취소 실패: " + msg);
            } else {
                JOptionPane.showMessageDialog(this, "알 수 없는 응답: " + response);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인하세요.");
        }
    }

    // 체크인
    private void checkIn() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "체크인할 예약을 먼저 선택하세요.");
            return;
        }

        String reservationId = (String) tableModel.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "선택한 예약을 체크인하시겠습니까?\n예약번호: " + reservationId,
                "체크인 확인",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            String cmd = "CHECK_IN|" + reservationId;
            String response = conn.sendAndReceive(cmd);

            if (response != null && response.startsWith("OK|")) {
                JOptionPane.showMessageDialog(this, "체크인이 완료되었습니다.");
                loadReservations();
                if (roomPanel != null) {
                    roomPanel.loadRooms();
                }
            } else if (response != null && response.startsWith("FAIL|")) {
                String msg = response.substring("FAIL|".length());
                JOptionPane.showMessageDialog(this, "체크인 실패: " + msg);
            } else {
                JOptionPane.showMessageDialog(this, "알 수 없는 응답: " + response);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인하세요.");
        }
    }

    // 체크아웃
    private void checkOut() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "체크아웃할 예약을 먼저 선택하세요.");
            return;
        }

        String reservationId = (String) tableModel.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "선택한 예약을 체크아웃하시겠습니까?\n예약번호: " + reservationId,
                "체크아웃 확인",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            String cmd = "CHECK_OUT|" + reservationId;
            String response = conn.sendAndReceive(cmd);

            if (response != null && response.startsWith("OK|")) {
                JOptionPane.showMessageDialog(this, "체크아웃이 완료되었습니다.");
                loadReservations();
                if (roomPanel != null) {
                    roomPanel.loadRooms();
                }
            } else if (response != null && response.startsWith("FAIL|")) {
                String msg = response.substring("FAIL|".length());
                JOptionPane.showMessageDialog(this, "체크아웃 실패: " + msg);
            } else {
                JOptionPane.showMessageDialog(this, "알 수 없는 응답: " + response);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인하세요.");
        }
    }
}
