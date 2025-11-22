package cse.oop2.hotelflow.Client.ui;

import cse.oop2.hotelflow.Client.net.ClientConnection;
import cse.oop2.hotelflow.Common.model.UserRole;
import cse.oop2.hotelflow.Client.ui.CustomerMainFrame;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class LoginFrame extends JFrame {

    private final LoginMode mode;   //  어떤 로그인 모드인지 저장

    private JTextField idField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    // 
    public LoginFrame() {
        this(LoginMode.STAFF_ADMIN);
    }

    // 새로운 생성자: 모드를 받아서 생성
    public LoginFrame(LoginMode mode) {
        this.mode = mode;

        setTitle("HotelFlow 로그인");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null); // 화면 중앙

        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 상태 라벨
        statusLabel = new JLabel("서버에 연결 후 로그인하세요.");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // 폼 패널 (ID / PW)
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        JLabel idLabel = new JLabel("아이디:");
        JLabel pwLabel = new JLabel("비밀번호:");

        idField = new JTextField();
        passwordField = new JPasswordField();

        formPanel.add(idLabel);
        formPanel.add(idField);
        formPanel.add(pwLabel);
        formPanel.add(passwordField);

        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton loginButton = new JButton("로그인");
        JButton cancelButton = new JButton("종료");

        loginButton.addActionListener(e -> doLogin());
        cancelButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);

        panel.add(statusLabel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
    }

    private void doLogin() {
        String id = idField.getText().trim();
        String pw = new String(passwordField.getPassword());

        // 기본 입력 검증
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "아이디를 입력하세요.");
            idField.requestFocus();
            return;
        }
        if (pw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "비밀번호를 입력하세요.");
            passwordField.requestFocus();
            return;
        }

        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            // 서버 상태 체크
            String pong = conn.sendAndReceive("PING");
            if (!"PONG".equals(pong)) {
                statusLabel.setText("서버 응답 오류");
                JOptionPane.showMessageDialog(this, "서버 응답이 올바르지 않습니다.");
                return;
            }

            // 로그인 요청
            String response = conn.sendAndReceive("LOGIN|" + id + "|" + pw);

            if (response != null && response.startsWith("OK|")) {
                String roleStr = response.split("\\|")[1];
                statusLabel.setText("로그인 성공 (" + roleStr + ")");

                UserRole role = UserRole.valueOf(roleStr);

                // 모드별 분기
                if (mode == LoginMode.STAFF_ADMIN) {
                    handleStaffAdminLogin(role);
                } else if (mode == LoginMode.CUSTOMER) {
                    handleCustomerLogin(role);
                }

            } else if (response != null && response.startsWith("FAIL|")) {
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

    // 직원/관리자 모드 처리
    private void handleStaffAdminLogin(UserRole role) {
        if (role == UserRole.ADMIN || role == UserRole.STAFF) {
            MainFrame mainFrame = new MainFrame(role);
            mainFrame.setVisible(true);
            mainFrame.loadInitialData();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "직원/관리자 전용 로그인입니다.\n(현재 역할: " + role + ")");
        }
    }

    // 고객 모드 처리
    private void handleCustomerLogin(UserRole role) {
        if (role == UserRole.CUSTOMER) {
            // 고객 전용 메인 프레임으로 이동
            CustomerMainFrame cm = new CustomerMainFrame(role);
            cm.setVisible(true);
            cm.loadInitialData();
            dispose();

        } else {
            JOptionPane.showMessageDialog(this,
                    "고객 전용 로그인입니다.\n(현재 역할: " + role + ")");
        }
    }
}
