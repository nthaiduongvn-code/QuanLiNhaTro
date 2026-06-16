package QuanLiNhaTro;

public class ThongTinKhachThue {
    private int maKhachThue;
    private String hoTen;
    private String soCCCD;
    private String soDienThoai;
    private String email;
    private String gioiTinh;
    private String ngaySinh;
    private String queQuan;
    private String bienSoXe;
    private String ghiChu;
    private boolean dangO;

    public ThongTinKhachThue(int maKhachThue, String hoTen, String soCCCD, String soDienThoai,
                              String email, String gioiTinh, String ngaySinh, String queQuan,
                              String bienSoXe, String ghiChu, boolean dangO) {
        this.maKhachThue = maKhachThue;
        this.hoTen = hoTen;
        this.soCCCD = soCCCD;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.gioiTinh = gioiTinh;
        this.ngaySinh = ngaySinh;
        this.queQuan = queQuan;
        this.bienSoXe = bienSoXe;
        this.ghiChu = ghiChu;
        this.dangO = dangO;
    }

    public ThongTinKhachThue(String hoTen, String soCCCD, String soDienThoai,
                              String email, String gioiTinh, String ngaySinh, String queQuan,
                              String bienSoXe, String ghiChu) {
        this.hoTen = hoTen;
        this.soCCCD = soCCCD;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.gioiTinh = gioiTinh;
        this.ngaySinh = ngaySinh;
        this.queQuan = queQuan;
        this.bienSoXe = bienSoXe;
        this.ghiChu = ghiChu;
        this.dangO = false;
    }

    public int getMaKhachThue() { return maKhachThue; }
    public String getHoTen() { return hoTen; }
    public String getSoCCCD() { return soCCCD; }
    public String getSoDienThoai() { return soDienThoai; }
    public String getEmail() { return email; }
    public String getGioiTinh() { return gioiTinh; }
    public String getNgaySinh() { return ngaySinh; }
    public String getQueQuan() { return queQuan; }
    public String getBienSoXe() { return bienSoXe; }
    public String getGhiChu() { return ghiChu; }
    public boolean isDangO() { return dangO; }

    public Object[] getThongTinKhachThue() {
        String trangThai = dangO
                ? "<html><font color='#0D9488'><b>Đang thuê</b></font></html>"
                : "<html><font color='#F59E0B'><b>Chờ phòng</b></font></html>";
        return new Object[]{maKhachThue, hoTen, trangThai, soCCCD, soDienThoai, email, gioiTinh, ngaySinh, queQuan, bienSoXe, ghiChu};
    }

    public Object[] getDiDoiRow() {
        String trangThai = "<html><font color='#64748b'><b>Đã di dời</b></font></html>";
        return new Object[]{maKhachThue, hoTen, trangThai, soCCCD, soDienThoai, email, gioiTinh, ngaySinh, queQuan, bienSoXe, ghiChu};
    }
}
