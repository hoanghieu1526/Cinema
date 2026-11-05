<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="/WEB-INF/cinema.tld" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="pageTitle" value="Phim đang chiếu"/>
<c:set var="nav" value="movies"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<main>
    <%-- Banner phim nổi bật (chỉ hiện khi không lọc / tìm kiếm) --%>
    <c:if test="${not empty heroMovie}">
        <section class="hero" style="--h:${f:hue(heroMovie.movieID)}">
            <div class="wrap hero-inner">
                <div>
                    <span class="eyebrow">Phim nổi bật</span>
                    <h1><c:out value="${heroMovie.movieName}"/></h1>
                    <div class="chips">
                        <span class="badge b-age"><c:out value="${heroMovie.ageRating}"/></span>
                        <span class="badge b-gold">★ ${heroMovie.ratingScore}</span>
                        <span class="badge">${heroMovie.duration} phút</span>
                        <span class="badge"><c:out value="${heroMovie.category}"/></span>
                    </div>
                    <p class="hero-bio"><c:out value="${heroMovie.bio}"/></p>
                    <div class="actions">
                        <a class="btn btn-primary" href="${ctx}/movie?id=${heroMovie.movieID}">Đặt vé ngay</a>
                        <c:if test="${not empty heroMovie.trailerURL}">
                            <a class="btn btn-ghost" href="<c:out value='${heroMovie.trailerURL}'/>" target="_blank" rel="noopener">Xem trailer</a>
                        </c:if>
                    </div>
                </div>
                <t:poster movie="${heroMovie}" cls="hero-poster"/>
            </div>
        </section>
    </c:if>

    <section class="wrap section">
        <div class="section-head">
            <h2>${empty q ? 'Danh sách phim' : 'Kết quả tìm kiếm'}</h2>
            <div class="tabs">
                <a class="tab ${tab == 'all' ? 'on' : ''}" href="${ctx}/home?tab=all&amp;genre=${genreID}&amp;q=${qParam}">Tất cả<small>${totalCount}</small></a>
                <a class="tab ${tab == 'now' ? 'on' : ''}" href="${ctx}/home?tab=now&amp;genre=${genreID}&amp;q=${qParam}">Đang chiếu<small>${nowCount}</small></a>
                <a class="tab ${tab == 'soon' ? 'on' : ''}" href="${ctx}/home?tab=soon&amp;genre=${genreID}&amp;q=${qParam}">Sắp chiếu<small>${soonCount}</small></a>
            </div>
        </div>

        <div class="genres">
            <a class="chip ${genreID == 0 ? 'on' : ''}" href="${ctx}/home?tab=${tab}&amp;q=${qParam}">Tất cả thể loại</a>
            <c:forEach items="${genres}" var="g">
                <a class="chip ${genreID == g.genreID ? 'on' : ''}"
                   href="${ctx}/home?tab=${tab}&amp;genre=${g.genreID}&amp;q=${qParam}"><c:out value="${g.genreName}"/></a>
            </c:forEach>
        </div>

        <c:choose>
            <c:when test="${empty movies}">
                <div class="empty">
                    <h3>Không tìm thấy phim phù hợp</h3>
                    <p>Thử đổi từ khoá hoặc bỏ bớt bộ lọc.</p>
                    <a class="btn btn-ghost" href="${ctx}/home">Xem tất cả phim</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="movie-grid">
                    <c:forEach items="${movies}" var="m">
                        <c:set var="status" value="${movieStatus[m.movieID]}"/>
                        <a class="movie-card" href="${ctx}/movie?id=${m.movieID}">
                            <div class="poster-box">
                                <t:poster movie="${m}"/>
                                <span class="tl badge b-age"><c:out value="${m.ageRating}"/></span>
                                <c:if test="${status == 'SOON'}"><span class="tr badge b-info">Sắp chiếu</span></c:if>
                                <c:if test="${status == 'END'}"><span class="tr badge">Hết suất</span></c:if>
                            </div>
                            <div>
                                <h3 class="movie-name"><c:out value="${m.movieName}"/></h3>
                                <p class="movie-sub"><c:out value="${m.category}"/></p>
                                <p class="movie-meta">
                                    <span class="rating">★ ${m.ratingScore}</span>
                                    <span>${m.duration} phút</span>
                                </p>
                                <c:if test="${status == 'NOW'}"><span class="btn btn-primary btn-sm btn-block">Đặt vé</span></c:if>
                            </div>
                        </a>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
