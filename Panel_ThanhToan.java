package QuanLiNhaTro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.EventObject;

public class Panel_ThanhToan extends JPanel {

    private final JComboBox<String> cbThangNam = new JComboBox<>();
    private final HoaDonTableModel tableModel = new HoaDonTableModel();
    private final JTable table = new JTable(tableModel);
    private final JLabel lblStats = new JLabel(" ");
    private final JTextField tfSearch = new JTextField();
    private JButton btnEmail;

    private ArrayList<ThongTinHoaDon> dsGoc = new ArrayList<>();
    private boolean isReloading = false;
    private String lastAutoGenMonth = "";
    private int autoCreated = 0;
    private int autoSkipped = 0;

    public Panel_ThanhToan() {
        setBackground(Theme.SLATE_50);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 24, 20, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);

        reload();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setOpaque(false);

        JPanel row1 = new JPanel(new BorderLayout(12, 0));
        row1.setOpaque(false);

        JLabel title = new JLabel("Thanh toán hóa đơn");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.SLATE_900);
        row1.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        Theme.styleComboBox(cbThangNam);
        cbThangNam.setPreferredSize(new Dimension(120, 34));
        cbThangNam.addActionListener(e -> reload());

        // Thanh tìm kiếm với icon rõ ràng
        Theme.styleTextField(tfSearch);
        tfSearch.setPreferredSize(new Dimension(200, 34));
        tfSearch.putClientProperty("JTextField.placeholderText", "Tìm theo tên phòng...");
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate (javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate (javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        JButton btnTaoHoaDon = Theme.successButton("Tạo hóa đơn tháng");
        btnTaoHoaDon.addActionListener(e -> taoHoaDonTuDong());

        btnEmail = Theme.infoButton("Gửi mail");
        btnEmail.addActionListener(e -> guiMailHangLoat());

        JButton btnReload = Theme.secondaryButton("");
        URL reloadUrl = Panel_ThanhToan.class.getResource("icon/icon_reload.png");
        if (reloadUrl != null) {
            Image img = new ImageIcon(reloadUrl).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btnReload.setIcon(new ImageIcon(img));
        } else { btnReload.setText("🔄"); }
        btnReload.addActionListener(e -> {
            reload();
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Panel_ThanhToan.this),
                    "Dữ liệu đã được cập nhật!", "Cập nhật", JOptionPane.INFORMATION_MESSAGE);
        });

        right.add(new JLabel("Kỳ:"));
        right.add(cbThangNam);
        right.add(Theme.wrapWithSearchIcon(tfSearch));
        right.add(btnReload);
        right.add(btnEmail);
        right.add(btnTaoHoaDon);
        row1.add(right, BorderLayout.EAST);

        header.add(row1, BorderLayout.NORTH);

        lblStats.setFont(Theme.FONT_BASE);
        lblStats.setForeground(Theme.SLATE_600);
        lblStats.setBorder(new EmptyBorder(8, 0, 0, 0));
        header.add(lblStats, BorderLayout.SOUTH);

        return header;
    }

    private JPanel buildTable() {
        Theme.styleTable(table);
        table.setRowHeight(48);
        // Ẩn cột Mã HĐ
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        // Cột tiền: căn phải
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int i = 2; i <= 6; i++) table.getColumnModel().getColumn(i).setCellRenderer(right);

        // Cột trạng thái (7): checkbox renderer + click để toggle
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusCheckboxRenderer());
        table.getColumnModel().getColumn(7).setMinWidth(170);
        table.getColumnModel().getColumn(7).setMaxWidth(200);

        // Click vào cột trạng thái để toggle
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int viewCol = table.columnAtPoint(e.getPoint());
                int viewRow = table.rowAtPoint(e.getPoint());
                if (viewCol == 7 && viewRow >= 0) {
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    ThongTinHoaDon hd = tableModel.getRow(modelRow);
                    String newStatus = hd.isPaid() ? "Chưa thanh toán" : "Đã thanh toán";
                    TongHopHoaDonDAO.capNhatTrangThai(hd.getMaHoaDon(), newStatus);
                    reload();
                }
            }
        });

        // Cột action: nút Xem & Gửi
        ActionCellRenderer acr = new ActionCellRenderer();
        ActionCellEditor   ace = new ActionCellEditor();
        table.getColumnModel().getColumn(8).setCellRenderer(acr);
        table.getColumnModel().getColumn(8).setCellEditor(ace);
        table.getColumnModel().getColumn(8).setMinWidth(130);
        table.getColumnModel().getColumn(8).setMaxWidth(160);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 0, 0, 0));
        body.add(Theme.wrapScroll(table), BorderLayout.CENTER);
        return body;
    }

    public void reload() {
        if (isReloading) return;
        isReloading = true;
        try {
            // Tự động tạo hóa đơn cho tháng hiện tại (chỉ chạy 1 lần/tháng trong phiên này)
            LocalDate now = LocalDate.now();
            String currentMonth = String.format("%02d-%04d", now.getMonthValue(), now.getYear());
            if (!currentMonth.equals(lastAutoGenMonth) || autoSkipped > 0) {
                int[] r = TongHopHoaDonDAO.taoHoaDonChoThang(currentMonth);
                lastAutoGenMonth = currentMonth;
                autoCreated = r[0];    // mới tạo
                autoSkipped = r[2];    // thiếu chỉ số (r[1] = đã cập nhật)
            }

            String prevSelected = (String) cbThangNam.getSelectedItem();
            cbThangNam.removeAllItems();
            ArrayList<String> ds = TongHopHoaDonDAO.layDanhSachThangNam();
            for (String t : ds) cbThangNam.addItem(t);
            if (prevSelected != null && ds.contains(prevSelected)) cbThangNam.setSelectedItem(prevSelected);

            String thang = (String) cbThangNam.getSelectedItem();
            if (thang == null) {
                tableModel.setData(new ArrayList<>());
                lblStats.setText("Chưa có dữ liệu.");
                return;
            }
            dsGoc = TongHopHoaDonDAO.getDanhSachHoaDonThang(thang);
            applyFilter();
            updateStats(thang);
        } finally {
            isReloading = false;
        }
    }

    private void applyFilter() {
        String q = tfSearch.getText().trim().toLowerCase();
        ArrayList<ThongTinHoaDon> filtered = new ArrayList<>();
        for (ThongTinHoaDon h : dsGoc) {
            if (!q.isEmpty() && !Theme.matchesSearch(q, h.toTableRow())) continue;
            filtered.add(h);
        }
        tableModel.setData(filtered);
    }

    private void updateStats(String thangNam) {
        long[] dt = TongHopHoaDonDAO.thongKeDoanhThu(thangNam);
        int totalHD = dsGoc.size();
        int daThanhToan = 0;
        for (ThongTinHoaDon h : dsGoc) if (h.isPaid()) daThanhToan++;

        StringBuilder sb = new StringBuilder("<html>");
        sb.append(String.format(
            "Tổng <b>%d</b> hóa đơn  •  Đã thu: <font color='#10B981'><b>%,d đ</b></font>" +
            "  •  Còn phải thu: <font color='#EF4444'><b>%,d đ</b></font>  •  Đã thanh toán: %d/%d",
            totalHD, dt[0], dt[1], daThanhToan, totalHD));

        if (autoCreated > 0 && thangNam.equals(lastAutoGenMonth)) {
            sb.append("  •  <font color='#0D9488'>✅ Tự động tạo <b>").append(autoCreated).append("</b> hóa đơn mới</font>");
            if (autoSkipped > 0)
                sb.append("  <font color='#F59E0B'>(⚠ ").append(autoSkipped).append(" phòng chưa có chỉ số)</font>");
        }
        sb.append("</html>");
        lblStats.setText(sb.toString());
    }

    // ── Tạo hóa đơn thủ công ──
    private void taoHoaDonTuDong() {
        String thang = (String) cbThangNam.getSelectedItem();
        String thangMoi = (String) JOptionPane.showInputDialog(this,
                "Tạo hóa đơn tự động cho kỳ nào?\n(Định dạng MM-YYYY)",
                "Tạo hóa đơn tháng", JOptionPane.PLAIN_MESSAGE,
                null, null, thang != null ? thang : "");
        if (thangMoi == null || !thangMoi.matches("\\d{2}-\\d{4}")) {
            if (thangMoi != null) JOptionPane.showMessageDialog(this, "Định dạng tháng không hợp lệ. Ví dụ: 06-2026");
            return;
        }
        int[] r = TongHopHoaDonDAO.taoHoaDonChoThang(thangMoi);
        // r[0]=daTao, r[1]=daCapNhat, r[2]=thieuChiSo
        String msg = "<html><b>Kết quả tạo hóa đơn cho kỳ " + thangMoi + ":</b><br><br>"
                + "✅ Tạo mới: <b>" + r[0] + "</b> hóa đơn<br>"
                + "🔄 Cập nhật lại: <b>" + r[1] + "</b> hóa đơn (giữ nguyên trạng thái thanh toán)<br>"
                + "⚠ Bỏ qua (thiếu/sai chỉ số): <b>" + r[2] + "</b> phòng";
        if (r[2] > 0) msg += "<br><br><i>Hãy kiểm tra và nhập đúng chỉ số điện/nước rồi bấm Tạo hóa đơn lại.</i>";
        msg += "</html>";
        JOptionPane.showMessageDialog(this, msg, "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
        // Reset trạng thái auto-gen để stats hiển thị đúng nếu tháng vừa tạo = tháng hiện tại
        lastAutoGenMonth = "";
        boolean has = false;
        for (int i = 0; i < cbThangNam.getItemCount(); i++) {
            if (thangMoi.equals(cbThangNam.getItemAt(i))) { has = true; break; }
        }
        if (!has) cbThangNam.addItem(thangMoi);
        cbThangNam.setSelectedItem(thangMoi);
        reload();
    }

    // ── Gửi mail hàng loạt ──
    private void guiMailHangLoat() {
        String thang = (String) cbThangNam.getSelectedItem();
        if (thang == null) { JOptionPane.showMessageDialog(this, "Chưa chọn kỳ hóa đơn."); return; }

        String[] options = {"Chưa thanh toán", "Đã thanh toán", "Tất cả"};
        String choice = (String) JOptionPane.showInputDialog(this,
                "Gửi mail thông báo đến khách thuê nào?",
                "Gửi mail hàng loạt", JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        if (choice == null) return;

        String filterTT = "Tất cả".equals(choice) ? null : choice;
        ArrayList<Object[]> list = TongHopHoaDonDAO.layHoaDonVoiEmail(thang, filterTT);

        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Không có khách thuê nào có email phù hợp trong kỳ " + thang + ".",
                    "Không có dữ liệu", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "Sẽ gửi " + list.size() + " email đến các khách thuê " + choice.toLowerCase()
                + " kỳ " + thang + ".\nTiếp tục?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        if (!AppConfig.emailConfigured()) {
            JOptionPane.showMessageDialog(this,
                    "Email chưa được cấu hình. Vui lòng kiểm tra file email.properties.",
                    "Lỗi cấu hình", JOptionPane.ERROR_MESSAGE);
            return;
        }

        btnEmail.setEnabled(false);
        new Thread(() -> {
            List<String[]> emails = new ArrayList<>();
            int skipped = 0;
            for (Object[] row : list) {
                int    maHoaDon = (int)    row[0];
                String tenPhong = (String) row[1];
                String email    = (String) row[3];
                ChiTietHoaDon ct = TongHopHoaDonDAO.layChiTietHoaDon(maHoaDon);
                if (ct == null) { skipped++; continue; }
                String subject = "Hóa đơn tiền phòng " + tenPhong + " - Kỳ " + thang;
                emails.add(new String[]{email, subject, EmailUtil.buildHtmlBienLai(ct)});
            }
            int[] result = EmailUtil.guiBulkHtml(emails);
            final int totalFailed = result[1] + skipped;
            SwingUtilities.invokeLater(() -> {
                btnEmail.setEnabled(true);
                JOptionPane.showMessageDialog(Panel_ThanhToan.this,
                        "Kết quả gửi mail:\n✅ Thành công: " + result[0] + "\n❌ Thất bại: " + totalFailed,
                        "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
            });
        }).start();
    }

    // ── Table model ──
    private static class HoaDonTableModel extends AbstractTableModel {
        private final String[] columns = {
            "Mã HĐ", "Phòng", "Tiền phòng", "Điện", "Nước",
            "Dịch vụ khác", "Tổng cộng", "Trạng thái", "Hành động"
        };
        private ArrayList<ThongTinHoaDon> data = new ArrayList<>();

        void setData(ArrayList<ThongTinHoaDon> d) {
            this.data = d == null ? new ArrayList<>() : d;
            fireTableDataChanged();
        }

        ThongTinHoaDon getRow(int row) { return data.get(row); }

        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int c) { return columns[c]; }
        @Override public boolean isCellEditable(int r, int c) { return c == 8; }
        @Override public Object getValueAt(int row, int col) {
            return data.get(row).toTableRow()[col];
        }
    }

    // ── Renderer cột Trạng thái: checkbox + văn bản màu ──
    private class StatusCheckboxRenderer extends JPanel implements TableCellRenderer {
        private final JCheckBox cb  = new JCheckBox();
        private final JLabel    lbl = new JLabel();

        StatusCheckboxRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 6, 0));
            setOpaque(true);
            cb.setOpaque(false);
            cb.setFocusable(false);
            lbl.setFont(Theme.FONT_BASE_BOLD);
            add(cb);
            add(lbl);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean selected, boolean focus, int row, int col) {
            int modelRow = t.convertRowIndexToModel(row);
            ThongTinHoaDon hd = tableModel.getRow(modelRow);
            cb.setSelected(hd.isPaid());
            lbl.setText(hd.isPaid() ? "Đã thanh toán" : "Chưa thanh toán");
            lbl.setForeground(hd.isPaid() ? new Color(0x10B981) : new Color(0xEF4444));
            Color bg = selected ? Theme.TEAL_50 : Color.WHITE;
            setBackground(bg);
            cb.setBackground(bg);
            return this;
        }
    }

    // ── Action cell: nút Xem & Gửi ──
    private class ActionCellRenderer extends JPanel implements TableCellRenderer {
        private final JButton btnView = Theme.primaryButton("Xem & Gửi");
        ActionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 4, 4));
            setOpaque(true);
            add(btnView);
        }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean s, boolean f, int r, int c) {
            setBackground(s ? Theme.TEAL_50 : Color.WHITE);
            return this;
        }
    }

    private class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel  panel   = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        private final JButton btnView = new JButton("Xem & Gửi");
        private ThongTinHoaDon currentHD;

        ActionCellEditor() {
            panel.setOpaque(true);
            panel.add(btnView);

            btnView.setBackground(Theme.TEAL_600);
            btnView.setForeground(Color.WHITE);
            btnView.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btnView.setBorderPainted(false);
            btnView.setFocusPainted(false);
            btnView.setOpaque(true);
            btnView.setPreferredSize(new Dimension(110, 30));

            btnView.addActionListener(e -> {
                fireEditingStopped();
                if (currentHD != null) openDialogBienLai(currentHD.getMaHoaDon());
            });
        }

        @Override public Object getCellEditorValue() { return "Hành động"; }
        @Override public boolean isCellEditable(EventObject e) { return true; }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            int modelRow = t.convertRowIndexToModel(r);
            currentHD = tableModel.getRow(modelRow);
            panel.setBackground(Theme.TEAL_50);
            return panel;
        }
    }

    private void openDialogBienLai(int maHoaDon) {
        ChiTietHoaDon ct = TongHopHoaDonDAO.layChiTietHoaDon(maHoaDon);
        if (ct == null) {
            JOptionPane.showMessageDialog(this, "Không tải được chi tiết hóa đơn");
            return;
        }
        new Dialog_BienLai((Frame) SwingUtilities.getWindowAncestor(this), ct, this::reload).setVisible(true);
    }
}