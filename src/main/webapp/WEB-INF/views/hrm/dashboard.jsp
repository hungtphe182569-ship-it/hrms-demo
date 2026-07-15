<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="HR Dashboard" scope="request"/>
<%@ include file="../partials/header.jspf" %>

<section class="welcome-banner admin-hero">
    <div><p class="eyebrow">HR MANAGER / UC-12 DASHBOARD</p>
        <h2>Xin chào, <c:out value="${sessionScope.currentUser.fullName}"/>!</h2>
        <p>Theo dõi KPI nhân sự, đơn nghỉ chờ duyệt và payroll đang chờ phê duyệt cuối.</p></div>
    <div class="hero-actions">
        <a class="button welcome-action" href="${pageContext.request.contextPath}/hrm/leaves">Duyệt leave →</a>
        <a class="button hero-secondary" href="${pageContext.request.contextPath}/hrm/payroll">Payroll →</a>
    </div>
</section>

<section class="metric-grid home-metrics">
    <article class="metric-card"><span class="metric-icon blue">EM</span><div><small>Total employees</small><strong>${stats.totalEmployees}</strong><p>Nhân viên ACTIVE</p></div></article>
    <article class="metric-card ${stats.pendingLeaves > 0 ? 'metric-warning' : ''}"><span class="metric-icon orange">LV</span><div><small>Pending leave</small><strong>${stats.pendingLeaves}</strong><p>Chờ HRM duyệt</p></div></article>
    <article class="metric-card"><span class="metric-icon purple">PR</span><div><small>Pending payroll</small><strong>${stats.pendingPayrollBatches}</strong><p>Batch SUBMITTED</p></div></article>
    <article class="metric-card"><span class="metric-icon green">$</span><div><small>Payroll queue</small><strong><fmt:formatNumber value="${stats.pendingPayrollAmount}" type="number"/></strong><p>Tổng net chờ duyệt</p></div></article>
</section>

<section class="hrm-tab-bar">
    <a class="active" href="${pageContext.request.contextPath}/hrm/dashboard">Dashboard</a>
    <a href="${pageContext.request.contextPath}/hrm/analytics">Analytics</a>
    <a href="${pageContext.request.contextPath}/hrm/activities">Activity Center</a>
</section>

<section class="dashboard-grid">
    <article class="panel chart-panel">
        <div class="panel-heading"><div><h2>Phân bố theo phòng ban</h2><p>Số nhân viên ACTIVE theo department</p></div><span class="status active"><i></i>Live</span></div>
        <c:choose><c:when test="${empty stats.employeesByDepartment}"><div class="empty-state">Không tải được biểu đồ phòng ban.</div></c:when><c:otherwise>
            <div class="bar-chart"><c:forEach var="entry" items="${stats.employeesByDepartment}">
                <div class="bar-row"><span><c:out value="${entry.key}"/></span><div class="bar-track"><i style="width:${entry.value * 18}%"></i></div><strong>${entry.value}</strong></div>
            </c:forEach></div>
        </c:otherwise></c:choose>
    </article>
    <div class="dashboard-side">
        <article class="panel">
            <div class="panel-heading"><div><h2>Sự kiện sắp tới</h2><p>Company calendar</p></div><a class="text-link" href="${pageContext.request.contextPath}/hrm/calendar">Mở lịch →</a></div>
            <div class="activity-list">
                <c:choose><c:when test="${empty stats.upcomingEvents}"><div class="empty-state">Chưa có sự kiện sắp tới.</div></c:when><c:otherwise>
                    <c:forEach var="event" items="${stats.upcomingEvents}">
                        <div class="activity-item"><span class="activity-icon">${event.eventType.substring(0,1)}</span>
                            <div><strong><c:out value="${event.title}"/></strong>
                                <small>${event.startAt.toLocalDate()} · <c:out value="${event.eventType}"/></small></div></div>
                    </c:forEach>
                </c:otherwise></c:choose>
            </div>
        </article>
        <article class="panel">
            <div class="panel-heading"><div><h2>Hoạt động gần đây</h2><p>Activity Center preview</p></div><a class="text-link" href="${pageContext.request.contextPath}/hrm/activities">Xem tất cả →</a></div>
            <div class="activity-list">
                <c:choose><c:when test="${empty stats.recentActivities}"><div class="empty-state">No activities recorded</div></c:when><c:otherwise>
                    <c:forEach var="activity" items="${stats.recentActivities}">
                        <div class="activity-item"><span class="activity-icon">${activity.actionName.substring(0,1)}</span>
                            <div><strong><c:out value="${activity.actionName}"/></strong>
                                <small><c:out value="${activity.actorName}"/> · ${activity.createdAt.toLocalDate()}</small>
                                <p><c:out value="${activity.details}"/></p></div></div>
                    </c:forEach>
                </c:otherwise></c:choose>
            </div>
        </article>
    </div>
</section>

<section class="panel quick-panel dashboard-quick"><div class="panel-heading"><div><h2>Thao tác nhanh</h2><p>UC12–UC16 HR Manager</p></div></div>
    <div class="quick-links quick-links-horizontal hrm-quick">
        <a href="${pageContext.request.contextPath}/hrm/leaves"><span>13</span><div><strong>Leave</strong><small>Approve / Reject / Revert</small></div><b>→</b></a>
        <a href="${pageContext.request.contextPath}/hrm/payroll"><span>14</span><div><strong>Payroll</strong><small>Final approve & CSV</small></div><b>→</b></a>
        <a href="${pageContext.request.contextPath}/hrm/departments"><span>15</span><div><strong>Departments</strong><small>CRUD + soft delete</small></div><b>→</b></a>
        <a href="${pageContext.request.contextPath}/hrm/calendar"><span>16</span><div><strong>Calendar</strong><small>Events & notify</small></div><b>→</b></a>
    </div>
</section>

<%@ include file="../partials/footer.jspf" %>
