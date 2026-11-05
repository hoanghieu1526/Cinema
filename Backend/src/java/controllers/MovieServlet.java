package controllers;

import dal.MoviesDAO;
import dal.RoomsDAO;
import dal.SeatsDAO;
import dal.ShowtimesDAO;
import dal.TicketsDAO;
import enums.ShowtimeStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import models.Movies;
import models.Rooms;
import models.Seats;
import models.Showtimes;
import util.Fmt;
import util.Pricing;

/** Trang chi tiết phim /movie?id=... kèm các suất chiếu đang mở bán, gom theo ngày. */
@WebServlet(name = "MovieServlet", urlPatterns = {"/movie"})
public class MovieServlet extends HttpServlet {

    private final MoviesDAO moviesDAO = new MoviesDAO();
    private final ShowtimesDAO showtimesDAO = new ShowtimesDAO();
    private final RoomsDAO roomsDAO = new RoomsDAO();
    private final SeatsDAO seatsDAO = new SeatsDAO();
    private final TicketsDAO ticketsDAO = new TicketsDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Movies movie = null;
        try {
            movie = moviesDAO.getMovieById(Integer.parseInt(request.getParameter("id")));
        } catch (NumberFormatException ignored) {
            // thiếu hoặc sai mã phim -> xử lý như không tìm thấy
        }
        if (movie == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        List<Showtimes> open = new ArrayList<>();
        for (Showtimes st : showtimesDAO.getAllShowtimes()) {
            if (st.getMovieID() == movie.getMovieID()
                    && (st.getStatus() == ShowtimeStatus.UPCOMING || st.getStatus() == ShowtimeStatus.PLAYING)) {
                open.add(st);
            }
        }
        open.sort((a, b) -> a.getStartTime().compareTo(b.getStartTime()));

        Map<Integer, Rooms> roomMap = new HashMap<>();
        for (Rooms r : roomsDAO.getAllRooms()) {
            roomMap.put(r.getRoomID(), r);
        }
        Map<Integer, Integer> seatsInRoom = new HashMap<>();
        for (Seats s : seatsDAO.getAllSeats()) {
            Integer n = seatsInRoom.get(s.getRoomID());
            seatsInRoom.put(s.getRoomID(), n == null ? 1 : n + 1);
        }
        Map<Integer, Integer> booked = ticketsDAO.getBookedCountByShowtime();

        // Mỗi suất: số ghế còn trống và giá vé; gom các suất theo ngày để hiển thị.
        Map<Integer, Integer> seatsLeft = new HashMap<>();
        Map<Integer, BigDecimal> price = new HashMap<>();
        Map<String, List<Showtimes>> byDay = new LinkedHashMap<>();
        for (Showtimes st : open) {
            Integer total = seatsInRoom.get(st.getRoomID());
            Integer taken = booked.get(st.getShowtimeID());
            seatsLeft.put(st.getShowtimeID(), Math.max(0, (total == null ? 0 : total) - (taken == null ? 0 : taken)));
            price.put(st.getShowtimeID(), Pricing.seatPrice(roomMap.get(st.getRoomID())));

            String day = Fmt.day(st.getStartTime());
            List<Showtimes> list = byDay.get(day);
            if (list == null) {
                list = new ArrayList<>();
                byDay.put(day, list);
            }
            list.add(st);
        }

        request.setAttribute("movie", movie);
        request.setAttribute("showtimesByDay", byDay);
        request.setAttribute("roomMap", roomMap);
        request.setAttribute("seatsLeft", seatsLeft);
        request.setAttribute("price", price);
        request.setAttribute("hasShowtimes", !open.isEmpty());
        request.getRequestDispatcher("/movie.jsp").forward(request, response);
    }
}
