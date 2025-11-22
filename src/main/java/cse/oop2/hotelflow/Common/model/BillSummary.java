package cse.oop2.hotelflow.Common.model;


//예상 청구 금액 요약 클래스
public class BillSummary {

    private final int roomAmount;
    private final int fnbAmount;
    private final int totalAmount;

    public BillSummary(int roomAmount, int fnbAmount) {
        this.roomAmount = roomAmount;
        this.fnbAmount = fnbAmount;
        this.totalAmount = roomAmount + fnbAmount;
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
}
