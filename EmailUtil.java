package QuanLiNhaTro;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.List;
import java.util.Properties;

public class EmailUtil {

    /** Gửi một email HTML (không đính kèm). Dùng cho gửi từng phòng. */
    public static boolean guiHoaDonHtml(String toEmail, String subject, String bodyHtml) {
        return guiHoaDonKemPdf(toEmail, subject, bodyHtml, null, null);
    }

    /** Gửi một email HTML kèm PDF. Dùng cho gửi từng phòng. */
    public static boolean guiHoaDonKemPdf(String toEmail, String subject, String bodyHtml,
                                           File pdfFile, String attachmentName) {
        if (!AppConfig.emailConfigured()) {
            System.err.println("Email chưa được cấu hình trong email.properties");
            return false;
        }
        try {
            Session session = createSession();
            MimeMessage msg = buildMessage(session, toEmail, subject, bodyHtml, pdfFile, attachmentName);
            Transport.send(msg);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gửi hàng loạt email — dùng một Transport duy nhất cho toàn bộ danh sách.
     * Mỗi phần tử trong emails là String[]{toEmail, subject, bodyHtml}.
     * Trả về int[]{sốThànhCông, sốThấtBại}.
     */
    public static int[] guiBulkHtml(List<String[]> emails) {
        int sent = 0, failed = 0;
        if (!AppConfig.emailConfigured() || emails.isEmpty()) {
            return new int[]{0, emails.size()};
        }
        Session session = createSession();
        Transport transport = null;
        try {
            transport = session .getTransport("smtp");
            transport.connect();
            for (String[] entry : emails) {
                try {
                    MimeMessage msg = buildMessage(session, entry[0], entry[1], entry[2], null, null);
                    transport.sendMessage(msg, msg.getAllRecipients());
                    sent++;
                } catch (Exception e) {
                    e.printStackTrace();
                    failed++;
                }
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
            failed += emails.size() - sent;
        } finally {
            if (transport != null) {
                try { transport.close(); } catch (Exception ignored) {}
            }
        }
        return new int[]{sent, failed};
    }

    // ── Helpers nội bộ ────────────────────────────────────────────────────────

    private static Session createSession() {
        String host = AppConfig.smtpHost();
        int    port = AppConfig.smtpPort();
        String from = AppConfig.emailFrom();
        String pass = AppConfig.emailPassword();

        Properties props = new Properties();
        props.put("mail.smtp.auth",              "true");
        props.put("mail.smtp.host",              host);
        props.put("mail.smtp.port",              String.valueOf(port));
        props.put("mail.smtp.ssl.trust",         "*");
        props.put("mail.smtp.connectiontimeout", "15000");
        props.put("mail.smtp.timeout",           "15000");
        props.put("mail.smtp.writetimeout",      "15000");

        if (port == 465) {
            // SMTPS — SSL kết nối ngay từ đầu
            props.put("mail.smtp.ssl.enable", "true");
        } else {
            // STARTTLS — kết nối plain rồi nâng lên TLS (port 587 hoặc 25)
            props.put("mail.smtp.starttls.enable",   "true");
            props.put("mail.smtp.starttls.required", "true");
        }

        return Session.getInstance(props, new Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, pass);
            }
        });
    }

    private static MimeMessage buildMessage(Session session, String toEmail,
                                             String subject, String bodyHtml,
                                             File pdfFile, String attachmentName) throws Exception {
        String from = AppConfig.emailFrom();
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from, "Quản lý nhà trọ", "UTF-8"));
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
        msg.setSubject(subject, "UTF-8");

        if (pdfFile == null) {
            msg.setContent(bodyHtml, "text/html; charset=UTF-8");
        } else {
            MimeMultipart mp = new MimeMultipart();
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(bodyHtml, "text/html; charset=UTF-8");
            mp.addBodyPart(htmlPart);

            MimeBodyPart attachPart = new MimeBodyPart();
            DataSource src = new FileDataSource(pdfFile);
            attachPart.setDataHandler(new DataHandler(src));
            String name = (attachmentName != null && !attachmentName.isEmpty())
                          ? attachmentName : pdfFile.getName();
            attachPart.setFileName(MimeUtility.encodeText(name, "UTF-8", null));
            mp.addBodyPart(attachPart);
            msg.setContent(mp);
        }
        return msg;
    }

    // ── Build HTML biên lai từ ChiTietHoaDon ─────────────────────────────────

    public static String buildHtmlBienLai(ChiTietHoaDon ct) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><body style='margin:0;padding:0;background:#F8FAFC;'>");
        sb.append("<div style='max-width:640px;margin:24px auto;font-family:\"Segoe UI\",Arial,sans-serif;")
          .append("background:#fff;border-radius:12px;overflow:hidden;")
          .append("box-shadow:0 4px 16px rgba(0,0,0,0.08);border:1px solid #E2E8F0;'>");

        sb.append("<div style='background:linear-gradient(135deg,#0D9488 0%,#14B8A6 100%);")
          .append("color:#fff;padding:28px 28px;'>")
          .append("<div style='font-size:12px;letter-spacing:2px;opacity:.85;'>HÓA ĐƠN TIỀN PHÒNG</div>")
          .append("<h2 style='margin:6px 0 0;font-size:26px;font-weight:700;'>")
          .append(safe(ct.getTenPhong())).append("</h2>")
          .append("<div style='margin-top:8px;font-size:13px;opacity:.9;'>")
          .append("Mã hóa đơn: <b>HD-").append(String.format("%04d", ct.getMaHoaDon())).append("</b>")
          .append(" &nbsp;•&nbsp; Kỳ: <b>").append(safe(ct.getThangNam())).append("</b></div>")
          .append("</div>");

        sb.append("<div style='padding:22px 28px 8px;'>")
          .append("<table style='width:100%;font-size:14px;color:#334155;'>")
          .append("<tr><td style='padding:4px 0;width:120px;color:#64748B;'>Khách hàng</td>")
          .append("<td style='padding:4px 0;font-weight:600;'>").append(safe(ct.getHoTenKhach())).append("</td></tr>");
        if (ct.getEmailKhach() != null && !ct.getEmailKhach().isEmpty()) {
            sb.append("<tr><td style='padding:4px 0;color:#64748B;'>Email</td>")
              .append("<td style='padding:4px 0;'>").append(safe(ct.getEmailKhach())).append("</td></tr>");
        }
        sb.append("</table></div>");

        sb.append("<div style='padding:8px 28px 24px;'>")
          .append("<table style='width:100%;border-collapse:collapse;font-size:14px;'>")
          .append("<thead><tr>")
          .append(th("Khoản mục", "left"))
          .append(th("Chi tiết", "center"))
          .append(th("Thành tiền", "right"))
          .append("</tr></thead><tbody>");

        sb.append(row("Tiền phòng", ct.getSoNgayO() + "/" + ct.getSoNgayTrongThang() + " ngày", vnd(ct.getTienPhong())));
        sb.append(row("Tiền điện", buildChiSoDetail(ct.getDienCu(), ct.getDienMoi(), ct.getDonGiaDien(), "kWh"), vnd(ct.getTienDien())));
        sb.append(row("Tiền nước", buildChiSoDetail(ct.getNuocCu(), ct.getNuocMoi(), ct.getDonGiaNuoc(), "m³"), vnd(ct.getTienNuoc())));

        for (Object[] dv : ct.getDichVuKhac()) {
            String ten      = (String) dv[0];
            String cachTinh = (String) dv[1];
            long   donGia   = (long)   dv[2];
            double soLuong  = (double) dv[3];
            String donVi    = (String) dv[4];
            long   thanhTien = (long)  dv[5];
            String detail;
            if (ThongTinDichVu.THEO_NGUOI.equals(cachTinh)) {
                detail = vnd(donGia) + (donVi == null || donVi.isEmpty() ? "" : "/" + donVi)
                       + " × " + soLuongStr(soLuong) + " người";
            } else {
                detail = donVi == null || donVi.isEmpty() ? "—" : "/ " + donVi;
            }
            sb.append(row(ten, detail, vnd(thanhTien)));
        }

        sb.append("<tr style='background:#0F766E;color:#fff;font-weight:700;font-size:15px;'>")
          .append("<td colspan='2' style='padding:14px 12px;'>TỔNG CỘNG</td>")
          .append("<td style='padding:14px 12px;text-align:right;'>")
          .append(vnd(ct.getTongCong())).append("</td></tr>");
        sb.append("</tbody></table></div>");

        boolean daThanhToan = ct.getTrangThai() != null && ct.getTrangThai().contains("Đã");
        String ttColor = daThanhToan ? "#10B981" : "#EF4444";
        String ttBg    = daThanhToan ? "#D1FAE5" : "#FEE2E2";
        sb.append("<div style='padding:0 28px 24px;text-align:right;'>")
          .append("<span style='display:inline-block;padding:6px 14px;border-radius:14px;")
          .append("background:").append(ttBg).append(";color:").append(ttColor).append(";")
          .append("font-weight:700;font-size:13px;'>")
          .append(safe(ct.getTrangThai())).append("</span></div>");

        sb.append("<div style='padding:16px 28px;background:#F1F5F9;color:#64748B;")
          .append("font-size:12px;text-align:center;border-top:1px solid #E2E8F0;'>")
          .append("Cảm ơn quý khách đã sử dụng dịch vụ. Vui lòng thanh toán đúng hạn.</div>");

        sb.append("</div></body></html>");
        return sb.toString();
    }

    private static String th(String text, String align) {
        return "<th style='padding:12px 10px;border-bottom:2px solid #E2E8F0;"
                + "color:#475569;font-size:12px;letter-spacing:.5px;text-transform:uppercase;"
                + "text-align:" + align + ";'>" + text + "</th>";
    }

    private static String row(String col1, String col2, String col3) {
        String td = "padding:11px 10px;border-bottom:1px solid #F1F5F9;color:#334155;";
        return "<tr>"
                + "<td style='" + td + "font-weight:500;'>" + col1 + "</td>"
                + "<td style='" + td + "text-align:center;color:#64748B;font-size:13px;'>" + col2 + "</td>"
                + "<td style='" + td + "text-align:right;font-weight:600;'>" + col3 + "</td>"
                + "</tr>";
    }

    private static String buildChiSoDetail(Integer cu, Integer moi, long donGia, String donVi) {
        if (cu == null || moi == null) return "—";
        int luong = moi - cu;
        String base = cu + " → " + moi + " = " + luong + " " + donVi;
        return donGia > 0 ? base + " × " + vnd(donGia) : base;
    }

    private static String vnd(long v) { return String.format("%,d đ", v); }
    private static String safe(String s) { return s != null ? s : ""; }

    /** Hiển thị số người: bỏ phần .0 nếu là số nguyên, giữ 1 chữ số thập phân nếu lẻ. */
    private static String soLuongStr(double v) {
        return (v == Math.floor(v)) ? String.valueOf((long) v) : String.format("%.1f", v);
    }
}
