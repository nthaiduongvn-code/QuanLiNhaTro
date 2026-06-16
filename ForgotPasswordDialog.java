package QuanLiNhaTro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.security.SecureRandom;

public class ForgotPasswordDialog extends JDialog {

    private JTextField txtUsername;
    private JLabel     lblMessage;

    public ForgotPasswordDialog(Frame owner) {
        super(owner, "Quên mật khẩu", true);
        setSize(400, 290);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(new EmptyBorder(28, 28, 24, 28));
        main.setBackground(Color.WHITE);

        JLabel title = new JLabel("Quên mật khẩu");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Theme.SLATE_900);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel desc = new JLabel("<html>Nhập tên đăng nhập (email) của bạn.<br>"
                + "Mật khẩu mới sẽ được gửi vào email đó.</html>");
        desc.setFont(Theme.FONT_SMALL);
        desc.setForeground(Theme.SLATE_500);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel("Tên đăng nhập (email)");
        lbl.setFont(Theme.FONT_LABEL);
        lbl.setForeground(Theme.SLATE_700);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername = new JTextField();
        Theme.styleTextField(txtUsername);
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblMessage = new JLabel(" ");
        lblMessage.setFont(Theme.FONT_SMALL);
        lblMessage.setForeground(Theme.DANGER);
        lblMessage.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSend = Theme.primaryButton("Gửi mật khẩu mới");
        btnSend.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnSend.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSend.addActionListener(e -> sendNewPassword());
        txtUsername.addActionListener(e -> sendNewPassword());

        main.add(title);
        main.add(Box.createVerticalStrut(6));
        main.add(desc);
        main.add(Box.createVerticalStrut(20));
        main.add(lbl);
        main.add(Box.createVerticalStrut(4));
        main.add(txtUsername);
        main.add(Box.createVerticalStrut(6));
        main.add(lblMessage);
        main.add(Box.createVerticalStrut(12));
        main.add(btnSend);

        setContentPane(main);
        getContentPane().setBackground(Color.WHITE);
    }

    private void sendNewPassword() {
        String username = txtUsername.getText().trim();

        if (username.isEmpty()) {
            setMsg(Theme.DANGER, "Vui lòng nhập tên đăng nhập.");
            return;
        }
        if (!TaiKhoanDAO.isUsernameExists(username)) {
            setMsg(Theme.DANGER, "Tên đăng nhập không tồn tại trong hệ thống.");
            return;
        }

        String newPass = generatePassword(8);

        if (!TaiKhoanDAO.updatePassword(username, newPass)) {
            setMsg(Theme.DANGER, "Có lỗi khi cập nhật mật khẩu. Vui lòng thử lại.");
            return;
        }

        boolean sent = EmailUtil.guiHoaDonHtml(username,
                "Mật khẩu mới — Quản lý nhà trọ", buildEmailBody(username, newPass));

        if (sent) {
            setMsg(Theme.SUCCESS, "✓ Mật khẩu mới đã được gửi vào email của bạn.");
        } else {
            // DB đã cập nhật nhưng gửi mail thất bại — hiện mật khẩu trực tiếp
            setMsg(Theme.WARNING,
                    "<html>Gửi email thất bại. Mật khẩu mới của bạn: <b>" + newPass + "</b></html>");
        }
    }

    private void setMsg(Color color, String text) {
        lblMessage.setForeground(color);
        lblMessage.setText(text);
    }

    private static String generatePassword(int len) {
        // Bỏ ký tự dễ nhầm: 0/O, 1/l/I
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    private static String buildEmailBody(String username, String newPassword) {
        return "<!DOCTYPE html><html><body style='margin:0;padding:0;background:#F8FAFC;'>"
            + "<div style='max-width:480px;margin:24px auto;font-family:\"Segoe UI\",Arial,sans-serif;"
            + "background:#fff;border-radius:12px;overflow:hidden;"
            + "box-shadow:0 4px 16px rgba(0,0,0,0.08);border:1px solid #E2E8F0;'>"

            + "<div style='background:linear-gradient(135deg,#0D9488 0%,#14B8A6 100%);"
            + "color:#fff;padding:28px;'>"
            + "<div style='font-size:28px;'>🏠</div>"
            + "<h2 style='margin:8px 0 0;font-size:22px;font-weight:700;'>Mật khẩu mới của bạn</h2>"
            + "</div>"

            + "<div style='padding:28px;'>"
            + "<p style='color:#334155;font-size:14px;margin-top:0;'>Xin chào <b>" + username + "</b>,</p>"
            + "<p style='color:#334155;font-size:14px;'>Mật khẩu mới của tài khoản là:</p>"

            + "<div style='background:#F0FDFA;border:1px solid #CCFBF1;border-radius:10px;"
            + "padding:20px;text-align:center;margin:16px 0;'>"
            + "<span style='font-size:28px;font-weight:700;color:#0F766E;letter-spacing:6px;'>"
            + newPassword + "</span></div>"

            + "<p style='color:#64748B;font-size:13px;'>Hãy đăng nhập và đổi lại mật khẩu để bảo mật tài khoản.</p>"
            + "</div>"

            + "<div style='padding:16px 28px;background:#F1F5F9;color:#64748B;"
            + "font-size:12px;text-align:center;border-top:1px solid #E2E8F0;'>"
            + "Quản lý nhà trọ — Email tự động, vui lòng không trả lời.</div>"
            + "</div></body></html>";
    }
}
