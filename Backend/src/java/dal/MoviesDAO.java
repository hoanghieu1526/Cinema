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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import models.Movies;

public class MoviesDAO {

    private Movies mapRow(ResultSet rs) throws SQLException {
        return new Movies(
                rs.getInt("movieID"),
                rs.getString("movieName"),
                rs.getInt("approvedByAdminID"),
                rs.getString("category"),
                rs.getString("bio"),
                rs.getString("trailerURL"),
                rs.getString("posterURL"),
                rs.getString("director"),
                rs.getString("actor"),
                rs.getObject("releaseDate", java.time.LocalDateTime.class),
                rs.getObject("createdAt", java.time.LocalDateTime.class),
                rs.getInt("duration"),
                rs.getString("language"),
                rs.getBigDecimal("ratingScore"),
                rs.getString("ageRating"),
                rs.getBigDecimal("rate")
        );
    }

    public List<Movies> getAllMovies() {
        String sql = "SELECT * FROM Movies";
        List<Movies> moviesList = new ArrayList<>();
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql);
                ResultSet rs = pts.executeQuery()
        ) {
            while (rs.next()) {
                moviesList.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return moviesList;
    }

    public Movies getMovieById(int movieID) {
        String sql = "SELECT * FROM Movies WHERE movieID = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql);
        ) {
            pts.setInt(1, movieID);
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

    public boolean insertMovie(Movies movie) {
        String sql = "INSERT INTO Movies (movieName, approvedByAdminID, category, bio, trailerURL, posterURL, director, actor, releaseDate, createdAt, duration, language, ratingScore, ageRating, rate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setString(1, movie.getMovieName());
            pts.setInt(2, movie.getApprovedByAdminID());
            pts.setString(3, movie.getCategory());
            pts.setString(4, movie.getBio());
            pts.setString(5, movie.getTrailerURL());
            pts.setString(6, movie.getPosterURL());
            pts.setString(7, movie.getDirector());
            pts.setString(8, movie.getActor());
            pts.setObject(9, movie.getReleaseDate());
            pts.setObject(10, movie.getCreatedAt());
            pts.setInt(11, movie.getDuration());
            pts.setString(12, movie.getLanguage());
            pts.setBigDecimal(13, movie.getRatingScore());
            pts.setString(14, movie.getAgeRating());
            pts.setBigDecimal(15, movie.getRate());

            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateMovie(Movies movie) {
        String sql = "UPDATE Movies SET movieName = ?, approvedByAdminID = ?, category = ?, bio = ?, trailerURL = ?, posterURL = ?, director = ?, actor = ?, releaseDate = ?, duration = ?, language = ?, ratingScore = ?, ageRating = ?, rate = ? WHERE movieID = ?";
        try (
                Connection conn = DBcontext.getConnection();
                PreparedStatement pts = conn.prepareStatement(sql)
        ) {
            pts.setString(1, movie.getMovieName());
            pts.setInt(2, movie.getApprovedByAdminID());
            pts.setString(3, movie.getCategory());
            pts.setString(4, movie.getBio());
            pts.setString(5, movie.getTrailerURL());
            pts.setString(6, movie.getPosterURL());
            pts.setString(7, movie.getDirector());
            pts.setString(8, movie.getActor());
            pts.setObject(9, movie.getReleaseDate());
            // createdAt thường không bị update
            pts.setInt(10, movie.getDuration());
            pts.setString(11, movie.getLanguage());
            pts.setBigDecimal(12, movie.getRatingScore());
            pts.setString(13, movie.getAgeRating());
            pts.setBigDecimal(14, movie.getRate());
            
            pts.setInt(15, movie.getMovieID());

            int rowsAffected = pts.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
