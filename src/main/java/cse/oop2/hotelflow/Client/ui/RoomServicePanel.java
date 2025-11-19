package cse.oop2.hotelflow.Client.ui;

import cse.oop2.hotelflow.Client.net.ClientConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;

public class RoomServicePanel extends JPanel {

    private final DefaultTableModel tableModel;
    private final JTable table;

    private final JTextField roomField;
    private final JTextField descriptionField;

    public RoomServicePanel() {
        setLayout(new BorderLayout(10, 10));

        // 상단: 요청 입력 폼
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        roomField = new JTextField();
        descriptionField = new JTextField();

        formPanel.add(new JLabel("객실 번호"));
        formPanel.add(roomField);
        formPanel.add(new JLabel("요청 내용"));
        formPanel.add(descriptionField);

        // 중앙: 룸서비스 요청 목록
        String[] cols = {"ID", "객실", "요청 내용", "상태", "요청 시각"};
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
        JButton requestButton = new JButton("룸서비스 요청");
        JButton refreshButton = new JButton("새로고침");
        JButton completeButton = new JButton("완료 처리");

        requestButton.addActionListener(e -> createOrder());
        refreshButton.addActionListener(e -> loadOrders());
        completeButton.addActionListener(e -> completeSelectedOrder());

        buttonPanel.add(requestButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(completeButton);

        add(formPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void createOrder() {
        String roomText = roomField.getText().trim();
        String description = descriptionField.getText().trim();

        if (roomText.isEmpty() || description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "객실 번호와 요청 내용을 입력하세요.");
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
            // description에 '|'가 들어가면 안 되므로 간단히 공백으로 치환
            String safeDesc = description.replace("|", " ");

            String cmd = "CREATE_ROOM_SERVICE_ORDER|" + roomNum + "|" + safeDesc;
            String response = conn.sendAndReceive(cmd);

            if (response != null && response.startsWith("OK|")) {
                String id = response.substring("OK|".length());
                JOptionPane.showMessageDialog(this, "룸서비스 요청이 등록되었습니다.\nID: " + id);
                loadOrders();
            } else if (response != null && response.startsWith("FAIL|")) {
                String msg = response.substring("FAIL|".length());
                JOptionPane.showMessageDialog(this, "요청 실패: " + msg);
            } else {
                JOptionPane.showMessageDialog(this, "알 수 없는 응답: " + response);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인하세요.");
        }
    }

    public void loadOrders() {
        tableModel.setRowCount(0);

        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            String response = conn.sendAndReceive("GET_ROOM_SERVICE_ORDERS");

            if (response == null || !response.startsWith("ROOM_SERVICE_ORDERS|")) {
                return;
            }

            String dataPart = response.substring("ROOM_SERVICE_ORDERS|".length());
            if (dataPart.isEmpty()) return;

            String[] tokens = dataPart.split(";");
            for (String token : tokens) {
                String[] parts = token.split(",", 5);
                if (parts.length < 5) continue;

                String id = parts[0].trim();
                String room = parts[1].trim();
                String desc = parts[2].trim();
                String status = parts[3].trim();
                String createdAt = parts[4].trim();

                tableModel.addRow(new Object[]{id, room, desc, status, createdAt});
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인하세요.");
        }
    }

    private void completeSelectedOrder() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "완료 처리할 요청을 선택하세요.");
            return;
        }

        String orderId = (String) tableModel.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "선택한 룸서비스 요청을 완료 처리하시겠습니까?\nID: " + orderId,
                "완료 처리 확인",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            String cmd = "COMPLETE_ROOM_SERVICE_ORDER|" + orderId;
            String response = conn.sendAndReceive(cmd);

            if (response != null && response.startsWith("OK|")) {
                JOptionPane.showMessageDialog(this, "완료 처리되었습니다.");
                loadOrders();
            } else if (response != null && response.startsWith("FAIL|")) {
                String msg = response.substring("FAIL|".length());
                JOptionPane.showMessageDialog(this, "완료 처리 실패: " + msg);
            } else {
                JOptionPane.showMessageDialog(this, "알 수 없는 응답: " + response);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인하세요.");
        }
    }
}
