<%@ page isErrorPage="true" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="code" value="${pageContext.errorData.statusCode}"/>
<c:set var="pageTitle" value="${code == 404 ? 'Không tìm thấy trang' : 'Có lỗi xảy ra'}"/>
<c:set var="nav" value=""/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<main class="wrap error-page">
    <div class="code">${code == 404 ? '404' : 'Oops'}</div>
    <h1>${code == 404 ? 'Không tìm thấy trang bạn cần' : 'Hệ thống đang gặp sự cố'}</h1>
    <p>
        ${code == 404 ? 'Đường dẫn có thể đã thay đổi, hoặc phim / suất chiếu này không còn tồn tại.'
                      : 'Xin lỗi vì sự bất tiện này. Vui lòng thử lại sau ít phút.'}
    </p>
    <a class="btn btn-primary" href="${ctx}/home">Về trang chủ</a>
</main>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
