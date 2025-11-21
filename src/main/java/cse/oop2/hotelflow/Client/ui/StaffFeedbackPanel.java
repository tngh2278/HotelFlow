package cse.oop2.hotelflow.Client.ui;

import cse.oop2.hotelflow.Client.net.ClientConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;

// ★ [변경] JFrame -> JPanel 상속
public class StaffFeedbackPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public StaffFeedbackPanel() {
        setLayout(new BorderLayout(10, 10));
        initComponents();
        loadFeedbackData(); // 패널이 생성될 때 데이터 로드
    }

    private void initComponents() {
        // 상단 제목
        JLabel title = new JLabel("투숙객 피드백 목록", SwingConstants.CENTER);
        title.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // 중앙 테이블
        String[] cols = {"예약 번호", "내용", "작성 일시"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 하단 버튼 (닫기 버튼 삭제, 새로고침만 유지)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("새로고침");
        
        btnRefresh.addActionListener(e -> loadFeedbackData());
        
        btnPanel.add(btnRefresh);
        add(btnPanel, BorderLayout.SOUTH);
    }

    // 데이터 로드 메서드 (public으로 열어둠 -> MainFrame에서 호출 가능)
    public void loadFeedbackData() {
        tableModel.setRowCount(0);

        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            String response = conn.sendAndReceive("GET_ALL_FEEDBACK");

            if (response != null && response.startsWith("ALL_FEEDBACK|")) {
                String dataPart = response.substring("ALL_FEEDBACK|".length());
                if (dataPart.isEmpty()) return;

                String[] rows = dataPart.split(";");
                for (String row : rows) {
                    String[] cols = row.split(",");
                    if (cols.length >= 3) {
                        tableModel.addRow(cols);
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "피드백 로드 실패");
        }
    }
}