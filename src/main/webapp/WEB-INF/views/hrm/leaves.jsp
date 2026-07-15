<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="../partials/header.jspf" %>

<section class="summary-strip">
    <div><span>UC-13 / LEAVE MANAGEMENT</span><strong>Pending Leave (HRM)</strong><small>Approve · Reject (bắt buộc lý do) · Revert trong 24 giờ</small></div>
    <div class="sequence"><i>1</i><span>Review</span><b></b><i>2</i><span>Decide</span><b></b><i>3</i><span>Notify</span></div>
</section>

<section class="toolbar">
    <form method="get" class="toolbar" style="width:100%;margin:0">
        <select name="view" style="max-width:180px">
            <option value="pending" ${view != 'processed' ? 'selected' : ''}>Pending HRM</option>
            <option value="processed" ${view == 'processed' ? 'selected' : ''}>Processed</option>
        </select>
        <select name="leaveType" style="max-width:200px">
            <option value="">All leave types</option>
            <option value="ANNUAL" ${leaveType == 'ANNUAL' ? 'selected' : ''}>ANNUAL</option>
            <option value="SICK" ${leaveType == 'SICK' ? 'selected' : ''}>SICK</option>
            <option value="MATERNITY" ${leaveType == 'MATERNITY' ? 'selected' : ''}>MATERNITY</option>
            <option value="LONG_TERM" ${leaveType == 'LONG_TERM' ? 'selected' : ''}>LONG_TERM</option>
            <option value="UNPAID" ${leaveType == 'UNPAID' ? 'selected' : ''}>UNPAID</option>
        </select>
        <button class="button ghost" type="submit">Lọc</button>
    </form>
</section>

<section class="panel">
    <div class="panel-heading"><div><h2>${view == 'processed' ? 'Processed requests' : 'Pending leave requests'}</h2>
        <p>Chỉ HR Manager được quyết định cấp quản trị</p></div>
        <span class="count-badge">${leaves.size()} đơn</span></div>

    <c:choose>
        <c:when test="${view != 'processed'}">
            <form method="post" id="bulkLeaveForm" class="toolbar" style="padding:0 22px">
                <input type="hidden" name="action" value="approveBulk">
                <button class="button primary" type="submit">Approve selected</button>
            </form>
            <div class="table-wrap"><table>
                <thead><tr><th></th><th>Employee</th><th>Type</th><th>Period</th><th>Days</th><th>Reason</th><th class="right">Actions</th></tr></thead>
                <tbody>
                <c:choose><c:when test="${empty leaves}"><tr><td colspan="7"><div class="empty-state">Không có đơn chờ duyệt.</div></td></tr></c:when>
                <c:otherwise><c:forEach var="leave" items="${leaves}">
                    <tr>
                        <td><input form="bulkLeaveForm" type="checkbox" name="leaveIds" value="${leave.id}"></td>
                        <td><div class="person"><span class="person-avatar">${leave.employeeName.substring(0,1)}</span>
                            <div><strong><c:out value="${leave.employeeName}"/></strong><small><c:out value="${leave.departmentName}"/></small></div></div></td>
                        <td><span class="role-pill">${leave.leaveType}</span></td>
                        <td><small>${leave.startDate} → ${leave.endDate}</small></td>
                        <td><strong>${leave.daysCount}</strong></td>
                        <td><c:out value="${leave.reason}"/></td>
                        <td class="right actions">
                            <form method="post" class="inline-form" data-confirm="Phê duyệt đơn này?">
                                <input type="hidden" name="action" value="approve"><input type="hidden" name="leaveId" value="${leave.id}">
                                <button class="icon-button" title="Approve">✓</button>
                            </form>
                            <button class="icon-button danger" type="button" title="Reject"
                                    data-open-modal="rejectLeaveModal" data-leave-id="${leave.id}"
                                    data-leave-name="${fn:escapeXml(leave.employeeName)}">×</button>
                        </td>
                    </tr>
                </c:forEach></c:otherwise></c:choose>
                </tbody>
            </table></div>
        </c:when>
        <c:otherwise>
            <div class="table-wrap"><table>
                <thead><tr><th>Employee</th><th>Type</th><th>Status</th><th>Decided at</th><th>Reason</th><th class="right">Revert</th></tr></thead>
                <tbody>
                <c:choose><c:when test="${empty leaves}"><tr><td colspan="6"><div class="empty-state">Chưa có đơn đã xử lý.</div></td></tr></c:when>
                <c:otherwise><c:forEach var="leave" items="${leaves}">
                    <tr>
                        <td><strong><c:out value="${leave.employeeName}"/></strong><small><c:out value="${leave.departmentName}"/></small></td>
                        <td><span class="role-pill">${leave.leaveType}</span></td>
                        <td><span class="status ${leave.status == 'APPROVED' ? 'active' : 'deleted'}"><i></i>${leave.status}</span></td>
                        <td><small>${leave.decidedAt}</small></td>
                        <td><c:out value="${empty leave.rejectionReason ? leave.reason : leave.rejectionReason}"/></td>
                        <td class="right">
                            <c:if test="${leave.canRevert()}">
                                <form method="post" class="inline-form" data-confirm="Revert quyết định này?">
                                    <input type="hidden" name="action" value="revert"><input type="hidden" name="leaveId" value="${leave.id}">
                                    <button class="button small ghost" type="submit">Revert (24h)</button>
                                </form>
                            </c:if>
                            <c:if test="${!leave.canRevert()}"><span class="deleted-note">Expired</span></c:if>
                        </td>
                    </tr>
                </c:forEach></c:otherwise></c:choose>
                </tbody>
            </table></div>
        </c:otherwise>
    </c:choose>
</section>

<div class="modal" id="rejectLeaveModal" aria-hidden="true"><div class="modal-card" role="dialog" aria-modal="true">
    <button class="modal-close" type="button" data-close-modal>×</button><div class="danger-mark">!</div>
    <h2>Reject leave request</h2>
    <p>Từ chối đơn của <strong id="rejectLeaveName"></strong>. Lý do là bắt buộc.</p>
    <form method="post"><input type="hidden" name="action" value="reject"><input type="hidden" name="leaveId" id="rejectLeaveId">
        <label><span>Rejection reason *</span><textarea name="reason" required maxlength="500"></textarea></label>
        <div class="form-actions"><button class="button ghost" type="button" data-close-modal>Hủy</button>
            <button class="button danger-button" type="submit">Xác nhận từ chối</button></div>
    </form>
</div></div>

<script>
document.querySelectorAll('[data-leave-id]').forEach(btn => {
  btn.addEventListener('click', () => {
    document.getElementById('rejectLeaveId').value = btn.dataset.leaveId;
    document.getElementById('rejectLeaveName').textContent = btn.dataset.leaveName || '';
  });
});
</script>

<%@ include file="../partials/footer.jspf" %>
