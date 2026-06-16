package QuanLiNhaTro;

public class ThongTinHoaDon {

    private final int    maHoaDon;
    private final String maPhong;
    private final String tenPhong;
    private final long   tienPhong;
    private final long   tienDien;
    private final long   tienNuoc;
    private final long   tongTienDichVu;
    private final long   tongCong;
    private final String trangThai;

    public ThongTinHoaDon(int maHoaDon, String maPhong, String tenPhong,
                          long tienPhong, long tienDien, long tienNuoc,
                          long tongTienDichVu, long tongCong, String trangThai) {
        this.maHoaDon       = maHoaDon;
        this.maPhong        = maPhong;
        this.tenPhong       = tenPhong;
        this.tienPhong      = tienPhong;
        this.tienDien       = tienDien;
        this.tienNuoc       = tienNuoc;
        this.tongTienDichVu = tongTienDichVu;
        this.tongCong       = tongCong;
        this.trangThai      = trangThai;
    }

    /** Trả về Object[] để đổ vào DefaultTableModel. */
    public Object[] toTableRow() {
        return new Object[]{
            maHoaDon,                          // 0 — ẩn
            tenPhong,                          // 1
            vnd(tienPhong),                    // 2
            vnd(tienDien),                     // 3
            vnd(tienNuoc),                     // 4
            vnd(tongTienDichVu),               // 5
            vnd(tongCong),                     // 6
            htmlTrangThai(trangThai),          // 7
            "Hành động"                        // 8 (renderer + editor)
        };
    }

    private static String vnd(long v) { return String.format("%,d đ", v); }

    private static String htmlTrangThai(String tt) {
        if (tt != null && (tt.contains("Đã") || tt.toLowerCase().contains("đã thanh toán")))
            return Theme.pillSuccess("Đã thanh toán");
        return Theme.pillDanger("Chưa thanh toán");
    }

    public boolean isPaid() { return trangThai != null && trangThai.contains("Đã"); }

    public int    getMaHoaDon()        { return maHoaDon; }
    public String getMaPhong()         { return maPhong; }
    public String getTenPhong()        { return tenPhong; }
    public long   getTienPhong()       { return tienPhong; }
    public long   getTienDien()        { return tienDien; }
    public long   getTienNuoc()        { return tienNuoc; }
    public long   getTongTienDichVu()  { return tongTienDichVu; }
    public long   getTongCong()        { return tongCong; }
    public String getTrangThai()       { return trangThai; }
}
