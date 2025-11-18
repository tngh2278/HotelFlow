package cse.oop2.hotelflow.Common.model;

public class Room {
    private int roomNumber;
    private RoomStatus status;
    private int capacity;

    public Room(int roomNumber, RoomStatus status , int capacity){
        this.roomNumber = roomNumber;
        this.status = status;
        this.capacity = capacity;
    }


    //getter , setter 

    public int getRoomNum() { return roomNumber;}
    public RoomStatus getRoomStatus() {return status;}
    public int getCapacity() { return capacity;}

    @Override
    public String toString(){
        return roomNumber + "," + status + "," + capacity;
    }

}
