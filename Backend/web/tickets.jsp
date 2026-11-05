<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="/WEB-INF/cinema.tld" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="pageTitle" value="Vé của tôi"/>
<c:set var="nav" value="tickets"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<main class="wrap">
    <div class="page-head">
        <h1>Vé của tôi</h1>
        <p>Lịch sử đặt vé của <strong><c:out value="${sessionScope.currentUser.fullName}"/></strong></p>
    </div>

    <c:if test="${not empty bookedId}">
        <div class="alert alert-ok" role="status" data-autohide>Đặt vé thành công! Mã đơn của bạn là <strong>#<c:out value="${bookedId}"/></strong>.</div>
    </c:if>
    <c:choose>
        <c:when test="${msg == 'cancelled'}"><div class="alert alert-ok" role="status" data-autohide>Đã huỷ đơn đặt vé. Các ghế đã được trả lại.</div></c:when>
        <c:when test="${msg == 'paid'}"><div class="alert alert-ok" role="status" data-autohide>Thanh toán thành công.</div></c:when>
        <c:when test="${msg == 'error'}"><div class="alert alert-err" role="alert">Không thực hiện được thao tác này với đơn đã chọn.</div></c:when>
    </c:choose>

    <c:choose>
        <c:when test="${empty bookings}">
            <div class="empty">
                <h3>Bạn chưa đặt vé nào</h3>
                <p>Chọn một bộ phim và suất chiếu để bắt đầu.</p>
                <a class="btn btn-primary" href="${ctx}/home">Xem phim đang chiếu</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="tickets">
                <c:forEach items="${bookings}" var="b">
                    <c:set var="bk" value="${b.booking}"/>
                    <c:set var="canChange" value="${not empty b.showtime and b.showtime.status == 'UPCOMING'}"/>
                    <article class="ticket ${bk.status == 'CANCELLED' ? 'cancelled' : ''}">
                        <c:choose>
                            <c:when test="${not empty b.movie}"><t:poster movie="${b.movie}"/></c:when>
                            <c:otherwise><div class="poster" style="--h:210"></div></c:otherwise>
                        </c:choose>

                        <div class="ticket-body">
                            <div class="ticket-head">
                                <h3>
                                    <c:choose>
                                        <c:when test="${not empty b.movie}"><c:out value="${b.movie.movieName}"/></c:when>
                                        <c:otherwise>Phim đã bị xoá</c:otherwise>
                                    </c:choose>
                                </h3>
                                <c:choose>
                                    <c:when test="${bk.status == 'COMPLETED'}"><span class="badge b-ok">Đã thanh toán</span></c:when>
                                    <c:when test="${bk.status == 'PENDING'}"><span class="badge b-warn">Chờ thanh toán</span></c:when>
                                    <c:otherwise><span class="badge b-err">Đã huỷ</span></c:otherwise>
                                </c:choose>
                            </div>

                            <dl class="ticket-facts">
                                <div><dt>Suất chiếu</dt><dd>${empty b.showtime ? '—' : f:datetime(b.showtime.startTime)}</dd></div>
                                <div><dt>Phòng</dt><dd><c:out value="${empty b.room ? '—' : b.room.roomName}"/></dd></div>
                                <div><dt>Ghế (${b.seatCount})</dt><dd><c:out value="${empty b.seatNames ? '—' : b.seatNames}"/></dd></div>
                                <div><dt>Tổng tiền</dt><dd>${f:money(bk.totalAmount)}</dd></div>
                            </dl>

                            <div class="ticket-foot">
                                <span>Mã đơn #${bk.bookingID} · đặt lúc ${f:datetime(bk.bookingTime)}</span>
                                <c:if test="${bk.status != 'CANCELLED' and canChange}">
                                    <div class="ticket-actions">
                                        <c:if test="${bk.status == 'PENDING'}">
                                            <form method="post" action="${ctx}/tickets">
                                                <input type="hidden" name="action" value="pay">
                                                <input type="hidden" name="bookingID" value="${bk.bookingID}">
                                                <button type="submit" class="btn btn-primary btn-sm">Thanh toán</button>
                                            </form>
                                        </c:if>
                                        <form method="post" action="${ctx}/tickets" data-confirm="Huỷ đơn #${bk.bookingID}? Các ghế sẽ được trả lại.">
                                            <input type="hidden" name="action" value="cancel">
                                            <input type="hidden" name="bookingID" value="${bk.bookingID}">
                                            <button type="submit" class="btn btn-danger btn-sm">Huỷ đơn</button>
                                        </form>
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </article>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
