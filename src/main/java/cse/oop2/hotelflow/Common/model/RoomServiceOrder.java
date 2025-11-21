     package cse.oop2.hotelflow.Common.model;

public class RoomServiceOrder {

    private String id;                     // 주문 ID
    private int roomNum;                   // 객실 번호
    private String description;            // 요청 내용 (예: 라면, 수건 추가 등)
    private RoomServiceOrderStatus status; // 상태
    private String createdAt;              // 생성 시각 문자열 (yyyy-MM-dd HH:mm 형식)

    public RoomServiceOrder(String id,
                            int roomNum,
                            String description,
                            RoomServiceOrderStatus status,
                            String createdAt) {
        this.id = id;
        this.roomNum = roomNum;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public int getRoomNum() {
        return roomNum;
    }

    public String getDescription() {
        return description;
    }

    public RoomServiceOrderStatus getStatus() {
        return status;
    }

    public void setStatus(RoomServiceOrderStatus status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    // CSV 한 줄로 변환
    public String toCsv() {
        // id,roomNum,description,status,createdAt
        return id + "," +
               roomNum + "," +
               escape(description) + "," +
               status.name() + "," +
               createdAt;
    }

    private static String escape(String text) {
        if (text == null) return "";
        return text.replace(",", " "); 
    }

    public static RoomServiceOrder fromCsv(String line) {
        String[] parts = line.split(",", 5); // 5개로만 나누기
        if (parts.length < 5) {
            throw new IllegalArgumentException("잘못된 룸서비스 라인: " + line);
        }

        String id = parts[0].trim();
        int roomNum = Integer.parseInt(parts[1].trim());
        String description = parts[2].trim();
        RoomServiceOrderStatus status =
                RoomServiceOrderStatus.valueOf(parts[3].trim());
        String createdAt = parts[4].trim();

        return new RoomServiceOrder(id, roomNum, description, status, createdAt);
    }
}
