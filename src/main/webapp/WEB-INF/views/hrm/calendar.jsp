<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="../partials/header.jspf" %>

<section class="summary-strip purple">
    <div><span>UC16 / COMPANY CALENDAR</span><strong>Lịch công ty</strong><small>Create · Edit · Delete · Notify employees sau khi commit</small></div>
    <div class="sequence"><i>1</i><span>Form</span><b></b><i>2</i><span>Validate</span><b></b><i>3</i><span>Notify</span></div>
</section>

<section class="toolbar">
    <div class="toolbar-spacer"></div>
    <button class="button primary" type="button" data-open-modal="createEventModal">+ Create Event</button>
</section>

<section class="panel">
    <div class="panel-heading"><div><h2>Event list</h2><p>End >= Start theo BR13</p></div><span class="count-badge">${events.size()} events</span></div>
    <div class="table-wrap"><table>
        <thead><tr><th>Title</th><th>Type</th><th>Start</th><th>End</th><th>Description</th><th class="right">Actions</th></tr></thead>
        <tbody>
        <c:forEach var="event" items="${events}">
            <tr>
                <td><strong><c:out value="${event.title}"/></strong></td>
                <td><span class="role-pill">${event.eventType}</span></td>
                <td><small>${event.startAt.toLocalDate()} ${event.startAt.toLocalTime().withNano(0)}</small></td>
                <td><small>${event.endAt.toLocalDate()} ${event.endAt.toLocalTime().withNano(0)}</small></td>
                <td><c:out value="${event.description}"/></td>
                <td class="right actions">
                    <button class="icon-button" type="button" title="Edit" data-open-modal="editEventModal"
                            data-event-id="${event.id}" data-event-title="${fn:escapeXml(event.title)}"
                            data-event-type="${event.eventType}"
                            data-event-start="${event.startAt.toString().substring(0,16)}"
                            data-event-end="${event.endAt.toString().substring(0,16)}"
                            data-event-desc="${fn:escapeXml(event.description)}">✎</button>
                    <form method="post" class="inline-form" data-confirm="Xóa sự kiện này?">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="eventId" value="${event.id}">
                        <button class="icon-button danger" title="Delete">×</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty events}"><tr><td colspan="6"><div class="empty-state">Chưa có sự kiện.</div></td></tr></c:if>
        </tbody>
    </table></div>
</section>

<div class="modal" id="createEventModal" aria-hidden="true"><div class="modal-card wide" role="dialog" aria-modal="true">
    <button class="modal-close" type="button" data-close-modal>×</button>
    <p class="eyebrow">UC16 / CREATE</p><h2>Create calendar event</h2>
    <form method="post" class="form-grid"><input type="hidden" name="action" value="create">
        <label class="full"><span>Title *</span><input name="title" required maxlength="160"></label>
        <label><span>Type *</span><select name="eventType" required>
            <option value="HOLIDAY">HOLIDAY</option><option value="MEETING">MEETING</option>
            <option value="DEADLINE">DEADLINE</option><option value="OTHER">OTHER</option></select></label>
        <label><span>Start *</span><input name="startAt" type="datetime-local" required></label>
        <label><span>End *</span><input name="endAt" type="datetime-local" required></label>
        <label class="full"><span>Description</span><textarea name="description" maxlength="500"></textarea></label>
        <div class="form-actions full"><button class="button ghost" type="button" data-close-modal>Hủy</button>
            <button class="button primary" type="submit">Save</button></div>
    </form>
</div></div>

<div class="modal" id="editEventModal" aria-hidden="true"><div class="modal-card wide" role="dialog" aria-modal="true">
    <button class="modal-close" type="button" data-close-modal>×</button>
    <p class="eyebrow">UC16 / EDIT</p><h2>Edit calendar event</h2>
    <form method="post" class="form-grid"><input type="hidden" name="action" value="update">
        <input type="hidden" name="eventId" id="editEventId">
        <label class="full"><span>Title *</span><input name="title" id="editEventTitle" required maxlength="160"></label>
        <label><span>Type *</span><select name="eventType" id="editEventType" required>
            <option value="HOLIDAY">HOLIDAY</option><option value="MEETING">MEETING</option>
            <option value="DEADLINE">DEADLINE</option><option value="OTHER">OTHER</option></select></label>
        <label><span>Start *</span><input name="startAt" id="editEventStart" type="datetime-local" required></label>
        <label><span>End *</span><input name="endAt" id="editEventEnd" type="datetime-local" required></label>
        <label class="full"><span>Description</span><textarea name="description" id="editEventDesc" maxlength="500"></textarea></label>
        <div class="form-actions full"><button class="button ghost" type="button" data-close-modal>Hủy</button>
            <button class="button primary" type="submit">Save</button></div>
    </form>
</div></div>

<script>
document.querySelectorAll('[data-event-id]').forEach(btn => {
  btn.addEventListener('click', () => {
    document.getElementById('editEventId').value = btn.dataset.eventId;
    document.getElementById('editEventTitle').value = btn.dataset.eventTitle || '';
    document.getElementById('editEventType').value = btn.dataset.eventType || 'OTHER';
    document.getElementById('editEventStart').value = btn.dataset.eventStart || '';
    document.getElementById('editEventEnd').value = btn.dataset.eventEnd || '';
    document.getElementById('editEventDesc').value = btn.dataset.eventDesc || '';
  });
});
</script>

<%@ include file="../partials/footer.jspf" %>
