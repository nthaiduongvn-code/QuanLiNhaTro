package QuanLiNhaTro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class DichVuDAO {
    public static ArrayList<ThongTinDichVu> getThongTinDichVu() {
        ArrayList<ThongTinDichVu> ds = new ArrayList<>();
        String sql = "SELECT * FROM DichVu ORDER BY MaDichVu";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ds.add(new ThongTinDichVu(
                        rs.getString("MaDichVu"),
                        rs.getString("TenDichVu"),
                        rs.getInt("GiaDichVu"),
                        rs.getString("DonViTinh"),
                        rs.getString("GhiChu"),
                        rs.getString("CachTinh"),
                        rs.getBoolean("LaDichVuMacDinh")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    public static ArrayList<ThongTinDichVu> getDichVuByHopDong(int maHopDong) {
        ArrayList<ThongTinDichVu> ds = new ArrayList<>();
        String sql =
            "SELECT dv.MaDichVu, dv.TenDichVu, dv.GiaDichVu, dv.DonViTinh, dv.GhiChu, dv.CachTinh, dv.LaDichVuMacDinh " +
            "FROM ChiTiet_HopDong_DichVu ctdv " +
            "JOIN DichVu dv ON ctdv.MaDichVu = dv.MaDichVu " +
            "WHERE ctdv.MaHopDong = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHopDong);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ds.add(new ThongTinDichVu(
                        rs.getString("MaDichVu"),
                        rs.getString("TenDichVu"),
                        rs.getInt("GiaDichVu"),
                        rs.getString("DonViTinh"),
                        rs.getString("GhiChu"),
                        rs.getString("CachTinh"),
                        rs.getBoolean("LaDichVuMacDinh")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * Đếm số hợp đồng đang hoạt động còn đang dùng dịch vụ này.
     * Dùng để hiện cảnh báo trước khi xóa.
     */
    public static int demHopDongHienTaiDangDung(String maDichVu) {
        String sql =
            "SELECT COUNT(*) FROM ChiTiet_HopDong_DichVu ct " +
            "JOIN HopDong hd ON ct.MaHopDong = hd.MaHopDong " +
            "WHERE ct.MaDichVu = ? " +
            "  AND hd.TrangThai IN (N'Còn hiệu lực', N'Chờ nhận phòng')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDichVu);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Xóa dịch vụ: cascade xóa ChiTiet_HopDong_DichVu trước rồi mới xóa DichVu. */
    public static boolean xoaDichVu(String maDichVu) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM ChiTiet_HopDong_DichVu WHERE MaDichVu = ?")) {
                    ps.setString(1, maDichVu);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM DichVu WHERE MaDichVu = ?")) {
                    ps.setString(1, maDichVu);
                    ps.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void themDichVuMoi(ThongTinDichVu dv) {
        String sql = "INSERT INTO DichVu (TenDichVu, GiaDichVu, DonViTinh, GhiChu, CachTinh) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dv.getTenDichVu());
            ps.setInt(2, dv.getGiaDichVu());
            ps.setString(3, dv.getDonVi());
            ps.setString(4, dv.getGhiChu());
            ps.setString(5, dv.getCachTinh());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Áp dụng một dịch vụ cụ thể vào tất cả hợp đồng đang còn hiệu lực.
     * Chỉ INSERT những hợp đồng chưa có dịch vụ này, tránh trùng lặp.
     * Trả về số hợp đồng được thêm mới.
     */
    public static int apDungMotDichVuVaoTatCaPhong(String maDichVu) {
        String sql =
            "INSERT INTO ChiTiet_HopDong_DichVu (MaHopDong, MaDichVu) " +
            "SELECT hd.MaHopDong, ? " +
            "FROM HopDong hd " +
            "WHERE hd.TrangThai IN (N'Còn hiệu lực', N'Chờ nhận phòng') " +
            "  AND NOT EXISTS (" +
            "      SELECT 1 FROM ChiTiet_HopDong_DichVu ct " +
            "      WHERE ct.MaHopDong = hd.MaHopDong AND ct.MaDichVu = ?" +
            "  )";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDichVu);
            ps.setString(2, maDichVu);
            return ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void suaDichVu(String maDichVu, String tenMoi, int giaMoi, String donViMoi, String ghiChuMoi, String cachTinhMoi) {
        String sql = "UPDATE DichVu SET TenDichVu=?, GiaDichVu=?, DonViTinh=?, GhiChu=?, CachTinh=? WHERE MaDichVu=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenMoi);
            ps.setInt(2, giaMoi);
            ps.setString(3, donViMoi);
            ps.setString(4, ghiChuMoi);
            ps.setString(5, cachTinhMoi);
            ps.setString(6, maDichVu);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
