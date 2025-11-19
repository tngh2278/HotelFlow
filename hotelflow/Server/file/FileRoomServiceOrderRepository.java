package cse.oop2.hotelflow.Server.file;

import cse.oop2.hotelflow.Common.model.RoomServiceOrder;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileRoomServiceOrderRepository {

    private final Path orderFile;

    public FileRoomServiceOrderRepository(String filePath) {
        this.orderFile = Paths.get(filePath);
    }

    public List<RoomServiceOrder> findAll() throws IOException {
        List<RoomServiceOrder> list = new ArrayList<>();
        if (!Files.exists(orderFile)) {
            return list;
        }

        try (BufferedReader br = Files.newBufferedReader(orderFile)) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // 첫 줄이 헤더라면 스킵
                if (first && line.startsWith("id,")) {
                    first = false;
                    continue;
                }
                first = false;

                try {
                    list.add(RoomServiceOrder.fromCsv(line));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    public void saveAll(List<RoomServiceOrder> list) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("id,roomNum,description,status,createdAt\n");
        for (RoomServiceOrder o : list) {
            sb.append(o.toCsv()).append("\n");
        }
        FileUtils.writeWithLock(orderFile, sb.toString());
    }

    public void add(RoomServiceOrder order) throws IOException {
        List<RoomServiceOrder> all = findAll();
        all.add(order);
        saveAll(all);
    }
}
