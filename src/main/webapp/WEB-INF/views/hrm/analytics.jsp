<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="../partials/header.jspf" %>

<section class="summary-strip">
    <div><span>UC-12.1 / HR ANALYTICS</span><strong>Phân tích nhân sự theo bộ lọc</strong><small>Department · Salary · Diversity · Training (read-only)</small></div>
    <div class="sequence"><i>1</i><span>Filter</span><b></b><i>2</i><span>Aggregate</span><b></b><i>3</i><span>Charts</span></div>
</section>

<section class="hrm-tab-bar">
    <a href="${pageContext.request.contextPath}/hrm/dashboard">Dashboard</a>
    <a class="active" href="${pageContext.request.contextPath}/hrm/analytics">Analytics</a>
    <a href="${pageContext.request.contextPath}/hrm/activities">Activity Center</a>
</section>

<section class="panel" style="margin-bottom:16px">
    <div class="panel-heading"><div><h2>Bộ lọc</h2><p>Mặc định: tất cả phòng ban</p></div></div>
    <form method="get" class="toolbar" style="padding:0 22px 18px;margin:0">
        <label style="min-width:260px"><span>Department</span>
            <select name="departmentId">
                <option value="">All departments</option>
                <c:forEach var="dept" items="${departments}">
                    <option value="${dept.id}" ${selectedDepartmentId == dept.id ? 'selected' : ''}><c:out value="${dept.name}"/></option>
                </c:forEach>
            </select>
        </label>
        <button class="button primary" type="submit">Áp dụng filter</button>
    </form>
</section>

<section class="metric-grid">
    <article class="metric-card"><span class="metric-icon blue">EM</span><div><small>Employees in scope</small><strong>${stats.totalEmployees}</strong><p>Theo filter hiện tại</p></div></article>
    <article class="metric-card"><span class="metric-icon purple">DP</span><div><small>Departments</small><strong>${stats.employeesByDepartment.size()}</strong><p>Nhóm đang có dữ liệu</p></div></article>
    <article class="metric-card"><span class="metric-icon green">GD</span><div><small>Gender groups</small><strong>${stats.genderBreakdown.size()}</strong><p>Diversity demographics</p></div></article>
    <article class="metric-card"><span class="metric-icon orange">TR</span><div><small>Training buckets</small><strong>${stats.trainingBuckets.size()}</strong><p>Tiến độ đào tạo</p></div></article>
</section>

<section class="chart-grid">
    <article class="panel chart-panel">
        <div class="panel-heading"><div><h2>Department breakdown</h2><p>Số nhân viên theo phòng ban</p></div></div>
        <c:choose><c:when test="${empty stats.employeesByDepartment}"><div class="empty-state">Chart unavailable for this module.</div></c:when>
        <c:otherwise><div class="bar-chart"><c:forEach var="entry" items="${stats.employeesByDepartment}">
            <div class="bar-row"><span><c:out value="${entry.key}"/></span><div class="bar-track"><i style="width:${entry.value * 18}%"></i></div><strong>${entry.value}</strong></div>
        </c:forEach></div></c:otherwise></c:choose>
    </article>
    <article class="panel chart-panel">
        <div class="panel-heading"><div><h2>Salary allocation</h2><p>Tổng lương theo phòng ban</p></div></div>
        <c:choose><c:when test="${empty stats.salaryByDepartment}"><div class="empty-state">Chart unavailable for this module.</div></c:when>
        <c:otherwise><div class="bar-chart"><c:forEach var="entry" items="${stats.salaryByDepartment}">
            <div class="bar-row"><span><c:out value="${entry.key}"/></span><div class="bar-track"><i style="width:70%;background:linear-gradient(90deg,#16835d,#6d58b8)"></i></div>
                <strong><fmt:formatNumber value="${entry.value}" type="number" maxFractionDigits="0"/></strong></div>
        </c:forEach></div></c:otherwise></c:choose>
    </article>
</section>

<section class="chart-grid" style="margin-top:16px">
    <article class="panel">
        <div class="panel-heading"><div><h2>Diversity demographics</h2><p>Phân bố giới tính</p></div></div>
        <div class="compact-stats" style="padding-top:12px">
            <c:choose><c:when test="${empty stats.genderBreakdown}"><div class="empty-state">Chart unavailable for this module.</div></c:when>
            <c:otherwise><c:forEach var="entry" items="${stats.genderBreakdown}">
                <div><span><i class="status-dot active"></i><c:out value="${entry.key}"/></span><strong>${entry.value}</strong></div>
            </c:forEach></c:otherwise></c:choose>
        </div>
    </article>
    <article class="panel">
        <div class="panel-heading"><div><h2>Training progress</h2><p>Buckets tiến độ</p></div></div>
        <div class="compact-stats" style="padding-top:12px">
            <c:choose><c:when test="${empty stats.trainingBuckets}"><div class="empty-state">Chart unavailable for this module.</div></c:when>
            <c:otherwise><c:forEach var="entry" items="${stats.trainingBuckets}">
                <div><span><i class="status-dot locked"></i><c:out value="${entry.key}"/></span><strong>${entry.value}</strong></div>
            </c:forEach></c:otherwise></c:choose>
        </div>
    </article>
</section>

<%@ include file="../partials/footer.jspf" %>
