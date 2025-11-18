package cse.oop2.hotelflow.Client.ui;

import cse.oop2.hotelflow.Client.net.ClientConnection;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class LoginFrame extends JFrame{
    private JTextField idField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public LoginFrame() {
        setTitle("HotelFlow 로그인");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));

        JLabel idLabel = new JLabel("아이디:");
        JLabel pwLabel = new JLabel("비밀번호:");

        idField = new JTextField();
        passwordField = new JPasswordField();

        formPanel.add(idLabel);
        formPanel.add(idField);
        formPanel.add(pwLabel);
        formPanel.add(passwordField);

        JButton loginButton = new JButton("로그인");
        loginButton.addActionListener(e -> doLogin());

        statusLabel = new JLabel("서버 연결 상태: 확인 중...");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(loginButton, BorderLayout.SOUTH);
        panel.add(statusLabel, BorderLayout.NORTH);

        add(panel);
    }

    private void doLogin() {
        String id = idField.getText().trim();
        String pw = new String(passwordField.getPassword());

        if (id.isEmpty() || pw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 입력하세요.");
            return;
        }

        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            // 먼저 서버 체크
            String pong = conn.sendAndReceive("PING");
            if (!"PONG".equals(pong)) {
                statusLabel.setText("서버 응답 오류");
                return;
            }

            String response = conn.sendAndReceive("LOGIN|" + id + "|" + pw);
            if (response.startsWith("OK|")) {
                String role = response.split("\\|")[1];
                statusLabel.setText("로그인 성공 (" + role + ")");
                // TODO: MainFrame 띄우고, 현재 창 dispose()
            } else if (response.startsWith("FAIL|")) {
                String msg = response.substring("FAIL|".length());
                statusLabel.setText("로그인 실패");
                JOptionPane.showMessageDialog(this, msg);
            } else {
                statusLabel.setText("알 수 없는 응답: " + response);
            }
        } catch (IOException ex) {
            statusLabel.setText("서버에 연결할 수 없습니다.");
            JOptionPane.showMessageDialog(this,
                    "서버가 실행 중인지 확인하세요.\n(서버 선실행 규칙 위반)");
        }
    }
}
