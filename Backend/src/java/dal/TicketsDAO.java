/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import models.Tickets;

public class TicketsDAO {

    private Tickets mapRow(ResultSet rs) throws SQLException {
        return new Tickets(
                rs.getInt("ticketID"),
                rs.getBigDecimal("price"),
                rs.getInt("roomID"), 
                rs.getInt("seatID"),
                rs.getInt("bookingID")
        );
    }

    public List<Tickets> getAllTickets() {
        String sql = "SELECT * FROM Tickets";
        List<Tickets> ticketsList = new ArrayList<>();
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql);
                ResultSet rs = pts.executeQuery()
        ) {
            while (rs.next()) {
                ticketsList.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ticketsList;
    }

    public Tickets getTicketById(int ticketID) {
        String sql = "SELECT * FROM Tickets WHERE ticketID = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql);
        ) {
            pts.setInt(1, ticketID);
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
    
    public List<Tickets> getTicketsByBookingId(int bookingID) {
        String sql = "SELECT * FROM Tickets WHERE bookingID = ?";
        List<Tickets> ticketsList = new ArrayList<>();
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql);
        ) {
            pts.setInt(1, bookingID);
            try (ResultSet rs = pts.executeQuery()) {
                while (rs.next()) {
                    ticketsList.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ticketsList;
    }

    public boolean insertTicket(Tickets ticket) {
        String sql = "INSERT INTO Tickets (price, roomID, seatID, bookingID) VALUES (?, ?, ?, ?)";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setBigDecimal(1, ticket.getPrice());
            pts.setInt(2, ticket.getRoomID());
            pts.setInt(3, ticket.getSeatID());
            pts.setInt(4, ticket.getBookingID());

            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateTicket(Tickets ticket) {
        String sql = "UPDATE Tickets SET price = ?, roomID = ?, seatID = ?, bookingID = ? WHERE ticketID = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setBigDecimal(1, ticket.getPrice());
            pts.setInt(2, ticket.getRoomID());
            pts.setInt(3, ticket.getSeatID());
            pts.setInt(4, ticket.getBookingID());
            pts.setInt(5, ticket.getTicketID());

            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
