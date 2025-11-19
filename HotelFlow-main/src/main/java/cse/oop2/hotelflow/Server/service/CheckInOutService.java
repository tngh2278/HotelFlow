package cse.oop2.hotelflow.Server.service;

import cse.oop2.hotelflow.Common.model.Reservation;
import cse.oop2.hotelflow.Common.model.ReservationStatus;
import cse.oop2.hotelflow.Common.model.Room;
import cse.oop2.hotelflow.Common.model.RoomStatus;
import cse.oop2.hotelflow.Server.file.FileReservationRepository;
import cse.oop2.hotelflow.Server.file.FileRoomRepository;

import java.io.IOException;
import java.util.List;

public class CheckInOutService {

    private final FileReservationRepository reservationRepository;
    private final FileRoomRepository roomRepository;

    public CheckInOutService(String reservationFilePath, String roomFilePath) {
        this.reservationRepository = new FileReservationRepository(reservationFilePath);
        this.roomRepository = new FileRoomRepository(roomFilePath);
    }

    public void checkIn(String reservationId) throws IOException {
        List<Reservation> reservations = reservationRepository.findAll();
        Reservation target = null;

        for (Reservation r : reservations) {
            if (r.getId().equals(reservationId)) {
                target = r;
                break;
            }
        }

        if (target == null) {
            throw new IllegalArgumentException("해당 예약을 찾을 수 없습니다.");
        }
        if (target.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalArgumentException("예약 상태인 예약만 체크인할 수 있습니다.");
        }

        List<Room> rooms = roomRepository.findAll();
        Room room = null;
        for (Room rm : rooms) {
            if (rm.getRoomNum() == target.getRoomNum()) {
                room = rm;
                break;
            }
        }

        if (room == null) {
            throw new IllegalArgumentException("해당 객실을 찾을 수 없습니다.");
        }
        if (room.getRoomStatus() != RoomStatus.VACANT) {
            throw new IllegalArgumentException("빈 방인 객실만 체크인할 수 있습니다.");
        }

        // 상태 변경
        target.setStatus(ReservationStatus.CHECKED_IN);
        room.setRoomStatus(RoomStatus.OCCUPIED);

        reservationRepository.saveAll(reservations);
        roomRepository.saveAll(rooms);
    }

    public void checkOut(String reservationId) throws IOException {
        List<Reservation> reservations = reservationRepository.findAll();
        Reservation target = null;

        for (Reservation r : reservations) {
            if (r.getId().equals(reservationId)) {
                target = r;
                break;
            }
        }

        if (target == null) {
            throw new IllegalArgumentException("해당 예약을 찾을 수 없습니다.");
        }
        if (target.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new IllegalArgumentException("체크인 상태인 예약만 체크아웃할 수 있습니다.");
        }

        List<Room> rooms = roomRepository.findAll();
        Room room = null;
        for (Room rm : rooms) {
            if (rm.getRoomNum() == target.getRoomNum()) {
                room = rm;
                break;
            }
        }

        if (room == null) {
            throw new IllegalArgumentException("해당 객실을 찾을 수 없습니다.");
        }

        // 상태 변경
        target.setStatus(ReservationStatus.CHECKED_OUT);
        room.setRoomStatus(RoomStatus.VACANT);

        //파일에 적용
        reservationRepository.saveAll(reservations);
        roomRepository.saveAll(rooms);
    }
}
