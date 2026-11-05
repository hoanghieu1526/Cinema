package controllers;

import dal.AdminsDAO;
import dal.BookingsDAO;
import dal.GenresDAO;
import dal.Movie_GenreDAO;
import dal.MoviesDAO;
import dal.RoomsDAO;
import dal.SeatsDAO;
import dal.ShowtimesDAO;
import dal.TicketsDAO;
import dal.UsersDAO;
import enums.AdminRole;
import enums.BookingStatus;
import enums.RoomStatus;
import enums.ShowtimeStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import models.Admins;
import models.Bookings;
import models.Genres;
import models.Movie_Genre;
import models.Movies;
import models.Rooms;
import models.Seats;
import models.Showtimes;
import models.Tickets;
import models.Users;

/**
 * Trang quản trị /admin.
 *
 * GET  /admin : hiển thị dashboard (chỉ khi đã đăng nhập bằng tài khoản trong bảng Admins).
 * POST /admin : thực hiện 1 thao tác theo tham số "action" rồi redirect về lại /admin#<mục>.
 *
 * Phân quyền (AdminRole):
 *  - MOVIES_MANAGER : quản lý phim, thể loại, suất chiếu.
 *  - SUPER_ADMIN    : làm được mọi việc, và là role duy nhất được quản lý phòng chiếu,
 *                     đơn đặt vé, khách hàng (xem SUPER_ADMIN_ACTIONS).
 */
@WebServlet(name = "AdminServlet", urlPatterns = {"/admin"})
public class AdminServlet extends HttpServlet {

    private static final String SESSION_ADMIN = "currentAdmin";
    private static final String SESSION_CSRF = "adminCsrfToken";
    private static final String SESSION_FLASH_TYPE = "adminFlashType";
    private static final String SESSION_FLASH_MSG = "adminFlashMessage";

    private static final String DB_FAIL = "Không ghi được vào cơ sở dữ liệu. Vui lòng thử lại hoặc xem log máy chủ.";

    private static final int CLEANUP_MINUTES = 15;   // thời gian dọn phòng giữa 2 suất chiếu liên tiếp
    private static final int MAX_ROWS = 15;          // giới hạn khi tạo phòng mới
    private static final int MAX_SEATS_PER_ROW = 20;

    private static final List<String> AGE_RATINGS = Arrays.asList("P", "K", "T13", "T16", "T18", "C");
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final SecureRandom RANDOM = new SecureRandom();

    // action hợp lệ -> mục (id thẻ HTML) sẽ cuộn tới sau khi xử lý xong
    private static final Map<String, String> ACTION_SECTION = new HashMap<>();
    // action chỉ SUPER_ADMIN được làm
    private static final Set<String> SUPER_ADMIN_ACTIONS = new HashSet<>(Arrays.asList(
            "addRoom", "updateRoomStatus", "updateBookingStatus", "deleteUser"));
    // trạng thái đích được phép theo từng trạng thái hiện tại (dùng cho cả kiểm tra lẫn giao diện)
    private static final Map<ShowtimeStatus, List<ShowtimeStatus>> SHOWTIME_NEXT = new EnumMap<>(ShowtimeStatus.class);
    private static final Map<BookingStatus, List<BookingStatus>> BOOKING_NEXT = new EnumMap<>(BookingStatus.class);

    static {
        ACTION_SECTION.put("addMovie", "movies");
        ACTION_SECTION.put("updateMovie", "movies");
        ACTION_SECTION.put("deleteMovie", "movies");
        ACTION_SECTION.put("addGenre", "genres");
        ACTION_SECTION.put("deleteGenre", "genres");
        ACTION_SECTION.put("addShowtime", "showtimes");
        ACTION_SECTION.put("updateShowtimeStatus", "showtimes");
        ACTION_SECTION.put("deleteShowtime", "showtimes");
        ACTION_SECTION.put("addRoom", "rooms");
        ACTION_SECTION.put("updateRoomStatus", "rooms");
        ACTION_SECTION.put("updateBookingStatus", "bookings");
        ACTION_SECTION.put("deleteUser", "users");

        SHOWTIME_NEXT.put(ShowtimeStatus.UPCOMING, Arrays.asList(ShowtimeStatus.PLAYING, ShowtimeStatus.ENDED, ShowtimeStatus.CANCELLED));
        SHOWTIME_NEXT.put(ShowtimeStatus.PLAYING, Arrays.asList(ShowtimeStatus.ENDED, ShowtimeStatus.CANCELLED));
        SHOWTIME_NEXT.put(ShowtimeStatus.ENDED, Collections.<ShowtimeStatus>emptyList());
        SHOWTIME_NEXT.put(ShowtimeStatus.CANCELLED, Collections.<ShowtimeStatus>emptyList());

        BOOKING_NEXT.put(BookingStatus.PENDING, Arrays.asList(BookingStatus.COMPLETED, BookingStatus.CANCELLED));
        BOOKING_NEXT.put(BookingStatus.COMPLETED, Arrays.asList(BookingStatus.CANCELLED));
        BOOKING_NEXT.put(BookingStatus.CANCELLED, Collections.<BookingStatus>emptyList());
    }

    /** Lỗi do dữ liệu/thao tác không hợp lệ; thông điệp được hiển thị thẳng cho admin. */
    private static class ValidationException extends RuntimeException {
        ValidationException(String message) {
            super(message);
        }
    }

    private final AdminsDAO adminsDAO = new AdminsDAO();
    private final MoviesDAO moviesDAO = new MoviesDAO();
    private final GenresDAO genresDAO = new GenresDAO();
    private final Movie_GenreDAO movieGenreDAO = new Movie_GenreDAO();
    private final ShowtimesDAO showtimesDAO = new ShowtimesDAO();
    private final RoomsDAO roomsDAO = new RoomsDAO();
    private final SeatsDAO seatsDAO = new SeatsDAO();
    private final BookingsDAO bookingsDAO = new BookingsDAO();
    private final TicketsDAO ticketsDAO = new TicketsDAO();
    private final UsersDAO usersDAO = new UsersDAO();

    // ------------------------------------------------------------------ GET

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Admins admin = requireAdmin(request, response);
        if (admin == null) {
            return;
        }
        HttpSession session = request.getSession();
        response.setHeader("Cache-Control", "no-store");

        // Thông báo kết quả của thao tác POST ngay trước đó: hiện 1 lần rồi xoá.
        request.setAttribute("flashType", session.getAttribute(SESSION_FLASH_TYPE));
        request.setAttribute("flashMessage", session.getAttribute(SESSION_FLASH_MSG));
        session.removeAttribute(SESSION_FLASH_TYPE);
        session.removeAttribute(SESSION_FLASH_MSG);

        List<Movies> moviesList = moviesDAO.getAllMovies();
        List<Genres> genresList = genresDAO.getAllGenres();
        List<Showtimes> showtimesList = showtimesDAO.getAllShowtimes();
        List<Rooms> roomsList = roomsDAO.getAllRooms();
        List<Bookings> bookingsList = bookingsDAO.getAllBookings();
        List<Tickets> ticketsList = ticketsDAO.getAllTickets();
        List<Users> usersList = usersDAO.getAllUsers();

        // Suất chiếu xếp theo giờ chiếu, đơn đặt vé mới nhất lên đầu.
        showtimesList.sort((a, b) -> a.getStartTime().compareTo(b.getStartTime()));
        Collections.reverse(bookingsList);

        Map<Integer, Movies> movieMap = new HashMap<>();
        for (Movies m : moviesList) {
            movieMap.put(m.getMovieID(), m);
        }
        Map<Integer, Showtimes> showtimeMap = new HashMap<>();
        for (Showtimes st : showtimesList) {
            showtimeMap.put(st.getShowtimeID(), st);
        }
        Map<Integer, Users> userMap = new HashMap<>();
        for (Users u : usersList) {
            userMap.put(u.getUserID(), u);
        }

        // movieID -> "1,4,7" (các genreID), để giao diện tick sẵn thể loại khi sửa phim
        Map<Integer, String> movieGenreIds = new HashMap<>();
        for (Movie_Genre mg : movieGenreDAO.getAllMovieGenres()) {
            String current = movieGenreIds.get(mg.getMovieID());
            movieGenreIds.put(mg.getMovieID(),
                    current == null ? String.valueOf(mg.getGenreID()) : current + "," + mg.getGenreID());
        }

        Map<Integer, Rooms> roomMap = new HashMap<>();
        int standbyRoomsCount = 0;
        int maintRoomsCount = 0;
        int closedRoomsCount = 0;
        for (Rooms r : roomsList) {
            roomMap.put(r.getRoomID(), r);
            if (r.getStatus() == RoomStatus.STANDBY) {
                standbyRoomsCount++;
            } else if (r.getStatus() == RoomStatus.MAINTENANCE) {
                maintRoomsCount++;
            } else if (r.getStatus() == RoomStatus.CLOSED) {
                closedRoomsCount++;
            }
        }
        Map<Integer, Integer> seatCountMap = new HashMap<>();
        for (Seats s : seatsDAO.getAllSeats()) {
            Integer n = seatCountMap.get(s.getRoomID());
            seatCountMap.put(s.getRoomID(), n == null ? 1 : n + 1);
        }

        // Doanh thu và vé bán ra chỉ tính đơn COMPLETED (vé của đơn CANCELLED/PENDING không tính).
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int pendingBookingsCount = 0;
        Set<Integer> completedBookingIds = new HashSet<>();
        for (Bookings b : bookingsList) {
            if (b.getStatus() == BookingStatus.COMPLETED) {
                completedBookingIds.add(b.getBookingID());
                if (b.getTotalAmount() != null) {
                    totalRevenue = totalRevenue.add(b.getTotalAmount());
                }
            } else if (b.getStatus() == BookingStatus.PENDING) {
                pendingBookingsCount++;
            }
        }
        int soldTicketsCount = 0;
        Map<Integer, Integer> ticketCountMap = new HashMap<>();
        for (Tickets t : ticketsList) {
            Integer n = ticketCountMap.get(t.getBookingID());
            ticketCountMap.put(t.getBookingID(), n == null ? 1 : n + 1);
            if (completedBookingIds.contains(t.getBookingID())) {
                soldTicketsCount++;
            }
        }

        int activeShowtimesCount = 0;
        for (Showtimes st : showtimesList) {
            if (st.getStatus() == ShowtimeStatus.PLAYING || st.getStatus() == ShowtimeStatus.UPCOMING) {
                activeShowtimesCount++;
            }
        }

        request.setAttribute("currentAdmin", admin);
        request.setAttribute("isSuperAdmin", admin.getRoles() == AdminRole.SUPER_ADMIN);
        request.setAttribute("csrfToken", csrfToken(session));

        request.setAttribute("moviesList", moviesList);
        request.setAttribute("genresList", genresList);
        request.setAttribute("showtimesList", showtimesList);
        request.setAttribute("roomsList", roomsList);
        request.setAttribute("bookingsList", bookingsList);
        request.setAttribute("usersList", usersList);

        request.setAttribute("movieMap", movieMap);
        request.setAttribute("showtimeMap", showtimeMap);
        request.setAttribute("roomMap", roomMap);
        request.setAttribute("userMap", userMap);
        request.setAttribute("movieGenreIds", movieGenreIds);
        request.setAttribute("seatCountMap", seatCountMap);
        request.setAttribute("ticketCountMap", ticketCountMap);

        request.setAttribute("totalRevenue", totalRevenue);
        request.setAttribute("soldTicketsCount", soldTicketsCount);
        request.setAttribute("activeShowtimesCount", activeShowtimesCount);
        request.setAttribute("pendingBookingsCount", pendingBookingsCount);
        request.setAttribute("standbyRoomsCount", standbyRoomsCount);
        request.setAttribute("maintRoomsCount", maintRoomsCount);
        request.setAttribute("closedRoomsCount", closedRoomsCount);

        request.setAttribute("ageRatings", AGE_RATINGS);
        request.setAttribute("roomStatuses", RoomStatus.values());
        request.setAttribute("showtimeNext", SHOWTIME_NEXT);
        request.setAttribute("bookingNext", BOOKING_NEXT);

        request.getRequestDispatcher("admin.jsp").forward(request, response);
    }

    // ----------------------------------------------------------------- POST

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Admins admin = requireAdmin(request, response);
        if (admin == null) {
            return;
        }
        HttpSession session = request.getSession();

        String action = request.getParameter("action");
        String section = action == null ? null : ACTION_SECTION.get(action);

        try {
            if (!validCsrf(session, request.getParameter("csrf"))) {
                throw new ValidationException("Phiên làm việc không hợp lệ (CSRF). Hãy tải lại trang rồi thử lại.");
            }
            if (section == null) {
                throw new ValidationException("Hành động không hợp lệ.");
            }
            if (SUPER_ADMIN_ACTIONS.contains(action) && admin.getRoles() != AdminRole.SUPER_ADMIN) {
                throw new ValidationException("Bạn không có quyền thực hiện thao tác này (cần quyền SUPER_ADMIN).");
            }
            flash(session, "success", execute(action, request, admin));
        } catch (ValidationException e) {
            flash(session, "error", e.getMessage());
        } catch (Exception e) {
            log("AdminServlet: lỗi không mong đợi khi xử lý action=" + action, e);
            flash(session, "error", "Có lỗi hệ thống khi xử lý yêu cầu. Vui lòng thử lại.");
        }

        response.sendRedirect(request.getContextPath() + "/admin" + (section != null ? "#" + section : ""));
    }

    private String execute(String action, HttpServletRequest request, Admins admin) {
        switch (action) {
            case "addMovie":
                return saveMovie(request, admin, null);
            case "updateMovie":
                return saveMovie(request, admin, requireMovie(request));
            case "deleteMovie":
                return deleteMovie(requireMovie(request));
            case "addGenre":
                return addGenre(request);
            case "deleteGenre":
                return deleteGenre(request);
            case "addShowtime":
                return addShowtime(request);
            case "updateShowtimeStatus":
                return updateShowtimeStatus(request);
            case "deleteShowtime":
                return deleteShowtime(request);
            case "addRoom":
                return addRoom(request);
            case "updateRoomStatus":
                return updateRoomStatus(request);
            case "updateBookingStatus":
                return updateBookingStatus(request);
            case "deleteUser":
                return deleteUser(request);
            default:
                throw new ValidationException("Hành động không hợp lệ.");
        }
    }

    // ------------------------------------------------- đăng nhập / CSRF / flash

    /**
     * Trả về admin đang đăng nhập (đọc lại từ DB để role và việc tài khoản còn tồn tại luôn mới nhất).
     * Chưa đăng nhập thì redirect về trang chủ và trả về null.
     */
    private Admins requireAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Admins inSession = session == null ? null : (Admins) session.getAttribute(SESSION_ADMIN);
        if (inSession != null) {
            Admins fresh = adminsDAO.getAdminById(inSession.getAdminID());
            if (fresh != null) {
                session.setAttribute(SESSION_ADMIN, fresh);
                return fresh;
            }
            session.removeAttribute(SESSION_ADMIN);
        }
        response.sendRedirect(request.getContextPath() + "/home?error=admin_required");
        return null;
    }

    private String csrfToken(HttpSession session) {
        String token = (String) session.getAttribute(SESSION_CSRF);
        if (token == null) {
            byte[] bytes = new byte[24];
            RANDOM.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute(SESSION_CSRF, token);
        }
        return token;
    }

    private boolean validCsrf(HttpSession session, String submitted) {
        String expected = (String) session.getAttribute(SESSION_CSRF);
        return expected != null && submitted != null
                && MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), submitted.getBytes(StandardCharsets.UTF_8));
    }

    private void flash(HttpSession session, String type, String message) {
        session.setAttribute(SESSION_FLASH_TYPE, type);
        session.setAttribute(SESSION_FLASH_MSG, message);
    }

    // ---------------------------------------------------------------- Phim

    /** Thêm phim (existing == null) hoặc cập nhật phim đã có. */
    private String saveMovie(HttpServletRequest request, Admins admin, Movies existing) {
        String name = text(request, "movieName", "Tên phim", 200, true);
        String director = text(request, "director", "Đạo diễn", 150, true);
        String actor = text(request, "actor", "Diễn viên", 300, true);
        String bio = text(request, "bio", "Tóm tắt nội dung", 4000, true);
        String language = text(request, "language", "Ngôn ngữ", 50, true);
        int duration = intParam(request, "duration", "Thời lượng (phút)", 1, 600);
        BigDecimal ratingScore = decimalParam(request, "ratingScore", "Điểm đánh giá", 0, 10);
        String ageRating = text(request, "ageRating", "Độ tuổi", 5, true).toUpperCase(Locale.ROOT);
        if (!AGE_RATINGS.contains(ageRating)) {
            throw new ValidationException("Độ tuổi phải là một trong: " + AGE_RATINGS + ".");
        }
        String posterURL = urlParam(request, "posterURL", "Đường dẫn poster", true);
        String trailerURL = urlParam(request, "trailerURL", "Link trailer", false);
        LocalDateTime releaseDate = dateParam(request, "releaseDate", "Ngày khởi chiếu",
                existing != null ? existing.getReleaseDate() : LocalDateTime.now());

        // `category` (chuỗi) là thứ trang chủ dùng để lọc theo thể loại, nên luôn dựng lại từ các thể loại đã chọn.
        Map<Integer, String> chosenGenres = genreParams(request);
        String category = String.join(", ", chosenGenres.values());

        Movies movie = existing != null ? existing : new Movies();
        movie.setMovieName(name);
        movie.setDirector(director);
        movie.setActor(actor);
        movie.setBio(bio);
        movie.setLanguage(language);
        movie.setDuration(duration);
        movie.setRatingScore(ratingScore);
        movie.setAgeRating(ageRating);
        // Không nhập poster thì để trống: giao diện tự vẽ poster thay thế theo màu của phim.
        movie.setPosterURL(posterURL);
        movie.setTrailerURL(trailerURL);
        movie.setReleaseDate(releaseDate);
        movie.setCategory(category);

        if (existing == null) {
            movie.setApprovedByAdminID(admin.getAdminID());
            movie.setCreatedAt(LocalDateTime.now());
            movie.setRate(BigDecimal.ZERO);
            if (!moviesDAO.insertMovie(movie) || movie.getMovieID() <= 0) {
                throw new ValidationException(DB_FAIL);
            }
        } else {
            if (existing.getApprovedByAdminID() <= 0) {
                existing.setApprovedByAdminID(admin.getAdminID());
            }
            if (!moviesDAO.updateMovie(existing)) {
                throw new ValidationException(DB_FAIL);
            }
        }
        syncMovieGenres(movie.getMovieID(), chosenGenres.keySet());
        return (existing == null ? "Đã thêm phim \"" : "Đã cập nhật phim \"") + name + "\".";
    }

    private String deleteMovie(Movies movie) {
        Set<Integer> booked = showtimeIdsWithBookings();
        int removedShowtimes = 0;
        for (Showtimes st : showtimesDAO.getAllShowtimes()) {
            if (st.getMovieID() == movie.getMovieID()) {
                if (booked.contains(st.getShowtimeID())) {
                    throw new ValidationException("Phim \"" + movie.getMovieName() + "\" đã có đơn đặt vé ở suất chiếu #"
                            + st.getShowtimeID() + " nên không thể xoá. Hãy chuyển các suất chiếu của phim sang CANCELLED.");
                }
                removedShowtimes++;
            }
        }
        if (!moviesDAO.deleteMovie(movie.getMovieID())) {
            throw new ValidationException(DB_FAIL);
        }
        return "Đã xoá phim \"" + movie.getMovieName() + "\"" + (removedShowtimes > 0 ? " cùng " + removedShowtimes + " suất chiếu chưa có đơn." : ".");
    }

    /** Đọc các genreIDs được tick; trả về genreID -> tên, đúng thứ tự trong bảng Genres. */
    private Map<Integer, String> genreParams(HttpServletRequest request) {
        Set<Integer> wanted = new HashSet<>();
        String[] raw = request.getParameterValues("genreIDs");
        if (raw != null) {
            for (String s : raw) {
                try {
                    wanted.add(Integer.parseInt(s.trim()));
                } catch (NumberFormatException e) {
                    throw new ValidationException("Thể loại không hợp lệ.");
                }
            }
        }
        Map<Integer, String> chosen = new LinkedHashMap<>();
        for (Genres g : genresDAO.getAllGenres()) {
            if (wanted.remove(g.getGenreID())) {
                chosen.put(g.getGenreID(), g.getGenreName());
            }
        }
        if (!wanted.isEmpty()) {
            throw new ValidationException("Có thể loại không tồn tại (có thể vừa bị xoá). Hãy tải lại trang.");
        }
        if (chosen.isEmpty()) {
            throw new ValidationException("Hãy chọn ít nhất một thể loại cho phim.");
        }
        return chosen;
    }

    private void syncMovieGenres(int movieID, Set<Integer> selected) {
        Set<Integer> current = new HashSet<>();
        for (Movie_Genre mg : movieGenreDAO.getGenresByMovieId(movieID)) {
            current.add(mg.getGenreID());
        }
        for (Integer genreID : current) {
            if (!selected.contains(genreID)) {
                movieGenreDAO.deleteMovieGenre(movieID, genreID);
            }
        }
        for (Integer genreID : selected) {
            if (!current.contains(genreID)) {
                movieGenreDAO.insertMovieGenre(new Movie_Genre(movieID, genreID));
            }
        }
    }

    // ------------------------------------------------------------ Thể loại

    private String addGenre(HttpServletRequest request) {
        String name = text(request, "genreName", "Tên thể loại", 100, true);
        for (Genres g : genresDAO.getAllGenres()) {
            if (g.getGenreName().equalsIgnoreCase(name)) {
                throw new ValidationException("Thể loại \"" + g.getGenreName() + "\" đã tồn tại.");
            }
        }
        if (!genresDAO.insertGenre(new Genres(name))) {
            throw new ValidationException(DB_FAIL);
        }
        return "Đã thêm thể loại \"" + name + "\".";
    }

    private String deleteGenre(HttpServletRequest request) {
        int genreID = idParam(request, "genreID", "Mã thể loại");
        Genres genre = genresDAO.getGenreById(genreID);
        if (genre == null) {
            throw new ValidationException("Không tìm thấy thể loại #" + genreID + ".");
        }
        int used = 0;
        for (Movie_Genre mg : movieGenreDAO.getAllMovieGenres()) {
            if (mg.getGenreID() == genreID) {
                used++;
            }
        }
        if (used > 0) {
            throw new ValidationException("Thể loại \"" + genre.getGenreName() + "\" đang được dùng bởi " + used
                    + " phim. Hãy bỏ thể loại này khỏi các phim đó (nút Sửa) trước khi xoá.");
        }
        if (!genresDAO.deleteGenre(genreID)) {
            throw new ValidationException(DB_FAIL);
        }
        return "Đã xoá thể loại \"" + genre.getGenreName() + "\".";
    }

    // ------------------------------------------------------------ Suất chiếu

    private String addShowtime(HttpServletRequest request) {
        Movies movie = requireMovie(request);
        Rooms room = requireRoom(request);
        if (room.getStatus() != RoomStatus.STANDBY) {
            throw new ValidationException("Phòng \"" + room.getRoomName() + "\" đang ở trạng thái "
                    + room.getStatus() + ", không thể xếp suất chiếu.");
        }
        LocalDateTime start = dateTimeParam(request, "startTime", "Giờ bắt đầu");
        LocalDateTime end = start.plusMinutes(movie.getDuration());
        if (end.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Suất chiếu này đã kết thúc trong quá khứ, hãy chọn giờ khác.");
        }
        ShowtimeStatus status = enumParam(request, "status", "Trạng thái", ShowtimeStatus.class, ShowtimeStatus.UPCOMING);
        if (status != ShowtimeStatus.UPCOMING && status != ShowtimeStatus.PLAYING) {
            throw new ValidationException("Suất chiếu mới chỉ có thể ở trạng thái UPCOMING hoặc PLAYING.");
        }

        Showtimes conflict = findConflict(room.getRoomID(), start, end);
        if (conflict != null) {
            Movies other = moviesDAO.getMovieById(conflict.getMovieID());
            throw new ValidationException("Trùng lịch với suất chiếu #" + conflict.getShowtimeID() + " ("
                    + (other != null ? other.getMovieName() : "phim #" + conflict.getMovieID()) + ", "
                    + DISPLAY_TIME.format(conflict.getStartTime()) + " - " + DISPLAY_TIME.format(conflict.getEndTime())
                    + ") trong cùng phòng. Hai suất cần cách nhau ít nhất " + CLEANUP_MINUTES + " phút để dọn phòng.");
        }
        if (!showtimesDAO.insertShowtime(new Showtimes(movie.getMovieID(), room.getRoomID(), start, end, status))) {
            throw new ValidationException(DB_FAIL);
        }
        return "Đã tạo suất chiếu \"" + movie.getMovieName() + "\" lúc " + DISPLAY_TIME.format(start)
                + " tại " + room.getRoomName() + " (kết thúc " + DISPLAY_TIME.format(end) + ").";
    }

    /** Tìm suất chiếu (chưa huỷ) cùng phòng bị trùng giờ, tính cả thời gian dọn phòng. */
    private Showtimes findConflict(int roomID, LocalDateTime start, LocalDateTime end) {
        for (Showtimes s : showtimesDAO.getAllShowtimes()) {
            if (s.getRoomID() != roomID || s.getStatus() == ShowtimeStatus.CANCELLED) {
                continue;
            }
            boolean overlap = start.isBefore(s.getEndTime().plusMinutes(CLEANUP_MINUTES))
                    && s.getStartTime().isBefore(end.plusMinutes(CLEANUP_MINUTES));
            if (overlap) {
                return s;
            }
        }
        return null;
    }

    private String updateShowtimeStatus(HttpServletRequest request) {
        Showtimes showtime = requireShowtime(request);
        ShowtimeStatus target = enumParam(request, "status", "Trạng thái", ShowtimeStatus.class, null);
        if (!SHOWTIME_NEXT.get(showtime.getStatus()).contains(target)) {
            throw new ValidationException("Không thể chuyển suất chiếu #" + showtime.getShowtimeID()
                    + " từ " + showtime.getStatus() + " sang " + target + ".");
        }
        showtime.setStatus(target);
        if (!showtimesDAO.updateShowtime(showtime)) {
            throw new ValidationException(DB_FAIL);
        }
        String message = "Đã chuyển suất chiếu #" + showtime.getShowtimeID() + " sang " + target + ".";
        if (target == ShowtimeStatus.CANCELLED) {
            int open = 0;
            for (Bookings b : bookingsDAO.getAllBookings()) {
                if (b.getShowtimeID() == showtime.getShowtimeID() && b.getStatus() != BookingStatus.CANCELLED) {
                    open++;
                }
            }
            if (open > 0) {
                message += " Còn " + open + " đơn đặt vé chưa huỷ, hãy xử lý ở mục Đơn đặt vé.";
            }
        }
        return message;
    }

    private String deleteShowtime(HttpServletRequest request) {
        Showtimes showtime = requireShowtime(request);
        if (showtimeIdsWithBookings().contains(showtime.getShowtimeID())) {
            throw new ValidationException("Suất chiếu #" + showtime.getShowtimeID()
                    + " đã có đơn đặt vé nên không thể xoá. Hãy chuyển sang CANCELLED.");
        }
        if (!showtimesDAO.deleteShowtime(showtime.getShowtimeID())) {
            throw new ValidationException(DB_FAIL);
        }
        return "Đã xoá suất chiếu #" + showtime.getShowtimeID() + ".";
    }

    /** Các showtimeID đã có ít nhất 1 đơn đặt vé (mọi trạng thái; khoá ngoại Bookings chặn việc xoá). */
    private Set<Integer> showtimeIdsWithBookings() {
        Set<Integer> ids = new HashSet<>();
        for (Bookings b : bookingsDAO.getAllBookings()) {
            ids.add(b.getShowtimeID());
        }
        return ids;
    }

    // ----------------------------------------------------------- Phòng chiếu

    /** Tạo phòng và tự sinh lưới ghế A1..A{n}, B1..B{n}... */
    private String addRoom(HttpServletRequest request) {
        String name = text(request, "roomName", "Tên phòng", 50, true);
        String type = text(request, "roomType", "Loại phòng", 50, true);
        int rows = intParam(request, "rows", "Số hàng ghế", 1, MAX_ROWS);
        int perRow = intParam(request, "seatsPerRow", "Số ghế mỗi hàng", 1, MAX_SEATS_PER_ROW);

        for (Rooms r : roomsDAO.getAllRooms()) {
            if (r.getRoomName().equalsIgnoreCase(name)) {
                throw new ValidationException("Tên phòng \"" + r.getRoomName() + "\" đã tồn tại.");
            }
        }
        // Cột userID/showtimeID của Rooms không có khoá ngoại và không dùng đến, nên để 0.
        if (!roomsDAO.insertRoom(new Rooms(name, type, RoomStatus.STANDBY, 0, 0))) {
            throw new ValidationException(DB_FAIL);
        }
        Rooms created = null;
        for (Rooms r : roomsDAO.getAllRooms()) {   // roomName là UNIQUE nên tìm theo tên là chính xác
            if (r.getRoomName().equalsIgnoreCase(name)) {
                created = r;
            }
        }
        if (created == null) {
            throw new ValidationException(DB_FAIL);
        }

        List<Seats> seats = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int col = 1; col <= perRow; col++) {
                seats.add(new Seats("" + (char) ('A' + row) + col, created.getRoomID()));
            }
        }
        if (!seatsDAO.insertSeats(seats)) {
            roomsDAO.deleteRoom(created.getRoomID());   // không để lại phòng không có ghế
            throw new ValidationException("Không tạo được ghế nên đã huỷ việc tạo phòng. " + DB_FAIL);
        }
        return "Đã tạo phòng \"" + name + "\" với " + seats.size() + " ghế (" + rows + " hàng x " + perRow + " ghế).";
    }

    private String updateRoomStatus(HttpServletRequest request) {
        Rooms room = requireRoom(request);
        RoomStatus target = enumParam(request, "status", "Trạng thái", RoomStatus.class, null);
        if (room.getStatus() == target) {
            return "Phòng \"" + room.getRoomName() + "\" đã ở trạng thái " + target + ".";
        }
        if (target != RoomStatus.STANDBY) {
            int active = 0;
            LocalDateTime now = LocalDateTime.now();
            for (Showtimes s : showtimesDAO.getAllShowtimes()) {
                boolean live = s.getStatus() == ShowtimeStatus.UPCOMING || s.getStatus() == ShowtimeStatus.PLAYING;
                if (s.getRoomID() == room.getRoomID() && live && s.getEndTime().isAfter(now)) {
                    active++;
                }
            }
            if (active > 0) {
                throw new ValidationException("Phòng \"" + room.getRoomName() + "\" còn " + active
                        + " suất chiếu sắp/đang chiếu. Hãy huỷ hoặc chuyển các suất đó trước khi đổi sang " + target + ".");
            }
        }
        room.setStatus(target);
        if (!roomsDAO.updateRoom(room)) {
            throw new ValidationException(DB_FAIL);
        }
        return "Đã chuyển phòng \"" + room.getRoomName() + "\" sang " + target + ".";
    }

    // ------------------------------------------------- Đơn đặt vé / Khách hàng

    private String updateBookingStatus(HttpServletRequest request) {
        Bookings booking = requireBooking(request);
        BookingStatus target = enumParam(request, "status", "Trạng thái", BookingStatus.class, null);
        if (!BOOKING_NEXT.get(booking.getStatus()).contains(target)) {
            throw new ValidationException("Không thể chuyển đơn #" + booking.getBookingID()
                    + " từ " + booking.getStatus() + " sang " + target + ".");
        }
        booking.setStatus(target);
        if (!bookingsDAO.updateBooking(booking)) {
            throw new ValidationException(DB_FAIL);
        }
        return "Đã chuyển đơn đặt vé #" + booking.getBookingID() + " sang " + target
                + (target == BookingStatus.CANCELLED ? " (ghế của đơn đã được giải phóng)." : ".");
    }

    private String deleteUser(HttpServletRequest request) {
        int userID = idParam(request, "userID", "Mã khách hàng");
        Users user = usersDAO.getUserById(userID);
        if (user == null) {
            throw new ValidationException("Không tìm thấy khách hàng #" + userID + ".");
        }
        for (Bookings b : bookingsDAO.getAllBookings()) {
            if (b.getUserID() == userID) {
                throw new ValidationException("Khách hàng \"" + user.getFullName()
                        + "\" đã có đơn đặt vé nên không thể xoá (cần giữ lại dữ liệu doanh thu).");
            }
        }
        if (!usersDAO.deleteUser(userID)) {
            throw new ValidationException(DB_FAIL);
        }
        return "Đã xoá khách hàng \"" + user.getFullName() + "\".";
    }

    // ------------------------------------------------- tìm bản ghi theo tham số

    private Movies requireMovie(HttpServletRequest request) {
        int id = idParam(request, "movieID", "Mã phim");
        Movies movie = moviesDAO.getMovieById(id);
        if (movie == null) {
            throw new ValidationException("Không tìm thấy phim #" + id + " (có thể đã bị xoá).");
        }
        return movie;
    }

    private Rooms requireRoom(HttpServletRequest request) {
        int id = idParam(request, "roomID", "Mã phòng");
        Rooms room = roomsDAO.getRoomById(id);
        if (room == null) {
            throw new ValidationException("Không tìm thấy phòng #" + id + ".");
        }
        return room;
    }

    private Showtimes requireShowtime(HttpServletRequest request) {
        int id = idParam(request, "showtimeID", "Mã suất chiếu");
        Showtimes showtime = showtimesDAO.getShowtimeById(id);
        if (showtime == null) {
            throw new ValidationException("Không tìm thấy suất chiếu #" + id + ".");
        }
        return showtime;
    }

    private Bookings requireBooking(HttpServletRequest request) {
        int id = idParam(request, "bookingID", "Mã đơn đặt vé");
        Bookings booking = bookingsDAO.getBookingById(id);
        if (booking == null) {
            throw new ValidationException("Không tìm thấy đơn đặt vé #" + id + ".");
        }
        return booking;
    }

    // --------------------------------------------- đọc & kiểm tra tham số form

    private static String trimmed(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private static String text(HttpServletRequest request, String name, String label, int maxLength, boolean required) {
        String value = trimmed(request, name);
        if (value.isEmpty()) {
            if (required) {
                throw new ValidationException(label + " không được để trống.");
            }
            return null;
        }
        if (value.length() > maxLength) {
            throw new ValidationException(label + " tối đa " + maxLength + " ký tự.");
        }
        return value;
    }

    private static int idParam(HttpServletRequest request, String name, String label) {
        try {
            int id = Integer.parseInt(trimmed(request, name));
            if (id >= 1) {
                return id;
            }
        } catch (NumberFormatException ignored) {
        }
        throw new ValidationException(label + " không hợp lệ.");
    }

    private static int intParam(HttpServletRequest request, String name, String label, int min, int max) {
        String value = trimmed(request, name);
        if (value.isEmpty()) {
            throw new ValidationException(label + " không được để trống.");
        }
        int number;
        try {
            number = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ValidationException(label + " phải là số nguyên.");
        }
        if (number < min || number > max) {
            throw new ValidationException(label + " phải từ " + min + " đến " + max + ".");
        }
        return number;
    }

    private static BigDecimal decimalParam(HttpServletRequest request, String name, String label, int min, int max) {
        String value = trimmed(request, name);
        if (value.isEmpty()) {
            throw new ValidationException(label + " không được để trống.");
        }
        BigDecimal number;
        try {
            number = new BigDecimal(value.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new ValidationException(label + " phải là một số.");
        }
        if (number.compareTo(BigDecimal.valueOf(min)) < 0 || number.compareTo(BigDecimal.valueOf(max)) > 0) {
            throw new ValidationException(label + " phải từ " + min + " đến " + max + ".");
        }
        return number.setScale(2, RoundingMode.HALF_UP);
    }

    /** Chỉ cho http(s)://; với ảnh (allowRelative) còn cho đường dẫn tương đối. Chặn javascript:, data:... */
    private static String urlParam(HttpServletRequest request, String name, String label, boolean allowRelative) {
        String value = text(request, name, label, 500, false);
        if (value == null) {
            return null;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        boolean http = lower.startsWith("http://") || lower.startsWith("https://");
        boolean hasScheme = lower.matches("^[a-z][a-z0-9+.-]*:.*") || lower.startsWith("//");
        if (http || (allowRelative && !hasScheme)) {
            return value;
        }
        throw new ValidationException(label + " phải bắt đầu bằng http:// hoặc https://"
                + (allowRelative ? " (hoặc là đường dẫn tương đối như images/anh.jpg)." : "."));
    }

    /** Ngày dạng yyyy-MM-dd (input type="date"); để trống thì dùng giá trị mặc định. */
    private static LocalDateTime dateParam(HttpServletRequest request, String name, String label, LocalDateTime fallback) {
        String value = trimmed(request, name);
        if (value.isEmpty()) {
            return fallback;
        }
        try {
            return LocalDate.parse(value).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new ValidationException(label + " không hợp lệ.");
        }
    }

    /** Ngày giờ dạng yyyy-MM-ddTHH:mm (input type="datetime-local"). */
    private static LocalDateTime dateTimeParam(HttpServletRequest request, String name, String label) {
        String value = trimmed(request, name);
        if (value.isEmpty()) {
            throw new ValidationException(label + " không được để trống.");
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            throw new ValidationException(label + " không hợp lệ.");
        }
    }

    private static <E extends Enum<E>> E enumParam(HttpServletRequest request, String name, String label,
            Class<E> type, E fallback) {
        String value = trimmed(request, name);
        if (value.isEmpty()) {
            if (fallback != null) {
                return fallback;
            }
            throw new ValidationException(label + " không được để trống.");
        }
        try {
            return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ValidationException(label + " không hợp lệ.");
        }
    }
}
