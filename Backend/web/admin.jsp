<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="/WEB-INF/cinema.tld" %>
<c:set var="pageTitle" value="Quản trị"/>
<c:set var="nav" value="admin"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<main class="wrap">
    <%-- Kết quả của thao tác vừa thực hiện (AdminServlet đặt vào flashType / flashMessage) --%>
    <c:if test="${not empty flashMessage}">
        <div class="alert toast ${flashType == 'error' ? 'alert-err' : 'alert-ok'}" role="alert" data-autohide title="Bấm để đóng">
            <c:out value="${flashMessage}"/>
        </div>
    </c:if>

    <div class="admin">
        <aside class="side">
            <div class="me">
                <small>Đang đăng nhập</small>
                <strong><c:out value="${currentAdmin.fullName}"/></strong>
                <span class="badge ${isSuperAdmin ? 'b-gold' : 'b-info'}"><c:out value="${currentAdmin.roles}"/></span>
            </div>
            <nav>
                <a href="#overview">Tổng quan</a>
                <a href="#movies">Phim <span>${moviesList.size()}</span></a>
                <a href="#showtimes">Suất chiếu <span>${showtimesList.size()}</span></a>
                <a href="#rooms">Phòng chiếu <span>${roomsList.size()}</span></a>
                <a href="#genres">Thể loại <span>${genresList.size()}</span></a>
                <c:if test="${isSuperAdmin}">
                    <a href="#bookings">Đơn đặt vé <span>${bookingsList.size()}</span></a>
                    <a href="#users">Khách hàng <span>${usersList.size()}</span></a>
                </c:if>
            </nav>
        </aside>

        <div>
            <%-- ============ Tổng quan ============ --%>
            <section id="overview" class="kpis">
                <div class="kpi">
                    <small>Doanh thu</small>
                    <strong>${f:money(totalRevenue)}</strong>
                    <em>Chỉ tính đơn đã thanh toán</em>
                </div>
                <div class="kpi">
                    <small>Vé đã bán</small>
                    <strong>${soldTicketsCount}</strong>
                    <em>${pendingBookingsCount} đơn đang chờ thanh toán</em>
                </div>
                <div class="kpi">
                    <small>Suất đang mở bán</small>
                    <strong>${activeShowtimesCount}</strong>
                    <em>Sắp chiếu hoặc đang chiếu</em>
                </div>
                <div class="kpi">
                    <small>Phòng chiếu</small>
                    <strong>${standbyRoomsCount} / ${roomsList.size()}</strong>
                    <em>${maintRoomsCount} bảo trì · ${closedRoomsCount} đóng</em>
                </div>
            </section>

            <%-- ============ Phim ============ --%>
            <section id="movies" class="panel">
                <div class="panel-head">
                    <h2>Phim</h2>
                    <button type="button" class="btn btn-primary btn-sm js-movie-new">+ Thêm phim</button>
                    <p>Thêm, sửa, xoá phim và gán thể loại. Phim đã có đơn đặt vé không xoá được.</p>
                </div>
                <div class="table-wrap">
                    <table class="table">
                        <thead>
                            <tr><th>Phim</th><th>Thể loại</th><th>Thời lượng</th><th>Độ tuổi</th><th>Điểm</th><th></th></tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${moviesList}" var="m">
                                <tr>
                                    <td>
                                        <strong><c:out value="${m.movieName}"/></strong>
                                        <span class="sub"><c:out value="${m.director}"/></span>
                                    </td>
                                    <td><c:out value="${m.category}"/></td>
                                    <td>${m.duration} phút</td>
                                    <td><span class="badge b-age"><c:out value="${m.ageRating}"/></span></td>
                                    <td class="rating">★ ${m.ratingScore}</td>
                                    <td class="right">
                                        <div class="inline">
                                            <a class="btn btn-ghost btn-sm" href="${ctx}/movie?id=${m.movieID}">Xem</a>
                                            <button type="button" class="btn btn-ghost btn-sm js-movie-edit"
                                                    data-id="${m.movieID}"
                                                    data-name="<c:out value='${m.movieName}'/>"
                                                    data-director="<c:out value='${m.director}'/>"
                                                    data-actor="<c:out value='${m.actor}'/>"
                                                    data-duration="${m.duration}"
                                                    data-age="<c:out value='${m.ageRating}'/>"
                                                    data-rating="${m.ratingScore}"
                                                    data-language="<c:out value='${m.language}'/>"
                                                    data-bio="<c:out value='${m.bio}'/>"
                                                    data-poster="<c:out value='${m.posterURL}'/>"
                                                    data-trailer="<c:out value='${m.trailerURL}'/>"
                                                    data-release="${m.releaseDate}"
                                                    data-genre-ids="${movieGenreIds[m.movieID]}">Sửa</button>
                                            <form action="${ctx}/admin" method="post" class="inline"
                                                  data-confirm="Xoá phim này cùng các suất chiếu chưa có đơn đặt vé?">
                                                <input type="hidden" name="csrf" value="${csrfToken}">
                                                <input type="hidden" name="action" value="deleteMovie">
                                                <input type="hidden" name="movieID" value="${m.movieID}">
                                                <button type="submit" class="btn btn-danger btn-sm">Xoá</button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </section>

            <%-- ============ Suất chiếu ============ --%>
            <section id="showtimes" class="panel">
                <div class="panel-head">
                    <h2>Suất chiếu</h2>
                    <p>Giờ kết thúc = giờ bắt đầu + thời lượng phim. Hai suất cùng phòng phải cách nhau tối thiểu 15 phút để dọn phòng.</p>
                </div>
                <form action="${ctx}/admin" method="post" class="toolbar">
                    <input type="hidden" name="csrf" value="${csrfToken}">
                    <input type="hidden" name="action" value="addShowtime">
                    <select name="movieID" class="input" required aria-label="Phim">
                        <c:forEach items="${moviesList}" var="m">
                            <option value="${m.movieID}"><c:out value="${m.movieName}"/> (${m.duration}')</option>
                        </c:forEach>
                    </select>
                    <select name="roomID" class="input" required aria-label="Phòng">
                        <c:forEach items="${roomsList}" var="r">
                            <option value="${r.roomID}" ${r.status != 'STANDBY' ? 'disabled' : ''}>
                                <c:out value="${r.roomName}"/><c:if test="${r.status != 'STANDBY'}"> (${r.status})</c:if>
                            </option>
                        </c:forEach>
                    </select>
                    <input type="datetime-local" name="startTime" class="input" required aria-label="Giờ bắt đầu">
                    <select name="status" class="input" aria-label="Trạng thái">
                        <option value="UPCOMING">UPCOMING</option>
                        <option value="PLAYING">PLAYING</option>
                    </select>
                    <button type="submit" class="btn btn-primary btn-sm">+ Tạo suất</button>
                </form>

                <div class="table-wrap">
                    <table class="table">
                        <thead>
                            <tr><th>Phim</th><th>Phòng</th><th>Giờ chiếu</th><th>Trạng thái</th><th>Đổi trạng thái</th><th></th></tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${showtimesList}" var="st">
                                <tr>
                                    <td>
                                        <strong><c:out value="${movieMap[st.movieID].movieName}"/></strong>
                                    </td>
                                    <td><c:out value="${roomMap[st.roomID].roomName}"/></td>
                                    <td>${f:datetime(st.startTime)} → ${f:time(st.endTime)}</td>
                                    <td>
                                        <span class="badge ${st.status == 'PLAYING' ? 'b-ok' : (st.status == 'UPCOMING' ? 'b-info' : (st.status == 'CANCELLED' ? 'b-err' : ''))}">${st.status}</span>
                                    </td>
                                    <td>
                                        <c:set var="nexts" value="${showtimeNext[st.status]}"/>
                                        <c:choose>
                                            <c:when test="${empty nexts}"><span class="muted">—</span></c:when>
                                            <c:otherwise>
                                                <form action="${ctx}/admin" method="post" class="inline">
                                                    <input type="hidden" name="csrf" value="${csrfToken}">
                                                    <input type="hidden" name="action" value="updateShowtimeStatus">
                                                    <input type="hidden" name="showtimeID" value="${st.showtimeID}">
                                                    <select name="status" class="input" aria-label="Trạng thái mới">
                                                        <c:forEach items="${nexts}" var="s"><option value="${s}">${s}</option></c:forEach>
                                                    </select>
                                                    <button type="submit" class="btn btn-ghost btn-sm">Lưu</button>
                                                </form>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="right">
                                        <form action="${ctx}/admin" method="post" class="inline" data-confirm="Xoá suất chiếu này?">
                                            <input type="hidden" name="csrf" value="${csrfToken}">
                                            <input type="hidden" name="action" value="deleteShowtime">
                                            <input type="hidden" name="showtimeID" value="${st.showtimeID}">
                                            <button type="submit" class="btn btn-danger btn-sm">Xoá</button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </section>

            <%-- ============ Phòng chiếu & Thể loại ============ --%>
            <div class="cols" style="margin-bottom:22px">
                <section id="rooms" class="panel">
                    <div class="panel-head">
                        <h2>Phòng chiếu</h2>
                        <p>Chỉ phòng STANDBY mới xếp được suất chiếu. Dùng CLOSED thay cho việc xoá phòng.</p>
                    </div>
                    <c:if test="${isSuperAdmin}">
                        <form action="${ctx}/admin" method="post" class="toolbar">
                            <input type="hidden" name="csrf" value="${csrfToken}">
                            <input type="hidden" name="action" value="addRoom">
                            <input type="text" name="roomName" class="input" placeholder="Tên phòng" maxlength="50" required style="width:130px">
                            <input type="text" name="roomType" class="input" placeholder="Loại phòng" list="roomTypes" maxlength="50" required style="width:130px">
                            <datalist id="roomTypes">
                                <option value="2D Standard"></option>
                                <option value="IMAX 3D"></option>
                                <option value="VIP Gold"></option>
                            </datalist>
                            <input type="number" name="rows" class="input" value="5" min="1" max="15" required style="width:70px" title="Số hàng ghế (A, B, C...)">
                            <input type="number" name="seatsPerRow" class="input" value="10" min="1" max="20" required style="width:70px" title="Số ghế mỗi hàng">
                            <button type="submit" class="btn btn-primary btn-sm">+ Tạo phòng</button>
                        </form>
                    </c:if>
                    <div class="table-wrap">
                        <table class="table">
                            <thead><tr><th>Phòng</th><th>Loại</th><th>Ghế</th><th>Trạng thái</th></tr></thead>
                            <tbody>
                                <c:forEach items="${roomsList}" var="r">
                                    <tr>
                                        <td><strong><c:out value="${r.roomName}"/></strong></td>
                                        <td><c:out value="${r.roomType}"/></td>
                                        <td>${empty seatCountMap[r.roomID] ? 0 : seatCountMap[r.roomID]}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${isSuperAdmin}">
                                                    <form action="${ctx}/admin" method="post" class="inline">
                                                        <input type="hidden" name="csrf" value="${csrfToken}">
                                                        <input type="hidden" name="action" value="updateRoomStatus">
                                                        <input type="hidden" name="roomID" value="${r.roomID}">
                                                        <select name="status" class="input" aria-label="Trạng thái phòng">
                                                            <c:forEach items="${roomStatuses}" var="rs">
                                                                <option value="${rs}" ${r.status == rs ? 'selected' : ''}>${rs}</option>
                                                            </c:forEach>
                                                        </select>
                                                        <button type="submit" class="btn btn-ghost btn-sm">Lưu</button>
                                                    </form>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge ${r.status == 'STANDBY' ? 'b-ok' : 'b-err'}">${r.status}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </section>

                <section id="genres" class="panel">
                    <div class="panel-head">
                        <h2>Thể loại</h2>
                        <p>Chỉ xoá được thể loại không còn phim nào dùng.</p>
                    </div>
                    <form action="${ctx}/admin" method="post" class="toolbar">
                        <input type="hidden" name="csrf" value="${csrfToken}">
                        <input type="hidden" name="action" value="addGenre">
                        <input type="text" name="genreName" class="input" placeholder="Tên thể loại mới" maxlength="100" required>
                        <button type="submit" class="btn btn-primary btn-sm">+ Thêm</button>
                    </form>
                    <div class="pills">
                        <c:forEach items="${genresList}" var="g">
                            <form action="${ctx}/admin" method="post" class="pill" data-confirm="Xoá thể loại này?">
                                <input type="hidden" name="csrf" value="${csrfToken}">
                                <input type="hidden" name="action" value="deleteGenre">
                                <input type="hidden" name="genreID" value="${g.genreID}">
                                <c:out value="${g.genreName}"/>
                                <button type="submit" title="Xoá thể loại" aria-label="Xoá thể loại">✕</button>
                            </form>
                        </c:forEach>
                    </div>
                </section>
            </div>

            <c:if test="${isSuperAdmin}">
                <%-- ============ Đơn đặt vé (chỉ SUPER_ADMIN) ============ --%>
                <section id="bookings" class="panel">
                    <div class="panel-head">
                        <h2>Đơn đặt vé</h2>
                        <p>Đơn chuyển sang CANCELLED sẽ giải phóng ghế. Đơn CANCELLED không thể mở lại.</p>
                    </div>
                    <div class="table-wrap">
                        <table class="table">
                            <thead>
                                <tr><th>Mã</th><th>Khách hàng</th><th>Phim / suất</th><th>Vé</th><th>Tổng tiền</th><th>Trạng thái</th><th>Đổi trạng thái</th></tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${bookingsList}" var="b">
                                    <c:set var="bst" value="${showtimeMap[b.showtimeID]}"/>
                                    <tr>
                                        <td>#${b.bookingID}</td>
                                        <td>
                                            <c:out value="${userMap[b.userID].fullName}"/>
                                            <span class="sub">${f:datetime(b.bookingTime)}</span>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty bst and not empty movieMap[bst.movieID]}">
                                                    <c:out value="${movieMap[bst.movieID].movieName}"/>
                                                    <span class="sub">${f:datetime(bst.startTime)}</span>
                                                </c:when>
                                                <c:otherwise>Suất #${b.showtimeID}</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>${empty ticketCountMap[b.bookingID] ? 0 : ticketCountMap[b.bookingID]}</td>
                                        <td>${f:money(b.totalAmount)}</td>
                                        <td>
                                            <span class="badge ${b.status == 'COMPLETED' ? 'b-ok' : (b.status == 'PENDING' ? 'b-warn' : 'b-err')}">${b.status}</span>
                                        </td>
                                        <td>
                                            <c:set var="bnexts" value="${bookingNext[b.status]}"/>
                                            <c:choose>
                                                <c:when test="${empty bnexts}"><span class="muted">—</span></c:when>
                                                <c:otherwise>
                                                    <form action="${ctx}/admin" method="post" class="inline">
                                                        <input type="hidden" name="csrf" value="${csrfToken}">
                                                        <input type="hidden" name="action" value="updateBookingStatus">
                                                        <input type="hidden" name="bookingID" value="${b.bookingID}">
                                                        <select name="status" class="input" aria-label="Trạng thái mới">
                                                            <c:forEach items="${bnexts}" var="s"><option value="${s}">${s}</option></c:forEach>
                                                        </select>
                                                        <button type="submit" class="btn btn-ghost btn-sm">Lưu</button>
                                                    </form>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </section>

                <%-- ============ Khách hàng (chỉ SUPER_ADMIN) ============ --%>
                <section id="users" class="panel">
                    <div class="panel-head">
                        <h2>Khách hàng</h2>
                        <p>Khách đã có đơn đặt vé không thể xoá (cần giữ dữ liệu doanh thu).</p>
                    </div>
                    <div class="table-wrap">
                        <table class="table">
                            <thead><tr><th>Họ tên</th><th>Tài khoản</th><th>Email</th><th>Điện thoại</th><th>Giới tính</th><th></th></tr></thead>
                            <tbody>
                                <c:forEach items="${usersList}" var="u">
                                    <tr>
                                        <td><strong><c:out value="${u.fullName}"/></strong></td>
                                        <td><c:out value="${u.userName}"/></td>
                                        <td><c:out value="${u.email}"/></td>
                                        <td><c:out value="${u.phone}"/></td>
                                        <td><span class="badge">${u.gender == 'MALE' ? 'Nam' : 'Nữ'}</span></td>
                                        <td class="right">
                                            <form action="${ctx}/admin" method="post" class="inline" data-confirm="Xoá khách hàng này?">
                                                <input type="hidden" name="csrf" value="${csrfToken}">
                                                <input type="hidden" name="action" value="deleteUser">
                                                <input type="hidden" name="userID" value="${u.userID}">
                                                <button type="submit" class="btn btn-danger btn-sm">Xoá</button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </section>
            </c:if>
        </div>
    </div>
</main>

<%-- Hộp thoại thêm / sửa phim (dùng chung; app.js đổi action và điền dữ liệu) --%>
<dialog id="movieDialog">
    <h2 id="movieDialogTitle">Thêm phim mới</h2>
    <form id="movieForm" action="${ctx}/admin" method="post">
        <input type="hidden" name="csrf" value="${csrfToken}">
        <input type="hidden" name="action" value="addMovie">
        <input type="hidden" name="movieID" value="">

        <div class="field">
            <label for="f_movieName">Tên phim</label>
            <input class="input" type="text" id="f_movieName" name="movieName" maxlength="200" required>
        </div>
        <div class="row2">
            <div class="field">
                <label for="f_director">Đạo diễn</label>
                <input class="input" type="text" id="f_director" name="director" maxlength="150" required>
            </div>
            <div class="field">
                <label for="f_actor">Diễn viên</label>
                <input class="input" type="text" id="f_actor" name="actor" maxlength="300" required>
            </div>
        </div>
        <div class="row4">
            <div class="field">
                <label for="f_duration">Thời lượng (phút)</label>
                <input class="input" type="number" id="f_duration" name="duration" value="120" min="1" max="600" required>
            </div>
            <div class="field">
                <label for="f_ageRating">Độ tuổi</label>
                <select class="input" id="f_ageRating" name="ageRating" required>
                    <c:forEach items="${ageRatings}" var="a">
                        <option value="${a}" ${a == 'T16' ? 'selected' : ''}>${a}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="field">
                <label for="f_ratingScore">Điểm (0-10)</label>
                <input class="input" type="number" id="f_ratingScore" name="ratingScore" value="8.0" min="0" max="10" step="0.1" required>
            </div>
            <div class="field">
                <label for="f_language">Ngôn ngữ</label>
                <input class="input" type="text" id="f_language" name="language" value="Phụ đề Việt" maxlength="50" required>
            </div>
        </div>
        <div class="field">
            <label for="f_releaseDate">Ngày khởi chiếu (để trống = hôm nay)</label>
            <input class="input" type="date" id="f_releaseDate" name="releaseDate">
        </div>
        <div class="field">
            <span class="label">Thể loại (chọn ít nhất 1)</span>
            <div class="checks">
                <c:forEach items="${genresList}" var="g">
                    <label><input type="checkbox" name="genreIDs" value="${g.genreID}"> <c:out value="${g.genreName}"/></label>
                </c:forEach>
                <c:if test="${empty genresList}"><span class="muted">Chưa có thể loại — hãy thêm ở mục Thể loại trước.</span></c:if>
            </div>
        </div>
        <div class="field">
            <label for="f_bio">Tóm tắt nội dung</label>
            <textarea class="input" id="f_bio" name="bio" rows="3" maxlength="4000" required></textarea>
        </div>
        <div class="row2">
            <div class="field">
                <label for="f_posterURL">Link poster (không bắt buộc)</label>
                <input class="input" type="text" id="f_posterURL" name="posterURL" maxlength="500" placeholder="https://...">
                <span class="field-hint">Để trống thì hệ thống tự vẽ poster.</span>
            </div>
            <div class="field">
                <label for="f_trailerURL">Link trailer (không bắt buộc)</label>
                <input class="input" type="text" id="f_trailerURL" name="trailerURL" maxlength="500" placeholder="https://...">
            </div>
        </div>
        <div class="dialog-actions">
            <button type="button" class="btn btn-ghost" data-close>Huỷ</button>
            <button type="submit" id="movieSubmitBtn" class="btn btn-primary">Lưu phim</button>
        </div>
    </form>
</dialog>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
