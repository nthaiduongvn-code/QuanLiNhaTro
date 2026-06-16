package QuanLiNhaTro;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Mini PDF generator pure Java — không cần thư viện ngoài.
 * Đủ dùng cho biên lai 1 trang A4 với text + bảng.
 *
 * Hỗ trợ font Helvetica (latin), với tiếng Việt sẽ thay dấu bằng dạng không dấu
 * (vì built-in PDF font không có Unicode). Để hiển thị tiếng Việt có dấu đẹp
 * cần embed font TTF (phức tạp hơn nhiều) — nếu cần thiết tôi sẽ làm bản nâng cao,
 * hiện tại fallback bỏ dấu cho file PDF, bản HTML/email vẫn giữ đầy đủ tiếng Việt.
 */
public class PdfExporter {

    private static final int PAGE_W = 595;  // A4 width  in points
    private static final int PAGE_H = 842;  // A4 height in points

    /** Sinh file PDF biên lai vào đường dẫn truyền vào. */
    public static boolean xuatBienLai(ChiTietHoaDon ct, File output) {
        try (FileOutputStream fos = new FileOutputStream(output)) {
            byte[] bytes = buildPdfBytes(ct);
            fos.write(bytes);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static byte[] buildPdfBytes(ChiTietHoaDon ct) {
        // Tạo nội dung content stream
        StringBuilder cs = new StringBuilder();
        // Header: gradient bằng rectangle xanh teal
        cs.append("q\n");
        cs.append("0.051 0.580 0.529 rg\n"); // teal-600 #0D9488
        cs.append(String.format("0 %d %d %d re f\n", PAGE_H - 130, PAGE_W, 130));

        // Tiêu đề
        cs.append("1 1 1 rg\n");
        cs.append("BT /F2 11 Tf 40 ").append(PAGE_H - 50).append(" Td (HOA DON TIEN PHONG) Tj ET\n");
        cs.append("BT /F2 22 Tf 40 ").append(PAGE_H - 80).append(" Td (").append(esc(noDau(ct.getTenPhong()))).append(") Tj ET\n");
        cs.append("BT /F1 11 Tf 40 ").append(PAGE_H - 110).append(" Td (Ma HD: HD-").append(String.format("%04d", ct.getMaHoaDon()))
          .append("    Ky: ").append(esc(noDau(ct.getThangNam()))).append(") Tj ET\n");
        cs.append("Q\n");

        // Thông tin khách
        int yPos = PAGE_H - 160;
        cs.append("0.290 0.333 0.388 rg\n");
        cs.append("BT /F2 12 Tf 40 ").append(yPos).append(" Td (Khach hang: ) Tj ET\n");
        cs.append("0.059 0.090 0.165 rg\n");
        cs.append("BT /F1 12 Tf 130 ").append(yPos).append(" Td (").append(esc(noDau(ct.getHoTenKhach()))).append(") Tj ET\n");

        if (ct.getEmailKhach() != null && !ct.getEmailKhach().isEmpty()) {
            yPos -= 18;
            cs.append("0.290 0.333 0.388 rg\n");
            cs.append("BT /F2 12 Tf 40 ").append(yPos).append(" Td (Email: ) Tj ET\n");
            cs.append("0.059 0.090 0.165 rg\n");
            cs.append("BT /F1 12 Tf 130 ").append(yPos).append(" Td (").append(esc(ct.getEmailKhach())).append(") Tj ET\n");
        }

        // Bảng chi tiết
        yPos -= 35;
        int tableLeft = 40;
        int tableRight = PAGE_W - 40;
        int colDetail = tableLeft + 180;
        int colAmount = tableRight - 110;

        // Header bảng
        cs.append("0.945 0.961 0.976 rg\n"); // slate-100
        cs.append(String.format("%d %d %d %d re f\n", tableLeft, yPos - 5, tableRight - tableLeft, 24));
        cs.append("0.278 0.333 0.412 rg\n");
        cs.append("BT /F2 10 Tf ").append(tableLeft + 8).append(" ").append(yPos + 8).append(" Td (KHOAN MUC) Tj ET\n");
        cs.append("BT /F2 10 Tf ").append(colDetail + 8).append(" ").append(yPos + 8).append(" Td (CHI TIET) Tj ET\n");
        cs.append("BT /F2 10 Tf ").append(colAmount + 8).append(" ").append(yPos + 8).append(" Td (THANH TIEN) Tj ET\n");

        yPos -= 30;

        // Các hàng
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"Tien phong", ct.getSoNgayO() + "/" + ct.getSoNgayTrongThang() + " ngay", vnd(ct.getTienPhong())});
        rows.add(new Object[]{"Tien dien", buildChiSoDetail(ct.getDienCu(), ct.getDienMoi(), ct.getDonGiaDien(), "kWh"), vnd(ct.getTienDien())});
        rows.add(new Object[]{"Tien nuoc", buildChiSoDetail(ct.getNuocCu(), ct.getNuocMoi(), ct.getDonGiaNuoc(), "m3"), vnd(ct.getTienNuoc())});

        for (Object[] dv : ct.getDichVuKhac()) {
            String ten       = noDau((String) dv[0]);
            String cachTinh  = (String) dv[1];
            long   donGia    = (long) dv[2];
            double soLuong   = (double) dv[3];
            String donVi     = noDau((String) dv[4]);
            long   thanhTien = (long) dv[5];
            String detail;
            if (ThongTinDichVu.THEO_NGUOI.equals(cachTinh)) {
                detail = vnd(donGia) + (donVi.isEmpty() ? "" : "/" + donVi) + " x " + soLuongStr(soLuong) + " nguoi";
            } else {
                detail = donVi.isEmpty() ? "-" : "/ " + donVi;
            }
            rows.add(new Object[]{ten, detail, vnd(thanhTien)});
        }

        cs.append("0.059 0.090 0.165 rg\n");
        for (Object[] r : rows) {
            cs.append("BT /F1 11 Tf ").append(tableLeft + 8).append(" ").append(yPos + 5).append(" Td (")
              .append(esc(noDau((String) r[0]))).append(") Tj ET\n");
            cs.append("0.392 0.455 0.545 rg\n");
            cs.append("BT /F1 10 Tf ").append(colDetail + 8).append(" ").append(yPos + 5).append(" Td (")
              .append(esc(noDau((String) r[1]))).append(") Tj ET\n");
            cs.append("0.059 0.090 0.165 rg\n");
            // right-aligned amount: position approximate (font width ~6 per char at 11pt)
            String amt = (String) r[2];
            int amtX = tableRight - amt.length() * 6 - 8;
            cs.append("BT /F2 11 Tf ").append(amtX).append(" ").append(yPos + 5).append(" Td (")
              .append(esc(amt)).append(") Tj ET\n");
            // line
            cs.append("0.886 0.910 0.941 RG\n0.5 w\n");
            cs.append(String.format("%d %d m %d %d l S\n", tableLeft, yPos, tableRight, yPos));
            yPos -= 25;
        }

        // Tổng cộng
        cs.append("0.059 0.463 0.431 rg\n"); // teal-700
        cs.append(String.format("%d %d %d %d re f\n", tableLeft, yPos - 5, tableRight - tableLeft, 30));
        cs.append("1 1 1 rg\n");
        cs.append("BT /F2 13 Tf ").append(tableLeft + 8).append(" ").append(yPos + 8).append(" Td (TONG CONG) Tj ET\n");
        String tongStr = vnd(ct.getTongCong());
        int tongX = tableRight - tongStr.length() * 7 - 8;
        cs.append("BT /F2 13 Tf ").append(tongX).append(" ").append(yPos + 8).append(" Td (").append(esc(tongStr)).append(") Tj ET\n");

        yPos -= 50;

        // Trạng thái
        boolean daThanhToan = ct.getTrangThai() != null && ct.getTrangThai().contains("Đã");
        if (daThanhToan) {
            cs.append("0.063 0.725 0.506 rg\n"); // emerald
        } else {
            cs.append("0.937 0.267 0.267 rg\n"); // red
        }
        cs.append("BT /F2 12 Tf ").append(tableLeft).append(" ").append(yPos).append(" Td (Trang thai: ")
          .append(esc(noDau(ct.getTrangThai()))).append(") Tj ET\n");

        // Footer
        yPos = 60;
        cs.append("0.392 0.455 0.545 rg\n");
        cs.append("BT /F1 9 Tf ").append(tableLeft).append(" ").append(yPos)
          .append(" Td (Cam on quy khach da su dung dich vu. Vui long thanh toan dung han.) Tj ET\n");

        byte[] contentStream = cs.toString().getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);

        // Build PDF skeleton
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        try {
            writeStr(out, "%PDF-1.4\n%âãÏÓ\n");

            // 1: Catalog
            offsets.add(out.size());
            writeStr(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

            // 2: Pages
            offsets.add(out.size());
            writeStr(out, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

            // 3: Page
            offsets.add(out.size());
            writeStr(out, "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 "
                    + PAGE_W + " " + PAGE_H + "] "
                    + "/Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> "
                    + "/Contents 6 0 R >>\nendobj\n");

            // 4: Font F1 (Helvetica)
            offsets.add(out.size());
            writeStr(out, "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>\nendobj\n");

            // 5: Font F2 (Helvetica-Bold)
            offsets.add(out.size());
            writeStr(out, "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>\nendobj\n");

            // 6: Content stream
            offsets.add(out.size());
            writeStr(out, "6 0 obj\n<< /Length " + contentStream.length + " >>\nstream\n");
            out.write(contentStream);
            writeStr(out, "\nendstream\nendobj\n");

            // xref
            int xrefPos = out.size();
            writeStr(out, "xref\n0 7\n0000000000 65535 f \n");
            for (int off : offsets) {
                writeStr(out, String.format("%010d 00000 n \n", off));
            }

            // trailer
            writeStr(out, "trailer\n<< /Size 7 /Root 1 0 R >>\nstartxref\n" + xrefPos + "\n%%EOF\n");

            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private static void writeStr(ByteArrayOutputStream out, String s) throws IOException {
        out.write(s.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
    }

    /** Escape ký tự đặc biệt trong PDF string. */
    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    /** Bỏ dấu tiếng Việt (vì font built-in PDF không support Unicode). */
    private static String noDau(String s) {
        if (s == null) return "";
        String result = s;
        String[][] map = {
            {"á","a"},{"à","a"},{"ả","a"},{"ã","a"},{"ạ","a"},
            {"ă","a"},{"ắ","a"},{"ằ","a"},{"ẳ","a"},{"ẵ","a"},{"ặ","a"},
            {"â","a"},{"ấ","a"},{"ầ","a"},{"ẩ","a"},{"ẫ","a"},{"ậ","a"},
            {"é","e"},{"è","e"},{"ẻ","e"},{"ẽ","e"},{"ẹ","e"},
            {"ê","e"},{"ế","e"},{"ề","e"},{"ể","e"},{"ễ","e"},{"ệ","e"},
            {"í","i"},{"ì","i"},{"ỉ","i"},{"ĩ","i"},{"ị","i"},
            {"ó","o"},{"ò","o"},{"ỏ","o"},{"õ","o"},{"ọ","o"},
            {"ô","o"},{"ố","o"},{"ồ","o"},{"ổ","o"},{"ỗ","o"},{"ộ","o"},
            {"ơ","o"},{"ớ","o"},{"ờ","o"},{"ở","o"},{"ỡ","o"},{"ợ","o"},
            {"ú","u"},{"ù","u"},{"ủ","u"},{"ũ","u"},{"ụ","u"},
            {"ư","u"},{"ứ","u"},{"ừ","u"},{"ử","u"},{"ữ","u"},{"ự","u"},
            {"ý","y"},{"ỳ","y"},{"ỷ","y"},{"ỹ","y"},{"ỵ","y"},
            {"đ","d"},
            {"Á","A"},{"À","A"},{"Ả","A"},{"Ã","A"},{"Ạ","A"},
            {"Ă","A"},{"Ắ","A"},{"Ằ","A"},{"Ẳ","A"},{"Ẵ","A"},{"Ặ","A"},
            {"Â","A"},{"Ấ","A"},{"Ầ","A"},{"Ẩ","A"},{"Ẫ","A"},{"Ậ","A"},
            {"É","E"},{"È","E"},{"Ẻ","E"},{"Ẽ","E"},{"Ẹ","E"},
            {"Ê","E"},{"Ế","E"},{"Ề","E"},{"Ể","E"},{"Ễ","E"},{"Ệ","E"},
            {"Í","I"},{"Ì","I"},{"Ỉ","I"},{"Ĩ","I"},{"Ị","I"},
            {"Ó","O"},{"Ò","O"},{"Ỏ","O"},{"Õ","O"},{"Ọ","O"},
            {"Ô","O"},{"Ố","O"},{"Ồ","O"},{"Ổ","O"},{"Ỗ","O"},{"Ộ","O"},
            {"Ơ","O"},{"Ớ","O"},{"Ờ","O"},{"Ở","O"},{"Ỡ","O"},{"Ợ","O"},
            {"Ú","U"},{"Ù","U"},{"Ủ","U"},{"Ũ","U"},{"Ụ","U"},
            {"Ư","U"},{"Ứ","U"},{"Ừ","U"},{"Ử","U"},{"Ữ","U"},{"Ự","U"},
            {"Ý","Y"},{"Ỳ","Y"},{"Ỷ","Y"},{"Ỹ","Y"},{"Ỵ","Y"},
            {"Đ","D"},
            {"đ ","d "}, // tiền VND ký hiệu
        };
        for (String[] pair : map) result = result.replace(pair[0], pair[1]);
        // Loại bỏ ký tự lạ còn lại (ngoài ascii)
        StringBuilder sb = new StringBuilder();
        for (char c : result.toCharArray()) {
            if (c < 128) sb.append(c);
            else if (c == 'đ') sb.append('d');
            else sb.append('?');
        }
        return sb.toString();
    }

    private static String vnd(long v) { return String.format("%,d d", v); }

    /** Hiển thị số người: bỏ phần .0 nếu là số nguyên, giữ 1 chữ số thập phân nếu lẻ. */
    private static String soLuongStr(double v) {
        return (v == Math.floor(v)) ? String.valueOf((long) v) : String.format("%.1f", v);
    }

    private static String buildChiSoDetail(Integer cu, Integer moi, long donGia, String donVi) {
        if (cu == null || moi == null) return "—";
        int luong = moi - cu;
        String base = cu + " -> " + moi + " = " + luong + " " + donVi;
        return donGia > 0 ? base + " x " + vnd(donGia) : base;
    }
}
