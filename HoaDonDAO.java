package QuanLiNhaTro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class HoaDonDAO {

    public static ArrayList<Object[]> getHoaDonByPhong(int maHopDong) {
        ArrayList<Object[]> ds = new ArrayList<>();
        String sql =
            "SELECT hd.MaHoaDon, hd.ThangNam, kt.HoTen, hd.TongTien, hd.TrangThai " +
            "FROM HoaDon hd " +
            "JOIN HopDong hp ON hd.MaHopDong = hp.MaHopDong " +
            "JOIN KhachThue kt ON hp.MaKhachThue = kt.MaKhachThue " +
            "WHERE hd.MaHopDong = ? " +
            "ORDER BY hd.MaHoaDon DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHopDong);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String maHoaDon = "HD-" + String.format("%04d", rs.getInt("MaHoaDon"));
                String thangNam = rs.getString("ThangNam");
                String hoTen    = rs.getString("HoTen");
                String tongTien = String.format("%,d đ", rs.getLong("TongTien"));
                String trangThai = formatTrangThai(rs.getString("TrangThai"));
                ds.add(new Object[]{maHoaDon, thangNam, hoTen, tongTien, trangThai});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    private static String formatTrangThai(String tt) {
        if (tt == null) return "";
        if (tt.contains("Đã"))    return Theme.pillSuccess("Đã thanh toán");
        return Theme.pillDanger("Chưa thanh toán");
    }
}
