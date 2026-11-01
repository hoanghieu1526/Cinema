/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import java.math.BigDecimal;

/**
 *
 * @author NGUYEN HOANG HIEU
 */
public class Tickets {
    private int ticketID;
    private BigDecimal price;
    private int roomID;
    private int seatID;
    private int bookingID;

    public Tickets() {
    }

    public Tickets(int ticketID, BigDecimal price, int roomID, int seatID, int bookingID) {
        this.ticketID = ticketID;
        this.price = price;
        this.roomID = roomID;
        this.seatID = seatID;
        this.bookingID = bookingID;
    }

    public Tickets(BigDecimal price, int roomID, int seatID, int bookingID) {
        this.price = price;
        this.roomID = roomID;
        this.seatID = seatID;
        this.bookingID = bookingID;
    }

    public int getTicketID() {
        return ticketID;
    }

    public void setTicketID(int ticketID) {
        this.ticketID = ticketID;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public int getSeatID() {
        return seatID;
    }

    public void setSeatID(int seatID) {
        this.seatID = seatID;
    }

    public int getBookingID() {
        return bookingID;
    }

    public void setBookingID(int bookingID) {
        this.bookingID = bookingID;
    }

    @Override
    public String toString() {
        return "Tickets{" + "ticketID=" + ticketID + ", price=" + price + ", roomID=" + roomID + ", seatID=" + seatID + ", bookingID=" + bookingID + '}';
    }
}
