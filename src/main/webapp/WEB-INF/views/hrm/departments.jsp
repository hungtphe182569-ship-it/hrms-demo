<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="../partials/header.jspf" %>

<section class="summary-strip">
    <div><span>UC15 / DEPARTMENT MANAGEMENT</span><strong>Quản lý phòng ban</strong><small>Create · Edit · Soft delete khi không còn nhân viên</small></div>
    <div class="sequence"><i>1</i><span>Validate</span><b></b><i>2</i><span>Save</span><b></b><i>3</i><span>Audit</span></div>
</section>

<section class="toolbar">
    <label class="switch-label"><input id="includeInactive" type="checkbox" ${includeInactive ? 'checked' : ''} onchange="location.href='?includeInactive='+this.checked"><span></span> Hiện phòng ban inactive</label>
    <div class="toolbar-spacer"></div>
    <button class="button primary" type="button" data-open-modal="createDepartmentModal">+ Create Department</button>
</section>

<section class="panel">
    <div class="panel-heading"><div><h2>Danh sách phòng ban</h2><p>Code và name phải duy nhất (case-insensitive)</p></div>
        <span class="count-badge">${departments.size()} departments</span></div>
    <div class="table-wrap"><table>
        <thead><tr><th>Code</th><th>Name</th><th>Employees</th><th>Status</th><th class="right">Actions</th></tr></thead>
        <tbody>
        <c:forEach var="dept" items="${departments}">
            <tr>
                <td><span class="role-pill"><c:out value="${dept.code}"/></span></td>
                <td><strong><c:out value="${dept.name}"/></strong></td>
                <td><strong>${dept.employeeCount}</strong></td>
                <td><span class="status ${dept.status.toLowerCase()}"><i></i>${dept.status}</span></td>
                <td class="right actions">
                    <c:if test="${dept.status == 'ACTIVE'}">
                        <button class="icon-button" type="button" title="Edit"
                                data-open-modal="editDepartmentModal"
                                data-dept-id="${dept.id}" data-dept-code="${fn:escapeXml(dept.code)}"
                                data-dept-name="${fn:escapeXml(dept.name)}">✎</button>
                        <form method="post" class="inline-form" data-confirm="Soft delete phòng ban này?">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="departmentId" value="${dept.id}">
                            <button class="icon-button danger" title="Soft delete">×</button>
                        </form>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty departments}"><tr><td colspan="5"><div class="empty-state">Chưa có phòng ban.</div></td></tr></c:if>
        </tbody>
    </table></div>
</section>

<div class="modal" id="createDepartmentModal" aria-hidden="true"><div class="modal-card" role="dialog" aria-modal="true">
    <button class="modal-close" type="button" data-close-modal>×</button>
    <p class="eyebrow">UC15 / CREATE</p><h2>Create Department</h2>
    <form method="post" class="stack-form"><input type="hidden" name="action" value="create">
        <label><span>Code *</span><input name="code" required maxlength="30"></label>
        <label><span>Name *</span><input name="name" required maxlength="120"></label>
        <div class="form-actions"><button class="button ghost" type="button" data-close-modal>Hủy</button>
            <button class="button primary" type="submit">Save</button></div>
    </form>
</div></div>

<div class="modal" id="editDepartmentModal" aria-hidden="true"><div class="modal-card" role="dialog" aria-modal="true">
    <button class="modal-close" type="button" data-close-modal>×</button>
    <p class="eyebrow">UC15 / EDIT</p><h2>Edit Department</h2>
    <form method="post" class="stack-form"><input type="hidden" name="action" value="update">
        <input type="hidden" name="departmentId" id="editDeptId">
        <label><span>Code *</span><input name="code" id="editDeptCode" required maxlength="30"></label>
        <label><span>Name *</span><input name="name" id="editDeptName" required maxlength="120"></label>
        <div class="form-actions"><button class="button ghost" type="button" data-close-modal>Hủy</button>
            <button class="button primary" type="submit">Save</button></div>
    </form>
</div></div>

<script>
document.querySelectorAll('[data-dept-id]').forEach(btn => {
  btn.addEventListener('click', () => {
    document.getElementById('editDeptId').value = btn.dataset.deptId;
    document.getElementById('editDeptCode').value = btn.dataset.deptCode || '';
    document.getElementById('editDeptName').value = btn.dataset.deptName || '';
  });
});
</script>

<%@ include file="../partials/footer.jspf" %>
