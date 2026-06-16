package QuanLiNhaTro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;

public class Panel_Khach_Thue extends JPanel {

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Mã KT", "Họ tên", "Trạng thái", "CCCD", "SĐT", "Email",
                         "Giới tính", "Ngày sinh", "Quê quán", "Biển số xe", "Ghi chú"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextField tfSearch = new JTextField();
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private ArrayList<ThongTinKhachThue> dsGoc = new ArrayList<>();

    private boolean showHistory = false;
    private JButton btnHienTai;
    private JButton btnLichSu;
    private JButton btnAdd;
    private JButton btnDelete;

    public Panel_Khach_Thue() {
        setBackground(Theme.SLATE_50);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 24, 20, 24));

        add(buildHeader(), BorderLayout.NORTH);

        Theme.styleTable(table);
        table.setRowSorter(sorter);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);

        JPanel body = new JPanel(new BorderLayout());
        body.setBorder(new EmptyBorder(12, 0, 0, 0));
        body.setOpaque(false);
        body.add(Theme.wrapScroll(table), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        hienThiDanhSach();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        // ── Dòng 1: tiêu đề ──────────────────────────────────────────────
        JPanel row1 = new JPanel(new BorderLayout());
        row1.setOpaque(false);
        JLabel title = new JLabel("Khách thuê");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.SLATE_900);
        row1.add(title, BorderLayout.WEST);
        header.add(row1);
        header.add(Box.createVerticalStrut(12));

        // ── Dòng 2: tabs + tìm kiếm + nút ────────────────────────────────
        JPanel row2 = new JPanel(new BorderLayout(8, 0));
        row2.setOpaque(false);

        // Tab toggle
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabPanel.setOpaque(false);

        btnHienTai = buildTabButton("Đang thuê / Chờ phòng", true);
        btnLichSu  = buildTabButton("Lịch sử di dời", false);
        btnHienTai.addActionListener(e -> setView(false));
        btnLichSu.addActionListener(e -> setView(true));
        tabPanel.add(btnHienTai);
        tabPanel.add(btnLichSu);
        row2.add(tabPanel, BorderLayout.WEST);

        // Tìm kiếm + nút thao tác
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        Theme.styleTextField(tfSearch);
        tfSearch.setPreferredSize(new Dimension(220, 34));
        tfSearch.putClientProperty("JTextField.placeholderText", "Tìm theo tên, CCCD, SĐT...");
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate (javax.swing.event.DocumentEvent e) { search(); }
            public void removeUpdate (javax.swing.event.DocumentEvent e) { search(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }
        });

        JButton btnReload = Theme.secondaryButton("");
        URL reloadUrl = Panel_Khach_Thue.class.getResource("icon/icon_reload.png");
        if (reloadUrl != null) {
            Image img = new ImageIcon(reloadUrl).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btnReload.setIcon(new ImageIcon(img));
        } else { btnReload.setText("🔄"); }
        JButton btnEdit   = Theme.warningButton("Sửa");
        btnDelete         = Theme.dangerButton("Xóa");
        btnAdd            = Theme.primaryButton("+ Thêm khách");

        btnReload.addActionListener(e -> {
            hienThiDanhSach();
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Panel_Khach_Thue.this),
                    "Dữ liệu đã được cập nhật!", "Cập nhật", JOptionPane.INFORMATION_MESSAGE);
        });
        btnEdit.addActionListener(e -> openFormEdit());
        btnDelete.addActionListener(e -> xoaKhach());
        btnAdd.addActionListener(e -> openFormAdd());

        right.add(Theme.wrapWithSearchIcon(tfSearch));
        right.add(btnReload);
        right.add(btnDelete);
        right.add(btnEdit);
        right.add(btnAdd);
        row2.add(right, BorderLayout.EAST);

        header.add(row2);
        return header;
    }

    private JButton buildTabButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_BASE_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(220, 34));
        applyTabStyle(btn, active);
        return btn;
    }

    private void applyTabStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(Theme.TEAL_600);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(Theme.SLATE_200);
            btn.setForeground(Theme.SLATE_600);
        }
    }

    private void setView(boolean history) {
        showHistory = history;
        applyTabStyle(btnHienTai, !history);
        applyTabStyle(btnLichSu,  history);
        btnAdd.setVisible(!history);
        btnDelete.setVisible(!history);
        tfSearch.setText("");
        hienThiDanhSach();
    }

    public void hienThiDanhSach() {
        model.setRowCount(0);
        if (showHistory) {
            dsGoc = KhachThueDAO.getKhachThueDaDiDoi();
            for (ThongTinKhachThue k : dsGoc) model.addRow(k.getDiDoiRow());
        } else {
            dsGoc = KhachThueDAO.getKhachThueHienTai();
            for (ThongTinKhachThue k : dsGoc) model.addRow(k.getThongTinKhachThue());
        }
    }

    private void search() {
        sorter.setRowFilter(Theme.searchFilter(tfSearch.getText()));
    }

    private void xoaKhach() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn khách cần xóa"); return; }
        int modelRow = table.convertRowIndexToModel(row);
        int maKT   = (int)    model.getValueAt(modelRow, 0);
        String hoTen = (String) model.getValueAt(modelRow, 1);

        // Chặn nếu đang ở: kiểm tra cả chủ HĐ lẫn ở ghép, cả "Còn hiệu lực" lẫn "Chờ nhận phòng"
        if (KhachThueDAO.dangOHienTai(maKT)) {
            JOptionPane.showMessageDialog(this,
                    "<html>Khách <b>\"" + hoTen + "\"</b> đang có hợp đồng còn hiệu lực.<br>Không thể xóa.</html>",
                    "Không thể xóa", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int soHD = KhachThueDAO.demHopDong(maKT);
        String msg = soHD > 0
                ? "<html>Khách <b>\"" + hoTen + "\"</b> có <b>" + soHD + "</b> hợp đồng trong lịch sử.<br>"
                  + "Xóa sẽ xóa luôn toàn bộ hợp đồng cũ và dữ liệu liên quan.<br>"
                  + "Bạn có chắc muốn xóa?</html>"
                : "Xóa khách \"" + hoTen + "\"? Thao tác này không thể hoàn tác.";

        int ok = JOptionPane.showConfirmDialog(this, msg, "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            if (soHD > 0) KhachThueDAO.xoaKhachThueVaHopDong(maKT);
            else          KhachThueDAO.xoaKhachThue(maKT);
            hienThiDanhSach();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Xóa thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openFormAdd() { showForm(null); }

    private void openFormEdit() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn khách cần sửa"); return; }
        int modelRow = table.convertRowIndexToModel(row);
        int maKT = (int) model.getValueAt(modelRow, 0);
        for (ThongTinKhachThue k : dsGoc) {
            if (k.getMaKhachThue() == maKT) { showForm(k); return; }
        }
    }

    private void showForm(ThongTinKhachThue k) {
        boolean isEdit = k != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Sửa khách thuê" : "Thêm khách thuê", true);
        dlg.setSize(480, 660);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        form.setBackground(Color.WHITE);

        JTextField tfHoTen     = new JTextField();
        JTextField tfCCCD      = new JTextField();
        JTextField tfSDT       = new JTextField();
        JTextField tfEmail     = new JTextField();
        JComboBox<String> cbGT = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        JTextField tfQQ        = new JTextField();
        JTextField tfBienSoXe  = new JTextField();
        JTextField tfGhiChu    = new JTextField();

        for (JTextField t : new JTextField[]{tfHoTen, tfCCCD, tfSDT, tfEmail, tfQQ, tfBienSoXe, tfGhiChu})
            Theme.styleTextField(t);
        Theme.styleComboBox(cbGT);

        DateMaskField tfNgaySinh = new DateMaskField();
        tfNgaySinh.setFont(Theme.FONT_BASE);
        tfNgaySinh.setBackground(Color.WHITE);

        if (isEdit) {
            tfHoTen.setText(k.getHoTen());
            tfCCCD.setText(k.getSoCCCD());
            tfSDT.setText(k.getSoDienThoai());
            tfEmail.setText(safe(k.getEmail()));
            cbGT.setSelectedItem(k.getGioiTinh() == null ? "Nam" : k.getGioiTinh());
            tfNgaySinh.setValue(safe(k.getNgaySinh()));
            tfQQ.setText(safe(k.getQueQuan()));
            tfBienSoXe.setText(safe(k.getBienSoXe()));
            tfGhiChu.setText(safe(k.getGhiChu()));
        }

        form.add(formRow("Họ tên *", tfHoTen));
        form.add(Box.createVerticalStrut(10));
        form.add(formRow("CCCD *", tfCCCD));
        form.add(Box.createVerticalStrut(10));
        form.add(formRow("Số điện thoại *", tfSDT));
        form.add(Box.createVerticalStrut(10));
        form.add(formRow("Email", tfEmail));
        form.add(Box.createVerticalStrut(10));
        form.add(formRow("Giới tính", cbGT));
        form.add(Box.createVerticalStrut(10));
        form.add(formRow("Ngày sinh  (dd/MM/yyyy)", tfNgaySinh));
        form.add(Box.createVerticalStrut(10));
        form.add(formRow("Quê quán", tfQQ));
        form.add(Box.createVerticalStrut(10));
        form.add(formRow("Biển số xe", tfBienSoXe));
        form.add(Box.createVerticalStrut(10));
        form.add(formRow("Ghi chú", tfGhiChu));
        form.add(Box.createVerticalStrut(20));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        JButton btnCancel = Theme.secondaryButton("Hủy");
        btnCancel.addActionListener(e -> dlg.dispose());
        JButton btnSave = Theme.primaryButton(isEdit ? "Cập nhật" : "Thêm");
        btnSave.addActionListener(e -> {
            String hoTen   = tfHoTen.getText().trim();
            String cccd    = tfCCCD.getText().trim();
            String sdt     = tfSDT.getText().trim();
            if (hoTen.isEmpty() || cccd.isEmpty() || sdt.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Họ tên, CCCD, SĐT không được trống");
                return;
            }
            String ngaySinh = tfNgaySinh.getValue();
            if (isEdit) {
                if (KhachThueDAO.kiemTraTonTaiCCCDKhiSua(cccd, k.getMaKhachThue())) {
                    JOptionPane.showMessageDialog(dlg, "CCCD đã tồn tại trên hệ thống");
                    return;
                }
                KhachThueDAO.suaKhachThue(k.getMaKhachThue(), hoTen, cccd, sdt,
                        tfEmail.getText().trim(), (String) cbGT.getSelectedItem(),
                        ngaySinh, tfQQ.getText().trim(),
                        tfBienSoXe.getText().trim(), tfGhiChu.getText().trim());
            } else {
                if (KhachThueDAO.kiemTraTonTaiCCCD(cccd)) {
                    JOptionPane.showMessageDialog(dlg, "CCCD đã tồn tại trên hệ thống");
                    return;
                }
                KhachThueDAO.themKhachThueMoi(new ThongTinKhachThue(hoTen, cccd, sdt,
                        tfEmail.getText().trim(), (String) cbGT.getSelectedItem(),
                        ngaySinh, tfQQ.getText().trim(),
                        tfBienSoXe.getText().trim(), tfGhiChu.getText().trim()));
            }
            dlg.dispose();
            hienThiDanhSach();
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
        field.setPreferredSize(new Dimension(0, 34));
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        return p;
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
