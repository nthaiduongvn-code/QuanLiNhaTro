package QuanLiNhaTro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;

public class Panel_DichVu extends JPanel {

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Mã DV", "Tên dịch vụ", "Đơn giá", "Đơn vị", "Tính theo", "Ghi chú"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private ArrayList<ThongTinDichVu> dsGoc = new ArrayList<>();

    public Panel_DichVu() {
        setBackground(Theme.SLATE_50);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JLabel title = new JLabel("Dịch vụ");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.SLATE_900);
        header.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton btnAdd      = Theme.primaryButton("+ Thêm dịch vụ");
        JButton btnEdit     = Theme.warningButton("Sửa");
        JButton btnDelete   = Theme.dangerButton("Xóa");
        JButton btnApDung   = Theme.infoButton("Áp dụng vào tất cả phòng");
        JButton btnReload   = Theme.secondaryButton("");
        URL reloadUrl = Panel_DichVu.class.getResource("icon/icon_reload.png");
        if (reloadUrl != null) {
            Image img = new ImageIcon(reloadUrl).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btnReload.setIcon(new ImageIcon(img));
        } else { btnReload.setText("🔄"); }

        btnAdd.addActionListener(e -> showForm(null));
        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        btnReload.addActionListener(e -> {
            hienThiDanhSach();
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Panel_DichVu.this),
                    "Dữ liệu đã được cập nhật!", "Cập nhật", JOptionPane.INFORMATION_MESSAGE);
        });
        btnApDung.addActionListener(e -> apDungDichVuDaChon());

        right.add(btnReload);
        right.add(btnDelete);
        right.add(btnEdit);
        right.add(btnApDung);
        right.add(btnAdd);
        header.add(right, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Body
        Theme.styleTable(table);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);
        JPanel body = new JPanel(new BorderLayout());
        body.setBorder(new EmptyBorder(16, 0, 0, 0));
        body.setOpaque(false);
        body.add(Theme.wrapScroll(table), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        hienThiDanhSach();
    }

    public void hienThiDanhSach() {
        model.setRowCount(0);
        dsGoc = DichVuDAO.getThongTinDichVu();
        for (ThongTinDichVu dv : dsGoc) {
            model.addRow(new Object[]{
                    dv.getMaDichVu(),
                    dv.getTenDichVu(),
                    String.format("%,d đ", dv.getGiaDichVu()),
                    dv.getDonVi(),
                    dv.isTheoNguoi() ? "Theo đầu người" : "Theo phòng",
                    dv.getGhiChu() == null ? "" : dv.getGhiChu()
            });
        }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn dịch vụ cần sửa"); return; }
        String maDV = (String) model.getValueAt(row, 0);
        for (ThongTinDichVu dv : dsGoc) {
            if (dv.getMaDichVu().equals(maDV)) { showForm(dv); return; }
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn dịch vụ cần xóa"); return; }
        String maDV = (String) model.getValueAt(row, 0);
        String ten = (String) model.getValueAt(row, 1);

        ThongTinDichVu dvToDelete = null;
        for (ThongTinDichVu d : dsGoc) {
            if (d.getMaDichVu().equals(maDV)) { dvToDelete = d; break; }
        }

        if (dvToDelete != null && dvToDelete.isLaDichVuMacDinh()) {
            JOptionPane.showMessageDialog(this,
                    "Dịch vụ \"" + ten + "\" là dịch vụ hệ thống mặc định, không thể xóa.",
                    "Không thể xóa", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int soHDHienTai = DichVuDAO.demHopDongHienTaiDangDung(maDV);
        String cauHoi;
        if (soHDHienTai > 0) {
            cauHoi = "⚠ Dịch vụ \"" + ten + "\" đang được dùng bởi " + soHDHienTai
                    + " hợp đồng còn hiệu lực.\n"
                    + "Xóa sẽ gỡ dịch vụ này khỏi tất cả hợp đồng đó ngay lập tức.\n\n"
                    + "Bạn có chắc muốn xóa không?";
        } else {
            cauHoi = "Xóa dịch vụ \"" + ten + "\"?\nCác tham chiếu trong lịch sử hợp đồng cũng sẽ bị xóa.";
        }
        int ok = JOptionPane.showConfirmDialog(this, cauHoi, "Xác nhận xóa", JOptionPane.YES_NO_OPTION,
                soHDHienTai > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.QUESTION_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            if (DichVuDAO.xoaDichVu(maDV)) hienThiDanhSach();
        }
    }

    private void showForm(ThongTinDichVu dv) {
        boolean isEdit = dv != null;
        boolean isSystemDV = isEdit && dv.isLaDichVuMacDinh();

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Sửa dịch vụ" : "Thêm dịch vụ", true);
        dlg.setSize(420, 430);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        form.setBackground(Color.WHITE);

        JTextField tfTen = new JTextField();
        JTextField tfGia = new JTextField();
        JTextField tfDonVi = new JTextField();
        JTextField tfGhiChu = new JTextField();
        for (JTextField t : new JTextField[]{tfTen, tfGia, tfDonVi, tfGhiChu}) Theme.styleTextField(t);

        JComboBox<String> cbCachTinh = new JComboBox<>(new String[]{"Theo phòng", "Theo đầu người"});
        Theme.styleComboBox(cbCachTinh);

        if (isEdit) {
            tfTen.setText(dv.getTenDichVu());
            tfGia.setText(String.valueOf(dv.getGiaDichVu()));
            tfDonVi.setText(dv.getDonVi() == null ? "" : dv.getDonVi());
            tfGhiChu.setText(dv.getGhiChu() == null ? "" : dv.getGhiChu());
            cbCachTinh.setSelectedIndex(dv.isTheoNguoi() ? 1 : 0);
            if (isSystemDV) tfTen.setEnabled(false);
        }
        // Điện/Nước tính theo chỉ số riêng — không áp dụng "Tính theo", luôn cố định "Theo phòng"
        if (isSystemDV) cbCachTinh.setEnabled(false);

        form.add(formRow("Tên dịch vụ *", tfTen));
        form.add(Box.createVerticalStrut(10));
        form.add(formRow("Đơn giá (VND) *", tfGia));
        form.add(Box.createVerticalStrut(10));
        form.add(formRow("Đơn vị tính", tfDonVi));
        form.add(Box.createVerticalStrut(10));
        form.add(formRow("Tính theo *", cbCachTinh));
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
            String ten = tfTen.getText().trim();
            String gia = tfGia.getText().trim();
            if (ten.isEmpty() || gia.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Tên và giá không được trống");
                return;
            }
            int g;
            try { g = Integer.parseInt(gia.replaceAll("[^0-9]", "")); }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Giá phải là số");
                return;
            }
            String cachTinh = cbCachTinh.getSelectedIndex() == 1
                    ? ThongTinDichVu.THEO_NGUOI : ThongTinDichVu.THEO_PHONG;
            if (isEdit) {
                DichVuDAO.suaDichVu(dv.getMaDichVu(), ten, g, tfDonVi.getText().trim(), tfGhiChu.getText().trim(), cachTinh);
            } else {
                DichVuDAO.themDichVuMoi(new ThongTinDichVu(ten, g, tfDonVi.getText().trim(), tfGhiChu.getText().trim(), cachTinh));
            }
            dlg.dispose();
            hienThiDanhSach();  // BUG FIX: trước đây không refresh
        });
        btnRow.add(btnCancel);
        btnRow.add(btnSave);
        form.add(btnRow);

        dlg.add(form);
        dlg.setVisible(true);
    }

    private void apDungDichVuDaChon() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn dịch vụ muốn áp dụng vào tất cả phòng.");
            return;
        }
        String maDV = (String) model.getValueAt(row, 0);
        String tenDV = (String) model.getValueAt(row, 1);

        int ok = JOptionPane.showConfirmDialog(this,
                "Áp dụng dịch vụ \"" + tenDV + "\" vào tất cả phòng đang thuê?\n"
                + "Phòng nào đã có dịch vụ này sẽ không bị thêm trùng.",
                "Xác nhận áp dụng", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        int soPhong = DichVuDAO.apDungMotDichVuVaoTatCaPhong(maDV);
        if (soPhong < 0) {
            JOptionPane.showMessageDialog(this,
                    "Có lỗi xảy ra khi áp dụng dịch vụ.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        } else if (soPhong == 0) {
            JOptionPane.showMessageDialog(this,
                    "Tất cả phòng đang thuê đã có dịch vụ \"" + tenDV + "\", không cần cập nhật.",
                    "Không có thay đổi", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Đã thêm dịch vụ \"" + tenDV + "\" vào " + soPhong + " phòng đang thuê.",
                    "Áp dụng thành công", JOptionPane.INFORMATION_MESSAGE);
        }
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
}
