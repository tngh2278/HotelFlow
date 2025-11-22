package cse.oop2.hotelflow.Server.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import cse.oop2.hotelflow.Common.model.Room;
import cse.oop2.hotelflow.Common.model.RoomStatus;

public class FileRoomRepository {

    private final Path roomFile;

    public FileRoomRepository(String filePath) {
        this.roomFile = Paths.get(filePath);
    }

    // rooms 전체를 파일에 저장
    public void saveAll(List<Room> rooms) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Room r : rooms) {
            sb.append(r.getRoomNum())
                    .append(',')
                    .append(r.getRoomStatus().name())
                    .append(',')
                    .append(r.getCapacity())
                    .append('\n');
        }
        // 네가 쓰던 유틸 그대로 사용
        FileUtils.writeWithLock(roomFile, sb.toString());
    }

    // 파일에서 rooms 전체 읽기
    public List<Room> findAll() throws IOException {
        List<Room> rooms = new ArrayList<>();
        if (!Files.exists(roomFile)) {
            return rooms;
        }

        try (BufferedReader br = Files.newBufferedReader(roomFile)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length < 3) {
                    continue;
                }

                int roomNumber = Integer.parseInt(parts[0].trim());
                RoomStatus status = RoomStatus.valueOf(parts[1].trim());
                int capacity = Integer.parseInt(parts[2].trim());
                rooms.add(new Room(roomNumber, status, capacity));
            }
        }
        return rooms;
    }
}
