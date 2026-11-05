# CINEVERSE — Web đặt vé xem phim (JSP / Servlet / SQL Server)

Ứng dụng Java Web: khách xem phim, chọn suất chiếu, chọn ghế và đặt vé; quản trị viên quản lý phim, suất chiếu, phòng, thể loại, đơn đặt vé và khách hàng.

## Yêu cầu

- JDK 17 trở lên
- Apache Tomcat **10.1** (Jakarta EE 10 — code dùng `jakarta.servlet.*`)
- SQL Server (mặc định `localhost:1433`)
- Thư viện đã có sẵn trong `Backend/build/web/WEB-INF/lib`: `mssql-jdbc`, `jakarta.servlet.jsp.jstl` (thêm vào classpath của project nếu NetBeans báo thiếu)

## Cài đặt

1. Chạy file [`cinemaDB.sql`](cinemaDB.sql) trong SQL Server Management Studio. Script tạo lại database `cinemaDB` cùng dữ liệu mẫu (**xoá database cũ cùng tên**).
2. Cấu hình kết nối bằng biến môi trường (không đặt thì dùng mặc định cho máy dev):

   | Biến | Mặc định |
   |---|---|
   | `DB_URL` | `jdbc:sqlserver://localhost:1433;databaseName=cinemaDB;encrypt=true;trustServerCertificate=true` |
   | `DB_USERNAME` | `sa` |
   | `DB_PASSWORD` | `123` |

3. Mở thư mục `Backend` bằng NetBeans, chạy project trên Tomcat 10.1, rồi vào <http://localhost:8080/Backend/>.

## Tài khoản mẫu

| Vai trò | Tài khoản | Mật khẩu |
|---|---|---|
| Khách hàng | `nguyenvanan` | `123456` |
| Quản trị (SUPER_ADMIN) | `admin` | `123456` |
| Quản trị (MOVIES_MANAGER) | `Hai` | `123456` |

Tài khoản đăng ký mới được lưu mật khẩu dạng băm PBKDF2. Mật khẩu của dữ liệu mẫu là chữ thường để dễ thử, vẫn đăng nhập được.

## Các trang

| Đường dẫn | Chức năng |
|---|---|
| `/home` | Trang chủ: phim nổi bật, lọc theo trạng thái / thể loại, tìm kiếm (không phân biệt dấu) |
| `/movie?id=` | Chi tiết phim và lịch chiếu theo ngày |
| `/booking?showtimeID=` | Sơ đồ ghế và đặt vé (cần đăng nhập khi xác nhận) |
| `/tickets` | Vé của tôi: xem, thanh toán, huỷ đơn của chính mình |
| `/auth` | Đăng nhập / đăng ký / đăng xuất |
| `/admin` | Quản trị (chỉ tài khoản trong bảng `Admins`; đơn đặt vé, khách hàng, phòng chỉ `SUPER_ADMIN`) |

## Cấu trúc

```
Backend/
  src/java/
    controllers/   Servlet: Home, Movie, Booking, Tickets, Auth, Admin
    dal/           DAO truy cập SQL Server
    models/ enums/ Lớp dữ liệu
    util/          Fmt (định dạng cho JSP), Pricing (giá vé), Passwords (băm mật khẩu)
  web/
    *.jsp          Giao diện
    WEB-INF/       web.xml, cinema.tld (hàm định dạng), tags/poster.tag, jspf/ (header, footer dùng chung)
    css/style.css  js/app.js
Frontend/          Ảnh mockup và bản HTML tĩnh dùng để thiết kế (không nằm trong ứng dụng)
cinemaDB.sql       Tạo database và dữ liệu mẫu
```

## Quy tắc nghiệp vụ

- Giá vé một ghế theo loại phòng: Standard 80.000 đ, IMAX 120.000 đ, VIP/Gold 150.000 đ. Server tự tính, không tin giá từ trình duyệt.
- Tối đa 8 ghế mỗi lần đặt. Đơn và vé được ghi trong một transaction có khoá, nên hai người đặt cùng một ghế thì chỉ một người thành công.
- Chỉ suất `UPCOMING` / `PLAYING` mới bán vé; chỉ đơn của suất `UPCOMING` mới huỷ được. Huỷ đơn sẽ trả ghế lại.
- Dữ liệu mẫu tạo suất chiếu theo giờ hiện tại lúc chạy `cinemaDB.sql`; sau vài giờ hãy tạo suất mới ở trang quản trị hoặc chạy lại script.
