package QuanLiNhaTro;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class ChiSoDAO {

    public static ArrayList<Object[]> getDataThangNam(String thangNam) {
        // Chỉ tự tạo record cho kỳ này nếu kỳ đã tồn tại dữ liệu, hoặc đây là lần đầu tiên
        // chưa có kỳ nào trong hệ thống. Tránh việc duyệt combobox sang tháng tương lai
        // vô tình "khởi tạo ngầm" kỳ đó với chỉ số cũ = 0, bỏ qua luồng "Khởi tạo tháng mới".
        if (kiemTraTonTai(thangNam) || layThangNamMoiNhat() == null) {
            ensureActiveRoomsInMonth(thangNam);
        }

        int month = Integer.parseInt(thangNam.substring(0, 2));
        int year  = Integer.parseInt(thangNam.substring(3));
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay  = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        // Chỉ hiển thị phòng có hợp đồng thực sự phủ tháng này
        // (lọc ra các entry sai do tháng bắt đầu HĐ nằm ở tháng khác)
        String sql =
            "SELECT c.MaChiSo, c.MaPhong, p.TenPhong, c.ThangNam, " +
            "       c.DienCu, c.DienMoi, c.DienDaDung, " +
            "       c.NuocCu, c.NuocMoi, c.NuocDaDung " +
            "FROM ChiSo_DienNuoc c " +
            "JOIN Phong p ON c.MaPhong = p.MaPhong " +
            "WHERE c.ThangNam = ? " +
            "  AND EXISTS (" +
            "      SELECT 1 FROM HopDong hd " +
            "      WHERE hd.MaPhong = c.MaPhong " +
            "        AND hd.NgayBatDau <= ? " +
            "        AND (hd.NgayKetThuc IS NULL OR YEAR(hd.NgayKetThuc) >= 3000 " +
            "             OR hd.NgayKetThuc >= ?)" +
            "  ) " +
            "ORDER BY p.TenPhong";

        ArrayList<Object[]> ds = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, thangNam);
            ps.setDate  (2, java.sql.Date.valueOf(lastDay));
            ps.setDate  (3, java.sql.Date.valueOf(firstDay));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int dienMoiVal = rs.getInt("DienMoi");
                Object dienMoi = rs.wasNull() ? "" : dienMoiVal;
                int dienDaDungVal = rs.getInt("DienDaDung");
                Object dienDaDung = rs.wasNull() ? "" : dienDaDungVal;
                int nuocMoiVal = rs.getInt("NuocMoi");
                Object nuocMoi = rs.wasNull() ? "" : nuocMoiVal;
                int nuocDaDungVal = rs.getInt("NuocDaDung");
                Object nuocDaDung = rs.wasNull() ? "" : nuocDaDungVal;

                ds.add(new Object[]{
                        rs.getInt("MaChiSo"),
                        rs.getString("MaPhong"),
                        rs.getString("TenPhong"),
                        rs.getString("ThangNam"),
                        rs.getInt("DienCu"), dienMoi, dienDaDung,
                        rs.getInt("NuocCu"), nuocMoi, nuocDaDung
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    public static ArrayList<Integer> layDanhSachNam() {
        String sql = "SELECT DISTINCT CAST(RIGHT(ThangNam,4) AS INT) AS Nam " +
                     "FROM ChiSo_DienNuoc ORDER BY Nam";
        ArrayList<Integer> ds = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) ds.add(rs.getInt("Nam"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ds.isEmpty()) ds.add(LocalDate.now().getYear());
        return ds;
    }

    public static String layThangNamMoiNhat() {
        String sql =
            "SELECT TOP 1 ThangNam FROM ChiSo_DienNuoc " +
            "ORDER BY CAST(RIGHT(ThangNam,4) AS INT) DESC, " +
            "         CAST(LEFT(ThangNam,2) AS INT) DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getString("ThangNam");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String tinhThangNamTiepTheo() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql1 = "SELECT MAX(CAST(RIGHT(ThangNam,4) AS INT)) AS MaxNam FROM ChiSo_DienNuoc";
            ResultSet rs1 = conn.createStatement().executeQuery(sql1);
            if (!rs1.next()) return thangNamHienTai();
            int maxNam = rs1.getInt("MaxNam");
            if (rs1.wasNull()) return thangNamHienTai();

            String sql2 =
                "SELECT MAX(CAST(LEFT(ThangNam,2) AS INT)) AS MaxThang " +
                "FROM ChiSo_DienNuoc WHERE RIGHT(ThangNam,4) = ?";
            PreparedStatement ps2 = conn.prepareStatement(sql2);
            ps2.setString(1, String.valueOf(maxNam));
            ResultSet rs2 = ps2.executeQuery();
            if (!rs2.next()) return thangNamHienTai();
            int maxThang = rs2.getInt("MaxThang");

            if (maxThang == 12) return String.format("01-%04d", maxNam + 1);
            else                return String.format("%02d-%04d", maxThang + 1, maxNam);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return thangNamHienTai();
    }

    public static boolean coPhongChuaGhiSo(String thangNam) {
        // Chỉ kiểm tra phòng đang hoạt động (có hợp đồng còn hiệu lực)
        String sql =
            "SELECT 1 FROM HopDong hd " +
            "LEFT JOIN ChiSo_DienNuoc cs ON hd.MaPhong = cs.MaPhong AND cs.ThangNam = ? " +
            "WHERE hd.TrangThai = N'Còn hiệu lực' " +
            "  AND (cs.DienMoi IS NULL OR cs.NuocMoi IS NULL)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, thangNam);
            return ps.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Kiểm tra kỳ đã được lập hoá đơn hay chưa (dùng để cảnh báo khi sửa chỉ số kỳ cũ). */
    public static boolean coHoaDonChoKy(String thangNam) {
        String sql = "SELECT 1 FROM HoaDon WHERE ThangNam = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, thangNam);
            return ps.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean kiemTraTonTai(String thangNam) {
        String sql = "SELECT 1 FROM ChiSo_DienNuoc WHERE ThangNam = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, thangNam);
            return ps.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Khởi tạo tháng mới (thay thế Sp_KhoiTaoThangMoi).
     * Validation chỉ kiểm tra phòng có HĐ bắt đầu <= cuối ThangCu —
     * tránh block khi có HĐ ký trước cho tháng sau nhưng chưa có chỉ số tháng hiện tại.
     */
    public static void khoiTaoThangMoi(String thangCu, String thangMoi) throws Exception {
        int monthCu = Integer.parseInt(thangCu.substring(0, 2));
        int yearCu  = Integer.parseInt(thangCu.substring(3));
        LocalDate firstDayCu = LocalDate.of(yearCu, monthCu, 1);
        LocalDate lastDayCu  = firstDayCu.withDayOfMonth(firstDayCu.lengthOfMonth());

        int monthMoi = Integer.parseInt(thangMoi.substring(0, 2));
        int yearMoi  = Integer.parseInt(thangMoi.substring(3));
        LocalDate firstDayMoi = LocalDate.of(yearMoi, monthMoi, 1);
        LocalDate lastDayMoi  = firstDayMoi.withDayOfMonth(firstDayMoi.lengthOfMonth());

        try (Connection conn = DBConnection.getConnection()) {
            // 1. Kiểm tra còn phòng carry-over (HĐ bắt đầu trong/trước ThangCu
            //    và vẫn hoạt động sang ThangMoi) chưa có bản ghi ThangMoi không.
            //    Nếu không còn AND ThangMoi đã có dữ liệu → đã khởi tạo đầy đủ.
            //    (Không dùng "SELECT 1 WHERE ThangNam=ThangMoi" vì HĐ ký trước
            //     cho tháng sau đã tạo sẵn entry, không phải "đã khởi tạo" thực sự.)
            String sqlPending =
                "SELECT COUNT(DISTINCT hd.MaPhong) AS cnt " +
                "FROM HopDong hd " +
                "WHERE hd.TrangThai IN (N'Còn hiệu lực', N'Chờ nhận phòng') " +
                "  AND hd.NgayBatDau <= ? " +
                "  AND (hd.NgayKetThuc IS NULL OR YEAR(hd.NgayKetThuc) >= 3000 " +
                "       OR hd.NgayKetThuc >= ?) " +
                "  AND NOT EXISTS (" +
                "      SELECT 1 FROM ChiSo_DienNuoc c " +
                "      WHERE c.MaPhong = hd.MaPhong AND c.ThangNam = ?" +
                "  )";
            try (PreparedStatement ps = conn.prepareStatement(sqlPending)) {
                ps.setDate  (1, java.sql.Date.valueOf(lastDayCu));
                ps.setDate  (2, java.sql.Date.valueOf(firstDayMoi));
                ps.setString(3, thangMoi);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt("cnt") == 0) {
                    try (PreparedStatement ps2 = conn.prepareStatement(
                            "SELECT 1 FROM ChiSo_DienNuoc WHERE ThangNam = ?")) {
                        ps2.setString(1, thangMoi);
                        if (ps2.executeQuery().next())
                            throw new Exception("Kỳ " + thangMoi + " đã được khởi tạo đầy đủ rồi.");
                    }
                }
            }

            // 2. Kiểm tra phòng hoạt động TRONG ThangCu chưa ghi số mới
            //    (bỏ qua HĐ bắt đầu sau cuối ThangCu — tức HĐ ký trước cho tháng sau)
            String sqlCheck =
                "SELECT DISTINCT p.TenPhong " +
                "FROM HopDong hd " +
                "JOIN Phong p ON hd.MaPhong = p.MaPhong " +
                "LEFT JOIN ChiSo_DienNuoc cs ON cs.MaPhong = hd.MaPhong AND cs.ThangNam = ? " +
                "WHERE hd.TrangThai IN (N'Còn hiệu lực', N'Chờ nhận phòng') " +
                "  AND hd.NgayBatDau <= ? " +
                "  AND (hd.NgayKetThuc IS NULL OR YEAR(hd.NgayKetThuc) >= 3000 " +
                "       OR hd.NgayKetThuc >= ?) " +
                "  AND (cs.DienMoi IS NULL OR cs.NuocMoi IS NULL)";
            try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
                ps.setString(1, thangCu);
                ps.setDate  (2, java.sql.Date.valueOf(lastDayCu));
                ps.setDate  (3, java.sql.Date.valueOf(firstDayCu));
                ResultSet rs = ps.executeQuery();
                java.util.List<String> missing = new java.util.ArrayList<>();
                while (rs.next() && missing.size() < 5) missing.add(rs.getString("TenPhong"));
                if (!missing.isEmpty())
                    throw new Exception("Các phòng sau chưa có số mới kỳ " + thangCu + ":\n"
                            + String.join(", ", missing)
                            + "\nVui lòng nhập đủ trước khi khởi tạo tháng mới.");
            }

            // 3. INSERT bản ghi ThangMoi: DienCu/NuocCu = DienMoi/NuocMoi của ThangCu
            //    Phòng HĐ bắt đầu trong ThangMoi: DienCu = 0 (hoặc đã tạo sẵn ở themPhongVaoThangHienTaiVoiChiSo)
            String sqlInsert =
                "INSERT INTO ChiSo_DienNuoc (MaPhong, ThangNam, DienCu, NuocCu) " +
                "SELECT rooms.MaPhong, ?, " +
                "       ISNULL(cs_cu.DienMoi, 0), ISNULL(cs_cu.NuocMoi, 0) " +
                "FROM (" +
                "    SELECT DISTINCT hd.MaPhong " +
                "    FROM HopDong hd " +
                "    WHERE hd.TrangThai IN (N'Còn hiệu lực', N'Chờ nhận phòng') " +
                "      AND hd.NgayBatDau <= ? " +
                "      AND (hd.NgayKetThuc IS NULL OR YEAR(hd.NgayKetThuc) >= 3000 " +
                "           OR hd.NgayKetThuc >= ?) " +
                "      AND NOT EXISTS (" +
                "          SELECT 1 FROM ChiSo_DienNuoc c " +
                "          WHERE c.MaPhong = hd.MaPhong AND c.ThangNam = ?" +
                "      )" +
                ") rooms " +
                "LEFT JOIN ChiSo_DienNuoc cs_cu " +
                "    ON cs_cu.MaPhong = rooms.MaPhong AND cs_cu.ThangNam = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                ps.setString(1, thangMoi);
                ps.setDate  (2, java.sql.Date.valueOf(lastDayMoi));
                ps.setDate  (3, java.sql.Date.valueOf(firstDayMoi));
                ps.setString(4, thangMoi);
                ps.setString(5, thangCu);
                ps.executeUpdate();
            }
        }
    }

    /** Tính chuỗi tháng kế tiếp từ định dạng "MM-YYYY". */
    public static String tinhThangTiepTheo(String thangNam) {
        int thang = Integer.parseInt(thangNam.substring(0, 2));
        int nam   = Integer.parseInt(thangNam.substring(3));
        if (thang == 12) return String.format("01-%04d", nam + 1);
        return String.format("%02d-%04d", thang + 1, nam);
    }

    /**
     * Cập nhật chỉ số điện-nước.
     * DienDaDung / NuocDaDung là computed columns trong schema mới — không cần ghi.
     * Sau khi lưu, tự động cập nhật DienCu/NuocCu của tháng kế tiếp nếu đã tồn tại (data propagation).
     */
    public static void capNhatChiSo(int maChiSo, Integer dienCu, Integer dienMoi,
                                     Integer nuocCu, Integer nuocMoi) {
        String sql = "UPDATE ChiSo_DienNuoc SET DienCu=?, DienMoi=?, NuocCu=?, NuocMoi=? WHERE MaChiSo=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (dienCu == null)  ps.setNull(1, Types.INTEGER); else ps.setInt(1, dienCu);
            if (dienMoi == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, dienMoi);
            if (nuocCu == null)  ps.setNull(3, Types.INTEGER); else ps.setInt(3, nuocCu);
            if (nuocMoi == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, nuocMoi);
            ps.setInt(5, maChiSo);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        // Propagate: cập nhật bắc cầu số cũ của tháng kế tiếp
        if (dienMoi != null || nuocMoi != null) {
            propagateToNextMonth(maChiSo, dienMoi, nuocMoi);
        }
    }

    private static void propagateToNextMonth(int maChiSo, Integer newDienMoi, Integer newNuocMoi) {
        String sqlGetInfo = "SELECT MaPhong, ThangNam FROM ChiSo_DienNuoc WHERE MaChiSo = ?";
        try (Connection conn = DBConnection.getConnection()) {
            int maPhong; String thangNam;
            try (PreparedStatement ps = conn.prepareStatement(sqlGetInfo)) {
                ps.setInt(1, maChiSo);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) return;
                maPhong  = rs.getInt("MaPhong");
                thangNam = rs.getString("ThangNam");
            }
            String nextMonth = tinhThangTiepTheo(thangNam);
            if (newDienMoi != null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE ChiSo_DienNuoc SET DienCu=? WHERE MaPhong=? AND ThangNam=?")) {
                    ps.setInt(1, newDienMoi);
                    ps.setInt(2, maPhong);
                    ps.setString(3, nextMonth);
                    ps.executeUpdate();
                }
            }
            if (newNuocMoi != null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE ChiSo_DienNuoc SET NuocCu=? WHERE MaPhong=? AND ThangNam=?")) {
                    ps.setInt(1, newNuocMoi);
                    ps.setInt(2, maPhong);
                    ps.setString(3, nextMonth);
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tự động INSERT record cho những phòng có hợp đồng còn hoạt động
     * TRONG tháng yêu cầu (NgayBatDau <= cuối tháng VÀ NgayKetThuc >= đầu tháng).
     * Tránh chèn phòng vào các tháng trước khi hợp đồng bắt đầu.
     */
    private static void ensureActiveRoomsInMonth(String thangNam) {
        // Tính ngày đầu/cuối tháng từ chuỗi "MM-YYYY" tại Java để tránh multi-statement SQL
        int month = Integer.parseInt(thangNam.substring(0, 2));
        int year  = Integer.parseInt(thangNam.substring(3));
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay  = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        String sql =
            "INSERT INTO ChiSo_DienNuoc (MaPhong, ThangNam, DienCu, NuocCu) " +
            "SELECT DISTINCT hd.MaPhong, ?, 0, 0 " +
            "FROM HopDong hd " +
            "WHERE hd.TrangThai IN (N'Còn hiệu lực', N'Chờ nhận phòng') " +
            "  AND hd.NgayBatDau <= ? " +
            "  AND (hd.NgayKetThuc IS NULL OR YEAR(hd.NgayKetThuc) >= 3000 " +
            "       OR hd.NgayKetThuc >= ?) " +
            "  AND NOT EXISTS (" +
            "      SELECT 1 FROM ChiSo_DienNuoc c " +
            "      WHERE c.MaPhong = hd.MaPhong AND c.ThangNam = ?" +
            "  )";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, thangNam);                              // giá trị INSERT
            ps.setDate  (2, java.sql.Date.valueOf(lastDay));        // NgayBatDau <= cuối tháng
            ps.setDate  (3, java.sql.Date.valueOf(firstDay));       // NgayKetThuc >= đầu tháng
            ps.setString(4, thangNam);                              // NOT EXISTS
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Khi thêm hợp đồng mới, tự thêm record ChiSo với số cũ thực tế vừa chốt với khách.
     * ThangNam được tính từ ngayBatDau của HĐ — tránh ghi sai vào tháng không liên quan.
     * Nếu record đã tồn tại (do ensureActiveRoomsInMonth tạo trước với 0,0), cập nhật lại.
     */
    public static void themPhongVaoThangHienTaiVoiChiSo(int maPhong, int chiSoDien, int chiSoNuoc,
                                                          String ngayBatDau) {
        String thangNam;
        if (ngayBatDau != null && !ngayBatDau.isEmpty()) {
            try {
                LocalDate bd = LocalDate.parse(ngayBatDau);
                thangNam = String.format("%02d-%04d", bd.getMonthValue(), bd.getYear());
            } catch (Exception e) {
                thangNam = layThangNamMoiNhat();
                if (thangNam == null) thangNam = thangNamHienTai();
            }
        } else {
            thangNam = layThangNamMoiNhat();
            if (thangNam == null) thangNam = thangNamHienTai();
        }

        String checkSql  = "SELECT 1 FROM ChiSo_DienNuoc WHERE MaPhong=? AND ThangNam=?";
        String insertSql = "INSERT INTO ChiSo_DienNuoc (MaPhong, ThangNam, DienCu, NuocCu) VALUES (?, ?, ?, ?)";
        String updateSql = "UPDATE ChiSo_DienNuoc SET DienCu=?, NuocCu=? WHERE MaPhong=? AND ThangNam=?";
        try (Connection conn = DBConnection.getConnection()) {
            boolean exists;
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, maPhong);
                ps.setString(2, thangNam);
                exists = ps.executeQuery().next();
            }
            if (!exists) {
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, maPhong);
                    ps.setString(2, thangNam);
                    ps.setInt(3, chiSoDien);
                    ps.setInt(4, chiSoNuoc);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, chiSoDien);
                    ps.setInt(2, chiSoNuoc);
                    ps.setInt(3, maPhong);
                    ps.setString(4, thangNam);
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Khi thêm hợp đồng mới, tự thêm record ChiSo cho tháng mới nhất (nếu có)
     * để phòng đó hiển thị ngay lên trang chỉ số mà không cần reload.
     */
    public static void themPhongVaoThangHienTai(int maPhong) {
        String thangMoiNhat = layThangNamMoiNhat();
        if (thangMoiNhat == null) return;
        // Kiểm tra đã có record chưa, tránh trùng
        String checkSql = "SELECT 1 FROM ChiSo_DienNuoc WHERE MaPhong=? AND ThangNam=?";
        String insertSql = "INSERT INTO ChiSo_DienNuoc (MaPhong, ThangNam, DienCu, NuocCu) VALUES (?, ?, 0, 0)";
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, maPhong);
                ps.setString(2, thangMoiNhat);
                if (ps.executeQuery().next()) return;
            }
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, maPhong);
                ps.setString(2, thangMoiNhat);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String thangNamHienTai() {
        LocalDate now = LocalDate.now();
        return String.format("%02d-%04d", now.getMonthValue(), now.getYear());
    }
}
