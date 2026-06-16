package QuanLiNhaTro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class RoomDAO {

    public static ArrayList<ThongTinPhong> getThongTinPhong() {
        ArrayList<ThongTinPhong> ds = new ArrayList<>();
        String sql = "SELECT * FROM Phong ORDER BY tenPhong";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ds.add(new ThongTinPhong(
                        rs.getString("maPhong"),
                        rs.getString("tenPhong"),
                        rs.getString("dienTich"),
                        rs.getString("giaThue"),
                        rs.getString("trangThai")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * Đồng bộ trangThai của tất cả phòng dựa trên hợp đồng thực tế:
     * có HĐ còn hiệu lực → "Đang thuê", không có → "Trống".
     */
    public static void dongBoTrangThaiPhong() {
        String sql =
            "UPDATE Phong SET trangThai = " +
            "  CASE WHEN EXISTS (" +
            "    SELECT 1 FROM HopDong h " +
            "    WHERE h.MaPhong = Phong.MaPhong " +
            "      AND h.TrangThai IN (N'Còn hiệu lực', N'Chờ nhận phòng')" +
            "  ) THEN N'Đang thuê' ELSE N'Trống' END";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void themPhong(String tenPhong, String dienTich, String giaThue, String trangThai) {
        String sql = "INSERT INTO Phong (tenPhong, dienTich, giaThue, trangThai) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tenPhong);
            ps.setString(2, dienTich);
            ps.setString(3, giaThue);
            ps.setString(4, (trangThai == null || trangThai.isEmpty()) ? "Trống" : trangThai);
            ps.executeUpdate();

            // Tự khởi tạo ChiSo_DienNuoc cho tháng hiện tại nếu đã có dữ liệu
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int maPhongMoi = keys.getInt(1);
                ChiSoDAO.themPhongVaoThangHienTai(maPhongMoi);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void suaPhong(String maPhong, String tenPhong, String dienTich, String giaThue, String trangThai) {
        String sql = "UPDATE Phong SET tenPhong=?, dienTich=?, giaThue=?, trangThai=? WHERE maPhong=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenPhong);
            ps.setString(2, dienTich);
            ps.setString(3, giaThue);
            ps.setString(4, (trangThai == null || trangThai.isEmpty()) ? "Trống" : trangThai);
            ps.setString(5, maPhong);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean xoaPhong(String maPhong) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (String sql : new String[]{
                    "DELETE FROM HoaDon                      WHERE MaHopDong IN (SELECT MaHopDong FROM HopDong WHERE MaPhong = ?)",
                    "DELETE FROM ChiTiet_HopDong_NguoiOGhep  WHERE MaHopDong IN (SELECT MaHopDong FROM HopDong WHERE MaPhong = ?)",
                    "DELETE FROM ChiTiet_HopDong_DichVu       WHERE MaHopDong IN (SELECT MaHopDong FROM HopDong WHERE MaPhong = ?)",
                    "DELETE FROM ChiSo_DienNuoc               WHERE MaPhong = ?",
                    "DELETE FROM HopDong                      WHERE MaPhong = ?",
                    "DELETE FROM Phong                        WHERE maPhong = ?"
                }) {
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, maPhong);
                        ps.executeUpdate();
                    }
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

    public static boolean kiemTraTenPhongTonTai(String tenPhong, String maPhongExclude) {
        String sql = maPhongExclude == null
            ? "SELECT 1 FROM Phong WHERE tenPhong = ?"
            : "SELECT 1 FROM Phong WHERE tenPhong = ? AND maPhong <> ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenPhong);
            if (maPhongExclude != null) ps.setString(2, maPhongExclude);
            return ps.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Cập nhật trạng thái phòng — gọi tự động khi tạo/thanh lý hợp đồng. */
    public static void capNhatTrangThai(String maPhong, String trangThai) {
        String sql = "UPDATE Phong SET trangThai=? WHERE maPhong=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setString(2, maPhong);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
