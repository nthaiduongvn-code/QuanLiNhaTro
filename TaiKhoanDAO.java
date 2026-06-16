package QuanLiNhaTro;

import java.sql.*;

public class TaiKhoanDAO {

    // ── Đăng nhập (so sánh plain text) ───────────────────────────

    public static TaiKhoan login(String tenDangNhap, String matKhau) {
        String sql = "SELECT TenDangNhap FROM TaiKhoan WHERE TenDangNhap = ? AND MatKhau = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            ps.setString(2, matKhau);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                TaiKhoan tk = new TaiKhoan();
                tk.setTenDangNhap(rs.getString("TenDangNhap"));
                return tk;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ── Kiểm tra tên đăng nhập có tồn tại không ─────────────────

    public static boolean isUsernameExists(String tenDangNhap) {
        String sql = "SELECT 1 FROM TaiKhoan WHERE TenDangNhap = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    // ── Cập nhật mật khẩu mới (plain text) ───────────────────────

    public static boolean updatePassword(String tenDangNhap, String newMatKhau) {
        String sql = "UPDATE TaiKhoan SET MatKhau = ? WHERE TenDangNhap = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newMatKhau);
            ps.setString(2, tenDangNhap);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
