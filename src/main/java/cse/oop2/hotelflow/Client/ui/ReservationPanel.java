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

    private final JTextField roomField;
    private final JTextField nameField;
    private final JTextField phoneField;
    private final JTextField checkInField;
    private final JTextField checkOutField;

    public ReservationPanel(RoomPanel roomPanel1) {
        this.roomPanel = roomPanel;
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
        String[] cols = {"예약번호", "객실", "고객명", "전화", "체크인", "체크아웃", "상태"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

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
        buttonPanel.add(refreshButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(checkInButton);
        buttonPanel.add(checkOutButton);

        add(formPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    //예약 생성 메서드
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
                JOptionPane.showMessageDialog(this, "예약이 등록되었습니다.\n예약번호: " + id);
                loadReservations();
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

                String id = parts[0].trim();
                String room = parts[1].trim();
                String name = parts[2].trim();
                String phone = parts[3].trim();
                String checkIn = parts[4].trim();
                String checkOut = parts[5].trim();
                String status = parts[6].trim();

                tableModel.addRow(new Object[]{
                        id, room, name, phone, checkIn, checkOut, status
                });
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인하세요.");
        }
    }
    // 예약 취소 메서드
    private void cancelReservation() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "취소할 예약을 먼저 선택하세요.");
            return;
        }

        String reservationId = (String) tableModel.getValueAt(row, 0); // 0번 컬럼: 예약번호

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "선택한 예약을 취소하시겠습니까?\n예약번호: " + reservationId,
                "예약 취소 확인",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            String cmd = "CANCEL_RESERVATION|" + reservationId;
            String response = conn.sendAndReceive(cmd);

            if (response != null && response.startsWith("OK|")) {
                JOptionPane.showMessageDialog(this, "예약이 취소되었습니다.");
                loadReservations(); // 목록 다시 불러오기
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
            JOptionPane.YES_NO_OPTION
    );
    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

    try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
        String cmd = "CHECK_IN|" + reservationId;
        String response = conn.sendAndReceive(cmd);

        if (response != null && response.startsWith("OK|")) {
            JOptionPane.showMessageDialog(this, "체크인이 완료되었습니다.");
            loadReservations();      // 예약 목록 새로고침
            if (roomPanel != null) {
                roomPanel.loadRooms(); // ✅ 객실 현황도 같이 새로고침
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
            JOptionPane.YES_NO_OPTION
    );
    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

    try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
        String cmd = "CHECK_OUT|" + reservationId;
        String response = conn.sendAndReceive(cmd);

        if (response != null && response.startsWith("OK|")) {
            JOptionPane.showMessageDialog(this, "체크아웃이 완료되었습니다.");
            loadReservations();      // 예약 목록 새로고침
            if (roomPanel != null) {
                roomPanel.loadRooms(); // ✅ 객실 현황도 같이 새로고침
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
