package cse.oop2.hotelflow.Common.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Feedback {
    private String bookingId; // 누가 썼는지 (예약번호)
    private String content;   // 내용
    private String createdAt; // 작성일시

    public Feedback(String bookingId, String content) {
        this.bookingId = bookingId;
        this.content = content;
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // 파일에서 읽어올 때 쓰는 생성자
    public Feedback(String bookingId, String content, String createdAt) {
        this.bookingId = bookingId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String toCsv() {
        // 콤마(,)가 내용에 있으면 파일이 깨지므로 공백으로 치환
        String safeContent = content.replace(",", " ").replace("\n", " ");
        return bookingId + "," + safeContent + "," + createdAt;
    }

    public String getBookingId() { return bookingId; }
    public String getContent() { return content; }
    public String getCreatedAt() { return createdAt; }
}