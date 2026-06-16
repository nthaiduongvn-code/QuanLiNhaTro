package QuanLiNhaTro;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    // Sửa thông tin DB tại đây nếu cần
    private static final String URL  = "jdbc:sqlserver://localhost:1433;"
            + "databaseName=DoAn1;encrypt=true;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASS = "1234512345";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            System.out.println("❌ Kết nối SQL Server thất bại: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
