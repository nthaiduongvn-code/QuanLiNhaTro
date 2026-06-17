package QuanLiNhaTro;

import java.sql.*;
import java.util.ArrayList;

public class HopDongDAO {

    /**
     * Đồng bộ TrangThai DB: hợp đồng quá hạn ngày → "Hết hiệu lực".
     * Cũng chuẩn hoá các giá trị cũ (Đã thanh lý, Chưa thanh lý).
     */
    public static void dongBoTrangThaiHopDong() {
        String sql =
            "UPDATE HopDong SET TrangThai = N'Hết hiệu lực' " +
            "WHERE TrangThai NOT IN (N'Hết hiệu lực') " +
            "  AND (" +
            "    TrangThai IN (N'Đã thanh lý', N'Chưa thanh lý') " +
            "    OR (NgayKetThuc IS NOT NULL AND YEAR(NgayKetThuc) < 3000 " +
            "        AND NgayKetThuc <= CAST(GETDATE() AS DATE))" +
            "  )";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Xóa một hợp đồng cùng toàn bộ dữ liệu con liên quan. */
    public static void xoaHopDong(int maHopDong) {
        try (Connection conn = DBConnection.getConnection()) {
            String maPhong = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT MaPhong FROM HopDong WHERE MaHopDong = ?")) {
                ps.setInt(1, maHopDong);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) maPhong = rs.getString(1);
            }
            // Xóa bảng con trước để tránh FK constraint
            for (String sql : new String[]{
                    "DELETE FROM HoaDon                    WHERE MaHopDong = ?",
                    "DELETE FROM ChiTiet_HopDong_NguoiOGhep WHERE MaHopDong = ?",
                    "DELETE FROM ChiTiet_HopDong_DichVu    WHERE MaHopDong = ?"
            }) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, maHopDong);
                    ps.executeUpdate();
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM HopDong WHERE MaHopDong = ?")) {
                ps.setInt(1, maHopDong);
                ps.executeUpdate();
            }
            if (maPhong != null && !coHopDongConHieuLuc(maPhong)) {
                RoomDAO.capNhatTrangThai(maPhong, "Trống");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<ThongTinHopDong> getFullHopDong() {
        dongBoTrangThaiHopDong();
        String sql =
            "SELECT h.MaHopDong, h.MaPhong, p.TenPhong, " +
            "       h.MaKhachThue, kt.HoTen, " +
            "       h.GiaThueThucTe, h.TienCoc, " +
            "       CONVERT(VARCHAR,h.NgayBatDau,23) AS NgayBatDau, " +
            "       CONVERT(VARCHAR,h.NgayKetThuc,23) AS NgayKetThuc, " +
            "       h.HinhAnh, h.TrangThai, h.GhiChu, " +
            "       STRING_AGG(dv.TenDichVu, '|') AS DanhSachDichVu " +
            "FROM HopDong h " +
            "LEFT JOIN Phong p        ON h.MaPhong      = p.MaPhong " +
            "LEFT JOIN KhachThue kt   ON h.MaKhachThue  = kt.MaKhachThue " +
            "LEFT JOIN ChiTiet_HopDong_DichVu ctdv ON h.MaHopDong = ctdv.MaHopDong " +
            "LEFT JOIN DichVu dv      ON ctdv.MaDichVu  = dv.MaDichVu " +
            "GROUP BY h.MaHopDong, h.MaPhong, p.TenPhong, h.MaKhachThue, kt.HoTen, " +
            "         h.GiaThueThucTe, h.TienCoc, h.NgayBatDau, h.NgayKetThuc, " +
            "         h.HinhAnh, h.TrangThai, h.GhiChu " +
            "ORDER BY h.MaHopDong DESC";

        ArrayList<ThongTinHopDong> ds = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new ThongTinHopDong(
                        rs.getInt("MaHopDong"),
                        rs.getString("MaPhong"),
                        rs.getString("TenPhong"),
                        rs.getInt("MaKhachThue"),
                        rs.getString("HoTen"),
                        rs.getInt("GiaThueThucTe"),
                        rs.getInt("TienCoc"),
                        rs.getString("NgayBatDau"),
                        rs.getString("NgayKetThuc"),
                        rs.getString("HinhAnh"),
                        rs.getString("DanhSachDichVu"),
                        rs.getString("TrangThai"),
                        rs.getString("GhiChu")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    public static ThongTinHopDong getHopDongHienTaiByPhong(String maPhong) {
        String sql =
            "SELECT TOP 1 h.MaHopDong, h.MaPhong, p.TenPhong, " +
            "       h.MaKhachThue, kt.HoTen, kt.SoDienThoai, kt.SoCCCD, " +
            "       h.GiaThueThucTe, h.TienCoc, " +
            "       CONVERT(VARCHAR,h.NgayBatDau,23) AS NgayBatDau, " +
            "       CONVERT(VARCHAR,h.NgayKetThuc,23) AS NgayKetThuc, " +
            "       h.HinhAnh, h.TrangThai, h.GhiChu " +
            "FROM HopDong h " +
            "JOIN Phong p ON h.MaPhong = p.MaPhong " +
            "JOIN KhachThue kt ON h.MaKhachThue = kt.MaKhachThue " +
            "WHERE h.MaPhong = ? " +
            "  AND h.TrangThai IN (N'Còn hiệu lực', N'Chờ nhận phòng') " +
            "ORDER BY h.NgayBatDau DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhong);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ThongTinHopDong hd = new ThongTinHopDong(
                        rs.getInt("MaHopDong"),
                        rs.getString("MaPhong"),
                        rs.getString("TenPhong"),
                        rs.getInt("MaKhachThue"),
                        rs.getString("HoTen"),
                        rs.getInt("GiaThueThucTe"),
                        rs.getInt("TienCoc"),
                        rs.getString("NgayBatDau"),
                        rs.getString("NgayKetThuc"),
                        rs.getString("HinhAnh"),
                        null,
                        rs.getString("TrangThai"),
                        rs.getString("GhiChu")
                );
                hd.setSoDienThoai(rs.getString("SoDienThoai"));
                hd.setSoCCCD(rs.getString("SoCCCD"));
                return hd;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ThongTinHopDong getHopDongForEdit(int maHopDong) {
        String sql =
            "SELECT h.MaHopDong, h.MaPhong, p.TenPhong, " +
            "       h.MaKhachThue, kt.HoTen, " +
            "       h.GiaThueThucTe, h.TienCoc, " +
            "       CONVERT(VARCHAR,h.NgayBatDau,23) AS NgayBatDau, " +
            "       CONVERT(VARCHAR,h.NgayKetThuc,23) AS NgayKetThuc, " +
            "       h.HinhAnh, h.TrangThai, h.GhiChu, " +
            "       STRING_AGG(dv.TenDichVu, '|') AS DanhSachDichVu " +
            "FROM HopDong h " +
            "LEFT JOIN Phong p        ON h.MaPhong      = p.MaPhong " +
            "LEFT JOIN KhachThue kt   ON h.MaKhachThue  = kt.MaKhachThue " +
            "LEFT JOIN ChiTiet_HopDong_DichVu ctdv ON h.MaHopDong = ctdv.MaHopDong " +
            "LEFT JOIN DichVu dv      ON ctdv.MaDichVu  = dv.MaDichVu " +
            "WHERE h.MaHopDong = ? " +
            "GROUP BY h.MaHopDong, h.MaPhong, p.TenPhong, h.MaKhachThue, kt.HoTen, " +
            "         h.GiaThueThucTe, h.TienCoc, h.NgayBatDau, h.NgayKetThuc, " +
            "         h.HinhAnh, h.TrangThai, h.GhiChu";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHopDong);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ThongTinHopDong hd = new ThongTinHopDong(
                        rs.getInt("MaHopDong"),
                        rs.getString("MaPhong"),
                        rs.getString("TenPhong"),
                        rs.getInt("MaKhachThue"),
                        rs.getString("HoTen"),
                        rs.getInt("GiaThueThucTe"),
                        rs.getInt("TienCoc"),
                        rs.getString("NgayBatDau"),
                        rs.getString("NgayKetThuc"),
                        rs.getString("HinhAnh"),
                        rs.getString("DanhSachDichVu"),
                        rs.getString("TrangThai"),
                        rs.getString("GhiChu")
                );
                hd.setDanhSachMaDichVu(getDanhSachMaDichVu(maHopDong));
                return hd;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Integer> getDanhSachMaDichVu(int maHopDong) {
        String sql = "SELECT MaDichVu FROM ChiTiet_HopDong_DichVu WHERE MaHopDong = ?";
        ArrayList<Integer> ds = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHopDong);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ds.add(rs.getInt("MaDichVu"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    /** Thêm HĐ mới và tự cập nhật trạng thái phòng tương ứng. */
    public static void themHopDongMoi(String maPhong, int maKhachThue, int giaThueThucTe,
                                       int tienCoc, String ngayBatDau, String ngayKetThuc,
                                       String hinhAnh, String trangThai, String ghiChu,
                                       ArrayList<Integer> danhSachMaDichVu,
                                       int chiSoDien, int chiSoNuoc,
                                       ArrayList<Integer> dsMaKhachOGhep) {
        String sqlHD =
            "INSERT INTO HopDong (MaPhong, MaKhachThue, GiaThueThucTe, TienCoc, " +
            "NgayBatDau, NgayKetThuc, HinhAnh, TrangThai, GhiChu) VALUES (?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlHD, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, maPhong);
            ps.setInt(2, maKhachThue);
            ps.setInt(3, giaThueThucTe);
            ps.setInt(4, tienCoc);
            ps.setDate(5, Date.valueOf(ngayBatDau));
            setDateOrNull(ps, 6, ngayKetThuc);
            ps.setString(7, nullIfEmpty(hinhAnh));
            ps.setString(8, trangThai);
            ps.setString(9, nullIfEmpty(ghiChu));
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int maHopDong = keys.getInt(1);
                themChiTietDichVu(conn, maHopDong, danhSachMaDichVu);
                luuNguoiOGhep(conn, maHopDong, dsMaKhachOGhep);
            }

            // Tự cập nhật trạng thái phòng và thêm chỉ số điện-nước ban đầu
            if ("Còn hiệu lực".equals(trangThai) || "Chờ nhận phòng".equals(trangThai)) {
                RoomDAO.capNhatTrangThai(maPhong, "Đang thuê");
                try { ChiSoDAO.themPhongVaoThangHienTaiVoiChiSo(Integer.parseInt(maPhong), chiSoDien, chiSoNuoc, ngayBatDau); }
                catch (NumberFormatException ignore) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void suaHopDong(int maHopDong, String maPhong, int maKhachThue,
                                   int giaThueThucTe, int tienCoc,
                                   String ngayBatDau, String ngayKetThuc,
                                   String hinhAnh, String trangThai, String ghiChu,
                                   ArrayList<Integer> danhSachMaDichVu,
                                   ArrayList<Integer> dsMaKhachOGhep) {
        String sqlUpdate =
            "UPDATE HopDong SET MaPhong=?, MaKhachThue=?, GiaThueThucTe=?, TienCoc=?, " +
            "NgayBatDau=?, NgayKetThuc=?, HinhAnh=?, TrangThai=?, GhiChu=? WHERE MaHopDong=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
            ps.setString(1, maPhong);
            ps.setInt(2, maKhachThue);
            ps.setInt(3, giaThueThucTe);
            ps.setInt(4, tienCoc);
            ps.setDate(5, Date.valueOf(ngayBatDau));
            setDateOrNull(ps, 6, ngayKetThuc);
            ps.setString(7, nullIfEmpty(hinhAnh));
            ps.setString(8, trangThai);
            ps.setString(9, nullIfEmpty(ghiChu));
            ps.setInt(10, maHopDong);
            ps.executeUpdate();
            xoaChiTietDichVu(conn, maHopDong);
            themChiTietDichVu(conn, maHopDong, danhSachMaDichVu);
            luuNguoiOGhep(conn, maHopDong, dsMaKhachOGhep);
            if ("Còn hiệu lực".equals(trangThai) || "Chờ nhận phòng".equals(trangThai)) {
                RoomDAO.capNhatTrangThai(maPhong, "Đang thuê");
                try { ChiSoDAO.themPhongVaoThangHienTai(Integer.parseInt(maPhong)); }
                catch (NumberFormatException ignore) {}
            } else if ("Hết hiệu lực".equals(trangThai)) {
                if (!coHopDongConHieuLuc(maPhong))
                    RoomDAO.capNhatTrangThai(maPhong, "Trống");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Đếm số HoaDon thuộc hợp đồng này. */
    public static int demHoaDonSauHopDong(String maPhong, int maHopDong) {
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE MaHopDong = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHopDong);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    /** Xóa hợp đồng cùng toàn bộ dữ liệu liên quan (HoaDon, DichVu, NguoiOGhep). */
    public static void xoaHopDongVaHoaDon(int maHopDong, String maPhong) {
        xoaHopDong(maHopDong);
    }

    public static void thanhLyHopDong(int maHopDong) {
        String sqlGetPhong = "SELECT MaPhong FROM HopDong WHERE MaHopDong = ?";
        String sqlUpdate   = "UPDATE HopDong SET TrangThai = N'Hết hiệu lực' WHERE MaHopDong = ?";
        try (Connection conn = DBConnection.getConnection()) {
            String maPhong = null;
            try (PreparedStatement ps = conn.prepareStatement(sqlGetPhong)) {
                ps.setInt(1, maHopDong);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) maPhong = rs.getString("MaPhong");
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setInt(1, maHopDong);
                ps.executeUpdate();
            }
            if (maPhong != null && !coHopDongConHieuLuc(maPhong)) {
                RoomDAO.capNhatTrangThai(maPhong, "Trống");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Kiểm tra phòng còn HĐ "Còn hiệu lực"/"Chờ nhận phòng" nào nữa không. */
    public static boolean coHopDongConHieuLuc(String maPhong) {
        String sql = "SELECT 1 FROM HopDong WHERE MaPhong=? AND TrangThai IN (N'Còn hiệu lực', N'Chờ nhận phòng')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhong);
            return ps.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Gia hạn hợp đồng: cập nhật NgayKetThuc và đặt lại TrangThai = Còn hiệu lực. */
    public static void giaHanHopDong(int maHopDong, String ngayKetThucMoi) {
        String sqlGetPhong = "SELECT MaPhong FROM HopDong WHERE MaHopDong = ?";
        String sqlUpdate   = "UPDATE HopDong SET NgayKetThuc=?, TrangThai=N'Còn hiệu lực' WHERE MaHopDong=?";
        try (Connection conn = DBConnection.getConnection()) {
            String maPhong = null;
            try (PreparedStatement ps = conn.prepareStatement(sqlGetPhong)) {
                ps.setInt(1, maHopDong);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) maPhong = rs.getString(1);
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setDate(1, Date.valueOf(ngayKetThucMoi));
                ps.setInt(2, maHopDong);
                ps.executeUpdate();
            }
            if (maPhong != null) {
                RoomDAO.capNhatTrangThai(maPhong, "Đang thuê");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Cho dashboard: số HĐ sắp hết hạn trong N ngày tới. */
    public static ArrayList<Object[]> getHopDongSapHetHan(int trongVongNgay) {
        ArrayList<Object[]> ds = new ArrayList<>();
        String sql =
            "SELECT TOP 50 h.MaHopDong, p.TenPhong, kt.HoTen, " +
            "       CONVERT(VARCHAR, h.NgayKetThuc, 23) AS NgayKetThuc, " +
            "       DATEDIFF(DAY, GETDATE(), h.NgayKetThuc) AS SoNgayConLai " +
            "FROM HopDong h " +
            "JOIN Phong p ON h.MaPhong = p.MaPhong " +
            "JOIN KhachThue kt ON h.MaKhachThue = kt.MaKhachThue " +
            "WHERE h.TrangThai = N'Còn hiệu lực' " +
            "  AND h.NgayKetThuc IS NOT NULL " +
            "  AND YEAR(h.NgayKetThuc) < 3000 " +
            "  AND DATEDIFF(DAY, GETDATE(), h.NgayKetThuc) BETWEEN 0 AND ? " +
            "ORDER BY h.NgayKetThuc";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trongVongNgay);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ds.add(new Object[]{
                        rs.getInt("MaHopDong"),
                        rs.getString("TenPhong"),
                        rs.getString("HoTen"),
                        rs.getString("NgayKetThuc"),
                        rs.getInt("SoNgayConLai")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    // --- Người ở ghép ---

    public static boolean capNhatNguoiOGhep(int maHopDong, ArrayList<Integer> dsMaKhach) {
        try (Connection conn = DBConnection.getConnection()) {
            luuNguoiOGhep(conn, maHopDong, dsMaKhach);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean capNhatNguoiOGhep(int maHopDong, ArrayList<Integer> dsMaKhach,
                                              ArrayList<java.time.LocalDate> dsNgayVao) {
        try (Connection conn = DBConnection.getConnection()) {
            luuNguoiOGhepVoiNgay(conn, maHopDong, dsMaKhach, dsNgayVao);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Lấy NgayVao của từng người ở ghép đang hoạt động (MaKhachThue → NgayVao). */
    public static java.util.LinkedHashMap<Integer, java.time.LocalDate> getNguoiOGhepWithNgayVao(int maHopDong) {
        java.util.LinkedHashMap<Integer, java.time.LocalDate> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT MaKhachThue, NgayVao FROM ChiTiet_HopDong_NguoiOGhep " +
                     "WHERE MaHopDong = ? AND NgayDi IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHopDong);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Date d = rs.getDate("NgayVao");
                map.put(rs.getInt("MaKhachThue"), d != null ? d.toLocalDate() : null);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return map;
    }

    /** Cập nhật NgayVao cho 1 người ở ghép đang hoạt động. */
    public static void capNhatNgayVaoNguoiOGhep(int maHopDong, int maKhach,
                                                  java.time.LocalDate ngayVao) {
        String sql = "UPDATE ChiTiet_HopDong_NguoiOGhep SET NgayVao=? " +
                     "WHERE MaHopDong=? AND MaKhachThue=? AND NgayDi IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (ngayVao != null) ps.setDate(1, Date.valueOf(ngayVao));
            else                 ps.setNull(1, Types.DATE);
            ps.setInt(2, maHopDong);
            ps.setInt(3, maKhach);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Ghi nhận ngày rời đi của người ở ghép: UPDATE NgayDi thay vì DELETE.
     * Chỉ cập nhật bản ghi còn đang hoạt động (NgayDi IS NULL).
     * Bản ghi sau khi có NgayDi sẽ không bị xóa bởi luuNguoiOGhepVoiNgay,
     * đảm bảo lịch sử người ở ghép được bảo toàn trong CSDL.
     */
    public static void ghiNgayDiNguoiOGhep(int maHopDong, int maKhachThue,
                                             java.time.LocalDate ngayDi) {
        String sql = "UPDATE ChiTiet_HopDong_NguoiOGhep SET NgayDi=? " +
                     "WHERE MaHopDong=? AND MaKhachThue=? AND NgayDi IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (ngayDi != null) ps.setDate(1, Date.valueOf(ngayDi));
            else                ps.setDate(1, Date.valueOf(java.time.LocalDate.now()));
            ps.setInt(2, maHopDong);
            ps.setInt(3, maKhachThue);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void capNhatDichVuHopDong(int maHopDong, ArrayList<Integer> dsMaDichVu) {
        try (Connection conn = DBConnection.getConnection()) {
            xoaChiTietDichVu(conn, maHopDong);
            themChiTietDichVu(conn, maHopDong, dsMaDichVu);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static ArrayList<Integer> getNguoiOGhep(int maHopDong) {
        ArrayList<Integer> ds = new ArrayList<>();
        String sql = "SELECT MaKhachThue FROM ChiTiet_HopDong_NguoiOGhep WHERE MaHopDong = ? AND NgayDi IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHopDong);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ds.add(rs.getInt("MaKhachThue"));
        } catch (Exception e) { e.printStackTrace(); }
        return ds;
    }

    private static void luuNguoiOGhep(Connection conn, int maHopDong,
                                       ArrayList<Integer> dsMaKhach) throws SQLException {
        luuNguoiOGhepVoiNgay(conn, maHopDong, dsMaKhach, null);
    }

    public static void luuNguoiOGhepVoiNgay(Connection conn, int maHopDong,
                                              ArrayList<Integer> dsMaKhach,
                                              ArrayList<java.time.LocalDate> dsNgayVao) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM ChiTiet_HopDong_NguoiOGhep WHERE MaHopDong = ? AND NgayDi IS NULL")) {
            ps.setInt(1, maHopDong);
            ps.executeUpdate();
        }
        if (dsMaKhach == null || dsMaKhach.isEmpty()) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO ChiTiet_HopDong_NguoiOGhep (MaHopDong, MaKhachThue, NgayVao) VALUES (?,?,?)")) {
            for (int i = 0; i < dsMaKhach.size(); i++) {
                ps.setInt(1, maHopDong);
                ps.setInt(2, dsMaKhach.get(i));
                java.time.LocalDate ngayVao = (dsNgayVao != null && i < dsNgayVao.size())
                        ? dsNgayVao.get(i) : null;
                if (ngayVao != null) ps.setDate(3, Date.valueOf(ngayVao));
                else                 ps.setNull(3, Types.DATE);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // --- Helpers ---

    private static void themChiTietDichVu(Connection conn, int maHopDong,
                                           ArrayList<Integer> danhSachMaDichVu) throws SQLException {
        if (danhSachMaDichVu == null || danhSachMaDichVu.isEmpty()) return;
        String sql = "INSERT INTO ChiTiet_HopDong_DichVu (MaHopDong, MaDichVu) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int maDichVu : danhSachMaDichVu) {
                ps.setInt(1, maHopDong);
                ps.setInt(2, maDichVu);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void xoaChiTietDichVu(Connection conn, int maHopDong) throws SQLException {
        String sql = "DELETE FROM ChiTiet_HopDong_DichVu WHERE MaHopDong = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHopDong);
            ps.executeUpdate();
        }
    }

    private static void setDateOrNull(PreparedStatement ps, int index, String dateStr) throws SQLException {
        if (dateStr == null || dateStr.trim().isEmpty()) ps.setNull(index, Types.DATE);
        else ps.setDate(index, Date.valueOf(dateStr));
    }

    private static String nullIfEmpty(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s;
    }
}
