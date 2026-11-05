package controllers;

import dal.GenresDAO;
import dal.Movie_GenreDAO;
import dal.MoviesDAO;
import dal.ShowtimesDAO;
import enums.ShowtimeStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import models.Movie_Genre;
import models.Movies;
import models.Showtimes;
import util.Fmt;

/**
 * Trang chủ /home: banner phim nổi bật và danh sách phim.
 *
 * Tham số lọc (tuỳ chọn): tab = now | soon (mặc định: tất cả), genre = mã thể loại, q = từ khoá.
 * Trạng thái của phim:
 *   NOW  - đang có suất chiếu mở bán (UPCOMING hoặc PLAYING)
 *   SOON - chưa có suất chiếu và ngày khởi chiếu còn ở tương lai
 *   END  - các trường hợp còn lại (đã hết suất chiếu)
 */
@WebServlet(name = "HomeServlet", urlPatterns = {"/home"})
public class HomeServlet extends HttpServlet {

    private final MoviesDAO moviesDAO = new MoviesDAO();
    private final GenresDAO genresDAO = new GenresDAO();
    private final ShowtimesDAO showtimesDAO = new ShowtimesDAO();
    private final Movie_GenreDAO movieGenreDAO = new Movie_GenreDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String tab = request.getParameter("tab");
        if (!"now".equals(tab) && !"soon".equals(tab)) {
            tab = "all";
        }
        String q = request.getParameter("q") == null ? "" : request.getParameter("q").trim();
        int genreID = parseInt(request.getParameter("genre"));

        List<Movies> allMovies = moviesDAO.getAllMovies();

        Set<Integer> hasOpenShowtime = new HashSet<>();
        for (Showtimes st : showtimesDAO.getAllShowtimes()) {
            if (st.getStatus() == ShowtimeStatus.UPCOMING || st.getStatus() == ShowtimeStatus.PLAYING) {
                hasOpenShowtime.add(st.getMovieID());
            }
        }

        LocalDateTime now = LocalDateTime.now();
        Map<Integer, String> movieStatus = new HashMap<>();
        int nowCount = 0;
        int soonCount = 0;
        for (Movies m : allMovies) {
            String status;
            if (hasOpenShowtime.contains(m.getMovieID())) {
                status = "NOW";
                nowCount++;
            } else if (m.getReleaseDate() != null && m.getReleaseDate().isAfter(now)) {
                status = "SOON";
                soonCount++;
            } else {
                status = "END";
            }
            movieStatus.put(m.getMovieID(), status);
        }

        Set<Integer> inGenre = null;
        if (genreID > 0) {
            inGenre = new HashSet<>();
            for (Movie_Genre mg : movieGenreDAO.getAllMovieGenres()) {
                if (mg.getGenreID() == genreID) {
                    inGenre.add(mg.getMovieID());
                }
            }
        }

        String keyword = Fmt.fold(q);
        List<Movies> movies = new ArrayList<>();
        for (Movies m : allMovies) {
            String status = movieStatus.get(m.getMovieID());
            if ("now".equals(tab) && !"NOW".equals(status)) {
                continue;
            }
            if ("soon".equals(tab) && !"SOON".equals(status)) {
                continue;
            }
            if (inGenre != null && !inGenre.contains(m.getMovieID())) {
                continue;
            }
            if (!keyword.isEmpty()) {
                String haystack = Fmt.fold(m.getMovieName() + " " + m.getDirector() + " " + m.getActor());
                if (!haystack.contains(keyword)) {
                    continue;
                }
            }
            movies.add(m);
        }
        // Phim đang chiếu lên đầu, trong cùng nhóm thì phim điểm cao hơn xếp trước.
        movies.sort((a, b) -> {
            int byStatus = rank(movieStatus.get(a.getMovieID())) - rank(movieStatus.get(b.getMovieID()));
            if (byStatus != 0) {
                return byStatus;
            }
            if (a.getRatingScore() == null || b.getRatingScore() == null) {
                return 0;
            }
            return b.getRatingScore().compareTo(a.getRatingScore());
        });

        // Banner chỉ hiện ở trang chủ "sạch" (không lọc/tìm kiếm): phim đang chiếu điểm cao nhất.
        Movies hero = null;
        boolean filtering = !"all".equals(tab) || genreID > 0 || !q.isEmpty();
        if (!filtering) {
            for (Movies m : movies) {
                if ("NOW".equals(movieStatus.get(m.getMovieID()))) {
                    hero = m;
                    break;
                }
            }
        }

        request.setAttribute("movies", movies);
        request.setAttribute("heroMovie", hero);
        request.setAttribute("movieStatus", movieStatus);
        request.setAttribute("genres", genresDAO.getAllGenres());
        request.setAttribute("tab", tab);
        request.setAttribute("genreID", genreID);
        request.setAttribute("q", q);
        request.setAttribute("qParam", URLEncoder.encode(q, "UTF-8")); // dùng trong link giữ nguyên từ khoá
        request.setAttribute("totalCount", allMovies.size());
        request.setAttribute("nowCount", nowCount);
        request.setAttribute("soonCount", soonCount);
        request.getRequestDispatcher("/home.jsp").forward(request, response);
    }

    private static int rank(String status) {
        return "NOW".equals(status) ? 0 : ("SOON".equals(status) ? 1 : 2);
    }

    private static int parseInt(String s) {
        try {
            return s == null ? 0 : Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
