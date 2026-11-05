package controllers;

import dal.BookingsDAO;
import dal.MoviesDAO;
import dal.RoomsDAO;
import dal.SeatsDAO;
import dal.ShowtimesDAO;
import dal.TicketsDAO;
import enums.BookingStatus;
import enums.ShowtimeStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.BookingView;
import models.Bookings;
import models.Movies;
import models.Rooms;
import models.Seats;
import models.Showtimes;
import models.Tickets;
import models.Users;

/**
 * "Vé của tôi": chỉ hiển thị và xử lý đơn của khách đang đăng nhập.
 *
 * GET  /tickets                         : danh sách đơn của tôi (?booked=ID để báo đặt vé thành công)
 * POST /tickets  action=cancel|pay      : huỷ đơn / thanh toán đơn đang chờ (bookingID phải là đơn của chính mình)
 */
@WebServlet(name = "TicketsServlet", urlPatterns = {"/tickets"})
public class TicketsServlet extends HttpServlet {

    private final BookingsDAO bookingsDAO = new BookingsDAO();
    private final TicketsDAO ticketsDAO = new TicketsDAO();
    private final ShowtimesDAO showtimesDAO = new ShowtimesDAO();
    private final MoviesDAO moviesDAO = new MoviesDAO();
    private final RoomsDAO roomsDAO = new RoomsDAO();
    private final SeatsDAO seatsDAO = new SeatsDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Users user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth?next=tickets");
            return;
        }

        Map<Integer, Movies> movieMap = new HashMap<>();
        for (Movies m : moviesDAO.getAllMovies()) {
            movieMap.put(m.getMovieID(), m);
        }
        Map<Integer, Showtimes> showtimeMap = new HashMap<>();
        for (Showtimes st : showtimesDAO.getAllShowtimes()) {
            showtimeMap.put(st.getShowtimeID(), st);
        }
        Map<Integer, Rooms> roomMap = new HashMap<>();
        for (Rooms r : roomsDAO.getAllRooms()) {
            roomMap.put(r.getRoomID(), r);
        }
        Map<Integer, Seats> seatMap = new HashMap<>();
        for (Seats s : seatsDAO.getAllSeats()) {
            seatMap.put(s.getSeatID(), s);
        }

        List<BookingView> views = new ArrayList<>();
        for (Bookings b : bookingsDAO.getBookingsByUserId(user.getUserID())) {
            Showtimes st = showtimeMap.get(b.getShowtimeID());
            List<String> names = new ArrayList<>();
            for (Tickets t : ticketsDAO.getTicketsByBookingId(b.getBookingID())) {
                Seats s = seatMap.get(t.getSeatID());
                names.add(s != null ? s.getSeatName() : "#" + t.getSeatID());
            }
            views.add(new BookingView(
                    b,
                    st == null ? null : movieMap.get(st.getMovieID()),
                    st,
                    st == null ? null : roomMap.get(st.getRoomID()),
                    String.join(", ", names),
                    names.size()));
        }

        request.setAttribute("bookings", views);
        request.setAttribute("bookedId", request.getParameter("booked"));
        request.setAttribute("msg", request.getParameter("msg"));
        request.getRequestDispatcher("/tickets.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Users user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth?next=tickets");
            return;
        }

        String result = "error";
        try {
            Bookings b = bookingsDAO.getBookingById(Integer.parseInt(request.getParameter("bookingID")));
            // Chỉ chủ đơn mới được thao tác; đơn của người khác coi như không tồn tại.
            if (b != null && b.getUserID() == user.getUserID()) {
                String action = request.getParameter("action");
                Showtimes st = showtimesDAO.getShowtimeById(b.getShowtimeID());
                boolean showtimeOpen = st != null && st.getStatus() == ShowtimeStatus.UPCOMING;

                if ("cancel".equals(action) && b.getStatus() != BookingStatus.CANCELLED && showtimeOpen) {
                    b.setStatus(BookingStatus.CANCELLED);
                    result = bookingsDAO.updateBooking(b) ? "cancelled" : "error";
                } else if ("pay".equals(action) && b.getStatus() == BookingStatus.PENDING && showtimeOpen) {
                    b.setStatus(BookingStatus.COMPLETED);
                    result = bookingsDAO.updateBooking(b) ? "paid" : "error";
                }
            }
        } catch (NumberFormatException ignored) {
            // bookingID thiếu hoặc sai định dạng
        }
        response.sendRedirect(request.getContextPath() + "/tickets?msg=" + result);
    }

    private Users currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (Users) session.getAttribute("currentUser");
    }
}
