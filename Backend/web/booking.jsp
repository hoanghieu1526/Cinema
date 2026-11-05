<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="/WEB-INF/cinema.tld" %>
<c:set var="pageTitle" value="Chọn ghế · ${movie.movieName}"/>
<c:set var="nav" value="movies"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<main class="wrap">
    <div class="page-head">
        <p class="crumb">
            <a href="${ctx}/home">Phim</a> /
            <a href="${ctx}/movie?id=${movie.movieID}"><c:out value="${movie.movieName}"/></a> / Chọn ghế
        </p>
        <h1><c:out value="${movie.movieName}"/></h1>
        <p>
            ${f:day(showtime.startTime)} · ${f:time(showtime.startTime)} – ${f:time(showtime.endTime)}
            · <c:out value="${room.roomName}"/> (<c:out value="${room.roomType}"/>)
        </p>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-err" role="alert"><c:out value="${error}"/></div>
    </c:if>

    <form id="bookingForm" class="booking" method="post" action="${ctx}/booking"
          data-price="${price.intValue()}" data-max="${maxSeats}">
        <input type="hidden" name="showtimeID" value="${showtime.showtimeID}">

        <section class="panel">
            <c:if test="${otherShowtimes.size() > 1}">
                <div class="switcher" aria-label="Các suất chiếu khác của phim">
                    <c:forEach items="${otherShowtimes}" var="st">
                        <a href="${ctx}/booking?showtimeID=${st.showtimeID}"
                           class="${st.showtimeID == showtime.showtimeID ? 'on' : ''}">${f:datetime(st.startTime)}</a>
                    </c:forEach>
                </div>
            </c:if>

            <c:choose>
                <c:when test="${not open}">
                    <div class="alert alert-info">Suất chiếu này đã đóng bán vé. Hãy chọn suất khác ở trang phim.</div>
                </c:when>
                <c:otherwise>
                    <div class="screen">MÀN HÌNH</div>
                    <div class="seat-map">
                        <c:forEach items="${seatRows}" var="row">
                            <div class="seat-row">
                                <span class="row-label"><c:out value="${row.key}"/></span>
                                <c:forEach items="${row.value}" var="s">
                                    <c:set var="taken" value="${bookedSeatIds.contains(s.seatID)}"/>
                                    <label class="seat">
                                        <input type="checkbox" name="seatID" value="${s.seatID}"
                                               data-name="<c:out value='${s.seatName}'/>"
                                               ${taken ? 'disabled data-taken' : ''}>
                                        <span><c:out value="${s.seatName}"/></span>
                                    </label>
                                </c:forEach>
                            </div>
                        </c:forEach>
                    </div>
                    <div class="legend">
                        <span><i></i>Còn trống</span>
                        <span><i class="sel"></i>Đang chọn</span>
                        <span><i class="tak"></i>Đã đặt</span>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <aside class="panel summary">
            <h2>Thông tin đặt vé</h2>
            <dl>
                <dt>Suất chiếu</dt><dd>${f:time(showtime.startTime)} · ${f:date(showtime.startTime)}</dd>
                <dt>Phòng</dt><dd><c:out value="${room.roomName}"/></dd>
                <dt>Còn trống</dt><dd>${seatsLeft} / ${seatCount} ghế</dd>
                <dt>Giá mỗi ghế</dt><dd>${f:money(price)}</dd>
                <dt>Ghế đã chọn</dt><dd id="pickedSeats">Chưa chọn</dd>
                <dt>Số lượng</dt><dd id="pickedCount">0</dd>
            </dl>
            <div class="total">
                <span>Tổng cộng</span>
                <strong id="pickedTotal">0 đ</strong>
            </div>

            <c:choose>
                <c:when test="${not open}">
                    <button type="button" class="btn btn-primary btn-block" disabled data-locked>Đã đóng bán vé</button>
                </c:when>
                <c:when test="${not empty sessionScope.currentAdmin}">
                    <button type="button" class="btn btn-primary btn-block" disabled data-locked>Tài khoản quản trị không đặt vé</button>
                </c:when>
                <c:when test="${empty sessionScope.currentUser}">
                    <button type="submit" id="bookingSubmit" class="btn btn-primary btn-block">Đăng nhập để đặt vé</button>
                    <p class="hint">Bạn sẽ được chuyển tới trang đăng nhập.</p>
                </c:when>
                <c:otherwise>
                    <button type="submit" id="bookingSubmit" class="btn btn-primary btn-block">Xác nhận đặt vé</button>
                </c:otherwise>
            </c:choose>
            <p class="hint" id="bookingHint">Tối đa ${maxSeats} ghế mỗi lần đặt.</p>
        </aside>
    </form>
</main>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
