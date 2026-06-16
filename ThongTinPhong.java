package QuanLiNhaTro;

public class ThongTinPhong {
    private String maPhong;
    private String tenPhong;
    private String dienTich;
    private String giaThue;
    private String trangThaiGoc;   // giá trị thô từ DB (ví dụ "Trống" / "Đang thuê")

    public ThongTinPhong(String maPhong, String tenPhong, String dienTich, String giaThue, String trangThai) {
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
        this.dienTich = dienTich;
        this.giaThue = giaThue;
        this.trangThaiGoc = trangThai;
    }

    /**
     * FIX: trước đây check null = trống, nhưng DB lưu "Trống" (không null)
     * → tất cả phòng đều bị hiểu là "đang ở". Đổi sang so chuỗi.
     */
    public boolean isDangO() {
        if (trangThaiGoc == null) return false;
        String s = trangThaiGoc.trim();
        return !s.isEmpty() && !s.equalsIgnoreCase("Trống");
    }

    public String getTenPhong()        { return tenPhong; }
    public String getDienTich()        { return dienTich; }
    public String getGiaThue()         { return giaThue; }
    public String getMaPhong()         { return maPhong; }
    public String getTrangThaiGoc()    { return trangThaiGoc; }
}
