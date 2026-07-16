package com.group5.hrms.model;

import java.time.LocalDate;

public class HrmReportFilter {
    private Integer year;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String reviewPeriod;
    private Long departmentId;
    private int page = 1;
    private int pageSize = 10;
    private boolean dateRangeSwapped;
    private boolean futureDateCorrected;

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public LocalDate getDateFrom() { return dateFrom; }
    public void setDateFrom(LocalDate dateFrom) { this.dateFrom = dateFrom; }
    public LocalDate getDateTo() { return dateTo; }
    public void setDateTo(LocalDate dateTo) { this.dateTo = dateTo; }
    public String getReviewPeriod() { return reviewPeriod; }
    public void setReviewPeriod(String reviewPeriod) {
        this.reviewPeriod = reviewPeriod == null || reviewPeriod.isBlank() ? null : reviewPeriod.trim();
    }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = Math.max(page, 1); }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = Math.min(Math.max(pageSize, 1), 50); }
    public boolean isDateRangeSwapped() { return dateRangeSwapped; }
    public void setDateRangeSwapped(boolean dateRangeSwapped) { this.dateRangeSwapped = dateRangeSwapped; }
    public boolean isFutureDateCorrected() { return futureDateCorrected; }
    public void setFutureDateCorrected(boolean futureDateCorrected) { this.futureDateCorrected = futureDateCorrected; }

    public String snapshot() {
        return "year=" + year
                + ";dateFrom=" + value(dateFrom)
                + ";dateTo=" + value(dateTo)
                + ";reviewPeriod=" + value(reviewPeriod)
                + ";departmentId=" + value(departmentId);
    }

    private String value(Object value) {
        return value == null ? "ALL" : String.valueOf(value);
    }
}
