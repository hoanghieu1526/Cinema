package dal;

import enums.AdminRole;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import models.Admins;

public class AdminsDAO {

    private AdminRole parseRole(String roleStr) {
        if (roleStr == null || roleStr.trim().isEmpty()) {
            return AdminRole.MOVIES_MANAGER;
        }
        try {
            return AdminRole.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return AdminRole.MOVIES_MANAGER;
        }
    }

    private Admins mapRow(ResultSet rs) throws SQLException {
        return new Admins(
                rs.getInt("adminID"),
                rs.getString("fullName"),
                rs.getString("account"),
                rs.getString("passwordHash"),
                rs.getString("phone"),
                rs.getString("email"),
                parseRole(rs.getString("roles")),
                rs.getObject("createdAt", java.time.LocalDateTime.class),
                rs.getObject("updatedAt", java.time.LocalDateTime.class)
        );
    }

    public List<Admins> getAllAdmins() {
        String sql = "SELECT * FROM Admins";
        List<Admins> adminsList = new ArrayList<>();
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql);
                ResultSet rs = pts.executeQuery()
        ) {
            while (rs.next()) {
                adminsList.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return adminsList;
    }

    public Admins getAdminById(int adminID) {
        String sql = "SELECT * FROM Admins WHERE adminID = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql);
        ) {
            pts.setInt(1, adminID);
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

    public Admins getAdminByAccount(String account) {
        String sql = "SELECT * FROM Admins WHERE account = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setString(1, account);
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

    public boolean insertAdmin(Admins admin) {
        String sql = "INSERT INTO Admins (fullName, account, passwordHash, phone, email, roles, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setString(1, admin.getFullName());
            pts.setString(2, admin.getAccount());
            pts.setString(3, admin.getPasswordHash());
            pts.setString(4, admin.getPhone());
            pts.setString(5, admin.getEmail());
            pts.setString(6, (admin.getRoles() != null ? admin.getRoles() : AdminRole.MOVIES_MANAGER).name());
            pts.setObject(7, admin.getCreatedAt() != null ? admin.getCreatedAt() : LocalDateTime.now());

            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateAdmin(Admins admin) {
        String sql = "UPDATE Admins SET fullName = ?, account = ?, passwordHash = ?, phone = ?, email = ?, roles = ?, updatedAt = ? WHERE adminID = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setString(1, admin.getFullName());
            pts.setString(2, admin.getAccount());
            pts.setString(3, admin.getPasswordHash());
            pts.setString(4, admin.getPhone());
            pts.setString(5, admin.getEmail());
            pts.setString(6, (admin.getRoles() != null ? admin.getRoles() : AdminRole.MOVIES_MANAGER).name());
            pts.setObject(7, LocalDateTime.now());
            pts.setInt(8, admin.getAdminID());

            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteAdmin(int adminID) {
        String sql = "DELETE FROM Admins WHERE adminID = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setInt(1, adminID);
            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
