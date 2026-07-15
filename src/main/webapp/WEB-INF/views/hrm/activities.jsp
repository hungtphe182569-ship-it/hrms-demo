<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="../partials/header.jspf" %>

<section class="summary-strip">
    <div><span>UC-12.2 / ACTIVITY CENTER</span><strong>Nhật ký hoạt động hệ thống</strong><small>View-only · Search · Filter · Paginated</small></div>
    <div class="rule-note"><b>Read-only</b><span>Không cho sửa / xóa log</span></div>
</section>

<section class="hrm-tab-bar">
    <a href="${pageContext.request.contextPath}/hrm/dashboard">Dashboard</a>
    <a href="${pageContext.request.contextPath}/hrm/analytics">Analytics</a>
    <a class="active" href="${pageContext.request.contextPath}/hrm/activities">Activity Center</a>
</section>

<section class="toolbar">
    <form method="get" class="toolbar" style="width:100%;margin:0">
        <div class="search-box"><span>⌕</span><input name="q" type="search" value="<c:out value='${keyword}'/>" placeholder="Tìm action, user, chi tiết..."></div>
        <select name="actionName" style="max-width:240px">
            <option value="">All actions</option>
            <option value="CREATE_DEPARTMENT" ${actionName == 'CREATE_DEPARTMENT' ? 'selected' : ''}>CREATE_DEPARTMENT</option>
            <option value="APPROVE_LEAVE" ${actionName == 'APPROVE_LEAVE' ? 'selected' : ''}>APPROVE_LEAVE</option>
            <option value="REJECT_LEAVE" ${actionName == 'REJECT_LEAVE' ? 'selected' : ''}>REJECT_LEAVE</option>
            <option value="REVERT_LEAVE" ${actionName == 'REVERT_LEAVE' ? 'selected' : ''}>REVERT_LEAVE</option>
            <option value="APPROVE_PAYROLL" ${actionName == 'APPROVE_PAYROLL' ? 'selected' : ''}>APPROVE_PAYROLL</option>
            <option value="CREATE_EVENT" ${actionName == 'CREATE_EVENT' ? 'selected' : ''}>CREATE_EVENT</option>
        </select>
        <button class="button primary" type="submit">Tìm kiếm</button>
    </form>
</section>

<section class="panel">
    <div class="panel-heading"><div><h2>Activity logs</h2><p>Sắp xếp mới nhất trước</p></div><span class="count-badge">${total} records</span></div>
    <div class="table-wrap"><table>
        <thead><tr><th>When</th><th>Actor</th><th>Action</th><th>Entity</th><th>Details</th></tr></thead>
        <tbody>
        <c:choose><c:when test="${empty activities}">
            <tr><td colspan="5"><div class="empty-state">No activity logs found matching your criteria</div></td></tr>
        </c:when><c:otherwise>
            <c:forEach var="activity" items="${activities}">
                <tr>
                    <td><small>${activity.createdAt.toLocalDate()} ${activity.createdAt.toLocalTime().withNano(0)}</small></td>
                    <td><strong class="muted-strong"><c:out value="${empty activity.actorName ? '—' : activity.actorName}"/></strong>
                        <small><c:if test="${not empty activity.actorUserId}">#${activity.actorUserId}</c:if></small></td>
                    <td><span class="role-pill"><c:out value="${activity.actionName}"/></span></td>
                    <td><small><c:out value="${activity.entityType}"/> <c:if test="${not empty activity.entityId}">#${activity.entityId}</c:if></small></td>
                    <td><c:out value="${activity.details}"/></td>
                </tr>
            </c:forEach>
        </c:otherwise></c:choose>
        </tbody>
    </table></div>
    <c:if test="${totalPages > 1}">
        <div class="toolbar" style="padding:12px 22px">
            <c:if test="${page > 1}"><a class="button ghost" href="?q=${fn:escapeXml(keyword)}&actionName=${fn:escapeXml(actionName)}&page=${page-1}">← Prev</a></c:if>
            <span class="count-badge">Page ${page} / ${totalPages}</span>
            <c:if test="${page < totalPages}"><a class="button ghost" href="?q=${fn:escapeXml(keyword)}&actionName=${fn:escapeXml(actionName)}&page=${page+1}">Next →</a></c:if>
        </div>
    </c:if>
</section>

<%@ include file="../partials/footer.jspf" %>
