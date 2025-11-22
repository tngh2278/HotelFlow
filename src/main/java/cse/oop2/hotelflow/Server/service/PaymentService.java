package cse.oop2.hotelflow.Server.service;

import cse.oop2.hotelflow.Common.model.Payment;
import cse.oop2.hotelflow.Common.model.PaymentMethod;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PaymentService {

    private final Path filePath;

    public PaymentService(String filePath) {
        this.filePath = Paths.get(filePath);
    }

    // 결제 저장
    public synchronized Payment savePayment(
            String reservationId,
            int roomAmount,
            int fnbAmount,
            PaymentMethod method
    ) throws IOException {

        // 디렉터리 없으면 생성
        if (filePath.getParent() != null && !Files.exists(filePath.getParent())) {
            Files.createDirectories(filePath.getParent());
        }

        String id = UUID.randomUUID().toString();

        Payment payment = Payment.createNew(id, reservationId, roomAmount, fnbAmount, method);

        try (BufferedWriter bw = Files.newBufferedWriter(
                filePath,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        )) {
            bw.write(payment.toCsv());
            bw.newLine();
        }

        return payment;
    }

    //  전체 결제 내역 조회 (GET_SALES에서 사용)
    public synchronized List<Payment> findAll() throws IOException {
        List<Payment> result = new ArrayList<>();

        if (!Files.exists(filePath)) {
            return result;
        }

        try (BufferedReader br = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                result.add(Payment.fromCsv(line));
            }
        }

        return result;
    }
}
