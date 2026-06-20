import java.sql.*;

public class CheckDB {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=DoAn1;encrypt=true;trustServerCertificate=true";
        String user = "sa";
        String pass = "1234512345";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, "DichVu", null);
            while (rs.next()) {
                System.out.println(rs.getString("COLUMN_NAME") + " - " + rs.getString("TYPE_NAME"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
