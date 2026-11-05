<%@ tag body-content="empty" pageEncoding="UTF-8" description="Poster phim: ảnh thật nếu có, nếu không thì tự vẽ nền màu kèm tên phim" %>
<%@ attribute name="movie" type="models.Movies" required="true" %>
<%@ attribute name="cls" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="f" uri="/WEB-INF/cinema.tld" %>
<%-- Dữ liệu mẫu dùng link example.com (không có ảnh thật) nên bỏ qua; ảnh lỗi sẽ tự gỡ để lộ poster vẽ sẵn. --%>
<c:set var="realPoster" value="${not empty movie.posterURL and not fn:contains(movie.posterURL, 'example.com')}"/>
<div class="poster ${cls}" style="--h:${f:hue(movie.movieID)}">
    <span class="poster-mono" aria-hidden="true"><c:out value="${fn:toUpperCase(fn:substring(movie.movieName, 0, 1))}"/></span>
    <c:if test="${realPoster}">
        <img src="<c:out value='${movie.posterURL}'/>" alt="" loading="lazy" onerror="this.remove()">
    </c:if>
</div>
