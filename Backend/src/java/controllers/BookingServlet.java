package controllers;

import dal.BookingsDAO;
import dal.MoviesDAO;
import dal.RoomsDAO;
import dal.SeatsDAO;
import dal.ShowtimesDAO;
import dal.TicketsDAO;
import dal.UsersDAO;
import enums.ShowtimeStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import models.Movies;
import models.Rooms;
import models.Seats;
import models.Showtimes;
import models.Users;
import util.Pricing;

/**
 * Chọn ghế và đặt vé cho một suất chiếu.
 *
 * GET  /booking?showtimeID=...   : sơ đồ ghế của suất chiếu (xem được khi chưa đăng nhập).
 * POST /booking                  : showtimeID + nhiều seatID, cần đăng nhập. Server tự tính giá và kiểm tra ghế.
 */
@WebServlet(name = "BookingServlet", urlPatterns = {"/booking"})
public class BookingServlet extends HttpServlet {

    private static final Pattern SEAT_NAME = Pattern.compile("^([A-Za-z]+)(\\d{1,6})$");

    private final MoviesDAO moviesDAO = new MoviesDAO();
    private final ShowtimesDAO showtimesDAO = new ShowtimesDAO();
    private final RoomsDAO roomsDAO = new RoomsDAO();
    private final SeatsDAO seatsDAO = new SeatsDAO();
    private final TicketsDAO ticketsDAO = new TicketsDAO();
    private final BookingsDAO bookingsDAO = new BookingsDAO();
    private final UsersDAO usersDAO = new UsersDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Showtimes showtime = findShowtime(request.getParameter("showtimeID"));
        if (showtime == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Movies movie = moviesDAO.getMovieById(showtime.getMovieID());
        Rooms room = roomsDAO.getRoomById(showtime.getRoomID());
        if (movie == null || room == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        boolean open = isOpen(showtime);
        Set<Integer> bookedSeatIds = ticketsDAO.getBookedSeatIds(showtime.getShowtimeID());

        // Các suất chiếu khác của cùng phim để đổi nhanh.
        List<Showtimes> others = new ArrayList<>();
        for (Showtimes st : showtimesDAO.getAllShowtimes()) {
            if (st.getMovieID() == movie.getMovieID() && isOpen(st)) {
                others.add(st);
            }
        }
        others.sort((a, b) -> a.getStartTime().compareTo(b.getStartTime()));

        List<Seats> seats = seatsDAO.getSeatsByRoomId(room.getRoomID());

        request.setAttribute("movie", movie);
        request.setAttribute("showtime", showtime);
        request.setAttribute("room", room);
        request.setAttribute("open", open);
        request.setAttribute("seatRows", groupByRow(seats));
        request.setAttribute("seatCount", seats.size());
        request.setAttribute("bookedSeatIds", bookedSeatIds);
        request.setAttribute("seatsLeft", Math.max(0, seats.size() - bookedSeatIds.size()));
        request.setAttribute("price", Pricing.seatPrice(room));
        request.setAttribute("maxSeats", Pricing.MAX_SEATS_PER_BOOKING);
        request.setAttribute("otherShowtimes", others);
        request.setAttribute("error", errorMessage(request.getParameter("error")));
        request.getRequestDispatcher("/booking.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Showtimes showtime = findShowtime(request.getParameter("showtimeID"));
        if (showtime == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String self = request.getContextPath() + "/booking?showtimeID=" + showtime.getShowtimeID();

        // Phải đăng nhập bằng tài khoản khách hàng (và tài khoản đó còn tồn tại trong DB).
        HttpSession session = request.getSession(false);
        Users user = session == null ? null : (Users) session.getAttribute("currentUser");
        if (user != null && usersDAO.getUserById(user.getUserID()) == null) {
            session.invalidate();
            user = null;
        }
        if (user == null) {
            String next = URLEncoder.encode("booking?showtimeID=" + showtime.getShowtimeID(), "UTF-8");
            response.sendRedirect(request.getContextPath() + "/auth?next=" + next);
            return;
        }

        if (!isOpen(showtime)) {
            response.sendRedirect(self + "&error=closed");
            return;
        }

        // Danh sách ghế đã chọn (bỏ trùng, bỏ giá trị không phải số).
        Set<Integer> seatIds = new LinkedHashSet<>();
        String[] raw = request.getParameterValues("seatID");
        if (raw != null) {
            for (String s : raw) {
                try {
                    seatIds.add(Integer.parseInt(s.trim()));
                } catch (NumberFormatException ignored) {
                    // bỏ qua giá trị rác
                }
            }
        }
        if (seatIds.isEmpty()) {
            response.sendRedirect(self + "&error=none");
            return;
        }
        if (seatIds.size() > Pricing.MAX_SEATS_PER_BOOKING) {
            response.sendRedirect(self + "&error=toomany");
            return;
        }

        Rooms room = roomsDAO.getRoomById(showtime.getRoomID());
        int result = bookingsDAO.createBooking(user.getUserID(), showtime.getShowtimeID(), showtime.getRoomID(),
                new ArrayList<>(seatIds), Pricing.seatPrice(room));

        if (result > 0) {
            response.sendRedirect(request.getContextPath() + "/tickets?booked=" + result);
        } else if (result == BookingsDAO.SEAT_TAKEN) {
            response.sendRedirect(self + "&error=taken");
        } else if (result == BookingsDAO.SEAT_INVALID) {
            response.sendRedirect(self + "&error=invalid");
        } else {
            response.sendRedirect(self + "&error=failed");
        }
    }

    private Showtimes findShowtime(String idText) {
        try {
            return showtimesDAO.getShowtimeById(Integer.parseInt(idText));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Chỉ suất UPCOMING / PLAYING mới còn bán vé.
    private static boolean isOpen(Showtimes st) {
        return st.getStatus() == ShowtimeStatus.UPCOMING || st.getStatus() == ShowtimeStatus.PLAYING;
    }

    /** Nhóm ghế theo hàng (A1, A2 -> hàng "A"; VIP1, VIP2 -> hàng "VIP"), mỗi hàng xếp theo số ghế. */
    private static Map<String, List<Seats>> groupByRow(List<Seats> seats) {
        List<Seats> sorted = new ArrayList<>(seats);
        Collections.sort(sorted, new Comparator<Seats>() {
            @Override
            public int compare(Seats a, Seats b) {
                String ra = rowOf(a.getSeatName());
                String rb = rowOf(b.getSeatName());
                int byRow = ra.compareTo(rb);
                if (byRow != 0) {
                    return byRow;
                }
                int byNumber = Integer.compare(numberOf(a.getSeatName()), numberOf(b.getSeatName()));
                return byNumber != 0 ? byNumber : Integer.compare(a.getSeatID(), b.getSeatID());
            }
        });
        Map<String, List<Seats>> rows = new LinkedHashMap<>();
        for (Seats s : sorted) {
            String row = rowOf(s.getSeatName());
            List<Seats> list = rows.get(row);
            if (list == null) {
                list = new ArrayList<>();
                rows.put(row, list);
            }
            list.add(s);
        }
        return rows;
    }

    private static String rowOf(String seatName) {
        Matcher m = SEAT_NAME.matcher(seatName == null ? "" : seatName);
        return m.matches() ? m.group(1).toUpperCase() : "?";
    }

    private static int numberOf(String seatName) {
        Matcher m = SEAT_NAME.matcher(seatName == null ? "" : seatName);
        return m.matches() ? Integer.parseInt(m.group(2)) : 0;
    }

    private static String errorMessage(String code) {
        if (code == null) {
            return null;
        }
        switch (code) {
            case "none":
                return "Hãy chọn ít nhất một ghế trống.";
            case "toomany":
                return "Mỗi lần đặt tối đa " + Pricing.MAX_SEATS_PER_BOOKING + " ghế.";
            case "taken":
                return "Có ghế vừa được người khác đặt. Vui lòng chọn lại ghế khác.";
            case "invalid":
                return "Ghế đã chọn không thuộc phòng chiếu này.";
            case "closed":
                return "Suất chiếu này đã đóng bán vé.";
            default:
                return "Không đặt được vé. Vui lòng thử lại.";
        }
    }
}
