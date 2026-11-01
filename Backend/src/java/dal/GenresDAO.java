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
import models.Genres;

public class GenresDAO {

    private Genres mapRow(ResultSet rs) throws SQLException {
        return new Genres(
                rs.getInt("genreID"),
                rs.getString("genreName")
        );
    }

    public List<Genres> getAllGenres() {
        String sql = "SELECT * FROM Genres";
        List<Genres> genresList = new ArrayList<>();
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql);
                ResultSet rs = pts.executeQuery()
        ) {
            while (rs.next()) {
                genresList.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return genresList;
    }

    public Genres getGenreById(int genreID) {
        String sql = "SELECT * FROM Genres WHERE genreID = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql);
        ) {
            pts.setInt(1, genreID);
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

    public boolean insertGenre(Genres genre) {
        String sql = "INSERT INTO Genres (genreName) VALUES (?)";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setString(1, genre.getGenreName());

            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateGenre(Genres genre) {
        String sql = "UPDATE Genres SET genreName = ? WHERE genreID = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setString(1, genre.getGenreName());
            pts.setInt(2, genre.getGenreID());

            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteGenre(int genreID) {
        String sql = "DELETE FROM Genres WHERE genreID = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setInt(1, genreID);
            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
