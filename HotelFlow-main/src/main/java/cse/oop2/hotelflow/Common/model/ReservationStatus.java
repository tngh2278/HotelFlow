package cse.oop2.hotelflow.Common.model;

public enum ReservationStatus {
    RESERVED,      // 예약 완료 (아직 체크인 전)
    CHECKED_IN,    // 체크인 완료
    CHECKED_OUT,   // 체크아웃 완료
    CANCELED       // 예약 취소
}