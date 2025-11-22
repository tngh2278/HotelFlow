package cse.oop2.hotelflow.Client.ui;

import cse.oop2.hotelflow.Client.net.ClientConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.IOException;

/**
 * 관리자/직원 계정 추가/삭제/조회 패널 - user.csv 를 직접 건드는게 아니라 서버에 명령을 보내서 처리
 */
public class UserManagementPanel extends javax.swing.JPanel {

    public UserManagementPanel() {
        initComponents();

        // 버튼 리스너 연결
        refreshButton.addActionListener(evt -> loadUserList());
        deleteButton.addActionListener(evt -> deleteUser());
        addButton.addActionListener(this::addButtonActionPerformed);

        // 처음 열릴 때 사용자 목록 한 번 로드
        loadUserList();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        userIdField = new javax.swing.JTextField();
        userPasswordField = new javax.swing.JPasswordField();
        roleComboBox = new javax.swing.JComboBox<>();
        addButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        userTable = new javax.swing.JTable();
        deleteButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();

        jLabel1.setText("ID");

        jLabel2.setText("Password");

        jLabel3.setText("직급");

        userIdField.addActionListener(this::userIdFieldActionPerformed);

        userPasswordField.addActionListener(this::userPasswordFieldActionPerformed);

        // 관리자 / 직원 선택
        roleComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[]{"관리자", "직원(CSR)"}
        ));

        addButton.setText("사용자 추가");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel3)
                                        .addComponent(jLabel2)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel1)
                                                .addGap(13, 13, 13)))
                                .addGap(42, 42, 42)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(userPasswordField)
                                        .addComponent(roleComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(userIdField))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 41, Short.MAX_VALUE)
                                .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(23, 23, 23))
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(userIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(14, 14, 14)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(userPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(23, 23, 23)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(roleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("사용자 추가", jPanel1);

        // 사용자 목록 테이블 기본 모델
        userTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "ID", "역할", "이름", "전화번호"
                }
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        jScrollPane1.setViewportView(userTable);

        deleteButton.setText("사용자 삭제");

        refreshButton.setText("새로고침");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(63, 63, 63)
                                .addComponent(deleteButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 70, Short.MAX_VALUE)
                                .addComponent(refreshButton)
                                .addGap(81, 81, 81))
        );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(deleteButton)
                                        .addComponent(refreshButton))
                                .addGap(0, 25, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("사용자 삭제/조회", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // ====== 사용자 추가 버튼 ======
    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed

        String userId = userIdField.getText().trim();
        String password = new String(userPasswordField.getPassword()).trim();
        String roleLabel = (String) roleComboBox.getSelectedItem();

        if (userId.isEmpty() || password.isEmpty() || roleLabel == null) {
            JOptionPane.showMessageDialog(this,
                    "ID와 비밀번호, 직급을 모두 입력하세요.",
                    "입력 오류",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 콤보박스 문자열 → UserRole 이름으로 매핑
        String roleCode;
        if (roleLabel.contains("관리자")) {
            roleCode = "ADMIN";
        } else {
            roleCode = "STAFF";
        }

        // 이름/전화 입력 칸이 없으므로:
        String name = userId;   // 이름 = ID
        String phone = "";      // 전화번호는 공백으로 저장

        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            // 프로토콜: ADD_USER|id|name|password|role|phone
            String cmd = String.format("ADD_USER|%s|%s|%s|%s|%s",
                    userId, name, password, roleCode, phone);

            String response = conn.sendAndReceive(cmd);

            if (response == null) {
                JOptionPane.showMessageDialog(this,
                        "서버 응답이 없습니다.",
                        "통신 오류",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (response.startsWith("OK|")) {
                JOptionPane.showMessageDialog(this,
                        "사용자 추가 성공!",
                        "성공",
                        JOptionPane.INFORMATION_MESSAGE);

                userIdField.setText("");
                userPasswordField.setText("");

                loadUserList(); // 목록 갱신

            } else if (response.startsWith("FAIL|")) {
                String msg = response.substring("FAIL|".length());
                JOptionPane.showMessageDialog(this,
                        "사용자 추가 실패: " + msg,
                        "오류",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "알 수 없는 응답: " + response,
                        "오류",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인하세요.",
                    "통신 오류",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_addButtonActionPerformed

    private void userIdFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userIdFieldActionPerformed
    }//GEN-LAST:event_userIdFieldActionPerformed

    private void userPasswordFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userPasswordFieldActionPerformed
    }//GEN-LAST:event_userPasswordFieldActionPerformed

    // ====== 사용자 삭제 ======
    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "삭제할 사용자를 목록에서 선택해주세요.",
                    "선택 오류",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        String userIdToDelete = model.getValueAt(selectedRow, 0).toString(); // 0번 컬럼: ID

        int confirm = JOptionPane.showConfirmDialog(this,
                "사용자 ID '" + userIdToDelete + "'를 정말 삭제하시겠습니까?",
                "삭제 확인",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            // 프로토콜: DELETE_USER|id
            String cmd = "DELETE_USER|" + userIdToDelete;
            String response = conn.sendAndReceive(cmd);

            if (response == null) {
                JOptionPane.showMessageDialog(this,
                        "서버 응답이 없습니다.",
                        "통신 오류",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (response.startsWith("OK|")) {
                JOptionPane.showMessageDialog(this,
                        "사용자 '" + userIdToDelete + "' 삭제 성공.",
                        "성공",
                        JOptionPane.INFORMATION_MESSAGE);

                loadUserList(); // 전체 목록 다시 로드

            } else if (response.startsWith("FAIL|")) {
                String msg = response.substring("FAIL|".length());
                JOptionPane.showMessageDialog(this,
                        "삭제 실패: " + msg,
                        "오류",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "알 수 없는 응답: " + response,
                        "오류",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인하세요.",
                    "통신 오류",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ====== 사용자 목록 조회 / 테이블 표시 ======
    private void loadUserList() {
        try (ClientConnection conn = new ClientConnection("localhost", 5555)) {
            String resp = conn.sendAndReceive("GET_USERS");

            if (resp == null) {
                JOptionPane.showMessageDialog(this, "서버 응답이 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!resp.startsWith("USERS|")) {
                if (resp.startsWith("FAIL|")) {
                    JOptionPane.showMessageDialog(this,
                            "사용자 목록 조회 실패: " + resp.substring("FAIL|".length()),
                            "오류",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "알 수 없는 응답: " + resp,
                            "오류",
                            JOptionPane.ERROR_MESSAGE);
                }
                return;
            }

            String data = resp.substring("USERS|".length());
            String[] rows = data.isEmpty() ? new String[0] : data.split(";");

            DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "이름", "역할", "전화번호"}, 0);
            for (String row : rows) {
                if (row.isBlank()) {
                    continue;
                }
                String[] parts = row.split(",");
                String id = parts.length > 0 ? parts[0].trim() : "";
                String name = parts.length > 1 ? parts[1].trim() : "";
                String role = parts.length > 2 ? parts[2].trim() : "";
                String phone = parts.length > 3 ? parts[3].trim() : "";
                model.addRow(new Object[]{id, name, role, phone});
            }

            userTable.setModel(model);

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "목록 로드 실패: " + e.getMessage(),
                    "오류",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton refreshButton;
    private javax.swing.JComboBox<String> roleComboBox;
    private javax.swing.JTextField userIdField;
    private javax.swing.JPasswordField userPasswordField;
    private javax.swing.JTable userTable;
    // End of variables declaration//GEN-END:variables
}
