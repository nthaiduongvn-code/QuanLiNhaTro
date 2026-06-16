package QuanLiNhaTro;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class LoginForm extends JFrame {

    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private JPanel         pnlError;
    private JLabel         lblErrorMsg;

    public LoginForm() {
        super("Đăng nhập — Quản lý nhà trọ");
        setSize(900, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        setContentPane(root);

        root.add(buildLeftPanel(),  BorderLayout.WEST);
        root.add(buildRightPanel(), BorderLayout.CENTER);
    }

    // ═══════════════════════════════════════════════════════════════
    // CỘT TRÁI — background teal + thông tin app
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildLeftPanel() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setPaint(new GradientPaint(0, 0, Theme.TEAL_700, getWidth(), getHeight(), Theme.TEAL_900));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Vòng tròn trang trí — phân bố đều 4 góc
                g2.setColor(new Color(255, 255, 255, 14));
                g2.fill(new Ellipse2D.Float(-70, -70, 240, 240));
                g2.fill(new Ellipse2D.Float(getWidth() - 130, getHeight() - 150, 260, 260));
                g2.setColor(new Color(255, 255, 255, 8));
                g2.fill(new Ellipse2D.Float(getWidth() - 110, 30, 190, 190));
                g2.fill(new Ellipse2D.Float(-50, getHeight() - 130, 190, 190));
                g2.dispose();
            }
        };
        p.setPreferredSize(new Dimension(370, 0));
        p.setLayout(new GridBagLayout());

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(new EmptyBorder(0, 44, 0, 44));

        // Icon nhà
        java.awt.image.BufferedImage houseImg =
                new java.awt.image.BufferedImage(100, 100, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        {
            Graphics2D g2h = houseImg.createGraphics();
            g2h.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cx = 50, cy = 54;

            // Vòng ngoài mờ
            g2h.setColor(new Color(255, 255, 255, 14));
            g2h.fillOval(0, 0, 100, 100);
            // Vòng trong
            g2h.setColor(new Color(255, 255, 255, 30));
            g2h.fillOval(10, 10, 80, 80);

            // Mái nhà
            g2h.setColor(Color.WHITE);
            int[] rx = {cx - 34, cx, cx + 34};
            int[] ry = {cy - 2, cy - 30, cy - 2};
            g2h.fillPolygon(rx, ry, 3);

            // Ống khói
            g2h.fillRect(cx + 13, cy - 34, 8, 15);

            // Thân nhà
            g2h.fillRoundRect(cx - 26, cy - 2, 52, 32, 5, 5);

            // Cửa ra vào
            g2h.setColor(new Color(0x0D9488));
            g2h.fillRoundRect(cx - 9, cy + 10, 18, 20, 4, 4);

            // Cửa sổ
            g2h.fillRect(cx - 22, cy + 4, 10, 9);
            g2h.fillRect(cx + 12, cy + 4, 10, 9);

            g2h.dispose();
        }
        JLabel ico = new JLabel(new ImageIcon(houseImg));
        ico.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appName = new JLabel("Nhà Trọ");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 34));
        appName.setForeground(Color.WHITE);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("<html><div style='text-align:center;'>Hệ thống quản lý chuyên nghiệp</div></html>");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tagline.setForeground(new Color(255, 255, 255, 175));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        tagline.setHorizontalAlignment(SwingConstants.CENTER);

        // Đường kẻ ngang — toàn bộ chiều rộng
        JPanel divider = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(255, 255, 255, 55));
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        divider.setOpaque(false);
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);

        inner.add(ico);
        inner.add(Box.createVerticalStrut(12));
        inner.add(appName);
        inner.add(Box.createVerticalStrut(6));
        inner.add(tagline);
        inner.add(Box.createVerticalStrut(32));
        inner.add(divider);
        inner.add(Box.createVerticalStrut(26));
        inner.add(featureRow("Quản lý phòng & khách thuê"));
        inner.add(Box.createVerticalStrut(16));
        inner.add(featureRow("Hợp đồng & hóa đơn tự động"));
        inner.add(Box.createVerticalStrut(16));
        inner.add(featureRow("Theo dõi điện nước hàng tháng"));
        inner.add(Box.createVerticalStrut(16));
        inner.add(featureRow("Gửi biên lai qua email"));

        p.add(inner);
        return p;
    }

    private static JPanel featureRow(String text) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        // Icon checkmark vẽ bằng Graphics2D — nhất quán trên mọi nền tảng
        JComponent checkIcon = new JComponent() {
            @Override public Dimension getPreferredSize() { return new Dimension(24, 24); }
            @Override public Dimension getMinimumSize()   { return new Dimension(24, 24); }
            @Override public Dimension getMaximumSize()   { return new Dimension(24, 24); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 38));
                g2.fillOval(2, 2, 20, 20);
                g2.setColor(new Color(255, 255, 255, 220));
                g2.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(7, 12, 10, 16);
                g2.drawLine(10, 16, 17, 8);
                g2.dispose();
            }
        };

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(255, 255, 255, 210));

        row.add(checkIcon);
        row.add(Box.createHorizontalStrut(12));
        row.add(lbl);
        return row;
    }

    // ═══════════════════════════════════════════════════════════════
    // CỘT PHẢI — form đăng nhập
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildRightPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Color.WHITE);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        // ── Tiêu đề ────────────────────────────────────────────────
        JLabel title = new JLabel("Chào mừng trở lại!");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Theme.SLATE_900);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Đăng nhập để tiếp tục quản lý nhà trọ");
        sub.setFont(Theme.FONT_BASE);
        sub.setForeground(Theme.SLATE_500);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Trường Tên đăng nhập ───────────────────────────────────
        txtUsername = new JTextField();
        styleInput(txtUsername);

        // ── Trường Mật khẩu ────────────────────────────────────────
        txtPassword = new JPasswordField();
        styleInput(txtPassword);

        // Checkbox hiện/ẩn mật khẩu
        JCheckBox chkShow = new JCheckBox("Hiện mật khẩu");
        chkShow.setFont(Theme.FONT_SMALL);
        chkShow.setForeground(Theme.SLATE_500);
        chkShow.setOpaque(false);
        chkShow.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkShow.addItemListener(e -> {
            if (chkShow.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('●');
            }
        });

        // ── Khung thông báo lỗi ────────────────────────────────────
        pnlError = new JPanel();
        pnlError.setLayout(new BoxLayout(pnlError, BoxLayout.X_AXIS));
        pnlError.setBackground(Theme.DANGER_BG);
        pnlError.setBorder(new CompoundBorder(
            Theme.roundedLineBorder(new Color(0xFCA5A5), 1, 8),
            new EmptyBorder(10, 12, 10, 12)
        ));
        pnlError.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        pnlError.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel errIcon = new JLabel("⚠  ");
        errIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        errIcon.setForeground(Theme.DANGER);

        lblErrorMsg = new JLabel("Thông báo");
        lblErrorMsg.setFont(Theme.FONT_SMALL);
        lblErrorMsg.setForeground(new Color(0xB91C1C));

        pnlError.add(errIcon);
        pnlError.add(lblErrorMsg);
        pnlError.setVisible(false);

        // ── Nút Đăng nhập ──────────────────────────────────────────
        JButton btnLogin = Theme.primaryButton("  Đăng nhập  →");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // ── Link quên mật khẩu ─────────────────────────────────────
        JLabel lnkForgot = new JLabel("<html><u>Quên mật khẩu?</u></html>");
        lnkForgot.setFont(Theme.FONT_SMALL);
        lnkForgot.setForeground(Theme.TEAL_600);
        lnkForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lnkForgot.setAlignmentX(Component.CENTER_ALIGNMENT);
        lnkForgot.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                new ForgotPasswordDialog(LoginForm.this).setVisible(true);
            }
        });

        // ── Actions ────────────────────────────────────────────────
        ActionListener doLogin = e -> login();
        btnLogin.addActionListener(doLogin);
        txtPassword.addActionListener(doLogin);
        txtUsername.addActionListener(e -> txtPassword.requestFocus());

        // ── Lắp ráp ────────────────────────────────────────────────
        form.add(title);
        form.add(Box.createVerticalStrut(6));
        form.add(sub);
        form.add(Box.createVerticalStrut(32));
        form.add(inputLabel("Tên đăng nhập"));
        form.add(Box.createVerticalStrut(6));
        form.add(txtUsername);
        form.add(Box.createVerticalStrut(18));
        form.add(inputLabel("Mật khẩu"));
        form.add(Box.createVerticalStrut(6));
        form.add(txtPassword);
        form.add(Box.createVerticalStrut(6));
        form.add(chkShow);
        form.add(Box.createVerticalStrut(16));
        form.add(pnlError);
        form.add(Box.createVerticalStrut(16));
        form.add(btnLogin);
        form.add(Box.createVerticalStrut(18));
        form.add(lnkForgot);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets  = new Insets(50, 70, 50, 70);
        outer.add(form, gbc);
        return outer;
    }

    // ── Đăng nhập logic ───────────────────────────────────────────

    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        pnlError.setVisible(false);

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng điền đầy đủ tên đăng nhập và mật khẩu.");
            return;
        }

        TaiKhoan tk = TaiKhoanDAO.login(username, password);
        if (tk != null) {
            UserSession.currentUser = tk;
            dispose();
            SwingUtilities.invokeLater(() -> new Main().setVisible(true));
        } else {
            showError("Tên đăng nhập hoặc mật khẩu không đúng.");
            txtPassword.setText("");
            txtPassword.requestFocus();
        }
    }

    private void showError(String msg) {
        lblErrorMsg.setText(msg);
        pnlError.setVisible(true);
        pnlError.revalidate();
    }

    // ── Helpers ───────────────────────────────────────────────────

    private static void styleInput(JComponent c) {
        if (c instanceof JTextField) Theme.styleTextField((JTextField) c);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setFont(Theme.FONT_BASE);
    }

    private static JLabel inputLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_LABEL);
        lbl.setForeground(Theme.SLATE_700);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }
}
