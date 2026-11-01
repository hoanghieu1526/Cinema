/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import enums.UserGender;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import models.Users;

public class UsersDAO {

    private Users mapRow(ResultSet rs) throws SQLException {
        return new Users(
                rs.getInt("userID"),
                rs.getString("fullName"),
                rs.getString("userName"),
                rs.getString("passwordHash"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("imageURL"),
                UserGender.valueOf(rs.getString("gender")),
                rs.getObject("dateOfBirth", java.time.LocalDateTime.class),
                rs.getObject("createdAt", java.time.LocalDateTime.class),
                rs.getObject("updateAt", java.time.LocalDateTime.class)
        );
    }

    public List<Users> getAllUsers() {
        String sql = "SELECT * FROM Users";
        List<Users> usersList = new ArrayList<>();
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql);
                ResultSet rs = pts.executeQuery()
        ) {
            while (rs.next()) {
                usersList.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usersList;
    }

    public Users getUserById(int userID) {
        String sql = "SELECT * FROM Users WHERE userID = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql);
        ) {
            pts.setInt(1, userID);
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
    
    
    public Users getUserByUserName(String userName) {
        String sql = "SELECT * FROM Users WHERE userName = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setString(1, userName);
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

    public Users getUserByEmail(String email) {
        String sql = "SELECT * FROM Users WHERE email = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setString(1, email);
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

    public boolean insertUser(Users user) {
        String sql = "INSERT INTO Users (fullName, userName, passwordHash, phone, email, imageURL, gender, dateOfBirth, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setString(1, user.getFullName());
            pts.setString(2, user.getUserName());
            pts.setString(3, user.getPasswordHash());
            pts.setString(4, user.getPhone());
            pts.setString(5, user.getEmail());
            pts.setString(6, user.getImageURL());
            pts.setString(7, user.getGender().name());
            pts.setObject(8, user.getDateOfBirth());
            pts.setObject(9, user.getCreatedAt());

            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateUser(Users user) {
        String sql = "UPDATE Users SET fullName = ?, userName = ?, passwordHash = ?, phone = ?, email = ?, imageURL = ?, gender = ?, dateOfBirth = ?, updateAt = ? WHERE userID = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setString(1, user.getFullName());
            pts.setString(2, user.getUserName());
            pts.setString(3, user.getPasswordHash());
            pts.setString(4, user.getPhone());
            pts.setString(5, user.getEmail());
            pts.setString(6, user.getImageURL());
            pts.setString(7, user.getGender().name());
            pts.setObject(8, user.getDateOfBirth());
            pts.setObject(9, LocalDateTime.now());
            pts.setInt(10, user.getUserID());

            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}