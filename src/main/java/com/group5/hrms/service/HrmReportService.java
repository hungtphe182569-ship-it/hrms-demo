package com.group5.hrms.service;

import com.group5.hrms.dao.HrmReportDao;
import com.group5.hrms.model.HrmReportData;
import com.group5.hrms.model.HrmReportFilter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Set;

public class HrmReportService {
    private static final Set<String> TYPES = Set.of("OVERVIEW", "ATTENDANCE", "LEAVE", "PAYROLL", "PERFORMANCE");
    private final HrmReportDao reportDao = new HrmReportDao();

    public HrmReportData viewReport(long actorId, String type, HrmReportFilter filter) throws SQLException {
        HrmReportData data = prepareReport(type, filter);
        reportDao.recordAudit(actorId, "VIEW_REPORT", reportId(data), data.getReportType(),
                filter, resultStatus(data));
        return data;
    }

    public HrmReportData prepareReport(String type, HrmReportFilter filter) throws SQLException {
        String normalizedType = normalizeType(type);
        validateFilters(normalizedType, filter);
        HrmReportData data = reportDao.findCached(normalizedType, filter).orElse(null);
        if (data == null) {
            data = reportDao.load(normalizedType, filter);
            data.setGeneratedAt(LocalDateTime.now());
            if (!data.isEmpty()) {
                reportDao.saveReport(data, filter);
            }
        }
        paginate(data, filter);
        return data;
    }

    public void recordExport(long actorId, HrmReportData data, HrmReportFilter filter) throws SQLException {
        reportDao.recordAudit(actorId, "EXPORT_REPORT", reportId(data), data.getReportType(),
                filter, resultStatus(data));
    }

    private void validateFilters(String type, HrmReportFilter filter) {
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        if (filter.getYear() == null) filter.setYear(currentYear);
        if (filter.getYear() > currentYear) {
            filter.setYear(currentYear);
            filter.setFutureDateCorrected(true);
        }
        if (!"OVERVIEW".equals(type) && !"PERFORMANCE".equals(type)) {
            if (filter.getDateFrom() == null) filter.setDateFrom(LocalDate.of(filter.getYear(), 1, 1));
            if (filter.getDateTo() == null) filter.setDateTo(today);
        }
        if (filter.getDateFrom() != null && filter.getDateTo() != null
                && filter.getDateFrom().isAfter(filter.getDateTo())) {
            LocalDate originalFrom = filter.getDateFrom();
            filter.setDateFrom(filter.getDateTo());
            filter.setDateTo(originalFrom);
            filter.setDateRangeSwapped(true);
        }
        if (filter.getDateFrom() != null && filter.getDateFrom().isAfter(today)) {
            filter.setDateFrom(today);
            filter.setFutureDateCorrected(true);
        }
        if (filter.getDateTo() != null && filter.getDateTo().isAfter(today)) {
            filter.setDateTo(today);
            filter.setFutureDateCorrected(true);
        }
    }

    private String normalizeType(String type) {
        String normalized = type == null || type.isBlank()
                ? "OVERVIEW" : type.trim().toUpperCase(Locale.ROOT);
        if (!TYPES.contains(normalized)) throw new IllegalArgumentException("Unsupported report type");
        return normalized;
    }

    private void paginate(HrmReportData data, HrmReportFilter filter) {
        var allRows = new ArrayList<>(data.getTableRows());
        int totalRows = allRows.size();
        int pageSize = filter.getPageSize();
        int totalPages = Math.max((int) Math.ceil(totalRows / (double) pageSize), 1);
        int page = Math.min(filter.getPage(), totalPages);
        int from = Math.min((page - 1) * pageSize, totalRows);
        int to = Math.min(from + pageSize, totalRows);
        data.setTotalRows(totalRows);
        data.setTotalPages(totalPages);
        data.setPage(page);
        data.setTableRows(new ArrayList<>(allRows.subList(from, to)));
    }

    private Long reportId(HrmReportData data) {
        return data.isEmpty() || data.getReportId() == 0 ? null : data.getReportId();
    }

    private String resultStatus(HrmReportData data) {
        return data.isEmpty() ? "NO_DATA" : "SUCCESS";
    }
}
