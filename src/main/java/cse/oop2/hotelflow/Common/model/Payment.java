package cse.oop2.hotelflow.Common.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Payment {

    private final String id;             // 결제 ID
    private final String reservationId;  // 어떤 예약에 대한 결제인지
    private final int roomAmount;        // 객실 요금
    private final int fnbAmount;         // 룸서비스(식음료) 요금
    private final PaymentMethod method;  // 결제 수단 (CARD, CASH, TRANSFER 등)
    private final String createdAt;      // 결제 시각 (yyyy-MM-ddTHH:mm:ss 형식 문자열)

    // 전체 필드 초기화용 생성자
    public Payment(String id,
            String reservationId,
            int roomAmount,
            int fnbAmount,
            PaymentMethod method,
            String createdAt) {
        this.id = id;
        this.reservationId = reservationId;
        this.roomAmount = roomAmount;
        this.fnbAmount = fnbAmount;
        this.method = method;
        this.createdAt = createdAt;
    }

    // 새 결제 생성용 팩토리 (createdAt = 현재 시각)
    public static Payment createNew(String id,
            String reservationId,
            int roomAmount,
            int fnbAmount,
            PaymentMethod method) {
        String now = LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return new Payment(id, reservationId, roomAmount, fnbAmount, method, now);
    }

    // ===== Getter들 =====
    public String getId() {
        return id;
    }

    public String getReservationId() {
        return reservationId;
    }

    public int getRoomAmount() {
        return roomAmount;
    }

    public int getFnbAmount() {
        return fnbAmount;
    }

    // 총 금액은 필드로 들고 있지 않고, 필요할 때 계산
    public int getTotalAmount() {
        return roomAmount + fnbAmount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    // ===== CSV 저장/로드 =====
    // CSV 한 줄로 저장 (id,reservationId,roomAmount,fnbAmount,method,createdAt)
    public String toCsv() {
        return String.join(",",
                id,
                reservationId,
                String.valueOf(roomAmount),
                String.valueOf(fnbAmount),
                method.name(),
                createdAt
        );
    }

    // CSV 한 줄을 Payment 객체로 변환
    public static Payment fromCsv(String line) {
        String[] parts = line.split(",");
        if (parts.length < 5) {
            throw new IllegalArgumentException("잘못된 결제 데이터: " + line);
        }

        String id = parts[0].trim();
        String reservationId = parts[1].trim();
        int roomAmount = Integer.parseInt(parts[2].trim());
        int fnbAmount = Integer.parseInt(parts[3].trim());

        PaymentMethod method;
        String createdAt;

        if (parts.length >= 7) {
            // ⚠ 옛날 포맷: id,resId,room,fnb,total,method,paidAt
            method = PaymentMethod.valueOf(parts[5].trim());
            createdAt = parts[6].trim();
        } else if (parts.length == 6) {
            // 새 포맷: id,resId,room,fnb,method,createdAt
            method = PaymentMethod.valueOf(parts[4].trim());
            createdAt = parts[5].trim();
        } else {
            // 5개짜리 특이 케이스가 있다면: id,resId,room,fnb,method
            method = PaymentMethod.valueOf(parts[4].trim());
            createdAt = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        return new Payment(id, reservationId, roomAmount, fnbAmount, method, createdAt);
    }
}
