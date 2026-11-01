/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import enums.ShowtimeStatus;
import java.time.LocalDateTime;

/**
 *
 * @author NGUYEN HOANG HIEU
 */
public class Showtimes {
    private int showtimeID;
    private int movieID;
    private int roomID;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ShowtimeStatus status;

    public Showtimes(int showtimeID, int movieID, int roomID, LocalDateTime startTime, LocalDateTime endTime, ShowtimeStatus status) {
        this.showtimeID = showtimeID;
        this.movieID = movieID;
        this.roomID = roomID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public int getShowtimeID() {
        return showtimeID;
    }

    public void setShowtimeID(int showtimeID) {
        this.showtimeID = showtimeID;
    }

    public int getMovieID() {
        return movieID;
    }

    public void setMovieID(int movieID) {
        this.movieID = movieID;
    }

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public ShowtimeStatus getStatus() {
        return status;
    }

    public void setStatus(ShowtimeStatus status) {
        this.status = status;
    }
    
    
}
