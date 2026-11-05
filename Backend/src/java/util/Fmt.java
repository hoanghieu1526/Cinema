package util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Các hàm định dạng dùng trong JSP qua EL (khai báo ở WEB-INF/cinema.tld, tiền tố "f").
 * Ví dụ: ${f:money(b.totalAmount)}, ${f:time(st.startTime)}.
 */
public final class Fmt {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private static final String[] WEEKDAYS = {
        "Thứ hai", "Thứ ba", "Thứ tư", "Thứ năm", "Thứ sáu", "Thứ bảy", "Chủ nhật"
    };

    private Fmt() {
    }

    /** 80000 -> "80.000 đ". */
    public static String money(Object amount) {
        if (amount == null) {
            return "0 đ";
        }
        BigDecimal value = amount instanceof BigDecimal ? (BigDecimal) amount : new BigDecimal(amount.toString());
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator('.');
        return new DecimalFormat("#,##0", symbols).format(value) + " đ";
    }

    /** "20:30 05/11/2026". */
    public static String datetime(LocalDateTime t) {
        return t == null ? "" : t.format(DATE_TIME);
    }

    /** "05/11/2026". */
    public static String date(LocalDateTime t) {
        return t == null ? "" : t.format(DATE);
    }

    /** "20:30". */
    public static String time(LocalDateTime t) {
        return t == null ? "" : t.format(TIME);
    }

    /** "Thứ năm, 05/11/2026". */
    public static String day(LocalDateTime t) {
        if (t == null) {
            return "";
        }
        return WEEKDAYS[t.getDayOfWeek().getValue() - 1] + ", " + t.format(DATE);
    }

    /** Bỏ dấu tiếng Việt và đưa về chữ thường, dùng để tìm kiếm không phân biệt dấu. */
    public static String fold(String text) {
        if (text == null) {
            return "";
        }
        String s = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return s.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT);
    }

    /** Màu nền poster giả (0-359) cố định theo mã phim, để mỗi phim một màu riêng. */
    public static int hue(int movieID) {
        return (Math.abs(movieID) * 47 + 12) % 360;
    }
}
