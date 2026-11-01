/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import enums.ShowtimeStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import models.Showtimes;

public class ShowtimesDAO {

    private Showtimes mapRow(ResultSet rs) throws SQLException {
        return new Showtimes(
                rs.getInt("showtimeID"),
                rs.getInt("movieID"),
                rs.getInt("roomID"),
                rs.getObject("startTime", java.time.LocalDateTime.class),
                rs.getObject("endTime", java.time.LocalDateTime.class),
                ShowtimeStatus.valueOf(rs.getString("status"))
        );
    }

    public List<Showtimes> getAllShowtimes() {
        String sql = "SELECT * FROM Showtimes";
        List<Showtimes> showtimesList = new ArrayList<>();
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql);
                ResultSet rs = pts.executeQuery()
        ) {
            while (rs.next()) {
                showtimesList.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return showtimesList;
    }

    public Showtimes getShowtimeById(int showtimeID) {
        String sql = "SELECT * FROM Showtimes WHERE showtimeID = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql);
        ) {
            pts.setInt(1, showtimeID);
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

    public boolean insertShowtime(Showtimes showtime) {
        String sql = "INSERT INTO Showtimes (movieID, roomID, startTime, endTime, status) VALUES (?, ?, ?, ?, ?)";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setInt(1, showtime.getMovieID());
            pts.setInt(2, showtime.getRoomID());
            pts.setObject(3, showtime.getStartTime());
            pts.setObject(4, showtime.getEndTime());
            pts.setString(5, showtime.getStatus().name());

            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateShowtime(Showtimes showtime) {
        String sql = "UPDATE Showtimes SET movieID = ?, roomID = ?, startTime = ?, endTime = ?, status = ? WHERE showtimeID = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setInt(1, showtime.getMovieID());
            pts.setInt(2, showtime.getRoomID());
            pts.setObject(3, showtime.getStartTime());
            pts.setObject(4, showtime.getEndTime());
            pts.setString(5, showtime.getStatus().name());
            pts.setInt(6, showtime.getShowtimeID());

            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
