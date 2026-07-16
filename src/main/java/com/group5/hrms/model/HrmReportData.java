package com.group5.hrms.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HrmReportData {
    private long reportId;
    private String reportType;
    private String reportTitle;
    private String departmentLabel = "All Departments";
    private LocalDateTime generatedAt;
    private Map<String, String> summaryMetrics = new LinkedHashMap<>();
    private Map<String, Long> chartData = new LinkedHashMap<>();
    private List<Map<String, String>> tableRows = new ArrayList<>();
    private boolean empty;
    private int totalRows;
    private int totalPages = 1;
    private int page = 1;

    public long getReportId() { return reportId; }
    public void setReportId(long reportId) { this.reportId = reportId; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getReportTitle() { return reportTitle; }
    public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }
    public String getDepartmentLabel() { return departmentLabel; }
    public void setDepartmentLabel(String departmentLabel) {
        this.departmentLabel = departmentLabel == null || departmentLabel.isBlank()
                ? "All Departments" : departmentLabel;
    }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public Map<String, String> getSummaryMetrics() { return summaryMetrics; }
    public void setSummaryMetrics(Map<String, String> summaryMetrics) { this.summaryMetrics = summaryMetrics; }
    public Map<String, Long> getChartData() { return chartData; }
    public void setChartData(Map<String, Long> chartData) { this.chartData = chartData; }
    public List<Map<String, String>> getTableRows() { return tableRows; }
    public void setTableRows(List<Map<String, String>> tableRows) { this.tableRows = tableRows; }
    public boolean isEmpty() { return empty; }
    public void setEmpty(boolean empty) { this.empty = empty; }
    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = Math.max(totalPages, 1); }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = Math.max(page, 1); }
}
