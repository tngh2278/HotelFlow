package cse.oop2.hotelflow.Server.service;

import cse.oop2.hotelflow.Common.model.Payment;
import cse.oop2.hotelflow.Common.model.PaymentMethod;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PaymentService {

    private final File file;

    public PaymentService(String filePath) {
        this.file = new File(filePath);
        ensureFileExists();
    }

    private void ensureFileExists() {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // CSV 전체 읽기
    public List<Payment> getAllPayments() {
        List<Payment> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split(",");
                if (parts.length < 7) continue;

                String id = parts[0].trim();
                String reservationId = parts[1].trim();
                int roomAmount = Integer.parseInt(parts[2].trim());
                int fnbAmount = Integer.parseInt(parts[3].trim());
                int totalAmount = Integer.parseInt(parts[4].trim());
                PaymentMethod method = PaymentMethod.valueOf(parts[5].trim());
                String paidAt = parts[6].trim();

                list.add(new Payment(id, reservationId, roomAmount, fnbAmount, totalAmount, method, paidAt));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    // 새 결제 ID 생성
    private String generatePaymentId() {
        List<Payment> all = getAllPayments();
        int next = all.size() + 1;
        return String.format("PAY-%04d", next);
    }

    // 결제 저장 (원자적 저장)
    public synchronized Payment savePayment(String reservationId,
                                            int roomAmount,
                                            int fnbAmount,
                                            PaymentMethod method) throws IOException {
        String id = generatePaymentId();
        int total = roomAmount + fnbAmount;
        String today = LocalDate.now().toString(); // yyyy-MM-dd

        Payment payment = new Payment(id, reservationId, roomAmount, fnbAmount, total, method, today);

        // 기존 데이터 + 신규 데이터로 temp 파일에 쓰고 교체 (원자적 저장)
        List<Payment> all = getAllPayments();
        all.add(payment);

        File temp = new File(file.getParentFile(), file.getName() + ".tmp");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
            for (Payment p : all) {
                bw.write(String.format("%s,%s,%d,%d,%d,%s,%s",
                        p.getId(),
                        p.getReservationId(),
                        p.getRoomAmount(),
                        p.getFnbAmount(),
                        p.getTotalAmount(),
                        p.getMethod().name(),
                        p.getPaidAt()));
                bw.newLine();
            }
        }

        if (!temp.renameTo(file)) {
            throw new IOException("payments.csv 저장 실패");
        }

        return payment;
    }

    // 기간별 매출 조회 (yyyy-MM-dd 문자열 기준)
    public List<Payment> getPaymentsByPeriod(String from, String to) {
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = LocalDate.parse(to);

        List<Payment> result = new ArrayList<>();

        for (Payment p : getAllPayments()) {
            LocalDate d = LocalDate.parse(p.getPaidAt());
            if ((d.isEqual(fromDate) || d.isAfter(fromDate)) &&
                (d.isEqual(toDate) || d.isBefore(toDate))) {
                result.add(p);
            }
        }
        return result;
    }
}
