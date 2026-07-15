package com.group5.hrms.service;

import com.group5.hrms.model.DashboardStats;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class ReportExportService {
    public void writeExcel(DashboardStats stats, OutputStream output) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle header = workbook.createCellStyle();
            header.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            header.setFont(headerFont);

            Sheet overview = workbook.createSheet("Overview");
            addRow(overview, 0, header, "Metric", "Value");
            addRow(overview, 1, null, "Total accounts", stats.getTotalAccounts());
            addRow(overview, 2, null, "Active accounts", stats.getActiveAccounts());
            addRow(overview, 3, null, "Locked accounts", stats.getLockedAccounts());
            addRow(overview, 4, null, "Deleted accounts", stats.getDeletedAccounts());
            addRow(overview, 5, null, "Roles", stats.getTotalRoles());
            addRow(overview, 6, null, "Permissions", stats.getTotalPermissions());
            addRow(overview, 7, null, "Attendance records", stats.getAttendanceRecords());
            overview.autoSizeColumn(0); overview.autoSizeColumn(1);
            addMapSheet(workbook, "Accounts by status", stats.getAccountsByStatus(), header);
            addMapSheet(workbook, "Accounts by role", stats.getAccountsByRole(), header);
            addMapSheet(workbook, "Attendance", stats.getAttendanceByStatus(), header);
            addMapSheet(workbook, "Permissions by role", stats.getPermissionsByRole(), header);
            workbook.write(output);
        }
    }

    public void writePdf(DashboardStats stats, OutputStream output) {
        Document document = new Document();
        PdfWriter.getInstance(document, output);
        document.open();
        Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(22, 125, 90));
        Paragraph heading = new Paragraph("PeopleFlow HRMS - Admin Report", title);
        heading.setAlignment(Element.ALIGN_CENTER);
        heading.setSpacingAfter(18);
        document.add(heading);
        PdfPTable totals = new PdfPTable(2);
        totals.setWidthPercentage(80);
        addPdfRow(totals, "Total accounts", stats.getTotalAccounts());
        addPdfRow(totals, "Active accounts", stats.getActiveAccounts());
        addPdfRow(totals, "Locked accounts", stats.getLockedAccounts());
        addPdfRow(totals, "Deleted accounts", stats.getDeletedAccounts());
        addPdfRow(totals, "System roles", stats.getTotalRoles());
        addPdfRow(totals, "Permissions", stats.getTotalPermissions());
        addPdfRow(totals, "Attendance records", stats.getAttendanceRecords());
        document.add(totals);
        addPdfSection(document, "Accounts by status", stats.getAccountsByStatus());
        addPdfSection(document, "Accounts by role", stats.getAccountsByRole());
        addPdfSection(document, "Attendance statistics", stats.getAttendanceByStatus());
        addPdfSection(document, "Permission statistics", stats.getPermissionsByRole());
        document.close();
    }

    private void addMapSheet(XSSFWorkbook workbook, String name, Map<String, Integer> values, CellStyle header) {
        Sheet sheet = workbook.createSheet(name);
        addRow(sheet, 0, header, "Category", "Total");
        int row = 1;
        for (Map.Entry<String, Integer> entry : values.entrySet()) addRow(sheet, row++, null, entry.getKey(), entry.getValue());
        sheet.autoSizeColumn(0); sheet.autoSizeColumn(1);
    }

    private void addRow(Sheet sheet, int index, CellStyle style, String label, Object value) {
        Row row = sheet.createRow(index);
        row.createCell(0).setCellValue(label);
        if (value instanceof Number number) row.createCell(1).setCellValue(number.doubleValue());
        else row.createCell(1).setCellValue(String.valueOf(value));
        if (style != null) { row.getCell(0).setCellStyle(style); row.getCell(1).setCellStyle(style); }
    }

    private void addPdfSection(Document document, String title, Map<String, Integer> values) {
        Paragraph heading = new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13));
        heading.setSpacingBefore(16); heading.setSpacingAfter(6); document.add(heading);
        PdfPTable table = new PdfPTable(2); table.setWidthPercentage(80);
        for (Map.Entry<String, Integer> entry : values.entrySet()) addPdfRow(table, entry.getKey(), entry.getValue());
        if (values.isEmpty()) addPdfRow(table, "No report data found", 0);
        document.add(table);
    }

    private void addPdfRow(PdfPTable table, String label, int value) {
        table.addCell(new Phrase(label)); table.addCell(new Phrase(String.valueOf(value)));
    }
}
