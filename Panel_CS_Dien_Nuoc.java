package QuanLiNhaTro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;

public class Panel_CS_Dien_Nuoc extends JPanel {

    private static final Color DIEN_BG = new Color(0xFEF3C7); // Vàng nhạt (Amber 100) cho Điện
    private static final Color NUOC_BG = new Color(0xDBEAFE); // Xanh nhạt (Blue 100) cho Nước

    private final JComboBox<String>  cbThang = new JComboBox<>();
    private final JComboBox<Integer> cbNam   = new JComboBox<>();
    private final JLabel lblTrangThaiKy = new JLabel();
    private final JButton btnKhoiTao = Theme.warningButton("Khởi tạo tháng mới");
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Mã CS", "Phòng", "Điện/Nước", "Chỉ số cũ", "Chỉ số mới", "Đã dùng"}, 0) {
        @Override public boolean isCellEditable(int r, int c) {
            return c == 3 || c == 4;
        }
    };
    private final JTable table = new JTable(model);

    private String thangNamHienTai;
    private boolean suppressComboLoad = false;

    public Panel_CS_Dien_Nuoc() {
        setBackground(Theme.SLATE_50);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 24, 20, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);

        suppressComboLoad = true;
        khoiTaoComboBox();
        suppressComboLoad = false;
        loadData();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JLabel title = new JLabel("Chỉ số Điện - Nước");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.SLATE_900);
        header.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        Theme.styleComboBox(cbThang);
        Theme.styleComboBox(cbNam);
        cbThang.setPreferredSize(new Dimension(80, 34));
        cbNam.setPreferredSize(new Dimension(100, 34));
        cbThang.addActionListener(e -> handleComboChange());
        cbNam.addActionListener(e -> handleComboChange());

        btnKhoiTao.addActionListener(e -> khoiTaoThangMoi());

        lblTrangThaiKy.setFont(Theme.FONT_SMALL_B);

        JButton btnSave = Theme.primaryButton("Lưu");
        btnSave.addActionListener(e -> luuChiSo());

        JButton btnReload = Theme.secondaryButton("");
        URL reloadUrl = Panel_CS_Dien_Nuoc.class.getResource("icon/icon_reload.png");
        if (reloadUrl != null) {
            Image img = new ImageIcon(reloadUrl).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btnReload.setIcon(new ImageIcon(img));
        } else { btnReload.setText("🔄"); }
        btnReload.addActionListener(e -> {
            loadData();
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Panel_CS_Dien_Nuoc.this),
                    "Dữ liệu đã được cập nhật!", "Cập nhật", JOptionPane.INFORMATION_MESSAGE);
        });

        right.add(new JLabel("Tháng:"));
        right.add(cbThang);
        right.add(new JLabel("Năm:"));
        right.add(cbNam);
        right.add(lblTrangThaiKy);
        right.add(btnReload);
        right.add(btnSave);
        right.add(btnKhoiTao);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JPanel buildTable() {
        Theme.styleTable(table);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);
        table.getColumnModel().getColumn(2).setMaxWidth(90);
        table.setRowHeight(44);

        table.getColumnModel().getColumn(0).setCellRenderer(new MergePhongRenderer(false));
        table.getColumnModel().getColumn(1).setCellRenderer(new MergePhongRenderer(true));

        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                String val = String.valueOf(v);
                if ("Điện".equals(val)) l.setText("<html><font color='#D97706'><b>Điện</b></font></html>");
                else if ("Nước".equals(val)) l.setText("<html><font color='#2563EB'><b>Nước</b></font></html>");
                else l.setText(val);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setVerticalAlignment(SwingConstants.CENTER);
                l.setBackground(s ? Theme.TEAL_50 : getRowBg(r));
                return l;
            }
        });

        // Số cũ — editable
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setVerticalAlignment(SwingConstants.CENTER);
                l.setFont(Theme.FONT_BASE);
                l.setBackground(s ? Theme.TEAL_50 : lighten(getRowBg(r)));
                l.setToolTipText("Nhấp để chỉnh sửa số cũ");
                return l;
            }
        });

        // Số mới — editable, tô đỏ nếu nhỏ hơn số cũ
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setVerticalAlignment(SwingConstants.CENTER);
                String val = (v == null) ? "" : v.toString().trim();

                Integer cu  = parseIntSafe(t.getModel().getValueAt(r, 3));
                Integer moi = parseIntSafe(val.isEmpty() ? null : val);
                boolean invalid = cu != null && moi != null && moi < cu;

                if (val.isEmpty()) {
                    l.setText("Nhập số...");
                    l.setForeground(Theme.SLATE_400);
                    l.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    l.setBackground(s ? Theme.TEAL_50 : lighten(getRowBg(r)));
                    l.setToolTipText("Nhấp để nhập số mới");
                } else if (invalid) {
                    l.setForeground(Theme.DANGER);
                    l.setFont(Theme.FONT_BASE_BOLD);
                    l.setBackground(s ? Theme.TEAL_50 : Theme.DANGER_BG);
                    l.setToolTipText("⚠ Số mới không được nhỏ hơn số cũ!");
                } else {
                    l.setForeground(Theme.SLATE_900);
                    l.setFont(Theme.FONT_BASE);
                    l.setBackground(s ? Theme.TEAL_50 : lighten(getRowBg(r)));
                    l.setToolTipText("Nhấp để nhập số mới");
                }
                return l;
            }
        });

        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                Integer cu  = parseIntSafe(t.getModel().getValueAt(r, 3));
                Integer moi = parseIntSafe(t.getModel().getValueAt(r, 4));
                if (cu != null && moi != null && moi >= cu) {
                    l.setText(String.valueOf(moi - cu));
                    l.setForeground(Theme.SLATE_900);
                } else {
                    l.setText("—");
                    l.setForeground(Theme.SLATE_400);
                }
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setVerticalAlignment(SwingConstants.CENTER);
                l.setFont(Theme.FONT_BASE_BOLD);
                l.setBackground(s ? Theme.TEAL_50 : getRowBg(r));
                return l;
            }
        });

        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                table.setCursor(col == 3 || col == 4
                        ? Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR)
                        : Cursor.getDefaultCursor());
            }
        });

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 0, 0, 0));
        body.add(Theme.wrapScroll(table), BorderLayout.CENTER);
        return body;
    }

    private void khoiTaoComboBox() {
        for (int t = 1; t <= 12; t++) cbThang.addItem(String.format("%02d", t));
        for (int n : ChiSoDAO.layDanhSachNam()) cbNam.addItem(n);

        String latest = ChiSoDAO.layThangNamMoiNhat();
        if (latest != null) {
            cbThang.setSelectedItem(latest.substring(0, 2));
            cbNam.setSelectedItem(Integer.parseInt(latest.substring(3)));
        } else {
            LocalDate now = LocalDate.now();
            cbThang.setSelectedItem(String.format("%02d", now.getMonthValue()));
            cbNam.setSelectedItem(now.getYear());
        }
    }

    private void handleComboChange() {
        if (suppressComboLoad) return;
        loadData();
    }

    private void loadData() {
        if (cbThang.getSelectedItem() == null || cbNam.getSelectedItem() == null) return;
        thangNamHienTai = cbThang.getSelectedItem() + "-" + cbNam.getSelectedItem();
        model.setRowCount(0);
        ArrayList<Object[]> ds = ChiSoDAO.getDataThangNam(thangNamHienTai);
        for (Object[] row : ds) {
            model.addRow(new Object[]{row[0], row[2], "Điện", row[4], row[5], row[6]});
            model.addRow(new Object[]{row[0], row[2], "Nước", row[7], row[8], row[9]});
        }
        capNhatTrangThaiKy();
    }

    /**
     * Cập nhật badge trạng thái kỳ và bật/tắt nút "Khởi tạo tháng mới".
     * Chỉ cho phép khởi tạo kỳ mới từ kỳ gần nhất, tránh khởi tạo nhầm từ một kỳ trong quá khứ.
     */
    private void capNhatTrangThaiKy() {
        String latest = ChiSoDAO.layThangNamMoiNhat();
        boolean isLatest = thangNamHienTai.equals(latest);
        boolean exists = ChiSoDAO.kiemTraTonTai(thangNamHienTai);

        btnKhoiTao.setEnabled(isLatest);
        btnKhoiTao.setToolTipText(isLatest ? null
                : "Chỉ có thể khởi tạo tháng mới từ kỳ gần nhất (" + latest + ")");

        if (!exists) {
            lblTrangThaiKy.setForeground(Theme.SLATE_400);
            lblTrangThaiKy.setText("Kỳ này chưa có dữ liệu");
        } else if (isLatest) {
            lblTrangThaiKy.setForeground(Theme.SUCCESS);
            lblTrangThaiKy.setText("● Kỳ hiện tại");
        } else if (ChiSoDAO.coHoaDonChoKy(thangNamHienTai)) {
            lblTrangThaiKy.setForeground(Theme.SLATE_500);
            lblTrangThaiKy.setText("Đã lập hoá đơn");
        } else {
            lblTrangThaiKy.setForeground(Theme.SLATE_500);
            lblTrangThaiKy.setText("Kỳ đã qua");
        }
    }

    private void luuChiSo() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu để lưu.");
            return;
        }

        if (ChiSoDAO.coHoaDonChoKy(thangNamHienTai)) {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Kỳ " + thangNamHienTai + " đã được lập hoá đơn.\n"
                    + "Sửa chỉ số lúc này sẽ ảnh hưởng đến hóa đơn đã được lập,\n"
                    + "và có thể ảnh hưởng đến chỉ số cũ của kỳ kế tiếp.\n\n"
                    + "Bạn vẫn muốn lưu?",
                    "Kỳ đã lập hoá đơn", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok != JOptionPane.YES_OPTION) return;
        }

        // Kiểm tra toàn bộ trước khi lưu
        for (int i = 0; i < model.getRowCount(); i += 2) {
            String tenPhong = String.valueOf(model.getValueAt(i, 1));
            Integer dienCu  = parseIntSafe(model.getValueAt(i, 3));
            Integer dienMoi = parseIntSafe(model.getValueAt(i, 4));
            Integer nuocCu  = parseIntSafe(model.getValueAt(i + 1, 3));
            Integer nuocMoi = parseIntSafe(model.getValueAt(i + 1, 4));

            if (dienMoi == null) {
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(table.getCellRect(i, 4, true));
                JOptionPane.showMessageDialog(this,
                        "Phòng " + tenPhong + " — Điện:\n"
                        + "Chưa nhập chỉ số mới.",
                        "Thiếu chỉ số điện", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (nuocMoi == null) {
                table.setRowSelectionInterval(i + 1, i + 1);
                table.scrollRectToVisible(table.getCellRect(i + 1, 4, true));
                JOptionPane.showMessageDialog(this,
                        "Phòng " + tenPhong + " — Nước:\n"
                        + "Chưa nhập chỉ số mới.",
                        "Thiếu chỉ số nước", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (dienCu != null && dienMoi != null && dienMoi < dienCu) {
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(table.getCellRect(i, 4, true));
                JOptionPane.showMessageDialog(this,
                        "Phòng " + tenPhong + " — Điện:\n"
                        + "Chỉ số mới (" + dienMoi + ") không được nhỏ hơn chỉ số cũ (" + dienCu + ").",
                        "Lỗi chỉ số điện", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (nuocCu != null && nuocMoi != null && nuocMoi < nuocCu) {
                table.setRowSelectionInterval(i + 1, i + 1);
                table.scrollRectToVisible(table.getCellRect(i + 1, 4, true));
                JOptionPane.showMessageDialog(this,
                        "Phòng " + tenPhong + " — Nước:\n"
                        + "Chỉ số mới (" + nuocMoi + ") không được nhỏ hơn chỉ số cũ (" + nuocCu + ").",
                        "Lỗi chỉ số nước", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Tất cả hợp lệ → lưu
        for (int i = 0; i < model.getRowCount(); i += 2) {
            int maCS = (int) model.getValueAt(i, 0);
            Integer dienCu  = parseIntSafe(model.getValueAt(i, 3));
            Integer dienMoi = parseIntSafe(model.getValueAt(i, 4));
            Integer nuocCu  = parseIntSafe(model.getValueAt(i + 1, 3));
            Integer nuocMoi = parseIntSafe(model.getValueAt(i + 1, 4));
            ChiSoDAO.capNhatChiSo(maCS, dienCu, dienMoi, nuocCu, nuocMoi);
        }
        JOptionPane.showMessageDialog(this, "Đã lưu chỉ số cho kỳ " + thangNamHienTai + ".");
        loadData();
    }

    /**
     * Khởi tạo tháng mới: gọi Sp_KhoiTaoThangMoi qua ChiSoDAO.
     * SP tự kiểm tra: phòng hoạt động đã chốt số hợp lệ chưa, tháng sau đã tồn tại chưa.
     * Nếu SP từ chối → hiển thị thông báo lỗi; nếu thành công → chuyển sang tháng mới.
     */
    private void khoiTaoThangMoi() {
        String thangMoi = ChiSoDAO.tinhThangTiepTheo(thangNamHienTai);

        int ok = JOptionPane.showConfirmDialog(this,
                "Khởi tạo tháng mới " + thangMoi + "?\n",
                "Xác nhận khởi tạo", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            ChiSoDAO.khoiTaoThangMoi(thangNamHienTai, thangMoi);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(), "Không thể khởi tạo", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Thêm năm vào combo nếu chưa có
        int namMoi = Integer.parseInt(thangMoi.substring(3));
        suppressComboLoad = true;
        boolean hasYear = false;
        for (int i = 0; i < cbNam.getItemCount(); i++) if (cbNam.getItemAt(i) == namMoi) hasYear = true;
        if (!hasYear) cbNam.addItem(namMoi);
        cbThang.setSelectedItem(thangMoi.substring(0, 2));
        cbNam.setSelectedItem(namMoi);
        suppressComboLoad = false;

        loadData();
        JOptionPane.showMessageDialog(this,
                "Đã khởi tạo thành công tháng " + thangMoi + "!\nChỉ số cũ đã được điền sẵn từ tháng trước.");
    }

    private Integer parseIntSafe(Object o) {
        if (o == null) return null;
        String s = o.toString().trim();
        if (s.isEmpty() || s.equals("—")) return null;
        try { return Integer.parseInt(s); }
        catch (Exception e) { return null; }
    }

    private static Color getRowBg(int row) {
        return (row % 2 == 0) ? DIEN_BG : NUOC_BG;
    }

    private static Color lighten(Color c) {
        int r = Math.min(255, c.getRed()   + (255 - c.getRed())   / 2);
        int g = Math.min(255, c.getGreen() + (255 - c.getGreen()) / 2);
        int b = Math.min(255, c.getBlue()  + (255 - c.getBlue())  / 2);
        return new Color(r, g, b);
    }

    private static class MergePhongRenderer extends DefaultTableCellRenderer {
        private final boolean showFontBold;
        MergePhongRenderer(boolean showFontBold) { this.showFontBold = showFontBold; }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
            if (r % 2 == 0) {
                String txt = String.valueOf(v);
                l.setText("0".equals(txt) ? "" : txt);
                l.setFont(showFontBold ? Theme.FONT_BASE_BOLD : Theme.FONT_SMALL);
            } else {
                l.setText("");
            }
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setVerticalAlignment(SwingConstants.CENTER);
            if (!s) l.setBackground(Color.WHITE);
            return l;
        }
    }
}
