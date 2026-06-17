package QuanLiNhaTro;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class TongHopHoaDonDAO {

    // ─────────────────────────────────────────────────────────────────────────
    // 1) Danh sách hóa đơn theo tháng (panel Thanh toán)
    // ─────────────────────────────────────────────────────────────────────────
    private static final String SQL_DS =
        "SELECT " +
        "    hd.MaHoaDon, hop.MaPhong, p.TenPhong, " +
        "    hd.TienPhong, hd.TienDien, hd.TienNuoc, hd.TienDichVuKhac, " +
        "    hd.TongTien, hd.TrangThai " +
        "FROM HoaDon hd " +
        "JOIN HopDong hop ON hd.MaHopDong = hop.MaHopDong " +
        "JOIN Phong p ON hop.MaPhong = p.MaPhong " +
        "WHERE hd.ThangNam = ? " +
        "ORDER BY p.TenPhong";

    public static ArrayList<ThongTinHoaDon> getDanhSachHoaDonThang(String thangNam) {
        ArrayList<ThongTinHoaDon> ds = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DS)) {
            ps.setString(1, thangNam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new ThongTinHoaDon(
                        rs.getInt("MaHoaDon"),
                        rs.getString("MaPhong"),
                        rs.getString("TenPhong"),
                        rs.getLong("TienPhong"),
                        rs.getLong("TienDien"),
                        rs.getLong("TienNuoc"),
                        rs.getLong("TienDichVuKhac"),
                        rs.getLong("TongTien"),
                        rs.getString("TrangThai")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2) Chi tiết một hóa đơn — cho biên lai PDF/HTML
    // ─────────────────────────────────────────────────────────────────────────
    private static final String SQL_CHITIET =
        "SELECT " +
        "    hd.MaHoaDon, hop.MaHopDong, hop.MaPhong, hd.ThangNam, hd.TongTien, hd.TrangThai, " +
        "    hd.TienPhong, hd.TienDien, hd.TienNuoc, hd.TienDichVuKhac, " +
        "    hop.NgayBatDau, " +
        "    p.TenPhong, " +
        "    kt.HoTen, kt.Email, " +
        "    cs.DienCu, cs.DienMoi, cs.NuocCu, cs.NuocMoi, " +
        "    dvD.GiaDichVu AS DonGiaDien, " +
        "    dvN.GiaDichVu AS DonGiaNuoc, " +
        "    1 + ISNULL(og.SoNguoiOGhep, 0) AS SoNguoi " +
        "FROM HoaDon hd " +
        "JOIN HopDong hop ON hd.MaHopDong = hop.MaHopDong " +
        "JOIN Phong p ON hop.MaPhong = p.MaPhong " +
        "LEFT JOIN KhachThue kt ON hop.MaKhachThue = kt.MaKhachThue " +
        "LEFT JOIN ChiSo_DienNuoc cs " +
        "    ON cs.MaPhong = hop.MaPhong AND cs.ThangNam = hd.ThangNam " +
        "LEFT JOIN (SELECT TOP 1 GiaDichVu FROM DichVu WHERE TenDichVu = N'Điện') dvD ON 1=1 " +
        "LEFT JOIN (SELECT TOP 1 GiaDichVu FROM DichVu WHERE TenDichVu = N'Nước') dvN ON 1=1 " +
        "LEFT JOIN (SELECT MaHopDong, COUNT(*) AS SoNguoiOGhep " +
        "           FROM ChiTiet_HopDong_NguoiOGhep WHERE NgayDi IS NULL " +
        "           GROUP BY MaHopDong) og ON og.MaHopDong = hop.MaHopDong " +
        "WHERE hd.MaHoaDon = ?";

    private static final String SQL_DICHVU_KHAC =
        "SELECT TenDichVu, CachTinh, DonGia, SoLuong, ISNULL(DonViTinh,'') AS DonVi, ThanhTien " +
        "FROM ChiTiet_HoaDon_DichVu " +
        "WHERE MaHoaDon = ? " +
        "ORDER BY MaChiTiet";

    public static ChiTietHoaDon layChiTietHoaDon(int maHoaDon) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_CHITIET)) {

            ps.setInt(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                String maPhong = rs.getString("MaPhong");

                int dcv = rs.getInt("DienCu");  Integer dienCu  = rs.wasNull() ? null : dcv;
                int dmv = rs.getInt("DienMoi"); Integer dienMoi = rs.wasNull() ? null : dmv;
                int ncv = rs.getInt("NuocCu");  Integer nuocCu  = rs.wasNull() ? null : ncv;
                int nmv = rs.getInt("NuocMoi"); Integer nuocMoi = rs.wasNull() ? null : nmv;

                long donGiaDien = rs.getLong("DonGiaDien"); if (rs.wasNull()) donGiaDien = 0;
                long donGiaNuoc = rs.getLong("DonGiaNuoc"); if (rs.wasNull()) donGiaNuoc = 0;
                int  soNguoi    = rs.getInt("SoNguoi");     if (rs.wasNull() || soNguoi < 1) soNguoi = 1;

                long tienDien = rs.getLong("TienDien");
                long tienNuoc = rs.getLong("TienNuoc");

                List<Object[]> dichVuKhac = layDichVuKhac(conn, maHoaDon);
                long tongDV = rs.getLong("TienDichVuKhac");

                // ── Số ngày ở trong tháng (cho dòng "Tiền phòng") ──
                String thangNam = rs.getString("ThangNam");
                int soNgayTrongThang = 30;
                int soNgayO = soNgayTrongThang;
                LocalDate ngayBD = null;
                List<Object[]> nguoiOGhep = new ArrayList<>();
                try {
                    int month = Integer.parseInt(thangNam.substring(0, 2));
                    int year  = Integer.parseInt(thangNam.substring(3));
                    soNgayTrongThang = YearMonth.of(year, month).lengthOfMonth();
                    soNgayO = soNgayTrongThang;
                    
                    LocalDate firstDay = LocalDate.of(year, month, 1);
                    LocalDate lastDay = firstDay.withDayOfMonth(soNgayTrongThang);
                    int maHopDong = rs.getInt("MaHopDong");
                    nguoiOGhep = layNguoiOGhepTrongThang(conn, maHopDong, firstDay, lastDay);

                    java.sql.Date sqlNgayBD = rs.getDate("NgayBatDau");
                    if (sqlNgayBD != null) {
                        ngayBD = sqlNgayBD.toLocalDate();
                        if (ngayBD.getMonthValue() == month && ngayBD.getYear() == year
                                && ngayBD.getDayOfMonth() > 1) {
                            soNgayO = soNgayTrongThang - ngayBD.getDayOfMonth() + 1;
                        }
                    }
                } catch (Exception ignore) {}

                return new ChiTietHoaDon(
                    rs.getInt("MaHoaDon"),
                    maPhong,
                    rs.getString("TenPhong"),
                    thangNam,
                    rs.getString("HoTen"),
                    rs.getString("Email"),
                    rs.getLong("TienPhong"),
                    dienCu, dienMoi, donGiaDien, tienDien,
                    nuocCu, nuocMoi, donGiaNuoc, tienNuoc,
                    dichVuKhac, tongDV,
                    soNguoi,
                    soNgayO, soNgayTrongThang,
                    ngayBD,
                    nguoiOGhep,
                    rs.getLong("TongTien"),
                    rs.getString("TrangThai")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Mỗi dòng: [TenDichVu, CachTinh, DonGia, SoLuong, DonVi, ThanhTien] — snapshot lúc tạo hóa đơn. */
    private static List<Object[]> layDichVuKhac(Connection conn, int maHoaDon)
            throws SQLException {
        List<Object[]> list = new ArrayList<>();
        try (PreparedStatement ps2 = conn.prepareStatement(SQL_DICHVU_KHAC)) {
            ps2.setInt(1, maHoaDon);
            try (ResultSet rs2 = ps2.executeQuery()) {
                while (rs2.next()) {
                    list.add(new Object[]{
                        rs2.getString("TenDichVu"),
                        rs2.getString("CachTinh"),
                        rs2.getLong("DonGia"),
                        rs2.getDouble("SoLuong"),
                        rs2.getString("DonVi"),
                        rs2.getLong("ThanhTien")
                    });
                }
            }
        }
        return list;
    }

    private static List<Object[]> layNguoiOGhepTrongThang(Connection conn, int maHopDong,
                                                          LocalDate firstDay, LocalDate lastDay) {
        String sql =
            "SELECT NgayVao, NgayDi FROM ChiTiet_HopDong_NguoiOGhep " +
            "WHERE MaHopDong = ? " +
            "  AND (NgayVao IS NULL OR NgayVao <= ?) " +
            "  AND (NgayDi  IS NULL OR NgayDi  >= ?)";
        List<Object[]> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHopDong);
            ps.setDate(2, java.sql.Date.valueOf(lastDay));
            ps.setDate(3, java.sql.Date.valueOf(firstDay));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date sqlVao = rs.getDate("NgayVao");
                    java.sql.Date sqlDi  = rs.getDate("NgayDi");
                    LocalDate ngayVao = sqlVao != null ? sqlVao.toLocalDate() : null;
                    LocalDate ngayDi  = sqlDi  != null ? sqlDi.toLocalDate()  : null;
                    list.add(new Object[]{ngayVao, ngayDi});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static ArrayList<String> layDanhSachThangNam() {
        String sql =
            "SELECT ThangNam FROM (" +
            "  SELECT DISTINCT ThangNam FROM HoaDon " +
            "  UNION " +
            "  SELECT DISTINCT ThangNam FROM ChiSo_DienNuoc" +
            ") t " +
            "ORDER BY CAST(RIGHT(ThangNam,4) AS INT) DESC, CAST(LEFT(ThangNam,2) AS INT) DESC";
        ArrayList<String> ds = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) ds.add(rs.getString(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ds.isEmpty()) {
            LocalDate now = LocalDate.now();
            ds.add(String.format("%02d-%04d", now.getMonthValue(), now.getYear()));
        }
        return ds;
    }

    public static void capNhatTrangThai(int maHoaDon, String trangThai) {
        String sql = "UPDATE HoaDon SET TrangThai = ? WHERE MaHoaDon = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setInt(2, maHoaDon);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3) TẠO HÓA ĐƠN TỰ ĐỘNG cho 1 tháng (tính năng mới)
    //
    // Logic:
    //   - Duyệt từng phòng có hợp đồng "Còn hiệu lực" và có Chi số tháng đó
    //     (DienMoi và NuocMoi đã được nhập, không null)
    //   - Bỏ qua phòng đã có hóa đơn tháng đó rồi (tránh trùng)
    //   - Tính TienPhong, TienDien, TienNuoc, TienDichVuKhac → INSERT
    //
    // Trả về tóm tắt: [sốHĐ đã tạo, sốHĐ bỏ qua do thiếu chỉ số, sốHĐ đã có sẵn]
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Tính số người hiệu quả (có thể lẻ) cho tháng invoice.
     * Người chính = 1.0. Mỗi người ghép = số ngày ở trong tháng / số ngày của tháng đó.
     * Ví dụ: ghép trọn tháng 31 ngày → 31/31 = 1.0. Ghép vào ngày 16 (tháng 30 ngày) → 15/30 = 0.5.
     */
    private static double getEffectiveSoNguoi(Connection conn, int maHopDong,
                                               LocalDate firstDay, LocalDate lastDay) {
        String sql =
            "SELECT NgayVao, NgayDi FROM ChiTiet_HopDong_NguoiOGhep " +
            "WHERE MaHopDong = ? " +
            "  AND (NgayVao IS NULL OR NgayVao <= ?) " +
            "  AND (NgayDi  IS NULL OR NgayDi  >= ?)";
        double total = 1.0;
        long soNgayTrongThang = ChronoUnit.DAYS.between(firstDay, lastDay) + 1;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt (1, maHopDong);
            ps.setDate(2, java.sql.Date.valueOf(lastDay));
            ps.setDate(3, java.sql.Date.valueOf(firstDay));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                java.sql.Date sqlVao = rs.getDate("NgayVao");
                java.sql.Date sqlDi  = rs.getDate("NgayDi");
                LocalDate start = (sqlVao != null) ? sqlVao.toLocalDate() : firstDay;
                LocalDate end   = (sqlDi  != null) ? sqlDi.toLocalDate()  : lastDay;
                if (start.isBefore(firstDay)) start = firstDay;
                if (end.isAfter(lastDay))     end   = lastDay;
                long days = ChronoUnit.DAYS.between(start, end) + 1;
                total += (double) days / soNgayTrongThang;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return total;
    }

    /**
     * Tạo hoặc cập nhật hóa đơn cho tháng chỉ định.
     * - Nếu hóa đơn chưa tồn tại: INSERT mới (TrangThai = 'Chưa thanh toán').
     * - Nếu hóa đơn đã tồn tại: UPDATE các khoản tiền, giữ nguyên TrangThai và MaHoaDon.
     * - Phòng thiếu chỉ số hợp lệ: bỏ qua (thieuChiSo++).
     * Trả về: [daTao, daCapNhat, thieuChiSo]
     */
    /** Dịch vụ (trừ Điện/Nước) áp dụng cho một hợp đồng — dùng khi tạo hóa đơn tháng. */
    private static final String SQL_DICHVU_HOPDONG =
        "SELECT dv.MaDichVu, dv.TenDichVu, dv.GiaDichVu, dv.DonViTinh, dv.CachTinh " +
        "FROM ChiTiet_HopDong_DichVu ctdv " +
        "JOIN DichVu dv ON ctdv.MaDichVu = dv.MaDichVu " +
        "WHERE ctdv.MaHopDong = ? AND dv.TenDichVu NOT IN (N'Điện', N'Nước')";

    private static final String SQL_DELETE_SNAPSHOT =
        "DELETE FROM ChiTiet_HoaDon_DichVu WHERE MaHoaDon = ?";

    private static final String SQL_INSERT_SNAPSHOT =
        "INSERT INTO ChiTiet_HoaDon_DichVu " +
        "(MaHoaDon, MaDichVu, TenDichVu, CachTinh, DonGia, SoLuong, DonViTinh, ThanhTien) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    public static int[] taoHoaDonChoThang(String thangNam) {
        int daTao = 0, daCapNhat = 0, thieuChiSo = 0;
        int invoiceMonth = Integer.parseInt(thangNam.substring(0, 2));
        int invoiceYear  = Integer.parseInt(thangNam.substring(3));
        LocalDate firstDay = LocalDate.of(invoiceYear, invoiceMonth, 1);
        LocalDate lastDay  = firstDay.withDayOfMonth(YearMonth.of(invoiceYear, invoiceMonth).lengthOfMonth());

        // Lấy tất cả phòng đang có hợp đồng hiệu lực kèm chỉ số tháng đó
        String sqlCanTao =
            "SELECT p.MaPhong, p.TenPhong, " +
            "       hop.MaHopDong, hop.GiaThueThucTe, hop.NgayBatDau, " +
            "       cs.DienCu, cs.DienMoi, cs.NuocCu, cs.NuocMoi, " +
            "       dvD.GiaDichVu AS DonGiaDien, dvN.GiaDichVu AS DonGiaNuoc " +
            "FROM Phong p " +
            "OUTER APPLY ( " +
            "    SELECT TOP 1 MaHopDong, GiaThueThucTe, NgayBatDau " +
            "    FROM HopDong " +
            "    WHERE MaPhong = p.MaPhong " +
            "      AND TrangThai IN (N'Còn hiệu lực', N'Chờ nhận phòng') " +
            "    ORDER BY NgayBatDau DESC " +
            ") hop " +
            "LEFT JOIN ChiSo_DienNuoc cs " +
            "    ON cs.MaPhong = p.MaPhong AND cs.ThangNam = ? " +
            "LEFT JOIN (SELECT TOP 1 GiaDichVu FROM DichVu WHERE TenDichVu = N'Điện') dvD ON 1=1 " +
            "LEFT JOIN (SELECT TOP 1 GiaDichVu FROM DichVu WHERE TenDichVu = N'Nước') dvN ON 1=1 " +
            "WHERE hop.MaHopDong IS NOT NULL";

        // UPDATE theo MaHopDong+ThangNam — không cần SELECT trước. OUTPUT để lấy MaHoaDon.
        String sqlUpdate =
            "UPDATE HoaDon SET TienPhong=?, TienDien=?, TienNuoc=?, TienDichVuKhac=? " +
            "OUTPUT INSERTED.MaHoaDon " +
            "WHERE MaHopDong=? AND ThangNam=?";

        String sqlInsert =
            "INSERT INTO HoaDon (MaHopDong, ThangNam, TienPhong, TienDien, TienNuoc, TienDichVuKhac, TrangThai) " +
            "OUTPUT INSERTED.MaHoaDon " +
            "VALUES (?, ?, ?, ?, ?, ?, N'Chưa thanh toán')";

        // ── Pass 1: thu thập toàn bộ dữ liệu phòng, đóng ResultSet trước khi làm gì khác ──
        // Tránh nested ResultSet trên cùng Connection (SQL Server JDBC không hỗ trợ).
        // Mỗi phần tử: [maHopDong, giaThue, ngayBatDau, dienCu, dienMoi, nuocCu, nuocMoi, donGiaDien, donGiaNuoc]
        List<Object[]> phongList = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psMain = conn.prepareStatement(sqlCanTao)) {
            psMain.setString(1, thangNam);
            try (ResultSet rs = psMain.executeQuery()) {
                while (rs.next()) {
                    int dienCu  = rs.getInt("DienCu");  boolean dcNull = rs.wasNull();
                    int dienMoi = rs.getInt("DienMoi"); boolean dmNull = rs.wasNull();
                    int nuocCu  = rs.getInt("NuocCu");  boolean ncNull = rs.wasNull();
                    int nuocMoi = rs.getInt("NuocMoi"); boolean nmNull = rs.wasNull();

                    if (dcNull || dmNull || ncNull || nmNull || dienMoi < dienCu || nuocMoi < nuocCu) {
                        thieuChiSo++;
                        continue;
                    }
                    phongList.add(new Object[]{
                        rs.getInt("MaHopDong"),
                        rs.getLong("GiaThueThucTe"),
                        rs.getDate("NgayBatDau"),      // java.sql.Date hoặc null
                        dienCu, dienMoi, nuocCu, nuocMoi,
                        rs.getLong("DonGiaDien"),
                        rs.getLong("DonGiaNuoc")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new int[]{daTao, daCapNhat, thieuChiSo};
        }

        // ── Pass 2: tính tiền + ghi snapshot dịch vụ ──
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psUpdate   = conn.prepareStatement(sqlUpdate);
             PreparedStatement psInsert   = conn.prepareStatement(sqlInsert);
             PreparedStatement psDichVu   = conn.prepareStatement(SQL_DICHVU_HOPDONG);
             PreparedStatement psDelSnap  = conn.prepareStatement(SQL_DELETE_SNAPSHOT);
             PreparedStatement psInsSnap  = conn.prepareStatement(SQL_INSERT_SNAPSHOT)) {

            for (Object[] row : phongList) {
                int  maHopDong  = (int)  row[0];
                long tienPhong  = (long) row[1];
                java.sql.Date sqlNgayBD = (java.sql.Date) row[2];
                int  dienCu     = (int)  row[3];
                int  dienMoi    = (int)  row[4];
                int  nuocCu     = (int)  row[5];
                int  nuocMoi    = (int)  row[6];
                long donGiaDien = (long) row[7];
                long donGiaNuoc = (long) row[8];

                // Pro-rate nếu HĐ bắt đầu giữa tháng (áp dụng cho tiền phòng + dịch vụ)
                double proRate = 1.0;
                if (sqlNgayBD != null) {
                    LocalDate ngayBD = sqlNgayBD.toLocalDate();
                    if (ngayBD.getMonthValue() == invoiceMonth && ngayBD.getYear() == invoiceYear
                            && ngayBD.getDayOfMonth() > 1) {
                        int  soNgayThang = YearMonth.of(invoiceYear, invoiceMonth).lengthOfMonth();
                        long daysStayed  = soNgayThang - ngayBD.getDayOfMonth() + 1;
                        tienPhong = tienPhong * daysStayed / soNgayThang;
                        proRate   = (double) daysStayed / soNgayThang;
                    }
                }

                double effectiveSoNguoi = getEffectiveSoNguoi(conn, maHopDong, firstDay, lastDay);

                long tienDien = (long)(dienMoi - dienCu) * donGiaDien;
                long tienNuoc = (long)(nuocMoi - nuocCu) * donGiaNuoc;

                // ── Lấy danh sách dịch vụ của hợp đồng, tính từng dòng theo CachTinh ──
                List<Object[]> dsDichVu = new ArrayList<>(); // [maDichVu, ten, cachTinh, donGia, soLuong, donVi, thanhTien]
                long tienDvKhac = 0;
                psDichVu.setInt(1, maHopDong);
                try (ResultSet rsDv = psDichVu.executeQuery()) {
                    while (rsDv.next()) {
                        long giaDichVu = rsDv.getLong("GiaDichVu");
                        String cachTinh = rsDv.getString("CachTinh");
                        boolean theoNguoi = ThongTinDichVu.THEO_NGUOI.equals(cachTinh);
                        double soLuong = theoNguoi ? effectiveSoNguoi : 1.0;
                        long thanhTien = theoNguoi
                                ? Math.round(giaDichVu * proRate * effectiveSoNguoi)
                                : Math.round(giaDichVu * proRate);
                        tienDvKhac += thanhTien;
                        dsDichVu.add(new Object[]{
                            rsDv.getInt("MaDichVu"),
                            rsDv.getString("TenDichVu"),
                            cachTinh,
                            giaDichVu,
                            soLuong,
                            rsDv.getString("DonViTinh"),
                            thanhTien
                        });
                    }
                }

                // Thử UPDATE trước — nếu hóa đơn đã tồn tại sẽ trả về dòng MaHoaDon
                psUpdate.setLong  (1, tienPhong);
                psUpdate.setLong  (2, tienDien);
                psUpdate.setLong  (3, tienNuoc);
                psUpdate.setLong  (4, tienDvKhac);
                psUpdate.setInt   (5, maHopDong);
                psUpdate.setString(6, thangNam);

                Integer maHoaDon = null;
                try (ResultSet rsU = psUpdate.executeQuery()) {
                    if (rsU.next()) maHoaDon = rsU.getInt("MaHoaDon");
                }

                if (maHoaDon == null) {
                    // Chưa tồn tại → INSERT mới
                    psInsert.setInt   (1, maHopDong);
                    psInsert.setString(2, thangNam);
                    psInsert.setLong  (3, tienPhong);
                    psInsert.setLong  (4, tienDien);
                    psInsert.setLong  (5, tienNuoc);
                    psInsert.setLong  (6, tienDvKhac);
                    try (ResultSet rsI = psInsert.executeQuery()) {
                        if (rsI.next()) maHoaDon = rsI.getInt("MaHoaDon");
                    }
                    daTao++;
                } else {
                    daCapNhat++;
                }

                // Ghi lại snapshot chi tiết dịch vụ cho hóa đơn này
                psDelSnap.setInt(1, maHoaDon);
                psDelSnap.executeUpdate();

                for (Object[] dv : dsDichVu) {
                    psInsSnap.setInt   (1, maHoaDon);
                    psInsSnap.setInt   (2, (int) dv[0]);
                    psInsSnap.setString(3, (String) dv[1]);
                    psInsSnap.setString(4, (String) dv[2]);
                    psInsSnap.setLong  (5, (long) dv[3]);
                    psInsSnap.setDouble(6, (double) dv[4]);
                    psInsSnap.setString(7, (String) dv[5]);
                    psInsSnap.setLong  (8, (long) dv[6]);
                    psInsSnap.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new int[]{daTao, daCapNhat, thieuChiSo};
    }

    /**
     * Lấy danh sách hóa đơn kèm email khách thuê để gửi mail hàng loạt.
     * filterTrangThai: "Đã thanh toán", "Chưa thanh toán", hoặc null (tất cả).
     * Chỉ trả về hóa đơn của khách có địa chỉ email hợp lệ.
     * Mỗi phần tử: [maHoaDon, tenPhong, hoTenKhach, email, tongTien, trangThai]
     */
    public static ArrayList<Object[]> layHoaDonVoiEmail(String thangNam, String filterTrangThai) {
        String sql =
            "SELECT hd.MaHoaDon, p.TenPhong, kt.HoTen, kt.Email, " +
            "       hd.TongTien, hd.TrangThai " +
            "FROM HoaDon hd " +
            "JOIN HopDong hop ON hd.MaHopDong = hop.MaHopDong " +
            "JOIN Phong p ON hop.MaPhong = p.MaPhong " +
            "LEFT JOIN KhachThue kt ON hop.MaKhachThue = kt.MaKhachThue " +
            "WHERE hd.ThangNam = ? " +
            (filterTrangThai != null ? "AND hd.TrangThai = ? " : "") +
            "AND kt.Email IS NOT NULL AND LTRIM(RTRIM(kt.Email)) <> '' " +
            "ORDER BY p.TenPhong";
        ArrayList<Object[]> ds = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, thangNam);
            if (filterTrangThai != null) ps.setString(2, filterTrangThai);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ds.add(new Object[]{
                    rs.getInt("MaHoaDon"),
                    rs.getString("TenPhong"),
                    rs.getString("HoTen"),
                    rs.getString("Email"),
                    rs.getLong("TongTien"),
                    rs.getString("TrangThai")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    public static void xoaHoaDon(int maHoaDon) {
        String sql = "DELETE FROM HoaDon WHERE MaHoaDon=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHoaDon);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4) Dashboard stats
    // ─────────────────────────────────────────────────────────────────────────
    /** [totalPhong, dangThue, trong] */
    public static int[] thongKePhong() {
        int total = 0, dangThue = 0, trong = 0;
        String sql = "SELECT trangThai, COUNT(*) AS cnt FROM Phong GROUP BY trangThai";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String t = rs.getString("trangThai");
                int c = rs.getInt("cnt");
                total += c;
                if (t == null || t.equalsIgnoreCase("Trống")) trong += c;
                else dangThue += c;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new int[]{total, dangThue, trong};
    }

    public static int demKhachThue() {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM KhachThue")) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int demHopDongConHieuLuc() {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM HopDong WHERE TrangThai = N'Còn hiệu lực'")) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Tổng tiền chưa thanh toán trên toàn bộ tháng (không lọc theo tháng cụ thể). */
    public static long tongTienChuaThanhToanTatCaThang() {
        String sql = "SELECT ISNULL(SUM(TongTien), 0) FROM HoaDon WHERE TrangThai NOT LIKE N'%Đã%'";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** [doanhThuDaThu, conPhaiThu, soHoaDonChua] cho tháng truyền vào. */
    public static long[] thongKeDoanhThu(String thangNam) {
        long daThu = 0, conThu = 0, soHDChua = 0;
        String sql =
            "SELECT TrangThai, SUM(TongTien) AS Tong, COUNT(*) AS Cnt " +
            "FROM HoaDon WHERE ThangNam = ? GROUP BY TrangThai";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, thangNam);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String tt = rs.getString("TrangThai");
                long tong = rs.getLong("Tong");
                int cnt = rs.getInt("Cnt");
                if (tt != null && tt.contains("Đã")) {
                    daThu += tong;
                } else {
                    conThu += tong;
                    soHDChua += cnt;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new long[]{daThu, conThu, soHDChua};
    }

    /** Doanh thu 6 tháng gần nhất (chỉ tính Đã thanh toán). */
    public static ArrayList<Object[]> doanhThu6ThangGanNhat() {
        ArrayList<Object[]> ds = new ArrayList<>();
        String sql =
            "SELECT TOP 6 ThangNam, SUM(CASE WHEN TrangThai LIKE N'%Đã%' THEN TongTien ELSE 0 END) AS DaThu, " +
            "       SUM(CASE WHEN TrangThai NOT LIKE N'%Đã%' OR TrangThai IS NULL THEN TongTien ELSE 0 END) AS ConThu " +
            "FROM HoaDon " +
            "GROUP BY ThangNam " +
            "ORDER BY CAST(RIGHT(ThangNam,4) AS INT) DESC, " +
            "         CAST(LEFT(ThangNam,2) AS INT) DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ds.add(new Object[]{rs.getString("ThangNam"), rs.getLong("DaThu"), rs.getLong("ConThu")});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    /** [daThuCount, chuaThuCount] cho tháng truyền vào. */
    public static int[] thongKeTrangThaiCount(String thangNam) {
        int[] result = {0, 0};
        String sql = "SELECT TrangThai, COUNT(*) AS Cnt FROM HoaDon WHERE ThangNam = ? GROUP BY TrangThai";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, thangNam);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String tt = rs.getString("TrangThai");
                int cnt = rs.getInt("Cnt");
                if (tt != null && tt.contains("Đã")) result[0] += cnt;
                else result[1] += cnt;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    /** Danh sách phòng chưa thanh toán tháng truyền vào. Mỗi phần tử: [TenPhong, HoTen, TongTien]. */
    public static ArrayList<Object[]> getPhongChuaThanhToan(String thangNam) {
        ArrayList<Object[]> ds = new ArrayList<>();
        String sql =
            "SELECT p.TenPhong, kt.HoTen, hd.TongTien " +
            "FROM HoaDon hd " +
            "JOIN HopDong hop ON hd.MaHopDong = hop.MaHopDong " +
            "JOIN Phong p ON hop.MaPhong = p.MaPhong " +
            "LEFT JOIN KhachThue kt ON hop.MaKhachThue = kt.MaKhachThue " +
            "WHERE hd.ThangNam = ? AND hd.TrangThai NOT LIKE N'%Đã%' " +
            "ORDER BY p.TenPhong";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, thangNam);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ds.add(new Object[]{
                    rs.getString("TenPhong"),
                    rs.getString("HoTen"),
                    rs.getLong("TongTien")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ds;
    }

    /**
     * Doanh thu 6 tháng gần nhất phân theo loại: tiền phòng, điện+nước, dịch vụ khác.
     * Tính trên toàn bộ hóa đơn (không phân biệt trạng thái thanh toán).
     * Mỗi phần tử: [ThangNam (String), TienPhong (long), TienDienNuoc (long), TienDichVu (long)]
     */
    public static ArrayList<Object[]> doanhThu6ThangChiTiet() {
        ArrayList<Object[]> ds = new ArrayList<>();
        String sql =
            "SELECT TOP 6 ThangNam, " +
            "    SUM(TienPhong)             AS TienPhong, " +
            "    SUM(TienDien + TienNuoc)   AS TienDienNuoc, " +
            "    SUM(TienDichVuKhac)        AS TienDichVu " +
            "FROM HoaDon " +
            "GROUP BY ThangNam " +
            "ORDER BY CAST(RIGHT(ThangNam,4) AS INT) DESC, " +
            "         CAST(LEFT(ThangNam,2) AS INT) DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ds.add(new Object[]{
                    rs.getString("ThangNam"),
                    rs.getLong("TienPhong"),
                    rs.getLong("TienDienNuoc"),
                    rs.getLong("TienDichVu")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }
}
