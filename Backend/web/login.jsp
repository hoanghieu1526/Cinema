<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="${tab == 'register' ? 'Đăng ký' : 'Đăng nhập'}"/>
<c:set var="nav" value=""/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<main class="wrap auth">
    <div class="auth-card">
        <h1>${tab == 'register' ? 'Tạo tài khoản' : 'Chào mừng trở lại'}</h1>
        <p>${tab == 'register' ? 'Đăng ký miễn phí để đặt vé và lưu lịch sử xem phim.' : 'Đăng nhập để đặt vé và xem lại vé của bạn.'}</p>

        <div class="tabs">
            <a class="tab ${tab == 'login' ? 'on' : ''}" href="${ctx}/auth?tab=login&amp;next=${nextParam}">Đăng nhập</a>
            <a class="tab ${tab == 'register' ? 'on' : ''}" href="${ctx}/auth?tab=register&amp;next=${nextParam}">Đăng ký</a>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-err" role="alert" style="margin-top:0"><c:out value="${error}"/></div>
        </c:if>

        <c:choose>
            <c:when test="${tab == 'register'}">
                <form method="post" action="${ctx}/auth">
                    <input type="hidden" name="action" value="register">
                    <input type="hidden" name="next" value="<c:out value='${next}'/>">

                    <div class="field">
                        <label for="fullName">Họ và tên</label>
                        <input class="input" id="fullName" name="fullName" required maxlength="100" autocomplete="name"
                               value="<c:out value='${form_fullName}'/>">
                    </div>
                    <div class="row2">
                        <div class="field">
                            <label for="r_userName">Tên đăng nhập</label>
                            <input class="input" id="r_userName" name="userName" required minlength="3" maxlength="30"
                                   pattern="[A-Za-z0-9_.]+" autocomplete="username" value="<c:out value='${form_userName}'/>">
                        </div>
                        <div class="field">
                            <label for="phone">Số điện thoại</label>
                            <input class="input" id="phone" name="phone" inputmode="tel" maxlength="15" autocomplete="tel"
                                   value="<c:out value='${form_phone}'/>">
                        </div>
                    </div>
                    <div class="field">
                        <label for="email">Email</label>
                        <input class="input" id="email" type="email" name="email" required maxlength="100" autocomplete="email"
                               value="<c:out value='${form_email}'/>">
                    </div>
                    <div class="row2">
                        <div class="field">
                            <label for="gender">Giới tính</label>
                            <select class="input" id="gender" name="gender">
                                <option value="MALE" ${form_gender == 'MALE' ? 'selected' : ''}>Nam</option>
                                <option value="FEMALE" ${form_gender == 'FEMALE' ? 'selected' : ''}>Nữ</option>
                            </select>
                        </div>
                        <div class="field">
                            <label for="dob">Ngày sinh</label>
                            <input class="input" id="dob" type="date" name="dateOfBirth" value="<c:out value='${form_dateOfBirth}'/>">
                        </div>
                    </div>
                    <div class="row2">
                        <div class="field">
                            <label for="r_password">Mật khẩu</label>
                            <input class="input" id="r_password" type="password" name="password" required minlength="6" maxlength="72"
                                   autocomplete="new-password">
                        </div>
                        <div class="field">
                            <label for="confirm">Nhập lại mật khẩu</label>
                            <input class="input" id="confirm" type="password" name="confirm" required minlength="6" maxlength="72"
                                   autocomplete="new-password">
                        </div>
                    </div>
                    <button type="submit" class="btn btn-primary btn-block">Tạo tài khoản</button>
                </form>
            </c:when>

            <c:otherwise>
                <form method="post" action="${ctx}/auth">
                    <input type="hidden" name="action" value="login">
                    <input type="hidden" name="next" value="<c:out value='${next}'/>">

                    <div class="field">
                        <label for="userName">Tên đăng nhập hoặc email</label>
                        <input class="input" id="userName" name="userName" required autofocus autocomplete="username"
                               value="<c:out value='${param.userName}'/>">
                    </div>
                    <div class="field">
                        <label for="password">Mật khẩu</label>
                        <input class="input" id="password" type="password" name="password" required autocomplete="current-password">
                    </div>
                    <button type="submit" class="btn btn-primary btn-block">Đăng nhập</button>
                </form>
            </c:otherwise>
        </c:choose>
    </div>
</main>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
