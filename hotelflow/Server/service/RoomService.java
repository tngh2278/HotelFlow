package cse.oop2.hotelflow.Server.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import cse.oop2.hotelflow.Common.model.Room;
import cse.oop2.hotelflow.Server.file.FileRoomRepository;

public class RoomService {
    private final FileRoomRepository roomRepo;

    public RoomService(FileRoomRepository roomRepo) {
        this.roomRepo = roomRepo;
    }

    public List<Room> getAllRooms() {
        try {
            return roomRepo.findAll();   //  매번 파일에서 다시 읽기
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}