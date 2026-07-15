<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0">
<title>Đăng nhập | PeopleFlow HRMS</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css"></head>
<body class="auth-page"><main class="auth-layout"><section class="auth-story">
    <a class="auth-brand" href="${pageContext.request.contextPath}/"><span class="brand-mark">P</span><span><strong>PeopleFlow</strong><small>Human Resources Suite</small></span></a>
    <div class="auth-copy"><p class="eyebrow">QUẢN TRỊ NHÂN SỰ THÔNG MINH</p><h1>Nơi con người và công việc cùng phát triển.</h1><p>Mỗi vai trò được đưa đến một workspace phù hợp sau khi đăng nhập.</p></div>
    <div class="auth-points"><span>✓ Dữ liệu tập trung</span><span>✓ Phân quyền rõ ràng</span><span>✓ Dashboard theo role</span></div>
</section><section class="auth-form-side"><div class="login-card">
    <div class="login-heading"><span class="login-symbol">PF</span><div><p class="eyebrow">WELCOME BACK</p><h2>Đăng nhập hệ thống</h2></div></div><p class="login-intro">Hệ thống sẽ tự nhận role và chuyển đến dashboard tương ứng.</p>
    <c:if test="${param.logout == '1'}"><div class="flash success"><span>✓</span>Đăng xuất thành công.</div></c:if><c:if test="${not empty error}"><div class="flash error"><span>!</span><c:out value="${error}"/></div></c:if>
    <form method="post" class="stack-form"><label><span>Tên đăng nhập</span><input name="username" autocomplete="username" required autofocus value="<c:out value='${username}'/>" placeholder="Nhập tên đăng nhập"></label><label><span>Mật khẩu</span><div class="password-field"><input id="loginPassword" name="password" type="password" autocomplete="current-password" required placeholder="Nhập mật khẩu"><button type="button" class="password-toggle" data-toggle-password="loginPassword">Hiện</button></div></label><button class="button primary auth-submit" type="submit">Đăng nhập <span>→</span></button></form>
    <div class="demo-roles"><div><strong>Admin</strong><span>admin / Admin@123</span></div><div><strong>HR Manager</strong><span>hrm01 / Hrm@123</span></div><div><strong>Manager</strong><span>manager01 / Manager@123</span></div><div><strong>Employee</strong><span>employee01 / Employee@123</span></div></div>
</div><p class="auth-footer">© 2026 PeopleFlow · HRMS Group 5</p></section></main><script src="${pageContext.request.contextPath}/assets/js/app.js"></script></body></html>
