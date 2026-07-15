<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Báo cáo & thống kê" scope="request"/>
<%@ include file="partials/header.jspf" %>

<section class="summary-strip green">
    <div><span>PHÂN TÍCH NHÂN SỰ</span><strong>Bức tranh tổng quan</strong><small>Số liệu được tổng hợp trực tiếp từ hệ thống</small></div>
    <div class="rule-note"><b>Dữ liệu trực tiếp</b><span>Tự động cập nhật sau mỗi thao tác</span></div>
</section>

<section class="metric-grid">
    <article class="metric-card"><span class="metric-icon blue">Σ</span><div><small>Total accounts</small><strong>${stats.totalAccounts}</strong><p>Including soft-deleted records</p></div></article>
    <article class="metric-card"><span class="metric-icon green">✓</span><div><small>Active accounts</small><strong>${stats.activeAccounts}</strong><p>Available for system access</p></div></article>
    <article class="metric-card"><span class="metric-icon orange">!</span><div><small>Locked accounts</small><strong>${stats.lockedAccounts}</strong><p>Require Admin review</p></div></article>
    <article class="metric-card"><span class="metric-icon purple">R</span><div><small>System roles</small><strong>${stats.totalRoles}</strong><p>RBAC role definitions</p></div></article>
</section>

<section class="chart-grid">
    <article class="panel chart-panel">
        <div class="panel-heading"><div><h2>Accounts by status</h2><p>Current account lifecycle distribution</p></div><span class="status active"><i></i>Live data</span></div>
        <div class="bar-chart" data-chart>
            <c:forEach var="entry" items="${stats.accountsByStatus}">
                <div class="bar-row"><span><c:out value="${entry.key}"/></span><div class="bar-track"><i data-value="${entry.value}"></i></div><strong>${entry.value}</strong></div>
            </c:forEach>
        </div>
    </article>
    <article class="panel chart-panel">
        <div class="panel-heading"><div><h2>Accounts by role</h2><p>Active assignment through user_roles</p></div><span class="role-pill">RBAC</span></div>
        <div class="donut-layout">
            <div class="donut" style="--a:${stats.activeAccounts}; --b:${stats.lockedAccounts}; --c:${stats.deletedAccounts}"><span><strong>${stats.totalAccounts}</strong><small>accounts</small></span></div>
            <div class="role-stat-list">
                <c:forEach var="entry" items="${stats.accountsByRole}">
                    <div><span><i></i><c:out value="${entry.key}"/></span><strong>${entry.value}</strong></div>
                </c:forEach>
            </div>
        </div>
    </article>
</section>

<section class="panel insight-panel">
    <div><p class="eyebrow">DEMO TALKING POINT</p><h2>Data is recalculated after every Admin operation</h2><p>Hãy tạo hoặc soft delete một account ở UC17, sau đó quay lại trang này. Các COUNT và GROUP BY sẽ phản ánh dữ liệu mới trong SQL Server.</p></div>
    <a class="button primary" href="${pageContext.request.contextPath}/accounts">Open account management →</a>
</section>

<%@ include file="partials/footer.jspf" %>
