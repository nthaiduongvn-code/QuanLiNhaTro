package QuanLiNhaTro;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class Panel_Dashboard extends JPanel {

    private final JLabel valTotalPhong = bigVal("0");
    private final JLabel valKhachThue  = bigVal("0");
    private final JLabel valHopDong    = bigVal("0");
    private final JLabel valCanThu     = bigValColor("–", Theme.DANGER);

    private final DefaultTableModel modelHetHan = new DefaultTableModel(
            new Object[]{"Mã HĐ", "Phòng", "Khách thuê", "Ngày kết thúc", "Còn lại"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tableHetHan = new JTable(modelHetHan);

    private final DefaultTableModel modelChuaTT = new DefaultTableModel(
            new Object[]{"Phòng", "Khách thuê", "Số tiền"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tableChuaTT = new JTable(modelChuaTT);

    private final ChartPanel chart          = new ChartPanel();
    private final DonutPanel donutPhong     = new DonutPanel(
            "Tỷ lệ lấp đầy phòng",
            Theme.TEAL_600, Theme.DANGER,
            "Đang thuê", "Phòng trống", "phòng");
    private final DonutPanel donutThanhToan = new DonutPanel(
            "Tình trạng thanh toán",
            Theme.TEAL_600, Theme.DANGER,
            "Đã thanh toán", "Chưa thanh toán", "HĐ");

    public Panel_Dashboard() {
        setBackground(Theme.SLATE_50);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // ── Header ──
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Tổng quan");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.SLATE_900);

        LocalDate now = LocalDate.now();
        JLabel sub = new JLabel("Kỳ: " + String.format("%02d-%04d", now.getMonthValue(), now.getYear())
                + "  •  Cập nhật khi mở trang");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.SLATE_500);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(3));
        titleBox.add(sub);
        header.add(titleBox, BorderLayout.WEST);

        JButton btnRefresh = Theme.secondaryButton(" Cập nhật");
        URL reloadUrl = Panel_Dashboard.class.getResource("icon/icon_reload.png");
        if (reloadUrl != null) {
            Image img = new ImageIcon(reloadUrl).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btnRefresh.setIcon(new ImageIcon(img));
        }
        btnRefresh.addActionListener(e -> {
            refresh();
            JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(Panel_Dashboard.this),
                    "Dữ liệu đã được cập nhật!",
                    "Cập nhật",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        header.add(btnRefresh, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Center (GridBagLayout) ──
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(12, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill      = GridBagConstraints.BOTH;
        gbc.weightx   = 1.0;
        gbc.gridx     = 0;
        gbc.gridwidth = 1;

        // ── Hàng 1: 4 stat cards (compact, không giãn) ──
        JPanel row1 = new JPanel(new GridLayout(1, 4, 12, 0));
        row1.setOpaque(false);
        row1.add(statCard("Tổng số phòng",      null,   valTotalPhong, Theme.INFO,     Theme.INFO_BG,    1));
        row1.add(statCard("Khách thuê",          null,  valKhachThue,  Theme.TEAL_600, Theme.TEAL_50,    2));
        row1.add(statCard("Hợp đồng hoạt động", null,   valHopDong,    Theme.WARNING,  Theme.WARNING_BG, 3));
        row1.add(statCard("Cần phải thu",        null,  valCanThu,     Theme.DANGER,   Theme.DANGER_BG,  4));

        gbc.gridy   = 0;
        gbc.weighty = 0.0;
        gbc.insets  = new Insets(0, 0, 12, 0);
        center.add(row1, gbc);

        // ── Hàng 2: chart cột + 2 donut ──
        JPanel rowCharts = new JPanel(new GridLayout(1, 2, 14, 0));
        rowCharts.setOpaque(false);
        rowCharts.setPreferredSize(new Dimension(0, 0)); // Ép GridBagLayout chia không gian theo weight, tránh bị JScrollPane của table chèn ép

        JPanel chartCard = card();
        chartCard.setLayout(new BorderLayout(0, 8));
        JLabel chartTitle = sectionLabel("Cơ cấu doanh thu 6 tháng gần nhất");
        chartCard.add(chartTitle, BorderLayout.NORTH);
        chartCard.add(chart, BorderLayout.CENTER);
        rowCharts.add(chartCard);

        JPanel donutCard = card();
        donutCard.setLayout(new GridLayout(1, 2, 10, 0));
        donutCard.add(donutPhong);
        donutCard.add(donutThanhToan);
        rowCharts.add(donutCard);

        gbc.gridy   = 1;
        gbc.weighty = 2.5;
        gbc.insets  = new Insets(0, 0, 12, 0);
        center.add(rowCharts, gbc);

        // ── Hàng 3: 2 bảng nằm ngang ──
        JPanel rowTables = new JPanel(new GridLayout(1, 2, 14, 0));
        rowTables.setOpaque(false);
        rowTables.setPreferredSize(new Dimension(0, 0)); // Ép GridBagLayout chia không gian theo weight, tránh bị JScrollPane của table chèn ép

        JPanel hetHanCard = card();
        hetHanCard.setLayout(new BorderLayout(0, 8));
        hetHanCard.add(sectionLabel("- Hợp đồng sắp hết hạn (≤ 30 ngày) : "), BorderLayout.NORTH);
        Theme.styleTable(tableHetHan);
        tableHetHan.getColumnModel().getColumn(0).setMinWidth(0);
        tableHetHan.getColumnModel().getColumn(0).setMaxWidth(0);
        tableHetHan.getColumnModel().getColumn(0).setPreferredWidth(0);
        tableHetHan.getColumnModel().getColumn(4).setMaxWidth(80);
        tableHetHan.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                int n = 0;
                try { n = Integer.parseInt(v.toString()); } catch (Exception ignore) {}
                l.setForeground(n <= 7 ? Theme.DANGER : n <= 14 ? Theme.WARNING : Theme.SLATE_700);
                l.setText(v + " ngày");
                l.setHorizontalAlignment(SwingConstants.CENTER);
                return l;
            }
        });
        hetHanCard.add(Theme.wrapScroll(tableHetHan), BorderLayout.CENTER);
        rowTables.add(hetHanCard);

        JPanel chuaTTCard = card();
        chuaTTCard.setLayout(new BorderLayout(0, 8));
        chuaTTCard.add(sectionLabel("- Phòng chưa thanh toán : "), BorderLayout.NORTH);
        Theme.styleTable(tableChuaTT);
        tableChuaTT.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setForeground(Theme.DANGER);
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                return l;
            }
        });
        chuaTTCard.add(Theme.wrapScroll(tableChuaTT), BorderLayout.CENTER);
        rowTables.add(chuaTTCard);

        gbc.gridy   = 2;
        gbc.weighty = 2.0;
        gbc.insets  = new Insets(0, 0, 0, 0);
        center.add(rowTables, gbc);

        add(center, BorderLayout.CENTER);

        // Khi cửa sổ phóng to/thu nhỏ, các custom-painted panel cần repaint tường minh
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                chart.repaint();
                donutPhong.repaint();
                donutThanhToan.repaint();
            }
        });

        // Trì hoãn refresh() đến sau khi layout hoàn tất để chart có kích thước thực
        SwingUtilities.invokeLater(this::refresh);
    }

    public void refresh() {
        try {
            int[] p = TongHopHoaDonDAO.thongKePhong();
            valTotalPhong.setText(String.valueOf(p[0]));
            donutPhong.setData(p[1], p[0]);

            valKhachThue.setText(String.valueOf(TongHopHoaDonDAO.demKhachThue()));
            valHopDong.setText(String.valueOf(TongHopHoaDonDAO.demHopDongConHieuLuc()));
            valCanThu.setText(fmtTien(TongHopHoaDonDAO.tongTienChuaThanhToanTatCaThang()));

            LocalDate now = LocalDate.now();
            String thangNam = String.format("%02d-%04d", now.getMonthValue(), now.getYear());

            int[] tt = TongHopHoaDonDAO.thongKeTrangThaiCount(thangNam);
            donutThanhToan.setData(tt[0], tt[0] + tt[1]);

            modelHetHan.setRowCount(0);
            for (Object[] r : HopDongDAO.getHopDongSapHetHan(30)) modelHetHan.addRow(r);

            modelChuaTT.setRowCount(0);
            for (Object[] r : TongHopHoaDonDAO.getPhongChuaThanhToan(thangNam))
                modelChuaTT.addRow(new Object[]{r[0], r[1], String.format("%,d đ", r[2])});

            chart.setData(TongHopHoaDonDAO.doanhThu6ThangChiTiet());
            chart.repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Helpers ──

    /** Card trắng bo góc, padding nhỏ gọn. */
    private JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(Theme.cardBorder());
        return p;
    }

    /** Tiêu đề đồng nhất cho tất cả card. */
    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_LABEL);
        l.setForeground(Theme.SLATE_800);
        return l;
    }

    /** Stat card — icon tự vẽ bằng Graphics2D, padding thoáng, sub-label tùy chọn. */
    private JPanel statCard(String label, String subLabel, JLabel valueLabel,
                             Color accent, Color accentBg, int iconType) {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(
                new Theme.LineBorderRounded(Theme.SLATE_200, 1, 12),
                new EmptyBorder(14, 14, 14, 14)));

        JComponent ic = buildStatIcon(accent, accentBg, iconType);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.SLATE_500);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        valueLabel.setAlignmentX(LEFT_ALIGNMENT);

        right.add(lbl);
        right.add(Box.createVerticalStrut(4));
        right.add(valueLabel);

        if (subLabel != null) {
            JLabel sub = new JLabel(subLabel);
            sub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            sub.setForeground(Theme.SLATE_400);
            sub.setAlignmentX(LEFT_ALIGNMENT);
            right.add(Box.createVerticalStrut(2));
            right.add(sub);
        }

        p.add(ic,    BorderLayout.WEST);
        p.add(right, BorderLayout.CENTER);
        return p;
    }

    private JComponent buildStatIcon(Color accent, Color accentBg, int type) {
        return new JComponent() {
            {
                Dimension d = new Dimension(50, 50);
                setPreferredSize(d); setMinimumSize(d); setMaximumSize(d);
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                // Nền icon
                g2.setColor(accentBg);
                g2.fillRoundRect(3, 3, 44, 44, 14, 14);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = 25, cy = 26;
                switch (type) {
                    case 1: // Nhà — tổng số phòng
                        g2.drawLine(cx - 12, cy - 2,  cx,      cy - 13);
                        g2.drawLine(cx,      cy - 13, cx + 12, cy - 2);
                        g2.drawLine(cx - 9,  cy - 2,  cx - 9,  cy + 11);
                        g2.drawLine(cx + 9,  cy - 2,  cx + 9,  cy + 11);
                        g2.drawLine(cx - 9,  cy + 11, cx + 9,  cy + 11);
                        g2.drawLine(cx - 9,  cy - 2,  cx + 9,  cy - 2);
                        g2.drawRoundRect(cx - 4, cy + 3, 8, 8, 2, 2); // cửa
                        break;
                    case 2: // Người — khách thuê
                        g2.drawOval(cx - 11, cy - 13, 9, 9);
                        g2.drawArc( cx - 15, cy - 4,  14, 11, 0, 180);
                        g2.drawOval(cx + 2,  cy - 13, 9, 9);
                        g2.drawArc( cx - 1,  cy - 4,  14, 11, 0, 180);
                        break;
                    case 3: // Tài liệu — hợp đồng
                        g2.drawRoundRect(cx - 9, cy - 13, 18, 24, 4, 4);
                        g2.drawLine(cx - 5, cy - 7,  cx + 5, cy - 7);
                        g2.drawLine(cx - 5, cy - 2,  cx + 5, cy - 2);
                        g2.drawLine(cx - 5, cy + 3,  cx + 2, cy + 3);
                        break;
                    case 4: // Tiền — cần phải thu
                        g2.drawOval(cx - 10, cy - 10, 20, 20);
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                        FontMetrics fm = g2.getFontMetrics();
                        String s = "đ";
                        g2.drawString(s, cx - fm.stringWidth(s) / 2, cy + fm.getAscent() / 2 - 1);
                        break;
                }
                g2.dispose();
            }
        };
    }

    private static JLabel bigVal(String s) {
        JLabel l = new JLabel(s);
        l.setFont(new Font("Segoe UI", Font.BOLD, 20));
        l.setForeground(Theme.SLATE_900);
        return l;
    }

    private static JLabel bigValColor(String s, Color color) {
        JLabel l = bigVal(s);
        l.setForeground(color);
        return l;
    }

    private static String fmtTien(long v) {
        return String.format("%,d", v).replace(',', '.') + "đ";
    }

    // ── Biểu đồ cột xếp chồng ──
    private static class ChartPanel extends JPanel {
        private static final Color C_PHONG    = Theme.TEAL_500;
        private static final Color C_DIENNUOC = new Color(0xF59E0B);
        private static final Color C_DICHVU   = new Color(0x6366F1);
        private static final int   N = 6;

        private final List<String> labels   = new ArrayList<>();
        private final long[]       phong    = new long[N];
        private final long[]       dienNuoc = new long[N];
        private final long[]       dichVu   = new long[N];

        ChartPanel() { setBackground(Color.WHITE); }

        void setData(ArrayList<Object[]> d) {
            labels.clear();
            int size = Math.min(d.size(), N);
            for (int i = 0; i < N; i++) { phong[i] = 0; dienNuoc[i] = 0; dichVu[i] = 0; }
            for (int i = 0; i < size; i++) {
                Object[] row = d.get(size - 1 - i);
                labels.add((String) row[0]);
                phong[i]    = (long) row[1];
                dienNuoc[i] = (long) row[2];
                dichVu[i]   = (long) row[3];
            }
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (labels.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int W = getWidth(), H = getHeight();
            int padT = 20, padB = 44, padL = 58, padR = 12;
            int cW = W - padL - padR, cH = H - padT - padB;
            if (cW < 20 || cH < 20) { g2.dispose(); return; }

            int n = labels.size();
            long max = 1;
            for (int i = 0; i < n; i++) {
                long t = phong[i] + dienNuoc[i] + dichVu[i];
                if (t > max) max = t;
            }

            g2.setFont(Theme.FONT_SMALL);
            FontMetrics fm = g2.getFontMetrics();

            for (int gi = 0; gi <= 4; gi++) {
                int y = padT + cH - (int)(cH * gi / 4.0);
                g2.setColor(gi == 0 ? Theme.SLATE_300 : Theme.SLATE_100);
                g2.drawLine(padL, y, padL + cW, y);
                String lbl = fmt(max * gi / 4);
                g2.setColor(Theme.SLATE_400);
                g2.drawString(lbl, padL - fm.stringWidth(lbl) - 5, y + fm.getAscent() / 2 - 1);
            }
            g2.setColor(Theme.SLATE_200);
            g2.drawLine(padL, padT, padL, padT + cH);

            int gap = 14;
            int optimalBarW = (cW - gap * 7) / 6;
            int barW = Math.min(optimalBarW, 60);

            int totalContentWidth = n * barW + (n - 1) * gap;
            int startX = padL + (cW - totalContentWidth) / 2;
            int base = padT + cH;

            for (int i = 0; i < n; i++) {
                long tp = phong[i], td = dienNuoc[i], tv = dichVu[i], tot = tp + td + tv;
                int hP = (int)(tp * cH / (double) max);
                int hD = (int)(td * cH / (double) max);
                int hV = (int)(tv * cH / (double) max);
                int th = hP + hD + hV;
                int x  = startX + i * (barW + gap);

                if (th > 0) {
                    Shape cl = g2.getClip();
                    g2.setClip(new RoundRectangle2D.Float(x, base - th, barW, th, 6, 6));
                    if (hP > 0) { g2.setColor(C_PHONG);    g2.fillRect(x, base - hP,       barW, hP); }
                    if (hD > 0) { g2.setColor(C_DIENNUOC); g2.fillRect(x, base - hP - hD,  barW, hD); }
                    if (hV > 0) { g2.setColor(C_DICHVU);   g2.fillRect(x, base - th,        barW, hV); }
                    g2.setClip(cl);
                    String ts = fmt(tot);
                    g2.setColor(Theme.SLATE_700);
                    g2.drawString(ts, x + (barW - fm.stringWidth(ts)) / 2, base - th - 3);
                }
                String lbl = labels.get(i);
                g2.setColor(Theme.SLATE_500);
                g2.drawString(lbl, x + (barW - fm.stringWidth(lbl)) / 2, base + 13);
            }

            int lx = padL, ly = H - 10;
            leg(g2, fm, lx,       ly, C_PHONG,    "Tiền phòng");
            leg(g2, fm, lx + 90,  ly, C_DIENNUOC, "Điện + Nước");
            leg(g2, fm, lx + 192, ly, C_DICHVU,   "Dịch vụ");
            g2.dispose();
        }

        private void leg(Graphics2D g2, FontMetrics fm, int x, int y, Color c, String s) {
            g2.setColor(c);  g2.fillRoundRect(x, y - 9, 10, 10, 3, 3);
            g2.setColor(Theme.SLATE_600); g2.drawString(s, x + 14, y);
        }

        private String fmt(long v) {
            if (v >= 1_000_000) return String.format("%.1fM", v / 1_000_000.0);
            if (v >= 1_000)     return String.format("%dK",   v / 1000);
            return String.valueOf(v);
        }
    }

    // ── Biểu đồ tròn donut ──
    private static class DonutPanel extends JPanel {
        private int value = 0, total = 0;
        private final Color  cP, cS;
        private final String lP, lS, unit, title;

        DonutPanel(String title, Color primary, Color secondary,
                   String lP, String lS, String unit) {
            this.title = title; this.cP = primary; this.cS = secondary;
            this.lP = lP; this.lS = lS; this.unit = unit;
            setBackground(Color.WHITE);
        }

        void setData(int v, int t) { value = v; total = t; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int W = getWidth(), H = getHeight();
            if (W < 20 || H < 40) { g2.dispose(); return; }

            // ── Tiêu đề ──
            g2.setFont(Theme.FONT_SMALL_B);
            FontMetrics fmT = g2.getFontMetrics();
            g2.setColor(Theme.SLATE_700);
            g2.drawString(title, (W - fmT.stringWidth(title)) / 2, 8 + fmT.getAscent());
            int titleBottom = 8 + fmT.getHeight() + 4;

            // ── Legend ở đáy (2 dòng) ──
            g2.setFont(Theme.FONT_SMALL);
            FontMetrics fmL = g2.getFontMetrics();
            int legH = fmL.getHeight() * 2 + 14;
            int legTop = H - legH;

            // ── Donut ──
            int arcH = legTop - titleBottom - 6;
            int arcSize = Math.min(W - 20, arcH);
            if (arcSize < 16) { g2.dispose(); return; }

            int ax = (W - arcSize) / 2;
            int ay = titleBottom + (arcH - arcSize) / 2;

            g2.setColor(total > 0 ? cS : Theme.SLATE_100);
            g2.fillOval(ax, ay, arcSize, arcSize);

            if (total > 0 && value > 0) {
                g2.setColor(cP);
                g2.fillArc(ax, ay, arcSize, arcSize, 90,
                        -Math.min(360, (int) Math.round(360.0 * value / total)));
            }

            int hole = (int)(arcSize * 0.65);
            g2.setColor(Color.WHITE);
            g2.fillOval(ax + (arcSize - hole) / 2, ay + (arcSize - hole) / 2, hole, hole);

            // ── Chữ giữa ──
            int cx = ax + arcSize / 2, cy = ay + arcSize / 2;
            String pct = total > 0 ? String.format("%.1f%%", 100.0 * value / total) : "–";
            int fs = Math.max(11, Math.min(20, arcSize / 6));
            g2.setFont(new Font("Segoe UI", Font.BOLD, fs));
            FontMetrics fmPct = g2.getFontMetrics();
            g2.setColor(Theme.SLATE_900);
            int pctY = cy + fmPct.getAscent() / 2 - 2;
            g2.drawString(pct, cx - fmPct.stringWidth(pct) / 2, pctY);

            g2.setFont(Theme.FONT_SMALL);
            FontMetrics fmSub = g2.getFontMetrics();
            g2.setColor(Theme.SLATE_500);
            g2.drawString(lP, cx - fmSub.stringWidth(lP) / 2,
                    pctY + fmPct.getDescent() + fmSub.getAscent());

            // ── Legend ──
            int ly = legTop + fmL.getAscent() + 4;
            legRow(g2, fmL, W, ly,                     cP, lP + ": " + value           + " " + unit);
            legRow(g2, fmL, W, ly + fmL.getHeight() + 3, cS, lS + ": " + (total - value) + " " + unit);

            g2.dispose();
        }

        private void legRow(Graphics2D g2, FontMetrics fm, int W, int y, Color c, String text) {
            int dot = 9, rowW = dot + 4 + fm.stringWidth(text);
            int lx = Math.max(2, (W - rowW) / 2);
            g2.setColor(c); g2.fillRoundRect(lx, y - dot + 2, dot, dot, 3, 3);
            g2.setColor(Theme.SLATE_600); g2.drawString(text, lx + dot + 4, y);
        }
    }
}
