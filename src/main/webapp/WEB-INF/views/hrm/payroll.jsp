<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="../partials/header.jspf" %>

<section class="summary-strip green">
    <div><span>UC14 / PAYROLL FOR APPROVAL</span><strong>Final salary approval</strong><small>Review · Final approve · Reject · Export bank CSV</small></div>
    <div class="sequence"><i>1</i><span>Review</span><b></b><i>2</i><span>Lock</span><b></b><i>3</i><span>Publish</span></div>
</section>

<section class="dashboard-grid">
    <article class="panel">
        <div class="panel-heading"><div><h2>Payroll batches</h2><p>Ưu tiên batch SUBMITTED</p></div><span class="count-badge">${batches.size()}</span></div>
        <div class="activity-list">
            <c:forEach var="batch" items="${batches}">
                <a class="activity-item" href="?batchId=${batch.id}" style="${selectedBatchId == batch.id ? 'background:#f3faf6' : ''}">
                    <span class="activity-icon">PR</span>
                    <div><strong><c:out value="${batch.periodLabel}"/></strong>
                        <small>${batch.periodStart} → ${batch.periodEnd} · ${batch.employeeCount} NV</small>
                        <p><span class="status ${batch.status == 'APPROVED_FINAL' ? 'active' : (batch.status == 'REJECTED' ? 'deleted' : 'locked')}"><i></i>${batch.status}</span>
                            · <fmt:formatNumber value="${batch.totalAmount}" type="number"/></p></div>
                </a>
            </c:forEach>
            <c:if test="${empty batches}"><div class="empty-state">Chưa có payroll batch.</div></c:if>
        </div>
    </article>

    <div class="dashboard-side">
        <c:if test="${not empty selectedBatch}">
            <article class="panel">
                <div class="panel-heading"><div><h2>Batch <c:out value="${selectedBatch.periodLabel}"/></h2>
                    <p>Status: ${selectedBatch.status}<c:if test="${selectedBatch.locked}"> · LOCKED</c:if></p></div></div>
                <div class="compact-stats">
                    <div><span>Employees</span><strong>${selectedBatch.employeeCount}</strong></div>
                    <div><span>Total net</span><strong><fmt:formatNumber value="${selectedBatch.totalAmount}" type="number"/></strong></div>
                </div>
                <div class="toolbar" style="padding:8px 22px 18px;flex-wrap:wrap">
                    <c:if test="${selectedBatch.status == 'SUBMITTED'}">
                        <form method="post" class="inline-form" data-confirm="Final approve và khóa batch này?">
                            <input type="hidden" name="action" value="approve"><input type="hidden" name="batchId" value="${selectedBatch.id}">
                            <button class="button primary" type="submit">Final Approve</button>
                        </form>
                        <button class="button danger-outline" type="button" data-open-modal="rejectPayrollModal">Reject</button>
                    </c:if>
                    <c:if test="${selectedBatch.status == 'APPROVED_FINAL'}">
                        <a class="button primary" href="?export=${selectedBatch.id}">Export Bank CSV</a>
                    </c:if>
                    <c:if test="${selectedBatch.status != 'APPROVED_FINAL' && selectedBatch.status != 'SUBMITTED'}">
                        <span class="deleted-note">Không thể export trước khi APPROVED_FINAL</span>
                    </c:if>
                </div>
            </article>
        </c:if>
    </div>
</section>

<section class="panel" style="margin-top:16px">
    <div class="panel-heading"><div><h2>Payslip details</h2><p>Chi tiết phiếu lương thuộc batch đang chọn</p></div></div>
    <div class="table-wrap"><table>
        <thead><tr><th>Employee</th><th>Department</th><th>Bank</th><th>Gross</th><th>Net</th><th>Published</th></tr></thead>
        <tbody>
        <c:choose><c:when test="${empty payslips}"><tr><td colspan="6"><div class="empty-state">Chọn một payroll batch.</div></td></tr></c:when>
        <c:otherwise><c:forEach var="p" items="${payslips}">
            <tr>
                <td><strong><c:out value="${p.employeeName}"/></strong></td>
                <td><c:out value="${p.departmentName}"/></td>
                <td><small><c:out value="${p.bankAccount}"/></small></td>
                <td><fmt:formatNumber value="${p.grossPay}" type="number"/></td>
                <td><fmt:formatNumber value="${p.netPay}" type="number"/></td>
                <td><span class="status ${p.published ? 'active' : 'inactive'}"><i></i>${p.published ? 'YES' : 'NO'}</span></td>
            </tr>
        </c:forEach></c:otherwise></c:choose>
        </tbody>
    </table></div>
</section>

<c:if test="${not empty selectedBatch && selectedBatch.status == 'SUBMITTED'}">
<div class="modal" id="rejectPayrollModal" aria-hidden="true"><div class="modal-card" role="dialog" aria-modal="true">
    <button class="modal-close" type="button" data-close-modal>×</button><div class="danger-mark">!</div>
    <h2>Reject payroll batch</h2>
    <p>Lý do từ chối là bắt buộc. Payslip sẽ không được publish.</p>
    <form method="post"><input type="hidden" name="action" value="reject">
        <input type="hidden" name="batchId" value="${selectedBatch.id}">
        <label><span>Rejection reason *</span><textarea name="reason" required maxlength="500"></textarea></label>
        <div class="form-actions"><button class="button ghost" type="button" data-close-modal>Hủy</button>
            <button class="button danger-button" type="submit">Reject batch</button></div>
    </form>
</div></div>
</c:if>

<%@ include file="../partials/footer.jspf" %>
