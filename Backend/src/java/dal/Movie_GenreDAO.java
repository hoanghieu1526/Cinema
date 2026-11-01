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
import models.Movie_Genre;

public class Movie_GenreDAO {

    private Movie_Genre mapRow(ResultSet rs) throws SQLException {
        return new Movie_Genre(
                rs.getInt("movieID"),
                rs.getInt("genreID")
        );
    }

    public List<Movie_Genre> getAllMovieGenres() {
        String sql = "SELECT * FROM Movie_Genre";
        List<Movie_Genre> list = new ArrayList<>();
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql);
                ResultSet rs = pts.executeQuery()
        ) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public List<Movie_Genre> getGenresByMovieId(int movieID) {
        String sql = "SELECT * FROM Movie_Genre WHERE movieID = ?";
        List<Movie_Genre> list = new ArrayList<>();
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql);
        ) {
            pts.setInt(1, movieID);
            try (ResultSet rs = pts.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insertMovieGenre(Movie_Genre mg) {
        String sql = "INSERT INTO Movie_Genre (movieID, genreID) VALUES (?, ?)";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setInt(1, mg.getMovieID());
            pts.setInt(2, mg.getGenreID());

            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteMovieGenre(int movieID, int genreID) {
        String sql = "DELETE FROM Movie_Genre WHERE movieID = ? AND genreID = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setInt(1, movieID);
            pts.setInt(2, genreID);

            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
