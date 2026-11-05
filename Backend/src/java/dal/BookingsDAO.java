package dal;

import enums.BookingStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import models.Bookings;

public class BookingsDAO {

    // Mã trả về của createBooking khi không đặt được (giá trị dương là bookingID của đơn mới).
    public static final int SEAT_TAKEN = -1;     // có ghế đã bị người khác đặt
    public static final int SEAT_INVALID = -2;   // có ghế không thuộc phòng chiếu của suất này
    public static final int FAILED = -3;         // lỗi cơ sở dữ liệu

    // Đơn đặt vé của một khách, mới nhất lên đầu.
    public List<Bookings> getBookingsByUserId(int userID) {
        String sql = "SELECT * FROM Bookings WHERE userID = ? ORDER BY bookingTime DESC, bookingID DESC";
        List<Bookings> bookingsList = new ArrayList<>();
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setInt(1, userID);
            try (ResultSet rs = pts.executeQuery()) {
                while (rs.next()) {
                    bookingsList.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookingsList;
    }

    /**
     * Tạo đơn (COMPLETED) cùng vé cho từng ghế trong MỘT transaction.
     * Ghế được kiểm tra và khoá ngay trong transaction nên hai người đặt cùng lúc một ghế
     * thì chỉ một người thành công, người còn lại nhận SEAT_TAKEN.
     *
     * @return bookingID (> 0) nếu thành công, ngược lại là SEAT_TAKEN / SEAT_INVALID / FAILED
     */
    public int createBooking(int userID, int showtimeID, int roomID, List<Integer> seatIds, BigDecimal pricePerSeat) {
        if (seatIds == null || seatIds.isEmpty()) {
            return SEAT_INVALID;
        }
        StringBuilder marks = new StringBuilder();
        for (int i = 0; i < seatIds.size(); i++) {
            marks.append(i == 0 ? "?" : ",?");
        }
        String validSql = "SELECT COUNT(*) FROM Seats WHERE roomID = ? AND seatID IN (" + marks + ")";
        String takenSql = "SELECT COUNT(*) FROM Tickets t WITH (UPDLOCK, HOLDLOCK) "
                + "JOIN Bookings b ON b.bookingID = t.bookingID "
                + "WHERE b.showtimeID = ? AND b.status <> 'CANCELLED' AND t.seatID IN (" + marks + ")";
        String bookingSql = "INSERT INTO Bookings (bookingTime, totalAmount, status, userID, showtimeID) VALUES (?, ?, 'COMPLETED', ?, ?)";
        String ticketSql = "INSERT INTO Tickets (price, roomID, seatID, bookingID) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBcontext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (countWhere(conn, validSql, roomID, seatIds) != seatIds.size()) {
                    conn.rollback();
                    return SEAT_INVALID;
                }
                if (countWhere(conn, takenSql, showtimeID, seatIds) > 0) {
                    conn.rollback();
                    return SEAT_TAKEN;
                }

                int bookingID;
                BigDecimal total = pricePerSeat.multiply(BigDecimal.valueOf(seatIds.size()));
                try (PreparedStatement pts = conn.prepareStatement(bookingSql, Statement.RETURN_GENERATED_KEYS)) {
                    pts.setObject(1, LocalDateTime.now());
                    pts.setBigDecimal(2, total);
                    pts.setInt(3, userID);
                    pts.setInt(4, showtimeID);
                    pts.executeUpdate();
                    try (ResultSet keys = pts.getGeneratedKeys()) {
                        if (!keys.next()) {
                            conn.rollback();
                            return FAILED;
                        }
                        bookingID = keys.getInt(1);
                    }
                }
                try (PreparedStatement pts = conn.prepareStatement(ticketSql)) {
                    for (Integer seatID : seatIds) {
                        pts.setBigDecimal(1, pricePerSeat);
                        pts.setInt(2, roomID);
                        pts.setInt(3, seatID);
                        pts.setInt(4, bookingID);
                        pts.addBatch();
                    }
                    pts.executeBatch();
                }
                conn.commit();
                return bookingID;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return FAILED;
        }
    }

    // Chạy câu COUNT(*) có dạng "... = ? AND x IN (?,?,...)": tham số đầu là mã phòng/suất, còn lại là mã ghế.
    private int countWhere(Connection conn, String sql, int firstParam, List<Integer> seatIds) throws SQLException {
        try (PreparedStatement pts = conn.prepareStatement(sql)) {
            pts.setInt(1, firstParam);
            for (int i = 0; i < seatIds.size(); i++) {
                pts.setInt(i + 2, seatIds.get(i));
            }
            try (ResultSet rs = pts.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private BookingStatus parseStatus(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return BookingStatus.PENDING;
        }
        try {
            return BookingStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return BookingStatus.PENDING;
        }
    }

    private Bookings mapRow(ResultSet rs) throws SQLException {
        return new Bookings(
                rs.getInt("bookingID"),
                rs.getObject("bookingTime", java.time.LocalDateTime.class),
                rs.getBigDecimal("totalAmount"),
                parseStatus(rs.getString("status")),
                rs.getInt("userID"),
                rs.getInt("showtimeID")
        );
    }

    public List<Bookings> getAllBookings() {
        String sql = "SELECT * FROM Bookings";
        List<Bookings> bookingsList = new ArrayList<>();
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql);
                ResultSet rs = pts.executeQuery()
        ) {
            while (rs.next()) {
                bookingsList.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookingsList;
    }

    public Bookings getBookingById(int bookingID) {
        String sql = "SELECT * FROM Bookings WHERE bookingID = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql);
        ) {
            pts.setInt(1, bookingID);
            try (ResultSet rs = pts.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insertBooking(Bookings booking) {
        String sql = "INSERT INTO Bookings (bookingTime, totalAmount, status, userID, showtimeID) VALUES (?, ?, ?, ?, ?)";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setObject(1, booking.getBookingTime() != null ? booking.getBookingTime() : LocalDateTime.now());
            pts.setBigDecimal(2, booking.getTotalAmount());
            pts.setString(3, (booking.getStatus() != null ? booking.getStatus() : BookingStatus.PENDING).name());
            pts.setInt(4, booking.getUserID());
            pts.setInt(5, booking.getShowtimeID());

            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateBooking(Bookings booking) {
        String sql = "UPDATE Bookings SET bookingTime = ?, totalAmount = ?, status = ?, userID = ?, showtimeID = ? WHERE bookingID = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setObject(1, booking.getBookingTime());
            pts.setBigDecimal(2, booking.getTotalAmount());
            pts.setString(3, (booking.getStatus() != null ? booking.getStatus() : BookingStatus.PENDING).name());
            pts.setInt(4, booking.getUserID());
            pts.setInt(5, booking.getShowtimeID());
            pts.setInt(6, booking.getBookingID());

            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteBooking(int bookingID) {
        String sql = "DELETE FROM Bookings WHERE bookingID = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setInt(1, bookingID);
            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
