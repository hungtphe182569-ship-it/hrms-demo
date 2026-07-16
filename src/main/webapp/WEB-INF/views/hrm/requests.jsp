<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="../partials/header.jspf" %>

<section class="summary-strip purple">
    <div><span>UC-11 / REQUEST APPROVAL</span><strong>Request Management</strong><small>Salary, promotion, transfer, benefit, equipment, other</small></div>
    <div class="sequence"><i>1</i><span>Validate</span><b></b><i>2</i><span>Approve</span><b></b><i>3</i><span>Notify</span></div>
</section>

<section class="toolbar">
    <form method="get" class="toolbar" style="width:100%;margin:0">
        <select name="view" style="max-width:190px">
            <option value="pending" ${view != 'processed' ? 'selected' : ''}>Pending HR</option>
            <option value="processed" ${view == 'processed' ? 'selected' : ''}>Processed</option>
        </select>
        <button class="button ghost" type="submit">Filter</button>
    </form>
</section>

<section class="panel">
    <div class="panel-heading"><div><h2>${view == 'processed' ? 'Processed requests' : 'Pending employee requests'}</h2>
        <p>Promotion is forwarded to Admin; salary and transfer are applied by HR approval.</p></div>
        <span class="count-badge">${requests.size()} requests</span></div>
    <div class="table-wrap"><table>
        <thead><tr><th>Employee</th><th>Category</th><th>Details</th><th>Status</th><th>Created</th><th class="right">Actions</th></tr></thead>
        <tbody>
        <c:choose>
            <c:when test="${empty requests}">
                <tr><td colspan="6"><div class="empty-state">No requests found.</div></td></tr>
            </c:when>
            <c:otherwise>
                <c:forEach var="req" items="${requests}">
                    <tr>
                        <td><div class="person"><span class="person-avatar">${req.requesterName.substring(0,1)}</span>
                            <div><strong><c:out value="${req.requesterName}"/></strong><small><c:out value="${req.departmentName}"/></small></div></div></td>
                        <td><span class="role-pill">${req.category}</span></td>
                        <td><strong><c:out value="${req.title}"/></strong><small><c:out value="${req.description}"/></small>
                            <c:if test="${not empty req.proposedSalary}"><small>Salary: ${req.proposedSalary}</small></c:if>
                            <c:if test="${not empty req.targetDepartmentName}"><small>Target department: <c:out value="${req.targetDepartmentName}"/></small></c:if>
                            <c:if test="${not empty req.proposedPosition}"><small>Position: <c:out value="${req.proposedPosition}"/></small></c:if></td>
                        <td><span class="status ${req.status == 'APPROVED' ? 'active' : req.status == 'REJECTED' ? 'deleted' : 'locked'}"><i></i>${req.status}</span></td>
                        <td><small>${req.createdAt}</small></td>
                        <td class="right actions">
                            <c:if test="${view != 'processed'}">
                                <form method="post" class="inline-form" data-confirm="${req.requiresAdminApproval() ? 'Promotion will be forwarded to Admin. Continue?' : 'Approve this request?'}">
                                    <input type="hidden" name="action" value="approve"><input type="hidden" name="requestId" value="${req.id}">
                                    <button class="button small primary" type="submit">Approve</button>
                                </form>
                                <button class="button small danger-outline" type="button" data-open-modal="rejectRequestModal"
                                        data-request-id="${req.id}" data-request-name="${fn:escapeXml(req.title)}">Reject</button>
                            </c:if>
                            <c:if test="${view == 'processed'}"><span class="deleted-note"><c:out value="${req.rejectionReason}"/></span></c:if>
                        </td>
                    </tr>
                </c:forEach>
            </c:otherwise>
        </c:choose>
        </tbody>
    </table></div>
</section>

<div class="modal" id="rejectRequestModal" aria-hidden="true"><div class="modal-card" role="dialog" aria-modal="true">
    <button class="modal-close" type="button" data-close-modal>×</button><div class="danger-mark">!</div>
    <h2>Reject request</h2>
    <p>Reject <strong id="rejectRequestName"></strong>. Reason is required.</p>
    <form method="post"><input type="hidden" name="action" value="reject"><input type="hidden" name="requestId" id="rejectRequestId">
        <label><span>Rejection reason *</span><textarea name="reason" required maxlength="500"></textarea></label>
        <div class="form-actions"><button class="button ghost" type="button" data-close-modal>Cancel</button>
            <button class="button danger-button" type="submit">Confirm reject</button></div>
    </form>
</div></div>

<script>
document.querySelectorAll('[data-request-id]').forEach(btn => {
  btn.addEventListener('click', () => {
    document.getElementById('rejectRequestId').value = btn.dataset.requestId;
    document.getElementById('rejectRequestName').textContent = btn.dataset.requestName || '';
  });
});
</script>

<%@ include file="../partials/footer.jspf" %>
