package dal;

import enums.BookingStatus;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import models.Bookings;

public class BookingsDAO {

    private Bookings mapRow(ResultSet rs) throws SQLException {
        return new Bookings(
                rs.getInt("bookingID"),
                rs.getObject("bookingTime", java.time.LocalDateTime.class),
                rs.getBigDecimal("totalAmount"),
                BookingStatus.valueOf(rs.getString("status")),
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
            pts.setObject(1, booking.getBookingTime());
            pts.setBigDecimal(2, booking.getTotalAmount());
            pts.setString(3, booking.getStatus().name()); 
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
            pts.setString(3, booking.getStatus().name());
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
}