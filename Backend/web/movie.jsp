<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="/WEB-INF/cinema.tld" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="pageTitle" value="${movie.movieName}"/>
<c:set var="nav" value="movies"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<main>
    <section class="wrap detail">
        <t:poster movie="${movie}"/>

        <div>
            <p class="crumb"><a href="${ctx}/home">Phim</a> / <c:out value="${movie.movieName}"/></p>
            <h1><c:out value="${movie.movieName}"/></h1>
            <div class="chips">
                <span class="badge b-age"><c:out value="${movie.ageRating}"/></span>
                <span class="badge b-gold">★ ${movie.ratingScore}</span>
                <span class="badge">${movie.duration} phút</span>
                <c:if test="${not empty movie.language}"><span class="badge"><c:out value="${movie.language}"/></span></c:if>
            </div>

            <p class="bio"><c:out value="${movie.bio}"/></p>

            <dl class="facts">
                <dt>Thể loại</dt><dd><c:out value="${movie.category}"/></dd>
                <dt>Đạo diễn</dt><dd><c:out value="${movie.director}"/></dd>
                <dt>Diễn viên</dt><dd><c:out value="${movie.actor}"/></dd>
                <c:if test="${not empty movie.releaseDate}">
                    <dt>Khởi chiếu</dt><dd>${f:date(movie.releaseDate)}</dd>
                </c:if>
            </dl>

            <c:if test="${not empty movie.trailerURL}">
                <a class="btn btn-ghost" href="<c:out value='${movie.trailerURL}'/>" target="_blank" rel="noopener">Xem trailer</a>
            </c:if>
        </div>
    </section>

    <section class="wrap section" id="showtimes">
        <div class="section-head"><h2>Lịch chiếu</h2></div>

        <c:choose>
            <c:when test="${hasShowtimes}">
                <c:forEach items="${showtimesByDay}" var="day">
                    <h3 class="day">${day.key}</h3>
                    <div class="slots">
                        <c:forEach items="${day.value}" var="st">
                            <c:set var="left" value="${seatsLeft[st.showtimeID]}"/>
                            <a class="slot ${left == 0 ? 'full' : ''}" href="${ctx}/booking?showtimeID=${st.showtimeID}">
                                <strong>${f:time(st.startTime)}</strong>
                                <span><c:out value="${roomMap[st.roomID].roomName}"/> · <c:out value="${roomMap[st.roomID].roomType}"/></span>
                                <span class="price">${f:money(price[st.showtimeID])}
                                    <small>· <c:choose>
                                        <c:when test="${left == 0}">Hết vé</c:when>
                                        <c:otherwise>Còn ${left} ghế</c:otherwise>
                                    </c:choose></small>
                                </span>
                            </a>
                        </c:forEach>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="empty">
                    <h3>Chưa có suất chiếu</h3>
                    <p>Phim này hiện chưa mở bán vé. Vui lòng quay lại sau.</p>
                    <a class="btn btn-ghost" href="${ctx}/home">Xem phim khác</a>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
