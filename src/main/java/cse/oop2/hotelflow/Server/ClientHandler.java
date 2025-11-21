package cse.oop2.hotelflow.Server;

import cse.oop2.hotelflow.Common.model.User;
import cse.oop2.hotelflow.Common.model.Room;
import cse.oop2.hotelflow.Common.model.Reservation;
import cse.oop2.hotelflow.Common.model.RoomServiceOrder;

import cse.oop2.hotelflow.Server.service.AuthService;
import cse.oop2.hotelflow.Server.service.RoomService;
import cse.oop2.hotelflow.Server.service.ReservationService;
import cse.oop2.hotelflow.Server.service.CheckInOutService;
import cse.oop2.hotelflow.Server.service.RoomServiceOrderService;
import cse.oop2.hotelflow.Server.service.FeedbackService;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final AuthService authService;
    private final RoomService roomService;
    private final ReservationService reservationService;
    private final CheckInOutService checkInOutService;
    private final RoomServiceOrderService roomServiceOrderService;
    private final FeedbackService feedbackService;

    public ClientHandler(Socket socket, AuthService authService, RoomService roomService) {
        this.socket = socket;
        this.authService = authService;
        this.roomService = roomService;
        this.reservationService = new ReservationService("data/reservations.csv");
        this.checkInOutService = new CheckInOutService("data/reservations.csv", "data/rooms.csv");
        this.roomServiceOrderService = new RoomServiceOrderService("data/room_service_orders.csv");
        this.feedbackService = new FeedbackService("data/feedback.csv");
    }

    @Override
    public void run() {
        System.out.println("클라이언트 핸들러 시작: " + socket);
        
        try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("[Server] 수신: " + line);
                String[] parts = line.split("\\|", -1);
                String command = parts[0];

                // 1. PING
                if ("PING".equals(command)) {
                    out.println("PONG");

                // 2. 로그인 (직원용)
                } else if ("LOGIN".equals(command) && parts.length >= 3) {
                    String id = parts[1];
                    String password = parts[2];
                    Optional<User> userOpt = authService.login(id, password);
                    if (userOpt.isPresent()) {
                        out.println("OK|" + userOpt.get().getRole());
                    } else {
                        out.println("FAIL|아이디 또는 비밀번호가 올바르지 않습니다.");
                    }

                // 3. 투숙객(비회원) 로그인
                } else if ("GUEST_LOGIN".equals(command) && parts.length >= 3) {
                    String inputName = parts[1].trim();
                    String inputPhone = parts[2].trim();
                    
                    List<Reservation> allReservations = reservationService.getAllReservations();
                    Reservation found = null;
                    
                    for (Reservation r : allReservations) {
                        if (r.getCustomerName().equals(inputName) && r.getPhone().equals(inputPhone)) {
                            found = r;
                            break;
                        }
                    }
                    
                    if (found != null) {
                        out.println("OK|" + found.getId());
                    } else {
                        out.println("FAIL|일치하는 예약 정보가 없습니다.");
                    }

                // 4. 객실 조회
                } else if ("GET_ROOMS".equals(command)) {
                    List<Room> rooms = roomService.getAllRooms();
                    StringBuilder sb = new StringBuilder("ROOMS|");
                    for (int i = 0; i < rooms.size(); i++) {
                        Room r = rooms.get(i);
                        sb.append(r.getRoomNum()).append(',')
                          .append(r.getRoomStatus()).append(',')
                          .append(r.getCapacity());
                        if (i < rooms.size() - 1) sb.append(';');
                    }
                    out.println(sb.toString());

                // 5. 예약 조회 (전체)
                } else if ("GET_RESERVATIONS".equals(command)) {
                    List<Reservation> reservations = reservationService.getAllReservations();
                    StringBuilder sb = new StringBuilder("RESERVATIONS|");
                    for (int i = 0; i < reservations.size(); i++) {
                        Reservation r = reservations.get(i);
                        sb.append(r.getId()).append(',')
                          .append(r.getRoomNum()).append(',')
                          .append(r.getCustomerName()).append(',')
                          .append(r.getPhone()).append(',')
                          .append(r.getCheckInDate()).append(',')
                          .append(r.getCheckOutDate()).append(',')
                          .append(r.getStatus().name());
                        if (i < reservations.size() - 1) sb.append(';');
                    }
                    out.println(sb.toString());

                // 6. 예약 생성
                } else if ("CREATE_RESERVATION".equals(command) && parts.length >= 6) {
                    try {
                        int roomNum = Integer.parseInt(parts[1].trim());
                        String customerName = parts[2].trim();
                        String phone = parts[3].trim();
                        String checkInDate = parts[4].trim();
                        String checkOutDate = parts[5].trim();

                        Reservation created = reservationService.createReservation(
                                roomNum, customerName, phone, checkInDate, checkOutDate
                        );
                        out.println("OK|" + created.getId());
                    } catch (Exception e) {
                        out.println("FAIL|" + e.getMessage());
                    }

                // 7. 예약 취소
                } else if ("CANCEL_RESERVATION".equals(command) && parts.length >= 2) {
                    try {
                        reservationService.cancelReservation(parts[1].trim());
                        out.println("OK|예약이 취소되었습니다.");
                    } catch (Exception e) {
                        out.println("FAIL|" + e.getMessage());
                    }

                // 8. 체크인 (직원용)
                } else if ("CHECK_IN".equals(command) && parts.length >= 2) {
                    try {
                        checkInOutService.checkIn(parts[1].trim());
                        out.println("OK|체크인 완료되었습니다.");
                    } catch (Exception e) {
                        out.println("FAIL|" + e.getMessage());
                    }

                // 9. 투숙객 온라인 체크인 요청
                } else if ("GUEST_CHECK_IN".equals(command) && parts.length >= 2) {
                    String reservationId = parts[1].trim();
                    try {
                        checkInOutService.checkIn(reservationId);
                        out.println("OK|온라인 체크인 성공");
                    } catch (IllegalArgumentException e) {
                        out.println("FAIL|" + e.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                        out.println("FAIL|체크인 처리 중 오류 발생");
                    }

                // 10. 투숙객 예약 상세 조회 요청
                } else if ("GUEST_GET_DETAIL".equals(command) && parts.length >= 2) {
                    String targetId = parts[1].trim();
                    List<Reservation> list = reservationService.getAllReservations();
                    Reservation found = null;
                    for (Reservation r : list) {
                        if (r.getId().equals(targetId)) {
                            found = r;
                            break;
                        }
                    }
                    
                    if (found != null) {
                        // 순서: ID, Room, Name, Phone, In, Out, Status
                        String response = String.format("OK|%s|%d|%s|%s|%s|%s|%s",
                                found.getId(), found.getRoomNum(), found.getCustomerName(),
                                found.getPhone(), found.getCheckInDate(), found.getCheckOutDate(),
                                found.getStatus().name());
                        out.println(response);
                    } else {
                        out.println("FAIL|예약 정보를 찾을 수 없습니다.");
                    }                    
                    
                //11. GUEST_GET_BILL (청구 내역 조회)
                } else if ("GUEST_GET_BILL".equals(command) && parts.length >= 2) {     
                    String targetId = parts[1].trim();
                    try {
                    // 1. 예약 정보 찾기
                    List<Reservation> resList = reservationService.getAllReservations();
                    Reservation res = resList.stream()                
                            .filter(r -> r.getId().equals(targetId))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

                     // 2. 객실 정보 찾기 (1박 가격 확인용)
                    List<Room> roomList = roomService.getAllRooms();
                    Room room = roomList.stream()
                            .filter(r -> String.valueOf(r.getRoomNum()).equals(String.valueOf(res.getRoomNum())))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("객실 정보를 찾을 수 없습니다."));

                    // 3. 숙박비 계산 (박수 * 1박 가격)
                    long nights = ChronoUnit.DAYS.between(
                        LocalDate.parse(res.getCheckInDate(), DateTimeFormatter.ISO_DATE),
                        LocalDate.parse(res.getCheckOutDate(), DateTimeFormatter.ISO_DATE)
                    );
                    if (nights < 1) nights = 1; // 최소 1박

                     //수용 인원(Capacity)에 따라 가격 책정
                    long pricePerNight;
                    int cap = room.getCapacity();
        
                    if (cap <= 2) {
                        pricePerNight = 100000; // 2인실 이하 10만원
                    } else if (cap <= 4) {
                        pricePerNight = 200000; // 4인실 이하 20만원
                    } else {
                        pricePerNight = 300000; // 대형 객실 30만원
                    }

                    long roomCost = nights * pricePerNight;

                    // 4. 식음료(F&B) 비용 계산 (메뉴명에서 가격 파싱)
                    List<RoomServiceOrder> orders = roomServiceOrderService.getAllOrders();
                    long fnbCost = 0;
                    
                    for (RoomServiceOrder order : orders) {           
                        if (order.getRoomNum() == res.getRoomNum()) { 
                            String desc = order.getDescription(); // 예: "스테이크 (50,000원)"
                            try {
                                int start = desc.lastIndexOf('(');
                                int end = desc.lastIndexOf('원');
                                if (start != -1 && end != -1) {
                                    String priceStr = desc.substring(start + 1, end).replace(",", "").replace(" ", "").trim();
                                    fnbCost += Long.parseLong(priceStr);
                                }
                            } catch (Exception ignored) {}
                        }
                     }

                    // 5. 결과 전송: OK|객실료|식음료비|총합
                    out.println("OK|" + roomCost + "|" + fnbCost + "|" + (roomCost + fnbCost));

                } catch (Exception e) {
                    e.printStackTrace();
                    out.println("FAIL|계산 중 오류 발생: " + e.getMessage());
                }

                // 12. 체크아웃
                } else if ("CHECK_OUT".equals(command) && parts.length >= 2) {
                    try {
                        checkInOutService.checkOut(parts[1].trim());
                        out.println("OK|체크아웃 완료되었습니다.");
                    } catch (Exception e) {
                        out.println("FAIL|" + e.getMessage());
                    }
                
                // 13. 룸서비스 조회
                } else if ("GET_ROOM_SERVICE_ORDERS".equals(command)) {
                    List<RoomServiceOrder> orders = roomServiceOrderService.getAllOrders();
                    StringBuilder sb = new StringBuilder("ROOM_SERVICE_ORDERS|");
                    for (int i = 0; i < orders.size(); i++) {
                        RoomServiceOrder o = orders.get(i);
                        sb.append(o.getId()).append(',')
                          .append(o.getRoomNum()).append(',')
                          .append(o.getDescription()).append(',')
                          .append(o.getStatus().name()).append(',')
                          .append(o.getCreatedAt());
                        if (i < orders.size() - 1) sb.append(';');
                    }
                    out.println(sb.toString());

                // 14. 룸서비스 생성
                } else if ("CREATE_ROOM_SERVICE_ORDER".equals(command) && parts.length >= 3) {
                    try {
                        int roomNum = Integer.parseInt(parts[1].trim());
                        String description = parts[2].trim();
                        RoomServiceOrder created = roomServiceOrderService.createOrder(roomNum, description);
                        out.println("OK|" + created.getId());
                    } catch (Exception e) {
                        out.println("FAIL|" + e.getMessage());
                    }

                // 15. 룸서비스 완료 처리
                } else if ("COMPLETE_ROOM_SERVICE_ORDER".equals(command) && parts.length >= 2) {
                    try {
                        roomServiceOrderService.completeOrder(parts[1].trim());
                        out.println("OK|COMPLETED");
                    } catch (Exception e) {
                        out.println("FAIL|" + e.getMessage());
                    }
                
                // 16. 투숙객 피드백 저장
                } else if ("GUEST_SEND_FEEDBACK".equals(command) && parts.length >= 3) {
                    try {
                        String bookingId = parts[1].trim();
                        String content = parts[2].trim();
        
                        feedbackService.saveFeedback(bookingId, content);
                        out.println("OK|피드백이 저장되었습니다.");
                     } catch (Exception e) {
                        e.printStackTrace();
                        out.println("FAIL|피드백 저장 중 오류 발생");
                }
                
                // 17. 직원용 피드백 전체 조회
                } else if ("GET_ALL_FEEDBACK".equals(command)) {
                    List<cse.oop2.hotelflow.Common.model.Feedback> list = feedbackService.findAll();
                    StringBuilder sb = new StringBuilder("ALL_FEEDBACK|");
    
                    for (int i = 0; i < list.size(); i++) {
                        cse.oop2.hotelflow.Common.model.Feedback f = list.get(i);
                        // 데이터 구분자: 세미콜론(;)
                        sb.append(f.getBookingId()).append(",")
                          .append(f.getContent()).append(",")
                          .append(f.getCreatedAt());
          
                        if (i < list.size() - 1) sb.append(";");
                    }
                    out.println(sb.toString());
                    
                } else {
                    out.println("FAIL|알 수 없는 명령: " + command);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
            System.out.println("클라이언트 연결 종료: " + socket);
        }
    }
}