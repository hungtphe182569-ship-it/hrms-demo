<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0">
<title>PeopleFlow HRMS</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css"></head>
<body class="landing-page">
<header class="landing-nav"><a class="landing-brand" href="${pageContext.request.contextPath}/"><span class="brand-mark">P</span><span><strong>PeopleFlow</strong><small>Human Resources Suite</small></span></a>
    <nav><a href="#features">Tính năng</a><a href="#roles">Vai trò</a>
        <c:choose><c:when test="${not empty sessionScope.currentUser}"><a class="button primary" href="${pageContext.request.contextPath}/home">Vào dashboard →</a></c:when><c:otherwise><a class="button primary" href="${pageContext.request.contextPath}/login">Đăng nhập →</a></c:otherwise></c:choose>
    </nav></header>
<main>
    <section class="landing-hero"><div class="landing-copy"><p class="eyebrow">HUMAN RESOURCE MANAGEMENT SYSTEM</p><h1>Một nơi cho con người,<br><em>công việc và tăng trưởng.</em></h1><p>PeopleFlow kết nối nhân viên, quản lý và bộ phận quản trị trên một nền tảng HRMS an toàn, minh bạch.</p>
        <div class="landing-actions"><a class="button primary landing-cta" href="${pageContext.request.contextPath}/login">Bắt đầu đăng nhập</a><a class="button ghost" href="#roles">Khám phá theo role</a></div></div>
        <div class="landing-visual"><div class="visual-card visual-main"><span>PEOPLEFLOW / LIVE</span><strong>HR operations,<br>beautifully connected.</strong><div class="visual-metrics"><i></i><i></i><i></i><i></i></div></div><div class="visual-card visual-float"><b>3</b><span>Role dashboards</span></div></div>
    </section>
    <section class="landing-features" id="features"><article><span>01</span><h2>Quản trị an toàn</h2><p>RBAC, audit trail, quản lý tài khoản và báo cáo.</p></article><article><span>02</span><h2>Quản lý đội ngũ</h2><p>Theo dõi nhân sự và công việc của Manager.</p></article><article><span>03</span><h2>Trải nghiệm nhân viên</h2><p>Không gian cá nhân cho hồ sơ, chấm công và nghỉ phép.</p></article></section>
    <section class="role-showcase" id="roles"><div><p class="eyebrow">PERSONALIZED WORKSPACE</p><h2>Mỗi role, một dashboard phù hợp</h2></div><div class="role-showcase-grid"><article class="role-preview admin"><span>ADMIN</span><strong>System control</strong><small>Accounts · Roles · Reports · Audit</small></article><article class="role-preview manager"><span>MANAGER</span><strong>Team workspace</strong><small>Team · Attendance · Approvals</small></article><article class="role-preview employee"><span>EMPLOYEE</span><strong>My workspace</strong><small>Profile · Attendance · Leave</small></article></div></section>
</main><footer class="landing-footer"><strong>PeopleFlow HRMS</strong><span>© 2026 Group 5 · SWD392</span></footer>
</body></html>
