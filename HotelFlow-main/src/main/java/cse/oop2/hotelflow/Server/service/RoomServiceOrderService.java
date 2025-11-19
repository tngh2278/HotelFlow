package cse.oop2.hotelflow.Server.service;

import cse.oop2.hotelflow.Common.model.RoomServiceOrder;
import cse.oop2.hotelflow.Common.model.RoomServiceOrderStatus;
import cse.oop2.hotelflow.Server.file.FileRoomServiceOrderRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class RoomServiceOrderService {

    private final FileRoomServiceOrderRepository orderRepository;
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public RoomServiceOrderService(String filePath) {
        this.orderRepository = new FileRoomServiceOrderRepository(filePath);
    }

    public List<RoomServiceOrder> getAllOrders() {
        try {
            return orderRepository.findAll();
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public RoomServiceOrder createOrder(int roomNum, String description)
            throws IOException {

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("요청 내용을 입력하세요.");
        }

        if (roomNum <= 0) {
            throw new IllegalArgumentException("유효한 객실 번호를 입력하세요.");
        }

        String id = "S" + System.currentTimeMillis();
        String now = LocalDateTime.now().format(formatter);

        RoomServiceOrder order = new RoomServiceOrder(
                id,
                roomNum,
                description,
                RoomServiceOrderStatus.REQUESTED,
                now
        );

        orderRepository.add(order);
        return order;
    }

    public void completeOrder(String orderId) throws IOException {
        List<RoomServiceOrder> all = orderRepository.findAll();
        boolean found = false;

        for (RoomServiceOrder o : all) {
            if (o.getId().equals(orderId)) {
                if (o.getStatus() == RoomServiceOrderStatus.COMPLETED) {
                    throw new IllegalArgumentException("이미 완료된 요청입니다.");
                }
                if (o.getStatus() == RoomServiceOrderStatus.CANCELED) {
                    throw new IllegalArgumentException("취소된 요청입니다.");
                }
                o.setStatus(RoomServiceOrderStatus.COMPLETED);
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("해당 룸서비스 요청을 찾을 수 없습니다.");
        }

        orderRepository.saveAll(all);
    }
}
