package cse.oop2.hotelflow.Client.ui;

import cse.oop2.hotelflow.Client.net.ClientConnection;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class CustomerRegisterFrame extends JFrame {

    private JTextField idField;
    private JTextField nameField;
    private JPasswordField pwField;
    private JPasswordField pwConfirmField;
    private JTextField phoneField;
    private JLabel statusLabel;

    public CustomerRegisterFrame() {
        setTitle("고객 회원가입");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 320);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statusLabel = new JLabel("정보를 입력 후 가입 버튼을 눌러주세요.");
        panel.add(statusLabel, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.add(new JLabel("아이디:"));
        idField = new JTextField();
        form.add(idField);

        form.add(new JLabel("이름:"));
        nameField = new JTextField();
        form.add(nameField);

        form.add(new JLabel("비밀번호:"));
        pwField = new JPasswordField();
        form.add(pwField);

        form.add(new JLabel("비밀번호 확인:"));
        pwConfirmField = new JPasswordField();
        form.add(pwConfirmField);

        form.add(new JLabel("전화번호:"));
        phoneField = new JTextField();
        form.add(phoneField);

        panel.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton registerBtn = new JButton("가입");
        JButton cancelBtn = new JButton("닫기");

        registerBtn.addActionListener(e -> doRegister());
        cancelBtn.addActionListener(e -> dispose());

        buttons.add(registerBtn);
        buttons.add(cancelBtn);

        panel.add(buttons, BorderLayout.SOUTH);

        add(panel);
    }

    private void doRegister() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String pw = new String(pwField.getPassword());
        String pw2 = new String(pwConfirmField.getPassword());
        String phone = phoneField.getText().trim();

        if (id.isEmpty() || name.isEmpty() || pw.isEmpty()
                || pw2.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "모든 항목을 입력해주세요.");
            return;
        }

        if (!pw.equals(pw2)) {
            JOptionPane.showMessageDialog(this, "비밀번호가 일치하지 않습니다.");
            pwField.setText("");
            pwConfirmField.setText("");
            pwField.requestFocus();
            return;
        }

        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            // REGISTER_CUSTOMER|id|name|password|phone
            String cmd = String.format("REGISTER_CUSTOMER|%s|%s|%s|%s",
                    id, name, pw, phone);
            String resp = conn.sendAndReceive(cmd);

            if (resp == null) {
                JOptionPane.showMessageDialog(this, "서버 응답이 없습니다.");
                return;
            }

            if (resp.startsWith("OK|")) {
                JOptionPane.showMessageDialog(this, "회원가입이 완료되었습니다.\n이제 로그인을 해주세요.");
                dispose();
            } else if (resp.startsWith("FAIL|")) {
                String msg = resp.substring("FAIL|".length());
                JOptionPane.showMessageDialog(this, "회원가입 실패: " + msg);
            } else {
                JOptionPane.showMessageDialog(this, "알 수 없는 응답: " + resp);
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인하세요.");
        }
    }
}
