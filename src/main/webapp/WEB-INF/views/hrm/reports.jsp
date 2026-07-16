<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="../partials/header.jspf" %>

<section class="summary-strip green">
    <div><span>UC-09 / REPORTS DASHBOARD</span><strong>Reports Dashboard</strong><small>Overview, Attendance, Leave, Payroll, Performance</small></div>
    <div class="sequence"><i>1</i><span>Filter</span><b></b><i>2</i><span>Analyze</span><b></b><i>3</i><span>Export</span></div>
</section>

<section class="panel" style="margin-top:22px">
    <div class="panel-heading"><div><h2>Report filters</h2><p>Department-aware filters with date correction and audit logging</p></div></div>
    <div class="tab-strip" style="padding:18px 22px 0">
        <c:forEach var="tab" items="${tabs}">
            <a class="button ${selectedTab == tab ? 'primary' : 'ghost'} small"
               href="${pageContext.request.contextPath}/hrm/reports?tab=${tab}&year=${filter.year}">
                <c:out value="${tab}"/>
            </a>
        </c:forEach>
    </div>
    <form method="get" class="form-grid" style="padding:18px 22px 22px">
        <input type="hidden" name="tab" value="${selectedTab}">
        <label><span>Year</span><input type="number" name="year" min="2000" max="2100" value="${filter.year}"></label>
        <label><span>Date from</span><input type="date" name="dateFrom" value="${filter.dateFrom}"></label>
        <label><span>Date to</span><input type="date" name="dateTo" value="${filter.dateTo}"></label>
        <label><span>Department</span><select name="departmentId">
            <option value="">All Departments</option>
            <c:forEach var="dept" items="${departments}">
                <option value="${dept.id}" ${filter.departmentId == dept.id ? 'selected' : ''}><c:out value="${dept.name}"/></option>
            </c:forEach>
        </select></label>
        <label><span>Review period</span><input name="reviewPeriod" value="<c:out value='${filter.reviewPeriod}'/>" placeholder="Optional"></label>
        <div class="form-actions full">
            <button class="button primary" type="submit">View Report</button>
            <button class="button ghost" type="submit" name="format" value="pdf">Export PDF</button>
        </div>
    </form>
</section>

<c:if test="${filter.dateRangeSwapped}">
    <div class="flash success" style="margin-top:18px"><span>!</span>Date range swapped</div>
</c:if>
<c:if test="${filter.futureDateCorrected}">
    <div class="flash error" style="margin-top:18px"><span>!</span>Future date not allowed</div>
</c:if>

<section class="metric-grid" style="margin-top:22px">
    <c:forEach var="metric" items="${report.summaryMetrics}">
        <article class="metric-card"><span class="metric-icon green">${fn:substring(metric.key,0,1)}</span>
            <div><small><c:out value="${metric.key}"/></small><strong><c:out value="${metric.value}"/></strong><p><c:out value="${report.departmentLabel}"/></p></div></article>
    </c:forEach>
</section>

<section class="content-grid" style="margin-top:22px">
    <article class="panel">
        <div class="panel-heading"><div><h2><c:out value="${report.reportTitle}"/></h2><p>Generated at ${report.generatedAt}</p></div>
            <span class="count-badge"><c:out value="${report.departmentLabel}"/></span></div>
        <c:choose>
            <c:when test="${report.empty}">
                <div class="empty-state">No data available for the selected period and department. Select a different reporting period.</div>
            </c:when>
            <c:otherwise>
                <div class="bar-chart" data-chart style="padding:18px 22px">
                    <c:forEach var="entry" items="${report.chartData}">
                        <div class="bar-row"><span><c:out value="${entry.key}"/></span><div class="bar-track"><i data-value="${entry.value}"></i></div><strong>${entry.value}</strong></div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </article>

    <article class="panel">
        <div class="panel-heading"><div><h2>Report table</h2><p>${report.totalRows} rows, page ${report.page}/${report.totalPages}</p></div></div>
        <div class="table-wrap"><table>
            <tbody>
            <c:choose>
                <c:when test="${empty report.tableRows}">
                    <tr><td><div class="empty-state">No table rows for this filter.</div></td></tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="row" items="${report.tableRows}">
                        <tr><td>
                            <c:forEach var="cell" items="${row}">
                                <span class="role-pill" style="margin:3px"><c:out value="${cell.key}"/>: <c:out value="${cell.value}"/></span>
                            </c:forEach>
                        </td></tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table></div>
        <c:if test="${report.totalPages > 1}">
            <div class="form-actions" style="padding:16px 22px">
                <c:if test="${report.page > 1}">
                    <a class="button ghost" href="?tab=${selectedTab}&year=${filter.year}&dateFrom=${filter.dateFrom}&dateTo=${filter.dateTo}&departmentId=${filter.departmentId}&reviewPeriod=${filter.reviewPeriod}&page=${report.page - 1}">Prev</a>
                </c:if>
                <c:if test="${report.page < report.totalPages}">
                    <a class="button ghost" href="?tab=${selectedTab}&year=${filter.year}&dateFrom=${filter.dateFrom}&dateTo=${filter.dateTo}&departmentId=${filter.departmentId}&reviewPeriod=${filter.reviewPeriod}&page=${report.page + 1}">Next</a>
                </c:if>
            </div>
        </c:if>
    </article>
</section>

<%@ include file="../partials/footer.jspf" %>
