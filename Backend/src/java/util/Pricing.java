package util;

import java.math.BigDecimal;
import java.util.Locale;
import models.Rooms;

/** Giá vé một ghế, tính theo loại phòng chiếu. Server luôn tự tính lại, không tin giá từ trình duyệt. */
public final class Pricing {

    public static final int MAX_SEATS_PER_BOOKING = 8;

    private static final BigDecimal STANDARD = new BigDecimal("80000");
    private static final BigDecimal IMAX = new BigDecimal("120000");
    private static final BigDecimal VIP = new BigDecimal("150000");

    private Pricing() {
    }

    public static BigDecimal seatPrice(Rooms room) {
        String type = (room == null || room.getRoomType() == null) ? "" : room.getRoomType().toUpperCase(Locale.ROOT);
        if (type.contains("IMAX")) {
            return IMAX;
        }
        if (type.contains("VIP") || type.contains("GOLD")) {
            return VIP;
        }
        return STANDARD;
    }
}
