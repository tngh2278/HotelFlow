package cse.oop2.hotelflow.Common.model;

public class Payment {

    private final String id;
    private final String reservationId;
    private final int roomAmount;
    private final int fnbAmount;
    private final int totalAmount;
    private final PaymentMethod method;
    private final String paidAt; // yyyy-MM-dd

    public Payment(String id,
                   String reservationId,
                   int roomAmount,
                   int fnbAmount,
                   int totalAmount,
                   PaymentMethod method,
                   String paidAt) {
        this.id = id;
        this.reservationId = reservationId;
        this.roomAmount = roomAmount;
        this.fnbAmount = fnbAmount;
        this.totalAmount = totalAmount;
        this.method = method;
        this.paidAt = paidAt;
    }

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

    public int getTotalAmount() {
        return totalAmount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public String getPaidAt() {
        return paidAt;
    }
}
