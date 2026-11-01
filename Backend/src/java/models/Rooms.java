/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import enums.RoomStatus;

/**
 *
 * @author NGUYEN HOANG HIEU
 */
public class Rooms {
    private int roomID;
    private String roomName;
    private String roomType;
    private RoomStatus status;
    private int userID;
    private int showtimeID;

    public Rooms(int roomID, String roomName, String roomType, RoomStatus status, int userID, int showtimeID) {
        this.roomID = roomID;
        this.roomName = roomName;
        this.roomType = roomType;
        this.status = status;
        this.userID = userID;
        this.showtimeID = showtimeID;
    }

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getShowtimeID() {
        return showtimeID;
    }

    public void setShowtimeID(int showtimeID) {
        this.showtimeID = showtimeID;
    }
    
    
}
