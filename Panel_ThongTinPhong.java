package QuanLiNhaTro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.function.Consumer;

public class Panel_ThongTinPhong extends JPanel {

    private final JPanel gridPanel = new JPanel(new GridBagLayout());
    private final JTextField tfSearch = new JTextField();
    private final JComboBox<String> cbFilter = new JComboBox<>(new String[]{"Tất cả", "Đang thuê", "Trống"});
    private final Consumer<ThongTinPhong> onOpenDetail;
    private ArrayList<ThongTinPhong> dsGoc = new ArrayList<>();

    public Panel_ThongTinPhong(Consumer<ThongTinPhong> onOpenDetail) {
        this.onOpenDetail = onOpenDetail;
        setBackground(Theme.SLATE_50);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // === Header ===
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        JLabel title = new JLabel("Danh sách phòng");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.SLATE_900);
        header.add(title, BorderLayout.WEST);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightControls.setOpaque(false);

        Theme.styleTextField(tfSearch);
        tfSearch.setPreferredSize(new Dimension(220, 34));
        tfSearch.putClientProperty("JTextField.placeholderText", "Tìm phòng...");
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate (javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate (javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        Theme.styleComboBox(cbFilter);
        cbFilter.setPreferredSize(new Dimension(130, 34));
        cbFilter.addActionListener(e -> applyFilter());

        JButton btnAdd = Theme.primaryButton("+ Thêm phòng");
        btnAdd.addActionListener(e -> openFormAdd());

        JButton btnReload = Theme.secondaryButton(" Cập nhật");
        URL reloadUrl = Panel_ThongTinPhong.class.getResource("icon/icon_reload.png");
        if (reloadUrl != null) {
            Image img = new ImageIcon(reloadUrl).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btnReload.setIcon(new ImageIcon(img));
        }
        btnReload.addActionListener(e -> {
            hienThiDanhSach();
            JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(Panel_ThongTinPhong.this),
                    "Dữ liệu đã được cập nhật!",
                    "Cập nhật",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        rightControls.add(Theme.wrapWithSearchIcon(tfSearch));
        rightControls.add(cbFilter);
        rightControls.add(btnReload);
        rightControls.add(btnAdd);
        header.add(rightControls, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // === Grid ===
        gridPanel.setBackground(Theme.SLATE_50);
        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.SLATE_50);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        hienThiDanhSach();
    }

    public void hienThiDanhSach() {
        RoomDAO.dongBoTrangThaiPhong();
        dsGoc = RoomDAO.getThongTinPhong();
        applyFilter();
    }

    private void applyFilter() {
        String q = tfSearch.getText().trim().toLowerCase();
        String filter = (String) cbFilter.getSelectedItem();

        ArrayList<ThongTinPhong> filtered = new ArrayList<>();
        for (ThongTinPhong p : dsGoc) {
            if (!q.isEmpty() && !Theme.matchesSearch(q,
                    p.getMaPhong(), p.getTenPhong(), p.getDienTich(),
                    p.getGiaThue(), p.getTrangThaiGoc())) continue;
            if ("Đang thuê".equals(filter) && !p.isDangO()) continue;
            if ("Trống".equals(filter) && p.isDangO()) continue;
            filtered.add(p);
        }
        renderGrid(filtered);
    }

    private void renderGrid(ArrayList<ThongTinPhong> ds) {
        gridPanel.removeAll();
        gridPanel.setBorder(new EmptyBorder(16, 0, 16, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 16, 16);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        int cols = 4;
        for (int i = 0; i < ds.size(); i++) {
            gbc.gridx = i % cols;
            gbc.gridy = i / cols;
            gridPanel.add(buildCard(ds.get(i)), gbc);
        }

        // Fill khoảng trống bằng glue
        gbc.gridx = 0;
        gbc.gridy = (ds.size() / cols) + 1;
        gbc.gridwidth = cols;
        gbc.weighty = 1.0;
        gridPanel.add(Box.createGlue(), gbc);

        if (ds.isEmpty()) {
            JLabel empty = new JLabel("Không có phòng nào.", SwingConstants.CENTER);
            empty.setFont(Theme.FONT_BASE);
            empty.setForeground(Theme.SLATE_400);
            empty.setBorder(new EmptyBorder(60, 0, 60, 0));
            gridPanel.add(empty);
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel buildCard(ThongTinPhong p) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                new Theme.LineBorderRounded(p.isDangO() ? Theme.TEAL_300 : Theme.SLATE_200, 1, 12),
                new EmptyBorder(14, 16, 14, 16)
        ));

        // Top: tên + pill trạng thái
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setAlignmentX(LEFT_ALIGNMENT);
        JLabel name = new JLabel(p.getTenPhong());
        name.setFont(new Font("Segoe UI", Font.BOLD, 18));
        name.setForeground(Theme.SLATE_900);
        top.add(name, BorderLayout.WEST);

        JLabel pill = new JLabel(p.isDangO()
                ? Theme.pillInfo("Đang thuê")
                : Theme.pillSuccess("Trống"));
        top.add(pill, BorderLayout.EAST);

        card.add(top);
        card.add(Box.createVerticalStrut(10));

        // Thông tin
        card.add(infoLine("Diện tích:", safeText(p.getDienTich())));
        card.add(Box.createVerticalStrut(4));
        card.add(infoLine("Giá thuê:", formatGia(p.getGiaThue())));

        card.add(Box.createVerticalStrut(14));

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        JButton btnDetail = Theme.primaryButton("Chi tiết");
        btnDetail.addActionListener(e -> onOpenDetail.accept(p));
        JButton btnEdit = Theme.warningButton("Sửa");
        btnEdit.addActionListener(e -> openFormEdit(p));
        JButton btnDelete = Theme.dangerButton("Xóa");
        btnDelete.addActionListener(e -> xoaPhong(p));

        btnRow.add(btnDetail);
        btnRow.add(btnEdit);
        btnRow.add(btnDelete);
        card.add(btnRow);

        // Click on card → detail
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        name.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onOpenDetail.accept(p); }
        });

        return card;
    }

    private JPanel infoLine(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.SLATE_500);
        l.setPreferredSize(new Dimension(90, 18));
        JLabel v = new JLabel(value);
        v.setFont(Theme.FONT_BASE_BOLD);
        v.setForeground(Theme.SLATE_900);
        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    private static String safeText(String s) { return s == null ? "—" : s; }

    private static String formatGia(String gia) {
        try {
            long g = Long.parseLong(gia.replaceAll("[^0-9]", ""));
            return String.format("%,d đ", g);
        } catch (Exception e) {
            return gia;
        }
    }

    private void xoaPhong(ThongTinPhong p) {
        if (p.isDangO()) {
            JOptionPane.showMessageDialog(this,
                    "Phòng " + p.getTenPhong() + " đang có người ở, không thể xóa.\nVui lòng thanh lý hợp đồng trước.",
                    "Không thể xóa", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa phòng " + p.getTenPhong() + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            if (RoomDAO.xoaPhong(p.getMaPhong())) {
                hienThiDanhSach();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Xóa phòng thất bại. Vui lòng kiểm tra lại.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openFormAdd() {
        showFormPhong(null);
    }

    private void openFormEdit(ThongTinPhong p) {
        showFormPhong(p);
    }

    /** Form thêm/sửa phòng — dialog đơn giản. */
    private void showFormPhong(ThongTinPhong p) {
        boolean isEdit = p != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Sửa phòng" : "Thêm phòng mới", true);
        dlg.setSize(420, isEdit ? 360 : 320);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        form.setBackground(Color.WHITE);

        // Trạng thái read-only ở đầu form (chỉ khi sửa)
        if (isEdit) {
            JLabel pillLabel = new JLabel(p.isDangO()
                    ? Theme.pillInfo("Đang thuê")
                    : Theme.pillSuccess("Trống"));
            pillLabel.setHorizontalAlignment(SwingConstants.CENTER);
            pillLabel.setAlignmentX(CENTER_ALIGNMENT);
            form.add(pillLabel);
            form.add(Box.createVerticalStrut(16));
        }

        JTextField tfTen = new JTextField();
        JTextField tfDT  = new JTextField();
        JTextField tfGia = new JTextField();
        Theme.styleTextField(tfTen);
        Theme.styleTextField(tfDT);
        Theme.styleTextField(tfGia);

        if (isEdit) {
            tfTen.setText(p.getTenPhong());
            tfDT.setText(p.getDienTich());
            tfGia.setText(p.getGiaThue());
        } else {
            tfDT.setText("20m²");
            tfGia.setText("2000000");
        }

        form.add(formRow("Tên phòng *", tfTen));
        form.add(Box.createVerticalStrut(10));
        form.add(formRow("Diện tích",   tfDT));
        form.add(Box.createVerticalStrut(10));
        form.add(formRow("Giá thuê (VND)", tfGia));
        form.add(Box.createVerticalStrut(20));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        JButton btnCancel = Theme.secondaryButton("Hủy");
        btnCancel.addActionListener(e -> dlg.dispose());
        JButton btnSave = Theme.primaryButton(isEdit ? "Cập nhật" : "Thêm");
        btnSave.addActionListener(e -> {
            String ten = tfTen.getText().trim();
            String dt  = tfDT.getText().trim();
            String gia = tfGia.getText().trim();
            if (ten.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Tên phòng không được trống");
                return;
            }
            try { Long.parseLong(gia.replaceAll("[^0-9]", "")); }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Giá thuê phải là số");
                return;
            }
            if (RoomDAO.kiemTraTenPhongTonTai(ten, isEdit ? p.getMaPhong() : null)) {
                JOptionPane.showMessageDialog(dlg,
                        "Tên phòng '" + ten + "' đã tồn tại.\nVui lòng chọn tên khác.",
                        "Trùng tên phòng", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Trạng thái do hợp đồng tự quản lý — giữ nguyên khi sửa, mặc định Trống khi thêm
            String tt = isEdit ? p.getTrangThaiGoc() : "Trống";
            if (isEdit) RoomDAO.suaPhong(p.getMaPhong(), ten, dt, gia, tt);
            else        RoomDAO.themPhong(ten, dt, gia, tt);
            dlg.dispose();
            hienThiDanhSach();
        });
        btnRow.add(btnCancel);
        btnRow.add(btnSave);
        form.add(btnRow);

        dlg.add(form);
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
}
