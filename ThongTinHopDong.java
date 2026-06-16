package QuanLiNhaTro;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class ThongTinHopDong {
    private int maHopDong;
    private String maPhong;
    private String tenPhong;
    private int maKhachThue;
    private String hoTenKhach;
    private int giaThueThucTe;
    private int tienCoc;
    private String ngayBatDau;
    private String ngayKetThuc;
    private String hinhAnh;
    private String trangThaiGoc;
    private String ghiChu;

    // Hiển thị (HTML đã format)
    private String danhSachDichVuHtml;
    private String trangThaiHtml;
    // Trạng thái hiển thị thực tế (tính từ ngày, không phụ thuộc giá trị DB)
    private String trangThaiHienThi;

    // Dùng khi mở form sửa
    private ArrayList<Integer> danhSachMaDichVu = new ArrayList<>();

    // Khi join với KhachThue
    private String soDienThoai;
    private String soCCCD;

    public ThongTinHopDong(int maHopDong, String maPhong, String tenPhong,
                            int maKhachThue, String hoTenKhach,
                            int giaThueThucTe, int tienCoc,
                            String ngayBatDau, String ngayKetThuc,
                            String hinhAnh, String danhSachDichVuRaw,
                            String trangThaiGoc, String ghiChu) {
        this.maHopDong = maHopDong;
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
        this.maKhachThue = maKhachThue;
        this.hoTenKhach = hoTenKhach;
        this.giaThueThucTe = giaThueThucTe;
        this.tienCoc = tienCoc;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.hinhAnh = hinhAnh;
        this.trangThaiGoc = trangThaiGoc;
        this.ghiChu = ghiChu;
        this.danhSachDichVuHtml = formatDichVu(danhSachDichVuRaw);
        this.trangThaiHienThi  = tinhTrangThaiHienThi(trangThaiGoc, ngayBatDau, ngayKetThuc);
        this.trangThaiHtml     = formatTrangThai(trangThaiHienThi);
    }

    /** Quy ước: "3000-..." = vô thời hạn (xem HopDongDAO). */
    public boolean isVoThoiHan() {
        return ngayKetThuc != null && ngayKetThuc.startsWith("3000");
    }

    /** Hiển thị thân thiện: thay 3000-01-01 → "Vô thời hạn" */
    public String getNgayKetThucDisplay() {
        if (ngayKetThuc == null || ngayKetThuc.isEmpty()) return "Không xác định";
        if (isVoThoiHan()) return "Vô thời hạn";
        return ngayKetThuc;
    }

    private static String formatDichVu(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "<html><font color='#94A3B8'><i>Không có</i></font></html>";
        }
        StringBuilder sb = new StringBuilder("<html>");
        for (String s : raw.split("\\|")) {
            sb.append("• ").append(s.trim()).append("<br>");
        }
        return sb.append("</html>").toString();
    }

    /**
     * Tính trạng thái hiển thị từ giá trị DB + ngày thực tế:
     *  - Hết hiệu lực : đã hết hạn hoặc đã thanh lý
     *  - Sắp hết hạn  : tổng thời hạn HĐ (start→end) ≤ 5 ngày, HOẶC còn ≤ 10 ngày đến hết hạn
     *  - Còn hiệu lực : bình thường / vô thời hạn
     */
    public static String tinhTrangThaiHienThi(String trangThaiGoc, String ngayBatDau, String ngayKetThuc) {
        if (trangThaiGoc != null && (trangThaiGoc.contains("Hết hiệu lực")
                || trangThaiGoc.contains("Đã thanh lý")
                || trangThaiGoc.contains("Chưa thanh lý"))) {
            return "Hết hiệu lực";
        }
        if (ngayKetThuc != null && !ngayKetThuc.startsWith("3000")) {
            try {
                LocalDate end   = LocalDate.parse(ngayKetThuc);
                LocalDate today = LocalDate.now();
                if (!end.isAfter(today)) return "Hết hiệu lực";
                // Hợp đồng ngắn hạn: tổng thời hạn ≤ 5 ngày
                if (ngayBatDau != null) {
                    try {
                        LocalDate start = LocalDate.parse(ngayBatDau);
                        long tongNgay = ChronoUnit.DAYS.between(start, end);
                        if (tongNgay >= 0 && tongNgay <= 5) return "Sắp hết hạn";
                    } catch (Exception ignore2) {}
                }
                // Sắp hết hạn theo ngày còn lại (≤ 10 ngày)
                if (end.isBefore(today.plusDays(11))) return "Sắp hết hạn";
            } catch (Exception ignore) {}
        }
        return "Còn hiệu lực";
    }

    private static String formatTrangThai(String trangThai) {
        if (trangThai == null) return "";
        switch (trangThai) {
            case "Còn hiệu lực":
                return "<html><font color='#047857'><b>● Còn hiệu lực</b></font></html>";
            case "Sắp hết hạn":
                return "<html><font color='#B45309'><b>⚠ Sắp hết hạn</b></font></html>";
            case "Hết hiệu lực":
                return "<html><font color='#94A3B8'>○ Hết hiệu lực</font></html>";
            default:
                return trangThai;
        }
    }

    public String getTrangThaiHienThi() { return trangThaiHienThi; }

    public Object[] getThongTinHopDong() {
        return new Object[]{
                maHopDong,
                tenPhong,
                hoTenKhach,
                ngayBatDau,
                getNgayKetThucDisplay(),
                String.format("%,d đ", tienCoc),
                danhSachDichVuHtml,
                hinhAnh,
                trangThaiHtml,
                ghiChu
        };
    }

    public int getMaHopDong()          { return maHopDong; }
    public String getMaPhong()         { return maPhong; }
    public String getTenPhong()        { return tenPhong; }
    public int getMaKhachThue()        { return maKhachThue; }
    public String getHoTenKhach()      { return hoTenKhach; }
    public int getGiaThueThucTe()      { return giaThueThucTe; }
    public int getTienCoc()            { return tienCoc; }
    public String getNgayBatDau()      { return ngayBatDau; }
    public String getNgayKetThuc()     { return ngayKetThuc; }
    public String getHinhAnh()         { return hinhAnh; }
    public String getTrangThaiGoc()    { return trangThaiGoc; }
    public String getGhiChu()          { return ghiChu; }
    public ArrayList<Integer> getDanhSachMaDichVu() { return danhSachMaDichVu; }
    public void setDanhSachMaDichVu(ArrayList<Integer> ds) { this.danhSachMaDichVu = ds; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getSoCCCD() { return soCCCD; }
    public void setSoCCCD(String soCCCD) { this.soCCCD = soCCCD; }
}
