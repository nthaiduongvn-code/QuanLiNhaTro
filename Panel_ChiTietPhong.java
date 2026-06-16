package QuanLiNhaTro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class Panel_ChiTietPhong extends JPanel {

    private final JLabel lblTenPhong  = new JLabel("—");
    private final JLabel lblDienTich  = new JLabel("—");
    private final JLabel lblGiaThue   = new JLabel("—");
    private final JLabel lblTrangThai = new JLabel("—");

    // Tab Khách thuê
    private JPanel pnlThanhVienBody;
    private final ArrayList<ThongTinKhachThue> dsGhepChon  = new ArrayList<>();
    private final ArrayList<ThongTinKhachThue> dsGhepAvail = new ArrayList<>();
    private final LinkedHashMap<ThongTinKhachThue, DateMaskField> mapNgayVaoFields = new LinkedHashMap<>();

    // Tab Dịch vụ
    private JPanel pnlDvBody;
    private JButton btnSaveDv;

    // Tab Hóa đơn
    private final DefaultTableModel modelHD = new DefaultTableModel(
            new Object[]{"Mã HĐ", "Kỳ", "Khách thuê", "Tổng tiền", "Trạng thái"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tableHD = new JTable(modelHD);

    private ThongTinHopDong currentHopDong;
    private final Runnable onBack;

    public Panel_ChiTietPhong(Runnable onBack) {
        this.onBack = onBack;
        setBackground(Theme.SLATE_50);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 24, 20, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JButton btnBack = Theme.secondaryButton("← Quay lại");
        btnBack.addActionListener(e -> { if (onBack != null) onBack.run(); });

        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftBox.setOpaque(false);
        leftBox.add(btnBack);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        lblTenPhong.setFont(Theme.FONT_HEADER);
        lblTenPhong.setForeground(Theme.SLATE_900);
        JLabel sub = new JLabel("Chi tiết phòng");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.SLATE_500);
        titleBox.add(lblTenPhong);
        titleBox.add(sub);

        leftBox.add(titleBox);
        header.add(leftBox, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 24, 0));
        right.setOpaque(false);
        right.add(miniStat("Diện tích", lblDienTich));
        right.add(miniStat("Giá thuê", lblGiaThue));
        right.add(miniStat("Trạng thái", lblTrangThai));
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JPanel miniStat(String label, JLabel valueLabel) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.SLATE_500);
        l.setAlignmentX(LEFT_ALIGNMENT);
        valueLabel.setFont(Theme.FONT_BASE_BOLD);
        valueLabel.setForeground(Theme.SLATE_900);
        valueLabel.setAlignmentX(LEFT_ALIGNMENT);
        p.add(l);
        p.add(valueLabel);
        return p;
    }

    private JTabbedPane buildBody() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.FONT_BASE_BOLD);
        tabs.setBackground(Color.WHITE);

        tabs.addTab("👥 Khách thuê",       buildTabKhachThue());
        tabs.addTab("🧰 Dịch vụ sử dụng",  buildTabDichVuSuDung());
        tabs.addTab("💳 Hóa đơn",          buildTabHoaDon());
        return tabs;
    }

    // ── Tab 1: Khách thuê ────────────────────────────────────────────────────

    private JPanel buildTabKhachThue() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.SLATE_50);
        outer.setBorder(new EmptyBorder(16, 16, 16, 16));

        pnlThanhVienBody = new JPanel();
        pnlThanhVienBody.setLayout(new BoxLayout(pnlThanhVienBody, BoxLayout.Y_AXIS));
        pnlThanhVienBody.setBackground(Theme.SLATE_50);

        JScrollPane sp = new JScrollPane(pnlThanhVienBody);
        sp.setBorder(null);
        sp.getViewport().setBackground(Theme.SLATE_50);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        outer.add(sp, BorderLayout.CENTER);
        return outer;
    }

    private void rebuildThanhVienUI() {
        pnlThanhVienBody.removeAll();

        if (currentHopDong == null) {
            JLabel lbNo = new JLabel(
                    "<html><i><font color='#94A3B8'>Phòng chưa có hợp đồng nào còn hiệu lực.</font></i></html>");
            lbNo.setFont(Theme.FONT_BASE);
            lbNo.setAlignmentX(LEFT_ALIGNMENT);
            lbNo.setBorder(new EmptyBorder(20, 0, 0, 0));
            pnlThanhVienBody.add(lbNo);
            pnlThanhVienBody.revalidate();
            pnlThanhVienBody.repaint();
            return;
        }

        // Khách thuê chính
        pnlThanhVienBody.add(sectionLabel("Khách thuê chính"));
        pnlThanhVienBody.add(Box.createVerticalStrut(6));
        ThongTinKhachThue primary = KhachThueDAO.getKhachThueById(currentHopDong.getMaKhachThue());
        if (primary != null) {
            pnlThanhVienBody.add(buildMemberCard(primary, false, null));
        }
        pnlThanhVienBody.add(Box.createVerticalStrut(20));

        // Người ở cùng
        pnlThanhVienBody.add(sectionLabel("Người ở cùng (" + dsGhepChon.size() + " người)"));
        pnlThanhVienBody.add(Box.createVerticalStrut(6));
        if (dsGhepChon.isEmpty()) {
            JLabel lbEmpty = new JLabel("Chưa có người ở cùng");
            lbEmpty.setFont(Theme.FONT_SMALL);
            lbEmpty.setForeground(Theme.SLATE_400);
            lbEmpty.setAlignmentX(LEFT_ALIGNMENT);
            lbEmpty.setBorder(new EmptyBorder(4, 0, 8, 0));
            pnlThanhVienBody.add(lbEmpty);
        }
        for (ThongTinKhachThue k : new ArrayList<>(dsGhepChon)) {
            DateMaskField dfNgayVao = mapNgayVaoFields.computeIfAbsent(k, x -> {
                DateMaskField df = new DateMaskField();
                df.setValue(LocalDate.now().toString());
                return df;
            });
            pnlThanhVienBody.add(buildMemberCard(k, true, () -> {
                dsGhepChon.remove(k);
                dsGhepAvail.add(k);
                mapNgayVaoFields.remove(k);
                rebuildThanhVienUI();
            }, dfNgayVao));
            pnlThanhVienBody.add(Box.createVerticalStrut(8));
        }
        pnlThanhVienBody.add(Box.createVerticalStrut(12));

        // Widget thêm thành viên
        int primaryMa = currentHopDong.getMaKhachThue();
        ArrayList<ThongTinKhachThue> dsCoTheGhep = new ArrayList<>();
        for (ThongTinKhachThue k : dsGhepAvail) {
            if (k.getMaKhachThue() != primaryMa) dsCoTheGhep.add(k);
        }

        if (dsCoTheGhep.isEmpty()) {
            JLabel lbNoneAvail = new JLabel(
                    "<html><i><font color='#94A3B8'>Không có khách thuê nào sẵn sàng để ghép phòng. "
                            + "Hãy thêm khách thuê mới ở trang \"Khách thuê\" (chưa gán phòng/hợp đồng) "
                            + "để có thể chọn ở đây.</font></i></html>");
            lbNoneAvail.setFont(Theme.FONT_SMALL);
            lbNoneAvail.setAlignmentX(LEFT_ALIGNMENT);
            lbNoneAvail.setBorder(new EmptyBorder(4, 0, 8, 0));
            pnlThanhVienBody.add(lbNoneAvail);
        } else {
            JComboBox<String> cbAddGhep = new JComboBox<>();
            Theme.styleComboBox(cbAddGhep);
            cbAddGhep.addItem("-- Chọn người ở cùng --");
            for (ThongTinKhachThue k : dsCoTheGhep) {
                cbAddGhep.addItem(k.getHoTen() + " - " + k.getSoCCCD());
            }

            JButton btnAdd = Theme.secondaryButton("+ Thêm");
            btnAdd.addActionListener(e -> {
                int idx = cbAddGhep.getSelectedIndex();
                if (idx <= 0) return;
                ThongTinKhachThue k = dsCoTheGhep.get(idx - 1);
                dsGhepChon.add(k);
                dsGhepAvail.remove(k);
                rebuildThanhVienUI();
            });

            JPanel addRow = new JPanel(new BorderLayout(6, 0));
            addRow.setOpaque(false);
            addRow.setAlignmentX(LEFT_ALIGNMENT);
            addRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            addRow.add(cbAddGhep, BorderLayout.CENTER);
            addRow.add(btnAdd, BorderLayout.EAST);
            pnlThanhVienBody.add(addRow);
        }
        pnlThanhVienBody.add(Box.createVerticalStrut(12));

        JButton btnSave = Theme.primaryButton("Lưu người ở cùng");
        btnSave.addActionListener(e -> {
            ArrayList<Integer> ids = new ArrayList<>();
            ArrayList<LocalDate> ngayVaoList = new ArrayList<>();
            for (ThongTinKhachThue k : dsGhepChon) {
                ids.add(k.getMaKhachThue());
                DateMaskField df = mapNgayVaoFields.get(k);
                LocalDate ngayVao = null;
                if (df != null && !df.getValue().isEmpty()) {
                    try { ngayVao = LocalDate.parse(df.getValue()); } catch (Exception ignore) {}
                }
                ngayVaoList.add(ngayVao);
            }
            if (HopDongDAO.capNhatNguoiOGhep(currentHopDong.getMaHopDong(), ids, ngayVaoList)) {
                JOptionPane.showMessageDialog(Panel_ChiTietPhong.this, "Đã lưu người ở cùng.");
            } else {
                JOptionPane.showMessageDialog(Panel_ChiTietPhong.this,
                        "Lưu người ở cùng thất bại. Vui lòng kiểm tra lại kết nối CSDL.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        JPanel saveRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        saveRow.setOpaque(false);
        saveRow.setAlignmentX(LEFT_ALIGNMENT);
        saveRow.add(btnSave);
        pnlThanhVienBody.add(saveRow);
        pnlThanhVienBody.add(Box.createVerticalStrut(20));

        pnlThanhVienBody.revalidate();
        pnlThanhVienBody.repaint();
    }

    // ── Tab 2: Dịch vụ sử dụng ───────────────────────────────────────────────

    private JPanel buildTabDichVuSuDung() {
        JPanel outer = new JPanel(new BorderLayout(0, 12));
        outer.setBackground(Theme.SLATE_50);
        outer.setBorder(new EmptyBorder(16, 16, 16, 16));

        pnlDvBody = new JPanel();
        pnlDvBody.setLayout(new BoxLayout(pnlDvBody, BoxLayout.Y_AXIS));
        pnlDvBody.setBackground(Color.WHITE);
        pnlDvBody.setBorder(new EmptyBorder(12, 16, 12, 16));

        JScrollPane sp = new JScrollPane(pnlDvBody);
        sp.setBorder(Theme.cardBorder());
        sp.getViewport().setBackground(Color.WHITE);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        outer.add(sp, BorderLayout.CENTER);

        btnSaveDv = Theme.primaryButton("Lưu");
        btnSaveDv.setEnabled(false);
        btnSaveDv.addActionListener(e -> saveDichVu());
        JPanel southRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        southRow.setOpaque(false);
        southRow.add(btnSaveDv);
        outer.add(southRow, BorderLayout.SOUTH);

        return outer;
    }

    private void loadDichVuSuDung() {
        pnlDvBody.removeAll();

        if (currentHopDong == null) {
            btnSaveDv.setEnabled(false);
            JLabel lbNo = new JLabel(
                    "<html><i><font color='#94A3B8'>Phòng chưa có hợp đồng nào còn hiệu lực.</font></i></html>");
            lbNo.setFont(Theme.FONT_BASE);
            pnlDvBody.add(lbNo);
            pnlDvBody.revalidate();
            pnlDvBody.repaint();
            return;
        }

        Set<String> dvHienTai = new HashSet<>();
        for (ThongTinDichVu dv : DichVuDAO.getDichVuByHopDong(currentHopDong.getMaHopDong()))
            dvHienTai.add(dv.getMaDichVu());

        for (ThongTinDichVu dv : DichVuDAO.getThongTinDichVu()) {
            String donVi = dv.getDonVi() == null ? "" : dv.getDonVi();
            String label = dv.getTenDichVu()
                    + "   —   " + String.format("%,d đ", dv.getGiaDichVu())
                    + (donVi.isEmpty() ? "" : " / " + donVi);
            JCheckBox ck = new JCheckBox(label);
            ck.setFont(Theme.FONT_BASE);
            ck.setForeground(Theme.SLATE_800);
            ck.setBackground(Color.WHITE);
            ck.setSelected(dvHienTai.contains(dv.getMaDichVu()));
            ck.setAlignmentX(LEFT_ALIGNMENT);
            try { ck.putClientProperty("maDV", Integer.parseInt(dv.getMaDichVu())); }
            catch (NumberFormatException ignore) { ck.putClientProperty("maDV", 0); }
            pnlDvBody.add(ck);
            pnlDvBody.add(Box.createVerticalStrut(4));
        }

        btnSaveDv.setEnabled(true);
        pnlDvBody.revalidate();
        pnlDvBody.repaint();
    }

    private void saveDichVu() {
        if (currentHopDong == null) return;
        ArrayList<Integer> chon = new ArrayList<>();
        for (Component c : pnlDvBody.getComponents()) {
            if (c instanceof JCheckBox) {
                JCheckBox ck = (JCheckBox) c;
                if (ck.isSelected()) {
                    Object ma = ck.getClientProperty("maDV");
                    if (ma instanceof Integer && (Integer) ma != 0) chon.add((Integer) ma);
                }
            }
        }
        HopDongDAO.capNhatDichVuHopDong(currentHopDong.getMaHopDong(), chon);
        JOptionPane.showMessageDialog(this, "Đã lưu dịch vụ sử dụng.");
    }

    // ── Tab 3: Hóa đơn ───────────────────────────────────────────────────────

    private JPanel buildTabHoaDon() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        p.setBackground(Theme.SLATE_50);

        Theme.styleTable(tableHD);
        tableHD.getColumnModel().getColumn(0).setMinWidth(0);
        tableHD.getColumnModel().getColumn(0).setMaxWidth(0);
        tableHD.getColumnModel().getColumn(0).setPreferredWidth(0);
        tableHD.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                return l;
            }
        });
        p.add(Theme.wrapScroll(tableHD), BorderLayout.CENTER);
        return p;
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    public void setPhong(ThongTinPhong p) {
        lblTenPhong.setText(p.getTenPhong());
        lblDienTich.setText(p.getDienTich() == null ? "—" : p.getDienTich());
        lblGiaThue.setText(formatGia(p.getGiaThue()));
        if (p.isDangO()) lblTrangThai.setText(Theme.pillInfo("Đang thuê"));
        else             lblTrangThai.setText(Theme.pillSuccess("Trống"));

        currentHopDong = HopDongDAO.getHopDongHienTaiByPhong(p.getMaPhong());

        dsGhepChon.clear();
        dsGhepAvail.clear();
        mapNgayVaoFields.clear();
        if (currentHopDong != null) {
            java.util.Map<Integer, LocalDate> dbNgayVao =
                    HopDongDAO.getNguoiOGhepWithNgayVao(currentHopDong.getMaHopDong());
            for (int id : HopDongDAO.getNguoiOGhep(currentHopDong.getMaHopDong())) {
                ThongTinKhachThue k = KhachThueDAO.getKhachThueById(id);
                if (k != null) {
                    dsGhepChon.add(k);
                    DateMaskField df = new DateMaskField();
                    LocalDate d = dbNgayVao.get(k.getMaKhachThue());
                    if (d != null) df.setValue(d.toString());
                    mapNgayVaoFields.put(k, df);
                }
            }
            dsGhepAvail.addAll(KhachThueDAO.getKhachChuaCoHopDong(currentHopDong.getMaHopDong()));
            dsGhepAvail.removeIf(k -> dsGhepChon.stream()
                    .anyMatch(g -> g.getMaKhachThue() == k.getMaKhachThue()));
        }

        rebuildThanhVienUI();
        loadDichVuSuDung();
        loadHoaDon();
    }

    private void loadHoaDon() {
        modelHD.setRowCount(0);
        if (currentHopDong == null) return;
        for (Object[] r : HoaDonDAO.getHoaDonByPhong(currentHopDong.getMaHopDong())) {
            modelHD.addRow(r);
        }
    }

    // ── UI Helpers ────────────────────────────────────────────────────────────

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_TITLE);
        l.setForeground(Theme.SLATE_900);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JPanel buildMemberCard(ThongTinKhachThue k, boolean canRemove, Runnable onRemove) {
        return buildMemberCard(k, canRemove, onRemove, null);
    }

    private JPanel buildMemberCard(ThongTinKhachThue k, boolean canRemove, Runnable onRemove, DateMaskField dfNgayVao) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                Theme.cardBorder(), new EmptyBorder(12, 16, 12, 16)));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Header row: tên + ngày vào (nếu có) + optional remove button
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel lblName = new JLabel(k.getHoTen() == null ? "—" : k.getHoTen());
        lblName.setFont(Theme.FONT_BASE_BOLD);
        lblName.setForeground(Theme.SLATE_900);
        header.add(lblName, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        if (dfNgayVao != null) {
            JLabel lblVao = new JLabel("Ngày vào:");
            lblVao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblVao.setForeground(Theme.SLATE_600);
            dfNgayVao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dfNgayVao.setPreferredSize(new Dimension(100, 26));
            right.add(lblVao);
            right.add(dfNgayVao);
        }
        if (canRemove && onRemove != null) {
            JButton btnX = new JButton("✕ Xóa");
            btnX.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btnX.setForeground(new Color(0xDC2626));
            btnX.setBackground(Color.WHITE);
            btnX.setBorderPainted(false);
            btnX.setFocusPainted(false);
            btnX.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnX.addActionListener(e -> onRemove.run());
            right.add(btnX);
        }
        if (right.getComponentCount() > 0) header.add(right, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // Fields 2-column grid
        JPanel fields = new JPanel(new GridLayout(0, 2, 16, 8));
        fields.setOpaque(false);
        fields.add(infoField("CCCD", k.getSoCCCD()));
        fields.add(infoField("Số điện thoại", k.getSoDienThoai()));
        fields.add(infoField("Email", k.getEmail()));
        fields.add(infoField("Giới tính", k.getGioiTinh()));
        fields.add(infoField("Ngày sinh", k.getNgaySinh()));
        fields.add(infoField("Quê quán", k.getQueQuan()));
        fields.add(infoField("Biển số xe", k.getBienSoXe()));
        card.add(fields, BorderLayout.CENTER);

        return card;
    }

    private JPanel infoField(String label, String value) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.SLATE_500);
        JLabel val = new JLabel(value == null || value.isEmpty() ? "—" : value);
        val.setFont(Theme.FONT_BASE);
        val.setForeground(Theme.SLATE_800);
        p.add(lbl);
        p.add(val);
        return p;
    }

    private static String formatGia(String gia) {
        if (gia == null) return "—";
        try {
            long g = Long.parseLong(gia.replaceAll("[^0-9]", ""));
            return String.format("%,d đ", g);
        } catch (Exception e) {
            return gia;
        }
    }
}
