/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 *
 * @author NGUYEN HOANG HIEU
 */
public class Genres {
    private int genreID;
    private String genreName;

    public Genres(int genreID, String genreName) {
        this.genreID = genreID;
        this.genreName = genreName;
    }

    public int getGenreID() {
        return genreID;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreID(int genreID) {
        this.genreID = genreID;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }
    

}
