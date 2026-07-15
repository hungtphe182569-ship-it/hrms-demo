<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Quản lý tài khoản" scope="request"/>
<%@ include file="partials/header.jspf" %>

<section class="summary-strip">
    <div><span>TRUNG TÂM NHÂN SỰ</span><strong>Quản lý tài khoản</strong><small>Tạo mới · Cấp lại mật khẩu · Ngưng truy cập</small></div>
    <div class="sequence"><i>1</i><span>Kiểm tra</span><b></b><i>2</i><span>Cập nhật</span><b></b><i>3</i><span>Ghi nhật ký</span></div>
</section>

<section class="toolbar">
    <div class="search-box">
        <span>⌕</span>
        <input id="accountSearch" type="search" placeholder="Tìm username, email hoặc họ tên..." aria-label="Tìm tài khoản">
    </div>
    <label class="switch-label">
        <input id="includeDeleted" type="checkbox" ${includeDeleted ? 'checked' : ''}>
        <span></span> Hiện tài khoản đã xóa
    </label>
    <button class="button primary" type="button" data-open-modal="createAccountModal">+ Thêm tài khoản</button>
</section>

<section class="panel">
    <div class="panel-heading">
        <div><h2>Danh sách nhân sự</h2><p>Quản lý thông tin và quyền truy cập trong một nơi</p></div>
        <span class="count-badge">${accounts.size()} tài khoản</span>
    </div>

    <div class="table-wrap">
        <table id="accountTable">
            <thead>
            <tr><th>Account</th><th>Contact</th><th>Role</th><th>Status</th><th>Created</th><th class="right">Actions</th></tr>
            </thead>
            <tbody>
            <c:forEach var="account" items="${accounts}">
                <tr data-search="${account.username} ${account.email} ${account.fullName}">
                    <td><div class="person"><span class="person-avatar">${account.fullName.substring(0,1)}</span><div><strong><c:out value="${account.fullName}"/></strong><small>@<c:out value="${account.username}"/></small></div></div></td>
                    <td><strong class="muted-strong"><c:out value="${account.email}"/></strong><small><c:out value="${account.phone}"/></small></td>
                    <td><span class="role-pill"><c:out value="${account.roleNames}"/></span></td>
                    <td><span class="status ${account.status.toLowerCase()}"><i></i>${account.status}</span></td>
                    <td><small>${account.createdAt.toLocalDate()}</small></td>
                    <td class="right actions">
                        <c:if test="${account.status != 'DELETED'}">
                            <form method="post" class="inline-form" data-confirm="Reset password cho tài khoản này?">
                                <input type="hidden" name="action" value="resetPassword">
                                <input type="hidden" name="accountId" value="${account.id}">
                                <button class="icon-button" title="Reset password" aria-label="Reset password">↻</button>
                            </form>
                            <button class="icon-button danger" type="button" title="Soft delete"
                                    data-delete-account="${account.id}" data-account-name="${account.username}">×</button>
                        </c:if>
                        <c:if test="${account.status == 'DELETED'}"><span class="deleted-note">Deleted</span></c:if>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</section>

<div class="modal" id="createAccountModal" aria-hidden="true">
    <div class="modal-card wide" role="dialog" aria-modal="true" aria-labelledby="createAccountTitle">
        <button class="modal-close" type="button" data-close-modal>×</button>
        <p class="eyebrow">UC17 / MAIN FLOW</p>
        <h2 id="createAccountTitle">Create a new account</h2>
        <p class="modal-intro">Username được kiểm tra duy nhất, password được hash SHA-256 và account–role được lưu trong một transaction.</p>
        <form method="post" class="form-grid" data-validate="account">
            <input type="hidden" name="action" value="create">
            <label><span>Username *</span><input name="username" minlength="3" maxlength="50" pattern="[A-Za-z0-9._-]+" required placeholder="employee03"></label>
            <label><span>Email *</span><input name="email" type="email" required placeholder="employee03@hrms.local"></label>
            <label><span>Full name *</span><input name="fullName" required placeholder="Nguyễn Văn An"></label>
            <label><span>Phone</span><input name="phone" placeholder="0901234567"></label>
            <label><span>Password *</span><input name="password" type="password" minlength="8" required placeholder="Tối thiểu 8 ký tự"></label>
            <label><span>Role *</span><select name="roleId" required><option value="">Select role</option><c:forEach var="role" items="${roles}"><option value="${role.id}"><c:out value="${role.name}"/></option></c:forEach></select></label>
            <div class="form-actions full"><button class="button ghost" type="button" data-close-modal>Cancel</button><button class="button primary" type="submit">Create account</button></div>
        </form>
    </div>
</div>

<div class="modal" id="deleteAccountModal" aria-hidden="true">
    <div class="modal-card" role="dialog" aria-modal="true" aria-labelledby="deleteAccountTitle">
        <button class="modal-close" type="button" data-close-modal>×</button>
        <div class="danger-mark">!</div>
        <h2 id="deleteAccountTitle">Soft delete account</h2>
        <p>Tài khoản <strong id="deleteAccountName"></strong> sẽ chuyển sang DELETED. Record và audit history vẫn được giữ.</p>
        <form method="post">
            <input type="hidden" name="action" value="delete">
            <input type="hidden" name="accountId" id="deleteAccountId">
            <label><span>Deletion reason *</span><textarea name="reason" required maxlength="500" placeholder="Ví dụ: Employee left the company"></textarea></label>
            <div class="form-actions"><button class="button ghost" type="button" data-close-modal>Cancel</button><button class="button danger-button" type="submit">Confirm soft delete</button></div>
        </form>
    </div>
</div>

<%@ include file="partials/footer.jspf" %>
