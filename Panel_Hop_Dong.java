package QuanLiNhaTro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Panel_Hop_Dong extends JPanel {

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Mã HĐ", "Phòng", "Khách thuê", "Bắt đầu", "Kết thúc",
                         "Tiền cọc", "Dịch vụ", "Hình ảnh", "Trạng thái", "Ghi chú"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private final JTextField tfSearch = new JTextField();
    private final JComboBox<String> cbStatus = new JComboBox<>(new String[]{
            "Tất cả", "Còn hiệu lực", "Sắp hết hạn"
    });
    private ArrayList<ThongTinHopDong> dsGoc = new ArrayList<>();

    public Panel_Hop_Dong() {
        setBackground(Theme.SLATE_50);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 24, 20, 24));

        add(buildHeader(), BorderLayout.NORTH);

        Theme.styleTable(table);
        table.setRowSorter(sorter);
        table.setRowHeight(50);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        DefaultTableCellRenderer vcenter = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setVerticalAlignment(SwingConstants.CENTER);
                return l;
            }
        };
        for (int i : new int[]{0, 1, 2, 3, 4, 5, 7, 9}) {
            table.getColumnModel().getColumn(i).setCellRenderer(vcenter);
        }

        // Dịch vụ
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setHorizontalAlignment(SwingConstants.LEFT);
                l.setVerticalAlignment(SwingConstants.TOP);
                return l;
            }
        });
        table.getColumnModel().getColumn(6).setMinWidth(200);
        table.getColumnModel().getColumn(6).setPreferredWidth(240);

        // Trạng thái
        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setVerticalAlignment(SwingConstants.CENTER);
                return l;
            }
        });

        // Hình ảnh
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (v != null && !v.toString().isEmpty()) {
                    l.setText("Xem"); l.setForeground(Theme.TEAL_700);
                } else {
                    l.setText("—"); l.setForeground(Theme.SLATE_400);
                }
                l.setHorizontalAlignment(SwingConstants.CENTER);
                return l;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (col == 7 && row >= 0) {
                    int mr = table.convertRowIndexToModel(row);
                    String img = (String) model.getValueAt(mr, 7);
                    if (img != null && !img.isEmpty()) showImageDialog(img);
                }
            }
        });

        JPanel body = new JPanel(new BorderLayout());
        body.setBorder(new EmptyBorder(16, 0, 0, 0));
        body.setOpaque(false);
        body.add(Theme.wrapScroll(table), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        hienThiDanhSach();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JLabel title = new JLabel("Hợp đồng");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.SLATE_900);
        header.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        Theme.styleTextField(tfSearch);
        tfSearch.setPreferredSize(new Dimension(200, 34));
        tfSearch.putClientProperty("JTextField.placeholderText", "Tìm phòng, khách...");
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate (javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate (javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        Theme.styleComboBox(cbStatus);
        cbStatus.setPreferredSize(new Dimension(160, 34));
        cbStatus.addActionListener(e -> applyFilter());

        JButton btnAdd    = Theme.primaryButton("+ Tạo hợp đồng");
        JButton btnEdit   = Theme.warningButton("Chỉnh sửa");
        JButton btnLuuTru = Theme.infoButton("Lịch sử");
        JButton btnReload = Theme.secondaryButton("");
        URL reloadUrl = Panel_Hop_Dong.class.getResource("icon/icon_reload.png");
        if (reloadUrl != null) {
            Image img = new ImageIcon(reloadUrl).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btnReload.setIcon(new ImageIcon(img));
        } else { btnReload.setText("🔄"); }

        btnAdd.addActionListener(e -> showForm(null));
        btnEdit.addActionListener(e -> editSelected());
        btnLuuTru.addActionListener(e -> showArchiveDialog());
        btnReload.addActionListener(e -> {
            hienThiDanhSach();
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Panel_Hop_Dong.this),
                    "Dữ liệu đã được cập nhật!", "Cập nhật", JOptionPane.INFORMATION_MESSAGE);
        });

        right.add(Theme.wrapWithSearchIcon(tfSearch));
        right.add(cbStatus);
        right.add(btnReload);
        right.add(btnLuuTru);
        right.add(btnEdit);
        right.add(btnAdd);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    public void hienThiDanhSach() {
        model.setRowCount(0);
        dsGoc = HopDongDAO.getFullHopDong();
        for (ThongTinHopDong hd : dsGoc) model.addRow(hd.getThongTinHopDong());
        applyFilter();
        SwingUtilities.invokeLater(this::adjustRowHeights);
    }

    private void adjustRowHeights() {
        for (int row = 0; row < table.getRowCount(); row++) {
            int mr = table.convertRowIndexToModel(row);
            Object svc = model.getValueAt(mr, 6);
            int height = 50;
            if (svc != null && !svc.toString().isEmpty()) {
                int lines = svc.toString().split("<br>", -1).length;
                height = Math.max(50, lines * 22 + 12);
            }
            if (table.getRowHeight(row) != height) table.setRowHeight(row, height);
        }
    }

    private void applyFilter() {
        String q  = tfSearch.getText().trim();
        String st = (String) cbStatus.getSelectedItem();
        java.util.List<RowFilter<Object, Object>> filters = new ArrayList<>();
        // Luôn loại bỏ hợp đồng hết hiệu lực khỏi bảng chính
        filters.add(new RowFilter<Object, Object>() {
            @Override public boolean include(Entry<? extends Object, ? extends Object> entry) {
                Object v = entry.getValue(8);
                return v == null || !v.toString().contains("Hết hiệu lực");
            }
        });
        if (!q.isEmpty()) {
            javax.swing.RowFilter<Object, Object> sf = Theme.searchFilter(q);
            if (sf != null) filters.add(sf);
        }
        if (st != null && !"Tất cả".equals(st)) {
            final String stFinal = st;
            filters.add(new RowFilter<Object, Object>() {
                @Override public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    Object v = entry.getValue(8);
                    return v != null && v.toString().contains(stFinal);
                }
            });
        }
        sorter.setRowFilter(RowFilter.andFilter(filters));
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn hợp đồng cần sửa"); return; }
        int mr = table.convertRowIndexToModel(row);
        int maHD = (int) model.getValueAt(mr, 0);
        ThongTinHopDong hd = HopDongDAO.getHopDongForEdit(maHD);
        if (hd != null) showForm(hd);
    }

    // ─── Dialog lịch sử / lưu trữ ───────────────────────────────────────────
    private void showArchiveDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Lịch sử - Hợp đồng hết hiệu lực", true);
        dlg.setSize(1000, 540);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(Theme.SLATE_50);
        dlg.setLayout(new BorderLayout(0, 0));

        // ── Header: tiêu đề + tìm kiếm + đóng ──
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(16, 20, 12, 20));

        JLabel lbl = new JLabel("Hợp đồng đã hết hiệu lực");
        lbl.setFont(Theme.FONT_TITLE);
        lbl.setForeground(Theme.SLATE_900);
        top.add(lbl, BorderLayout.WEST);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchRow.setOpaque(false);

        JTextField tfSearch = new JTextField();
        Theme.styleTextField(tfSearch);
        tfSearch.setPreferredSize(new Dimension(260, 32));
        tfSearch.putClientProperty("JTextField.placeholderText", "Tìm phòng, khách, ngày...");

        JButton btnClose = Theme.secondaryButton("Đóng");
        btnClose.addActionListener(e -> dlg.dispose());
        searchRow.add(Theme.wrapWithSearchIcon(tfSearch));
        searchRow.add(btnClose);
        top.add(searchRow, BorderLayout.EAST);
        dlg.add(top, BorderLayout.NORTH);

        // ── Table ──
        DefaultTableModel archiveModel = new DefaultTableModel(
                new Object[]{"Mã HĐ", "Phòng", "Khách thuê", "Bắt đầu", "Kết thúc",
                             "Tiền cọc", "Ghi chú", "Gia hạn"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 7; }
        };
        JTable archiveTable = new JTable(archiveModel);
        TableRowSorter<DefaultTableModel> archiveSorter = new TableRowSorter<>(archiveModel);
        archiveTable.setRowSorter(archiveSorter);
        Theme.styleTable(archiveTable);
        archiveTable.setRowHeight(46);
        archiveTable.getColumnModel().getColumn(0).setMinWidth(0);
        archiveTable.getColumnModel().getColumn(0).setMaxWidth(0);
        archiveTable.getColumnModel().getColumn(0).setPreferredWidth(0);
        archiveTable.getColumnModel().getColumn(7).setMinWidth(110);
        archiveTable.getColumnModel().getColumn(7).setPreferredWidth(120);
        archiveTable.getColumnModel().getColumn(7).setMaxWidth(130);

        archiveTable.getColumnModel().getColumn(7).setCellRenderer((t, v, s, f, r, c) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 7));
            p.setOpaque(true);
            p.setBackground(s ? Theme.TEAL_50 : Color.WHITE);
            p.add(Theme.infoButton("Gia hạn"));
            return p;
        });

        archiveTable.getColumnModel().getColumn(7).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 7));
            private final JButton btnGH = Theme.infoButton("↩ Gia hạn");
            private int clickRow;
            {
                setClickCountToStart(1);
                panel.setOpaque(true);
                panel.add(btnGH);
                btnGH.addActionListener(e -> {
                    fireEditingStopped();
                    int modelRow = archiveTable.convertRowIndexToModel(clickRow);
                    showGiaHanDialog(dlg, archiveModel, modelRow);
                });
            }
            @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
                clickRow = r;
                panel.setBackground(Theme.TEAL_50);
                return panel;
            }
            @Override public Object getCellEditorValue() { return ""; }
        });

        // ── Tìm kiếm tất cả cột lịch sử hợp đồng ──
        tfSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { filter(); }
            public void removeUpdate(DocumentEvent e)  { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
            void filter() {
                archiveSorter.setRowFilter(Theme.searchFilter(tfSearch.getText()));
            }
        });

        loadArchive(archiveModel);

        JPanel body = new JPanel(new BorderLayout());
        body.setBorder(new EmptyBorder(0, 16, 16, 16));
        body.setOpaque(false);
        body.add(Theme.wrapScroll(archiveTable), BorderLayout.CENTER);
        dlg.add(body, BorderLayout.CENTER);
        dlg.setVisible(true);
    }

    private void loadArchive(DefaultTableModel archiveModel) {
        archiveModel.setRowCount(0);
        for (ThongTinHopDong hd : HopDongDAO.getFullHopDong()) {
            if (!"Hết hiệu lực".equals(hd.getTrangThaiHienThi())) continue;
            archiveModel.addRow(new Object[]{
                hd.getMaHopDong(), hd.getTenPhong(), hd.getHoTenKhach(),
                hd.getNgayBatDau(), hd.getNgayKetThucDisplay(),
                String.format("%,d đ", hd.getTienCoc()),
                hd.getGhiChu(), ""
            });
        }
    }

    // ─── Dialog gia hạn hợp đồng ────────────────────────────────────────────
    private void showGiaHanDialog(JDialog parentDlg, DefaultTableModel archiveModel, int row) {
        int maHD = (int) archiveModel.getValueAt(row, 0);
        String ngayBatDau   = (String) archiveModel.getValueAt(row, 3);
        String ngayKetThucCu = (String) archiveModel.getValueAt(row, 4);

        JDialog dlg = new JDialog(parentDlg, "Gia hạn hợp đồng #" + maHD, true);
        dlg.setSize(420, 250);
        dlg.setLocationRelativeTo(parentDlg);
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        form.setBackground(Color.WHITE);

        DateMaskField tfBD = new DateMaskField();
        tfBD.setValue(ngayBatDau != null ? ngayBatDau : "");
        tfBD.setEnabled(false);
        tfBD.setFont(Theme.FONT_BASE);
        tfBD.setBackground(new Color(0xF1F5F9));

        DateMaskField tfKT = new DateMaskField();
        tfKT.setFont(Theme.FONT_BASE);
        tfKT.setBackground(Color.WHITE);
        // Điền ngày kết thúc cũ nếu là ngày hợp lệ (không phải "Vô thời hạn")
        if (ngayKetThucCu != null && ngayKetThucCu.matches("\\d{4}-\\d{2}-\\d{2}")) {
            tfKT.setValue(ngayKetThucCu);
        }

        form.add(formRow("Ngày bắt đầu", tfBD));
        form.add(Box.createVerticalStrut(10));
        form.add(formRow("Ngày kết thúc mới *", tfKT));
        form.add(Box.createVerticalStrut(16));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        JButton btnCancel = Theme.secondaryButton("Hủy");
        btnCancel.addActionListener(e -> dlg.dispose());
        JButton btnSave = Theme.primaryButton("Gia hạn");
        btnSave.addActionListener(e -> {
            String newNgayKT = tfKT.getValue();
            if (newNgayKT.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập ngày kết thúc mới");
                return;
            }
            try {
                LocalDate endDate = LocalDate.parse(newNgayKT);
                if (!endDate.isAfter(LocalDate.now())) {
                    JOptionPane.showMessageDialog(dlg,
                            "Ngày kết thúc phải lớn hơn ngày hiện tại\nđể hợp đồng được chuyển về còn hiệu lực.",
                            "Không hợp lệ", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                HopDongDAO.giaHanHopDong(maHD, newNgayKT);
                dlg.dispose();
                loadArchive(archiveModel);
                hienThiDanhSach();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Ngày không hợp lệ: " + ex.getMessage());
            }
        });
        btnRow.add(btnCancel);
        btnRow.add(btnSave);
        form.add(btnRow);

        dlg.add(form);
        dlg.setVisible(true);
    }

    // ─── Image dialog ────────────────────────────────────────────────────────
    private void showImageDialog(String imgPath) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Hình ảnh hợp đồng", true);
        d.setSize(720, 520);
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(Color.WHITE);
        try {
            ImageIcon icon = new ImageIcon(imgPath);
            Image scaled = icon.getImage().getScaledInstance(700, -1, Image.SCALE_SMOOTH);
            d.add(new JScrollPane(new JLabel(new ImageIcon(scaled), SwingConstants.CENTER)));
        } catch (Exception e) {
            d.add(new JLabel("Không tải được hình: " + imgPath, SwingConstants.CENTER));
        }
        d.setVisible(true);
    }

    // ─── Form thêm/sửa hợp đồng ─────────────────────────────────────────────
    private void showForm(ThongTinHopDong hd) {
        boolean isEdit = hd != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Sửa hợp đồng" : "Tạo hợp đồng mới", true);
        dlg.setSize(560, 700);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        form.setBackground(Color.WHITE);

        // Phòng
        ArrayList<ThongTinPhong> dsPhong = RoomDAO.getThongTinPhong();
        ArrayList<String> phongItems = new ArrayList<>();
        ArrayList<String> phongMa = new ArrayList<>();
        for (ThongTinPhong p : dsPhong) {
            if (!isEdit && p.isDangO()) continue;
            phongItems.add(p.getTenPhong() + " (" + formatGiaShort(p.getGiaThue()) + ")");
            phongMa.add(p.getMaPhong());
        }
        if (isEdit && !phongMa.contains(hd.getMaPhong())) {
            phongItems.add(0, hd.getTenPhong() + " (hiện tại)");
            phongMa.add(0, hd.getMaPhong());
        }
        JComboBox<String> cbPhong = new JComboBox<>(phongItems.toArray(new String[0]));
        Theme.styleComboBox(cbPhong);

        // Khách — chỉ hiển thị người chưa có HĐ đang hoạt động
        int excludeHopDongId = isEdit ? hd.getMaHopDong() : 0;
        ArrayList<ThongTinKhachThue> dsKhachAvail = KhachThueDAO.getKhachChuaCoHopDong(excludeHopDongId);
        // Khi sửa: đảm bảo khách hiện tại luôn có trong danh sách
        if (isEdit) {
            boolean found = false;
            for (ThongTinKhachThue k : dsKhachAvail)
                if (k.getMaKhachThue() == hd.getMaKhachThue()) { found = true; break; }
            if (!found) {
                for (ThongTinKhachThue k : KhachThueDAO.getFullKhachThue())
                    if (k.getMaKhachThue() == hd.getMaKhachThue()) { dsKhachAvail.add(0, k); break; }
            }
        }
        ArrayList<String> khachItems = new ArrayList<>();
        ArrayList<Integer> khachMa = new ArrayList<>();
        for (ThongTinKhachThue k : dsKhachAvail) {
            khachItems.add(k.getHoTen() + " - " + k.getSoCCCD());
            khachMa.add(k.getMaKhachThue());
        }
        JComboBox<String> cbKhach = new JComboBox<>(khachItems.toArray(new String[0]));
        Theme.styleComboBox(cbKhach);

        JTextField tfGiaThue = new JTextField();
        JTextField tfTienCoc = new JTextField();
        DateMaskField tfNgayBD = new DateMaskField();
        DateMaskField tfNgayKT = new DateMaskField();
        JCheckBox ckVoThoiHan = new JCheckBox("Vô thời hạn");
        JTextField tfHinhAnh = new JTextField();
        JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Còn hiệu lực", "Hết hiệu lực"});
        JTextField tfGhiChu = new JTextField();

        for (JTextField t : new JTextField[]{tfGiaThue, tfTienCoc, tfHinhAnh, tfGhiChu}) Theme.styleTextField(t);
        tfNgayBD.setFont(Theme.FONT_BASE); tfNgayBD.setBackground(Color.WHITE);
        tfNgayKT.setFont(Theme.FONT_BASE); tfNgayKT.setBackground(Color.WHITE);
        Theme.styleComboBox(cbTrangThai);
        ckVoThoiHan.setBackground(Color.WHITE);
        ckVoThoiHan.setFont(Theme.FONT_BASE);
        ckVoThoiHan.setForeground(Theme.SLATE_700);

        if (isEdit) {
            int idx = phongMa.indexOf(hd.getMaPhong());
            if (idx >= 0) cbPhong.setSelectedIndex(idx);
            int kIdx = khachMa.indexOf(hd.getMaKhachThue());
            if (kIdx >= 0) cbKhach.setSelectedIndex(kIdx);
            tfGiaThue.setText(String.valueOf(hd.getGiaThueThucTe()));
            tfTienCoc.setText(String.valueOf(hd.getTienCoc()));
            tfNgayBD.setValue(hd.getNgayBatDau());
            if (hd.isVoThoiHan()) { ckVoThoiHan.setSelected(true); tfNgayKT.setEnabled(false); }
            else                   { tfNgayKT.setValue(hd.getNgayKetThuc()); }
            tfHinhAnh.setText(hd.getHinhAnh() == null ? "" : hd.getHinhAnh());
            String rawSt = hd.getTrangThaiGoc();
            cbTrangThai.setSelectedItem(
                (rawSt != null && rawSt.contains("Hết")) ? "Hết hiệu lực" : "Còn hiệu lực");
            tfGhiChu.setText(hd.getGhiChu() == null ? "" : hd.getGhiChu());
        } else {
            tfTienCoc.setText("2000000");
            tfNgayBD.setValue(java.time.LocalDate.now().toString());
            cbTrangThai.setSelectedItem("Còn hiệu lực");
        }

        ckVoThoiHan.addActionListener(e -> {
            if (ckVoThoiHan.isSelected()) { tfNgayKT.setValue(""); tfNgayKT.setEnabled(false); }
            else tfNgayKT.setEnabled(true);
        });

        // Chỉ số điện-nước ban đầu (chỉ khi tạo mới)
        JTextField tfChiSoDien = new JTextField("0");
        JTextField tfChiSoNuoc = new JTextField("0");
        Theme.styleTextField(tfChiSoDien);
        Theme.styleTextField(tfChiSoNuoc);

        // Dịch vụ
        ArrayList<ThongTinDichVu> dsDV = DichVuDAO.getThongTinDichVu();
        ArrayList<JCheckBox> ckDvs = new ArrayList<>();
        JPanel dvPanel = new JPanel();
        dvPanel.setLayout(new BoxLayout(dvPanel, BoxLayout.Y_AXIS));
        dvPanel.setBackground(Color.WHITE);
        for (ThongTinDichVu dv : dsDV) {
            JCheckBox cb = new JCheckBox(dv.getTenDichVu() + " — " +
                    String.format("%,d đ", dv.getGiaDichVu()) +
                    (dv.getDonVi() == null || dv.getDonVi().isEmpty() ? "" : " / " + dv.getDonVi()));
            cb.setBackground(Color.WHITE);
            cb.setFont(Theme.FONT_BASE);
            cb.setForeground(Theme.SLATE_700);
            cb.putClientProperty("maDV", Integer.parseInt(dv.getMaDichVu()));
            if (isEdit) {
                cb.setSelected(hd.getDanhSachMaDichVu().contains(Integer.parseInt(dv.getMaDichVu())));
            } else {
                cb.setSelected(true);
            }
            ckDvs.add(cb);
            dvPanel.add(cb);
        }
        JScrollPane spDv = new JScrollPane(dvPanel);
        spDv.setBorder(new Theme.LineBorderRounded(Theme.SLATE_300, 1, 8));
        spDv.setPreferredSize(new Dimension(0, 120));

        // Browse hình ảnh
        JPanel imgRow = new JPanel(new BorderLayout(6, 0));
        imgRow.setOpaque(false);
        imgRow.setAlignmentX(LEFT_ALIGNMENT);
        imgRow.add(tfHinhAnh, BorderLayout.CENTER);
        JButton btnBrowse = Theme.secondaryButton("Thêm");
        btnBrowse.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Hình ảnh", "jpg", "jpeg", "png", "bmp"));
            if (fc.showOpenDialog(dlg) == JFileChooser.APPROVE_OPTION)
                tfHinhAnh.setText(fc.getSelectedFile().getAbsolutePath());
        });
        imgRow.add(btnBrowse, BorderLayout.EAST);
        imgRow.setPreferredSize(new Dimension(0, 34));

        form.add(formRow("Phòng *", cbPhong));
        form.add(Box.createVerticalStrut(10));
        form.add(formRow("Khách thuê *", cbKhach));
        form.add(Box.createVerticalStrut(10));
        form.add(formRow("Giá thuê thực tế *", tfGiaThue));
        form.add(Box.createVerticalStrut(10));
        form.add(formRow("Tiền cọc", tfTienCoc));
        form.add(Box.createVerticalStrut(10));
        form.add(formRow("Ngày bắt đầu *", tfNgayBD));
        form.add(Box.createVerticalStrut(10));
        form.add(formRow("Ngày kết thúc", tfNgayKT));
        form.add(ckVoThoiHan);
        if (!isEdit) {
            form.add(Box.createVerticalStrut(14));
            JLabel lblCSHeader = new JLabel("Chỉ số Điện - Nước ban đầu (chốt với khách)");
            lblCSHeader.setFont(Theme.FONT_LABEL);
            lblCSHeader.setForeground(Theme.TEAL_700);
            lblCSHeader.setAlignmentX(LEFT_ALIGNMENT);
            form.add(lblCSHeader);
            form.add(Box.createVerticalStrut(6));
            form.add(formRow("Số điện hiện tại", tfChiSoDien));
            form.add(Box.createVerticalStrut(8));
            form.add(formRow("Số nước hiện tại", tfChiSoNuoc));
        }
        form.add(Box.createVerticalStrut(10));
        form.add(formRow("Hình ảnh HĐ", imgRow));
        if (isEdit) {
            form.add(Box.createVerticalStrut(10));
            form.add(formRow("Trạng thái", cbTrangThai));
        }
        form.add(Box.createVerticalStrut(10));
        form.add(formRow("Ghi chú", tfGhiChu));
        form.add(Box.createVerticalStrut(10));
        form.add(formRow("Dịch vụ kèm theo", spDv));
        form.add(Box.createVerticalStrut(10));

        // ── Người ở ghép ──
        ArrayList<ThongTinKhachThue> dsGhepAvail = new ArrayList<>(dsKhachAvail);
        ArrayList<ThongTinKhachThue> dsGhepChon  = new ArrayList<>();
        java.util.LinkedHashMap<ThongTinKhachThue, DateMaskField> mapNgayVaoFields = new java.util.LinkedHashMap<>();
        // Theo dõi người vừa bị xóa khỏi danh sách ghép + ngày rời đi tương ứng
        // (chỉ dùng khi isEdit; sẽ được ghi xuống DB khi nhấn Cập nhật)
        java.util.LinkedHashMap<ThongTinKhachThue, LocalDate> mapNgayDiGhep = new java.util.LinkedHashMap<>();

        // Khi sửa: nạp người ở ghép + NgayVao hiện tại
        if (isEdit) {
            ArrayList<Integer> dsHienTai = HopDongDAO.getNguoiOGhep(hd.getMaHopDong());
            java.util.Map<Integer, LocalDate> dbNgayVao = HopDongDAO.getNguoiOGhepWithNgayVao(hd.getMaHopDong());
            dsGhepAvail.removeIf(k -> {
                if (dsHienTai.contains(k.getMaKhachThue())) {
                    dsGhepChon.add(k);
                    DateMaskField df = new DateMaskField();
                    LocalDate d = dbNgayVao.get(k.getMaKhachThue());
                    if (d != null) df.setValue(d.toString());
                    mapNgayVaoFields.put(k, df);
                    return true;
                }
                return false;
            });
        }

        JComboBox<String> cbAddGhep = new JComboBox<>();
        Theme.styleComboBox(cbAddGhep);
        JButton btnAddGhep = Theme.secondaryButton("+ Thêm");

        JPanel pnlGhepChon = new JPanel();
        pnlGhepChon.setLayout(new BoxLayout(pnlGhepChon, BoxLayout.Y_AXIS));
        pnlGhepChon.setBackground(Color.WHITE);

        Runnable[] rr = {null};
        rr[0] = () -> {
            int mainMa = khachMa.isEmpty() ? -1 : khachMa.get(cbKhach.getSelectedIndex());
            cbAddGhep.removeAllItems();
            cbAddGhep.addItem("-- Chọn người ở ghép --");
            for (ThongTinKhachThue k : dsGhepAvail)
                if (k.getMaKhachThue() != mainMa)
                    cbAddGhep.addItem(k.getHoTen() + " - " + k.getSoCCCD());

            pnlGhepChon.removeAll();
            if (dsGhepChon.isEmpty()) {
                JLabel lbEmpty = new JLabel("Chưa có người ở ghép");
                lbEmpty.setFont(Theme.FONT_SMALL);
                lbEmpty.setForeground(Theme.SLATE_400);
                lbEmpty.setBorder(new EmptyBorder(4, 4, 4, 4));
                pnlGhepChon.add(lbEmpty);
            }
            for (ThongTinKhachThue k : new ArrayList<>(dsGhepChon)) {
                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.setBackground(new Color(0xF0FDF4));
                row.setBorder(BorderFactory.createCompoundBorder(
                        new Theme.LineBorderRounded(new Color(0x6EE7B7), 1, 6),
                        new EmptyBorder(4, 10, 4, 4)));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
                row.setAlignmentX(LEFT_ALIGNMENT);
                JLabel lbl = new JLabel(k.getHoTen() + " — " + k.getSoCCCD());
                lbl.setFont(Theme.FONT_BASE);
                lbl.setForeground(Theme.TEAL_700);

                // DateMaskField NgayVao — tạo 1 lần, persist qua các lần rebuild
                DateMaskField dfNgayVao = mapNgayVaoFields.computeIfAbsent(k, x -> {
                    DateMaskField df = new DateMaskField();
                    df.setValue(LocalDate.now().toString());
                    return df;
                });
                dfNgayVao.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                dfNgayVao.setBackground(new Color(0xF0FDF4));
                dfNgayVao.setPreferredSize(new Dimension(95, 24));

                JButton btnX = new JButton("✕");
                btnX.setFont(new Font("Segoe UI", Font.BOLD, 11));
                btnX.setForeground(Theme.SLATE_500);
                btnX.setBackground(new Color(0xF0FDF4));
                btnX.setBorderPainted(false);
                btnX.setFocusPainted(false);
                btnX.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btnX.addActionListener(ev -> {
                    if (isEdit) {
                        // Hợp đồng đã tồn tại → hỏi ngày rời đi để lưu lịch sử
                        LocalDate ngayDi = showNgayDiDialog(dlg, k.getHoTen());
                        if (ngayDi == null) return; // Người dùng bấm Hủy
                        mapNgayDiGhep.put(k, ngayDi);
                    }
                    mapNgayVaoFields.remove(k);
                    dsGhepChon.remove(k);
                    dsGhepAvail.add(k);
                    rr[0].run();
                });

                JLabel lblVao = new JLabel("Vào:");
                lblVao.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lblVao.setForeground(Theme.SLATE_600);
                JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
                right.setOpaque(false);
                right.add(lblVao);
                right.add(dfNgayVao);
                right.add(btnX);

                row.add(lbl, BorderLayout.CENTER);
                row.add(right, BorderLayout.EAST);
                pnlGhepChon.add(row);
                pnlGhepChon.add(Box.createVerticalStrut(3));
            }
            pnlGhepChon.revalidate();
            pnlGhepChon.repaint();
        };

        btnAddGhep.addActionListener(e -> {
            int idx = cbAddGhep.getSelectedIndex();
            if (idx <= 0) return; // 0 = placeholder
            int mainMa = khachMa.isEmpty() ? -1 : khachMa.get(cbKhach.getSelectedIndex());
            int pos = 0;
            for (ThongTinKhachThue k : dsGhepAvail) {
                if (k.getMaKhachThue() == mainMa) continue;
                if (++pos == idx) { dsGhepChon.add(k); dsGhepAvail.remove(k); break; }
            }
            rr[0].run();
        });

        cbKhach.addActionListener(e -> rr[0].run());
        rr[0].run();

        JPanel addGhepRow = new JPanel(new BorderLayout(6, 0));
        addGhepRow.setOpaque(false);
        addGhepRow.setAlignmentX(LEFT_ALIGNMENT);
        addGhepRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        addGhepRow.add(cbAddGhep, BorderLayout.CENTER);
        addGhepRow.add(btnAddGhep, BorderLayout.EAST);

        JScrollPane spGhepChon = new JScrollPane(pnlGhepChon);
        spGhepChon.setBorder(new Theme.LineBorderRounded(Theme.SLATE_200, 1, 8));
        spGhepChon.setPreferredSize(new Dimension(0, 90));

        JPanel ghepWrapper = new JPanel();
        ghepWrapper.setLayout(new BoxLayout(ghepWrapper, BoxLayout.Y_AXIS));
        ghepWrapper.setOpaque(false);
        ghepWrapper.setAlignmentX(LEFT_ALIGNMENT);
        ghepWrapper.add(addGhepRow);
        ghepWrapper.add(Box.createVerticalStrut(6));
        ghepWrapper.add(spGhepChon);

        JLabel lblGhep = new JLabel("Người ở ghép");
        lblGhep.setFont(Theme.FONT_LABEL);
        lblGhep.setForeground(Theme.SLATE_700);
        lblGhep.setAlignmentX(LEFT_ALIGNMENT);
        JPanel ghepFormRow = new JPanel();
        ghepFormRow.setLayout(new BoxLayout(ghepFormRow, BoxLayout.Y_AXIS));
        ghepFormRow.setOpaque(false);
        ghepFormRow.setAlignmentX(LEFT_ALIGNMENT);
        ghepFormRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        ghepFormRow.add(lblGhep);
        ghepFormRow.add(Box.createVerticalStrut(4));
        ghepFormRow.add(ghepWrapper);
        form.add(ghepFormRow);

        form.add(Box.createVerticalStrut(20));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        JButton btnCancel = Theme.secondaryButton("Hủy");
        btnCancel.addActionListener(e -> dlg.dispose());
        JButton btnSave = Theme.primaryButton(isEdit ? "Cập nhật" : "Tạo");
        btnSave.addActionListener(e -> {
            String maPhong = phongMa.get(cbPhong.getSelectedIndex());
            int maKhach = khachMa.get(cbKhach.getSelectedIndex());
            int gia;
            try { gia = Integer.parseInt(tfGiaThue.getText().trim().replaceAll("[^0-9]", "")); }
            catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "Giá thuê phải là số"); return; }
            int coc = 0;
            try { coc = Integer.parseInt(tfTienCoc.getText().trim().replaceAll("[^0-9]", "")); }
            catch (Exception ignore) {}
            String ngayBD = tfNgayBD.getValue();
            if (ngayBD.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Ngày bắt đầu không được trống"); return; }
            String ngayKT = ckVoThoiHan.isSelected() ? "3000-01-01" : tfNgayKT.getValue();
            if (ngayKT.isEmpty()) ngayKT = "3000-01-01";
            String trangThai = isEdit ? (String) cbTrangThai.getSelectedItem() : "Còn hiệu lực";

            ArrayList<Integer> dichVuChon = new ArrayList<>();
            for (JCheckBox cb : ckDvs) if (cb.isSelected()) dichVuChon.add((int) cb.getClientProperty("maDV"));

            ArrayList<Integer> nguoiOGhepChon = new ArrayList<>();
            for (ThongTinKhachThue k : dsGhepChon) nguoiOGhepChon.add(k.getMaKhachThue());

            // Validate: NgayVao của mỗi người ở ghép không được trống
            for (ThongTinKhachThue k : dsGhepChon) {
                DateMaskField df = mapNgayVaoFields.get(k);
                if (df == null || df.getValue().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dlg,
                            "Ngày vào của \"" + k.getHoTen() + "\" không được để trống.\n"
                            + "Vui lòng nhập ngày vào cho tất cả người ở ghép.",
                            "Thiếu ngày vào", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            if (nguoiOGhepChon.contains(maKhach)) {
                JOptionPane.showMessageDialog(dlg,
                        "Khách thuê và người ở ghép không được cùng một đối tượng.\nHãy kiểm tra và thử lại.",
                        "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int chiSoDien = 0, chiSoNuoc = 0;
            if (!isEdit) {
                try { chiSoDien = Integer.parseInt(tfChiSoDien.getText().trim()); }
                catch (Exception ignore) {}
                try { chiSoNuoc = Integer.parseInt(tfChiSoNuoc.getText().trim()); }
                catch (Exception ignore) {}
            }

            try {
                if (isEdit && "Hết hiệu lực".equals(trangThai)) {
                    // Kiểm tra hợp đồng rác: thời hạn ngày bắt đầu → ngày kết thúc < 5 ngày
                    boolean isTrashContract = false;
                    try {
                        LocalDate startDate = LocalDate.parse(ngayBD);
                        LocalDate endDate   = LocalDate.parse(ngayKT);
                        long days = ChronoUnit.DAYS.between(startDate, endDate);
                        isTrashContract = days >= 0 && days <= 5;
                    } catch (Exception ignore) {}

                    int soHD = HopDongDAO.demHoaDonSauHopDong(maPhong, hd.getMaHopDong());

                    if (isTrashContract) {
                        // Hợp đồng rác (dưới 5 ngày): xóa luôn kể cả có hóa đơn
                        if (soHD > 0) {
                            HopDongDAO.xoaHopDongVaHoaDon(hd.getMaHopDong(), maPhong);
                        } else {
                            HopDongDAO.xoaHopDong(hd.getMaHopDong());
                        }
                        dlg.dispose();
                        hienThiDanhSach();
                        JOptionPane.showMessageDialog(null,
                                "Hợp đồng đã bị xóa vì thời hạn hợp đồng ≤ 5 ngày.",
                                "Đã xóa hợp đồng rác", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    // Trên 5 ngày → lưu vào lịch sử, fall through xuống suaHopDong
                }
                if (isEdit) {
                    // 1. Ghi NgayDi cho những người vừa bị xóa khỏi danh sách.
                    //    Phải làm TRƯỚC khi gọi suaHopDong (luuNguoiOGhep chỉ DELETE NgayDi IS NULL
                    //    → người đã có NgayDi sẽ không bị xóa, lịch sử được bảo toàn).
                    for (java.util.Map.Entry<ThongTinKhachThue, LocalDate> entry : mapNgayDiGhep.entrySet()) {
                        HopDongDAO.ghiNgayDiNguoiOGhep(
                                hd.getMaHopDong(),
                                entry.getKey().getMaKhachThue(),
                                entry.getValue());
                    }
                    // 2. Lưu/cập nhật danh sách người ở ghép còn lại
                    HopDongDAO.suaHopDong(hd.getMaHopDong(), maPhong, maKhach, gia, coc,
                            ngayBD, ngayKT, tfHinhAnh.getText().trim(), trangThai, tfGhiChu.getText().trim(),
                            dichVuChon, nguoiOGhepChon);
                    // 3. Lưu NgayVao cho từng người ghép còn ở
                    for (ThongTinKhachThue kg : dsGhepChon) {
                        DateMaskField df = mapNgayVaoFields.get(kg);
                        LocalDate ngayVao = null;
                        if (df != null && !df.getValue().isEmpty()) {
                            try { ngayVao = LocalDate.parse(df.getValue()); } catch (Exception ignore) {}
                        }
                        HopDongDAO.capNhatNgayVaoNguoiOGhep(hd.getMaHopDong(), kg.getMaKhachThue(), ngayVao);
                    }
                } else {
                    HopDongDAO.themHopDongMoi(maPhong, maKhach, gia, coc,
                            ngayBD, ngayKT, tfHinhAnh.getText().trim(), trangThai, tfGhiChu.getText().trim(),
                            dichVuChon, chiSoDien, chiSoNuoc, nguoiOGhepChon);
                }
                dlg.dispose();
                hienThiDanhSach();
                if (isEdit && "Hết hiệu lực".equals(trangThai)) {
                    JOptionPane.showMessageDialog(null,
                            "Hợp đồng đã được lưu vào lịch sử.",
                            "Lưu lịch sử", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dlg, "Lỗi: " + ex.getMessage());
            }
        });
        btnRow.add(btnCancel);
        btnRow.add(btnSave);
        form.add(btnRow);

        JScrollPane sp = new JScrollPane(form);
        sp.setBorder(null);
        sp.getViewport().setBackground(Color.WHITE);
        dlg.add(sp);
        dlg.setVisible(true);
    }

    private JPanel formRow(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setFont(Theme.FONT_LABEL);
        l.setForeground(Theme.SLATE_700);
        if (!(field instanceof JScrollPane)) field.setPreferredSize(new Dimension(0, 34));
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, field instanceof JScrollPane ? 150 : 60));
        return p;
    }

    private static String formatGiaShort(String gia) {
        try { return String.format("%,d đ", Long.parseLong(gia.replaceAll("[^0-9]", ""))); }
        catch (Exception e) { return gia; }
    }

    /**
     * Hiển thị dialog nhập ngày rời đi của người ở ghép.
     * Tự động điền sẵn ngày hôm nay để thuận tiện.
     * @return LocalDate ngày rời đi, hoặc null nếu người dùng nhấn Hủy.
     */
    private static LocalDate showNgayDiDialog(java.awt.Window parent, String tenKhach) {
        JDialog dlgDi = new JDialog(parent, "Ngày rời đi", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlgDi.setSize(380, 200);
        dlgDi.setLocationRelativeTo(parent);
        dlgDi.getContentPane().setBackground(Color.WHITE);
        dlgDi.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 24, 16, 24));
        content.setBackground(Color.WHITE);

        JLabel lblInfo = new JLabel("<html><b>" + tenKhach + "</b> sẽ rời đi vào ngày:</html>");
        lblInfo.setFont(Theme.FONT_BASE);
        lblInfo.setForeground(Theme.SLATE_700);
        lblInfo.setAlignmentX(LEFT_ALIGNMENT);

        DateMaskField tfNgayDi = new DateMaskField();
        tfNgayDi.setValue(LocalDate.now().toString()); // điền sẵn ngày hôm nay
        tfNgayDi.setFont(Theme.FONT_BASE);
        tfNgayDi.setBackground(Color.WHITE);
        tfNgayDi.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblNote = new JLabel("<html><i><font color='#94A3B8'>Ngày rời đi sẽ được lưu vào lịch sử.</font></i></html>");
        lblNote.setFont(Theme.FONT_SMALL);
        lblNote.setAlignmentX(LEFT_ALIGNMENT);

        JPanel btnRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        JButton btnCancel = Theme.secondaryButton("Hủy");
        JButton btnConfirm = Theme.dangerButton("Xác nhận rời đi");

        LocalDate[] result = {null};
        btnCancel.addActionListener(e -> dlgDi.dispose());
        btnConfirm.addActionListener(e -> {
            String val = tfNgayDi.getValue();
            if (val == null || val.trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlgDi,
                        "Vui lòng nhập ngày rời đi.", "Thiếu ngày", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                result[0] = LocalDate.parse(val);
                dlgDi.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlgDi,
                        "Ngày không hợp lệ. Vui lòng nhập đúng định dạng yyyy-MM-dd.",
                        "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnRow.add(btnCancel);
        btnRow.add(btnConfirm);

        content.add(lblInfo);
        content.add(Box.createVerticalStrut(10));
        content.add(tfNgayDi);
        content.add(Box.createVerticalStrut(6));
        content.add(lblNote);
        content.add(Box.createVerticalStrut(14));
        content.add(btnRow);

        dlgDi.add(content);
        dlgDi.setVisible(true); // blocks until disposed
        return result[0];
    }
}