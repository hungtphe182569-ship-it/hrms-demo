<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Quản lý tài khoản" scope="request"/>
<%@ include file="partials/header.jspf" %>

<section class="summary-strip">
    <div><span>UC17 / ACCOUNT MANAGEMENT</span><strong>Quản lý vòng đời tài khoản</strong><small>Tạo · Xem · Cập nhật · Gán role · Trạng thái · Reset password · Soft delete</small></div>
    <div class="sequence"><i>1</i><span>Kiểm tra</span><b></b><i>2</i><span>Cập nhật</span><b></b><i>3</i><span>Audit</span></div>
</section>

<section class="toolbar">
    <div class="search-box"><span>⌕</span><input id="accountSearch" type="search" placeholder="Tìm username, email hoặc họ tên..."></div>
    <label class="switch-label"><input id="includeDeleted" type="checkbox" ${includeDeleted ? 'checked' : ''}><span></span> Hiện tài khoản đã xóa</label>
    <button class="button primary" type="button" data-open-modal="createAccountModal">+ Thêm tài khoản</button>
</section>

<section class="panel">
    <div class="panel-heading"><div><h2>Danh sách tài khoản</h2><p>Mọi thay đổi quan trọng đều được ghi audit log</p></div><span class="count-badge">${accounts.size()} tài khoản</span></div>
    <div class="table-wrap"><table id="accountTable"><thead><tr><th>Account</th><th>Contact</th><th>Role</th><th>Status</th><th>Created</th><th class="right">Actions</th></tr></thead><tbody>
    <c:forEach var="account" items="${accounts}">
        <tr data-search="${fn:escapeXml(account.username)} ${fn:escapeXml(account.email)} ${fn:escapeXml(account.fullName)}">
            <td><div class="person"><span class="person-avatar">${account.fullName.substring(0,1)}</span><div><strong><c:out value="${account.fullName}"/></strong><small>@<c:out value="${account.username}"/></small></div></div></td>
            <td><strong class="muted-strong"><c:out value="${account.email}"/></strong><small><c:out value="${account.phone}"/></small></td>
            <td><span class="role-pill"><c:out value="${account.roleNames}"/></span></td>
            <td><span class="status ${account.status.toLowerCase()}"><i></i>${account.status}</span></td>
            <td><small>${account.createdAt.toLocalDate()}</small></td>
            <td class="right actions">
                <c:if test="${account.status != 'DELETED'}">
                    <button class="icon-button" type="button" title="Xem / Sửa"
                            data-edit-account="${account.id}" data-username="${fn:escapeXml(account.username)}"
                            data-email="${fn:escapeXml(account.email)}" data-full-name="${fn:escapeXml(account.fullName)}"
                            data-phone="${fn:escapeXml(account.phone)}" data-role-ids="<c:forEach var='r' items='${account.roles}' varStatus='s'>${s.first ? '' : ','}${r.id}</c:forEach>">✎</button>
                    <form method="post" class="inline-form" data-confirm="Reset password cho tài khoản này?">
                        <input type="hidden" name="action" value="resetPassword"><input type="hidden" name="accountId" value="${account.id}">
                        <button class="icon-button" title="Reset password">↻</button>
                    </form>
                    <c:choose>
                        <c:when test="${account.status == 'ACTIVE'}">
                            <form method="post" class="inline-form" data-confirm="Chuyển tài khoản sang INACTIVE?">
                                <input type="hidden" name="action" value="status"><input type="hidden" name="accountId" value="${account.id}">
                                <input type="hidden" name="currentStatus" value="ACTIVE"><input type="hidden" name="targetStatus" value="INACTIVE">
                                <button class="icon-button" title="Deactivate">–</button>
                            </form>
                            <form method="post" class="inline-form" data-confirm="Khóa tài khoản này?">
                                <input type="hidden" name="action" value="status"><input type="hidden" name="accountId" value="${account.id}">
                                <input type="hidden" name="currentStatus" value="ACTIVE"><input type="hidden" name="targetStatus" value="LOCKED">
                                <button class="icon-button" title="Lock">■</button>
                            </form>
                        </c:when>
                        <c:when test="${account.status == 'INACTIVE' || account.status == 'LOCKED'}">
                            <form method="post" class="inline-form" data-confirm="Kích hoạt lại tài khoản?">
                                <input type="hidden" name="action" value="status"><input type="hidden" name="accountId" value="${account.id}">
                                <input type="hidden" name="currentStatus" value="${account.status}"><input type="hidden" name="targetStatus" value="ACTIVE">
                                <button class="icon-button" title="Activate / Unlock">✓</button>
                            </form>
                        </c:when>
                    </c:choose>
                    <button class="icon-button danger" type="button" title="Soft delete" data-delete-account="${account.id}" data-account-name="${fn:escapeXml(account.username)}">×</button>
                </c:if>
                <c:if test="${account.status == 'DELETED'}"><span class="deleted-note" title="${fn:escapeXml(account.deleteReason)}">Deleted</span></c:if>
            </td>
        </tr>
    </c:forEach>
    </tbody></table></div>
</section>

<div class="modal" id="createAccountModal" aria-hidden="true"><div class="modal-card wide" role="dialog" aria-modal="true">
    <button class="modal-close" type="button" data-close-modal>×</button><p class="eyebrow">UC17 / CREATE</p><h2>Tạo tài khoản</h2>
    <p class="modal-intro">Username và email phải duy nhất. Password gồm chữ hoa, chữ thường, số và ký tự đặc biệt.</p>
    <form method="post" class="form-grid" data-validate="account"><input type="hidden" name="action" value="create">
        <label><span>Username *</span><input name="username" minlength="3" maxlength="50" pattern="[A-Za-z0-9._-]+" required></label>
        <label><span>Email *</span><input name="email" type="email" required></label>
        <label><span>Full name *</span><input name="fullName" required></label><label><span>Phone</span><input name="phone"></label>
        <label><span>Password *</span><input name="password" type="password" minlength="8" required></label>
        <label><span>Roles * (giữ Ctrl để chọn nhiều)</span><select name="roleIds" multiple required><c:forEach var="role" items="${roles}"><option value="${role.id}"><c:out value="${role.name}"/></option></c:forEach></select></label>
        <div class="form-actions full"><button class="button ghost" type="button" data-close-modal>Hủy</button><button class="button primary" type="submit">Tạo tài khoản</button></div>
    </form>
</div></div>

<div class="modal" id="editAccountModal" aria-hidden="true"><div class="modal-card wide" role="dialog" aria-modal="true">
    <button class="modal-close" type="button" data-close-modal>×</button><p class="eyebrow">UC17 / VIEW & UPDATE</p><h2>Xem và cập nhật tài khoản</h2>
    <form method="post" class="form-grid"><input type="hidden" name="action" value="update"><input type="hidden" name="accountId" id="editAccountId">
        <label><span>Username</span><input id="editAccountUsername" disabled></label>
        <label><span>Email *</span><input name="email" id="editAccountEmail" type="email" required></label>
        <label><span>Full name *</span><input name="fullName" id="editAccountFullName" required></label>
        <label><span>Phone</span><input name="phone" id="editAccountPhone"></label>
        <label class="full"><span>Roles * (giữ Ctrl để chọn nhiều)</span><select name="roleIds" id="editAccountRoles" multiple required><c:forEach var="role" items="${roles}"><option value="${role.id}"><c:out value="${role.name}"/></option></c:forEach></select></label>
        <div class="form-actions full"><button class="button ghost" type="button" data-close-modal>Hủy</button><button class="button primary" type="submit">Lưu thay đổi</button></div>
    </form>
</div></div>

<div class="modal" id="deleteAccountModal" aria-hidden="true"><div class="modal-card" role="dialog" aria-modal="true">
    <button class="modal-close" type="button" data-close-modal>×</button><div class="danger-mark">!</div><h2>Soft delete account</h2>
    <p>Tài khoản <strong id="deleteAccountName"></strong> sẽ chuyển sang DELETED, bị thu hồi session và giữ lại audit.</p>
    <form method="post"><input type="hidden" name="action" value="delete"><input type="hidden" name="accountId" id="deleteAccountId">
        <label><span>Lý do xóa *</span><textarea name="reason" required maxlength="500"></textarea></label>
        <div class="form-actions"><button class="button ghost" type="button" data-close-modal>Hủy</button><button class="button danger-button" type="submit">Xác nhận soft delete</button></div>
    </form>
</div></div>

<%@ include file="partials/footer.jspf" %>
