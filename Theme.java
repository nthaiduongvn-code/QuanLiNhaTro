package QuanLiNhaTro;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Theme trung tâm: màu, font, helper tạo button/border/panel.
 * Tông Teal + Slate, sáng và sạch.
 *
 * Đụng vào màu/font chỉ cần sửa ở đây.
 */
public class Theme {

    // ========== TEAL (primary) ==========
    public static final Color TEAL_50   = new Color(0xF0FDFA);
    public static final Color TEAL_100  = new Color(0xCCFBF1);
    public static final Color TEAL_300  = new Color(0x5EEAD4);
    public static final Color TEAL_500  = new Color(0x14B8A6);
    public static final Color TEAL_600  = new Color(0x0D9488);  // primary
    public static final Color TEAL_700  = new Color(0x0F766E);  // primary-hover
    public static final Color TEAL_900  = new Color(0x134E4A);

    // ========== SLATE (gray neutrals) ==========
    public static final Color SLATE_50  = new Color(0xF8FAFC);  // page background
    public static final Color SLATE_100 = new Color(0xF1F5F9);  // subtle bg
    public static final Color SLATE_200 = new Color(0xE2E8F0);  // border
    public static final Color SLATE_300 = new Color(0xCBD5E1);  // border strong
    public static final Color SLATE_400 = new Color(0x94A3B8);
    public static final Color SLATE_500 = new Color(0x64748B);  // muted text
    public static final Color SLATE_600 = new Color(0x475569);  // secondary text
    public static final Color SLATE_700 = new Color(0x334155);
    public static final Color SLATE_800 = new Color(0x1E293B);
    public static final Color SLATE_900 = new Color(0x0F172A);  // primary text

    // ========== Semantic colors ==========
    public static final Color SUCCESS   = new Color(0x10B981);  // emerald-500
    public static final Color SUCCESS_BG= new Color(0xD1FAE5);  // emerald-100
    public static final Color WARNING   = new Color(0xF59E0B);  // amber-500
    public static final Color WARNING_BG= new Color(0xFEF3C7);  // amber-100
    public static final Color DANGER    = new Color(0xEF4444);  // red-500
    public static final Color DANGER_BG = new Color(0xFEE2E2);  // red-100
    public static final Color INFO      = new Color(0x6366F1);  // indigo-500
    public static final Color INFO_BG   = new Color(0xE0E7FF);  // indigo-100

    public static final Color WHITE     = Color.WHITE;

    // ========== FONTS ==========
    public static final Font FONT_BASE      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BASE_BOLD = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_SMALL     = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SMALL_B   = new Font("Segoe UI", Font.BOLD,  12);
    public static final Font FONT_LABEL     = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_TITLE     = new Font("Segoe UI", Font.BOLD,  18);
    public static final Font FONT_HEADER    = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_HUGE      = new Font("Segoe UI", Font.BOLD,  28);
    public static final Font FONT_BTN       = new Font("Segoe UI", Font.BOLD,  13);

    // ========== Tạo Button bo góc ==========

    /** Button primary (teal đậm) — cho hành động chính. */
    public static JButton primaryButton(String text) {
        return roundedButton(text, TEAL_600, WHITE, TEAL_700);
    }

    /** Button success (xanh lá) — confirm, lưu. */
    public static JButton successButton(String text) {
        return roundedButton(text, SUCCESS, WHITE, new Color(0x059669));
    }

    /** Button danger (đỏ) — xóa, hành động phá. */
    public static JButton dangerButton(String text) {
        return roundedButton(text, DANGER, WHITE, new Color(0xDC2626));
    }

    /** Button warning (vàng cam). */
    public static JButton warningButton(String text) {
        return roundedButton(text, WARNING, WHITE, new Color(0xD97706));
    }

    /** Button info (indigo). */
    public static JButton infoButton(String text) {
        return roundedButton(text, INFO, WHITE, new Color(0x4F46E5));
    }

    /** Button phụ — nền trắng, viền xám, chữ slate. */
    public static JButton secondaryButton(String text) {
        JButton btn = new RoundedButton(text, WHITE, SLATE_700, SLATE_100);
        btn.setBorder(new CompoundBorder(
            new LineBorderRounded(SLATE_300, 1, 8),
            new EmptyBorder(6, 14, 6, 14)
        ));
        return btn;
    }

    /** Button ghost — trong suốt, chỉ chữ teal — hành động phụ, ít nhấn. */
    public static JButton ghostButton(String text) {
        JButton btn = new RoundedButton(text, SLATE_50, TEAL_700, TEAL_50);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        return btn;
    }

    private static JButton roundedButton(String text, Color bg, Color fg, Color hover) {
        JButton btn = new RoundedButton(text, bg, fg, hover);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    // ========== Border helpers ==========

    public static Border cardBorder() {
        return new CompoundBorder(
            new LineBorderRounded(SLATE_200, 1, 12),
            new EmptyBorder(14, 16, 14, 16)
        );
    }

    public static Border roundedLineBorder(Color color, int thickness, int radius) {
        return new LineBorderRounded(color, thickness, radius);
    }

    public static Border padded(int t, int l, int b, int r) {
        return new EmptyBorder(t, l, b, r);
    }

    public static Border padded(int all) {
        return new EmptyBorder(all, all, all, all);
    }

    // ========== TextField & ComboBox styling ==========

    public static void styleTextField(JTextField tf) {
        tf.setFont(FONT_BASE);
        tf.setBackground(WHITE);
        tf.setForeground(SLATE_900);
        tf.setBorder(new CompoundBorder(
            new LineBorderRounded(SLATE_300, 1, 8),
            new EmptyBorder(6, 10, 6, 10)
        ));
        tf.setCaretColor(TEAL_600);
    }

    /**
     * Bọc JTextField trong panel bo góc có icon kính lúp bên trái.
     * Gọi SAU khi đã styleTextField và setPreferredSize.
     */
    public static JPanel wrapWithSearchIcon(JTextField tf) {
        Dimension size = tf.getPreferredSize();

        // Bỏ border của field (border sẽ nằm ở wrapper)
        tf.setBorder(new EmptyBorder(0, 4, 0, 0));
        tf.setOpaque(false);

        JPanel wrapper = new JPanel(new BorderLayout(0, 0));
        wrapper.setBackground(WHITE);
        wrapper.setBorder(new CompoundBorder(
                new LineBorderRounded(SLATE_300, 1, 8),
                new EmptyBorder(0, 8, 0, 8)));
        wrapper.setPreferredSize(size);

        java.net.URL iconUrl = Theme.class.getResource("icon/ico_search.jpg");
        if (iconUrl != null) {
            Image img = new ImageIcon(iconUrl).getImage()
                    .getScaledInstance(14, 14, Image.SCALE_SMOOTH);
            JLabel ic = new JLabel(new ImageIcon(img));
            ic.setOpaque(false);
            ic.setBorder(new EmptyBorder(0, 0, 0, 4));
            wrapper.add(ic, BorderLayout.WEST);
        }
        wrapper.add(tf, BorderLayout.CENTER);
        return wrapper;
    }

    public static void styleComboBox(JComboBox<?> cb) {
        cb.setFont(FONT_BASE);
        cb.setBackground(WHITE);
        cb.setForeground(SLATE_900);
        cb.setBorder(new LineBorderRounded(SLATE_300, 1, 8));
        ((JComponent) cb.getRenderer()).setOpaque(true);
    }

    /**
     * RowFilter tìm kiếm tất cả cột, strip HTML trước khi so sánh.
     * Dùng với TableRowSorter.setRowFilter(Theme.searchFilter(query)).
     */
    public static javax.swing.RowFilter<Object, Object> searchFilter(String query) {
        if (query == null || query.trim().isEmpty()) return null;
        final String q = query.trim().toLowerCase();
        return new javax.swing.RowFilter<Object, Object>() {
            @Override
            public boolean include(Entry<? extends Object, ? extends Object> entry) {
                for (int i = 0; i < entry.getValueCount(); i++) {
                    Object val = entry.getValue(i);
                    if (val != null) {
                        String text = val.toString().replaceAll("<[^>]+>", "").toLowerCase();
                        if (text.contains(q)) return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * Kiểm tra q có khớp với bất kỳ giá trị nào không, strip HTML.
     * Dùng cho các panel filter bằng ArrayList (không dùng TableRowSorter).
     */
    public static boolean matchesSearch(String q, Object... values) {
        if (q == null || q.isEmpty()) return true;
        for (Object val : values) {
            if (val != null) {
                String text = val.toString().replaceAll("<[^>]+>", "").toLowerCase();
                if (text.contains(q)) return true;
            }
        }
        return false;
    }

    public static void styleTable(JTable t) {
        t.setFont(FONT_BASE);
        t.setRowHeight(36);
        t.setBackground(WHITE);
        t.setForeground(SLATE_900);
        t.setGridColor(SLATE_100);
        t.setShowVerticalLines(false);
        t.setSelectionBackground(TEAL_50);
        t.setSelectionForeground(SLATE_900);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);

        var header = t.getTableHeader();
        header.setFont(FONT_BASE_BOLD);
        header.setBackground(SLATE_100);
        header.setForeground(SLATE_700);
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, SLATE_200));
    }

    /** Bọc một JScrollPane bằng style mới (border bo + scrollbar sáng). */
    public static JScrollPane wrapScroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(new LineBorderRounded(SLATE_200, 1, 10));
        sp.getViewport().setBackground(WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getVerticalScrollBar().setUI(new SoftScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new SoftScrollBarUI());
        return sp;
    }

    // ========== Render trạng thái dạng "pill" (chip) ==========

    /** Tạo HTML "pill" hiển thị trạng thái — dùng trong JLabel/JTable cell. */
    public static String pill(String text, String bgHex, String fgHex) {
        return "<html><div style='background:" + bgHex + ";color:" + fgHex + ";"
             + "padding:3px 10px;border-radius:10px;font-weight:bold;font-size:11px;"
             + "display:inline-block;'>" + text + "</div></html>";
    }

    public static String pillSuccess(String t) { return pill(t, "#D1FAE5", "#047857"); }
    public static String pillWarning(String t) { return pill(t, "#FEF3C7", "#B45309"); }
    public static String pillDanger (String t) { return pill(t, "#FEE2E2", "#B91C1C"); }
    public static String pillInfo   (String t) { return pill(t, "#E0E7FF", "#4338CA"); }
    public static String pillNeutral(String t) { return pill(t, "#F1F5F9", "#475569"); }

    // ========== Inner class: Button bo góc tự vẽ ==========
    public static class RoundedButton extends JButton {
        private final Color bgNormal;
        private final Color bgHover;
        private boolean hovered = false;
        private boolean pressed = false;

        public RoundedButton(String text, Color bgNormal, Color fg, Color bgHover) {
            super(text);
            this.bgNormal = bgNormal;
            this.bgHover  = bgHover;
            setForeground(fg);
            setFont(FONT_BTN);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited (MouseEvent e) { hovered = false; pressed = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
                @Override public void mouseReleased(MouseEvent e){ pressed = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color use = !isEnabled() ? SLATE_300
                       : pressed     ? bgHover.darker()
                       : hovered     ? bgHover
                                     : bgNormal;
            g2.setColor(use);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ========== Inner class: Line border bo góc ==========
    public static class LineBorderRounded extends AbstractBorder {
        private final Color color;
        private final int thickness;
        private final int radius;

        public LineBorderRounded(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f,
                    w - thickness, h - thickness, radius, radius));
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c) {
            int p = thickness + 2;
            return new Insets(p, p, p, p);
        }

        @Override public Insets getBorderInsets(Component c, Insets i) {
            int p = thickness + 2;
            i.set(p, p, p, p);
            return i;
        }
    }

    // ========== Inner class: ScrollBar sáng ==========
    private static class SoftScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            this.thumbColor = SLATE_300;
            this.trackColor = SLATE_50;
        }
        @Override protected JButton createDecreaseButton(int orientation) { return zeroBtn(); }
        @Override protected JButton createIncreaseButton(int orientation) { return zeroBtn(); }
        private JButton zeroBtn() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackColor);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
    }
}
