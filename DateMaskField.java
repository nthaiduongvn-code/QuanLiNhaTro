package QuanLiNhaTro;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Ô nhập ngày kiểu masked input — định dạng dd/MM/yyyy màu mờ.
 *
 * Validation theo thứ tự nhập:
 *  1. Ngày  : chữ số đầu 4-9 → tự chèn "0" (gõ 5 → 05).
 *             Ngày 00 và 32-39 bị từ chối ngay.
 *  2. Tháng : chữ số đầu 2-9 → tự chèn "0" (gõ 3 → 03).
 *             Tháng 00 và 13-19 bị từ chối ngay.
 *             Sau khi nhập xong tháng: kiểm tra ngày có khớp không
 *             (tháng 4/6/9/11 ≤ 30 ngày; tháng 2 ≤ 29 ngày tạm thời).
 *  3. Năm   : sau khi nhập đủ 4 chữ số, kiểm tra tháng 2:
 *             nếu ngày > 28 và không phải năm nhuận → lỗi, nhập lại.
 *
 * Phím: 0-9 nhập / Backspace xóa từng ký tự / Delete xóa toàn bộ.
 * getValue() → "yyyy-MM-dd" hoặc "" nếu trống / chưa đủ / không hợp lệ.
 * setValue("yyyy-MM-dd") → điền sẵn.
 */
public class DateMaskField extends JTextField {

    // ── Màu sắc ──────────────────────────────────────────────────────────────
    private static final Color C_DIGIT  = new Color(0x1E293B);
    private static final Color C_HINT   = new Color(0xB0BEC5);
    private static final Color C_SEP    = new Color(0x94A3B8);
    private static final Color C_FOCUS  = new Color(0x0D9488);
    private static final Color C_BORDER = new Color(0xCBD5E1);
    private static final Color C_CURSOR = new Color(0x0D9488);
    private static final Color C_ERR_BG = new Color(0xFEE2E2);
    private static final Color C_ERR_BD = new Color(0xEF4444);

    // ── Dữ liệu ──────────────────────────────────────────────────────────────
    private final char[] slots = new char[8]; // [0,1]=ngày [2,3]=tháng [4-7]=năm
    private int filled = 0;

    private static final char[] HINTS      = {'d','d','M','M','y','y','y','y'};
    private static final int[]  DRAW_ORDER = {0, 1, -1, 2, 3, -1, 4, 5, 6, 7};

    private boolean cursorVisible = true;
    private boolean flashError    = false;
    private final Timer blinkTimer;
    private final Timer errorTimer;

    // ── Khởi tạo ─────────────────────────────────────────────────────────────

    public DateMaskField() {
        super("");
        setOpaque(false);
        setEditable(false);
        setFocusable(true);
        setBorder(null);
        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        setBackground(Color.WHITE);

        blinkTimer = new Timer(530, e -> { cursorVisible = !cursorVisible; repaint(); });
        errorTimer = new Timer(400, e -> { flashError = false; repaint(); });
        errorTimer.setRepeats(false);

        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                cursorVisible = true; blinkTimer.start(); repaint();
            }
            @Override public void focusLost(FocusEvent e) {
                blinkTimer.stop(); cursorVisible = false; repaint();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (Character.isDigit(c)) {
                    if (filled >= 8) filled = 0;
                    handleDigit(c - '0');
                    cursorVisible = true;
                    repaint();
                }
                e.consume();
            }
            @Override public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_BACK_SPACE:
                        if (filled > 0) { filled--; cursorVisible = true; repaint(); }
                        e.consume(); break;
                    case KeyEvent.VK_DELETE:
                        filled = 0; cursorVisible = true; repaint();
                        e.consume(); break;
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { requestFocusInWindow(); }
        });
    }

    // ── Logic nhập thông minh ────────────────────────────────────────────────

    private void handleDigit(int d) {
        switch (filled) {

            // ─── Chữ số đầu của NGÀY ──────────────────────────────────────
            case 0:
                if (d >= 4) {
                    slots[filled++] = '0';
                    slots[filled++] = (char)('0' + d);
                } else {
                    slots[filled++] = (char)('0' + d);
                }
                break;

            // ─── Chữ số thứ hai của NGÀY ──────────────────────────────────
            case 1: {
                int d1 = slots[0] - '0';
                if (d1 == 0 && d == 0) { rejectDigit(); return; } // ngày 00
                if (d1 == 3 && d >= 2)  { rejectDigit(); return; } // ngày 32-39
                slots[filled++] = (char)('0' + d);
                break;
            }

            // ─── Chữ số đầu của THÁNG ─────────────────────────────────────
            case 2:
                if (d >= 2) {
                    slots[filled++] = '0';
                    slots[filled++] = (char)('0' + d);
                } else {
                    slots[filled++] = (char)('0' + d);
                }
                break;

            // ─── Chữ số thứ hai của THÁNG ─────────────────────────────────
            case 3: {
                int m1 = slots[2] - '0';
                if (m1 == 0 && d == 0) { rejectDigit(); return; } // tháng 00
                if (m1 == 1 && d >= 3)  { rejectDigit(); return; } // tháng 13-19
                slots[filled++] = (char)('0' + d);
                break;
            }

            // ─── Các chữ số NĂM (4 vị trí) ────────────────────────────────
            default:
                slots[filled++] = (char)('0' + d);
                break;
        }

        // ─── Kiểm tra sau khi hoàn thành tháng (filled == 4) ──────────────
        if (filled == 4) {
            checkDayVsMonth();
        }

        // ─── Kiểm tra sau khi hoàn thành năm (filled == 8) ────────────────
        if (filled == 8) {
            checkLeapYear();
        }
    }

    /**
     * Sau khi tháng đủ 2 chữ số: kiểm tra ngày có vượt quá số ngày tối đa không.
     * Tháng 2 tạm cho phép 29 ngày (năm nhuận sẽ kiểm tra sau khi nhập năm).
     */
    private void checkDayVsMonth() {
        int day   = (slots[0]-'0')*10 + (slots[1]-'0');
        int month = (slots[2]-'0')*10 + (slots[3]-'0');

        int maxDay;
        switch (month) {
            case 2:              maxDay = 29; break; // tạm thời cho 29, kiểm tra leap sau
            case 4: case 6:
            case 9: case 11:     maxDay = 30; break;
            default:             maxDay = 31; break;
        }

        if (day > maxDay) {
            // Ngày vượt quá số ngày của tháng này → xóa hết, nhập lại
            rejectAndReset();
        }
    }

    /**
     * Sau khi năm đủ 4 chữ số: kiểm tra tháng 2.
     * Nếu ngày = 29 nhưng không phải năm nhuận → lỗi.
     */
    private void checkLeapYear() {
        int day   = (slots[0]-'0')*10 + (slots[1]-'0');
        int month = (slots[2]-'0')*10 + (slots[3]-'0');
        int year  = (slots[4]-'0')*1000 + (slots[5]-'0')*100
                  + (slots[6]-'0')*10   + (slots[7]-'0');

        if (month == 2 && day == 29) {
            boolean isLeap = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
            if (!isLeap) {
                rejectAndReset();
            }
        }
    }

    /** Từ chối một chữ số đơn lẻ (không thay đổi filled). */
    private void rejectDigit() {
        flashError = true;
        errorTimer.restart();
        repaint();
    }

    /** Từ chối và xóa toàn bộ (dùng khi ngày/tháng/năm không khớp). */
    private void rejectAndReset() {
        filled = 0;
        flashError = true;
        errorTimer.restart();
        repaint();
    }

    // ── API ──────────────────────────────────────────────────────────────────

    public String getValue() {
        if (filled < 8) return "";
        try {
            int d = (slots[0]-'0')*10 + (slots[1]-'0');
            int m = (slots[2]-'0')*10 + (slots[3]-'0');
            int y = (slots[4]-'0')*1000 + (slots[5]-'0')*100
                  + (slots[6]-'0')*10   + (slots[7]-'0');
            if (d < 1 || d > 31 || m < 1 || m > 12 || y < 1) return "";
            // Kiểm tra lần cuối ngày vs tháng
            int[] maxDays = {0,31,isLeap(y)?29:28,31,30,31,30,31,31,30,31,30,31};
            if (d > maxDays[m]) return "";
            return String.format("%04d-%02d-%02d", y, m, d);
        } catch (Exception ex) { return ""; }
    }

    public void setValue(String yyyyMMdd) {
        filled = 0;
        if (yyyyMMdd == null || yyyyMMdd.trim().isEmpty()) { repaint(); return; }
        try {
            String[] p = yyyyMMdd.split("-");
            if (p.length < 3) { repaint(); return; }
            int y = Integer.parseInt(p[0]);
            int m = Integer.parseInt(p[1]);
            int d = Integer.parseInt(p[2]);
            slots[0] = (char)('0' + d/10);      slots[1] = (char)('0' + d%10);
            slots[2] = (char)('0' + m/10);      slots[3] = (char)('0' + m%10);
            slots[4] = (char)('0' + y/1000);
            slots[5] = (char)('0' + (y%1000)/100);
            slots[6] = (char)('0' + (y%100)/10);
            slots[7] = (char)('0' + y%10);
            filled = 8;
        } catch (Exception ex) { filled = 0; }
        repaint();
    }

    public boolean isEmpty() { return filled == 0; }

    private static boolean isLeap(int y) {
        return (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0);
    }

    // ── Vẽ ───────────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int w = getWidth(), h = getHeight();
        boolean focused = isFocusOwner();

        // Nền
        Color bg = flashError ? C_ERR_BG : (isEnabled() ? getBackground() : new Color(0xF8FAFC));
        g2.setColor(bg);
        g2.fillRoundRect(1, 1, w-2, h-2, 8, 8);

        if (focused && !flashError) {
            g2.setColor(new Color(13, 148, 136, 18));
            g2.fillRoundRect(1, 1, w-2, h-2, 8, 8);
        }

        // Văn bản
        Font font = getFont() != null ? getFont() : Theme.FONT_BASE;
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int textY   = (h + fm.getAscent() - fm.getDescent()) / 2;
        int x       = 10;
        int cursorX = x;

        for (int idx : DRAW_ORDER) {
            if (idx == -1) {
                g2.setColor(flashError ? C_ERR_BD : C_SEP);
                g2.drawString("/", x, textY);
                x += fm.charWidth('/');
            } else {
                if (idx == filled) cursorX = x;
                char  c   = idx < filled ? slots[idx] : HINTS[idx];
                Color col = idx < filled ? (flashError ? C_ERR_BD : C_DIGIT) : C_HINT;
                g2.setColor(col);
                g2.drawString(String.valueOf(c), x, textY);
                x += fm.charWidth(c);
            }
        }
        if (filled == 8) cursorX = x;

        // Con trỏ
        if (focused && cursorVisible && filled < 8 && !flashError) {
            g2.setColor(C_CURSOR);
            g2.fillRect(cursorX, textY - fm.getAscent() + 2, 2, fm.getAscent() - 2);
        }

        // Viền
        if (flashError) {
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(C_ERR_BD);
            g2.drawRoundRect(1, 1, w-3, h-3, 7, 7);
        } else if (focused) {
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(C_FOCUS);
            g2.drawRoundRect(1, 1, w-3, h-3, 7, 7);
        } else {
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(C_BORDER);
            g2.drawRoundRect(0, 0, w-1, h-1, 8, 8);
        }

        g2.dispose();
    }

    @Override public void paintBorder(Graphics g) {}

    @Override
    public Insets getInsets() { return new Insets(0, 10, 0, 10); }
}