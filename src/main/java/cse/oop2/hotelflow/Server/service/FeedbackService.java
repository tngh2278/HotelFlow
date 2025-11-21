package cse.oop2.hotelflow.Server.service;

import cse.oop2.hotelflow.Common.model.Feedback;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class FeedbackService {
    private final Path filePath;

    public FeedbackService(String path) {
        this.filePath = Paths.get(path);
        initFile();
    }

    private void initFile() {
        if (!Files.exists(filePath)) {
            try {
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveFeedback(String bookingId, String content) throws IOException {
        Feedback feedback = new Feedback(bookingId, content);
        
        // 파일 끝에 추가 (Append 모드), UTF-8 인코딩 사용
        try (BufferedWriter writer = Files.newBufferedWriter(
                filePath, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            writer.write(feedback.toCsv());
            writer.newLine();
        }
    }
    
    // 모든 피드백 읽어오기
    public java.util.List<Feedback> findAll() {
        java.util.List<Feedback> list = new java.util.ArrayList<>();
        if (!Files.exists(filePath)) return list;

        try (BufferedReader br = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                // CSV 형식: bookingId,content,createdAt
                String[] parts = line.split(",", 3);
                if (parts.length >= 3) {
                    list.add(new Feedback(parts[0], parts[1], parts[2]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}