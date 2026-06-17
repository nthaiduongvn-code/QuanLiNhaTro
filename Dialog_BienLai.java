package QuanLiNhaTro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class Dialog_BienLai extends JDialog {

    private final ChiTietHoaDon ct;
    private final Runnable onClosed;

    public Dialog_BienLai(Frame owner, ChiTietHoaDon ct, Runnable onClosed) {
        super(owner, "Biên lai HD-" + String.format("%04d", ct.getMaHoaDon()), true);
        this.ct = ct;
        this.onClosed = onClosed;

        setSize(720, 760);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(Theme.SLATE_50);
        setLayout(new BorderLayout());

        // Preview HTML
        JEditorPane preview = new JEditorPane();
        preview.setContentType("text/html");
        preview.setEditable(false);
        preview.setText(EmailUtil.buildHtmlBienLai(ct));
        preview.setBackground(Theme.SLATE_50);
        preview.setCaretPosition(0);

        JScrollPane sp = new JScrollPane(preview);
        sp.setBorder(null);
        add(sp, BorderLayout.CENTER);

        // Buttons bar
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        buttons.setBackground(Color.WHITE);
        buttons.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.SLATE_200));

        JButton btnClose = Theme.secondaryButton("Đóng");
        btnClose.addActionListener(e -> {
            dispose();
            if (onClosed != null) onClosed.run();
        });

        JButton btnSavePdf = Theme.warningButton("Lưu PDF");
        btnSavePdf.addActionListener(e -> savePdf());

        JButton btnMail = Theme.primaryButton("Gửi mail (kèm PDF)");
        btnMail.addActionListener(e -> sendMail());

        // Toggle paid
        JButton btnTogglePaid = ct.getTrangThai() != null && ct.getTrangThai().contains("Đã")
                ? Theme.dangerButton("Hủy thanh toán")
                : Theme.successButton("Đánh dấu đã thanh toán");
        btnTogglePaid.addActionListener(e -> {
            String newStatus = ct.getTrangThai() != null && ct.getTrangThai().contains("Đã")
                    ? "Chưa thanh toán" : "Đã thanh toán";
            TongHopHoaDonDAO.capNhatTrangThai(ct.getMaHoaDon(), newStatus);
            dispose();
            if (onClosed != null) onClosed.run();
        });

        buttons.add(btnClose);
        buttons.add(btnTogglePaid);
        buttons.add(btnSavePdf);
        buttons.add(btnMail);
        add(buttons, BorderLayout.SOUTH);
    }

    private void savePdf() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("BienLai_HD-" + String.format("%04d", ct.getMaHoaDon())
                + "_" + ct.getThangNam() + ".pdf"));
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF", "pdf"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".pdf"))
                f = new File(f.getAbsolutePath() + ".pdf");
            if (PdfExporter.xuatBienLai(ct, f)) {
                int ok = JOptionPane.showConfirmDialog(this,
                        "Đã lưu PDF tại:\n" + f.getAbsolutePath() + "\n\nMở file ngay?",
                        "Thành công", JOptionPane.YES_NO_OPTION);
                if (ok == JOptionPane.YES_OPTION) {
                    try {
                        Desktop.getDesktop().open(f);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất PDF.");
            }
        }
    }

    private void sendMail() {
        String to = ct.getEmailKhach();
        if (to == null || to.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Khách hàng chưa có email. Vui lòng cập nhật email cho khách thuê.",
                    "Thiếu email", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!AppConfig.emailConfigured()) {
            JOptionPane.showMessageDialog(this,
                    "<html>Chưa cấu hình email gửi.<br><br>"
                    + "Vui lòng mở file <b>email.properties</b> trong thư mục chương trình<br>"
                    + "và điền:<br>"
                    + "&nbsp;• email.from = Gmail của bạn<br>"
                    + "&nbsp;• email.password = App Password 16 ký tự (xem hướng dẫn Google)<br><br>"
                    + "Sau đó khởi động lại chương trình.</html>",
                    "Chưa cấu hình", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Hỏi xác nhận
        int ok = JOptionPane.showConfirmDialog(this,
                "Gửi biên lai đến " + to + " (kèm file PDF)?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        // Tạo PDF tạm
        File tempPdf;
        try {
            tempPdf = File.createTempFile("bienlai-", ".pdf");
            tempPdf.deleteOnExit();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tạo file tạm: " + ex.getMessage());
            return;
        }
        PdfExporter.xuatBienLai(ct, tempPdf);

        String subject = "Hóa đơn tiền phòng " + ct.getTenPhong() + " - Kỳ " + ct.getThangNam();
        String body = EmailUtil.buildHtmlBienLai(ct);
        String attachName = "BienLai_" + ct.getTenPhong() + "_" + ct.getThangNam() + ".pdf";

        // Chạy nền
        JDialog loading = new JDialog(this, "Đang gửi...", true);
        loading.setSize(280, 100);
        loading.setLocationRelativeTo(this);
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 20));
        p.setBackground(Color.WHITE);
        JLabel l = new JLabel("Đang gửi email, vui lòng đợi...");
        l.setFont(Theme.FONT_BASE);
        p.add(l);
        loading.add(p);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() {
                return EmailUtil.guiHoaDonKemPdf(to, subject, body, tempPdf, attachName);
            }
            @Override protected void done() {
                loading.dispose();
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(Dialog_BienLai.this,
                                "✅ Đã gửi mail đến " + to);
                    } else {
                        JOptionPane.showMessageDialog(Dialog_BienLai.this,
                                "❌ Gửi mail thất bại. Kiểm tra console và cấu hình email.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(Dialog_BienLai.this, "Lỗi: " + ex.getMessage());
                }
            }
        };
        worker.execute();
        loading.setVisible(true);
    }
}
