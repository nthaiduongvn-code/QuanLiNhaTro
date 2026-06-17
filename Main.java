package QuanLiNhaTro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame {

    private final List<NavItem> navItems = new ArrayList<>();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel();

    public Main() {
        super("Hệ thống quản lý nhà trọ");
        setSize(1340, 760);
        setMinimumSize(new Dimension(1180, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        getContentPane().setBackground(Theme.SLATE_50);
        setLayout(new BorderLayout());

        // ── SIDEBAR ─────────────────────────────────────────────
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createEmptyBorder());

        // Logo / Brand
        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBackground(Color.WHITE);
        brand.setBorder(new EmptyBorder(20, 20, 16, 20));
        brand.setAlignmentX(LEFT_ALIGNMENT);

        JLabel logo;
        java.net.URL logoUrl = Main.class.getResource("icon/icon_NhaTro.png");
        if (logoUrl != null) {
            Image img = new ImageIcon(logoUrl).getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
            logo = new JLabel(" Nhà trọ", new ImageIcon(img), SwingConstants.LEFT);
        } else {
            logo = new JLabel("Nhà trọ");
        }
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Theme.TEAL_700);
        logo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Hệ thống quản lý");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.SLATE_500);
        sub.setAlignmentX(LEFT_ALIGNMENT);

        brand.add(logo);
        brand.add(Box.createVerticalStrut(2));
        brand.add(sub);
        sidebar.add(brand);

        // Separator
        sidebar.add(divider());

        // Section label
        JLabel section = new JLabel("  Quản lý");
        section.setFont(new Font("Segoe UI", Font.BOLD, 11));
        section.setForeground(Theme.SLATE_400);
        section.setBorder(new EmptyBorder(12, 16, 6, 16));
        section.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(section);

        // Khởi tạo các panel
        Panel_ThongTinPhong[] refPhong = {null};

        Panel_ChiTietPhong panelChiTiet = new Panel_ChiTietPhong(() -> {
            if (refPhong[0] != null) refPhong[0].hienThiDanhSach();
            cardLayout.show(content, "ThongTinPhong");
            highlightActive("ThongTinPhong");
        });

        Panel_ThongTinPhong panelDsPhong = new Panel_ThongTinPhong(phong -> {
            panelChiTiet.setPhong(phong);
            cardLayout.show(content, "ChiTietPhong");
        });
        refPhong[0] = panelDsPhong;

        Panel_Dashboard panelDashboard = new Panel_Dashboard();
        Panel_Khach_Thue panelKhachThue = new Panel_Khach_Thue();
        Panel_DichVu    panelDichVu    = new Panel_DichVu();
        Panel_Hop_Dong  panelHopDong   = new Panel_Hop_Dong();
        Panel_CS_Dien_Nuoc panelChiSo  = new Panel_CS_Dien_Nuoc();
        Panel_ThanhToan panelThanhToan = new Panel_ThanhToan();

        content.setLayout(cardLayout);
        content.setBackground(Theme.SLATE_50);
        content.add(panelDashboard, "Dashboard");
        content.add(panelDsPhong, "ThongTinPhong");
        content.add(panelChiTiet, "ChiTietPhong");
        content.add(panelDichVu, "DichVu");
        content.add(panelKhachThue, "KhachThue");
        content.add(panelHopDong, "HopDong");
        content.add(panelChiSo, "ChiSoDienNuoc");
        content.add(panelThanhToan, "ThanhToan");

        // Nav items
        addNavItem(sidebar, "📊", "Tổng quan",       "Dashboard",      () -> panelDashboard.refresh());
        addNavItem(sidebar, "🏘", "Thông tin phòng", "ThongTinPhong",  panelDsPhong::hienThiDanhSach);
        addNavItem(sidebar, "👥", "Khách thuê",      "KhachThue",      panelKhachThue::hienThiDanhSach);
        addNavItem(sidebar, "📋", "Hợp đồng",        "HopDong",        panelHopDong::hienThiDanhSach);
        addNavItem(sidebar, "🧰", "Dịch vụ",          "DichVu",         panelDichVu::hienThiDanhSach);
        addNavItem(sidebar, "⚡", "Chỉ số Điện-Nước", "ChiSoDienNuoc",  null);
        addNavItem(sidebar, "💳", "Thanh toán",      "ThanhToan",      panelThanhToan::reload);

        sidebar.add(Box.createVerticalGlue());

        // ── PHẦN TÀI KHOẢN (cuối sidebar) ──────────────────────────
        sidebar.add(divider());

        JLabel sectionAcc = new JLabel("  Tài khoản");
        sectionAcc.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sectionAcc.setForeground(Theme.SLATE_400);
        sectionAcc.setBorder(new EmptyBorder(10, 16, 4, 16));
        sectionAcc.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(sectionAcc);

        // Dòng hiển thị tên đăng nhập đang đăng nhập
        String currentUser = UserSession.currentUser != null
                ? UserSession.currentUser.getTenDangNhap() : "";
        JPanel userRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        userRow.setBackground(Theme.TEAL_50);
        userRow.setBorder(new EmptyBorder(8, 16, 8, 16));
        userRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        userRow.setAlignmentX(LEFT_ALIGNMENT);

        // Avatar tròn chứa chữ đầu
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.TEAL_600);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                String ch = currentUser.isEmpty() ? "?" : String.valueOf(Character.toUpperCase(currentUser.charAt(0)));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(ch, (getWidth() - fm.stringWidth(ch)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(28, 28));

        JLabel lblUser = new JLabel(currentUser.length() > 22
                ? currentUser.substring(0, 20) + "…" : currentUser);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(Theme.TEAL_700);

        userRow.add(avatar);
        userRow.add(lblUser);
        sidebar.add(userRow);
        sidebar.add(Box.createVerticalStrut(2));

        // Nút Đổi mật khẩu
        addActionItem(sidebar, "🔑", "Đổi mật khẩu", () ->
                new ChangePasswordDialog(this).setVisible(true));

        // Nút Đăng xuất
        addActionItem(sidebar, "🚪", "Đăng xuất", () -> {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn đăng xuất không?",
                    "Đăng xuất", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                UserSession.currentUser = null;
                dispose();
                SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
            }
        });

        sidebar.add(divider());

        // ── SIDEBAR TOGGLE STRIP ────────────────────────────────
        final boolean[] sidebarOpen = {true};

        JPanel leftContainer = new JPanel(new BorderLayout());
        leftContainer.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.SLATE_200));

        JPanel toggleStrip = new JPanel(new GridBagLayout());
        toggleStrip.setPreferredSize(new Dimension(20, 0));
        toggleStrip.setBackground(Color.WHITE);
        toggleStrip.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Theme.SLATE_100));

        JPanel togglePill = new JPanel() {
            boolean hov = false;
            Image imgOpen, imgClose;
            {
                setPreferredSize(new Dimension(16, 56));
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setToolTipText("Ẩn / hiện thanh điều hướng");
                try {
                    java.net.URL urlOpen = Main.class.getResource("icon/icon_open.png");
                    java.net.URL urlClose = Main.class.getResource("icon/icon_close.png");
                    if (urlOpen != null) imgOpen = new ImageIcon(urlOpen).getImage().getScaledInstance(12, 12, Image.SCALE_SMOOTH);
                    if (urlClose != null) imgClose = new ImageIcon(urlClose).getImage().getScaledInstance(12, 12, Image.SCALE_SMOOTH);
                } catch (Exception e) {}
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    @Override public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) {
                        sidebarOpen[0] = !sidebarOpen[0];
                        sidebar.setVisible(sidebarOpen[0]);
                        repaint();
                        leftContainer.revalidate();
                    }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? Theme.TEAL_100 : Theme.SLATE_100);
                g2.fillRoundRect(1, 0, getWidth() - 2, getHeight(), 8, 8);
                
                Image img = sidebarOpen[0] ? imgClose : imgOpen;
                if (img != null) {
                    int tx = (getWidth() - img.getWidth(null)) / 2;
                    int ty = (getHeight() - img.getHeight(null)) / 2;
                    g2.drawImage(img, tx, ty, null);
                } else {
                    g2.setColor(hov ? Theme.TEAL_700 : Theme.SLATE_400);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    FontMetrics fm = g2.getFontMetrics();
                    String arrow = sidebarOpen[0] ? "<" : ">";
                    int tx = (getWidth()  - fm.stringWidth(arrow)) / 2;
                    int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(arrow, tx, ty);
                }
                g2.dispose();
            }
        };
        toggleStrip.add(togglePill);

        leftContainer.add(sidebar, BorderLayout.CENTER);
        leftContainer.add(toggleStrip, BorderLayout.EAST);

        add(leftContainer, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        // Khởi đầu: Dashboard
        cardLayout.show(content, "Dashboard");
        highlightActive("Dashboard");
    }

    /** Item hành động (không navigate card) — dùng cho phần Tài khoản. */
    private void addActionItem(JPanel sidebar, String icon, String text, Runnable action) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0)) {
            boolean hov = false;
            { // init block
                setBorder(new EmptyBorder(10, 16, 10, 16));
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
                setAlignmentX(LEFT_ALIGNMENT);
                setBackground(Color.WHITE);
                setOpaque(true);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    @Override public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) { action.run(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                g.setColor(hov ? Theme.SLATE_50 : Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));

        JLabel lblText = new JLabel(text);
        lblText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblText.setForeground(Theme.SLATE_700);

        item.add(lblIcon);
        item.add(lblText);
        sidebar.add(item);
        sidebar.add(Box.createVerticalStrut(2));
    }

    private Component divider() {
        JPanel p = new JPanel();
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        p.setBackground(Theme.SLATE_200);
        return p;
    }

    /** Thêm 1 item vào sidebar; onSelected sẽ chạy mỗi khi item được click. */
    private void addNavItem(JPanel sidebar, String icon, String text, String cardKey, Runnable onSelected) {
        NavItem item = new NavItem(icon, text, cardKey);
        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                cardLayout.show(content, cardKey);
                if (onSelected != null) onSelected.run();
                highlightActive(cardKey);
            }
        });
        navItems.add(item);
        sidebar.add(item);
        sidebar.add(Box.createVerticalStrut(2));
    }

    private void highlightActive(String activeKey) {
        for (NavItem it : navItems) {
            // Ghi nhớ: ChiTietPhong cũng highlight ThongTinPhong
            boolean active = it.cardKey.equals(activeKey)
                    || (activeKey.equals("ChiTietPhong") && it.cardKey.equals("ThongTinPhong"));
            it.setActive(active);
        }
    }

    // ──────────────────────────────────────────────────────────
    // Inner class: 1 nav item
    // ──────────────────────────────────────────────────────────
    private static class NavItem extends JPanel {
        private final JLabel lblIcon, lblText;
        private boolean active = false;
        private boolean hovered = false;
        final String cardKey;

        NavItem(String icon, String text, String cardKey) {
            this.cardKey = cardKey;
            setLayout(new FlowLayout(FlowLayout.LEFT, 12, 0));
            setBorder(new EmptyBorder(10, 16, 10, 16));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setAlignmentX(LEFT_ALIGNMENT);
            setBackground(Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setOpaque(true);

            lblIcon = new JLabel(icon);
            lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            lblIcon.setForeground(Theme.SLATE_500);

            lblText = new JLabel(text);
            lblText.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblText.setForeground(Theme.SLATE_700);

            add(lblIcon);
            add(lblText);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            if (active) {
                setBackground(Theme.TEAL_50);
                lblIcon.setForeground(Theme.TEAL_700);
                lblText.setForeground(Theme.TEAL_700);
            } else {
                setBackground(Color.WHITE);
                lblIcon.setForeground(Theme.SLATE_500);
                lblText.setForeground(Theme.SLATE_700);
            }
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Background
            if (active) {
                g2.setColor(Theme.TEAL_50);
            } else if (hovered) {
                g2.setColor(Theme.SLATE_50);
            } else {
                g2.setColor(Color.WHITE);
            }
            g2.fillRect(0, 0, getWidth(), getHeight());
            // Vạch teal bên trái nếu active
            if (active) {
                g2.setColor(Theme.TEAL_600);
                g2.fillRect(0, 4, 3, getHeight() - 8);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
