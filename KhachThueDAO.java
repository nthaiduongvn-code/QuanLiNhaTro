package QuanLiNhaTro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class KhachThueDAO {

    /**
     * Khách chưa là chủ hợp đồng đang hoạt động (Còn hiệu lực / Chờ nhận phòng).
     * excludeHopDongId > 0: bỏ qua HĐ đó khi kiểm tra — dùng khi sửa HĐ hiện tại.
     */
    public static ArrayList<ThongTinKhachThue> getKhachChuaCoHopDong(int excludeHopDongId) {
        String ex = excludeHopDongId > 0 ? " AND hd.MaHopDong != ?" : "";
        String sql = "SELECT k.MaKhachThue, k.HoTen, k.SoCCCD, k.SoDienThoai, k.Email, " +
                     "k.GioiTinh, k.NgaySinh, k.QueQuan, k.BienSoXe, k.GhiChu " +
                     "FROM KhachThue k " +
                     "WHERE NOT EXISTS (" +
                     "  SELECT 1 FROM HopDong hd " +
                     "  WHERE hd.MaKhachThue = k.MaKhachThue " +
                     "  AND hd.TrangThai IN (N'Còn hiệu lực', N'Chờ nhận phòng')" + ex + ") " +
                     "AND NOT EXISTS (" +
                     "  SELECT 1 FROM ChiTiet_HopDong_NguoiOGhep nog " +
                     "  JOIN HopDong hd ON nog.MaHopDong = hd.MaHopDong " +
                     "  WHERE nog.MaKhachThue = k.MaKhachThue " +
                     "  AND nog.NgayDi IS NULL " +
                     "  AND hd.TrangThai IN (N'Còn hiệu lực', N'Chờ nhận phòng')" + ex + ") " +
                     "ORDER BY k.HoTen";
        ArrayList<ThongTinKhachThue> ds = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (excludeHopDongId > 0) { ps.setInt(1, excludeHopDongId); ps.setInt(2, excludeHopDongId); }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ds.add(new ThongTinKhachThue(
                        rs.getInt("MaKhachThue"), rs.getString("HoTen"), rs.getString("SoCCCD"),
                        rs.getString("SoDienThoai"), rs.getString("Email"), rs.getString("GioiTinh"),
                        rs.getString("NgaySinh"), rs.getString("QueQuan"),
                        rs.getString("BienSoXe"), rs.getString("GhiChu"), false));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ds;
    }

    public static ArrayList<ThongTinKhachThue> getFullKhachThue() {
        String sql = "SELECT k.MaKhachThue, k.HoTen, k.SoCCCD, k.SoDienThoai, k.Email, " +
                     "k.GioiTinh, k.NgaySinh, k.QueQuan, k.BienSoXe, k.GhiChu, " +
                     "CASE WHEN EXISTS (" +
                     "  SELECT 1 FROM HopDong hd " +
                     "  WHERE hd.MaKhachThue = k.MaKhachThue " +
                     "  AND hd.TrangThai IN (N'Còn hiệu lực', N'Chờ nhận phòng')" +
                     ") OR EXISTS (" +
                     "  SELECT 1 FROM ChiTiet_HopDong_NguoiOGhep nog " +
                     "  JOIN HopDong hd ON nog.MaHopDong = hd.MaHopDong " +
                     "  WHERE nog.MaKhachThue = k.MaKhachThue AND nog.NgayDi IS NULL " +
                     "  AND hd.TrangThai IN (N'Còn hiệu lực', N'Chờ nhận phòng')" +
                     ") THEN 1 ELSE 0 END AS DangO " +
                     "FROM KhachThue k ORDER BY k.MaKhachThue";
        ArrayList<ThongTinKhachThue> ds = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new ThongTinKhachThue(
                        rs.getInt("MaKhachThue"),
                        rs.getString("HoTen"),
                        rs.getString("SoCCCD"),
                        rs.getString("SoDienThoai"),
                        rs.getString("Email"),
                        rs.getString("GioiTinh"),
                        rs.getString("NgaySinh"),
                        rs.getString("QueQuan"),
                        rs.getString("BienSoXe"),
                        rs.getString("GhiChu"),
                        rs.getInt("DangO") == 1
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    public static void themKhachThueMoi(ThongTinKhachThue kt) {
        String sql = "INSERT INTO KhachThue (HoTen, SoCCCD, SoDienThoai, Email, GioiTinh, NgaySinh, QueQuan, BienSoXe, GhiChu) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kt.getHoTen());
            ps.setString(2, kt.getSoCCCD());
            ps.setString(3, kt.getSoDienThoai());
            ps.setString(4, nullIfEmpty(kt.getEmail()));
            ps.setString(5, nullIfEmpty(kt.getGioiTinh()));
            String ngaySinh = kt.getNgaySinh();
            if (ngaySinh == null || ngaySinh.isEmpty()) ps.setNull(6, java.sql.Types.DATE);
            else ps.setDate(6, java.sql.Date.valueOf(ngaySinh));
            ps.setString(7, nullIfEmpty(kt.getQueQuan()));
            ps.setString(8, nullIfEmpty(kt.getBienSoXe()));
            ps.setString(9, nullIfEmpty(kt.getGhiChu()));
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void xoaKhachThue(int maKhachThue) throws Exception {
        String sql = "DELETE FROM KhachThue WHERE MaKhachThue = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maKhachThue);
            ps.executeUpdate();
        }
    }

    public static void suaKhachThue(int maKhachThue, String hoTen, String soCCCD, String soDienThoai,
                                     String email, String gioiTinh, String ngaySinh, String queQuan,
                                     String bienSoXe, String ghiChu) {
        String sql = "UPDATE KhachThue SET HoTen=?, SoCCCD=?, SoDienThoai=?, Email=?, GioiTinh=?, NgaySinh=?, QueQuan=?, BienSoXe=?, GhiChu=? WHERE MaKhachThue=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hoTen);
            ps.setString(2, soCCCD);
            ps.setString(3, soDienThoai);
            ps.setString(4, nullIfEmpty(email));
            ps.setString(5, nullIfEmpty(gioiTinh));
            if (ngaySinh == null || ngaySinh.isEmpty()) ps.setNull(6, java.sql.Types.DATE);
            else ps.setDate(6, java.sql.Date.valueOf(ngaySinh));
            ps.setString(7, nullIfEmpty(queQuan));
            ps.setString(8, nullIfEmpty(bienSoXe));
            ps.setString(9, nullIfEmpty(ghiChu));
            ps.setInt(10, maKhachThue);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean kiemTraTonTaiCCCD(String soCCCD) {
        String sql = "SELECT 1 FROM KhachThue WHERE SoCCCD = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soCCCD);
            return ps.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean kiemTraTonTaiCCCDKhiSua(String soCCCD, int maKhachThue) {
        String sql = "SELECT 1 FROM KhachThue WHERE SoCCCD = ? AND MaKhachThue != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soCCCD);
            ps.setInt(2, maKhachThue);
            return ps.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Khách thuê có HopDong còn hiệu lực không. Để chặn xóa. */
    public static boolean coHopDongConHieuLuc(int maKhachThue) {
        String sql = "SELECT 1 FROM HopDong WHERE MaKhachThue=? AND TrangThai IN (N'Còn hiệu lực', N'Chờ nhận phòng')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maKhachThue);
            return ps.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Kiểm tra toàn diện: người đang ở hiện tại (chủ HĐ HOẶC ở ghép),
     * bao gồm cả trạng thái "Chờ nhận phòng".
     * Dùng để chặn xóa — đây là fix bug so với coHopDongConHieuLuc().
     */
    public static boolean dangOHienTai(int maKhachThue) {
        String sql =
            "SELECT 1 FROM HopDong " +
            "WHERE MaKhachThue=? AND TrangThai IN (N'Còn hiệu lực', N'Chờ nhận phòng') " +
            "UNION ALL " +
            "SELECT 1 FROM ChiTiet_HopDong_NguoiOGhep nog " +
            "JOIN HopDong hd ON nog.MaHopDong = hd.MaHopDong " +
            "WHERE nog.MaKhachThue=? AND nog.NgayDi IS NULL " +
            "AND hd.TrangThai IN (N'Còn hiệu lực', N'Chờ nhận phòng')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maKhachThue);
            ps.setInt(2, maKhachThue);
            return ps.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Khách trang chính: đang thuê / ở ghép (còn HĐ hiệu lực)
     * HOẶC mới thêm chưa có bất kỳ liên kết HĐ nào → hiển thị "Chờ phòng".
     * Người từng thuê/ở ghép đã hết hạn → thuộc lịch sử, không hiện ở đây.
     */
    public static ArrayList<ThongTinKhachThue> getKhachThueHienTai() {
        String activeCond =
            "EXISTS (" +
            "  SELECT 1 FROM HopDong hd WHERE hd.MaKhachThue = k.MaKhachThue " +
            "  AND hd.TrangThai IN (N'Còn hiệu lực', N'Chờ nhận phòng')" +
            ") OR EXISTS (" +
            "  SELECT 1 FROM ChiTiet_HopDong_NguoiOGhep nog " +
            "  JOIN HopDong hd ON nog.MaHopDong = hd.MaHopDong " +
            "  WHERE nog.MaKhachThue = k.MaKhachThue AND nog.NgayDi IS NULL " +
            "  AND hd.TrangThai IN (N'Còn hiệu lực', N'Chờ nhận phòng')" +
            ")";
        // Khách mới: chưa có HĐ riêng VÀ chưa từng là người ở ghép bao giờ
        String newCond =
            "NOT EXISTS (SELECT 1 FROM HopDong WHERE MaKhachThue = k.MaKhachThue) " +
            "AND NOT EXISTS (SELECT 1 FROM ChiTiet_HopDong_NguoiOGhep WHERE MaKhachThue = k.MaKhachThue)";
        String sql =
            "SELECT k.MaKhachThue, k.HoTen, k.SoCCCD, k.SoDienThoai, k.Email, " +
            "k.GioiTinh, k.NgaySinh, k.QueQuan, k.BienSoXe, k.GhiChu, " +
            "CASE WHEN " + activeCond + " THEN 1 ELSE 0 END AS DangO " +
            "FROM KhachThue k " +
            "WHERE (" + activeCond + ") OR (" + newCond + ") " +
            "ORDER BY k.HoTen";
        ArrayList<ThongTinKhachThue> ds = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new ThongTinKhachThue(
                    rs.getInt("MaKhachThue"), rs.getString("HoTen"), rs.getString("SoCCCD"),
                    rs.getString("SoDienThoai"), rs.getString("Email"), rs.getString("GioiTinh"),
                    rs.getString("NgaySinh"), rs.getString("QueQuan"),
                    rs.getString("BienSoXe"), rs.getString("GhiChu"),
                    rs.getInt("DangO") == 1));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ds;
    }

    /**
     * Khách đã di dời: không còn HĐ/ở ghép nào hiệu lực,
     * nhưng đã từng có HĐ riêng HOẶC đã từng là người ở ghép.
     */
    public static ArrayList<ThongTinKhachThue> getKhachThueDaDiDoi() {
        String sql =
            "SELECT k.MaKhachThue, k.HoTen, k.SoCCCD, k.SoDienThoai, k.Email, " +
            "k.GioiTinh, k.NgaySinh, k.QueQuan, k.BienSoXe, k.GhiChu " +
            "FROM KhachThue k " +
            "WHERE NOT EXISTS (" +
            "  SELECT 1 FROM HopDong hd WHERE hd.MaKhachThue = k.MaKhachThue " +
            "  AND hd.TrangThai IN (N'Còn hiệu lực', N'Chờ nhận phòng')" +
            ") AND NOT EXISTS (" +
            "  SELECT 1 FROM ChiTiet_HopDong_NguoiOGhep nog " +
            "  JOIN HopDong hd ON nog.MaHopDong = hd.MaHopDong " +
            "  WHERE nog.MaKhachThue = k.MaKhachThue AND nog.NgayDi IS NULL " +
            "  AND hd.TrangThai IN (N'Còn hiệu lực', N'Chờ nhận phòng')" +
            ") AND (" +
            "  EXISTS (SELECT 1 FROM HopDong WHERE MaKhachThue = k.MaKhachThue)" +
            "  OR EXISTS (SELECT 1 FROM ChiTiet_HopDong_NguoiOGhep WHERE MaKhachThue = k.MaKhachThue)" +
            ") ORDER BY k.HoTen";
        ArrayList<ThongTinKhachThue> ds = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new ThongTinKhachThue(
                    rs.getInt("MaKhachThue"), rs.getString("HoTen"), rs.getString("SoCCCD"),
                    rs.getString("SoDienThoai"), rs.getString("Email"), rs.getString("GioiTinh"),
                    rs.getString("NgaySinh"), rs.getString("QueQuan"),
                    rs.getString("BienSoXe"), rs.getString("GhiChu"), false));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ds;
    }

    /** Đếm tổng số hợp đồng của khách (kể cả đã hết hiệu lực). */
    public static int demHopDong(int maKhachThue) {
        String sql = "SELECT COUNT(*) FROM HopDong WHERE MaKhachThue = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maKhachThue);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Xóa toàn bộ hợp đồng (đã hết hiệu lực) của khách, sau đó xóa khách. */
    public static void xoaKhachThueVaHopDong(int maKhachThue) throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String subHD = "(SELECT MaHopDong FROM HopDong WHERE MaKhachThue = ?)";
                for (String sql : new String[]{
                    "DELETE FROM HoaDon                      WHERE MaHopDong IN " + subHD,
                    "DELETE FROM ChiTiet_HopDong_NguoiOGhep  WHERE MaHopDong IN " + subHD,
                    "DELETE FROM ChiTiet_HopDong_DichVu       WHERE MaHopDong IN " + subHD,
                    "DELETE FROM HopDong                      WHERE MaKhachThue = ?",
                    "DELETE FROM KhachThue                    WHERE MaKhachThue = ?"
                }) {
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, maKhachThue);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public static ThongTinKhachThue getKhachThueById(int maKhachThue) {
        String sql = "SELECT MaKhachThue, HoTen, SoCCCD, SoDienThoai, Email, " +
                     "GioiTinh, NgaySinh, QueQuan, BienSoXe, GhiChu FROM KhachThue WHERE MaKhachThue = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maKhachThue);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new ThongTinKhachThue(
                        rs.getInt("MaKhachThue"), rs.getString("HoTen"), rs.getString("SoCCCD"),
                        rs.getString("SoDienThoai"), rs.getString("Email"), rs.getString("GioiTinh"),
                        rs.getString("NgaySinh"), rs.getString("QueQuan"),
                        rs.getString("BienSoXe"), rs.getString("GhiChu"), false);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    private static String nullIfEmpty(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }
}
