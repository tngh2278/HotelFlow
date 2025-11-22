package cse.oop2.hotelflow.Client.Guest;

import cse.oop2.hotelflow.Client.net.ClientConnection;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class FeedbackDialog extends JDialog {
    private String bookingId;
    private JTextArea contentArea;

    public FeedbackDialog(Frame owner, String bookingId) {
        super(owner, "피드백 작성", true);
        this.bookingId = bookingId;
        setSize(400, 300);
        setLocationRelativeTo(owner);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JLabel label = new JLabel("호텔 이용 중 불편한 점이나 좋았던 점을 적어주세요.");
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        
        contentArea = new JTextArea();
        contentArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(contentArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnSend = new JButton("제출하기");
        btnSend.addActionListener(e -> sendFeedback());
        
        add(label, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(btnSend, BorderLayout.SOUTH);
    }

    private void sendFeedback() {
    String content = contentArea.getText().trim();
    if (content.isEmpty()) {
        JOptionPane.showMessageDialog(this, "내용을 입력해주세요.");
        return;
    }

    try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
        // 줄바꿈은 공백으로 처리해서 전송 (프로토콜 단순화)
        String safeContent = content.replace("\n", " ");
        String command = "GUEST_SEND_FEEDBACK|" + bookingId + "|" + safeContent;

        String response = conn.sendAndReceive(command);

        if (response == null) {
            JOptionPane.showMessageDialog(this, "서버 응답이 없습니다.");
            return;
        }

        if (response.startsWith("OK|")) {
            JOptionPane.showMessageDialog(this, "소중한 의견 감사합니다.");
            dispose(); // 창 닫기

        } else if (response.startsWith("FAIL|")) {
            String msg = response.substring("FAIL|".length());
            JOptionPane.showMessageDialog(this, msg);

        } else {
            JOptionPane.showMessageDialog(this, "알 수 없는 응답: " + response);
        }

    } catch (IOException e) {
        JOptionPane.showMessageDialog(this,
                "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인해주세요.");
    }
}
}