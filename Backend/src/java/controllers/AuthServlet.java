package controllers;

import dal.AdminsDAO;
import dal.UsersDAO;
import enums.UserGender;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;
import models.Admins;
import models.Users;
import util.Passwords;

/**
 * Đăng nhập / đăng ký / đăng xuất.
 *
 * GET  /auth                : trang đăng nhập (?tab=register để mở sẵn tab đăng ký, ?next=trang-cần-quay-lại)
 * GET  /auth?action=logout  : đăng xuất
 * POST /auth                : action=login (mặc định) hoặc action=register
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/auth"})
public class AuthServlet extends HttpServlet {

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_.]{3,30}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+ ]{8,15}$");
    // `next` chỉ được là đường dẫn tương đối trong web (vd: "booking?showtimeID=3"), tránh chuyển hướng ra trang ngoài.
    private static final Pattern SAFE_NEXT = Pattern.compile("^[a-z]+(\\?[A-Za-z0-9_=&.%-]*)?$");

    private final UsersDAO usersDAO = new UsersDAO();
    private final AdminsDAO adminsDAO = new AdminsDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("logout".equalsIgnoreCase(request.getParameter("action"))) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("currentAdmin") != null) {
            response.sendRedirect(request.getContextPath() + "/admin");
            return;
        }
        if (session != null && session.getAttribute("currentUser") != null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        show(request, response, "register".equals(request.getParameter("tab")) ? "register" : "login", null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if ("register".equalsIgnoreCase(request.getParameter("action"))) {
            register(request, response);
        } else {
            login(request, response);
        }
    }

    private void login(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String account = trim(request.getParameter("userName"));
        String password = request.getParameter("password");

        if (!account.isEmpty() && password != null) {
            // 1. Tài khoản quản trị
            Admins admin = adminsDAO.getAdminByAccount(account);
            if (admin != null && Passwords.matches(password, admin.getPasswordHash())) {
                HttpSession session = freshSession(request);
                session.removeAttribute("currentUser");
                session.setAttribute("currentAdmin", admin);
                response.sendRedirect(request.getContextPath() + "/admin");
                return;
            }

            // 2. Khách hàng: đăng nhập bằng tên đăng nhập hoặc email
            Users user = usersDAO.getUserByUserName(account);
            if (user == null && account.contains("@")) {
                user = usersDAO.getUserByEmail(account);
            }
            if (user != null && Passwords.matches(password, user.getPasswordHash())) {
                HttpSession session = freshSession(request);
                session.removeAttribute("currentAdmin");
                session.setAttribute("currentUser", user);
                redirectAfterLogin(request, response);
                return;
            }
        }
        show(request, response, "login", "Sai tên đăng nhập hoặc mật khẩu.");
    }

    private void register(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fullName = trim(request.getParameter("fullName"));
        String userName = trim(request.getParameter("userName"));
        String email = trim(request.getParameter("email"));
        String phone = trim(request.getParameter("phone"));
        String password = request.getParameter("password") == null ? "" : request.getParameter("password");
        String confirm = request.getParameter("confirm") == null ? "" : request.getParameter("confirm");
        String dobText = trim(request.getParameter("dateOfBirth"));
        UserGender gender = UserGender.fromString(request.getParameter("gender"));

        String error = null;
        LocalDateTime dob = null;
        if (fullName.length() < 2 || fullName.length() > 100) {
            error = "Họ và tên phải từ 2 đến 100 ký tự.";
        } else if (!USERNAME.matcher(userName).matches()) {
            error = "Tên đăng nhập gồm 3-30 ký tự: chữ cái, số, dấu chấm hoặc gạch dưới.";
        } else if (!EMAIL.matcher(email).matches() || email.length() > 100) {
            error = "Email không hợp lệ.";
        } else if (!phone.isEmpty() && !PHONE.matcher(phone).matches()) {
            error = "Số điện thoại chỉ gồm 8-15 chữ số.";
        } else if (password.length() < 6 || password.length() > 72) {
            error = "Mật khẩu phải từ 6 đến 72 ký tự.";
        } else if (!password.equals(confirm)) {
            error = "Mật khẩu nhập lại không khớp.";
        } else if (!dobText.isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(dobText);
                if (date.isAfter(LocalDate.now())) {
                    error = "Ngày sinh không được ở tương lai.";
                } else {
                    dob = date.atStartOfDay();
                }
            } catch (DateTimeParseException e) {
                error = "Ngày sinh không hợp lệ.";
            }
        }
        if (error == null && (usersDAO.getUserByUserName(userName) != null
                || adminsDAO.getAdminByAccount(userName) != null)) {
            error = "Tên đăng nhập này đã có người dùng.";
        }
        if (error == null && usersDAO.getUserByEmail(email) != null) {
            error = "Email này đã được đăng ký.";
        }

        if (error == null) {
            Users newUser = new Users(fullName, userName, Passwords.hash(password), phone, email,
                    "../images/default_image/haiz.png", gender, dob);
            if (usersDAO.insertUser(newUser)) {
                Users saved = usersDAO.getUserByUserName(userName);
                if (saved != null) {
                    HttpSession session = freshSession(request);
                    session.removeAttribute("currentAdmin");
                    session.setAttribute("currentUser", saved);
                    redirectAfterLogin(request, response);
                    return;
                }
            }
            error = "Không tạo được tài khoản. Vui lòng thử lại sau.";
        }

        // Giữ lại những gì đã nhập (trừ mật khẩu) để người dùng sửa tiếp.
        request.setAttribute("form_fullName", fullName);
        request.setAttribute("form_userName", userName);
        request.setAttribute("form_email", email);
        request.setAttribute("form_phone", phone);
        request.setAttribute("form_dateOfBirth", dobText);
        request.setAttribute("form_gender", gender.name());
        show(request, response, "register", error);
    }

    private void show(HttpServletRequest request, HttpServletResponse response, String tab, String error)
            throws ServletException, IOException {
        request.setAttribute("tab", tab);
        request.setAttribute("error", error);
        String next = safeNext(request.getParameter("next"));
        request.setAttribute("next", next);
        request.setAttribute("nextParam", URLEncoder.encode(next, "UTF-8")); // dùng trong link đổi tab
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    private void redirectAfterLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String next = safeNext(request.getParameter("next"));
        response.sendRedirect(request.getContextPath() + "/" + (next.isEmpty() ? "home" : next));
    }

    // Đăng nhập xong phải đổi mã phiên để chống tấn công cố định phiên (session fixation).
    private HttpSession freshSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        request.changeSessionId();
        return session;
    }

    private static String safeNext(String next) {
        return next != null && SAFE_NEXT.matcher(next).matches() ? next : "";
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
