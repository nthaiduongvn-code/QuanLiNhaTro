package QuanLiNhaTro;

public class ThongTinPhong {
    private String maPhong;
    private String tenPhong;
    private String dienTich;
    private String giaThue;
    private String trangThaiGoc;

    public ThongTinPhong(String maPhong, String tenPhong, String dienTich, String giaThue, String trangThai) {
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
        this.dienTich = dienTich;
        this.giaThue = giaThue;
        this.trangThaiGoc = trangThai;
    }

    public boolean isTrangThaiPhong() {
        //đang ở = true , ngược lại faule
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
