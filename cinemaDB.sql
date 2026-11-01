-- =============================================================================
-- SCRIPT KHỞI TẠO CƠ SỞ DỮ LIỆU CHUẨN CHO DỰ ÁN CINEMA (SQL SERVER)
-- Bao gồm đầy đủ 10 bảng khớp 100% với tầng Models & DAO + Dữ liệu mẫu (Seed Data)
-- =============================================================================

USE master;
GO

IF DB_ID('cinemaDB') IS NOT NULL
BEGIN
    ALTER DATABASE cinemaDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE cinemaDB;
END
GO

CREATE DATABASE cinemaDB;
GO

USE cinemaDB;
GO

-- =============================================================================
-- 1. BẢNG Admins
-- =============================================================================
CREATE TABLE Admins (
    adminID      INT IDENTITY(1,1) PRIMARY KEY,
    fullName     NVARCHAR(100) NOT NULL,
    account      VARCHAR(50)   NOT NULL UNIQUE,
    passwordHash VARCHAR(255)  NOT NULL,
    phone        VARCHAR(20)   NULL,
    email        VARCHAR(100)  NOT NULL UNIQUE,
    roles        VARCHAR(30)   NOT NULL DEFAULT 'MOVIES_MANAGER',
    createdAt    DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    updatedAt    DATETIME2     NULL,
    CONSTRAINT CK_Admins_Roles CHECK (roles IN ('MOVIES_MANAGER', 'SUPER_ADMIN'))
);
GO

-- =============================================================================
-- 2. BẢNG Users
-- Lưu ý: Cột gender lưu 0 (FEMALE) hoặc 1 (MALE) khớp với UserGender.getDbValue()
-- Cột updateAt giữ đúng tên theo UsersDAO.java
-- =============================================================================
CREATE TABLE Users (
    userID       INT IDENTITY(1,1) PRIMARY KEY,
    fullName     NVARCHAR(100) NOT NULL,
    userName     VARCHAR(50)   NOT NULL UNIQUE,
    passwordHash VARCHAR(255)  NOT NULL,
    phone        VARCHAR(20)   NULL,
    email        VARCHAR(100)  NOT NULL UNIQUE,
    imageURL     NVARCHAR(500) NULL DEFAULT '../images/default_image/haiz.png',
    gender       CHAR(1)       NOT NULL DEFAULT '0',
    dateOfBirth  DATETIME2     NULL,
    createdAt    DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    updateAt     DATETIME2     NULL,
    CONSTRAINT CK_Users_Gender CHECK (gender IN ('0', '1'))
);
GO

-- =============================================================================
-- 3. BẢNG Genres (Thể loại phim)
-- =============================================================================
CREATE TABLE Genres (
    genreID   INT IDENTITY(1,1) PRIMARY KEY,
    genreName NVARCHAR(100) NOT NULL UNIQUE
);
GO

-- =============================================================================
-- 4. BẢNG Movies (Phim)
-- =============================================================================
CREATE TABLE Movies (
    movieID           INT IDENTITY(1,1) PRIMARY KEY,
    movieName         NVARCHAR(200) NOT NULL,
    approvedByAdminID INT           NULL,
    category          NVARCHAR(100) NULL,
    bio               NVARCHAR(MAX) NULL,
    trailerURL        NVARCHAR(500) NULL,
    posterURL         NVARCHAR(500) NULL,
    director          NVARCHAR(150) NULL,
    actor             NVARCHAR(300) NULL,
    releaseDate       DATETIME2     NULL,
    createdAt         DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    duration          INT           NOT NULL CHECK (duration > 0),
    language          NVARCHAR(50)  NULL,
    ratingScore       DECIMAL(4,2)  NULL DEFAULT 0.0,
    ageRating         VARCHAR(20)   NULL,
    rate              DECIMAL(4,2)  NULL DEFAULT 0.0,
    CONSTRAINT FK_Movies_Admins FOREIGN KEY (approvedByAdminID) REFERENCES Admins(adminID) ON DELETE SET NULL
);
GO

-- =============================================================================
-- 5. BẢNG Movie_Genre (Bảng trung gian Phim - Thể loại)
-- =============================================================================
CREATE TABLE Movie_Genre (
    movieID INT NOT NULL,
    genreID INT NOT NULL,
    CONSTRAINT PK_Movie_Genre PRIMARY KEY (movieID, genreID),
    CONSTRAINT FK_MovieGenre_Movies FOREIGN KEY (movieID) REFERENCES Movies(movieID) ON DELETE CASCADE,
    CONSTRAINT FK_MovieGenre_Genres FOREIGN KEY (genreID) REFERENCES Genres(genreID) ON DELETE CASCADE
);
GO

-- =============================================================================
-- 6. BẢNG Rooms (Phòng chiếu)
-- =============================================================================
CREATE TABLE Rooms (
    roomID     INT IDENTITY(1,1) PRIMARY KEY,
    roomName   NVARCHAR(50) NOT NULL UNIQUE,
    roomType   NVARCHAR(50) NOT NULL,
    status     VARCHAR(30)  NOT NULL DEFAULT 'STANDBY',
    userID     INT          NULL,
    showtimeID INT          NULL,
    CONSTRAINT CK_Rooms_Status CHECK (status IN ('MAINTENANCE', 'STANDBY', 'CLOSED'))
);
GO

-- =============================================================================
-- 7. BẢNG Seats (Ghế ngồi trong phòng chiếu)
-- =============================================================================
CREATE TABLE Seats (
    seatID   INT IDENTITY(1,1) PRIMARY KEY,
    seatName VARCHAR(20) NOT NULL,
    roomID   INT         NOT NULL,
    CONSTRAINT FK_Seats_Rooms FOREIGN KEY (roomID) REFERENCES Rooms(roomID) ON DELETE CASCADE,
    CONSTRAINT UQ_Seats_Room_SeatName UNIQUE (roomID, seatName)
);
GO

-- =============================================================================
-- 8. BẢNG Showtimes (Suất chiếu)
-- =============================================================================
CREATE TABLE Showtimes (
    showtimeID INT IDENTITY(1,1) PRIMARY KEY,
    movieID    INT         NOT NULL,
    roomID     INT         NOT NULL,
    startTime  DATETIME2   NOT NULL,
    endTime    DATETIME2   NOT NULL,
    status     VARCHAR(30) NOT NULL DEFAULT 'UPCOMING',
    CONSTRAINT FK_Showtimes_Movies FOREIGN KEY (movieID) REFERENCES Movies(movieID) ON DELETE CASCADE,
    CONSTRAINT FK_Showtimes_Rooms  FOREIGN KEY (roomID)  REFERENCES Rooms(roomID)  ON DELETE CASCADE,
    CONSTRAINT CK_Showtimes_Status CHECK (status IN ('UPCOMING', 'PLAYING', 'ENDED', 'CANCELLED')),
    CONSTRAINT CK_Showtimes_Time   CHECK (endTime > startTime)
);
GO

-- =============================================================================
-- 9. BẢNG Bookings (Đơn đặt vé)
-- =============================================================================
CREATE TABLE Bookings (
    bookingID   INT IDENTITY(1,1) PRIMARY KEY,
    bookingTime DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    totalAmount DECIMAL(18,2) NOT NULL DEFAULT 0,
    status      VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    userID      INT           NOT NULL,
    showtimeID  INT           NOT NULL,
    CONSTRAINT FK_Bookings_Users     FOREIGN KEY (userID)     REFERENCES Users(userID),
    CONSTRAINT FK_Bookings_Showtimes FOREIGN KEY (showtimeID) REFERENCES Showtimes(showtimeID),
    CONSTRAINT CK_Bookings_Status    CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED'))
);
GO

-- =============================================================================
-- 10. BẢNG Tickets (Chi tiết vé theo ghế)
-- =============================================================================
CREATE TABLE Tickets (
    ticketID  INT IDENTITY(1,1) PRIMARY KEY,
    price     DECIMAL(18,2) NOT NULL,
    roomID    INT           NOT NULL,
    seatID    INT           NOT NULL,
    bookingID INT           NOT NULL,
    CONSTRAINT FK_Tickets_Rooms    FOREIGN KEY (roomID)    REFERENCES Rooms(roomID),
    CONSTRAINT FK_Tickets_Seats    FOREIGN KEY (seatID)    REFERENCES Seats(seatID),
    CONSTRAINT FK_Tickets_Bookings FOREIGN KEY (bookingID) REFERENCES Bookings(bookingID) ON DELETE CASCADE
);
GO

-- =============================================================================
-- DỮ LIỆU MẪU (SEED DATA) CHO TẤT CẢ 10 BẢNG
-- =============================================================================

-- 1. Seed Admins
INSERT INTO Admins (fullName, account, passwordHash, phone, email, roles, createdAt)
VALUES
    (N'Nguyễn Hoàng Hiếu', 'admin', '123456', '0901112222', 'hieu.admin@cinema.vn', 'SUPER_ADMIN', SYSDATETIME()),
    (N'Trần Minh Hải', 'Hai', '123456', '0903334444', 'hai.manager@cinema.vn', 'MOVIES_MANAGER', SYSDATETIME()),
    (N'Lê Thu Hà', 'halethu', '123456', '0905556666', 'ha.manager@cinema.vn', 'MOVIES_MANAGER', SYSDATETIME());
GO

-- 2. Seed Users (gender: '1' = MALE, '0' = FEMALE)
INSERT INTO Users (fullName, userName, passwordHash, phone, email, imageURL, gender, dateOfBirth, createdAt)
VALUES
    (N'Nguyễn Văn An', 'nguyenvanan', '123456', '0912345678', 'an.nguyen@gmail.com', '../images/default_image/haiz.png', '1', '2002-05-15T00:00:00', SYSDATETIME()),
    (N'Phạm Thị Mai', 'phamthimai', '123456', '0987654321', 'mai.pham@gmail.com', '../images/default_image/haiz.png', '0', '2003-08-20T00:00:00', SYSDATETIME()),
    (N'Hoàng Minh Tuấn', 'hoangtuan', '123456', '0933445566', 'tuan.hoang@gmail.com', '../images/default_image/haiz.png', '1', '2001-12-10T00:00:00', SYSDATETIME()),
    (N'Vũ Ngọc Lan', 'ngoclan', '123456', '0977889900', 'lan.vu@gmail.com', '../images/default_image/haiz.png', '0', '2004-03-25T00:00:00', SYSDATETIME());
GO

-- 3. Seed Genres
INSERT INTO Genres (genreName)
VALUES
    (N'Hành động'),
    (N'Khoa học viễn tưởng'),
    (N'Kinh dị'),
    (N'Tình cảm'),
    (N'Hoạt hình'),
    (N'Hài hước'),
    (N'Tâm lý');
GO

-- 4. Seed Movies
INSERT INTO Movies (movieName, approvedByAdminID, category, bio, trailerURL, posterURL, director, actor, releaseDate, createdAt, duration, language, ratingScore, ageRating, rate)
VALUES
    (N'Mai', 1, N'Tâm lý, Tình cảm', N'Câu chuyện về cuộc đời và tình yêu đầy biến cố của Mai.', 'https://www.youtube.com/watch?v=EX6clvId19s', 'https://example.com/posters/mai.jpg', N'Trấn Thành', N'Phương Anh Đào, Tuấn Trần', '2026-02-10T00:00:00', SYSDATETIME(), 131, N'Tiếng Việt - Phụ đề Anh', 8.70, 'T18', 4.60),
    (N'Dune: Hành Tinh Cát - Phần 2', 1, N'Khoa học viễn tưởng, Hành động', N'Hành trình báo thù và giải cứu tộc Fremen của Paul Atreides trên hành tinh Arrakis.', 'https://www.youtube.com/watch?v=Way9Dexny3w', 'https://example.com/posters/dune2.jpg', N'Denis Villeneuve', N'Timothée Chalamet, Zendaya', '2026-03-01T00:00:00', SYSDATETIME(), 166, N'Tiếng Anh - Phụ đề Việt', 9.20, 'T16', 4.90),
    (N'Lật Mặt 7: Một Điều Ước', 2, N'Tâm lý, Gia đình', N'Hành trình cảm động của người mẹ già 73 tuổi và 5 người con.', 'https://www.youtube.com/watch?v=example_lm7', 'https://example.com/posters/latmat7.jpg', N'Lý Hải', N'Thanh Hiền, Trương Minh Cường', '2026-04-26T00:00:00', SYSDATETIME(), 138, N'Tiếng Việt', 8.90, 'K', 4.70),
    (N'Kung Fu Panda 4', 2, N'Hoạt hình, Hài hước', N'Gấu Po tìm kiếm truyền nhân Thần Long Đại Hiệp mới và đối đầu Tắc Kè Hoa.', 'https://www.youtube.com/watch?v=_inKs4eeHiI', 'https://example.com/posters/kfp4.jpg', N'Mike Mitchell', N'Jack Black, Awkwafina', '2026-03-08T00:00:00', SYSDATETIME(), 94, N'Lồng tiếng Việt', 8.10, 'P', 4.30),
    (N'Quật Mộ Trùng Ma (Exhuma)', 1, N'Kinh dị, Bí ẩn', N'Những hiện tượng tâm linh kỳ bí xảy ra khi khai quật ngôi mộ cổ trên đỉnh núi.', 'https://www.youtube.com/watch?v=example_exhuma', 'https://example.com/posters/exhuma.jpg', N'Jang Jae-hyun', N'Choi Min-sik, Kim Go-eun, Lee Do-hyun', '2026-03-15T00:00:00', SYSDATETIME(), 134, N'Tiếng Hàn - Phụ đề Việt', 8.80, 'T16', 4.65);
GO

-- 5. Seed Movie_Genre
INSERT INTO Movie_Genre (movieID, genreID)
VALUES
    (1, 4), (1, 7),
    (2, 1), (2, 2),
    (3, 6), (3, 7),
    (4, 1), (4, 5), (4, 6),
    (5, 3), (5, 7);
GO

-- 6. Seed Rooms
INSERT INTO Rooms (roomName, roomType, status, userID, showtimeID)
VALUES
    (N'Phòng 01 - Standard', N'2D Standard', 'STANDBY', 1, 1),
    (N'Phòng 02 - IMAX',     N'IMAX 3D',     'STANDBY', 1, 2),
    (N'Phòng 03 - GoldClass', N'VIP Gold',   'STANDBY', 1, 3),
    (N'Phòng 04 - Bảo Trì',  N'2D Standard', 'MAINTENANCE', 1, 1);
GO

-- 7. Seed Seats (Mỗi phòng tạo ghế hàng A, B, C)
INSERT INTO Seats (seatName, roomID)
VALUES
    -- Phòng 1 (seatID: 1..10)
    ('A1', 1), ('A2', 1), ('A3', 1), ('A4', 1), ('A5', 1),
    ('B1', 1), ('B2', 1), ('B3', 1), ('B4', 1), ('B5', 1),
    -- Phòng 2 (seatID: 11..20)
    ('A1', 2), ('A2', 2), ('A3', 2), ('A4', 2), ('A5', 2),
    ('B1', 2), ('B2', 2), ('B3', 2), ('B4', 2), ('B5', 2),
    -- Phòng 3 (seatID: 21..26)
    ('VIP1', 3), ('VIP2', 3), ('VIP3', 3), ('VIP4', 3), ('VIP5', 3), ('VIP6', 3);
GO

-- 8. Seed Showtimes
INSERT INTO Showtimes (movieID, roomID, startTime, endTime, status)
VALUES
    (1, 1, DATEADD(HOUR, 2,  SYSDATETIME()), DATEADD(HOUR, 4,  SYSDATETIME()), 'UPCOMING'),
    (2, 2, DATEADD(HOUR, 3,  SYSDATETIME()), DATEADD(HOUR, 6,  SYSDATETIME()), 'UPCOMING'),
    (3, 1, DATEADD(HOUR, 5,  SYSDATETIME()), DATEADD(HOUR, 7,  SYSDATETIME()), 'UPCOMING'),
    (4, 3, DATEADD(HOUR, 1,  SYSDATETIME()), DATEADD(HOUR, 3,  SYSDATETIME()), 'PLAYING'),
    (5, 2, DATEADD(HOUR, -4, SYSDATETIME()), DATEADD(HOUR, -2, SYSDATETIME()), 'ENDED');
GO

-- 9. Seed Bookings
INSERT INTO Bookings (bookingTime, totalAmount, status, userID, showtimeID)
VALUES
    (SYSDATETIME(), 160000, 'COMPLETED', 1, 1),
    (SYSDATETIME(), 240000, 'COMPLETED', 2, 2),
    (SYSDATETIME(), 150000, 'PENDING',   3, 3),
    (SYSDATETIME(), 80000,  'CANCELLED', 4, 1);
GO

-- 10. Seed Tickets
INSERT INTO Tickets (price, roomID, seatID, bookingID)
VALUES
    (80000,  1, 1,  1), -- Booking 1: Phòng 1, Ghế A1
    (80000,  1, 2,  1), -- Booking 1: Phòng 1, Ghế A2
    (120000, 2, 11, 2), -- Booking 2: Phòng 2, Ghế A1
    (120000, 2, 12, 2), -- Booking 2: Phòng 2, Ghế A2
    (75000,  1, 6,  3), -- Booking 3: Phòng 1, Ghế B1
    (75000,  1, 7,  3), -- Booking 3: Phòng 1, Ghế B2
    (80000,  1, 3,  4); -- Booking 4: Phòng 1, Ghế A3 (Cancelled)
GO
