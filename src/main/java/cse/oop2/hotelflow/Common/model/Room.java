package cse.oop2.hotelflow.Common.model;

public class Room {

    private int roomNum;              // 객실 번호
    private RoomStatus roomStatus;    // 객실 상태 (VACANT, OCCUPIED, ...)
    private int capacity;             // 수용 인원

    public Room(int roomNum, RoomStatus roomStatus, int capacity) {
        this.roomNum = roomNum;
        this.roomStatus = roomStatus;
        this.capacity = capacity;
    }

    public int getRoomNum() {
        return roomNum;
    }

    public RoomStatus getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(RoomStatus roomStatus) {
        this.roomStatus = roomStatus;
    }

    public int getCapacity() {
        return capacity;
    }
}
