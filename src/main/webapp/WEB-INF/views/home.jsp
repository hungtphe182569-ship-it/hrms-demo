<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Tổng quan Admin" scope="request"/>
<%@ include file="partials/header.jspf" %>

<section class="welcome-banner admin-hero">
    <div><p class="eyebrow">ADMIN CENTER / LIVE OVERVIEW</p><h2>Xin chào, <c:out value="${sessionScope.currentUser.fullName}"/>!</h2>
        <p>Theo dõi tài khoản, phân quyền và các hoạt động quản trị từ một nơi.</p></div>
    <div class="hero-actions"><a class="button welcome-action" href="${pageContext.request.contextPath}/accounts">+ Tạo tài khoản</a>
        <a class="button hero-secondary" href="${pageContext.request.contextPath}/reports">Xem báo cáo →</a></div>
</section>

<section class="metric-grid home-metrics">
    <article class="metric-card"><span class="metric-icon blue">AC</span><div><small>Tổng tài khoản</small><strong>${stats.totalAccounts}</strong><p>Bao gồm ${stats.deletedAccounts} record đã soft-delete</p></div></article>
    <article class="metric-card"><span class="metric-icon green">✓</span><div><small>Đang hoạt động</small><strong>${stats.activeAccounts}</strong><p>Có thể đăng nhập hệ thống</p></div></article>
    <article class="metric-card ${stats.lockedAccounts > 0 ? 'metric-warning' : ''}"><span class="metric-icon orange">!</span><div><small>Cần xử lý</small><strong>${stats.lockedAccounts}</strong><p>Tài khoản đang bị khóa</p></div></article>
    <article class="metric-card"><span class="metric-icon purple">RB</span><div><small>RBAC</small><strong>${stats.totalRoles}</strong><p>${stats.totalPermissions} permission đã khai báo</p></div></article>
</section>

<section class="dashboard-grid">
    <article class="panel dashboard-activity">
        <div class="panel-heading"><div><h2>Hoạt động quản trị gần đây</h2><p>Audit trail của các thay đổi quan trọng</p></div><a class="text-link" href="${pageContext.request.contextPath}/accounts?includeDeleted=true">Xem tài khoản →</a></div>
        <div class="activity-list">
            <c:choose><c:when test="${empty recentActivities}"><div class="empty-state">Chưa có hoạt động quản trị.</div></c:when><c:otherwise>
                <c:forEach var="activity" items="${recentActivities}">
                    <div class="activity-item"><span class="activity-icon">${activity.action.substring(0,1)}</span><div><strong><c:out value="${activity.action}"/> · @<c:out value="${activity.accountName}"/></strong>
                        <small>Thực hiện bởi @<c:out value="${activity.performedBy}"/> · ${activity.performedAt.toLocalDate()} ${activity.performedAt.toLocalTime().withSecond(0).withNano(0)}</small>
                        <c:if test="${not empty activity.reason}"><p><c:out value="${activity.reason}"/></p></c:if></div></div>
                </c:forEach>
            </c:otherwise></c:choose>
        </div>
    </article>

    <div class="dashboard-side">
        <article class="panel"><div class="panel-heading"><div><h2>Phân bố trạng thái</h2><p>Dữ liệu từ MySQL</p></div><span class="status active"><i></i>Live</span></div>
            <div class="compact-stats"><c:forEach var="entry" items="${stats.accountsByStatus}"><div><span><i class="status-dot ${entry.key.toLowerCase()}"></i><c:out value="${entry.key}"/></span><strong>${entry.value}</strong></div></c:forEach></div>
        </article>
        <article class="panel system-health"><div class="panel-heading"><div><h2>Trạng thái hệ thống</h2><p>Kiểm tra nhanh</p></div><span class="count-badge online">Hoạt động</span></div>
            <div class="system-list"><div><span>MySQL</span><strong><i></i>Đã kết nối</strong></div><div><span>RBAC</span><strong><i></i>${stats.totalPermissions} permissions</strong></div><div><span>Phiên hiện tại</span><strong><i></i>${sessionScope.currentUser.status}</strong></div></div>
        </article>
    </div>
</section>

<section class="panel quick-panel dashboard-quick"><div class="panel-heading"><div><h2>Thao tác nhanh</h2><p>Các use case Admin trong tài liệu</p></div></div><div class="quick-links quick-links-horizontal">
    <a href="${pageContext.request.contextPath}/accounts"><span>17</span><div><strong>Manage Account</strong><small>Tạo, cập nhật, trạng thái và soft delete</small></div><b>→</b></a>
    <a href="${pageContext.request.contextPath}/reports"><span>18</span><div><strong>Reports & Statistics</strong><small>Attendance, permission, Excel và PDF</small></div><b>→</b></a>
    <a href="${pageContext.request.contextPath}/roles"><span>19</span><div><strong>Manage Roles</strong><small>Quản lý role và phân quyền RBAC</small></div><b>→</b></a>
</div></section>

<%@ include file="partials/footer.jspf" %>
