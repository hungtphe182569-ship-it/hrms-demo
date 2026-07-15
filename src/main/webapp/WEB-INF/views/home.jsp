<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Tổng quan" scope="request"/>
<%@ include file="partials/header.jspf" %>
<section class="welcome-banner">
    <div><p class="eyebrow">CHÀO MỪNG TRỞ LẠI</p><h2>Xin chào, <c:out value="${sessionScope.currentUser.fullName}"/>!</h2><p>Mọi hoạt động nhân sự đang được cập nhật. Đây là tổng quan hệ thống hôm nay.</p></div>
    <a class="button welcome-action" href="${pageContext.request.contextPath}/accounts">Quản lý nhân sự →</a>
</section>
<section class="metric-grid home-metrics">
    <article class="metric-card"><span class="metric-icon blue">NV</span><div><small>Tổng tài khoản</small><strong>${stats.totalAccounts}</strong><p>Tất cả hồ sơ hệ thống</p></div></article>
    <article class="metric-card"><span class="metric-icon green">✓</span><div><small>Đang hoạt động</small><strong>${stats.activeAccounts}</strong><p>Tài khoản có thể truy cập</p></div></article>
    <article class="metric-card"><span class="metric-icon orange">!</span><div><small>Đang bị khóa</small><strong>${stats.lockedAccounts}</strong><p>Cần quản trị viên xử lý</p></div></article>
    <article class="metric-card"><span class="metric-icon purple">PQ</span><div><small>Vai trò</small><strong>${stats.totalRoles}</strong><p>Nhóm quyền trong hệ thống</p></div></article>
</section>
<section class="home-grid">
    <article class="panel quick-panel"><div class="panel-heading"><div><h2>Truy cập nhanh</h2><p>Các tác vụ quản trị thường dùng</p></div></div><div class="quick-links">
        <a href="${pageContext.request.contextPath}/accounts"><span>01</span><div><strong>Quản lý tài khoản</strong><small>Thêm mới, tìm kiếm và cập nhật truy cập</small></div><b>→</b></a>
        <a href="${pageContext.request.contextPath}/roles"><span>02</span><div><strong>Vai trò & phân quyền</strong><small>Thiết lập nhóm quyền cho nhân sự</small></div><b>→</b></a>
        <a href="${pageContext.request.contextPath}/reports"><span>03</span><div><strong>Xem báo cáo</strong><small>Theo dõi số liệu tổng quan hệ thống</small></div><b>→</b></a>
    </div></article>
    <article class="panel status-panel"><div class="panel-heading"><div><h2>Trạng thái hệ thống</h2><p>Cập nhật theo thời gian thực</p></div><span class="count-badge online">Hoạt động</span></div><div class="system-list"><div><span>Cơ sở dữ liệu</span><strong><i></i>Đã kết nối</strong></div><div><span>Phiên đăng nhập</span><strong><i></i>An toàn</strong></div><div><span>Tài khoản của bạn</span><strong><i></i>${sessionScope.currentUser.status}</strong></div></div></article>
</section>
<%@ include file="partials/footer.jspf" %>
