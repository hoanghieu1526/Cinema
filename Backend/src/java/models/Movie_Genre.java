/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 *
 * @author NGUYEN HOANG HIEU
 */
public class Movie_Genre {
    private int movieID;
    private int genreID;

    public Movie_Genre() {
    }

    public Movie_Genre(int movieID, int genreID) {
        this.movieID = movieID;
        this.genreID = genreID;
    }

    public int getMovieID() {
        return movieID;
    }

    public void setMovieID(int movieID) {
        this.movieID = movieID;
    }

    public int getGenreID() {
        return genreID;
    }

    public void setGenreID(int genreID) {
        this.genreID = genreID;
    }

    @Override
    public String toString() {
        return "Movie_Genre{" + "movieID=" + movieID + ", genreID=" + genreID + '}';
    }
}
