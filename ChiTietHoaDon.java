package QuanLiNhaTro;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ChiTietHoaDon {

    private final int    maHoaDon;
    private final String maPhong;
    private final String tenPhong;
    private final String thangNam;

    private final String hoTenKhach;
    private final String emailKhach;

    private final long tienPhong;

    private final Integer dienCu;
    private final Integer dienMoi;
    private final long    donGiaDien;
    private final long    tienDien;

    private final Integer nuocCu;
    private final Integer nuocMoi;
    private final long    donGiaNuoc;
    private final long    tienNuoc;

    /** Mỗi dòng: [TenDichVu, CachTinh, DonGia, SoLuong, DonVi, ThanhTien]. */
    private final List<Object[]> dichVuKhac;
    private final long           tongTienDichVu;
    private final int            soNguoi;

    /** Số ngày ở trong tháng / tổng số ngày của tháng — dùng cho dòng "Tiền phòng". */
    private final int soNgayO;
    private final int soNgayTrongThang;

    /** Ngày bắt đầu hợp đồng chính (để phát hiện vào giữa tháng). */
    private final LocalDate ngayBatDauHD;

    /**
     * Lịch sử người ở ghép có hoạt động trong tháng hóa đơn.
     * Mỗi phần tử: Object[]{LocalDate ngayVao_raw (null = từ trước tháng),
     *                        LocalDate ngayDi_raw  (null = còn đang ở)}.
     */
    private final List<Object[]> nguoiOGhepTrongThang;

    private final long   tongCong;
    private final String trangThai;

    public ChiTietHoaDon(int maHoaDon, String maPhong, String tenPhong, String thangNam,
                         String hoTenKhach, String emailKhach,
                         long tienPhong,
                         Integer dienCu, Integer dienMoi, long donGiaDien, long tienDien,
                         Integer nuocCu, Integer nuocMoi, long donGiaNuoc, long tienNuoc,
                         List<Object[]> dichVuKhac, long tongTienDichVu,
                         int soNguoi,
                         int soNgayO, int soNgayTrongThang,
                         LocalDate ngayBatDauHD,
                         List<Object[]> nguoiOGhepTrongThang,
                         long tongCong, String trangThai) {
        this.maHoaDon       = maHoaDon;
        this.maPhong        = maPhong;
        this.tenPhong       = tenPhong;
        this.thangNam       = thangNam;
        this.hoTenKhach     = hoTenKhach;
        this.emailKhach     = emailKhach;
        this.tienPhong      = tienPhong;
        this.dienCu         = dienCu;
        this.dienMoi        = dienMoi;
        this.donGiaDien     = donGiaDien;
        this.tienDien       = tienDien;
        this.nuocCu         = nuocCu;
        this.nuocMoi        = nuocMoi;
        this.donGiaNuoc     = donGiaNuoc;
        this.tienNuoc       = tienNuoc;
        this.dichVuKhac     = dichVuKhac;
        this.tongTienDichVu = tongTienDichVu;
        this.soNguoi        = soNguoi < 1 ? 1 : soNguoi;
        this.soNgayO          = soNgayO;
        this.soNgayTrongThang = soNgayTrongThang;
        this.ngayBatDauHD   = ngayBatDauHD;
        this.nguoiOGhepTrongThang = (nguoiOGhepTrongThang != null)
                                    ? nguoiOGhepTrongThang : new ArrayList<>();
        this.tongCong       = tongCong;
        this.trangThai      = trangThai;
    }

    public int             getMaHoaDon()               { return maHoaDon; }
    public String          getMaPhong()                { return maPhong; }
    public String          getTenPhong()               { return tenPhong; }
    public String          getThangNam()               { return thangNam; }
    public String          getHoTenKhach()             { return hoTenKhach; }
    public String          getEmailKhach()             { return emailKhach; }
    public long            getTienPhong()              { return tienPhong; }
    public Integer         getDienCu()                 { return dienCu; }
    public Integer         getDienMoi()                { return dienMoi; }
    public long            getDonGiaDien()             { return donGiaDien; }
    public long            getTienDien()               { return tienDien; }
    public Integer         getNuocCu()                 { return nuocCu; }
    public Integer         getNuocMoi()                { return nuocMoi; }
    public long            getDonGiaNuoc()             { return donGiaNuoc; }
    public long            getTienNuoc()               { return tienNuoc; }
    public List<Object[]>  getDichVuKhac()             { return dichVuKhac; }
    public long            getTongTienDichVu()         { return tongTienDichVu; }
    public int             getSoNguoi()                { return soNguoi; }
    public int             getSoNgayO()                { return soNgayO; }
    public int             getSoNgayTrongThang()       { return soNgayTrongThang; }
    public LocalDate       getNgayBatDauHD()           { return ngayBatDauHD; }
    public List<Object[]>  getNguoiOGhepTrongThang()   { return nguoiOGhepTrongThang; }
    public long            getTongCong()               { return tongCong; }
    public String          getTrangThai()              { return trangThai; }
}
