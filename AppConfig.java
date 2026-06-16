package QuanLiNhaTro;

import java.io.*;
import java.util.Properties;

/**
 * Đọc cấu hình email từ file email.properties (cùng thư mục với jar/class).
 * Nếu file không tồn tại → tự tạo file mẫu để user điền vào.
 *
 * Nội dung file email.properties:
 *   email.from=your_email@gmail.com
 *   email.password=xxxx xxxx xxxx xxxx
 *   email.smtp.host=smtp.gmail.com
 *   email.smtp.port=587
 */
public class AppConfig {

    private static final String FILE_NAME = "email.properties";
    private static Properties cache = null;

    /** Xóa cache để đọc lại file email.properties lần sau. */
    public static void reload() { cache = null; }

    public static Properties load() {
        if (cache != null) return cache;
        Properties p = new Properties();
        File f = new File(FILE_NAME);
        if (!f.exists()) {
            // Tạo file mẫu
            try (Writer w = new FileWriter(f)) {
                w.write("# Cấu hình gửi email — điền thông tin tài khoản Gmail vào đây\n");
                w.write("# Cách lấy App Password: https://support.google.com/accounts/answer/185833\n");
                w.write("email.from=nthaiduong.vn@gmail.com\n");
                w.write("email.password=vwxn viia tynh tbdt\n");
                w.write("email.smtp.host=smtp.gmail.com\n");
                w.write("email.smtp.port=465\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (InputStream in = new FileInputStream(f)) {
            p.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        cache = p;
        return p;
    }

    public static String emailFrom()    { return load().getProperty("email.from", "").trim(); }
    public static String emailPassword(){ return load().getProperty("email.password", "").trim(); }
    public static String smtpHost()     { return load().getProperty("email.smtp.host", "smtp.gmail.com").trim(); }
    public static int    smtpPort()     {
        try { return Integer.parseInt(load().getProperty("email.smtp.port", "465").trim()); }
        catch (Exception e) { return 465; }
    }

    /** Kiểm tra cấu hình email đã được điền đúng chưa. */
    public static boolean emailConfigured() {
        String from = emailFrom();
        String pass = emailPassword();
        return !from.isEmpty() && !from.startsWith("your_")
                && !pass.isEmpty() && !pass.startsWith("xxxx");
    }
}
