package cse.oop2.hotelflow.Client.ui;

import cse.oop2.hotelflow.Client.net.ClientConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class SalesPanel extends JPanel {

    private JTextField fromField;
    private JTextField toField;

    private JLabel occupancyLabel;
    private JLabel roomSalesLabel;
    private JLabel fnbSalesLabel;
    private JLabel totalSalesLabel;

    private JTable paymentTable;
    private DefaultTableModel paymentModel;

    public SalesPanel() {
        setLayout(new BorderLayout(10, 10));
        initComponents();
    }

    private void initComponents() {
        // 상단: 기간 입력 + 조회 버튼
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("시작일 (yyyy-MM-dd):"));
        fromField = new JTextField(10);
        top.add(fromField);

        top.add(new JLabel("종료일 (yyyy-MM-dd):"));
        toField = new JTextField(10);
        top.add(toField);

        JButton searchBtn = new JButton("조회");
        searchBtn.addActionListener(e -> doSearch());
        top.add(searchBtn);

        add(top, BorderLayout.NORTH);

        // 중앙 위쪽: 요약 정보 (점유율 + 매출)
        JPanel summaryPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        occupancyLabel = new JLabel("객실 점유율: - %");
        roomSalesLabel = new JLabel("객실 매출: - 원");
        fnbSalesLabel = new JLabel("룸서비스 매출: - 원");
        totalSalesLabel = new JLabel("총 매출: - 원");

        summaryPanel.add(occupancyLabel);
        summaryPanel.add(roomSalesLabel);
        summaryPanel.add(fnbSalesLabel);
        summaryPanel.add(totalSalesLabel);

        // 중앙 아래쪽: 결제 내역 테이블
        String[] cols = {"결제ID", "예약ID", "객실금액", "룸서비스금액", "총액", "결제수단", "결제일시"};
        paymentModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        paymentTable = new JTable(paymentModel);
        JScrollPane tableScroll = new JScrollPane(paymentTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("결제 내역"));

        JPanel center = new JPanel(new BorderLayout(5, 5));
        center.add(summaryPanel, BorderLayout.NORTH);
        center.add(tableScroll, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
    }

    private void doSearch() {
        String from = fromField.getText().trim();
        String to = toField.getText().trim();

        if (from.isEmpty() || to.isEmpty()) {
            JOptionPane.showMessageDialog(this, "조회할 시작일과 종료일을 모두 입력하세요.");
            return;
        }

        LocalDate fromDate;
        LocalDate toDate;
        try {
            fromDate = LocalDate.parse(from);
            toDate = LocalDate.parse(to);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "날짜 형식이 잘못되었습니다. 예: 2025-01-01");
            return;
        }

        // 1) 객실 점유율 계산 (GET_ROOMS + GET_RESERVATIONS)
        calculateOccupancy(fromDate, toDate);

        // 2) 매출 합계 (GET_SALES)
        loadSalesSummary(from, to);

        // 3) 결제 내역 테이블 (GET_PAYMENTS)
        loadPaymentList(from, to);
    }

    private void calculateOccupancy(LocalDate fromDate, LocalDate toDate) {
        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            // 객실 수
            String roomsResp = conn.sendAndReceive("GET_ROOMS");
            int roomCount = 0;
            if (roomsResp != null && roomsResp.startsWith("ROOMS|")) {
                String data = roomsResp.substring("ROOMS|".length());
                if (!data.isEmpty()) {
                    roomCount = data.split(";").length;
                }
            }

            if (roomCount == 0) {
                occupancyLabel.setText("객실 점유율: (객실 정보 없음)");
                return;
            }

            // 예약 정보
            String resResp = conn.sendAndReceive("GET_RESERVATIONS");
            long occupiedNights = 0;

            if (resResp != null && resResp.startsWith("RESERVATIONS|")) {
                String data = resResp.substring("RESERVATIONS|".length());
                if (!data.isEmpty()) {
                    String[] tokens = data.split(";");
                    for (String token : tokens) {
                        String[] parts = token.split(",");
                        if (parts.length < 7) {
                            continue;
                        }

                        String checkInStr = parts[4].trim();
                        String checkOutStr = parts[5].trim();

                        LocalDate resIn;
                        LocalDate resOut;
                        try {
                            resIn = LocalDate.parse(checkInStr);
                            resOut = LocalDate.parse(checkOutStr);
                        } catch (Exception ex) {
                            continue;
                        }

                        // 예약 기간과 조회 기간의 겹치는 박수 계산
                        LocalDate start = resIn.isAfter(fromDate) ? resIn : fromDate;
                        LocalDate end = resOut.isBefore(toDate) ? resOut : toDate;

                        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
                        if (days > 0) {
                            occupiedNights += days;
                        }
                    }
                }
            }

            long totalNights = java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate) * roomCount;
            if (totalNights <= 0) {
                occupancyLabel.setText("객실 점유율: 기간이 올바르지 않습니다.");
                return;
            }

            double rate = (occupiedNights * 100.0) / totalNights;
            occupancyLabel.setText(String.format("객실 점유율: %.1f %%", rate));

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "서버 연결 실패(점유율 계산)", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSalesSummary(String from, String to) {
        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            String cmd = "GET_SALES|" + from + "|" + to;
            String resp = conn.sendAndReceive(cmd);

            if (resp == null) {
                JOptionPane.showMessageDialog(this, "서버 응답이 없습니다(GET_SALES).");
                return;
            }

            if (!resp.startsWith("OK|")) {
                if (resp.startsWith("FAIL|")) {
                    JOptionPane.showMessageDialog(this, "매출 조회 실패: " + resp.substring("FAIL|".length()));
                } else {
                    JOptionPane.showMessageDialog(this, "알 수 없는 응답: " + resp);
                }
                return;
            }

            String[] parts = resp.split("\\|");
            if (parts.length < 4) {
                JOptionPane.showMessageDialog(this, "응답 형식 오류: " + resp);
                return;
            }

            long roomSales = Long.parseLong(parts[1]);
            long fnbSales = Long.parseLong(parts[2]);
            long total = Long.parseLong(parts[3]);

            roomSalesLabel.setText(String.format("객실 매출: %,d 원", roomSales));
            fnbSalesLabel.setText(String.format("룸서비스 매출: %,d 원", fnbSales));
            totalSalesLabel.setText(String.format("총 매출: %,d 원", total));

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "서버 연결 실패(GET_SALES)", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPaymentList(String from, String to) {
        paymentModel.setRowCount(0);

        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            String cmd = "GET_PAYMENTS|" + from + "|" + to;
            String resp = conn.sendAndReceive(cmd);

            if (resp == null) {
                JOptionPane.showMessageDialog(this, "서버 응답이 없습니다(GET_PAYMENTS).");
                return;
            }

            if (!resp.startsWith("PAYMENTS|")) {
                if (resp.startsWith("FAIL|")) {
                    JOptionPane.showMessageDialog(this, "결제 내역 조회 실패: " + resp.substring("FAIL|".length()));
                } else {
                    JOptionPane.showMessageDialog(this, "알 수 없는 응답: " + resp);
                }
                return;
            }

            String data = resp.substring("PAYMENTS|".length());
            if (data.isEmpty()) {
                return;
            }

            String[] rows = data.split(";");
            for (String row : rows) {
                String[] parts = row.split(",");
                if (parts.length < 7) {
                    continue;
                }

                Object[] one = new Object[]{
                    parts[0].trim(), // 결제ID
                    parts[1].trim(), // 예약ID
                    parts[2].trim(), // 객실금액
                    parts[3].trim(), // 룸서비스금액
                    parts[4].trim(), // 총액
                    parts[5].trim(), // 결제수단
                    parts[6].trim() // 결제일시
                };
                paymentModel.addRow(one);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "서버 연결 실패(GET_PAYMENTS)", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}
