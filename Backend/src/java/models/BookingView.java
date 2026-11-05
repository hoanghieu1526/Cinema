package models;

/** Một đơn đặt vé cùng thông tin phim / suất / phòng / ghế để hiển thị ở trang "Vé của tôi". */
public class BookingView {

    private final Bookings booking;
    private final Movies movie;
    private final Showtimes showtime;
    private final Rooms room;
    private final String seatNames;
    private final int seatCount;

    public BookingView(Bookings booking, Movies movie, Showtimes showtime, Rooms room, String seatNames, int seatCount) {
        this.booking = booking;
        this.movie = movie;
        this.showtime = showtime;
        this.room = room;
        this.seatNames = seatNames;
        this.seatCount = seatCount;
    }

    public Bookings getBooking() {
        return booking;
    }

    public Movies getMovie() {
        return movie;
    }

    public Showtimes getShowtime() {
        return showtime;
    }

    public Rooms getRoom() {
        return room;
    }

    public String getSeatNames() {
        return seatNames;
    }

    public int getSeatCount() {
        return seatCount;
    }
}
