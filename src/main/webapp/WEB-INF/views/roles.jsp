<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Vai trò & phân quyền" scope="request"/>
<%@ include file="partials/header.jspf" %>

<section class="summary-strip purple">
    <div><span>KIỂM SOÁT TRUY CẬP</span><strong>Vai trò & phân quyền</strong><small>Thiết lập phạm vi làm việc an toàn cho từng nhóm</small></div>
    <div class="rule-note"><b>Quy tắc an toàn</b><span>Vai trò đang sử dụng sẽ được bảo vệ</span></div>
</section>

<section class="toolbar">
    <div class="search-box"><span>⌕</span><input id="roleSearch" type="search" placeholder="Tìm role..." aria-label="Tìm role"></div>
    <div class="toolbar-spacer"></div>
    <button class="button primary purple-button" type="button" data-open-modal="createRoleModal">+ Thêm vai trò</button>
</section>

<section class="role-grid" id="roleGrid">
    <c:forEach var="role" items="${roles}">
        <article class="role-card" data-search="${role.name} ${role.description}">
            <div class="role-card-top"><span class="role-symbol">${role.name.substring(0,1)}</span><span class="role-id">ROLE-${role.id}</span></div>
            <h2><c:out value="${role.name}"/></h2>
            <p><c:out value="${empty role.description ? 'No description' : role.description}"/></p>
            <div class="role-metric"><strong>${role.assignedUsers}</strong><span>active assigned users</span></div>
            <div class="role-actions">
                <button class="button small ghost" type="button"
                        data-edit-role="${role.id}"
                        data-role-name="${fn:escapeXml(role.name)}"
                        data-role-description="${fn:escapeXml(role.description)}">Edit</button>
                <form method="post" data-confirm="${role.assignedUsers > 0 ? 'Role đang được sử dụng và sẽ bị từ chối. Tiếp tục demo alternative flow?' : 'Xóa role này?'}">
                    <input type="hidden" name="action" value="delete"><input type="hidden" name="roleId" value="${role.id}">
                    <button class="button small danger-outline" type="submit">Delete</button>
                </form>
            </div>
        </article>
    </c:forEach>
</section>

<div class="modal" id="createRoleModal" aria-hidden="true">
    <div class="modal-card" role="dialog" aria-modal="true" aria-labelledby="createRoleTitle">
        <button class="modal-close" type="button" data-close-modal>×</button>
        <p class="eyebrow">UC19 / MAIN FLOW</p><h2 id="createRoleTitle">Create role</h2>
        <form method="post" class="stack-form"><input type="hidden" name="action" value="create">
            <label><span>Role name *</span><input name="name" minlength="2" maxlength="50" required placeholder="Recruiter"></label>
            <label><span>Description</span><textarea name="description" maxlength="255" placeholder="Quyền dành cho bộ phận tuyển dụng"></textarea></label>
            <div class="form-actions"><button class="button ghost" type="button" data-close-modal>Cancel</button><button class="button primary purple-button" type="submit">Create role</button></div>
        </form>
    </div>
</div>

<div class="modal" id="editRoleModal" aria-hidden="true">
    <div class="modal-card" role="dialog" aria-modal="true" aria-labelledby="editRoleTitle">
        <button class="modal-close" type="button" data-close-modal>×</button>
        <p class="eyebrow">UC19 / UPDATE FLOW</p><h2 id="editRoleTitle">Update role</h2>
        <form method="post" class="stack-form"><input type="hidden" name="action" value="update"><input type="hidden" name="roleId" id="editRoleId">
            <label><span>Role name *</span><input name="name" id="editRoleName" minlength="2" maxlength="50" required></label>
            <label><span>Description</span><textarea name="description" id="editRoleDescription" maxlength="255"></textarea></label>
            <div class="form-actions"><button class="button ghost" type="button" data-close-modal>Cancel</button><button class="button primary purple-button" type="submit">Save changes</button></div>
        </form>
    </div>
</div>

<%@ include file="partials/footer.jspf" %>
