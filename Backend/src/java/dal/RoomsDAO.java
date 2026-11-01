/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import enums.RoomStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import models.Rooms;

public class RoomsDAO {

    private Rooms mapRow(ResultSet rs) throws SQLException {
        return new Rooms(
                rs.getInt("roomID"),
                rs.getString("roomName"),
                rs.getString("roomType"),
                RoomStatus.valueOf(rs.getString("status")), 
                rs.getInt("userID"),
                rs.getInt("showtimeID")
        );
    }

    public List<Rooms> getAllRooms() {
        String sql = "SELECT * FROM Rooms";
        List<Rooms> roomsList = new ArrayList<>();
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql);
                ResultSet rs = pts.executeQuery()
        ) {
            while (rs.next()) {
                roomsList.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roomsList;
    }

    public Rooms getRoomById(int roomID) {
        String sql = "SELECT * FROM Rooms WHERE roomID = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql);
        ) {
            pts.setInt(1, roomID);
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

    public boolean insertRoom(Rooms room) {
        String sql = "INSERT INTO Rooms (roomName, roomType, status, userID, showtimeID) VALUES (?, ?, ?, ?, ?)";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setString(1, room.getRoomName());
            pts.setString(2, room.getRoomType());
            pts.setString(3, room.getStatus().name());
            pts.setInt(4, room.getUserID());
            pts.setInt(5, room.getShowtimeID());

            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateRoom(Rooms room) {
        String sql = "UPDATE Rooms SET roomName = ?, roomType = ?, status = ?, userID = ?, showtimeID = ? WHERE roomID = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setString(1, room.getRoomName());
            pts.setString(2, room.getRoomType());
            pts.setString(3, room.getStatus().name());
            pts.setInt(4, room.getUserID());
            pts.setInt(5, room.getShowtimeID());
            pts.setInt(6, room.getRoomID());

            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
