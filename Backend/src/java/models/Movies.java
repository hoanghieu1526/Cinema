/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author NGUYEN HOANG HIEU
 */
public class Movies {
    private int movieID;
    private String movieName;
    private int approvedByAdminID;
    private String category;
    private String bio;
    private String trailerURL;
    private String posterURL;
    private String director;
    private String actor;
    private LocalDateTime releaseDate;        
    private LocalDateTime createdAt;      
    private int duration;                
    private String language;              
    private BigDecimal ratingScore;       
    private String ageRating;             
    private BigDecimal rate;

public Movies(int movieID, String movieName, int approvedByAdminID, String category, String bio, String trailerURL, String posterURL, String director, String actor, LocalDateTime releaseDate, LocalDateTime createdAt, int duration, String language, BigDecimal ratingScore, String ageRating, BigDecimal rate) {
        this.movieID = movieID;
        this.movieName = movieName;
        this.approvedByAdminID = approvedByAdminID;
        this.category = category;
        this.bio = bio;
        this.trailerURL = trailerURL;
        this.posterURL = posterURL;
        this.director = director;
        this.actor = actor;
        this.releaseDate = releaseDate;
        this.createdAt = createdAt;
        this.duration = duration;
        this.language = language;
        this.ratingScore = ratingScore;
        this.ageRating = ageRating;
        this.rate = rate;
    }

    public int getMovieID() {
        return movieID;
    }

    public void setMovieID(int movieID) {
        this.movieID = movieID;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public int getApprovedByAdminID() {
        return approvedByAdminID;
    }

    public void setApprovedByAdminID(int approvedByAdminID) {
        this.approvedByAdminID = approvedByAdminID;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getTrailerURL() {
        return trailerURL;
    }

    public void setTrailerURL(String trailerURL) {
        this.trailerURL = trailerURL;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public void setPosterURL(String posterURL) {
        this.posterURL = posterURL;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDateTime releaseDate) {
        this.releaseDate = releaseDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public BigDecimal getRatingScore() {
        return ratingScore;
    }

    public void setRatingScore(BigDecimal ratingScore) {
        this.ratingScore = ratingScore;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }


    
}
