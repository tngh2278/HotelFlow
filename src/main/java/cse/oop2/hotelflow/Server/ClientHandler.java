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

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Optional;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final AuthService authService;
    private final RoomService roomService;
    private final ReservationService reservationService;
    private final CheckInOutService checkInOutService;
    private final RoomServiceOrderService roomServiceOrderService;  // ✅ 룸서비스

    public ClientHandler(Socket socket, AuthService authService, RoomService roomService) {
        this.socket = socket;
        this.authService = authService;
        this.roomService = roomService;
        this.reservationService = new ReservationService("data/reservations.csv");
        this.checkInOutService = new CheckInOutService("data/reservations.csv", "data/rooms.csv");
        this.roomServiceOrderService = new RoomServiceOrderService("data/room_service_orders.csv");
    }

    @Override
    public void run() {
        System.out.println("클라이언트 핸들러 시작: " + socket);
        try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("[Server] 수신: " + line);   // 👈 디버깅용 로그
                String[] parts = line.split("\\|", -1);
                String command = parts[0];

                // PING
                if ("PING".equals(command)) {
                    out.println("PONG");

                // 로그인
                } else if ("LOGIN".equals(command) && parts.length >= 3) {
                    String id = parts[1];
                    String password = parts[2];

                    Optional<User> userOpt = authService.login(id, password);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        out.println("OK|" + user.getRole());
                    } else {
                        out.println("FAIL|아이디 또는 비밀번호가 올바르지 않습니다.");
                    }

                // 객실 전체 조회
                } else if ("GET_ROOMS".equals(command)) {
                    List<Room> rooms = roomService.getAllRooms();
                    StringBuilder sb = new StringBuilder("ROOMS|");
                    for (int i = 0; i < rooms.size(); i++) {
                        Room r = rooms.get(i);
                        sb.append(r.getRoomNum())
                          .append(',')
                          .append(r.getRoomStatus())
                          .append(',')
                          .append(r.getCapacity());
                        if (i < rooms.size() - 1) {
                            sb.append(';');
                        }
                    }
                    out.println(sb.toString());

                // 예약 전체 조회
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
                        if (i < reservations.size() - 1) {
                            sb.append(';');
                        }
                    }
                    out.println(sb.toString());

                // 예약 생성
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
                    } catch (IllegalArgumentException e) {
                        out.println("FAIL|" + e.getMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                        out.println("FAIL|예약 저장 중 오류가 발생했습니다.");
                    }

                // 예약 취소
                } else if ("CANCEL_RESERVATION".equals(command) && parts.length >= 2) {
                    String reservationId = parts[1].trim();
                    try {
                        reservationService.cancelReservation(reservationId);
                        out.println("OK|예약이 취소되었습니다.");
                    } catch (IllegalArgumentException e) {
                        out.println("FAIL|" + e.getMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                        out.println("FAIL|예약 취소 중 오류가 발생했습니다.");
                    }

                // 체크인
                } else if ("CHECK_IN".equals(command) && parts.length >= 2) {
                    String reservationId = parts[1].trim();
                    try {
                        checkInOutService.checkIn(reservationId);
                        out.println("OK|체크인 완료되었습니다.");
                    } catch (IllegalArgumentException e) {
                        out.println("FAIL|" + e.getMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                        out.println("FAIL|체크인 중 오류가 발생했습니다.");
                    }

                // 체크아웃
                } else if ("CHECK_OUT".equals(command) && parts.length >= 2) {
                    String reservationId = parts[1].trim();
                    try {
                        checkInOutService.checkOut(reservationId);
                        out.println("OK|체크아웃 완료되었습니다.");
                    } catch (IllegalArgumentException e) {
                        out.println("FAIL|" + e.getMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                        out.println("FAIL|체크아웃 중 오류가 발생했습니다.");
                    }

                //  룸서비스 전체 조회
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
                        if (i < orders.size() - 1) {
                            sb.append(';');
                        }
                    }
                    out.println(sb.toString());

                //  룸서비스 요청 생성
                } else if ("CREATE_ROOM_SERVICE_ORDER".equals(command) && parts.length >= 3) {
                    try {
                        int roomNum = Integer.parseInt(parts[1].trim());
                        String description = parts[2].trim();

                        RoomServiceOrder created =
                                roomServiceOrderService.createOrder(roomNum, description);

                        out.println("OK|" + created.getId());
                    } catch (NumberFormatException e) {
                        out.println("FAIL|객실 번호는 숫자여야 합니다.");
                    } catch (IllegalArgumentException e) {
                        out.println("FAIL|" + e.getMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                        out.println("FAIL|룸서비스 요청 저장 중 오류가 발생했습니다.");
                    }

                // 룸서비스 완료 처리
                } else if ("COMPLETE_ROOM_SERVICE_ORDER".equals(command) && parts.length >= 2) {
                    String orderId = parts[1].trim();
                    try {
                        roomServiceOrderService.completeOrder(orderId);
                        out.println("OK|COMPLETED");
                    } catch (IllegalArgumentException e) {
                        out.println("FAIL|" + e.getMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                        out.println("FAIL|룸서비스 완료 처리 중 오류가 발생했습니다.");
                    }

                // 알 수 없는 명령
                } else {
                    out.println("FAIL|알 수 없는 명령: " + command);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
            System.out.println("클라이언트 연결 종료: " + socket);
        }
    }
}
