package cse.oop2.hotelflow.Common.model;

public class Reservation {

    private String id; // 예약번호
    private int roomNum; // 객실 번호
    private String customerName; // 고객 이름
    private String phone; // 전화번호
    private String checkInDate; // 체크인 날짜 (yyyy-MM-dd)
    private String checkOutDate; // 체크아웃 날짜 (yyyy-MM-dd)
    private ReservationStatus status; // 예약 상태

    public Reservation(String id,
            int roomNum,
            String customerName,
            String phone,
            String checkInDate,
            String checkOutDate,
            ReservationStatus status) {
        this.id = id;
        this.roomNum = roomNum;
        this.customerName = customerName;
        this.phone = phone;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = status;
    }

    // getter
    public String getId() {
        return id;
    }

    public int getRoomNum() {
        return roomNum;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPhone() {
        return phone;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    // setter
    public void setRoomNum(int roomNum) {
        this.roomNum = roomNum;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCheckInDate(String checkInDate) {
        this.checkInDate = checkInDate;
    }

    public void setCheckOutDate(String checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    // CSV 한 줄로 변환
    public String toCsv() {
        return id + "," +
                roomNum + "," +
                customerName + "," +
                phone + "," +
                checkInDate + "," +
                checkOutDate + "," +
                status.name();
    }

    // CSV 한 줄에서 Reservation으로 변환
    public static Reservation fromCsv(String line) {
        String[] parts = line.split(",");
        if (parts.length < 7) {
            throw new IllegalArgumentException("잘못된 예약 라인: " + line);
        }
        String id = parts[0].trim();
        int roomNum = Integer.parseInt(parts[1].trim());
        String customerName = parts[2].trim();
        String phone = parts[3].trim();
        String checkInDate = parts[4].trim();
        String checkOutDate = parts[5].trim();
        ReservationStatus status = ReservationStatus.valueOf(parts[6].trim());

        return new Reservation(id, roomNum, customerName, phone,
                checkInDate, checkOutDate, status);
    }
}
