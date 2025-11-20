package cse.oop2.hotelflow.Server.file;

import cse.oop2.hotelflow.Common.model.Reservation;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileReservationRepository {

    private final Path reservationFile;

    public FileReservationRepository(String filePath) {
        this.reservationFile = Paths.get(filePath);
    }

    public List<Reservation> findAll() throws IOException {
        List<Reservation> list = new ArrayList<>();
        if (!Files.exists(reservationFile)) {
            return list;
        }

        try (BufferedReader br = Files.newBufferedReader(reservationFile)) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { // 헤더 스킵
                    first = false;
                    continue;
                }
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    list.add(Reservation.fromCsv(line));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    public void saveAll(List<Reservation> list) throws IOException {
        StringBuilder sb = new StringBuilder();
        // 헤더
        sb.append("id,roomNum,customerName,phone,checkInDate,checkOutDate,status\n");
        for (Reservation r : list) {
            sb.append(r.toCsv()).append("\n");
        }
        // 기존에 만든 FileUtils 활용
        FileUtils.writeWithLock(reservationFile, sb.toString());
    }

    public void add(Reservation reservation) throws IOException {
        List<Reservation> all = findAll();
        all.add(reservation);
        saveAll(all);
    }
}
