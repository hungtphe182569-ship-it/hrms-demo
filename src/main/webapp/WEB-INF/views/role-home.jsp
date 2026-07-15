<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${dashboardRole == 'Manager' ? 'Manager Dashboard' : 'Employee Dashboard'}" scope="request"/>
<%@ include file="partials/header.jspf" %>
<section class="welcome-banner role-hero ${dashboardRole.toLowerCase()}"><div><p class="eyebrow">${dashboardRole.toUpperCase()} WORKSPACE</p><h2>Xin chào, <c:out value="${sessionScope.currentUser.fullName}"/>!</h2><p><c:choose><c:when test="${dashboardRole == 'Manager'}">Quản lý đội ngũ, theo dõi chấm công và xử lý các yêu cầu.</c:when><c:otherwise>Theo dõi hồ sơ cá nhân, chấm công và các yêu cầu của bạn.</c:otherwise></c:choose></p></div><span class="role-hero-badge">${dashboardRole}</span></section>
<section class="role-dashboard-grid">
<c:choose><c:when test="${dashboardRole == 'Manager'}">
    <article class="role-action-card"><span>TM</span><div><h2>Team Members</h2><p>Xem danh sách và tình trạng nhân sự trong đội.</p></div><b>Coming next</b></article>
    <article class="role-action-card"><span>AT</span><div><h2>Team Attendance</h2><p>Theo dõi chấm công, đi muộn và vắng mặt.</p></div><b>Coming next</b></article>
    <article class="role-action-card"><span>AP</span><div><h2>Approvals</h2><p>Duyệt nghỉ phép và yêu cầu của nhân viên.</p></div><b>Coming next</b></article>
</c:when><c:otherwise>
    <article class="role-action-card"><span>PF</span><div><h2>My Profile</h2><p>Xem thông tin tài khoản và hồ sơ cá nhân.</p></div><b>Available</b></article>
    <article class="role-action-card"><span>AT</span><div><h2>My Attendance</h2><p>Theo dõi lịch sử chấm công của bạn.</p></div><b>Coming next</b></article>
    <article class="role-action-card"><span>LV</span><div><h2>Leave Requests</h2><p>Gửi và theo dõi yêu cầu nghỉ phép.</p></div><b>Coming next</b></article>
</c:otherwise></c:choose>
</section>
<section class="panel profile-summary"><div class="panel-heading"><div><h2>Phiên đăng nhập</h2><p>Thông tin được lấy từ tài khoản hiện tại</p></div><span class="status active"><i></i>${sessionScope.currentUser.status}</span></div><div class="profile-details"><div><small>Username</small><strong>@<c:out value="${sessionScope.currentUser.username}"/></strong></div><div><small>Email</small><strong><c:out value="${sessionScope.currentUser.email}"/></strong></div><div><small>Role</small><strong><c:out value="${sessionScope.currentUser.roleNames}"/></strong></div></div></section>
<%@ include file="partials/footer.jspf" %>
