package cse.oop2.hotelflow.Client.ui;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import cse.oop2.hotelflow.Client.net.ClientConnection;

public class RoomPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;

    public RoomPanel() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        String[] columns = {"객실 번호", "상태", "수용 인원"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 조회 전용
            }
        };

        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        JButton refreshButton = new JButton("새로고침");
        refreshButton.addActionListener(e -> loadRooms());

        add(scrollPane, BorderLayout.CENTER);
        add(refreshButton, BorderLayout.SOUTH);
    }

    public void loadRooms() {
    // 화면 초기화
    tableModel.setRowCount(0);

    try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
        String response = conn.sendAndReceive("GET_ROOMS");

        if (response == null) {
            JOptionPane.showMessageDialog(this, "서버 응답이 없습니다.");
            return;
        }

        if (!response.startsWith("ROOMS|")) {
            JOptionPane.showMessageDialog(this, "객실 목록 응답 형식 오류: " + response);
            return;
        }

        String dataPart = response.substring("ROOMS|".length());
        if (dataPart.isEmpty()) {
            return; // 객실 정보가 없는 경우
        }

        String[] roomTokens = dataPart.split(";");
        for (String token : roomTokens) {
            String[] parts = token.split(",");
            if (parts.length < 3) continue;

            try {
                int roomNumber = Integer.parseInt(parts[0].trim());
                String statusCode = parts[1].trim();  // VACANT, OCCUPIED, CLEANING, MAINTENANCE 등
                int capacity = Integer.parseInt(parts[2].trim());

                // 🔽 영어 코드 → 한글로 변환
                String statusText;
                statusText = switch (statusCode) {
                    case "VACANT" -> "빈 방";
                    case "OCCUPIED" -> "사용 중";
                    case "CLEANING" -> "청소 중";
                    case "MAINTENANCE" -> "점검 중";
                    default -> statusCode;
                }; // 혹시 모르는 값이면 그대로

                tableModel.addRow(new Object[]{roomNumber, statusText, capacity});
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                //디버그용 예외처리 코드
            }
        }
    } catch (IOException ex) {
        JOptionPane.showMessageDialog(this,
                "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인하세요.");
        }
    }

    class UserRole {

        public UserRole() {
        }
    }
}