package QuanLiNhaTro;

public class ThongTinDichVu {
    public static final String THEO_PHONG  = "Phong";
    public static final String THEO_NGUOI  = "Nguoi";

    private String maDichVu;
    private String tenDichVu;
    private int giaDichVu;
    private String donVi;
    private String ghiChu;
    private String cachTinh; // "Phong" hoặc "Nguoi"

    public ThongTinDichVu(String maDichVu, String tenDichVu, int giaDichVu, String donVi, String ghiChu, String cachTinh) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.giaDichVu = giaDichVu;
        this.donVi = donVi;
        this.ghiChu = ghiChu;
        this.cachTinh = cachTinh == null ? THEO_PHONG : cachTinh;
    }

    public ThongTinDichVu(String tenDichVu, int giaDichVu, String donVi, String ghiChu, String cachTinh) {
        this.tenDichVu = tenDichVu;
        this.giaDichVu = giaDichVu;
        this.donVi = donVi;
        this.ghiChu = ghiChu;
        this.cachTinh = cachTinh == null ? THEO_PHONG : cachTinh;
    }

    public String getMaDichVu() { return maDichVu; }
    public String getTenDichVu() { return tenDichVu; }
    public int getGiaDichVu() { return giaDichVu; }
    public String getDonVi() { return donVi; }
    public String getGhiChu() { return ghiChu; }
    public String getCachTinh() { return cachTinh; }
    public boolean isTheoNguoi() { return THEO_NGUOI.equals(cachTinh); }

    public static String nhanCachTinh(String cachTinh) {
        return THEO_NGUOI.equals(cachTinh) ? "Theo đầu người" : "Theo phòng";
    }

    public Object[] getThongTinDichVu() {
        return new Object[]{maDichVu, tenDichVu, giaDichVu, donVi, ghiChu, cachTinh};
    }
}
