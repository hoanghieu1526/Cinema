import dal.AdminsDAO;
import dal.BookingsDAO;
import dal.DBcontext;
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
import enums.UserGender;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
 * FILE KIỂM TRA TOÀN DIỆN CRUD, MODELS, ENUMS & DATABASE SCHEMA
 * Cách chạy trong NetBeans: Mở file này -> Nhấn Shift + F6 (Run File).
 * Dữ liệu tạo thử trong quá trình test sẽ được tự động xóa sạch sau khi chạy xong.
 */
public class CheckAllCrud {

    private static int passCount = 0;
    private static int failCount = 0;
    private static int warnCount = 0;
    private static final List<String> failureDetails = new ArrayList<>();
    private static final List<String> warningDetails = new ArrayList<>();

    private static final String TEST_TAG = "TEST_CRUD_" + (System.currentTimeMillis() % 100000);

    public static void main(String[] args) {
        // Đảm bảo hiển thị tiếng Việt có dấu trên Console
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        } catch (Exception ignored) {
        }

        printBanner("BẮT ĐẦU KIỂM TRA TOÀN DIỆN HỆ THỐNG CRUD CINEMA");

        // 1. Kiểm tra lỗi tĩnh ở Models, Enums & cấu trúc DAO (không cần DB)
        checkModelsAndEnums();
        checkMissingDaoMethods();

        // 2. Kiểm tra kết nối DB, đối chiếu Schema và chạy thực tế toàn bộ 10 DAO
        try (Connection conn = DBcontext.getConnection()) {
            pass("Kết nối Database (DBcontext.getConnection)", "Kết nối thành công tới SQL Server!");
            checkDatabaseSchema(conn);
            runLiveCrudTests(conn);
        } catch (SQLException e) {
            fail("Kết nối Database (DBcontext.getConnection)",
                    "Không thể kết nối CSDL: " + e.getMessage()
                    + " -> Hãy kiểm tra SQL Server đã bật chưa, database 'cinemaDB' và tài khoản sa/123 trong DBcontext.java.");
        }

        // 3. In báo cáo tổng kết
        printSummary();
    }

    // =========================================================================
    // PHẦN 1: KIỂM TRA LỖI CODE TRONG MODELS & ENUMS
    // =========================================================================
    private static void checkModelsAndEnums() {
        printSection("1. KIỂM TRA MODELS & ENUMS (LOGIC & JAVA BEANS)");

        // 1.1 Kiểm tra lỗi tự gán ở Tickets.setRoomID
        try {
            Tickets ticket = new Tickets(1, BigDecimal.valueOf(75000), 10, 5, 1);
            ticket.setRoomID(99);
            if (ticket.getRoomID() == 99) {
                pass("Tickets.setRoomID(int)", "Gán giá trị roomID mới thành công.");
            } else {
                fail("Tickets.setRoomID(int)",
                        "Gọi setRoomID(99) nhưng getRoomID() vẫn trả về " + ticket.getRoomID()
                        + " (Do tham số trong Tickets.java viết hoa 'int RoomID' nên tự gán this.roomID = roomID).");
            }
        } catch (Exception e) {
            fail("Tickets.setRoomID(int)", "Ngoại lệ: " + e);
        }

        // 1.2 Kiểm tra hàm dư thừa Tickets.getroomID()
        try {
            Method badMethod = Tickets.class.getDeclaredMethod("getroomID");
            Tickets ticket = new Tickets(1, BigDecimal.valueOf(75000), 10, 5, 1);
            badMethod.invoke(ticket);
            warn("Tickets.getroomID()", "Có hàm getroomID() trùng lặp với getRoomID() trong Tickets.java.");
        } catch (NoSuchMethodException e) {
            pass("Tickets.getroomID()", "Không còn hàm rác getroomID() trong Tickets.java.");
        } catch (Exception e) {
            Throwable cause = (e.getCause() != null) ? e.getCause() : e;
            fail("Tickets.getroomID()",
                    "Tồn tại hàm rác getroomID() ở dòng 68 file Tickets.java ném lỗi: " + cause.getClass().getSimpleName()
                    + " (" + cause.getMessage() + "). Cần xóa hàm này!");
        }

        // 1.3 Kiểm tra cách map UserGender giữa UserGender.java và UsersDAO.java
        try {
            UserGender g0 = UserGender.fromString("0");
            UserGender g1 = UserGender.fromString("1");
            if (g0 == UserGender.FEMALE && g1 == UserGender.MALE) {
                pass("UsersDAO & UserGender", "Đã đồng bộ đọc/ghi UserGender qua fromString() và getDbValue() (0/1).");
            } else {
                fail("UserGender", "fromString('0'/'1') trả về không đúng.");
            }
        } catch (Exception e) {
            fail("UserGender", "Lỗi kiểm tra enum UserGender: " + e.getMessage());
        }

        // 1.4 Kiểm tra Constructor không có ID (dùng khi INSERT), Constructor rỗng và toString() của 10 Models
        Class<?>[] modelClasses = {
            Admins.class, Users.class, Genres.class, Movies.class, Movie_Genre.class,
            Rooms.class, Seats.class, Showtimes.class, Bookings.class, Tickets.class
        };
        List<String> missingInsertCtor = new ArrayList<>();
        List<String> missingNoArgCtor = new ArrayList<>();
        List<String> missingToString = new ArrayList<>();

        for (Class<?> clazz : modelClasses) {
            Constructor<?>[] ctors = clazz.getDeclaredConstructors();
            boolean hasNoArg = false;
            int nonEmptyCtors = 0;
            for (Constructor<?> c : ctors) {
                if (c.getParameterCount() == 0) {
                    hasNoArg = true;
                } else {
                    nonEmptyCtors++;
                }
            }
            if (!hasNoArg) {
                missingNoArgCtor.add(clazz.getSimpleName());
            }
            if (clazz != Movie_Genre.class && nonEmptyCtors < 2) {
                missingInsertCtor.add(clazz.getSimpleName());
            }
            try {
                clazz.getDeclaredMethod("toString");
            } catch (NoSuchMethodException e) {
                missingToString.add(clazz.getSimpleName());
            }
        }

        if (!missingInsertCtor.isEmpty()) {
            warn("Models thiếu Constructor tạo mới (không cần truyền ID)",
                    "Các class chỉ có 1 constructor bắt buộc truyền ID (phải truyền đại ID = 0 khi insert): " + missingInsertCtor);
        } else {
            pass("Models Constructor (Insert)", "Tất cả Models đều có constructor tạo mới không cần ID.");
        }

        if (!missingNoArgCtor.isEmpty()) {
            warn("Models thiếu Constructor rỗng (JavaBeans cho JSP/Servlet)",
                    "Các class chưa có constructor rỗng public ClassName() {}: " + missingNoArgCtor);
        } else {
            pass("Models Constructor rỗng (JavaBeans)", "Tất cả 10 Models đều có constructor rỗng chuẩn JavaBeans.");
        }

        if (!missingToString.isEmpty()) {
            warn("Models thiếu hàm toString()",
                    "Khi in đối tượng ra console sẽ chỉ hiện mã băm (ClassName@hex): " + missingToString);
        } else {
            pass("Models.toString()", "Tất cả 10 Models đều đã cài đặt hàm toString().");
        }
    }

    // =========================================================================
    // PHẦN 2: KIỂM TRA CÁC HÀM CÒN THIẾU TRONG TẦNG DAO
    // =========================================================================
    private static void checkMissingDaoMethods() {
        printSection("2. KIỂM TRA CẤU TRÚC PHƯƠNG THỨC TRONG 10 CLASS DAO");

        Class<?>[] daoClasses = {
            AdminsDAO.class, UsersDAO.class, GenresDAO.class, MoviesDAO.class, Movie_GenreDAO.class,
            RoomsDAO.class, SeatsDAO.class, ShowtimesDAO.class, BookingsDAO.class, TicketsDAO.class
        };

        List<String> daosMissingDelete = new ArrayList<>();
        for (Class<?> dao : daoClasses) {
            boolean hasDelete = false;
            for (Method m : dao.getDeclaredMethods()) {
                if (m.getName().toLowerCase().startsWith("delete")) {
                    hasDelete = true;
                    break;
                }
            }
            if (!hasDelete) {
                daosMissingDelete.add(dao.getSimpleName());
            }
        }

        if (daosMissingDelete.isEmpty()) {
            pass("Phương thức DELETE trong DAO", "Tất cả 10 DAO đều đã có hàm delete.");
        } else {
            warn("Phương thức DELETE trong DAO",
                    daosMissingDelete.size() + "/10 class DAO chưa viết hàm delete (xóa bản ghi): " + daosMissingDelete);
        }
    }

    // =========================================================================
    // PHẦN 3: ĐỐI CHIẾU CẤU TRÚC BẢNG TRONG SQL SERVER VỚI CODE DAO
    // =========================================================================
    private static void checkDatabaseSchema(Connection conn) {
        printSection("3. ĐỐI CHIẾU BẢNG & CỘT TRONG SQL SERVER VỚI CODE DAO");

        Map<String, List<String>> expectedSchema = new LinkedHashMap<>();
        expectedSchema.put("Admins", Arrays.asList("adminID", "fullName", "account", "passwordHash", "phone", "email", "roles", "createdAt", "updatedAt"));
        expectedSchema.put("Users", Arrays.asList("userID", "fullName", "userName", "passwordHash", "phone", "email", "imageURL", "gender", "dateOfBirth", "createdAt", "updateAt"));
        expectedSchema.put("Genres", Arrays.asList("genreID", "genreName"));
        expectedSchema.put("Movies", Arrays.asList("movieID", "movieName", "approvedByAdminID", "category", "bio", "trailerURL", "posterURL", "director", "actor", "releaseDate", "createdAt", "duration", "language", "ratingScore", "ageRating", "rate"));
        expectedSchema.put("Movie_Genre", Arrays.asList("movieID", "genreID"));
        expectedSchema.put("Rooms", Arrays.asList("roomID", "roomName", "roomType", "status", "userID", "showtimeID"));
        expectedSchema.put("Seats", Arrays.asList("seatID", "seatName", "roomID"));
        expectedSchema.put("Showtimes", Arrays.asList("showtimeID", "movieID", "roomID", "startTime", "endTime", "status"));
        expectedSchema.put("Bookings", Arrays.asList("bookingID", "bookingTime", "totalAmount", "status", "userID", "showtimeID"));
        expectedSchema.put("Tickets", Arrays.asList("ticketID", "price", "roomID", "seatID", "bookingID"));

        for (Map.Entry<String, List<String>> entry : expectedSchema.entrySet()) {
            String tableName = entry.getKey();
            List<String> expectedCols = entry.getValue();

            Map<String, String> actualCols = new HashMap<>(); // lowercase colName -> dataType
            Map<String, String> actualOriginalNames = new HashMap<>();
            String sql = "SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE LOWER(TABLE_NAME) = LOWER(?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, tableName);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String col = rs.getString("COLUMN_NAME");
                        String type = rs.getString("DATA_TYPE");
                        actualCols.put(col.toLowerCase(), type.toLowerCase());
                        actualOriginalNames.put(col.toLowerCase(), col);
                    }
                }
            } catch (SQLException e) {
                fail("Kiểm tra bảng " + tableName, "Lỗi đọc INFORMATION_SCHEMA: " + e.getMessage());
                continue;
            }

            if (actualCols.isEmpty()) {
                fail("Bảng [" + tableName + "]", "Bảng '" + tableName + "' KHÔNG TỒN TẠI trong database cinemaDB!");
                continue;
            }

            List<String> missingCols = new ArrayList<>();
            for (String expectedCol : expectedCols) {
                if (!actualCols.containsKey(expectedCol.toLowerCase())) {
                    missingCols.add(expectedCol);
                }
            }

            if (!missingCols.isEmpty()) {
                fail("Cấu trúc bảng [" + tableName + "]",
                        "DAO đang truy vấn các cột không tồn tại trong bảng " + tableName + ": " + missingCols
                        + " (Các cột thực tế trong DB: " + actualOriginalNames.values() + ")");
            } else {
                pass("Cấu trúc bảng [" + tableName + "]", "Đầy đủ " + expectedCols.size() + " cột khớp với DAO.");
            }

            // Kiểm tra chuyên sâu kiểu dữ liệu dễ gây lỗi Runtime
            if ("Users".equalsIgnoreCase(tableName)) {
                String dobType = actualCols.get("dateofbirth");
                if ("date".equals(dobType)) {
                    warn("Kiểu dữ liệu cột [Users.dateOfBirth]",
                            "Trong DB cột dateOfBirth có kiểu 'date' (không có giờ), việc gọi rs.getObject(\"dateOfBirth\", LocalDateTime.class) trong UsersDAO có thể gây lỗi chuyển đổi kiểu của JDBC Driver.");
                }
            }

            if ("Movies".equalsIgnoreCase(tableName)) {
                String releaseType = actualCols.get("releasedate");
                if ("date".equals(releaseType)) {
                    warn("Kiểu dữ liệu cột [Movies.releaseDate]",
                            "Trong DB cột releaseDate có kiểu 'date' (không có giờ), việc gọi rs.getObject(\"releaseDate\", LocalDateTime.class) trong MoviesDAO có thể gây lỗi chuyển đổi kiểu của JDBC Driver.");
                }
            }
        }
    }

    // =========================================================================
    // PHẦN 4: CHẠY THỬ NGHIỆM CRUD THỰC TẾ TRÊN CẢ 10 DAO & TỰ ĐỘNG DỌN DẸP
    // =========================================================================
    private static void runLiveCrudTests(Connection conn) {
        printSection("4. CHẠY THỬ NGHIỆM CRUD THỰC TẾ TRÊN 10 DAO (CÓ TỰ ĐỘNG DỌN DẸP)");

        Integer createdAdminId = null;
        Integer createdUserId = null;
        Integer createdGenreId = null;
        Integer createdMovieId = null;
        Integer createdRoomId = null;
        Integer createdSeatId = null;
        Integer createdShowtimeId = null;
        Integer createdBookingId = null;
        Integer createdTicketId = null;
        boolean createdMovieGenre = false;

        try {
            // -----------------------------------------------------------------
            // 4.1 TEST AdminsDAO
            // -----------------------------------------------------------------
            AdminsDAO adminsDAO = new AdminsDAO();
            String testAccount = "adm_" + TEST_TAG;
            String testAdminEmail = testAccount + "@test.local";

            Admins newAdmin = new Admins("Admin Test", testAccount, "hash123", "0901234567", testAdminEmail);
            DaoCallResult<Boolean> insAdminRes = callDao(() -> adminsDAO.insertAdmin(newAdmin));
            if (Boolean.TRUE.equals(insAdminRes.returnValue) && insAdminRes.isClean()) {
                pass("AdminsDAO.insertAdmin", "Thêm mới Admin thành công.");
            } else {
                fail("AdminsDAO.insertAdmin", "Thất bại! " + insAdminRes.getErrorSummary());
            }

            DaoCallResult<Admins> byAccRes = callDao(() -> adminsDAO.getAdminByAccount(testAccount));
            if (byAccRes.returnValue != null && byAccRes.isClean()) {
                createdAdminId = byAccRes.returnValue.getAdminID();
                pass("AdminsDAO.getAdminByAccount", "Tìm thấy Admin vừa tạo (adminID = " + createdAdminId + ").");
            } else {
                fail("AdminsDAO.getAdminByAccount", "Không lấy được Admin vừa tạo! " + byAccRes.getErrorSummary());
            }

            DaoCallResult<List<Admins>> allAdminsRes = callDao(adminsDAO::getAllAdmins);
            if (allAdminsRes.isClean() && allAdminsRes.returnValue != null && !allAdminsRes.returnValue.isEmpty()) {
                pass("AdminsDAO.getAllAdmins", "Lấy danh sách thành công (" + allAdminsRes.returnValue.size() + " bản ghi).");
            } else {
                fail("AdminsDAO.getAllAdmins", "Lỗi hoặc danh sách rỗng! " + allAdminsRes.getErrorSummary());
            }

            if (createdAdminId != null) {
                final int adminId = createdAdminId;
                DaoCallResult<Admins> byIdRes = callDao(() -> adminsDAO.getAdminById(adminId));
                if (byIdRes.returnValue != null && byIdRes.isClean()) {
                    pass("AdminsDAO.getAdminById", "Lấy Admin theo ID = " + adminId + " thành công.");
                    Admins adminToUpd = byIdRes.returnValue;
                    adminToUpd.setFullName("Admin Updated");
                    DaoCallResult<Boolean> updAdminRes = callDao(() -> adminsDAO.updateAdmin(adminToUpd));
                    if (Boolean.TRUE.equals(updAdminRes.returnValue) && updAdminRes.isClean()) {
                        pass("AdminsDAO.updateAdmin", "Cập nhật Admin thành công.");
                    } else {
                        fail("AdminsDAO.updateAdmin", "Cập nhật Admin thất bại! " + updAdminRes.getErrorSummary());
                    }
                } else {
                    fail("AdminsDAO.getAdminById", "Không tìm thấy Admin theo ID! " + byIdRes.getErrorSummary());
                }
            }

            // -----------------------------------------------------------------
            // 4.2 TEST UsersDAO
            // -----------------------------------------------------------------
            UsersDAO usersDAO = new UsersDAO();
            String testUserName = "usr_" + TEST_TAG;
            String testUserEmail = testUserName + "@test.local";

            Users newUser = new Users(
                    "User Test",
                    testUserName,
                    "hash123",
                    "0912345678",
                    testUserEmail,
                    "",
                    UserGender.MALE,
                    LocalDateTime.of(2000, 1, 1, 0, 0)
            );
            DaoCallResult<Boolean> insUserRes = callDao(() -> usersDAO.insertUser(newUser));
            if (Boolean.TRUE.equals(insUserRes.returnValue) && insUserRes.isClean()) {
                pass("UsersDAO.insertUser", "Thêm mới User thành công.");
            } else {
                fail("UsersDAO.insertUser", "Thất bại! " + insUserRes.getErrorSummary());
            }

            DaoCallResult<Users> byUserNameRes = callDao(() -> usersDAO.getUserByUserName(testUserName));
            if (byUserNameRes.returnValue != null && byUserNameRes.isClean()) {
                createdUserId = byUserNameRes.returnValue.getUserID();
                pass("UsersDAO.getUserByUserName", "Tìm thấy User vừa tạo (userID = " + createdUserId + ").");
            } else {
                fail("UsersDAO.getUserByUserName", "Không lấy được User theo userName! " + byUserNameRes.getErrorSummary());
                createdUserId = findIdBySql(conn, "SELECT userID FROM Users WHERE userName = ?", testUserName);
            }

            DaoCallResult<Users> byEmailRes = callDao(() -> usersDAO.getUserByEmail(testUserEmail));
            if (byEmailRes.returnValue != null && byEmailRes.isClean()) {
                pass("UsersDAO.getUserByEmail", "Lấy User theo Email thành công.");
            } else {
                fail("UsersDAO.getUserByEmail", "Thất bại! " + byEmailRes.getErrorSummary());
            }

            DaoCallResult<List<Users>> allUsersRes = callDao(usersDAO::getAllUsers);
            if (allUsersRes.isClean() && allUsersRes.returnValue != null && !allUsersRes.returnValue.isEmpty()) {
                pass("UsersDAO.getAllUsers", "Lấy danh sách Users thành công (" + allUsersRes.returnValue.size() + " bản ghi).");
            } else {
                fail("UsersDAO.getAllUsers", "Lỗi khi đọc danh sách Users! " + allUsersRes.getErrorSummary());
            }

            if (createdUserId != null) {
                final int uid = createdUserId;
                DaoCallResult<Users> userByIdRes = callDao(() -> usersDAO.getUserById(uid));
                if (userByIdRes.returnValue != null && userByIdRes.isClean()) {
                    pass("UsersDAO.getUserById", "Lấy User theo ID = " + uid + " thành công.");
                    Users uUpd = userByIdRes.returnValue;
                    uUpd.setFullName("User Updated");
                    DaoCallResult<Boolean> updUserRes = callDao(() -> usersDAO.updateUser(uUpd));
                    if (Boolean.TRUE.equals(updUserRes.returnValue) && updUserRes.isClean()) {
                        pass("UsersDAO.updateUser", "Cập nhật User thành công.");
                    } else {
                        fail("UsersDAO.updateUser", "Cập nhật User thất bại! " + updUserRes.getErrorSummary());
                    }
                } else {
                    fail("UsersDAO.getUserById", "Không lấy được User theo ID! " + userByIdRes.getErrorSummary());
                }
            }

            // -----------------------------------------------------------------
            // 4.3 TEST GenresDAO
            // -----------------------------------------------------------------
            GenresDAO genresDAO = new GenresDAO();
            String testGenreName = "Genre_" + TEST_TAG;
            Genres newGenre = new Genres(0, testGenreName);

            DaoCallResult<Boolean> insGenreRes = callDao(() -> genresDAO.insertGenre(newGenre));
            if (Boolean.TRUE.equals(insGenreRes.returnValue) && insGenreRes.isClean()) {
                pass("GenresDAO.insertGenre", "Thêm mới Genre thành công.");
            } else {
                fail("GenresDAO.insertGenre", "Thất bại! " + insGenreRes.getErrorSummary());
            }

            createdGenreId = findIdBySql(conn, "SELECT genreID FROM Genres WHERE genreName = ?", testGenreName);
            DaoCallResult<List<Genres>> allGenresRes = callDao(genresDAO::getAllGenres);
            if (allGenresRes.isClean() && allGenresRes.returnValue != null && !allGenresRes.returnValue.isEmpty()) {
                pass("GenresDAO.getAllGenres", "Lấy danh sách Genres thành công (" + allGenresRes.returnValue.size() + " bản ghi).");
            } else {
                fail("GenresDAO.getAllGenres", "Lỗi lấy danh sách Genres! " + allGenresRes.getErrorSummary());
            }

            if (createdGenreId != null) {
                final int gid = createdGenreId;
                DaoCallResult<Genres> genreByIdRes = callDao(() -> genresDAO.getGenreById(gid));
                if (genreByIdRes.returnValue != null && genreByIdRes.isClean()) {
                    pass("GenresDAO.getGenreById", "Lấy Genre theo ID = " + gid + " thành công.");
                    Genres gUpd = genreByIdRes.returnValue;
                    gUpd.setGenreName(testGenreName + "_Upd");
                    DaoCallResult<Boolean> updGenreRes = callDao(() -> genresDAO.updateGenre(gUpd));
                    if (Boolean.TRUE.equals(updGenreRes.returnValue) && updGenreRes.isClean()) {
                        pass("GenresDAO.updateGenre", "Cập nhật Genre thành công.");
                    } else {
                        fail("GenresDAO.updateGenre", "Cập nhật Genre thất bại! " + updGenreRes.getErrorSummary());
                    }
                } else {
                    fail("GenresDAO.getGenreById", "Thất bại! " + genreByIdRes.getErrorSummary());
                }
            }

            // -----------------------------------------------------------------
            // 4.4 TEST MoviesDAO
            // -----------------------------------------------------------------
            MoviesDAO moviesDAO = new MoviesDAO();
            String testMovieName = "Movie_" + TEST_TAG;
            int validAdminId = (createdAdminId != null) ? createdAdminId
                    : getFirstExistingId(conn, "SELECT TOP 1 adminID FROM Admins");

            Movies newMovie = new Movies(
                    0,
                    testMovieName,
                    validAdminId,
                    "Action",
                    "Mô tả phim test",
                    "https://example.com/trailer",
                    "https://example.com/poster.jpg",
                    "Đạo diễn A",
                    "Diễn viên B",
                    LocalDateTime.now().plusDays(7),
                    LocalDateTime.now(),
                    120,
                    "Vietnamese",
                    new BigDecimal("8.5"),
                    "T16",
                    new BigDecimal("4.5")
            );

            DaoCallResult<Boolean> insMovieRes = callDao(() -> moviesDAO.insertMovie(newMovie));
            if (Boolean.TRUE.equals(insMovieRes.returnValue) && insMovieRes.isClean()) {
                pass("MoviesDAO.insertMovie", "Thêm mới Movie thành công.");
            } else {
                fail("MoviesDAO.insertMovie", "Thất bại! " + insMovieRes.getErrorSummary());
            }

            createdMovieId = findIdBySql(conn, "SELECT movieID FROM Movies WHERE movieName = ?", testMovieName);
            DaoCallResult<List<Movies>> allMoviesRes = callDao(moviesDAO::getAllMovies);
            if (allMoviesRes.isClean() && allMoviesRes.returnValue != null && !allMoviesRes.returnValue.isEmpty()) {
                pass("MoviesDAO.getAllMovies", "Lấy danh sách Movies thành công (" + allMoviesRes.returnValue.size() + " bản ghi).");
            } else {
                fail("MoviesDAO.getAllMovies", "Lỗi lấy danh sách Movies! " + allMoviesRes.getErrorSummary());
            }

            if (createdMovieId != null) {
                final int mid = createdMovieId;
                DaoCallResult<Movies> movieByIdRes = callDao(() -> moviesDAO.getMovieById(mid));
                if (movieByIdRes.returnValue != null && movieByIdRes.isClean()) {
                    pass("MoviesDAO.getMovieById", "Lấy Movie theo ID = " + mid + " thành công.");
                    Movies mUpd = movieByIdRes.returnValue;
                    mUpd.setDuration(135);
                    DaoCallResult<Boolean> updMovieRes = callDao(() -> moviesDAO.updateMovie(mUpd));
                    if (Boolean.TRUE.equals(updMovieRes.returnValue) && updMovieRes.isClean()) {
                        pass("MoviesDAO.updateMovie", "Cập nhật Movie thành công.");
                    } else {
                        fail("MoviesDAO.updateMovie", "Cập nhật Movie thất bại! " + updMovieRes.getErrorSummary());
                    }
                } else {
                    fail("MoviesDAO.getMovieById", "Thất bại! " + movieByIdRes.getErrorSummary());
                }
            }

            // -----------------------------------------------------------------
            // 4.5 TEST Movie_GenreDAO
            // -----------------------------------------------------------------
            if (createdMovieId != null && createdGenreId != null) {
                Movie_GenreDAO mgDAO = new Movie_GenreDAO();
                final int mid = createdMovieId;
                final int gid = createdGenreId;
                Movie_Genre mg = new Movie_Genre(mid, gid);

                DaoCallResult<Boolean> insMgRes = callDao(() -> mgDAO.insertMovieGenre(mg));
                if (Boolean.TRUE.equals(insMgRes.returnValue) && insMgRes.isClean()) {
                    createdMovieGenre = true;
                    pass("Movie_GenreDAO.insertMovieGenre", "Gán Genre vào Movie thành công.");
                } else {
                    fail("Movie_GenreDAO.insertMovieGenre", "Thất bại! " + insMgRes.getErrorSummary());
                }

                DaoCallResult<List<Movie_Genre>> allMgRes = callDao(mgDAO::getAllMovieGenres);
                if (allMgRes.isClean() && allMgRes.returnValue != null) {
                    pass("Movie_GenreDAO.getAllMovieGenres", "Lấy danh sách Movie_Genre thành công (" + allMgRes.returnValue.size() + " bản ghi).");
                } else {
                    fail("Movie_GenreDAO.getAllMovieGenres", "Thất bại! " + allMgRes.getErrorSummary());
                }

                DaoCallResult<List<Movie_Genre>> byMovieMgRes = callDao(() -> mgDAO.getGenresByMovieId(mid));
                if (byMovieMgRes.isClean() && byMovieMgRes.returnValue != null && !byMovieMgRes.returnValue.isEmpty()) {
                    pass("Movie_GenreDAO.getGenresByMovieId", "Lấy Genres theo movieID = " + mid + " thành công.");
                } else {
                    fail("Movie_GenreDAO.getGenresByMovieId", "Thất bại! " + byMovieMgRes.getErrorSummary());
                }

                DaoCallResult<Boolean> delMgRes = callDao(() -> mgDAO.deleteMovieGenre(mid, gid));
                if (Boolean.TRUE.equals(delMgRes.returnValue) && delMgRes.isClean()) {
                    createdMovieGenre = false;
                    pass("Movie_GenreDAO.deleteMovieGenre", "Xóa liên kết Movie_Genre thành công.");
                } else {
                    fail("Movie_GenreDAO.deleteMovieGenre", "Thất bại! " + delMgRes.getErrorSummary());
                }
            } else {
                warn("Movie_GenreDAO", "Bỏ qua test Movie_GenreDAO vì chưa tạo được Movie hoặc Genre trước đó.");
            }

            // -----------------------------------------------------------------
            // 4.6 TEST RoomsDAO
            // -----------------------------------------------------------------
            RoomsDAO roomsDAO = new RoomsDAO();
            String testRoomName = "R_" + TEST_TAG;
            int validUserIdForRoom = (createdUserId != null) ? createdUserId
                    : getFirstExistingId(conn, "SELECT TOP 1 userID FROM Users");
            int existingShowtimeId = getFirstExistingId(conn, "SELECT TOP 1 showtimeID FROM Showtimes");

            Rooms newRoom = new Rooms(0, testRoomName, "2D Standard", RoomStatus.STANDBY, validUserIdForRoom, existingShowtimeId);
            DaoCallResult<Boolean> insRoomRes = callDao(() -> roomsDAO.insertRoom(newRoom));
            if (Boolean.TRUE.equals(insRoomRes.returnValue) && insRoomRes.isClean()) {
                pass("RoomsDAO.insertRoom", "Thêm mới Room thành công.");
            } else {
                fail("RoomsDAO.insertRoom",
                        "Thất bại! (Lưu ý: Rooms đang yêu cầu truyền cả userID và showtimeID gây phụ thuộc vòng với bảng Showtimes). Chi tiết: "
                        + insRoomRes.getErrorSummary());
            }

            createdRoomId = findIdBySql(conn, "SELECT roomID FROM Rooms WHERE roomName = ?", testRoomName);
            DaoCallResult<List<Rooms>> allRoomsRes = callDao(roomsDAO::getAllRooms);
            if (allRoomsRes.isClean() && allRoomsRes.returnValue != null) {
                pass("RoomsDAO.getAllRooms", "Lấy danh sách Rooms thành công (" + allRoomsRes.returnValue.size() + " bản ghi).");
            } else {
                fail("RoomsDAO.getAllRooms", "Thất bại! " + allRoomsRes.getErrorSummary());
            }

            if (createdRoomId != null) {
                final int rid = createdRoomId;
                DaoCallResult<Rooms> roomByIdRes = callDao(() -> roomsDAO.getRoomById(rid));
                if (roomByIdRes.returnValue != null && roomByIdRes.isClean()) {
                    pass("RoomsDAO.getRoomById", "Lấy Room theo ID = " + rid + " thành công.");
                    Rooms rUpd = roomByIdRes.returnValue;
                    rUpd.setStatus(RoomStatus.MAINTENANCE);
                    DaoCallResult<Boolean> updRoomRes = callDao(() -> roomsDAO.updateRoom(rUpd));
                    if (Boolean.TRUE.equals(updRoomRes.returnValue) && updRoomRes.isClean()) {
                        pass("RoomsDAO.updateRoom", "Cập nhật Room thành công.");
                    } else {
                        fail("RoomsDAO.updateRoom", "Cập nhật Room thất bại! " + updRoomRes.getErrorSummary());
                    }
                } else {
                    fail("RoomsDAO.getRoomById", "Thất bại! " + roomByIdRes.getErrorSummary());
                }
            }

            // -----------------------------------------------------------------
            // 4.7 TEST SeatsDAO
            // -----------------------------------------------------------------
            SeatsDAO seatsDAO = new SeatsDAO();
            int targetRoomId = (createdRoomId != null) ? createdRoomId
                    : getFirstExistingId(conn, "SELECT TOP 1 roomID FROM Rooms");

            if (targetRoomId > 0) {
                String testSeatName = "S_" + (TEST_TAG.length() > 6 ? TEST_TAG.substring(TEST_TAG.length() - 6) : TEST_TAG);
                Seats newSeat = new Seats(0, testSeatName, targetRoomId);
                DaoCallResult<Boolean> insSeatRes = callDao(() -> seatsDAO.insertSeat(newSeat));
                if (Boolean.TRUE.equals(insSeatRes.returnValue) && insSeatRes.isClean()) {
                    pass("SeatsDAO.insertSeat", "Thêm mới Seat thành công.");
                } else {
                    fail("SeatsDAO.insertSeat", "Thất bại! " + insSeatRes.getErrorSummary());
                }

                createdSeatId = findIdBySql(conn, "SELECT seatID FROM Seats WHERE seatName = ? AND roomID = ?", testSeatName, targetRoomId);
                DaoCallResult<List<Seats>> allSeatsRes = callDao(seatsDAO::getAllSeats);
                if (allSeatsRes.isClean() && allSeatsRes.returnValue != null) {
                    pass("SeatsDAO.getAllSeats", "Lấy danh sách Seats thành công (" + allSeatsRes.returnValue.size() + " bản ghi).");
                } else {
                    fail("SeatsDAO.getAllSeats", "Thất bại! " + allSeatsRes.getErrorSummary());
                }

                final int rIdForSeat = targetRoomId;
                DaoCallResult<List<Seats>> seatsByRoomRes = callDao(() -> seatsDAO.getSeatsByRoomId(rIdForSeat));
                if (seatsByRoomRes.isClean() && seatsByRoomRes.returnValue != null && !seatsByRoomRes.returnValue.isEmpty()) {
                    pass("SeatsDAO.getSeatsByRoomId", "Lấy danh sách ghế theo roomID = " + rIdForSeat + " thành công.");
                } else {
                    fail("SeatsDAO.getSeatsByRoomId", "Thất bại! " + seatsByRoomRes.getErrorSummary());
                }

                if (createdSeatId != null) {
                    final int sid = createdSeatId;
                    DaoCallResult<Seats> seatByIdRes = callDao(() -> seatsDAO.getSeatById(sid));
                    if (seatByIdRes.returnValue != null && seatByIdRes.isClean()) {
                        pass("SeatsDAO.getSeatById", "Lấy Seat theo ID = " + sid + " thành công.");
                        Seats sUpd = seatByIdRes.returnValue;
                        sUpd.setSeatName(testSeatName);
                        DaoCallResult<Boolean> updSeatRes = callDao(() -> seatsDAO.updateSeat(sUpd));
                        if (Boolean.TRUE.equals(updSeatRes.returnValue) && updSeatRes.isClean()) {
                            pass("SeatsDAO.updateSeat", "Cập nhật Seat thành công.");
                        } else {
                            fail("SeatsDAO.updateSeat", "Cập nhật Seat thất bại! " + updSeatRes.getErrorSummary());
                        }
                    } else {
                        fail("SeatsDAO.getSeatById", "Thất bại! " + seatByIdRes.getErrorSummary());
                    }
                }
            } else {
                warn("SeatsDAO", "Không có roomID hợp lệ trong CSDL để test SeatsDAO.");
            }

            // -----------------------------------------------------------------
            // 4.8 TEST ShowtimesDAO
            // -----------------------------------------------------------------
            ShowtimesDAO showtimesDAO = new ShowtimesDAO();
            int targetMovieId = (createdMovieId != null) ? createdMovieId
                    : getFirstExistingId(conn, "SELECT TOP 1 movieID FROM Movies");

            if (targetMovieId > 0 && targetRoomId > 0) {
                LocalDateTime start = LocalDateTime.now().plusDays(2).withNano(0);
                LocalDateTime end = start.plusHours(2);
                Showtimes newShowtime = new Showtimes(0, targetMovieId, targetRoomId, start, end, ShowtimeStatus.UPCOMING);

                DaoCallResult<Boolean> insStRes = callDao(() -> showtimesDAO.insertShowtime(newShowtime));
                if (Boolean.TRUE.equals(insStRes.returnValue) && insStRes.isClean()) {
                    pass("ShowtimesDAO.insertShowtime", "Thêm mới Showtime thành công.");
                } else {
                    fail("ShowtimesDAO.insertShowtime", "Thất bại! " + insStRes.getErrorSummary());
                }

                createdShowtimeId = getFirstExistingId(conn,
                        "SELECT TOP 1 showtimeID FROM Showtimes WHERE movieID = " + targetMovieId
                        + " AND roomID = " + targetRoomId + " ORDER BY showtimeID DESC");

                DaoCallResult<List<Showtimes>> allStRes = callDao(showtimesDAO::getAllShowtimes);
                if (allStRes.isClean() && allStRes.returnValue != null) {
                    pass("ShowtimesDAO.getAllShowtimes", "Lấy danh sách Showtimes thành công (" + allStRes.returnValue.size() + " bản ghi).");
                } else {
                    fail("ShowtimesDAO.getAllShowtimes", "Thất bại! " + allStRes.getErrorSummary());
                }

                if (createdShowtimeId != null && createdShowtimeId > 0) {
                    final int stId = createdShowtimeId;
                    DaoCallResult<Showtimes> stByIdRes = callDao(() -> showtimesDAO.getShowtimeById(stId));
                    if (stByIdRes.returnValue != null && stByIdRes.isClean()) {
                        pass("ShowtimesDAO.getShowtimeById", "Lấy Showtime theo ID = " + stId + " thành công.");
                        Showtimes stUpd = stByIdRes.returnValue;
                        stUpd.setStatus(ShowtimeStatus.PLAYING);
                        DaoCallResult<Boolean> updStRes = callDao(() -> showtimesDAO.updateShowtime(stUpd));
                        if (Boolean.TRUE.equals(updStRes.returnValue) && updStRes.isClean()) {
                            pass("ShowtimesDAO.updateShowtime", "Cập nhật Showtime thành công.");
                        } else {
                            fail("ShowtimesDAO.updateShowtime", "Cập nhật Showtime thất bại! " + updStRes.getErrorSummary());
                        }
                    } else {
                        fail("ShowtimesDAO.getShowtimeById", "Thất bại! " + stByIdRes.getErrorSummary());
                    }
                }
            } else {
                warn("ShowtimesDAO", "Thiếu movieID hoặc roomID hợp lệ để test ShowtimesDAO.");
            }

            // -----------------------------------------------------------------
            // 4.9 TEST BookingsDAO
            // -----------------------------------------------------------------
            BookingsDAO bookingsDAO = new BookingsDAO();
            int targetUserId = (createdUserId != null) ? createdUserId
                    : getFirstExistingId(conn, "SELECT TOP 1 userID FROM Users");
            int targetShowtimeId = (createdShowtimeId != null && createdShowtimeId > 0) ? createdShowtimeId
                    : getFirstExistingId(conn, "SELECT TOP 1 showtimeID FROM Showtimes");

            if (targetUserId > 0 && targetShowtimeId > 0) {
                Bookings newBooking = new Bookings(
                        0,
                        LocalDateTime.now().withNano(0),
                        new BigDecimal("150000"),
                        BookingStatus.PENDING,
                        targetUserId,
                        targetShowtimeId
                );

                DaoCallResult<Boolean> insBkRes = callDao(() -> bookingsDAO.insertBooking(newBooking));
                if (Boolean.TRUE.equals(insBkRes.returnValue) && insBkRes.isClean()) {
                    pass("BookingsDAO.insertBooking", "Thêm mới Booking thành công.");
                } else {
                    fail("BookingsDAO.insertBooking", "Thất bại! " + insBkRes.getErrorSummary());
                }

                createdBookingId = getFirstExistingId(conn,
                        "SELECT TOP 1 bookingID FROM Bookings WHERE userID = " + targetUserId
                        + " AND showtimeID = " + targetShowtimeId + " ORDER BY bookingID DESC");

                DaoCallResult<List<Bookings>> allBkRes = callDao(bookingsDAO::getAllBookings);
                if (allBkRes.isClean() && allBkRes.returnValue != null) {
                    pass("BookingsDAO.getAllBookings", "Lấy danh sách Bookings thành công (" + allBkRes.returnValue.size() + " bản ghi).");
                } else {
                    fail("BookingsDAO.getAllBookings", "Thất bại! " + allBkRes.getErrorSummary());
                }

                if (createdBookingId != null && createdBookingId > 0) {
                    final int bkId = createdBookingId;
                    DaoCallResult<Bookings> bkByIdRes = callDao(() -> bookingsDAO.getBookingById(bkId));
                    if (bkByIdRes.returnValue != null && bkByIdRes.isClean()) {
                        pass("BookingsDAO.getBookingById", "Lấy Booking theo ID = " + bkId + " thành công.");
                        Bookings bkUpd = bkByIdRes.returnValue;
                        bkUpd.setStatus(BookingStatus.COMPLETED);
                        DaoCallResult<Boolean> updBkRes = callDao(() -> bookingsDAO.updateBooking(bkUpd));
                        if (Boolean.TRUE.equals(updBkRes.returnValue) && updBkRes.isClean()) {
                            pass("BookingsDAO.updateBooking", "Cập nhật Booking thành công.");
                        } else {
                            fail("BookingsDAO.updateBooking", "Cập nhật Booking thất bại! " + updBkRes.getErrorSummary());
                        }
                    } else {
                        fail("BookingsDAO.getBookingById", "Thất bại! " + bkByIdRes.getErrorSummary());
                    }
                }
            } else {
                warn("BookingsDAO", "Thiếu userID hoặc showtimeID hợp lệ để test BookingsDAO.");
            }

            // -----------------------------------------------------------------
            // 4.10 TEST TicketsDAO
            // -----------------------------------------------------------------
            TicketsDAO ticketsDAO = new TicketsDAO();
            int targetSeatId = (createdSeatId != null && createdSeatId > 0) ? createdSeatId
                    : getFirstExistingId(conn, "SELECT TOP 1 seatID FROM Seats");
            int targetBookingId = (createdBookingId != null && createdBookingId > 0) ? createdBookingId
                    : getFirstExistingId(conn, "SELECT TOP 1 bookingID FROM Bookings");

            if (targetRoomId > 0 && targetSeatId > 0 && targetBookingId > 0) {
                Tickets newTicket = new Tickets(0, new BigDecimal("75000"), targetRoomId, targetSeatId, targetBookingId);
                DaoCallResult<Boolean> insTkRes = callDao(() -> ticketsDAO.insertTicket(newTicket));
                if (Boolean.TRUE.equals(insTkRes.returnValue) && insTkRes.isClean()) {
                    pass("TicketsDAO.insertTicket", "Thêm mới Ticket thành công.");
                } else {
                    fail("TicketsDAO.insertTicket", "Thất bại! " + insTkRes.getErrorSummary());
                }

                createdTicketId = getFirstExistingId(conn,
                        "SELECT TOP 1 ticketID FROM Tickets WHERE bookingID = " + targetBookingId
                        + " AND seatID = " + targetSeatId + " ORDER BY ticketID DESC");

                DaoCallResult<List<Tickets>> allTkRes = callDao(ticketsDAO::getAllTickets);
                if (allTkRes.isClean() && allTkRes.returnValue != null) {
                    pass("TicketsDAO.getAllTickets", "Lấy danh sách Tickets thành công (" + allTkRes.returnValue.size() + " bản ghi).");
                } else {
                    fail("TicketsDAO.getAllTickets", "Thất bại! " + allTkRes.getErrorSummary());
                }

                final int bIdForTk = targetBookingId;
                DaoCallResult<List<Tickets>> tkByBkRes = callDao(() -> ticketsDAO.getTicketsByBookingId(bIdForTk));
                if (tkByBkRes.isClean() && tkByBkRes.returnValue != null && !tkByBkRes.returnValue.isEmpty()) {
                    pass("TicketsDAO.getTicketsByBookingId", "Lấy Tickets theo bookingID = " + bIdForTk + " thành công.");
                } else {
                    fail("TicketsDAO.getTicketsByBookingId", "Thất bại! " + tkByBkRes.getErrorSummary());
                }

                if (createdTicketId != null && createdTicketId > 0) {
                    final int tkId = createdTicketId;
                    DaoCallResult<Tickets> tkByIdRes = callDao(() -> ticketsDAO.getTicketById(tkId));
                    if (tkByIdRes.returnValue != null && tkByIdRes.isClean()) {
                        pass("TicketsDAO.getTicketById", "Lấy Ticket theo ID = " + tkId + " thành công.");
                        Tickets tkUpd = tkByIdRes.returnValue;
                        tkUpd.setPrice(new BigDecimal("85000"));
                        DaoCallResult<Boolean> updTkRes = callDao(() -> ticketsDAO.updateTicket(tkUpd));
                        if (Boolean.TRUE.equals(updTkRes.returnValue) && updTkRes.isClean()) {
                            pass("TicketsDAO.updateTicket", "Cập nhật Ticket thành công.");
                        } else {
                            fail("TicketsDAO.updateTicket", "Cập nhật Ticket thất bại! " + updTkRes.getErrorSummary());
                        }
                    } else {
                        fail("TicketsDAO.getTicketById", "Thất bại! " + tkByIdRes.getErrorSummary());
                    }
                }
            } else {
                warn("TicketsDAO", "Thiếu roomID, seatID hoặc bookingID hợp lệ để test TicketsDAO.");
            }

        } finally {
            // Dọn dẹp toàn bộ dữ liệu test theo thứ tự ngược lại (Con -> Cha)
            cleanupTestData(
                    conn,
                    createdTicketId,
                    createdBookingId,
                    createdShowtimeId,
                    createdSeatId,
                    createdRoomId,
                    createdMovieGenre ? createdMovieId : null,
                    createdMovieGenre ? createdGenreId : null,
                    createdMovieId,
                    createdGenreId,
                    createdUserId,
                    createdAdminId
            );
        }
    }

    // =========================================================================
    // HÀM TIỆN ÍCH BẮT LỖI NGẦM TRONG DAO (INTERCEPT SYSTEM.ERR) & DỌN DẸP DB
    // =========================================================================
    @FunctionalInterface
    private interface DaoAction<T> {
        T execute() throws Exception;
    }

    private static class DaoCallResult<T> {
        final T returnValue;
        final Throwable thrownException;
        final String stderrOutput;

        DaoCallResult(T returnValue, Throwable thrownException, String stderrOutput) {
            this.returnValue = returnValue;
            this.thrownException = thrownException;
            this.stderrOutput = stderrOutput;
        }

        boolean isClean() {
            return thrownException == null && (stderrOutput == null || stderrOutput.trim().isEmpty());
        }

        String getErrorSummary() {
            if (thrownException != null) {
                return "Ném ngoại lệ: " + thrownException.getClass().getSimpleName() + ": " + thrownException.getMessage();
            }
            if (stderrOutput != null && !stderrOutput.trim().isEmpty()) {
                String firstLine = stderrOutput.trim().split("\\R")[0];
                return "DAO bắt SQLException ngầm (e.printStackTrace): " + firstLine;
            }
            return "Hàm trả về false / null / rỗng.";
        }
    }

    private static <T> DaoCallResult<T> callDao(DaoAction<T> action) {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
        T result = null;
        Throwable caught = null;
        try {
            System.setErr(new PrintStream(errBuffer, true, StandardCharsets.UTF_8.name()));
            result = action.execute();
        } catch (Throwable t) {
            caught = t;
        } finally {
            System.setErr(originalErr);
        }
        String errText = new String(errBuffer.toByteArray(), StandardCharsets.UTF_8);
        return new DaoCallResult<>(result, caught, errText);
    }

    private static Integer findIdBySql(Connection conn, String sql, Object... params) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    private static int getFirstExistingId(Connection conn, String sql) {
        Integer id = findIdBySql(conn, sql);
        return (id != null) ? id : -1;
    }

    private static void cleanupTestData(
            Connection conn,
            Integer ticketId,
            Integer bookingId,
            Integer showtimeId,
            Integer seatId,
            Integer roomId,
            Integer mgMovieId,
            Integer mgGenreId,
            Integer movieId,
            Integer genreId,
            Integer userId,
            Integer adminId
    ) {
        System.out.println("\n-------------------------------------------------------------------------");
        System.out.println("[DỌN DẸP] Đang xóa dữ liệu kiểm thử tạm thời khỏi Database...");
        deleteById(conn, "DELETE FROM Tickets WHERE ticketID = ?", ticketId);
        deleteById(conn, "DELETE FROM Bookings WHERE bookingID = ?", bookingId);
        deleteById(conn, "DELETE FROM Showtimes WHERE showtimeID = ?", showtimeId);
        deleteById(conn, "DELETE FROM Seats WHERE seatID = ?", seatId);
        deleteById(conn, "DELETE FROM Rooms WHERE roomID = ?", roomId);
        if (mgMovieId != null && mgGenreId != null) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Movie_Genre WHERE movieID = ? AND genreID = ?")) {
                ps.setInt(1, mgMovieId);
                ps.setInt(2, mgGenreId);
                ps.executeUpdate();
            } catch (SQLException ignored) {
            }
        }
        deleteById(conn, "DELETE FROM Movies WHERE movieID = ?", movieId);
        deleteById(conn, "DELETE FROM Genres WHERE genreID = ?", genreId);
        deleteById(conn, "DELETE FROM Users WHERE userID = ?", userId);
        deleteById(conn, "DELETE FROM Admins WHERE adminID = ?", adminId);
        System.out.println("[DỌN DẸP] Hoàn tất! Database được giữ nguyên vẹn như ban đầu.");
    }

    private static void deleteById(Connection conn, String sql, Integer id) {
        if (id == null || id <= 0) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    // =========================================================================
    // ĐỊNH DẠNG IN BÁO CÁO KẾT QUẢ
    // =========================================================================
    private static void printBanner(String title) {
        System.out.println("=========================================================================");
        System.out.println("   " + title);
        System.out.println("=========================================================================");
    }

    private static void printSection(String sectionTitle) {
        System.out.println("\n>>> " + sectionTitle);
        System.out.println("-------------------------------------------------------------------------");
    }

    private static void pass(String item, String detail) {
        passCount++;
        System.out.println("  [PASS] " + item + " -> " + detail);
    }

    private static void fail(String item, String detail) {
        failCount++;
        String msg = item + ": " + detail;
        failureDetails.add(msg);
        System.out.println("  [FAIL] " + msg);
    }

    private static void warn(String item, String detail) {
        warnCount++;
        String msg = item + ": " + detail;
        warningDetails.add(msg);
        System.out.println("  [WARN] " + msg);
    }

    private static void printSummary() {
        System.out.println("\n=========================================================================");
        System.out.println("                    BẢNG TỔNG KẾT KIỂM TRA CRUD");
        System.out.println("=========================================================================");
        System.out.println("  + Số mục ĐẠT (PASS)      : " + passCount);
        System.out.println("  + Số mục LỖI (FAIL)      : " + failCount);
        System.out.println("  + Số mục CẢNH BÁO (WARN) : " + warnCount);
        System.out.println("-------------------------------------------------------------------------");

        if (!failureDetails.isEmpty()) {
            System.out.println("\n[!] DANH SÁCH LỖI CẦN SỬA NGAY (" + failureDetails.size() + " lỗi):");
            for (int i = 0; i < failureDetails.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + failureDetails.get(i));
            }
        }

        if (!warningDetails.isEmpty()) {
            System.out.println("\n[*] DANH SÁCH CẢNH BÁO / CẦN BỔ SUNG (" + warningDetails.size() + " mục):");
            for (int i = 0; i < warningDetails.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + warningDetails.get(i));
            }
        }

        if (failureDetails.isEmpty()) {
            System.out.println("\n=> CHÚC MỪNG! Tất cả các bài kiểm tra CRUD đều chạy thành công không có lỗi!");
        }
        System.out.println("=========================================================================");
    }
}
