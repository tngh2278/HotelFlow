package cse.oop2.hotelflow.Server.service;

import cse.oop2.hotelflow.Common.model.Reservation;
import cse.oop2.hotelflow.Common.model.ReservationStatus;
import cse.oop2.hotelflow.Server.file.FileReservationRepository;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ReservationService {

    private final FileReservationRepository reservationRepository;

    // 간단하게 파일 경로를 직접 받는 생성자
    public ReservationService(String filePath) {
        this.reservationRepository = new FileReservationRepository(filePath);
    }

    public List<Reservation> getAllReservations() {
        try {
            return reservationRepository.findAll();
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    // 예약 생성 메서드
    public Reservation createReservation(int roomNum,
                                         String customerName,
                                         String phone,
                                         String checkInDate,
                                         String checkOutDate) throws IOException {

        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("고객 이름은 필수입니다.");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("전화번호는 필수입니다.");
        }
        if (checkInDate == null || checkInDate.isBlank() ||
            checkOutDate == null || checkOutDate.isBlank()) {
            throw new IllegalArgumentException("체크인/체크아웃 날짜를 입력하세요. (yyyy-MM-dd)");
        }
        

        // 간단한 예약번호 생성
        String id = "R" + System.currentTimeMillis();

        Reservation reservation = new Reservation(
                id,
                roomNum,
                customerName,
                phone,
                checkInDate,
                checkOutDate,
                ReservationStatus.RESERVED
        );

        reservationRepository.add(reservation);
        return reservation;
    }
    //예약 취소 메서드
    public void cancelReservation(String reservationId) throws IOException {
        List<Reservation> all = reservationRepository.findAll();
        boolean found = false;

        for (Reservation r : all) {
            if (r.getId().equals(reservationId)) {
                // 상태에 따른 메세지
                if (r.getStatus() == ReservationStatus.CANCELED) {
                    throw new IllegalArgumentException("이미 취소된 예약입니다.");
                }
                if (r.getStatus() == ReservationStatus.CHECKED_IN) {
                    throw new IllegalArgumentException("체크인된 예약은 취소할 수 없습니다.");
                }
                if (r.getStatus() == ReservationStatus.CHECKED_OUT) {
                    throw new IllegalArgumentException("체크아웃된 예약은 취소할 수 없습니다.");
                }

                r.setStatus(ReservationStatus.CANCELED);
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("해당 예약을 찾을 수 없습니다.");
        }

        reservationRepository.saveAll(all);
    }
    // 예약 내용 수정 메서드
    public void updateReservation(String id,
                                  int roomNum,
                                  String customerName,
                                  String phone,
                                  String checkInDate,
                                  String checkOutDate) throws IOException {

        List<Reservation> all = reservationRepository.findAll();
        boolean found = false;

        for (Reservation r : all) {
            if (r.getId().equals(id)) {
                // 상태 체크: 취소/체크아웃 된 예약은 수정 금지
                if (r.getStatus() == ReservationStatus.CANCELED
                        || r.getStatus() == ReservationStatus.CHECKED_OUT) {
                    throw new IllegalArgumentException("취소되었거나 체크아웃된 예약은 수정할 수 없습니다.");
                }

                // 실제 필드 수정
                r.setRoomNum(roomNum);
                r.setCustomerName(customerName);
                r.setPhone(phone);
                r.setCheckInDate(checkInDate);
                r.setCheckOutDate(checkOutDate);

                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("해당 예약을 찾을 수 없습니다.");
        }

        reservationRepository.saveAll(all);
    }
}
