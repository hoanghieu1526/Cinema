/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import models.Seats;

public class SeatsDAO {

    private Seats mapRow(ResultSet rs) throws SQLException {
        return new Seats(
                rs.getInt("seatID"),
                rs.getString("seatName"),
                rs.getInt("roomID")
        );
    }

    public List<Seats> getAllSeats() {
        String sql = "SELECT * FROM Seats";
        List<Seats> seatsList = new ArrayList<>();
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql);
                ResultSet rs = pts.executeQuery()
        ) {
            while (rs.next()) {
                seatsList.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seatsList;
    }

    public Seats getSeatById(int seatID) {
        String sql = "SELECT * FROM Seats WHERE seatID = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql);
        ) {
            pts.setInt(1, seatID);
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

    // Hàm bổ sung: Lấy danh sách ghế theo phòng
    public List<Seats> getSeatsByRoomId(int roomID) {
        String sql = "SELECT * FROM Seats WHERE roomID = ?";
        List<Seats> seatsList = new ArrayList<>();
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql);
        ) {
            pts.setInt(1, roomID);
            try (ResultSet rs = pts.executeQuery()) {
                while (rs.next()) {
                    seatsList.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seatsList;
    }

    public boolean insertSeat(Seats seat) {
        String sql = "INSERT INTO Seats (seatName, roomID) VALUES (?, ?)";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setString(1, seat.getSeatName());
            pts.setInt(2, seat.getRoomID());

            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Thêm nhiều ghế trong 1 transaction: hoặc thêm đủ tất cả, hoặc không thêm ghế nào.
    public boolean insertSeats(List<Seats> seats) {
        String sql = "INSERT INTO Seats (seatName, roomID) VALUES (?, ?)";
        try (Connection conn = DBcontext.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pts = conn.prepareStatement(sql)) {
                for (Seats seat : seats) {
                    pts.setString(1, seat.getSeatName());
                    pts.setInt(2, seat.getRoomID());
                    pts.addBatch();
                }
                pts.executeBatch();
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateSeat(Seats seat) {
        String sql = "UPDATE Seats SET seatName = ?, roomID = ? WHERE seatID = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setString(1, seat.getSeatName());
            pts.setInt(2, seat.getRoomID());
            pts.setInt(3, seat.getSeatID());

            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteSeat(int seatID) {
        String sql = "DELETE FROM Seats WHERE seatID = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setInt(1, seatID);
            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
