package QuanLiNhaTro;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {

    private JPasswordField txtOld, txtNew, txtConfirm;
    private JPanel  pnlMsg;
    private JLabel  lblMsg;

    public ChangePasswordDialog(Frame owner) {
        super(owner, "Đổi mật khẩu", true);
        setSize(420, 480);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(23, 32, 24, 32));

        // ── Tiêu đề ────────────────────────────────────────────────
        JLabel title = new JLabel("Đổi mật khẩu");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.SLATE_900);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Nhập mật khẩu cũ và mật khẩu mới để cập nhật.");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.SLATE_500);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Các trường ─────────────────────────────────────────────
        txtOld     = passField();
        txtNew     = passField();
        txtConfirm = passField();

        // ── Khung thông báo ────────────────────────────────────────
        pnlMsg = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        pnlMsg.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        pnlMsg.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblMsg = new JLabel();
        lblMsg.setFont(Theme.FONT_SMALL);
        pnlMsg.add(new JLabel("●  ") {{
            setFont(new Font("Segoe UI", Font.BOLD, 10));
        }});
        pnlMsg.add(lblMsg);
        clearMsg();

        // ── Nút ────────────────────────────────────────────────────
        JButton btnSave = Theme.primaryButton("Lưu mật khẩu mới");
        btnSave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        btnSave.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSave.addActionListener(e -> save());

        JButton btnCancel = Theme.secondaryButton("Hủy");
        btnCancel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnCancel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCancel.addActionListener(e -> dispose());

        // ── Lắp ráp ────────────────────────────────────────────────
        root.add(title);
        root.add(Box.createVerticalStrut(4));
        root.add(sub);
        root.add(Box.createVerticalStrut(22));
        root.add(row("Mật khẩu hiện tại",  txtOld));
        root.add(Box.createVerticalStrut(12));
        root.add(row("Mật khẩu mới",        txtNew));
        root.add(Box.createVerticalStrut(12));
        root.add(row("Xác nhận mật khẩu mới", txtConfirm));
        root.add(Box.createVerticalStrut(14));
        root.add(pnlMsg);
        root.add(Box.createVerticalStrut(14));
        root.add(btnSave);
        root.add(Box.createVerticalStrut(8));
        root.add(btnCancel);

        setContentPane(root);
    }

    // ── Logic lưu ─────────────────────────────────────────────────

    private void save() {
        String oldPass  = new String(txtOld.getPassword());
        String newPass  = new String(txtNew.getPassword());
        String confirm  = new String(txtConfirm.getPassword());
        String username = UserSession.currentUser.getTenDangNhap();

        clearMsg();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            showMsg(Theme.DANGER, Theme.DANGER_BG, "Vui lòng điền đầy đủ tất cả các trường.");
            return;
        }
        if (TaiKhoanDAO.login(username, oldPass) == null) {
            showMsg(Theme.DANGER, Theme.DANGER_BG, "Mật khẩu hiện tại không đúng.");
            txtOld.setText("");
            txtOld.requestFocus();
            return;
        }
        if (newPass.length() < 6) {
            showMsg(Theme.DANGER, Theme.DANGER_BG, "Mật khẩu mới phải có ít nhất 6 ký tự.");
            return;
        }
        if (newPass.equals(oldPass)) {
            showMsg(Theme.DANGER, Theme.DANGER_BG, "Mật khẩu mới phải khác mật khẩu cũ.");
            return;
        }
        if (!newPass.equals(confirm)) {
            showMsg(Theme.DANGER, Theme.DANGER_BG, "Xác nhận mật khẩu không khớp.");
            txtConfirm.setText("");
            txtConfirm.requestFocus();
            return;
        }

        if (TaiKhoanDAO.updatePassword(username, newPass)) {
            showMsg(Theme.SUCCESS, Theme.SUCCESS_BG, "Đổi mật khẩu thành công!");
            txtOld.setText("");
            txtNew.setText("");
            txtConfirm.setText("");
        } else {
            showMsg(Theme.DANGER, Theme.DANGER_BG, "Có lỗi xảy ra, vui lòng thử lại.");
        }
    }

    private void showMsg(Color fg, Color bg, String text) {
        pnlMsg.setOpaque(true);
        pnlMsg.setBackground(bg);
        pnlMsg.setBorder(new CompoundBorder(
            Theme.roundedLineBorder(fg.brighter(), 1, 8),
            new EmptyBorder(2, 4, 2, 4)
        ));
        // Cập nhật màu icon bullet
        ((JLabel) pnlMsg.getComponent(0)).setForeground(fg);
        lblMsg.setForeground(fg);
        lblMsg.setText(text);
        pnlMsg.revalidate();
        pnlMsg.repaint();
    }

    /** Giữ nguyên chỗ trống của khung thông báo (không ẩn hẳn) để tránh đẩy lệch nút "Lưu". */
    private void clearMsg() {
        pnlMsg.setOpaque(false);
        pnlMsg.setBorder(null);
        ((JLabel) pnlMsg.getComponent(0)).setForeground(Color.WHITE);
        lblMsg.setForeground(Color.WHITE);
        lblMsg.setText(" ");
        pnlMsg.revalidate();
        pnlMsg.repaint();
    }

    // ── Helpers ───────────────────────────────────────────────────

    private static JPasswordField passField() {
        JPasswordField pf = new JPasswordField();
        Theme.styleTextField(pf);
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        pf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return pf;
    }

    private static JPanel row(String labelText, JComponent field) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(Theme.FONT_LABEL);
        lbl.setForeground(Theme.SLATE_700);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(lbl);
        p.add(Box.createVerticalStrut(5));
        p.add(field);
        return p;
    }
}
